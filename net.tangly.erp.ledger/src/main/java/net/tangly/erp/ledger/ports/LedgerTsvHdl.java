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

package net.tangly.erp.ledger.ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

import net.tangly.commons.lang.Strings;
import net.tangly.commons.logger.EventData;
import net.tangly.core.Tag;
import net.tangly.erp.ledger.domain.Account;
import net.tangly.erp.ledger.domain.AccountEntry;
import net.tangly.erp.ledger.domain.Transaction;
import net.tangly.erp.ledger.services.LedgerRealm;
import net.tangly.erp.ports.TsvHdl;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * The ledger CSV handler can import ledger plans and transactions journal. The import assumes that the program language and ledger template use English.
 * <p>The ledger structure CSV file has the columns id, account kind, account group, description, owned by group id. The handler reads a ledger structure
 * description from a CSV file and update a full ledger structure. </p>
 * <p>The transaction CSV file has the columns date, doc, description, account debit, account credit, amount, defineVat code, and date expected.</p>
 * <p>The accounting program used to generate the export files is <a href="https://www.banana.ch/">banana</a></p>
 */
public class LedgerTsvHdl {
    private static final String AMOUNT = "Amount";
    private static final String SECTION = "Section";
    private static final String GROUP = "Group";
    private static final String ACCOUNT = "Account";
    private static final String DATE = "Date";
    private static final String DESCRIPTION = "Description";
    private static final String DOC = "Doc";
    private static final String BCLASS = "BClass";
    private static final String GR = "Gr";
    private static final String CURRENCY = "Currency";
    private static final String ACCOUNT_DEBIT = "AccountDebit";
    private static final String ACCOUNT_CREDIT = "AccountCredit";
    private static final String VAT_CODE = "VatCode";
    private static final String DATE_EXPECTED = "DateExpected";

    private static final Logger logger = LogManager.getLogger();
    private final LedgerRealm ledger;

    @Inject
    public LedgerTsvHdl(@NotNull LedgerRealm ledger) {
        this.ledger = ledger;
    }

    public LedgerRealm ledger() {
        return ledger;
    }

