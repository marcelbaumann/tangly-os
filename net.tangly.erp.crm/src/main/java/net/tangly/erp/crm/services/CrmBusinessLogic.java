/*
 * Copyright 2006-2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.erp.crm.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import javax.inject.Inject;

import net.tangly.erp.crm.domain.Activity;
import net.tangly.erp.crm.domain.Contract;
import net.tangly.erp.crm.domain.Interaction;
import net.tangly.erp.crm.domain.InteractionCode;
import net.tangly.core.HasInterval;
import net.tangly.erp.crm.domain.Subject;
import org.jetbrains.annotations.NotNull;

/**
 * The business logic and rules of the bounded domain of CRM entities.
 */
public class CrmBusinessLogic {
    private final CrmRealm realm;

    @Inject
    public CrmBusinessLogic(@NotNull CrmRealm realm) {
        this.realm = realm;
    }

    public CrmRealm realm() {
        return realm;
    }

    public Optional<Subject> login(String username, String password) {
        return realm().subjects().items().stream().filter(o -> o.id().equals(username) && o.authenticate(password)).findAny();
    }

    public boolean changePassword(String username, String password, String newPassword) {
        var subject = realm().subjects().items().stream().filter(o -> o.id().equals(username) && o.authenticate(password)).findAny();
        subject.ifPresent(o -> o.newPassword(newPassword));
        return subject.isPresent();
    }

    /**
     * Sets the end date property of interaction to the end date of the last contract associated with the interaction in the case of customer and completed
     * state. Set the end date property of interaction to the end date of the last activity associated with the interaction in the case of lost state.
     */
    public void updateInteractions() {
        realm().interactions().items().forEach(interaction -> interaction.toDate(
            realm().contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.entity().oid()).map(Contract::toDate)
                .max(Comparator.comparing(LocalDate::toEpochDay)).orElseThrow()));
        realm().interactions().items().stream().filter(o -> o.code() == InteractionCode.lost).forEach(interaction -> interaction
            .toDate(interaction.activities().stream().map(Activity::date).max(Comparator.comparing(LocalDate::toEpochDay)).orElseThrow()));
    }

    /**
     * Returns the potential amount of all interactions in the selected time slot and tate.
     *
     * @param code defines the state of the expected interactions
     * @param from interactions should have been started after this date
     * @param to   interactions should have been started after this date
     * @return the aggregated potential amount
     */
    public BigDecimal funnel(@NotNull InteractionCode code, LocalDate from, LocalDate to) {
        return switch (code) {
            case lead, prospect, lost -> realm.interactions().items().stream().filter(o -> o.code() == code).filter(new HasInterval.IntervalFilter<>(from, to))
                .map(Interaction::weightedPotential).reduce(BigDecimal.ZERO, BigDecimal::add);
            case ordered, completed -> realm.interactions().items().stream().filter(o -> o.code() == code)
                .flatMap(interaction -> realm.contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.entity().oid()))
                .filter(new HasInterval.IntervalFilter<>(from, to)).map(Contract::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
        };
    }
}
