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

package net.tangly.bus.crm;


import java.net.URL;
import java.util.Objects;

import net.tangly.bus.core.Address;
import net.tangly.bus.core.EmailAddress;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.core.TagType;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * A taxonomy of tags for a customer relationship management system. The namespace is naturally <i>crm</i>.
 */
public final class CrmTags {
    public static final String CRM = "crm";
    public static final String GEO = "geo";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String ALTITUDE = "altitude";
    public static final String PLUSCODE = "pluscode";

    public static final String HOME = "home";
    public static final String WORK = "work";
    public static final String MOBILE = "mobile";
    private static final String BILLING = "billing";
    private static final String DELIVERY = "delivery";

    public static final String CRM_EMAIL_HOME = CRM + ":email-" + HOME;
    public static final String CRM_EMAIL_WORK = CRM + ":email-" + WORK;

    public static final String CRM_PHONE_MOBILE = CRM + ":phone-" + MOBILE;
    public static final String CRM_PHONE_HOME = CRM + ":phone-" + HOME;
    public static final String CRM_PHONE_WORK = CRM + ":phone-" + WORK;

    public static final String CRM_ADDRESS_WORK = CRM + ":address-" + WORK;
    public static final String CRM_ADDRESS_HOME = CRM + ":address-" + HOME;
    public static final String CRM_ADDRESS_BILLING = CRM + ":address-" + BILLING;
    public static final String CRM_ADDRESS_DELIVERY = CRM + ":address-" + DELIVERY;


    public static final String CRM_SITE_HOME = CRM + ":site-" + HOME;
    public static final String CRM_SITE_WORK = CRM + ":site-" + WORK;

    public static final String LINKEDIN = "linkedIn";
    public static final String SKYPE = "skype";
    public static final String GOOGLE = "google";

    public static final String CRM_IM_LINKEDIN = CRM + ":im-" + LINKEDIN;
    public static final String CRM_IM_SKYPE = CRM + ":im-" + SKYPE;
    public static final String CRM_IM_GOOGLE = CRM + ":im-" + GOOGLE;

    public static final String CRM_COMPANY_ID = CRM + ":company-id";
    public static final String CRM_VAT_NUMBER = CRM + ":vat-number";
    public static final String CRM_BANK_CONNECTION = CRM + ":bank-connection";

    public static final String CRM_EMPLOYEE_TITLE = CRM + ":title";

    /**
     * Private constructor of an utility class.
     */
    private CrmTags() {
    }

    public static void registerTags(@NotNull TagTypeRegistry registry) {
        registry.register(TagType.ofMandatory(CRM, "email-" + HOME, EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "email-" + WORK, EmailAddress.class, EmailAddress::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + MOBILE, PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + HOME, PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "phone-" + WORK, PhoneNr.class, PhoneNr::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + HOME, Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "address-" + WORK, Address.class, Address::of));
        registry.register(TagType.ofMandatory(CRM, "site-" + HOME, URL.class));
        registry.register(TagType.ofMandatory(CRM, "site-" + WORK, URL.class));
        registry.register(TagType.ofMandatory(CRM, "title", String.class));
        registry.register(TagType.ofMandatory(CRM, "im-" + LINKEDIN, String.class));
        registry.register(TagType.ofMandatory(CRM, "im-" + SKYPE, String.class));
        registry.register(TagType.ofMandatory(CRM, "im-" + GOOGLE, String.class));
        registry.register(TagType.ofMandatory(CRM, "company-id", String.class));
        registry.register(TagType.ofMandatory(CRM, "vat-number", String.class));
        registry.register(TagType.ofMandatory(CRM, "bank-connection", BankConnection.class));

        registry.register(TagType.ofMandatory(GEO, LATITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, LONGITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, ALTITUDE, Double.TYPE));
        registry.register(TagType.ofMandatory(GEO, PLUSCODE, String.class));
    }

    public static String phoneTag(@NotNull String kind) {
        return CRM + ":phone-" + Objects.requireNonNull(kind);
    }

    public static String emailTag(@NotNull String kind) {
        return CRM + ":email-" + Objects.requireNonNull(kind);
    }

    public static String addressTag(@NotNull String kind) {
        return CRM + ":address-" + Objects.requireNonNull(kind);
    }

    public static String siteTag(@NotNull String kind) {
        return CRM + ":site-" + Objects.requireNonNull(kind);
    }

    public static String imTag(@NotNull String im) {
        return CRM + ":im-" + Objects.requireNonNull(im);
    }

    public static String individualLinkedInUrl(@NotNull String linkedInReference) {
        return "https://www.linkedin.com/in/" + linkedInReference;
    }

    public static String organizationLinkedInUrl(@NotNull String linkedInReference) {
        return "https://www.linkedin.com/company/" + linkedInReference;
    }

    public static String organizationZefixUrl(@NotNull String uid) {
        return "https://www.zefix.ch/en/search/entity/list?name=" + uid + "&searchType=exact";
    }

}
