/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.erp.ledger;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The ledger implements a ledger with a chart of accounts and a set of transactions. It provides the logic for the
 * automatic processing of VAT amounts and related bookings to the VAT related accounts.
 */
public class Ledger {
    private static final Logger log = LoggerFactory.getLogger(Ledger.class);
    private final List<Account> accounts;
    private final List<Transaction> journal;
    private final List<AccountEntry> vatEntries;

    public Ledger() {
        accounts = new ArrayList<>();
        journal = new ArrayList<>();
        vatEntries = new ArrayList<>();
    }

    public List<Account> assets() {
        return accounts.stream().filter(o -> Account.AccountGroup.ASSETS.equals(o.group())).collect(Collectors.toList());
    }

    public List<Account> liabilities() {
        return accounts.stream().filter(o -> Account.AccountGroup.LIABILITIES.equals(o.group())).collect(Collectors.toList());
    }

    public List<Account> profitAndLoss() {
        return accounts.stream().filter(o -> Account.AccountGroup.PROFITS_AND_LOSSES.equals(o.group())).collect(Collectors.toList());
    }

    public List<Transaction> transactions(LocalDate from, LocalDate to) {
        return journal.stream().filter(o -> (o.date().isAfter(from) || o.date().equals(from)) && (o.date().isBefore(to) || o.date().isEqual(to)))
                .collect(Collectors.toList());
    }

    public Optional<Account> getAccountBy(String id) {
        return accounts.stream().filter(o -> id.equals(o.id())).findAny();
    }

    public List<Account> getAccountsOwnedBy(String id) {
        return accounts.stream().filter(o -> id.equals(o.ownedBy())).collect(Collectors.toList());
    }

    public void add(@NotNull Account account) {
        accounts.add(account);
    }

    /**
     * Adds a transaction to the ledger and the referenced accounts. A warning message is written to the log file if one of the
     * involved accounts is not registered in the ledger. The involved accounts cannot be aggregate accounts.
     *
     * @param transaction transaction to add to the ledger
     */
    public void add(@NotNull Transaction transaction) {
        journal.add(transaction);
        transaction.debitSplits().forEach(this::bookEntry);
        transaction.creditSplits().forEach(this::bookEntry);
        if (Objects.nonNull(transaction.vatCode())) {
            switch (transaction.vatCode()) {
                case "F1":
                    BigDecimal vatAmount = transaction.amount().multiply(new BigDecimal("0.061"));
                    bookEntry(new AccountEntry("2201", transaction.date(), vatAmount, transaction.reference(), true));
                    break;
            }
        }
    }

    public void validate() {
        accounts.stream().filter(Account::isAggregate)
                .forEach(o -> o.updateAggregatedAccounts(accounts.stream().filter(sub -> o.id().equals(sub.ownedBy())).collect(Collectors.toList())));
        accounts.stream().filter(Account::isAggregate).filter(o -> o.aggregatedAccounts().isEmpty())
                .forEach(o -> log.error("Aggregate account wrongly defined {}", o.id()));
    }

    private void bookEntry(@NotNull AccountEntry entry) {
        Optional<Account> account = getAccountBy(entry.account());
        if (!account.isPresent()) {
            log.error("account {} for entry with amount {} booked {} is undefined", entry.account(), entry.amount(), entry.date());
        }
        account.ifPresent(o -> o.addEntry(entry));
    }

    // region VAT-computations

    public BigDecimal computeVatSales(LocalDate from, LocalDate to) {
        return transactions(from, to).stream().filter(o -> "F1".equals(o.vatCode())).map(Transaction::amount).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal computeDueVat(LocalDate from, LocalDate to) {
        Optional<Account> account = getAccountBy("2201");
        return account.isPresent() ? account.get().getEntriesFor(from, to).stream().filter(AccountEntry::isDebit).map(AccountEntry::amount)
                .reduce(BigDecimal::add).orElse(BigDecimal.ZERO) : BigDecimal.ZERO;
    }

    // endregion
}
