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

package net.tangly.commons.models;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry of tag type and their associated tag values.
 */

public class TagTypeRegistry {
    /**
     * registered tag types in the registry.
     */
    private Set<TagType> types;

    public TagTypeRegistry() {
        types = new HashSet<>();
    }

    /**
     * Register a tag type.
     *
     * @param type tag type to register
     */
    public void register(@NotNull TagType type) {
        types.add(type);
    }

    /**
     * Returns all the namespaces registered.
     *
     * @return List of namespaces
     */
    public List<String> namespaces() {
        return types.stream().map(o -> o.namespace()).distinct().collect(Collectors.toUnmodifiableList());
    }

    public List<String> tagsForNamespace(@NotNull String namespace) {
        return types.stream().filter(o -> Objects.equals(o.namespace(), namespace)).map(TagType::namespace).distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    public Optional<TagType> find(String namespace, @NotNull String name) {
        return types.stream().filter(o -> Objects.equals(o.namespace(), namespace) && Objects.equals(o.name(), name)).findAny();
    }
}

