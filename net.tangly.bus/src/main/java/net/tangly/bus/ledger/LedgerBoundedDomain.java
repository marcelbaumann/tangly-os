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

package net.tangly.bus.ledger;

import net.tangly.bus.core.TagTypeRegistry;
import net.tangly.commons.app.BoundedDomain;

public class LedgerBoundedDomain extends BoundedDomain<LedgerRealm, LedgerBusinessLogic, LedgerHandler, LedgerPort> {
    public LedgerBoundedDomain(LedgerRealm realm, LedgerBusinessLogic logic, LedgerHandler handler, LedgerPort port) {
        super(realm, logic, handler, port);
    }
}
