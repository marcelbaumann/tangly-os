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

package net.tangly.commons.crm.ui;

import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.server.StreamResource;
import net.tangly.bus.codes.CodeType;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.GenderCode;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.RealmCrm;
import net.tangly.bus.providers.ViewProvider;
import net.tangly.commons.vaadin.CodeField;
import net.tangly.commons.vaadin.CommentsView;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.EntityField;
import net.tangly.commons.vaadin.InternalEntitiesView;
import net.tangly.commons.vaadin.One2ManyView;
import net.tangly.commons.vaadin.TabsComponent;
import net.tangly.commons.vaadin.TagsView;
import net.tangly.commons.vaadin.VaadinUtils;
import org.jetbrains.annotations.NotNull;

public class NaturalEntitiesView extends InternalEntitiesView<NaturalEntity> {
    private final RealmCrm realm;

    public NaturalEntitiesView(@NotNull RealmCrm realm, @NotNull Mode mode) {
        super(NaturalEntity.class, mode, realm.naturalEntities(), realm.tagTypeRegistry());
        this.realm = realm;
        initializeGrid();
    }

    @Override
    protected void initializeGrid() {
        Grid<NaturalEntity> grid = grid();
        grid.addColumn(NaturalEntity::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(NaturalEntity::name).setKey("name").setHeader("Name").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(NaturalEntity::fromDate, DateTimeFormatter.ISO_DATE)).setKey("from").setHeader("From").setAutoWidth(true)
                .setResizable(true).setSortable(true);
        grid.addColumn(new LocalDateRenderer<>(NaturalEntity::toDate, DateTimeFormatter.ISO_DATE)).setKey("to").setHeader("To").setAutoWidth(true)
                .setResizable(true).setSortable(true);
        grid.addColumn(NaturalEntity::lastname).setKey("lastname").setHeader("Last Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(NaturalEntity::firstname).setKey("firstname").setHeader("First Name").setSortable(true).setAutoWidth(true).setResizable(true);
        grid.addColumn(new ComponentRenderer<>(person -> (person.gender() == GenderCode.male) ? new Icon(VaadinIcon.MALE) : new Icon(VaadinIcon.FEMALE)))
                .setHeader("Gender").setAutoWidth(true).setResizable(true);
        grid.addColumn(VaadinUtils.linkedInComponentRenderer(CrmTags::individualLinkedInUrl)).setKey("linkedIn").setHeader("LinkedIn").setAutoWidth(true);
        // TODO find out why it is not working with PaginatedGrid but is working with standard Grid
        addAndExpand(filterCriteria(grid()), grid(), createCrudButtons());
    }

    public static void defineOne2ManyEmployees(@NotNull Grid<Employee> grid) {
        VaadinUtils.initialize(grid);
        grid.addColumn(Employee::oid).setKey("oid").setHeader("Oid").setAutoWidth(true).setResizable(true).setSortable(true).setFrozen(true);
        grid.addColumn(o -> o.organization().name()).setKey("organization").setHeader("Organization").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(o -> o.tag(CrmTags.CRM_EMPLOYEE_TITLE).orElse(null)).setKey("title").setHeader("Title").setAutoWidth(true).setResizable(true)
                .setSortable(true);
        grid.addColumn(Employee::fromDate).setKey("from").setHeader("From").setAutoWidth(true).setResizable(true).setSortable(true);
        grid.addColumn(Employee::toDate).setKey("to").setHeader("To").setAutoWidth(true).setResizable(true).setSortable(true);
    }

    @Override
    protected void registerTabs(@NotNull TabsComponent tabs, @NotNull Mode mode, @NotNull NaturalEntity entity) {
        tabs.add(new Tab("Overview"), createOverallView(mode, entity));
        tabs.add(new Tab("Comments"), new CommentsView(mode, entity));
        tabs.add(new Tab("Tags"), new TagsView(mode, entity, realm.tagTypeRegistry()));
        One2ManyView<Employee> employees = new One2ManyView<>(Employee.class, mode, NaturalEntitiesView::defineOne2ManyEmployees,
                ViewProvider.of(realm.employees(), o -> entity.oid() == o.person().oid()), new EmployeesView(realm, mode));
        tabs.add(new Tab("Employees"), employees);
    }

    @Override
    protected FormLayout createOverallView(@NotNull Mode mode, @NotNull NaturalEntity entity) {
        boolean readonly = Mode.readOnly(mode);
        EntityField<NaturalEntity> entityField = new EntityField<>();
        TextField firstname = VaadinUtils.createTextField("Firstname", "firstname", readonly);
        TextField lastname = VaadinUtils.createTextField("Lastname", "lastname", readonly);
        CodeField<GenderCode> gender = new CodeField<>(CodeType.of(GenderCode.class), "Gender");
        TextField mobilePhone = VaadinUtils.createTextField("Mobile Phone", "mobile phone number", true);
        EmailField homeEmail = new EmailField("Home Email");
        homeEmail.setClearButtonVisible(true);
        TextField homeSite = VaadinUtils.createTextField("Home Site", "home site", true);

        VaadinUtils.readOnly(Mode.readOnly(mode), entityField, homeEmail);

        FormLayout form = new FormLayout();
        VaadinUtils.setResponsiveSteps(form);
        entityField.addEntityComponentsTo(form);
        form.add(new HtmlComponent("br"));
        form.add(firstname, lastname, gender);

        if (entity.hasPhoto()) {
            Image image = new Image(new StreamResource("photo.jpg", () -> new ByteArrayInputStream(entity.photo())), "photo");
            image.setWidth("200px");
            image.setHeight("300px");
            form.add(image);
        }

        form.add(new HtmlComponent("br"));
        form.add(mobilePhone, homeEmail, homeSite);

        binder = new Binder<>(entityClass());
        entityField.bind(binder);
        binder.bind(firstname, NaturalEntity::firstname, NaturalEntity::firstname);
        binder.bind(lastname, NaturalEntity::lastname, NaturalEntity::lastname);
        binder.bind(gender, NaturalEntity::gender, NaturalEntity::gender);
        binder.bind(mobilePhone, e -> e.phoneNr(CrmTags.Type.mobile).map(PhoneNr::number).orElse(null), null);
        binder.bind(homeEmail, e -> e.email(CrmTags.Type.home).map(EmailAddress::text).orElse(null), null);
        binder.bind(homeSite, e -> e.site(CrmTags.Type.home).orElse(null), null);
        binder.readBean(entity);
        return form;
    }

    @Override
    protected NaturalEntity updateOrCreate(NaturalEntity entity) {
        return EntitiesView.updateOrCreate(entity, binder, NaturalEntity::new);
    }
}
