/*
 * Copyright 2006-2018 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License.
 */

package net.tangly.fsm.imp;

import net.tangly.fsm.Event;
import net.tangly.fsm.StateMachineEventHandler;
import net.tangly.fsm.dsl.FsmBuilder;
import net.tangly.fsm.utilities.StaticChecker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the example of bbv fsm library described https://code.google.com/p/bbvfsm/wiki/GettingStarted.
 */
class FsmBbvTest {
    @Test
    void simplyTurnOnAndOffTest() {
        var fsm = FsmBbv.build().machine("test-fsm", new FsmBbv());
        fsm.addEventHandler(new StateMachineEventHandler<>() {
        });
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB.exitDAB.exitOn.OnToOff.entryOff");
    }

    @Test
    void turnOnToggleModeTurnOffTurnOnTest() {
        var fsm = FsmBbv.build().machine("test-fsm", new FsmBbv());
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(new Event<>(FsmBbv.Events.ToggleMode));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay");
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitPlay.exitFM.exitOn.OnToOff.entryOff");
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitOff.OffToOn.entryOn.entryFM.entryPlay");
    }

    @Test
    void turnOnToggleModeStationLostTurnOffTurnOnTest() {
        var fsm = FsmBbv.build().machine("test-fsm", new FsmBbv());
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(new Event<>(FsmBbv.Events.ToggleMode));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay");
        fsm.fire(new Event<>(FsmBbv.Events.StationLost));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitPlay.PlayToAutoTune.entryAutoTune");
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitAutoTune.exitFM.exitOn.OnToOff.entryOff");
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitOff.OffToOn.entryOn.entryFM.entryAutoTune");
    }

    @Test
    void whenMaintenanceTurnOnTurnOffTest() {
        var fsm = FsmBbv.build().machine("test-fm", new FsmBbv());
        fsm.context().setMaintenance(true);
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToMaintenance.entryMaintenance");
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitMaintenance.MaintenanceToOff.entryOff");
    }

    @Test
    void whenDabStoreStationTest() {
        var fsm = FsmBbv.build().machine("test-fm", new FsmBbv());
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        fsm.fire(new Event<>(FsmBbv.Events.StoreStation));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB.DABToDAB");
    }

    @Test
    void whenPlayStoreStationTest() {
        var fsm = FsmBbv.build().machine("test-fm", new FsmBbv());
        fsm.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
        fsm.fire(new Event<>(FsmBbv.Events.ToggleMode));
        fsm.fire(new Event<>(FsmBbv.Events.StoreStation));
        assertThat(fsm.context().consumeLog()).isEqualTo("exitDAB.DABtoFM.entryFM.entryPlay.PlayToPlay");
    }

    @Test
    void independentParallelUsageTest() {
        FsmBuilder<FsmBbv, FsmBbv.States, FsmBbv.Events> builder = FsmBbv.build();
        var fsm1 = builder.machine("test-fsm1", new FsmBbv());
        var fsm2 = builder.machine("test-fsm2", new FsmBbv());
        fsm1.context().setMaintenance(true);
        fsm1.fire(new Event<>(FsmBbv.Events.TogglePower));
        fsm2.fire(new Event<>(FsmBbv.Events.TogglePower));
        assertThat(fsm1.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToMaintenance.entryMaintenance");
        assertThat(fsm2.context().consumeLog()).isEqualTo("entryOff.exitOff.OffToOn.entryOn.entryDAB");
    }

    @Test
    void staticCheckerTest() {
        FsmBuilder<FsmBbv, FsmBbv.States, FsmBbv.Events> builder = FsmBbv.build();
        StaticChecker<FsmBbv, FsmBbv.States, FsmBbv.Events> checker = new StaticChecker<>();
        assertThat(checker.check(builder.definition()).size()).isEqualTo(0);
        assertThat(checker.checkStateHasAtMostOneInitialState(builder.definition()).size()).isEqualTo(0);
        assertThat(checker.checkStateIdUsedOnce(builder.definition()).size()).isEqualTo(0);
        assertThat(checker.checkStateWithAfferentTransitionHasInitialState(builder.definition()).size()).isEqualTo(0);
    }
}
