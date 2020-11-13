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

package net.tangly.invoices.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tangly.bus.invoices.Article;
import net.tangly.bus.invoices.Invoice;
import net.tangly.bus.invoices.InvoiceLegalEntity;
import net.tangly.bus.invoices.InvoicesRealm;
import net.tangly.core.HasOid;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;

public class InvoicesEntities implements InvoicesRealm {
    private static class Data {
        List<Invoice> invoices;
        List<Article> articles;
        List<InvoiceLegalEntity> legalEntities;
        private long oidCounter;
        private Map<String, String> configuration;

        Data() {
            invoices = new ArrayList<>();
            articles = new ArrayList<>();
            legalEntities = new ArrayList<>();
            oidCounter = HasOid.UNDEFINED_OID;
            configuration = new HashMap<>();
        }
    }

    private final Data data;

    private final Provider<Invoice> invoices;
    private final Provider<Article> articles;
    private final Provider<InvoiceLegalEntity> legalEntities;
    private final EmbeddedStorageManager storageManager;


    public InvoicesEntities(Path path) {
        this.data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        invoices = new ProviderPersistence<>(storageManager, data.invoices);
        articles = new ProviderPersistence<>(storageManager, data.articles);
        legalEntities = new ProviderPersistence<>(storageManager, data.legalEntities);
    }

    public InvoicesEntities() {
        data = new Data();
        storageManager = null;
        invoices = new ProviderInMemory<>(data.invoices);
        articles = new ProviderInMemory<>(data.articles);
        legalEntities = new ProviderInMemory<>(data.legalEntities);
    }

    @Override
    public Provider<Article> articles() {
        return this.articles;
    }

    @Override
    public Provider<Invoice> invoices() {
        return this.invoices;
    }

    @Override
    public Provider<InvoiceLegalEntity> legalEntities() {
        return legalEntities;
    }
}
