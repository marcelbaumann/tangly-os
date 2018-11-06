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

package net.tangly.erp.ledger.ports;


import net.tangly.commons.utilities.AsciiDocHelper;
import net.tangly.erp.ledger.Account;
import net.tangly.erp.ledger.AccountEntry;
import net.tangly.erp.ledger.Ledger;
import net.tangly.erp.ledger.Transaction;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

/**
 * A complete accounting report for a specific time interval. We suggest you use year, half-year and quarter ports. The output format is AsciiDoc.
 */
public class ClosingReportAsciiDoc {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClosingReportAsciiDoc.class);
    private final Ledger ledger;
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    /**
     * Constructor of the closing report. Multiple ports with variable reporting period length can be generated with
     * the same instance.
     *
     * @param ledger ledger containing all the accounting information for the ports
     */
    public ClosingReportAsciiDoc(Ledger ledger) {
        this.ledger = ledger;
    }

    public void create(LocalDate from, LocalDate to, Path reportPath) {
        try (PrintWriter writer = new PrintWriter(reportPath.toFile(), StandardCharsets.UTF_8)) {
            create(from, to, writer);
        } catch (Exception e) {
            log.error("Error during reporting", e);
        }
    }

    public void create(LocalDate from, LocalDate to, PrintWriter writer) {
        final AsciiDocHelper helper = new AsciiDocHelper(writer);
        helper.header("Balance Sheet", 1);
        generateResultTableFor(helper, ledger.assets(), from, to, "Assets");
        generateResultTableFor(helper, ledger.liabilities(), from, to, "Liabilities");
        generateResultTableFor(helper, ledger.profitAndLoss(), from, to, "Profits and Losses");

        helper.tableHeader("VAT", "100, >25, >25 , >25", "Period", "Turnover", "VAT", "Due VAT");
        addVatRows(helper, from.getYear());
        if (from.getYear() != to.getYear()) {
            addVatRows(helper, to.getYear());
        }
        helper.tableEnd();

        helper.header("Transactions", 1);
        helper.tableHeader("Transactions", "20, 20, 70 , 15, 15, >20, >10", "Date", "Voucher", "Description", "Debit", "Credit", "Amount", "VAT");
        ledger.transactions(from, to).forEach(o -> createTransactionRow(helper, o));
        helper.tableEnd();
    }

    private void generateResultTableFor(AsciiDocHelper helper, List<Account> accounts, LocalDate from, LocalDate to, String category) {
        helper.header(category, 2);
        helper.tableHeader(category, "25, 150, 20, >25, >25", "Account", "Description", "Kind", "Balance", "Initial Balance");
        accounts.forEach(o -> createBalanceRow(helper, o, from, to));
        helper.tableEnd();
    }

    /**
     * Creates the VAT results table rows. For each half year - the period used by the Swiss government as VAT payment period for small companies
     * using the net tax rate VAT variant - and full year we provide the turnover, the invoiced VAT and the VAT tax to pay to the government.
     *
     * @param helper asciidoc helper to write the report
     * @param year   the year to raws shall be computed
     */
    private void addVatRows(AsciiDocHelper helper, int year) {
        LocalDate periodStart = LocalDate.of(year, 1, 1);
        LocalDate periodEnd = LocalDate.of(year, 6, 30);
        BigDecimal earningH1 = ledger.computeVatSales(periodStart, periodEnd);
        BigDecimal vatH1 = ledger.computeVat(periodStart, periodEnd);
        BigDecimal vatDueH1 = ledger.computeDueVat(periodStart, periodEnd);
        periodStart = LocalDate.of(year, 7, 1);
        periodEnd = LocalDate.of(year, 12, 31);
        BigDecimal earningH2 = ledger.computeVatSales(periodStart, periodEnd);
        BigDecimal vatH2 = ledger.computeVat(periodStart, periodEnd);
        BigDecimal vatDueH2 = ledger.computeDueVat(periodStart, periodEnd);

        helper.tableRow("First Half Year " + year, format(earningH1), format(vatH1), format(vatDueH1));
        helper.tableRow("Second Half Year " + year, format(earningH2), format(vatH2), format(vatDueH2));
        helper.tableRow("Totals Year " + year, format(earningH1.add(earningH2)), format(vatH1.add(vatH2)), format(vatDueH1.add(vatDueH2)));
    }

    private void createTransactionRow(AsciiDocHelper helper, Transaction transaction) {
        if (transaction.isSplit()) {
            if (transaction.debitSplits().size() > 1) {
                helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.description(), "", transaction.creditAccount(),
                        format(transaction.amount()), "-");
                for (AccountEntry entry : transaction.debitSplits()) {
                    helper.tableRow("", "", "", entry.account(), "", format(transaction.amount()), "-");
                }
            } else {
                helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.description(), transaction.debitAccount(), "",
                        format(transaction.amount()), "-");
                for (AccountEntry entry : transaction.debitSplits()) {
                    helper.tableRow("", "", "", "", entry.account(), format(transaction.amount()), "-");
                }
            }
        } else {
            helper.tableRow(transaction.date().toString(), transaction.reference(), transaction.description(), transaction.debitAccount(),
                    transaction.creditAccount(), format(transaction.amount()), vat(transaction.creditSplits().get(0)));
        }
    }

    private String vat(AccountEntry entry) {
        return entry.getVat().map(o -> format(o.multiply(new BigDecimal(100))) + "%").orElse("");
    }

    private void createBalanceRow(AsciiDocHelper helper, Account account, LocalDate from, LocalDate to) {
        BigDecimal fromBalance = account.balance(from.minusDays(1));
        BigDecimal toBalance = account.balance(to);

        if (BigDecimal.ZERO.equals(fromBalance) && BigDecimal.ZERO.equals(toBalance)) {
            return;
        }
        String accountId = account.id().startsWith("E") ? AsciiDocHelper.bold(account.id()) : account.id();
        String description = account.id().startsWith("E") ? AsciiDocHelper.bold(account.description()) : account.description();
        helper.tableRow(accountId, description, account.isAggregate() ? "" : account.kind().name().toLowerCase(), format(toBalance),
                format(fromBalance));
    }

    private String format(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) >= 0) {

            return df.format(value);
        } else {
            return "[red]#" + df.format(value) + "#";
        }
    }

}
