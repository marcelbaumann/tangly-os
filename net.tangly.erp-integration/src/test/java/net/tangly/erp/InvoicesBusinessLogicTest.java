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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;
import net.tangly.bus.invoices.InvoicesBusinessLogic;
import net.tangly.bus.invoices.InvoicesRealm;
import net.tangly.core.TagTypeRegistry;
import net.tangly.invoices.ports.InvoicesEntities;
import net.tangly.invoices.ports.InvoicesHdl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvoicesBusinessLogicTest {
    public static final String CONTRACT_HSLU_2015 = "HSLU-2015";

    @Test
    void testTsvInvoices() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            ErpStore store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            InvoicesHdl handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), store.invoicesRoot());
            handler.importEntities();

            verifyBusinessLogic(handler.realm());

            handler.exportEntities();

            handler = new InvoicesHdl(new InvoicesEntities(new TagTypeRegistry()), store.invoicesRoot());
            handler.importEntities();
        }
    }

    private void verifyBusinessLogic(@NotNull InvoicesRealm realm) {
        InvoicesBusinessLogic logic = new InvoicesBusinessLogic(realm);
        assertThat(logic.invoicedAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null))
            .isEqualByComparingTo(logic.paidAmountWithoutVatForContract(CONTRACT_HSLU_2015, null, null));
        assertThat(logic.expensesForContract(CONTRACT_HSLU_2015, null, null)).isNotNegative();
    }
}
