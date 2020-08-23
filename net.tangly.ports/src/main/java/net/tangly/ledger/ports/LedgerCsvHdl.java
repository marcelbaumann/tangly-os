/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.ledger.ports;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.tangly.bus.core.Tag;
import net.tangly.bus.ledger.Account;
import net.tangly.bus.ledger.AccountEntry;
import net.tangly.bus.ledger.Ledger;
import net.tangly.bus.ledger.Transaction;
import net.tangly.commons.lang.Strings;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ledger CSV handler can import ledger plans and transactions journal as exported by the banana 8 ledger application. The import assumes that the
 * program language and ledger template use English. The ledger structure CSV file has the columns id, account kind, account group, description, owned
 * by group id. The handler reads a ledger structure description from a CSV file and update a full ledger structure. The transaction CSV file has the
 * columns date, doc, description, account debit, account credit, amount, defineVat code.
 * <p>The accounting program used to generate the export files is <a href="https://www.banana.ch/">banana</a></p>
 */
public class LedgerCsvHdl {
    private static final String AMOUNT = "Amount";
    private static final String SECTION = "Section";
    private static final String GROUP = "Group";
    private static final String ACCOUNT = "Account";
    private static final String DATE = "Date";
    private static final String DESCRIPTION = "Description";
    private static final String DOC = "Doc";
    private static final String BCLASS = "BClass";
    private static final String GR = "Gr";
    private static final String ACCOUNT_DEBIT = "AccountDebit";
    private static final String ACCOUNT_CREDIT = "AccountCredit";
    private static final String VAT_CODE = "VatCode";

    private static final Logger log = LoggerFactory.getLogger(LedgerCsvHdl.class);

    private final Ledger ledger;

    /**
     * Constructor of the class.
     *
     * @param ledger ledger to use when importing data from CSV files.
     */
    public LedgerCsvHdl(@NotNull Ledger ledger) {
        this.ledger = ledger;
    }

    /**
     * Returns the ledger updated through the handler.
     *
     * @return the ledger used by the handler
     */
    public Ledger ledger() {
        return ledger;
    }

