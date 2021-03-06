/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.ledger.services;

import java.time.LocalDate;

/**
 * Defines the export port for the ledger bounded domain. It is the secondary port in DDD terminology.
 */

public interface LedgerPort {
    /**
     * Exports a ledger report with profits and losses, and assets and liabilities information for a specific time interval.
     *
     * @param name name of the document
     * @param from start of the time interval relevant for the report
     * @param to   end of the time interval relevant for the report
     * @param withVat flag indicating if VAT information should be part of the report
     * @param withTransactions flag indicating if the transactions should be part of the report
     */
     void exportLedgerDocument(String name, LocalDate from, LocalDate to, boolean withVat, boolean withTransactions);
}
