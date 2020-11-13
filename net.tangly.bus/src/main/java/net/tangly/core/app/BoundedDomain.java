/*
 * Copyright 2006-2020 Marcel Baumann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 *  a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 *  under the License.
 */

package net.tangly.core.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.tangly.commons.generator.IdGenerator;
import net.tangly.commons.generator.LongIdGenerator;
import net.tangly.core.HasTags;
import net.tangly.core.TagType;
import net.tangly.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class BoundedDomain<R, B, H extends Handler, P> {
    private final R realm;
    private final H handler;
    private final P port;
    private final B logic;
    private final transient TagTypeRegistry registry;
    private final transient Map<String, String> configuration;
    protected IdGenerator idGenerator;

    public BoundedDomain(R realm, B logic, H handler, P port, TagTypeRegistry registry) {
        this(realm, logic, handler, port, registry, Collections.emptyMap());
    }

    public BoundedDomain(R realm, B logic, H handler, P port, TagTypeRegistry registry, @NotNull Map<String, String> configuration) {
        this.realm = realm;
        this.logic = logic;
        this.handler = handler;
        this.port = port;
        this.registry = registry;
        this.configuration = new HashMap<>(configuration);
        idGenerator = new LongIdGenerator(1000);
        initialize();
    }

    static void addTagCounts(TagTypeRegistry registry, List<? extends HasTags> entities, HashMap<TagType<?>, Integer> counts) {
        entities.stream().flatMap(e -> e.tags().stream()).map(registry::find).flatMap(Optional::stream).forEach(e -> {
            if (!counts.containsKey(e)) {
                counts.put(e, 0);
            }
            counts.put(e, counts.get(e) + 1);
        });
    }

    public void countTags(@NotNull HashMap<TagType<?>, Integer> counts) {
    }

    public R realm() {
        return realm;
    }

    public B logic() {
        return logic;
    }

    public H handler() {
        return handler;
    }

    public P port() {
        return port;
    }

    public TagTypeRegistry registry() {
        return registry;
    }

    public IdGenerator idGenerator() {
        return idGenerator;
    }

    public Map<String, String> configuration() {
        return Collections.unmodifiableMap(configuration);
    }

    protected void initialize() {
    }
}
