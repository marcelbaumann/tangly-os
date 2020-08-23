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

package net.tangly.bus.core;

import java.io.Serializable;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * The abstraction of an email address until the Java JDK provides one..
 */
public record EmailAddress(@NotNull String recipient, @NotNull String domain) implements Serializable {
    public static EmailAddress of(@NotNull String email) {
        String[] parts = email.split("@");
        Objects.checkFromIndexSize(0, parts.length, 2);
        return new EmailAddress(parts[0], parts[1]);
    }
}
