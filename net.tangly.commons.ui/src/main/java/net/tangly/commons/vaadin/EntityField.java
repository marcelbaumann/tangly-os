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

package net.tangly.commons.vaadin;

import com.vaadin.flow.component.AbstractCompositeField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import net.tangly.bus.core.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityField extends AbstractCompositeField<VerticalLayout, EntityField, Entity> {
    private boolean readonly;
    private final TextField oid;
    private final TextField id;
    private final TextField name;
    private final DatePicker fromDate;
    private final DatePicker toDate;
    private final TextArea text;

    public EntityField() {
        super(null);
        oid = new TextField("Oid", "oid");
        id = new TextField("Id", "id");
        name = new TextField("Name", "name");
        fromDate = new DatePicker("From Date");
        toDate = new DatePicker("To Date");
        text = new TextArea("Text", "text");
        text.setWidthFull();
        Details textDetails = new Details("Text", text);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1), new FormLayout.ResponsiveStep("32em", 2), new FormLayout.ResponsiveStep("40em", 3));
        form.add(new HorizontalLayout(oid, id, name));
        form.add(new HtmlComponent("br"), 3);
        form.add(new HorizontalLayout(fromDate, toDate));
        form.add(new HtmlComponent("br"), 3);
        form.add(textDetails, 3);
        getContent().add(form);
    }

    public <T extends Entity> void bind(@NotNull Binder<T> binder) {
        binder.bind(oid, o -> Long.toString(o.oid()), null);
        binder.bind(id, Entity::id, Entity::id);
        binder.bind(name, Entity::name, Entity::name);
        binder.forField(fromDate)
                .withValidator(from -> (from == null) || (toDate.getValue() == null) || (from.isBefore(toDate.getValue())), "From date must be before to date")
                .bind(Entity::fromDate, Entity::fromDate);
        binder.forField(toDate)
                .withValidator(to -> (to == null) || (fromDate.getValue() == null) || (to.isAfter(fromDate.getValue())), "To date must be after from date")
                .bind(Entity::toDate, Entity::toDate);
        binder.bind(text, Entity::text, Entity::text);
    }

    @Override
    protected void setPresentationValue(Entity entity) {
        if (entity == null) {
            getChildren().filter(HasValue.class::isInstance).map(HasValue.class::cast).forEach(HasValue::clear);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        VaadinUtils.setReadOnly(this, readOnly);
    }
}
