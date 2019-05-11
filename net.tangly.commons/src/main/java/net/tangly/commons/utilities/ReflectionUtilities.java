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

package net.tangly.commons.utilities;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

/**
 * Utility cass to manipulate objects through reflection.
 */
public class ReflectionUtilities {
    public static <T> void setField(@NotNull T entity, @NotNull String name, @NotNull Object value) throws IllegalAccessException {
        Optional<Field> field = findField(entity.getClass(), name);
        if (field.isPresent()) {
            Field property = field.get();
            property.setAccessible(true);
            property.set(entity, value);
        }
    }

    /**
     * Finds the field with the given name.
     *
     * @param clazz class owning the field to retrieve
     * @param name of the field to be found
     * @return the requested field if found otherwise null
     * @param <T> type of the class owning the field
     */
    public static <T> Optional<Field> findField(@NotNull Class<T> clazz, @NotNull String name) {
        Field field = null;
        Class<?> pointer = clazz;
        while ((pointer != null) && (field == null)) {
            Field[] fields = pointer.getDeclaredFields();
            Optional<Field> result = Arrays.stream(fields).filter(o -> name.equals(o.getName())).findAny();
            if (result.isPresent()) {
                field = result.get();
            }
            pointer = pointer.getSuperclass();
        }
        return Optional.ofNullable(field);
    }

    private ReflectionUtilities() {
    }
}
