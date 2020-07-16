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

import java.util.List;

import com.vaadin.flow.data.provider.DataProvider;
import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.crm.ports.Crm;
import net.tangly.commons.vaadin.EntitiesView;
import org.jetbrains.annotations.NotNull;

public class LegalEntitiesView extends CrmEntitiesView<LegalEntity> {
    public LegalEntitiesView(@NotNull Crm crm) {
        super(crm, LegalEntity.class, EntitiesView::defineGrid, crm.legalEntities());
    }

    @Override
    protected LegalEntity create() {
        return new LegalEntity();
    }
}
