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

import net.tangly.bus.core.HasQualifiers;

/**
 * Mixin for views supporting selection of a item, in general related to a grid. The item should provide a id, a name and a text for display in the view.
 *
 * @param <T> tyoe of the items displayed in the view
 */
public interface HasIdView<T extends HasQualifiers> {
    T selectedItem();

    void selectedItem(T selectedItem);
}