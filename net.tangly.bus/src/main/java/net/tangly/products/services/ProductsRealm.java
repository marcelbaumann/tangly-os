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

package net.tangly.products.services;

import net.tangly.core.domain.Realm;
import net.tangly.core.providers.Provider;
import net.tangly.products.domain.Assignment;
import net.tangly.products.domain.Effort;
import net.tangly.products.domain.Product;

public interface ProductsRealm extends Realm {
    Provider<Assignment> assignments();

    Provider<Effort> efforts();

    Provider<Product> products();
}
