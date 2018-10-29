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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The immutable class tag models a tag with a name and a value. Tags provide a powerful approach
 * for multi-dimensional classifications of values.
 */
public class Tag implements Serializable {
    /**
     * Transforms a collection of tags into their textual representation.
     *
     * @param tags tags to be transformed
     * @return textual representation of the tags
     * @see Tag#toTags(String)
     */
    public static String toString(Collection<Tag> tags) {
        return tags.stream().map(Tag::toString).collect(Collectors.joining(","));
    }

    /**
     * Transforms a raw tag textual representation into a set of tags
     *
     * @param rawTags textual representation of tags
     * @return set of the tag instances
     * @see Tag#toString(Collection)
     */
    public static Set<Tag> toTags(String rawTags) {
        return Arrays.stream(rawTags.split(",")).map(Tag::parse).collect(Collectors.toSet());
    }

    public static String getNamespace(String tag) {
        return tag.contains(":") ? tag.substring(0, tag.indexOf(':')) : null;
    }

    public static String getName(String tag) {
        return tag.substring((tag.contains(":") ? tag.indexOf(':') + 1 : 0), (tag.contains("=")) ? tag.indexOf('=') : tag.length());
    }

    public static String getValue(String tag) {
        return tag.contains("=") ? tag.substring(tag.indexOf('=') + 1) : null;
    }

    public static Tag parse(String tag) {
        Objects.requireNonNull(tag);
        return new Tag(getNamespace(tag), getName(tag), getValue(tag));
    }

    public static Tag of(String tag) {
        return new Tag(getNamespace(tag), getName(tag), null);
    }

    public static Tag of(String tag, Serializable value) {
        return new Tag(getNamespace(tag), getName(tag), value);
    }

    public static Tag of(String namespace, String name, Serializable value) {
        return new Tag(namespace, name, value);
    }


    /**
     * The namespace of the tag identifying the domain
     */
    private final String namespace;

    /**
     * The name of the tag identifying the tag type and purpose.
     */
    private final String name;
    /**
     * The value of this tag instance.
     */
    private final Serializable value;

    /**
     * Constructor of the class.
     *
     * @param namespace optional namespace of the tag
     * @param name      name of the tag
     * @param value     optional value of the tag
     */
    public Tag(String namespace, String name, Serializable value) {
        this.namespace = namespace;
        this.name = Objects.requireNonNull(name);
        this.value = value;
    }

    /**
     * Returns true if the tag has a value otherwise false.
     *
     * @return flag indicating if the tag has a value field
     */
    public boolean hasValue() {
        return value != null;
    }

    /**
     * Returns the namespace of the tag.
     *
     * @return namespace of the tag
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the name of the tag.
     *
     * @return name of the tag
     */

    public String getName() {
        return name;
    }

    /**
     * Returns the string representation of the tag value.
     *
     * @return string representation of the tag
     */
    public String getStringValue() {
        return (value != null ? value.toString() : null);
    }

    /**
     * Returns the value of the tag.
     *
     * @return the value of the tag
     */
    public Object getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final Tag other = (Tag) obj;
        return Objects.equals(this.namespace, other.namespace) && Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
    }

    @Override
    public String toString() {
        return ((getNamespace() != null ? getNamespace() + ":" : "") + getName() + (getValue() != null ? "=" + getStringValue() : ""));
    }
}
