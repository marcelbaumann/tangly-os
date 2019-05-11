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

package net.tangly.commons.orm;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.tangly.commons.orm.imp.AbstractProperty;
import net.tangly.commons.orm.imp.PropertyCode;
import net.tangly.commons.orm.imp.PropertyJson;
import net.tangly.commons.orm.imp.PropertyOne2Many;
import net.tangly.commons.orm.imp.PropertyOne2One;
import net.tangly.commons.orm.imp.PropertySimple;
import net.tangly.commons.codes.Code;
import net.tangly.commons.codes.CodeType;
import net.tangly.commons.models.HasOid;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Represents the mapping between a Java class and a relational database table containing the values of the instances of the class. The design
 * promotes convention over configuration. The primary key for an entity is always the first field with the name oid and of type long.
 *
 * @param <T> the class being mapped. The table takes care of the handling of the unique object identifier.
 */
public class Dao<T extends HasOid> {
    /**
     * Builder for the table class. Upon building the class you should discard the builder instance. Any additional call on the builder will update a
     * runtime exception.
     */
    public static class Builder<V extends HasOid> {
        private Dao<V> table;
        private List<AbstractProperty<V>> properties;
        private List<PropertyOne2Many<V, ?>> relations;

        /**
         * Builder class to create a DAO for an entity and a table in the database.
         *
         * @param schema     optional name of the schema containing the entity
         * @param entity     name of the entity
         * @param clazz      class of the entity
         * @param dataSource data source used to acces the database
         */
        public Builder(String schema, @NotNull String entity, @NotNull Class<V> clazz, @NotNull DataSource dataSource) {
            this.table = new Dao<>(schema, entity, clazz, dataSource);
            this.properties = new ArrayList<>();
            this.relations = new ArrayList<>();
        }

        public Builder withOid() {
            properties.add(new PropertySimple<V>("oid", table.getType(), Long.TYPE, Types.BIGINT));
            return this;
        }