    public void importLedgerStructureFromBanana(@NotNull Path path) {
        try (Reader in = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            Account.AccountGroup currentSection = null;
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                String section = record.get(SECTION);
                if ((section != null) && !section.isEmpty()) {
                    currentSection = ofGroup(section);
                }
                String accountGroup = record.get(GROUP);
                String id = record.get(ACCOUNT);
                String text = record.get(DESCRIPTION);
                String accountKind = record.get(BCLASS);
                String ownedByGroupId = record.get(GR);
                if (isRecordPlanRelevant(text, id, accountGroup)) {
                    if (Strings.isNullOrEmpty(accountGroup)) {
                        ledger.add(Account.of(Integer.parseInt(id), ofKInd(accountKind), text, ownedByGroupId));
                    } else {
                        ledger.add(Account.of(accountGroup, currentSection, text, ownedByGroupId));
                    }
                }
                record = records.hasNext() ? records.next() : null;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Imports the transaction list exported from banana 8 software as CSV file with header.
     *
     * @param path path to the file containing the list of transactions
     */
    public void importTransactionsLedgerFromBanana(@NotNull Path path) {
        try (Reader in = new BufferedReader(new FileReader(path.toFile(), StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(in).iterator();
            CSVRecord record = records.hasNext() ? records.next() : null;
            while (record != null) {
                String date = record.get(DATE);
                String reference = record.get(DOC);
                String text = record.get(DESCRIPTION);
                String debitAccount = record.get(ACCOUNT_DEBIT);
                String creditAccount = record.get(ACCOUNT_CREDIT);
                String[] debitValues = debitAccount.split("-");
                String[] creditValues = creditAccount.split("-");
                String amount = record.get(AMOUNT);
                String vatCode = record.get(VAT_CODE);
                try {
                    Transaction transaction;
                    if (isPartOfSplitTransaction(record)) {
                        List<AccountEntry> splits = new ArrayList<>();
                        record = importSplits(records, splits);
                        transaction =
                                new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                                        new BigDecimal(amount), splits, text, reference
                                );
                    } else {
                        transaction =
                                new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                                        new BigDecimal(amount), text, reference
                                );
                        record = records.hasNext() ? records.next() : null;
                    }
                    defineProject(transaction.debitSplits(), debitAccount);
                    defineProject(transaction.creditSplits(), creditAccount);
                    defineVat(transaction.creditSplits(), vatCode);
                    ledger.add(transaction);
                } catch (NumberFormatException e) {
                    log.error("not a legal amount {}", amount, e);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CSVRecord importSplits(@NotNull Iterator<CSVRecord> records, List<AccountEntry> splits) {
        CSVRecord record = records.hasNext() ? records.next() : null;
        while (isPartOfSplitTransaction(record)) {
            String date = record.get(DATE);
            String text = record.get(DESCRIPTION);
            String debitAccount = record.get(ACCOUNT_DEBIT);
            String creditAccount = record.get(ACCOUNT_CREDIT);
            String splitAmount = record.get(AMOUNT);
            AccountEntry entry = null;
            if (!Strings.isNullOrEmpty(debitAccount)) {
                String[] debitValues = debitAccount.split("-");
                entry = AccountEntry.debit(debitValues[0], date, splitAmount, text);
            } else if (!Strings.isNullOrEmpty(creditAccount)) {
                String[] creditValues = creditAccount.split("-");
                entry = AccountEntry.credit(creditValues[0], date, splitAmount, text);
            }
            splits.add(entry);
            record = records.hasNext() ? records.next() : null;
        }
        return record;
    }

    /**
     * Returns true if the record is relevant for the ledger plan, meaning it has a description and either an account identifier not starting with an
     * semicolon or a group with an identifier different from 0.
     *
     * @return flag indicating if hte record is relevant for the ledger plan or not
     */
    private static boolean isRecordPlanRelevant(String description, String accountId, String groupId) {
        return !Strings.isNullOrEmpty(description) && ((!Strings.isNullOrEmpty(accountId) && !accountId.startsWith(":")) ||
                (!Strings.isNullOrEmpty(groupId) && !groupId.equalsIgnoreCase("0")));
    }

    // potential strategy pattern
    private static final String F1 = "F1";
    private static final String F3 = "F3";
    private static final BigDecimal VAT_F1_VALUE = new BigDecimal("0.08");
    private static final BigDecimal VAT_F3_VALUE = new BigDecimal("0.077");
    private static final BigDecimal VAT_F1_DUE_VALUE = new BigDecimal("0.061");
    private static final BigDecimal VAT_F3_DUE_VALUE = new BigDecimal("0.065");

    private static void defineVat(@NotNull List<AccountEntry> entries, String code) {
        if (!Strings.isNullOrEmpty(code)) {
            switch (code) {
                case F1 -> entries.forEach(o -> {
                    o.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F1_VALUE.toString()));
                    o.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F1_DUE_VALUE.toString()));
                });
                case F3 -> entries.forEach(o -> {
                    o.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F3_VALUE.toString()));
                    o.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F3_DUE_VALUE.toString()));
                });
                default -> log.info("Unknown VAT code in CSV file {}", code);
            }
        }
    }

    private static void defineProject(@NotNull List<AccountEntry> entries, String code) {
        if (!Strings.isNullOrEmpty(code)) {
            String[] values = code.split("-");
            if (values.length > 1) {
                entries.forEach(o -> o.add(new Tag(AccountEntry.FINANCE, AccountEntry.PROJECT, values[1])));
            }
        }
    }

    private static Account.AccountGroup ofGroup(String accountGroup) {
        Account.AccountGroup group;
        try {
            group = switch (Integer.parseInt(accountGroup)) {
                case 1 -> Account.AccountGroup.ASSETS;
                case 2 -> Account.AccountGroup.LIABILITIES;
                case 3 -> Account.AccountGroup.EXPENSES;
                case 4 -> Account.AccountGroup.PROFITS_AND_LOSSES;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
        return group;
    }

    private static Account.AccountKind ofKInd(String accountKind) {
        Account.AccountKind kind;
        try {
            kind = switch (Integer.parseInt(accountKind)) {
                case 1 -> Account.AccountKind.ASSET;
                case 2 -> Account.AccountKind.LIABILITY;
                case 3 -> Account.AccountKind.EXPENSE;
                case 4 -> Account.AccountKind.INCOME;
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
        return kind;
    }

    private static boolean isPartOfSplitTransaction(CSVRecord record) {
        return (record != null) && (Strings.isNullOrEmpty(record.get(ACCOUNT_DEBIT)) || Strings.isNullOrEmpty(record.get(ACCOUNT_CREDIT)));
    }
}
