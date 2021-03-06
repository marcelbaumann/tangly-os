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

package net.tangly.core.domain;

/**
 * Define the import port for the bounded domain. It is a primary port in the DDD terminology.
 * @param <R> Realm of the bounded domain
 */
public interface Handler<R extends Realm> {
    /**
     * Import all entities of the bounded domain from the file system.
     *
     * @see #exportEntities()
     */
    void importEntities();

    /**
     * Export all entities of the bounded domain to the file system.
     *
     * @see #importEntities()
     */
    void exportEntities();

    /**
     * Return the realm containing all the entities of the bounded domain.
     *
     * @return realm of the bounded domain
     */
    R realm();
}
