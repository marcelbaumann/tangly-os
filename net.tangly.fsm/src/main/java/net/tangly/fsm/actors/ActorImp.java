/*
 * Copyright 2021 Marcel Baumann
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package net.tangly.fsm.actors;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public abstract class ActorImp<T> implements Actor<T>, Runnable {
    private static final Logger logger = LogManager.getLogger();
    private final UUID id;
    private final String name;
    private final BlockingQueue<T> messages;

    public ActorImp(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.messages = new LinkedBlockingQueue<>();
    }


    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    @Override
    public void receive(@NotNull T message) {
        try {
            messages.put(message);
            logger.atInfo().log("Actor {} received event {}", name(), message);
        } catch (InterruptedException e) {
            logger.atError().log("Actor {} encountered interrupted exception {}", name(), e);
            Thread.currentThread().interrupt();
        }
    }

    protected T message() {
        try {
            return messages.take();
        } catch (InterruptedException e) {
            logger.atError().log("Actor {} encountered interrupted exception {}", name(), e);
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
