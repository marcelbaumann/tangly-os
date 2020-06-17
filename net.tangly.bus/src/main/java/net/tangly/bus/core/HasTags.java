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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

/**
 * The interface defines a mixin and abstracts an entity with tags.
 */
public interface HasTags {
    /**
     * Returns the set of tags of the entity.
     *
     * @return set of tags
     */
    Set<Tag> tags();

    /**
     * Adds the tag to the set of tags.
     *
     * @param tag tag to be added
     */
    void add(Tag tag);

    /**
     * Remove the tag from the set of tags.
     *
     * @param tag tag to remove
     */
    void remove(Tag tag);

    void clearTags();

    /**
     * Replace or insert the given tag. Tag equivalence is detected with optional namespace and tag name.
     * @param tag tag to replace or insert
     */
    default void replace(@NotNull Tag tag) {
        Objects.requireNonNull(tag);
        findBy(tag.namespace(), tag.name()).ifPresent(this::remove);
        add(tag);
    }

    /**
     * Remove the tag with the given tag identification containing optional namespace and tag name.
     * @param tag tag identification of the tag to be removed
     */
    default void removeTagNamed(String tag) {
        findBy(Tag.namespace(tag), Tag.name(tag)).ifPresent(this::remove);
    }

    /**
     * Find the tag with the given tag identification containing optional namespace and tag name.
     * @param tag tag identification of the tag to be removed
     */
    default Optional<Tag> findBy(String tag) {
        return findBy(Tag.namespace(tag), Tag.name(tag));
    }

    /**
     * Search for a tag with the given namespace and name.
     *
     * @param namespace optional namespace of the tag to be found
     * @param name      name of the tag to be found
     * @return optional found tag
     */
    default Optional<Tag> findBy(String namespace, String name) {
        Objects.requireNonNull(name);
        return tags().stream().filter(o -> Objects.equals(namespace, o.namespace()) && name.equals(o.name())).findAny();
    }

    /**
     * True if the tag with the given tag identification containing optional namespace and tag name could be found.
     * @param namespace optional namespace of the tag
     * @param name name of the tag
     */
    default boolean contains(String namespace, @NotNull String name) {
        return findBy(namespace, name).isPresent();
    }

    default Set<Tag> findByNamespace(String namespace) {
        Objects.requireNonNull(namespace);
        return tags().stream().filter(o -> Objects.equals(namespace, o.namespace())).collect(Collectors.toSet());
    }

    /**
     * Returns the set of tags as a string representation.
     *
     * @return text representation of the tag set
     */
    default String getRawTags() {
        return Tag.text(tags());
    }

    default void addRawTags(String rawTags) {
        Tag.toTags(rawTags).forEach(this::replace);
    }
}
