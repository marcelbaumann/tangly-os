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

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the Entity interface.
 *
 * @see Entity
 */
public abstract class EntityImp implements Entity {
    private long oid;
    private String id;
    private String name;
    private String text;
    private LocalDate fromDate;
    private LocalDate toDate;
    private final List<Comment> comments;
    private final Set<Tag> tags;

    /**
     * Default constructor.
     */
    public EntityImp() {
        this.oid = HasOid.UNDEFINED_OID;
        comments = new ArrayList<>();
        tags = new HashSet<>();
    }

    // region HasOid
    @Override
    public long oid() {
        return oid;
    }

    protected void oid(long oid) {
        this.oid = oid;
    }

    // endregion

    // region HasId
    @Override
    public String id() {
        return id;
    }

    @Override
    public void id(String id) {
        this.id = id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void name(String name) {
        this.name = name;
    }

    // endregion

    // region Entity

    @Override
    public String text() {
        return text;
    }

    @Override
    public void text(String text) {
        this.text = text;
    }

    @Override
    public LocalDate fromDate() {
        return fromDate;
    }

    @Override
    public void fromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public LocalDate toDate() {
        return toDate;
    }

    @Override
    public void toDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    // endregion

    // region Comments

    @Override
    public List<Comment> comments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public void add(@NotNull Comment comment) {
        comments.add(comment);
    }

    @Override
    public void remove(@NotNull Comment comment) {
        comments.remove(comment);
    }
    // endregion

    // region HasTags

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
    public void clearTags() {
        tags.clear();
    }
    // endregion
}
