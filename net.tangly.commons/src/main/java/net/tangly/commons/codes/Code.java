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

package net.tangly.commons.codes;

import java.io.Serializable;

/**
 * The abstraction of a code table format. A code table is an extensible and temporal enumeration. New values can be added without recompiling the
 * application. A code can have a validity period. To guaranty consistency a code format shall never be deleted, only be disabled.
 */
public interface Code extends Serializable {
    /**
     * Returns the unique identifier of a code table entity.
     *
     * @return unique identifier of the code table instance
     */
    int id();

    /**
     * Returns the human readable representation of a code table entity.
     *
     * @return human readable code
     */
    String code();

    /**
     * Returns true if the code table instance is enabled.
     *
     * @return flag indicating if the code is enabled or archived
     */
    boolean isEnabled();
}