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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The interface defines a mixin and abstracts an entity with comments.
 */
public interface HasComments {
    /**
     * Returns the list of comments owned by the entity.
     *
     * @return list of comments
     */
    List<Comment> getComments();

    /**
     * Adds a comment to the list of comments.
     *
     * @param comment comment to be added, cannot be null
     */
    void add(Comment comment);

    /**
     * Removes the given comment from the list of comments.
     *
     * @param comment comment to be removed, cannot be null
     */
    void remove(Comment comment);

    /**
     * Returns the comments authored by the given author.
     *
     * @param author author of the searched comment
     * @return list of requested comments
     */
    default List<Comment> findByAuthor(String author) {
        Objects.requireNonNull(author);
        return getComments().stream().filter(o -> Objects.equals(author, o.getAuthor())).collect(Collectors.toList());
    }

    /**
     * Returns the comments having the given tag.
     *
     * @param namespace namespace of the tag
     * @param name      name of the tag
     * @return list of the requested comments
     */
    default List<Comment> findByTag(String namespace, String name) {
        Objects.requireNonNull(name);
        return getComments().stream().filter(o -> o.contains(namespace, name)).collect(Collectors.toList());
    }

    /**
     * Returns all the comments which creation date is in the closed interval
     *
     * @param from beginning of the time interval or LocalDateTime.MIN
     * @param to   end of the time interval or LocalDateTime.MAX
     * @return list of requested comments
     */
    default List<Comment> findByTime(LocalDateTime from, LocalDateTime to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        return getComments().stream()
                .filter(o -> (o.getDate().equals(from) || o.getDate().isAfter(from)) && (o.getDate().equals(to) || o.getDate().isBefore(to)))
                .collect(Collectors.toList());
    }
}
