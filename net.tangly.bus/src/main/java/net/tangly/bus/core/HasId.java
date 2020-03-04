/*
 * Copyright 2006-2020 Marcel Baumann
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

package net.tangly.bus.core;

/**
 * Mixin states that the class has an external unique object identifier and a human readable name. The interface
 * supports only querying the external identifier. It is the responsibility of the application to decide if the
 * external identifier can be modified or not.
 */
public interface HasId extends HasOid {
    /**
     * Returns the unique external and visible identifier of the instance.
     *
     * @return unique external identifier
     */
    default String id() {
        return Long.toString(oid());
    }

    /**
     * Sets the unique external identifier of the instance
     *
     * @param id external identifier
     */
    default void id(String id) {
    }

    /**
     * Returns the human readable name of the instance. Per default it returns the external identifier.
     *
     * @return the human readable name of the instance
     */
    default String name() {
        return id();
    }

    /**
     * Sets the name of the entity. Per default it does nothing.
     *
     * @param name human readable name of the entity
     */
    default void name(String name) {
    }
}
