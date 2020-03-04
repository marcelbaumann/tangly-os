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

import java.io.Serializable;

/**
 * Marks a class with the capability to be uniquely identified through an object identifier.
 */
public interface HasOid extends Serializable {
    /**
     * Place holder to identify an illegal or undefined internal identifier. It is also the default value if a developer forget to set the value.
     */
    long UNDEFINED_OID = 0;

    /**
     * Returns the unique internal identifier of the instance.
     *
     * @return unique internal identifier
     */
    long oid();
}
