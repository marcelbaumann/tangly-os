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

package net.tangly.erp.crm;

import net.tangly.commons.models.Entity;
import net.tangly.commons.models.EntityImp;

public class Employee extends EntityImp implements CrmEntity {

    private NaturalEntity person;
    private transient LegalEntity organization;

    public Employee(long oid, String id) {
        super(oid, id);
    }

    public NaturalEntity person() {
        return person;
    }

    public void person(NaturalEntity person) {
        this.person = person;
    }

    public LegalEntity organization() {
        return organization;
    }

    public void organization(LegalEntity organization) {
        this.organization = organization;
    }
}
