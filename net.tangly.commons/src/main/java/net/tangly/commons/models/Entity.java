/*
 *
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package net.tangly.commons.models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * The named interface defines a powerful abstraction for entities in a domain model. The features are
 * * oid: an optional internal identifier, an internal identifier is never visible to external systems or users. It
 * is optional because the domain model decides if it is needed or not.
 * * id: a unique identifier of an instance in the context of the domain model.
 * * name: a human readable name of an instance in the context of the domain model. A name can but do not need to be
 * an identifier.
 * * text: a human readable documentation of the instance. We recommend using markdown syntax for the text.
 * * model: human readable model describing aspects of an instance.
 * * tags: human readable and machine processable tags defining an ontology to classify instance in the domain model.
 * Orthogonal information can therefore be attached to instances.
 */
public interface Entity extends HasId, HasTags, HasComments, Serializable {
    static Entity of() {
        return new EntityImp(UNDEFINED_OID, Long.toString(UNDEFINED_OID), Long.toString(UNDEFINED_OID));
    }

    static Entity of(long oid) {
        return new EntityImp(oid, Long.toString(oid), Long.toString(oid));
    }

    static Entity of(String id) {
        return new EntityImp(UNDEFINED_OID, id, id);
    }

    /**
     * Returns the text describing the entity.
     *
     * @return description of the entity
     */
    String text();

    /**
     * Sets the text describing the entity.
     *
     * @param text description of the entity
     */
    void text(String text);

    /**
     * Returns the date from when the entity is existing and active.
     *
     * @return the start of the existing period of the entity
     */
    LocalDate fromDate();

    void fromDate(LocalDate fromDate);

    /**
     * Returns the date until when the entity is existing and active.
     *
     * @return the end of the existing period of the entity
     */
    LocalDate toDate();

    void toDate(LocalDate toDate);
}
