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

package net.tangly.crm.ports;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

import net.tangly.bus.core.Comment;
import net.tangly.bus.core.HasComments;
import net.tangly.bus.core.HasOid;
import net.tangly.bus.core.HasTags;
import net.tangly.bus.core.PhoneNr;
import net.tangly.bus.crm.Activity;
import net.tangly.bus.crm.ActivityCode;
import net.tangly.bus.crm.Contract;
import net.tangly.bus.crm.CrmRealm;
import net.tangly.bus.crm.CrmTags;
import net.tangly.bus.crm.Employee;
import net.tangly.bus.crm.GenderCode;
import net.tangly.bus.crm.Interaction;
import net.tangly.bus.crm.InteractionCode;
import net.tangly.bus.crm.LegalEntity;
import net.tangly.bus.crm.NaturalEntity;
import net.tangly.bus.crm.Subject;
import net.tangly.bus.providers.InstanceProviderInMemory;
import net.tangly.bus.providers.Provider;
import net.tangly.commons.lang.ReflectionUtilities;
import net.tangly.gleam.model.TsvEntity;
import net.tangly.gleam.model.TsvProperty;
import net.tangly.ports.TsvHdl;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.tangly.bus.crm.CrmTags.CRM_EMAIL_HOME;
import static net.tangly.bus.crm.CrmTags.CRM_EMAIL_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_EMPLOYEE_TITLE;
import static net.tangly.bus.crm.CrmTags.CRM_IM_LINKEDIN;
import static net.tangly.bus.crm.CrmTags.CRM_PHONE_MOBILE;
import static net.tangly.bus.crm.CrmTags.CRM_PHONE_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_SCHOOL;
import static net.tangly.bus.crm.CrmTags.CRM_SITE_HOME;
import static net.tangly.bus.crm.CrmTags.CRM_SITE_WORK;
import static net.tangly.bus.crm.CrmTags.CRM_VAT_NUMBER;
import static net.tangly.ports.TsvHdl.FROM_DATE;
import static net.tangly.ports.TsvHdl.OID;
import static net.tangly.ports.TsvHdl.TEXT;
import static net.tangly.ports.TsvHdl.TO_DATE;
import static net.tangly.ports.TsvHdl.addComments;
import static net.tangly.ports.TsvHdl.convertFoidTo;
import static net.tangly.ports.TsvHdl.createAddressMapping;
import static net.tangly.ports.TsvHdl.createTsvBankConnection;
import static net.tangly.ports.TsvHdl.createTsvEntityFields;
import static net.tangly.ports.TsvHdl.createTsvQualifiedEntityFields;
import static net.tangly.ports.TsvHdl.emailProperty;
import static net.tangly.ports.TsvHdl.exportEntities;
import static net.tangly.ports.TsvHdl.get;
import static net.tangly.ports.TsvHdl.importEntities;
import static net.tangly.ports.TsvHdl.phoneNrProperty;
import static net.tangly.ports.TsvHdl.tagProperty;

/**
 * Provides import and export functions for CRM entities persisted in comma separated tabs files. These files are often defined for integration testing or
 * integration of legacy systems not providing programmatic API. The description of the TSV file structure and mapping rules are of declarative nature. One 2
 * one relations are mapped through the oid of the referenced entity if defined, otherwise an empty string. One 2 many relations are mapped through an comma
 * separated list of the oid of the referenced entities if at least one is defined, otherwise an empty string.
 */
public class CrmTsvHdl {
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    private static final String OWNER_FOID = "ownerFoid";
    private static final String CREATED = "created";
    private static final String AUTHOR = "author";
    private static final String TAGS = "tags";

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CrmRealm realm;

    @Inject
    public CrmTsvHdl(@NotNull CrmRealm realm) {
        this.realm = realm;
    }

    public void importComments(@NotNull Path path) {
        Provider<Comment> comments = new InstanceProviderInMemory<>();
        importEntities(path, createTsvComment(), comments);
        realm.naturalEntities().items().forEach(e -> addComments(e, comments));
        realm.legalEntities().items().forEach(e -> addComments(e, comments));
        realm.employees().items().forEach(e -> addComments(e, comments));
        realm.contracts().items().forEach(e -> addComments(e, comments));
        realm.subjects().items().forEach(e -> addComments(e, comments));
        realm.interactions().items().forEach(e -> addComments(e, comments));
    }

