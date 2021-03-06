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

package net.tangly.erp.products.ports;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.core.providers.ProviderHasOid;
import net.tangly.core.providers.ProviderInMemory;
import net.tangly.core.providers.ProviderPersistence;
import net.tangly.erp.products.domain.Assignment;
import net.tangly.erp.products.domain.Effort;
import net.tangly.erp.products.domain.Product;
import net.tangly.erp.products.services.ProductsRealm;
import one.microstream.storage.types.EmbeddedStorage;
import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;

public class ProductsEntities implements ProductsRealm {
    static class Data {
        List<Product> products;
        List<Assignment> assignments;
        List<Effort> efforts;

        Data() {
            products = new ArrayList<>();
            assignments = new ArrayList<>();
            efforts = new ArrayList<>();
        }
    }

    private static final long OID_SEQUENCE_START = 1000;
    private final Data data;
    private final Provider<Product> products;
    private final Provider<Assignment> assignments;
    private final Provider<Effort> efforts;
    private final IdGenerator generator;
    private final EmbeddedStorageManager storageManager;


    public ProductsEntities(@NotNull Path path) {
        data = new Data();
        storageManager = EmbeddedStorage.start(data, path);
        generator = generator();
        products = ProviderHasOid.of(generator, storageManager, data.products);
        assignments = ProviderHasOid.of(generator, storageManager, data.assignments);
        efforts = ProviderHasOid.of(generator, storageManager, data.efforts);
    }

    @Inject
    public ProductsEntities() {
        data = new Data();
        storageManager = null;
        generator = generator();
        this.products = ProviderHasOid.of(generator, data.products);
        this.assignments = ProviderHasOid.of(generator, data.assignments);
        this.efforts = ProviderHasOid.of(generator, data.efforts);
    }

    public void storeRoot() {
        storageManager.storeRoot();
    }

    public void shutdown() {
        storageManager.shutdown();
    }

    @Override
    public Provider<Product> products() {
        return products;
    }

    @Override
    public Provider<Assignment> assignments() {
        return assignments;
    }

    @Override
    public Provider<Effort> efforts() {
        return efforts;
    }

    @Override
    public void close() throws Exception {
        storageManager.close();
    }

    private IdGenerator generator() {
        long oidCounter = Realm.maxOid(data.products);
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.assignments));
        oidCounter = Math.max(oidCounter, Realm.maxOid(data.efforts));
        return new LongIdGenerator(Math.max(oidCounter, OID_SEQUENCE_START));
    }
}
