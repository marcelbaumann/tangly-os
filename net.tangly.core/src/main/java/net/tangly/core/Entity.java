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

package net.tangly.core;

import java.time.LocalDate;

/**
 * The named interface defines a powerful abstraction for entities in a domain model. The features are
 * <ul>
 * <li>oid: an optional internal identifier, an internal identifier is never visible to external systems or users. It
 * is optional because the domain model decides if it is needed or not.</li>
 * <li>text: a human readable documentation of the instance. We recommend using markdown syntax for the text.</li>
 * <li>comments: human readable comments describing aspects of an instance.</li>
 * <li>tags: human readable and machine processable tags defining an ontology to classify instance in the domain model.
 * Orthogonal information can therefore be attached to instances.</li>
 * </ul>
 */
public interface Entity extends HasOid, HasName, HasInterval, HasTags, HasComments {
    /**
     * Sets the start date from when the entity is existing and active.
     *
     * @param fromDate the start of the existing period of the entity
     */
    void fromDate(LocalDate fromDate);

    /**
     * Sets the end date from when the entity is existing and active.
     *
     * @param toDate the end of the existing period of the entity
     */
    void toDate(LocalDate toDate);

    /**
     * Returns the human readable text description of the instance.
     *
     * @return human readable text description of the instance
     * @see #text(String)
     */
    String text();

    /**
     * Sets the text description of the entity.
     *
     * @param text human readable text description
     * @see #text()
     */
    void text(String text);

    default boolean check() {
        return true;
    }
}