        public Builder withFid(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), Long.TYPE, Types.BIGINT));
            return this;
        }

        /**
         * Add an integer property and column.
         *
         * @param name name of the property and associated column in the table
         * @return builder as fluent interface
         */
        public Builder withInt(@NotNull String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), Integer.class, Types.INTEGER));
            return this;
        }

        public Builder withLong(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), Long.class, Types.BIGINT));
            return this;
        }

        public Builder withString(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), String.class, Types.VARCHAR));
            return this;
        }

        public Builder withText(String name) {
            return withString(name);
        }

        public Builder withDate(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), LocalDate.class, Types.DATE));
            return this;
        }

        public Builder withDateTime(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), LocalDateTime.class, Types.TIMESTAMP));
            return this;
        }

        public Builder withBigDecimal(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), BigDecimal.class, Types.DECIMAL));
            return this;
        }

        public Builder withTags(String name) {
            properties.add(new PropertySimple<V>(name, table.getType(), String.class, Types.VARCHAR, AbstractProperty::tags4java2jdbc,
                    AbstractProperty::tags4jdbc2java));
            return this;
        }

        public Builder withJson(String name, Class<V> type, boolean hasMultipleValues) {
            properties.add(new PropertyJson<>(name, table.getType(), type, hasMultipleValues));
            return this;
        }

        public <U extends Code> Builder withCode(String name, CodeType<U> codeType) {
            properties.add(new PropertyCode<V, U>(name, table.getType(), codeType));
            return this;
        }

        public Builder withOne2One(String name) {
            properties.add(new PropertyOne2One<V, V>(name, table.getType(), table));
            return this;
        }

        public <R extends HasOid> Builder withOne2One(String name, Dao<R> type) {
            properties.add(new PropertyOne2One<V, R>(name, table.getType(), type));
            return this;
        }

        public Builder withOne2Many(String name, String property) {
            relations.add(new PropertyOne2Many<V, V>(name, table.getType(), property, table));
            return this;
        }

        public <R extends HasOid> Builder withOne2Many(String name, String property, Dao<R> type) {
            relations.add(new PropertyOne2Many<V, R>(name, table.getType(), property, type));
            return this;
        }

        public Dao<V> build() throws NoSuchMethodException {
            table.configure(properties, relations);
            Dao<V> copy = this.table;
            table = null;
            return copy;
        }
    }

    private static final String PRIMARY_KEY = "oid";
    private static final int KEY_SQL_TYPE = Types.BIGINT;
    private static Logger log = org.slf4j.LoggerFactory.getLogger(Dao.class);
    private static AtomicLong oidGenerator = new AtomicLong(0);

    private final String schema;
    private final String entityName;
    private final Class<T> type;
    private final DataSource dataSource;
    private Constructor<T> constructor;
    private List<AbstractProperty<T>> properties;
    private List<PropertyOne2Many<T, ?>> relations;
    private String findSql;
    private String replaceSql;
    private String deleteSql;
    private String findWhereSql;

    /**
     * Cache holding all loaded instances handled through the record.
     */
    private Map<Long, WeakReference<T>> cache;

    public Dao(String schema, String entity, Class<T> type, DataSource dataSource) {
        this.schema = schema;
        this.entityName = entity;
        this.type = type;
        this.dataSource = dataSource;
        this.cache = new HashMap<>();
    }

    private void configure(List<AbstractProperty<T>> properties, List<PropertyOne2Many<T, ?>> relations) throws NoSuchMethodException {
        this.properties = List.copyOf(properties);
        this.relations = List.copyOf(relations);
        constructor = type.getConstructor();
        findSql = generateFindSql();
        replaceSql = generateReplaceSql();
        deleteSql = generateDeleteSql();
        findWhereSql = generateFindWhereSql();
    }

    public Class<T> getType() {
        return type;
    }

    public void clearCache() {
        cache.clear();
    }

    /**
     * Returns the property with the given name.
     *
     * @param name name of the property to be found
     * @return optional found property
     */
    public Optional<AbstractProperty<T>> getPropertyBy(@NotNull String name) {
        return properties.stream().filter(o -> o.name().equals(name)).findAny();
    }

    // region of-CRUD

    /**
     * Updates the persistent data associated with the entity.If the entity is new a row is inserted into the table otherwise the columns are updated.
     * The update is transitive and all referenced entities are also updated.
     *
     * @param entity entity to update
     */
    public void update(@NotNull T entity) {
        try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(replaceSql)) {
            if (entity.oid() == HasOid.UNDEFINED_OID) {
                properties.get(0).setField(entity, oidGenerator.incrementAndGet());
            }
            for (int i = 0; i < properties.size(); i++) {
                properties.get(i).setParameter(stmt, i + 1, entity);
            }
            stmt.executeUpdate();
            for (var relation : relations) {
                relation.update(entity);
            }
            addToCache(entity);
        } catch (SQLException | IllegalAccessException | JsonProcessingException e) {
            log.error("Esception creating {} id {}", entityName, entity.oid(), e);
        }
    }

    /**
     * Finds the entity instance with the given unique object identifier.
     *
     * @param oid object identifier of the instance to find
     * @return optional of the requested entity
     */
    public Optional<T> find(long oid) {
        Optional<T> entity = retrieveFromCache(oid);
        if (entity.isEmpty()) {
            try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(findSql)) {
                stmt.setObject(1, oid, KEY_SQL_TYPE);
                try (ResultSet set = stmt.executeQuery()) {
                    if (set.next()) {
                        entity = Optional.of(materializeEntity(set));
                    }
                }
            } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | IOException e) {
                log.error("Exception occured when retrieving entity {} id {}", entityName, oid, e);
            }
        }
        return entity;
    }

    public List<T> find(String where) {
        List<T> entities = new ArrayList<>();
        try (var connection = dataSource.getConnection(); var stmt = connection.createStatement();
             var set = stmt.executeQuery(findWhereSql + where)) {
            while (set.next()) {
                Optional<T> instance = retrieveFromCache((Long) set.getObject(1));
                entities.add(instance.orElse(materializeEntity(set)));
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | InvocationTargetException | IOException e) {
            log.error("Exception occured when retrieving entity with id {}", entityName, e);
        }
        return entities;
    }

    /**
     * Retrieves the values of the properties from the result set and retrieve the values of the relations.
     *
     * @param set result set containing the field values
     * @return the new instance with filled properties and relations
     * @throws SQLException if a problem was encountered when retrieving the field values from the result set
     */
    private T materializeEntity(ResultSet set) throws SQLException, IllegalAccessException, InstantiationException, InvocationTargetException,
            IOException {
        T entity = constructor.newInstance();
        for (int i = 0; i < properties.size(); i++) {
            properties.get(i).setField(set, i + 1, entity);
        }
        for (var relation : relations) {
            relation.retrieve(entity);
        }
        addToCache(entity);
        return entity;
    }

    public void delete(long oid) {
        try (var connection = dataSource.getConnection(); var stmt = connection.prepareStatement(deleteSql)) {
            stmt.setObject(1, oid, KEY_SQL_TYPE);
            stmt.executeUpdate();
            removeFromCache(oid);
        } catch (SQLException e) {
            log.error("SQL error when deleting instance {} id {}", entityName, oid, e);
        }
    }

    // endregion

    // region cache operations

    private Optional<T> retrieveFromCache(long id) {
        if (cache.containsKey(id)) {
            WeakReference<T> reference = cache.get(id);
            T entity = reference.get();
            if (entity != null) {
                return Optional.of(entity);
            } else {
                cache.remove(id);
            }
        }
        return Optional.empty();
    }

    private void addToCache(T entity) {
        if (cache.containsKey(entity.oid())) {
            log.debug("Invalidate cache {} for id {}", getClass().getSimpleName(), entity.oid());
        } else {
            log.debug("Add to cache {} id {}", getClass().getSimpleName(), entity.oid());
        }
        cache.put(entity.oid(), new WeakReference<>(entity));
    }

    private void removeFromCache(long id) {
        if (cache.containsKey(id)) {
            log.debug("Invalidate cache {} for id {}", getClass().getSimpleName(), id);
            cache.remove(id);
        }
    }

    // endregion

    private String generateReplaceSql() {
        return "REPLACE INTO " + ((schema != null) ? schema + "." + entityName : entityName) + " (" +
                properties.stream().map(AbstractProperty::name).collect(Collectors.joining(", ")) + ") VALUES (" + "?" +
                String.join("", Collections.nCopies(properties.size() - 1, ", ?")) + ")";
    }

    private String generateDeleteSql() {
        return "DELETE FROM " + ((schema != null) ? schema + "." + entityName : entityName) + " WHERE " + PRIMARY_KEY + "=?";
    }

    private String generateFindSql() {
        return "SELECT " + properties.stream().map(AbstractProperty::name).collect(Collectors.joining(", ")) + " FROM " +
                ((schema != null) ? schema + "." + entityName : entityName) + " WHERE " + PRIMARY_KEY + " = ?";
    }

    private String generateFindWhereSql() {
        return "SELECT " + properties.stream().map(AbstractProperty::name).collect(Collectors.joining(", ")) + " FROM " +
                ((schema != null) ? schema + "." + entityName : entityName) + " WHERE ";
    }

}