    public void exportComments(@NotNull Path path) {
        Provider<Comment> comments = new InstanceProviderInMemory<>();
        realm.naturalEntities().items().forEach(o -> updateAndCollectComments(o, comments));
        realm.legalEntities().items().forEach(o -> updateAndCollectComments(o, comments));
        realm.employees().items().forEach(o -> updateAndCollectComments(o, comments));
        realm.contracts().items().forEach(o -> updateAndCollectComments(o, comments));
        realm.subjects().items().forEach(o -> updateAndCollectComments(o, comments));
        realm.interactions().items().forEach(o -> updateAndCollectComments(o, comments));
        exportEntities(path, createTsvComment(), comments);
    }

    public void importNaturalEntities(@NotNull Path path) {
        importEntities(path, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void exportNaturalEntities(@NotNull Path path) {
        exportEntities(path, createTsvNaturalEntity(), realm.naturalEntities());
    }

    public void importLegalEntities(@NotNull Path path) {
        importEntities(path, createTsvLegalEntity(), realm.legalEntities());
    }

    public void exportLegalEntities(@NotNull Path path) {
        exportEntities(path, createTsvLegalEntity(), realm.legalEntities());
    }

    public void importEmployees(@NotNull Path path) {
        importEntities(path, createTsvEmployee(), realm.employees());
    }

    public void exportEmployees(@NotNull Path path) {
        exportEntities(path, createTsvEmployee(), realm.employees());
    }

    public void importContracts(@NotNull Path path) {
        importEntities(path, createTsvContract(), realm.contracts());
    }

    public void exportContracts(@NotNull Path path) {
        importEntities(path, createTsvContract(), realm.contracts());
    }

    public void importSubjects(@NotNull Path path) {
        importEntities(path, createTsvSubject(), realm.subjects());
    }

    public void exportSubjects(@NotNull Path path) {
        exportEntities(path, createTsvSubject(), realm.subjects());
    }

    public void importInteractions(@NotNull Path path) {
        importEntities(path, createTsvInteraction(), realm.interactions());
    }

    public void exportInteractions(@NotNull Path path) {
        exportEntities(path, createTsvInteraction(), realm.interactions());
    }

    public void importActivities(@NotNull Path path) {
        Provider<Activity> activities = new InstanceProviderInMemory<>();
        importEntities(path, createTsvActivity(), activities);
        realm.interactions().items().forEach(e -> addActivities(e, activities));
    }

    public void exportActivities(@NotNull Path path) {
        Provider<Activity> activities = new InstanceProviderInMemory<>();
        realm.interactions().items().forEach(o -> updateAndCollectActivities(o, activities));
        exportEntities(path, createTsvActivity(), activities);
    }

    static <T extends HasComments & HasOid> void updateAndCollectComments(T entity, Provider<Comment> comments) {
        entity.comments().forEach(comment -> ReflectionUtilities.set(comment, OWNER_FOID, entity.oid()));
        comments.updateAll(entity.comments());
    }

    static void updateAndCollectActivities(Interaction entity, Provider<Activity> activities) {
        entity.activities().forEach(activity -> ReflectionUtilities.set(activity, "interactionFoid", entity.oid()));
        activities.updateAll(entity.activities());
    }

    static void addActivities(Interaction entity, Provider<Activity> activities) {
        entity
            .addAll(activities.items().stream().filter(o -> (Long) ReflectionUtilities.get(o, "interactionFoid") == entity.oid()).collect(Collectors.toList()));
    }


    static TsvEntity<Comment> createTsvComment() {
        Function<CSVRecord, Comment> imports = (CSVRecord record) -> Comment
            .of(LocalDateTime.parse(get(record, CREATED)), Long.parseLong(get(record, OWNER_FOID)), get(record, AUTHOR), get(record, TEXT));

        List<TsvProperty<Comment, ?>> fields =
            List.of(TsvProperty.of(OID, Comment::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong),
                TsvProperty.ofLong(OWNER_FOID, Comment::ownerFoid, null),
                TsvProperty.of(CREATED, Comment::created, null, o -> (o != null) ? LocalDateTime.parse(o) : null),
                TsvProperty.ofString(AUTHOR, Comment::author, null), TsvProperty.ofString(TEXT, Comment::text, null),
                TsvProperty.ofString(TAGS, HasTags::rawTags, HasTags::rawTags));
        return TsvEntity.of(Comment.class, fields, imports);
    }

    static TsvEntity<NaturalEntity> createTsvNaturalEntity() {
        List<TsvProperty<NaturalEntity, ?>> fields = createTsvEntityFields();
        fields.add(TsvProperty.ofString(LASTNAME, NaturalEntity::lastname, NaturalEntity::lastname));
        fields.add(TsvProperty.ofString(FIRSTNAME, NaturalEntity::firstname, NaturalEntity::firstname));
        fields.add(TsvProperty.ofEnum(GenderCode.class, "gender", NaturalEntity::gender, NaturalEntity::gender));
        fields.add(createAddressMapping(CrmTags.Type.home));
        fields.add(emailProperty(CRM_EMAIL_HOME, CrmTags.Type.home));
        fields.add(phoneNrProperty(CRM_PHONE_MOBILE, CrmTags.Type.mobile));
        fields.add(tagProperty(CRM_IM_LINKEDIN));
        fields.add(tagProperty(CRM_SITE_HOME));
        return TsvEntity.of(NaturalEntity.class, fields, NaturalEntity::new);
    }

    static TsvEntity<LegalEntity> createTsvLegalEntity() {
        List<TsvProperty<LegalEntity, ?>> fields = createTsvQualifiedEntityFields();
        fields.add(TsvProperty.ofString(CRM_VAT_NUMBER, LegalEntity::vatNr, LegalEntity::vatNr));
        fields.add(tagProperty(CRM_IM_LINKEDIN));
        fields.add(tagProperty(CRM_EMAIL_WORK));
        fields.add(tagProperty(CRM_SITE_WORK));
        fields.add(phoneNrProperty(CRM_PHONE_WORK, CrmTags.Type.work));
        fields.add(createAddressMapping(CrmTags.Type.work));
        fields.add(TsvProperty.ofString(CRM_SCHOOL, e -> (e.containsTag(CRM_SCHOOL) ? "Y" : "N"), (e, p) -> {
            if ("Y".equals(p)) {
                e.tag(CRM_SCHOOL, null);
            }
        }));
        return TsvEntity.of(LegalEntity.class, fields, LegalEntity::new);
    }

    TsvEntity<Employee> createTsvEmployee() {
        List<TsvProperty<Employee, ?>> fields =
            List.of(TsvProperty.of(OID, Employee::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong),
                TsvProperty.of(FROM_DATE, Employee::fromDate, Employee::fromDate, TsvProperty.CONVERT_DATE_FROM),
                TsvProperty.of(TO_DATE, Employee::toDate, Employee::toDate, TsvProperty.CONVERT_DATE_FROM),
                TsvProperty.ofString(TEXT, Employee::text, Employee::text),
                TsvProperty.of("personOid", Employee::person, Employee::person, e -> findNaturalEntityByOid(e).orElse(null), convertFoidTo()), TsvProperty
                    .of("organizationOid", Employee::organization, Employee::organization, e -> findLegalEntityByOid(e).orElse(null), convertFoidTo()),
                tagProperty(CRM_EMPLOYEE_TITLE), tagProperty(CRM_EMAIL_WORK), TsvProperty
                    .ofString(CRM_PHONE_WORK, e -> e.phoneNr(CrmTags.Type.work).map(PhoneNr::number).orElse(""), (e, p) -> e.phoneNr(CrmTags.Type.work, p)));
        return TsvEntity.of(Employee.class, fields, Employee::new);
    }

    TsvEntity<Contract> createTsvContract() {
        List<TsvProperty<Contract, ?>> fields = createTsvQualifiedEntityFields();
        fields.add(TsvProperty.of("locale", Contract::locale, Contract::locale, TsvHdl::toLocale, Locale::getLanguage));
        fields.add(TsvProperty.of("currency", Contract::currency, Contract::currency, Currency::getInstance, Currency::getCurrencyCode));
        fields.add(TsvProperty.of(createTsvBankConnection(), Contract::bankConnection, Contract::bankConnection));
        fields.add(TsvProperty.ofBigDecimal("amountWithoutVat", Contract::amountWithoutVat, Contract::amountWithoutVat));
        fields.add(TsvProperty.of("sellerOid", Contract::seller, Contract::seller, e -> findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(TsvProperty.of("selleeOid", Contract::sellee, Contract::sellee, e -> findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        return TsvEntity.of(Contract.class, fields, Contract::new);
    }

    TsvEntity<Subject> createTsvSubject() {
        List<TsvProperty<Subject, ?>> fields = createTsvQualifiedEntityFields();
        fields.add(TsvProperty.of("userOid", Subject::user, Subject::user, e -> findNaturalEntityByOid(e).orElse(null), convertFoidTo()));
        fields.add(TsvProperty.ofString("gravatarEmail", Subject::gravatarEmail, Subject::gravatarEmail));
        fields.add(TsvProperty.ofString("passwordSalt", Subject::passwordSalt, Subject::passwordSalt));
        fields.add(TsvProperty.ofString("passwordHash", Subject::passwordHash, Subject::passwordHash));
        fields.add(TsvProperty.ofString("gmailUsername", Subject::gmailUsername, Subject::gmailUsername));
        fields.add(TsvProperty.ofString("gmailPassword", Subject::gmailPassword, Subject::gmailPassword));
        return TsvEntity.of(Subject.class, fields, Subject::new);
    }

    TsvEntity<Interaction> createTsvInteraction() {
        List<TsvProperty<Interaction, ?>> fields = createTsvQualifiedEntityFields();
        fields.add(TsvProperty.of("state", Interaction::code, Interaction::code, e -> Enum.valueOf(InteractionCode.class, e.toLowerCase()), Enum::name));
        fields.add(TsvProperty.ofBigDecimal("potential", Interaction::potential, Interaction::potential));
        fields.add(TsvProperty.ofBigDecimal("probability", Interaction::probability, Interaction::probability));
        fields
            .add(TsvProperty.of("legalEntity", Interaction::legalEntity, Interaction::legalEntity, e -> findLegalEntityByOid(e).orElse(null), convertFoidTo()));
        return TsvEntity.of(Interaction.class, fields, Interaction::new);
    }

    static TsvEntity<Activity> createTsvActivity() {
        List<TsvProperty<Activity, ?>> fields =
            List.of(TsvProperty.of(OID, Activity::oid, (entity, value) -> ReflectionUtilities.set(entity, OID, value), Long::parseLong), TsvProperty
                    .of("interactionFoid", Activity::interactionFoid, (entity, value) -> ReflectionUtilities.set(entity, "interactionFoid", value),
                        Long::parseLong),
                TsvProperty.of("code", Activity::code, Activity::code, e -> Enum.valueOf(ActivityCode.class, e.toLowerCase()), Enum::name),
                TsvProperty.of("date", Activity::date, Activity::date, TsvProperty.CONVERT_DATE_FROM),
                TsvProperty.ofInt("durationInMinutes", Activity::duration, Activity::duration),
                TsvProperty.ofString(AUTHOR, Activity::author, Activity::author), TsvProperty.ofString(TEXT, Activity::text, Activity::text));
        return TsvEntity.of(Activity.class, fields, Activity::new);
    }

    public Optional<NaturalEntity> findNaturalEntityByOid(String identifier) {
        return (identifier != null) ? realm.naturalEntities().find(Long.parseLong(identifier)) : Optional.empty();
    }

    private Optional<LegalEntity> findLegalEntityByOid(String identifier) {
        return (identifier != null) ? realm.legalEntities().find(Long.parseLong(identifier)) : Optional.empty();
    }
}
