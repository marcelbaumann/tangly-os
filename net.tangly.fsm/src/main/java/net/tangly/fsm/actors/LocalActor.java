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

package net.tangly.fsm.actors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.tangly.fsm.Event;
import net.tangly.fsm.StateMachine;
import net.tangly.fsm.dsl.FsmBuilder;
import org.jetbrains.annotations.NotNull;

import static net.tangly.fsm.actors.LocalActors.instance;

/**
 * A local actor implements the actor contract as a local thread running in the virtual machine. An actor completes when its finite state machine reaches a
 * final state and the associated thread is returned to the pool of available threads.
 *
 * @param <O> the class of the instance owning the finite state machine instance
 * @param <S> the state enumeration type uniquely identifying a state in the state machine
 * @param <E> the event enumeration type uniquely identifying the event sent to the state machine
 */
public class LocalActor<O extends LocalActor, S extends Enum<S>, E extends Enum<E>> implements Actor<E>, Runnable {

    /**
     * Logger of the instances of the class.
     */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocalActor.class);

    /**
     * Queue of received events waiting to be processed.
     */
    private final BlockingQueue<Event<E>> events;

    /**
     * The final state machine defining the behavior of the actor class.
     */
    private final StateMachine<O, S, E> fsm;

    /**
     * Constructor of the class.
     *
     * @param builder builder to create the finite state machine of the local actor
     * @param name    name of the local actor
     */
    @SuppressWarnings("unchecked")
    public LocalActor(@NotNull FsmBuilder<O, S, E> builder, @NotNull String name) {
        this.fsm = builder.machine(name, (O) this);
        events = new LinkedBlockingQueue<>();
        ((LocalActors<E>) instance()).register(this);
    }

    @Override
    public String name() {
        return fsm.name();
    }

    @Override
    public boolean isAlive() {
        return fsm.isAlive();
    }

    @Override
    public void receive(@NotNull Event<E> event) {
        try {
            events.put(event);
            log.atInfo().log("Actor {} received event {}", name(), event);
        } catch (InterruptedException e) {
            log.atError().log("Actor {} encountered interrupted exception {}", name(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (fsm.isAlive()) {
            try {
                Event<E> event = events.take();
                fsm.fire(event);
            } catch (InterruptedException e) {
                log.atError().log("Actor {} encountered interrupted exception {}", name(), e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
