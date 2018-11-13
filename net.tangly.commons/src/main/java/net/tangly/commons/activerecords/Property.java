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

package net.tangly.commons.activerecords;

import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * Models the common attributes of an entity property.
 * @param <T>
 */
public interface Property<T extends HasOid> {
    /**
     * Returns the name of the property.
     *
     * @return name of the property
     */
    String name();

    /**
     * Returns the type of the entity owning the property.
     *
     * @return type of the declaring entity
     */
    Class<T> entity();

    /**
     * Returns true if the type of the value of the property is a managed persisted type, otherwise false.
     * @return true if type of the property is managed
     */
    boolean hasManagedType();

    /**
     * Returns true if the property contains multiple instances as in a collection, otherwise false.
     *
     * @return true if the property has multiple values
     */
    boolean hasMultipleValues();

    /**
     * Finds the field with the given name.
     *
     * @param name of the field to be found
     * @return the requested field if found otherwise null
     */
    default Field findField(@NotNull String name) {
        Field field = null;
        Class<?> pointer = entity();
        while ((pointer != null) && (field == null)) {
            Field[] fields = pointer.getDeclaredFields();
            Optional<Field> result = Arrays.stream(fields).filter(o -> name.equals(o.getName())).findAny();
            if (result.isPresent()) {
                field = result.get();
            }
            pointer = pointer.getSuperclass();
        }
        return field;
    }
}
