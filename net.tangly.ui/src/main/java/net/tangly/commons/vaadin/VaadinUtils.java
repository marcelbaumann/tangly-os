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

package net.tangly.commons.vaadin;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Function;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.jetbrains.annotations.NotNull;

public final class VaadinUtils {
    public static final DecimalFormat FORMAT = new DecimalFormat("###,##0.00", new DecimalFormatSymbols(Locale.US));

    private VaadinUtils() {
    }

    public static TextField createTextField(String label, String placeholder) {
        return createTextField(label, placeholder, false, true);
    }

    public static TextField createTextField(String label, String placeholder, boolean readonly) {
        return createTextField(label, placeholder, readonly, true);
    }

    public static TextField createTextField(String label, String placeholder, boolean readonly, boolean enabled) {
        TextField field = new TextField(label, placeholder);
        field.setReadOnly(readonly);
        field.setEnabled(enabled);
        return field;
    }

    public static <C extends HasValue<?, T>, T> void setValue(@NotNull C component, T value) {
        if ((value != null)) {
            component.setValue(value);
        } else {
            component.clear();
        }
    }

    public static void setResponsiveSteps(@NotNull FormLayout layout) {
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("25em", 1), new FormLayout.ResponsiveStep("32em", 2), new FormLayout.ResponsiveStep("40em", 3));
    }

    public static <T extends Composite<?> & HasValue> void clear(T composite) {
        composite.getChildren().filter(HasValue.class::isInstance).map(HasValue.class::cast).forEach(e -> ((HasValue<?, ?>) e).clear());
    }

    public static <T extends Composite<?> & HasValue> void setReadOnly(T composite, boolean readOnly) {
        composite.setReadOnly(readOnly);
        composite.getChildren().filter(HasValue.class::isInstance).map(HasValue.class::cast).forEach(e -> ((HasValue<?, ?>) e).setReadOnly(readOnly));
    }

    public static <E> void initialize(@NotNull Grid<E> grid) {
        grid.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
        grid.setVerticalScrollingEnabled(true);
        grid.setWidthFull();
    }

    public static void setAttribute(Component component, String attribute, Object value) {
        component.getUI().ifPresent(o -> o.getSession().setAttribute(attribute, value));
    }

    public static Object getAttribute(Component component, String attribute) {
        return component.getUI().isPresent() ? component.getUI().get().getSession().getAttribute(attribute) : null;
    }

    public static BigDecimal toBigDecimal(String value) {
        return (value == null) ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static String format(BigDecimal value) {
        return FORMAT.format(value);
    }

    public static <T> ComponentRenderer<Span, T> coloredRender(Function<T, BigDecimal> getter, NumberFormat numberFormat) {
        return new ComponentRenderer<>((T e) -> {
            BigDecimal v = getter.apply(e);
            return switch (BigDecimal.ZERO.compareTo(v)) {
                case -1 -> new Span(numberFormat.format(v));
                case 0 -> new Span();
                case 1 -> {
                    Span s = new Span(numberFormat.format(v));
                    s.getElement().getStyle().set("color", "red");
                    yield s;
                }
                default -> new Span("");
            };
        });
    }
}
