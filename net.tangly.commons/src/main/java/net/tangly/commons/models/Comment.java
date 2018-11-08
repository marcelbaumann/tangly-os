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

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Comment defines a human-readable annotation to an entity. A comment is an immutable object. Comments can be tagged to provide classification.
 */
public class Comment implements HasTags, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The creation date and time of the comment.
     */
    private LocalDateTime created;

    /**
     * The author of the comment, the system should insures that the author is an unique external identifier.
     */
    private String author;

    /**
     * The comment as a markdown text.
     */
    private String text;

    /**
     * The tags of the comment.
     */
    private Set<Tag> tags;

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param author author of the comment
     * @param text   content of the comment
     * @param tags   optional tags of the comment
     * @return the newly created comment
     */
    public static Comment of(@NotNull String author, @NotNull String text, Tag... tags) {
        return of(LocalDateTime.now(), author, text, tags);
    }

    /**
     * Factory method to update a new comment. The current date and time are set a creation date.
     *
     * @param date   creation date of the comment
     * @param author author of the comment
     * @param text   content of the comment
     * @param tags   optional tags of the comment
     * @return the newly created comment
     */
    public static Comment of(@NotNull LocalDateTime date, @NotNull String author, @NotNull String text, Tag... tags) {
        var comment = new Comment(date, author, text);
        Arrays.stream(tags).forEach(comment::add);
        return comment;
    }

    private Comment() {
    }

    Comment(@NotNull LocalDateTime date, @NotNull String author, @NotNull String text) {
        this.created = Objects.requireNonNull(date);
        this.author = Objects.requireNonNull(author);
        this.text = Objects.requireNonNull(text);
        tags = new HashSet<>();
    }

    public @NotNull LocalDateTime created() {
        return created;
    }

    public @NotNull String author() {
        return author;
    }

    public @NotNull String text() {
        return text;
    }

    // region tags

    @Override
    public Set<Tag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void add(Tag tag) {
        tags.add(tag);
    }

    @Override
    public void remove(Tag tag) {
        tags.remove(tag);
    }

    @Override
    public void clear() {
        tags.clear();
    }
    // endregion

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("created", created).add("author", author).add("text", text).add("tags", Tag.toString(tags))
                .toString();
    }
}
