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

package net.tangly.commons.products.ui;

import java.lang.invoke.MethodHandles;
import javax.inject.Inject;

import com.vaadin.flow.component.grid.Grid;
import net.tangly.bus.products.Assignment;
import net.tangly.bus.products.ProductsBusinessLogic;
import net.tangly.commons.vaadin.EntitiesView;
import net.tangly.commons.vaadin.GridFiltersAndActions;
import net.tangly.commons.vaadin.InternalEntitiesView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssignementsView extends InternalEntitiesView<Assignment> {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ProductsBusinessLogic logic;

    @Inject
    public AssignementsView(@NotNull ProductsBusinessLogic logic, @NotNull Mode mode) {
        super(Assignment.class, mode, logic.realm().assignements(), logic.realm().tagTypeRegistry());
        this.logic = logic;
        initialize();
    }

    @Override
    protected void initialize() {
        Grid<Assignment> grid = grid();
        InternalEntitiesView.addQualifiedEntityColumns(grid);
        grid.addColumn(Assignment::collaboratorId).setKey("collaboratorId").setHeader("Collaborator").setSortable(true).setAutoWidth(true)
            .setResizable(true);
        grid.addColumn(e -> (e.product() != null) ? e.product().name() : null).setKey("project").setHeader("Project").setSortable(true).setAutoWidth(true)
            .setResizable(true);
        GridFiltersAndActions<Assignment> gridFunctions = gridFiltersAndActions();
        gridFunctions.actions().addItem("Print", e -> new CommandCreateAssignmentDocument(selectedItem(), logic));
        addAndExpand(gridFunctions, grid(), gridButtons());
    }

    @Override
    protected Assignment updateOrCreate(Assignment entity) {
        return EntitiesView.updateOrCreate(entity, binder, Assignment::new);
    }
}
