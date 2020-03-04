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

package net.tangly.commons.orm;

import net.tangly.bus.core.HasOid;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Models the common attributes of an entity property. Conceptually three kinds of properties exist:
 * <ul>
 * <li>A property referencing a Java object or a Java primitive type. The object can be a single value or a collection. Single value object can
 * either be persisted through standard JDBC mechanisms or through provided Function[S,T] transformers.</li>
 * <li>A single value property referencing another persisted entity persisted through a DAO.</li>
 * <li>A multiple value property referencing another persisted entity through a DAO</li>
 * </ul>
 * The property provides mechanisms to
 * <ul>
 * <li>Transform a SQL type value into a Java instance and set the property value with this instance.</li>
 * <li>Transform a Java instance into a SQL type value and set the corresponding prepared statement variable.</li>
 * <li>Transform a Java string into a SQL type value. The string can be read from a column of a TSV record.</li>
 * <li>Transform a SQL type value into a string. The string representation can be written into the corresponding column of a TSV record.</li>
 * </ul>
 *
 * @param <T> type of the entity owning the property.
 */
public interface Property<T extends HasOid> {
    enum ConverterType {java2jdbc, jdbc2java, java2text, text2java}

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
     * Returns the Java field associated with the property.
     *
     * @return field of the property
     */
    Field field();

    int sqlType();

    Class<?> jdbcType();

    <T, R> Function<T, R> getConverter(@NotNull ConverterType type);

    default <P, V> V get(@NotNull T entity, @NotNull ConverterType type) throws IllegalAccessException, IllegalArgumentException {
        Function<P, V> convert = getConverter(type);
        return convert.apply((P) field().get(entity));
    }

    default <P, V> void set(@NotNull T entity, V value, @NotNull ConverterType type) throws IllegalAccessException, IllegalArgumentException {
        Function<V, P> convert = getConverter(type);
        field().set(entity, convert.apply(value));
    }

    default void setParameter(@NotNull PreparedStatement statement, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        Object value = get(entity, ConverterType.java2jdbc);
        if (value != null) {
            statement.setObject(index, value, sqlType());
        } else {
            statement.setNull(index, sqlType());
        }
    }

    default void getParameter(@NotNull ResultSet set, int index, @NotNull T entity) throws SQLException, IllegalAccessException {
        set(entity, set.getObject(index, jdbcType()), ConverterType.jdbc2java);
    }
}