    /**
     * The file structure provided by the external program is:
     * <dl>
     *     <dt>Section</dt><dd>Defines the section the account is belonging to: 1 for assets, 2 for liabilities, 4 for profits and losses. A star indicates a
     *     separation line with a title in the document.</dd>
     *     <dt>Group</dt><dd>if the line has a group value, it is an aggregated account and therefore has no account identifier.</dd>
     *     <dt>Account</dt><dd>if the line has a account value, it is an account and the value is the account identifier. It has no group value.</dd>
     *     <dt>Description</dt><dd>the description is the human readable name of the account or aggregated account.</dd>
     *     <dt>BClass</dt><dd>Defines the type of account: 1 for assets, 2 for liabilities, 3 for expenses, and 4 for incomes.</dd>
     *     <dt>Gr</dt><dd>Defines the aggregate account owning the account.</dd>
     *     <dt>Currency</dt><dd>Defines the currency of the account.</dd>
     * </dl>
     *
     * @param path path to the file containing the chart of accounts
     * @see #exportChartOfAccounts(Path)
     */
    public void importChartOfAccounts(@NotNull Path path) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            int counter = 0;
            Account.AccountGroup currentSection = null;
            for (CSVRecord record : TsvHdl.FORMAT.parse(in)) {
                String section = record.get(SECTION);
                if (!Strings.isNullOrBlank(section)) {
                    currentSection = ofGroup(section);
                }
                String accountGroup = record.get(GROUP);
                String id = record.get(ACCOUNT);
                String text = record.get(DESCRIPTION);
                String accountKind = record.get(BCLASS);
                String ownedByGroupId = record.get(GR);
                String currency = record.get(CURRENCY);
                if (Strings.isNullOrBlank(currency)) {
                    currency = "CHF";
                }
                if (isRecordPlanRelevant(text, id, accountGroup)) {
                    Account account;
                    if (Strings.isNullOrEmpty(accountGroup)) {
                        account = Account.of(Integer.parseInt(id), ofKind(accountKind), currency, text, ownedByGroupId);
                    } else {
                        account = Account.of(accountGroup, currentSection, currency, text, ownedByGroupId);
                    }
                    ledger.add(account);
                    ++counter;
                    EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, Account.class.getSimpleName() + " imported",
                        Map.of("filename", path, "object", account));

                }
                EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, Account.class.getSimpleName() + " imported objects",
                    Map.of("filename", path, "count", counter));
            }
        } catch (IOException e) {
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Entities not imported from", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the chart of accounts to the provided file.
     *
     * @param path path to the file
     * @see #importChartOfAccounts(Path)
     */
    public void exportChartOfAccounts(@NotNull Path path) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), TsvHdl.FORMAT)) {
            out.printRecord(SECTION, GROUP, ACCOUNT, DESCRIPTION, BCLASS, GR, CURRENCY);
            int counter = 0;
            Account.AccountGroup group = null;
            for (Account account : ledger.accounts().items()) {
                if ((account.group() != null) && (account.group() != group)) {
                    group = account.group();
                    writeSection(group, out);
                }
                out.print(null);
                if (account.isAggregate()) {
                    out.print(account.id());
                    out.print(null);
                } else {
                    out.print(null);
                    out.print(account.id());
                }
                out.print(account.name());
                out.print(ofKind(account.kind()));
                out.print(account.ownedBy());
                out.print(account.isAggregate() ? null : account.currency().getCurrencyCode());
                out.println();
                if (account.isAggregate()) {
                    out.println();
                }
                ++counter;
                EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.SUCCESS, Account.class.getSimpleName() + " exported to charter of accounts",
                    Map.of("filename", path, "entity", account));
            }
            EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.INFO, "exported to charter of accounts",
                Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Accounts exported to", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Imports the transaction list exported from a CSV file with header.
     * <p>The file structure provided by the external program is:</p>
     * <dl>
     *     <dt>date</dt><dd>date of the transaction encoded as ISO standard YYYY-MM-DD.</dd>
     *     <dt>reference</dt><dd>optional reference to a document associated with the transaction.</dd>
     *     <dt>text</dt><dd>text describing the transaction.</dd>
     *     <dt>account debit</dt><dd>account debited. If empty indicates a split transaction.</dd>
     *     <dt>account credit</dt><dd>account credited. If empty indicates a split transaction.</dd>
     *     <dt>amount</dt><dd>amount of the transaction.</dd>
     *     <dt>VAT code</dt><dd>optional VAT code associated with the transaction or the split part of a transaction.</dd>
     * </dl>
     *
     * @param path path to the file containing the list of transactions
     * @see #exportJournal(Path, LocalDate, LocalDate)
     */
    public void importJournal(@NotNull Path path) {
        try (Reader in = new BufferedReader(Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            Iterator<CSVRecord> records = TsvHdl.FORMAT.parse(in).iterator();
            int counter = 0;
            var record = records.hasNext() ? records.next() : null;
            while (record != null) {
                String date = record.get(DATE);
                String debitAccount = record.get(ACCOUNT_DEBIT);
                String creditAccount = record.get(ACCOUNT_CREDIT);
                String[] debitValues = debitAccount.split("-");
                String[] creditValues = creditAccount.split("-");
                String amount = record.get(AMOUNT);
                String dateExpected = record.get(DATE_EXPECTED);
                Transaction transaction = null;
                if (isPartOfSplitTransaction(record)) {
                    String description = record.get(DESCRIPTION);
                    String reference = record.get(DOC);
                    List<AccountEntry> splits = new ArrayList<>();
                    record = importSplits(records, splits);
                    try {
                        transaction = new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                            new BigDecimal(amount), splits, description, reference);
                    } catch (NumberFormatException e) {
                        logger.atError().withThrowable(e).log("{}: not a legal amount {}", date, amount);
                    }
                } else {
                    try {
                        transaction = new Transaction(LocalDate.parse(date), Strings.emptyToNull(debitValues[0]), Strings.emptyToNull(creditValues[0]),
                            Strings.isNullOrEmpty(amount) ? BigDecimal.ZERO : new BigDecimal(amount), record.get(DESCRIPTION), record.get(DOC));
                        defineVat(transaction.creditSplits().get(0), record.get(VAT_CODE));
                    } catch (NumberFormatException e) {
                        logger.atError().withThrowable(e).log("{}: not a legal amount {}", date, amount);
                    }
                    record = records.hasNext() ? records.next() : null;
                }
                if (transaction != null) {
                    if (transaction.debitSplits().size() == 1) {
                        defineSegments(transaction.debitSplits().get(0), debitAccount);
                    }
                    if (transaction.creditSplits().size() == 1) {
                        defineSegments(transaction.creditSplits().get(0), creditAccount);
                    }
                    ledger.addVat(transaction);
                    ++counter;
                    EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.SUCCESS, Transaction.class.getSimpleName() + " imported to journal",
                        Map.of("filename", path, "entity", transaction));
                }
            }
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.INFO, "imported to journal", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            EventData.log(EventData.IMPORT, TsvHdl.MODULE, EventData.Status.FAILURE, "Transactions imported from", Map.of("filename", path), e);
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Exports the transactions with a date in the given time interval.
     *
     * @param path path to the file containing the list of transactions
     * @param from start of the time interval of relevant transactions
     * @param to   end of the time interval of relevant transactions
     */
    public void exportJournal(@NotNull Path path, LocalDate from, LocalDate to) {
        try (CSVPrinter out = new CSVPrinter(Files.newBufferedWriter(path, StandardCharsets.UTF_8), TsvHdl.FORMAT)) {
            out.printRecord(DATE, DOC, DESCRIPTION, ACCOUNT_DEBIT, ACCOUNT_CREDIT, AMOUNT, VAT_CODE, DATE_EXPECTED);
            int counter = 0;
            for (Transaction transaction : ledger.transactions(from, to)) {
                Optional<Tag> vat = transaction.creditSplits().get(0).findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                out.printRecord(transaction.date(), transaction.reference(), transaction.text(), accountCompositeId(transaction.debit()),
                    accountCompositeId(transaction.credit()), transaction.amount(), vat.map(Tag::value).orElse(null), null);
                if (transaction.isSplit()) {
                    if (transaction.debitAccount() == null) {
                        for (AccountEntry entry : transaction.debitSplits()) {
                            vat = entry.findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                            out.printRecord(entry.date(), transaction.reference(), entry.text(), accountCompositeId(entry), null, entry.amount(),
                                vat.map(Tag::value).orElse(null), null);
                        }
                    } else if (transaction.creditAccount() == null) {
                        for (AccountEntry entry : transaction.creditSplits()) {
                            vat = entry.findBy(AccountEntry.FINANCE, AccountEntry.VAT_FLAG);
                            out.printRecord(entry.date(), transaction.reference(), entry.text(), null, accountCompositeId(entry), entry.amount(),
                                vat.map(Tag::value).orElse(null), null);
                        }
                    }
                }
                ++counter;
            }
            EventData.log(EventData.EXPORT, TsvHdl.MODULE, EventData.Status.INFO, "exported from journal", Map.of("filename", path, "counter", counter));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static CSVRecord importSplits(@NotNull Iterator<CSVRecord> records, List<AccountEntry> splits) {
        var record = records.hasNext() ? records.next() : null;
        while (isPartOfSplitTransaction(record)) {
            String debitAccount = record.get(ACCOUNT_DEBIT);
            String creditAccount = record.get(ACCOUNT_CREDIT);
            AccountEntry entry = null;
            if (!Strings.isNullOrEmpty(debitAccount)) {
                String[] debitValues = debitAccount.split("-");
                entry = AccountEntry.debit(debitValues[0], record.get(DATE), record.get(AMOUNT), record.get(DESCRIPTION));
                defineSegments(entry, debitAccount);
                defineVat(entry, record.get(VAT_CODE));
            } else if (!Strings.isNullOrEmpty(creditAccount)) {
                String[] creditValues = creditAccount.split("-");
                entry = AccountEntry.credit(creditValues[0], record.get(DATE), record.get(AMOUNT), record.get(DESCRIPTION));
                defineSegments(entry, creditAccount);
                defineVat(entry, record.get(VAT_CODE));
            }
            splits.add(entry);
            record = records.hasNext() ? records.next() : null;
        }
        return record;
    }

    /**
     * Returns true if the record is relevant for the ledger plan, meaning it has a description and either an account identifier not starting with an semicolon
     * or a group with an identifier different from 0.
     *
     * @return flag indicating if hte record is relevant for the ledger plan or not
     */
    private static boolean isRecordPlanRelevant(String description, String accountId, String groupId) {
        return !Strings.isNullOrEmpty(description) &&
            ((!Strings.isNullOrEmpty(accountId) && !accountId.startsWith(":")) || (!Strings.isNullOrEmpty(groupId) && !groupId.equalsIgnoreCase("0")));
    }

    // potential strategy pattern
    private static final String F1 = "F1";
    private static final String F3 = "F3";
    private static final BigDecimal VAT_F1_VALUE = new BigDecimal("0.08");
    private static final BigDecimal VAT_F3_VALUE = new BigDecimal("0.077");
    private static final BigDecimal VAT_F1_DUE_VALUE = new BigDecimal("0.061");
    private static final BigDecimal VAT_F3_DUE_VALUE = new BigDecimal("0.065");

    private static void defineVat(@NotNull AccountEntry entry, String code) {
        if (!Strings.isNullOrEmpty(code)) {
            switch (code) {
                case F1 -> {
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F1_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F1_DUE_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_FLAG, F1));
                }
                case F3 -> {
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT, VAT_F3_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_DUE, VAT_F3_DUE_VALUE.toString()));
                    entry.add(Tag.of(AccountEntry.FINANCE, AccountEntry.VAT_FLAG, F3));
                }
                default -> logger.info("Unknown VAT code in CSV file {}", code);
            }
        }
    }

    private static void defineSegments(@NotNull AccountEntry entry, String code) {
        if (!Strings.isNullOrEmpty(code)) {
            var values = code.split("-");
            if (values.length > 1) {
                entry.add(new Tag(AccountEntry.FINANCE, AccountEntry.PROJECT, values[1]));
            }
            if (values.length > 2) {
                entry.add(new Tag(AccountEntry.FINANCE, AccountEntry.SEGMENT, values[2]));
            }
        }
    }

    private static String accountCompositeId(AccountEntry entry) {
        if (entry != null) {
            Optional<Tag> project = entry.findBy(AccountEntry.FINANCE, AccountEntry.PROJECT);
            Optional<Tag> segment = entry.findBy(AccountEntry.FINANCE, AccountEntry.SEGMENT);
            return entry.accountId() + (project.map(tag -> "-" + tag.value()).orElse("")) + (segment.map(value -> "-" + value.value()).orElse(""));
        } else {
            return null;
        }
    }

    private static Account.AccountGroup ofGroup(String accountGroup) {
        if (!accountGroup.contains("*")) {
            try {
                return switch (Integer.parseInt(accountGroup)) {
                    case 1 -> Account.AccountGroup.ASSETS;
                    case 2 -> Account.AccountGroup.LIABILITIES;
                    case 3 -> Account.AccountGroup.EXPENSES;
                    case 4 -> Account.AccountGroup.PROFITS_AND_LOSSES;
                    default -> null;
                };
            } catch (NumberFormatException e) {
                logger.atError().withThrowable(e).log("Format error for account group {}", accountGroup);
            }
        }
        return null;
    }

    private static String ofGroup(Account.AccountGroup group) {
        return switch (group) {
            case ASSETS -> "1";
            case LIABILITIES -> "2";
            case EXPENSES -> "3";
            case PROFITS_AND_LOSSES -> "4";
        };
    }

    private static Account.AccountKind ofKind(String accountKind) {
        try {
            return Strings.isNullOrBlank(accountKind) ? Account.AccountKind.AGGREGATE : switch (Integer.parseInt(accountKind)) {
                case 1 -> Account.AccountKind.ASSET;
                case 2 -> Account.AccountKind.LIABILITY;
                case 3 -> Account.AccountKind.EXPENSE;
                case 4 -> Account.AccountKind.INCOME;
                default -> Account.AccountKind.AGGREGATE;
            };
        } catch (NumberFormatException e) {
            logger.atError().withThrowable(e).log("Format error for account kind {}", accountKind);
            return null;
        }
    }

    private static String ofKind(Account.AccountKind kind) {
        return switch (kind) {
            case ASSET -> "1";
            case LIABILITY -> "2";
            case EXPENSE -> "3";
            case INCOME -> "4";
            case AGGREGATE -> null;
        };
    }

    private static void writeSection(Account.AccountGroup group, CSVPrinter out) throws IOException {
        switch (group) {
            case ASSETS -> {
                out.printRecord("*", null, null, "BALANCE SHEET", null, null, null);
                out.println();
                out.printRecord(ofGroup(group), null, null, "ASSETS", null, null, null);
                out.println();
            }
            case LIABILITIES -> {
                out.println();
                out.printRecord(ofGroup(group), null, null, "LIABILITIES", null, null, null);
                out.println();
            }
            case PROFITS_AND_LOSSES -> {
                out.println();
                out.printRecord("*", null, null, "PROFIT & LOSS STATEMENT", null, null, null);
                out.printRecord(ofGroup(group), null, null, null, null, null, null);
            }
        }
    }

    private static boolean isPartOfSplitTransaction(CSVRecord record) {
        return (record != null) && (Strings.isNullOrEmpty(record.get(ACCOUNT_DEBIT)) || Strings.isNullOrEmpty(record.get(ACCOUNT_CREDIT)));
    }
}
