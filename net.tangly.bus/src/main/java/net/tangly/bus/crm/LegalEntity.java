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

package net.tangly.bus.crm;


import net.tangly.bus.core.EntityImp;
import net.tangly.bus.core.Tag;

/**
 * A legal entity is legally recognized organization able to underwrite contracts and hire employees. A legal entity has a name, an identity defined
 * as the legal number of an organization (e.g. zefix number in Switzerland), a text describing it.
 */
public class LegalEntity extends EntityImp implements CrmEntity {
    private static final long serialVersionUID = 1L;

    public static LegalEntity of(long oid) {
        LegalEntity entity = new LegalEntity();
        entity.oid(oid);
        return entity;
    }

    public LegalEntity() {
        // default constructor
    }

    /**
     * Returns the VAT identifying number of the legal entity.
     *
     * @return the VAT identifying number
     */
    public String vatNr() {
        var value = findBy(CrmTags.CRM_VAT_NUMBER);
        return value.map(Tag::value).orElse(null);
    }

    /**
     * Sets the VAT identifying number of the legal entity.
     *
     * @param vatNr new VAT identifying number
     */
    public void vatNr(String vatNr) {
        replace(Tag.of(CrmTags.CRM_VAT_NUMBER, vatNr));
    }
}