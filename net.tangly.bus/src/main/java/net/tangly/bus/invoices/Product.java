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

package net.tangly.bus.invoices;


import java.math.BigDecimal;

import net.tangly.bus.core.HasQualifiers;
import org.jetbrains.annotations.NotNull;

/**
 * A product sold by a legal entity and referenced in an invoice or an invoice line. A product is a {@link HasQualifiers} mixin because the identifiier is
 * external and the Java record structure does not support internal {@link net.tangly.bus.core.HasOid} manipulation through reflection.
 *
 * @param id        unique external identifier of the product
 * @param name      human readable name of the product
 * @param text      human readable description of the product
 * @param unitPrice unit price of the product
 * @param unit      unit type of the product such as day, hour,or fix for a workshop
 * @param vatRate   VAT rate of the product, requested for specific VAT tax regimes
 */
public record Product(@NotNull String id, String name, String text, @NotNull BigDecimal unitPrice, String unit, BigDecimal vatRate) implements HasQualifiers {
}
