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


import java.net.URI;
import java.util.Optional;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.Tag;

public interface CrmEntity extends HasTags {
    default void setTag(String kind, String value) {
        if (value != null) {
            replace(Tag.of(CrmTags.CRM, kind, value));
        } else {
            findBy(CrmTags.CRM, kind).ifPresent(this::remove);
        }
    }

    default Optional<PhoneNr> findPhoneNr(String kind) {
        return findBy(CrmTags.getPhoneTag(kind)).map(o -> PhoneNr.of(o.value()));
    }

    default void setPhoneNr(String kind, String phoneNr) {
        if (phoneNr != null) {
            replace(Tag.of(CrmTags.getPhoneTag(kind), phoneNr));
        } else {
            removeTagNamed(CrmTags.getPhoneTag(kind));
        }
    }

    default Optional<String> findEmail(String kind) {
        return findBy(CrmTags.getEmailTag(kind)).map(Tag::value);
    }

    default void setEmail(String kind, String email) {
        if (email != null) {
            replace(Tag.of(CrmTags.getEmailTag(kind), email));
        } else {
            removeTagNamed(CrmTags.getEmailTag(kind));
        }
    }

    default Address address(String kind) {
        var value = findBy(kind);
        return value.map(Tag::value).map(Address::of).orElse(null);
    }

    default void address(String kind, Address address) {
        if (address != null) {
            replace(Tag.of(kind, address.text()));
        } else {
            removeTagNamed(kind);
        }
    }

    default void setIm(String kind, String reference) {
        if (reference != null) {
            replace(Tag.of(CrmTags.getImTag(kind), reference));
        } else {
            removeTagNamed(CrmTags.getImTag(kind));
        }
    }

    default void setSite(String kind, URI site) {
        if (site != null) {
            replace(Tag.of(CrmTags.getSiteTag(kind), site.toString()));
        } else {
            removeTagNamed(CrmTags.getSiteTag(kind));
        }
    }
}
