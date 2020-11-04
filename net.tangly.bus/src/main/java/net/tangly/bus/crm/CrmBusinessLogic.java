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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import javax.inject.Inject;

import net.tangly.bus.core.HasInterval;
import net.tangly.bus.core.TagTypeRegistry;
import org.jetbrains.annotations.NotNull;

/**
 * The business logic and rules of the bounded domain of CRM entities.
 */
public class CrmBusinessLogic {
    private final CrmRealm realm;
    private final CrmHandler handler;
    private final CrmPort port;

    @Inject
    public CrmBusinessLogic(@NotNull CrmRealm realm, CrmHandler handler, CrmPort port) {
        this.realm = realm;
        this.handler = handler;
        this.port = port;
    }

    public CrmRealm realm() {
        return realm;
    }

    public CrmHandler handler() {
        return handler;
    }

    public CrmPort port() {
        return port;
    }


    public void registerTags(@NotNull TagTypeRegistry registry) {
        CrmTags.registerTags(registry);
    }

    /**
     * Set the end date property of interaction to the end date of the last contract associated with the interaction in the case of customer and completed
     * state. Set the end date property of interaction to the end date of the last activity associated with the interaction in the case of lost state.
     */
    public void updateInteractions() {
        realm().interactions().items().forEach(interaction -> interaction
                .toDate(realm().contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.legalEntity().oid())
                        .map(Contract::toDate).max(Comparator.comparing(LocalDate::toEpochDay)).get()));
        realm().interactions().items().stream().filter(o -> o.code() == InteractionCode.lost).forEach(interaction -> interaction
                .toDate(interaction.activities().stream().map(Activity::date).max(Comparator.comparing(LocalDate::toEpochDay)).get()));
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
            case customer, completed -> realm.interactions().items().stream().filter(o -> o.code() == code)
                    .flatMap(interaction -> realm.contracts().items().stream().filter(contract -> contract.sellee().oid() == interaction.legalEntity().oid()))
                    .filter(new HasInterval.IntervalFilter<>(from, to)).map(Contract::amountWithoutVat).reduce(BigDecimal.ZERO, BigDecimal::add);
        };
    }
}