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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.tangly.bus.core.EntityImp;
import org.jetbrains.annotations.NotNull;

/**
 * Defines an interaction between your company and a set of legal and natural entities. Legal entities are the organizations you want a contract with. The
 * natural entities are the persons in these organizations you are communicated with. The interaction has a set of activities moving your negotiation through
 * stages. The final result is a contract or a lost opportunity.
 */
public class Interaction extends EntityImp {
    private final List<Activity> activities;
    private LegalEntity legalEntity;
    private InteractionCode state;
    private BigDecimal potential;
    private BigDecimal probability;

    public Interaction() {
        activities = new ArrayList<>();
        this.state = InteractionCode.prospect;
        this.potential = BigDecimal.ZERO;
        this.probability = BigDecimal.ZERO;
    }

    public LegalEntity legalEntity() {
        return legalEntity;
    }

    public void legalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity;
    }

    public BigDecimal anticipatedRevenue() {
        return potential.multiply(probability);
    }

    public InteractionCode state() {
        return state;
    }

    public void state(@NotNull InteractionCode state) {
        this.state = state;
    }

    public BigDecimal potential() {
        return potential;
    }

    public void potential(@NotNull BigDecimal potential) {
        this.potential = potential;
    }

    public BigDecimal probability() {
        return probability;
    }

    public void probability(@NotNull BigDecimal probability) {
        this.probability = probability;
    }

    public List<Activity> activities() {
        return Collections.unmodifiableList(activities);
    }

    public void add(Activity activity) {
        activities.add(activity);
    }

    public void remove(Activity activity) {
        activities.remove(activity);
    }

    public boolean isValid() {
        return (Objects.requireNonNull(potential).compareTo(BigDecimal.ZERO) > 0) && (Objects.requireNonNull(probability).compareTo(BigDecimal.ZERO) > 0);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Interaction[oid=%s, id=%s, name=%s, fromDate=%s, toDate=%s, text=%s, state=%s, potential=%s, probability=%s, tags=%s]",
                oid(), id(), name(), fromDate(), toDate(), text(), state(), potential(), probability(), tags());
    }

}
