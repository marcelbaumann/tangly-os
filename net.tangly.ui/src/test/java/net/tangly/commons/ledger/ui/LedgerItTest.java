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

package net.tangly.commons.ledger.ui;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import net.tangly.bus.ledger.LedgerBoundedDomain;
import net.tangly.bus.ledger.LedgerBusinessLogic;
import net.tangly.core.TypeRegistry;
import net.tangly.ledger.ports.LedgerAdapter;
import net.tangly.ledger.ports.LedgerEntities;
import net.tangly.ledger.ports.LedgerHdl;
import org.junit.jupiter.api.BeforeEach;

class LedgerItTest {
    static LedgerBoundedDomain ofDomain() {
        var realm = new LedgerEntities();
        return new LedgerBoundedDomain(realm, new LedgerBusinessLogic(realm), new LedgerHdl(realm, null), new LedgerAdapter(realm, null), new TypeRegistry());
    }

    @BeforeEach
    void setup() {
        MockVaadin.setup();
    }

}
