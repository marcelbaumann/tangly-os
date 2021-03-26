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

package net.tangly.core;

import java.time.LocalDate;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

/**
 * Mixin indicating the class has the capability to have a date.
 */
public interface HasDate {
    record IntervalFilter<T extends HasDate>(LocalDate from, LocalDate to) implements Predicate<T> {
        public boolean test(@NotNull T entity) {
            return (from == null || !from.isAfter(entity.date())) && (to == null || !to.isBefore(entity.date()));
        }
    }

    /**
     * Returns the date of entity
     *
     * @return the date of the entity
     */
    LocalDate date();
}
