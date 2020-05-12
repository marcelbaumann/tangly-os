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

package net.tangly.erp.crm.models.apps;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Crm {
    private TagTypeRegistry tagTypeRegistry;
    private final List<NaturalEntity> naturalEntities;
    private final List<LegalEntity> legalEntities;
    private final List<Employee> employees;
    private final List<Contract> contracts;

    public Crm() {
        tagTypeRegistry = new TagTypeRegistry();
        naturalEntities = new ArrayList<>();
        legalEntities = new ArrayList<>();
        employees = new ArrayList<>();
        contracts = new ArrayList<>();
    }

    public TagTypeRegistry tagTypeRegistry() {
        return tagTypeRegistry;
    }

    public void addNaturalEntities(Collection<NaturalEntity> naturalEntities) {
        this.naturalEntities.addAll(naturalEntities);
    }

    public List<NaturalEntity> naturalEntities() {
        return Collections.unmodifiableList(naturalEntities);
    }

    public void addLegalEntities(Collection<LegalEntity> legalEntities) {
        this.legalEntities.addAll(legalEntities);
    }

    public List<LegalEntity> legalEntities() {
        return Collections.unmodifiableList(legalEntities);
    }

    public void addEmployees(Collection<Employee> employees) {
        this.employees.addAll(employees);
    }

    public List<Employee> employees() {
        return Collections.unmodifiableList(employees);
    }

    public void addContracts(Collection<Contract> contracts) {
        this.contracts.addAll(contracts);
    }

    public List<Contract> contracts() {
        return Collections.unmodifiableList(contracts);
    }
}
