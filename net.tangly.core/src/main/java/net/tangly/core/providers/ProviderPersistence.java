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

package net.tangly.core.providers;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import one.microstream.storage.types.EmbeddedStorageManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider with instances in memory and persisted.
 * <p>The update method uses an eager storage strategy to insure that all instance variables of a Java object are persisted. This approach is necessary due
 * to the implementation restrictions of MicroStream.</p>
 *
 * @param <T> type of the instances handled in the provider
 */
public class ProviderPersistence<T> implements Provider<T> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private transient final EmbeddedStorageManager storageManager;
    private final List<T> items;

    public ProviderPersistence(EmbeddedStorageManager storageManager, @NotNull List<T> items) {
        this.storageManager = storageManager;
        this.items = items;
    }

    @Override
    public List<T> items() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void update(@NotNull T entity) {
        if (!items.contains(entity)) {
            items.add(entity);
            storageManager.store(items);
        } else {
            var storage = storageManager.createEagerStorer();
            storage.store(entity);
            storage.commit();
        }
    }

    @Override
    public void updateAll(@NotNull Iterable<? extends T> entities) {
        entities.forEach(entity -> {
            if (!items.contains(entity)) {
                items.add(entity);
            } else {
                storageManager.store(entity);
            }
        });
        storageManager.store(items);
    }

    @Override
    public void delete(@NotNull T entity) {
        items.remove(entity);
    }
}
