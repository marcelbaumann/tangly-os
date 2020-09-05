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

package net.tangly.commons.vaadin;

import java.time.LocalDateTime;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import net.tangly.bus.core.Comment;
import net.tangly.bus.core.HasComments;
import org.jetbrains.annotations.NotNull;

/**
 * The comments view is a Crud view with all the comments defined for an object implementing the {@link HasComments}. Edition functions are provided to add,
 * delete, and view individual comments. Update function is not supported because comments are immutable objects. Immutable objects must be explicitly be
 * deleted before an new version can be added. This approach supports auditing approaches.
 */
public class CommentsView extends Crud<Comment> implements CrudForm<Comment> {
    private final transient HasComments hasComments;
    private final DateTimePicker created;
    private final TextField author;
    private final TextArea text;

    public CommentsView(@NotNull Mode mode, @NotNull HasComments entity) {
        super(Comment.class, mode, new ListDataProvider<>(entity.comments()));
        this.hasComments = entity;
        created = new DateTimePicker("Created");
        author = VaadinUtils.createTextField("Author", "author");
        text = new TextArea("Text");
        initialize();
    }

    protected void initialize() {
        super.initialize(this, new GridActionsListener<>(grid().getDataProvider(), this::selectedItem));
        Grid<Comment> grid = grid();
        grid.addColumn(Comment::created).setKey("created").setHeader("Created").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false)
                .setFrozen(true);
        grid.addColumn(Comment::author).setKey("author").setHeader("Author").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
        grid.addColumn(Comment::text).setKey("text").setHeader("Text").setSortable(true).setFlexGrow(0).setWidth("200px").setResizable(false);
    }

    @Override
    public FormLayout createForm(@NotNull Operation operation, Comment entity) {
        boolean readonly = Operation.isReadOnly(operation);

        created.setReadOnly(readonly);

        author.setRequired(true);
        author.setReadOnly(readonly);

        text.setHeight("8em");
        text.setRequired(true);
        text.setReadOnly(readonly);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        form.add(created, author, new HtmlComponent("br"), text);
        form.setColspan(text, 2);
        if (readonly) {
            Binder<Comment> binder = new Binder<>(Comment.class);
            binder.bind(created, Comment::created, null);
            binder.bind(author, Comment::author, null);
            binder.bind(text, Comment::text, null);
            binder.readBean(entity);
        } else {
            created.setValue(LocalDateTime.now());
            author.clear();
            text.clear();
        }
        return form;
    }

    @Override
    public Comment formCompleted(@NotNull Operation operation, Comment entity) {
        return switch (operation) {
            case CREATE -> {
                Comment comment = create();
                hasComments.add(comment);
                yield comment;
            }
            case DELETE -> {
                hasComments.remove(entity);
                yield entity;
            }
            default -> entity;
        };
    }

    private Comment create() {
        return Comment.of(created.getValue(), author.getValue(), text.getValue());
    }
}
