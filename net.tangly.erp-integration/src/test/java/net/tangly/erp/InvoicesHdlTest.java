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

package net.tangly.erp;

import java.io.IOException;
import java.nio.file.FileSystem;

import com.google.common.jimfs.Jimfs;
import net.tangly.erp.invoices.ports.InvoicesEntities;
import net.tangly.erp.invoices.ports.InvoicesHdl;
import net.tangly.erp.invoices.services.InvoicesRealm;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoicesHdlTest {
    @Test
    void testTsvInvoices() throws IOException {
        try (FileSystem fs = Jimfs.newFileSystem(com.google.common.jimfs.Configuration.unix())) {
            var store = new ErpStore(fs);
            store.createCrmAndLedgerRepository();

            var handler = new InvoicesHdl(new InvoicesEntities(), store.invoicesRoot());
            handler.importEntities();

            verifyArticles(handler.realm());
            verifyInvoices(handler.realm());

            handler.exportEntities();

            handler = new InvoicesHdl(new InvoicesEntities(), store.invoicesRoot());
            handler.importEntities();
            verifyArticles(handler.realm());
        }
    }


    private void verifyInvoices(@NotNull InvoicesRealm realm) {
        assertThat(realm.invoices().items().isEmpty()).isFalse();
        realm.invoices().items().forEach(o -> assertThat(o.check()).isTrue());
    }

    private void verifyArticles(@NotNull InvoicesRealm realm) {
        assertThat(realm.articles().items().isEmpty()).isFalse();
    }
}
