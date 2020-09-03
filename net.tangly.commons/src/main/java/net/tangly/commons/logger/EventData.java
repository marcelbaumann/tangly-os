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

package net.tangly.commons.logger;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Provide a simple approach to create an audit trail for all relevant operations performed in the system. Contextual information such as user, IP address are
 * part of the MDR context and implicitly available to the event data logging process.
 */
public record EventData(@NotNull String event, @NotNull LocalDateTime timestamp, @NotNull String component, @NotNull Status status, String text,
                        @NotNull Map<String, Object> data, Throwable exception) {

    public enum Status {SUCCESS, WARNING, FAILURE}

    private static final String AUDIT_LOGGER = "AuditLogger";
    private static final Marker MARKER = MarkerFactory.getMarker("AUDIT_EVENT");

    public static void log(@NotNull String event, @NotNull String component, @NotNull Status status, String reason, @NotNull Map<String, Object> data) {
        log(new EventData(event, LocalDateTime.now(), component, status, reason, data, null));
    }

    public static void log(@NotNull String event, @NotNull String component, @NotNull Status status, String reason, @NotNull Map<String, Object> data,
                           Throwable exception) {
        log(new EventData(event, LocalDateTime.now(), component, status, reason, data, exception));
    }

    public static void log(EventData data) {
        Logger logger = LoggerFactory.getLogger(AUDIT_LOGGER);
        LoggingEventBuilder builder = logger.atInfo().addMarker(MARKER);
        if (Objects.nonNull(data.exception())) {
            builder.setCause(data.exception());
        }
        builder.log("{}-{}-{}-{}:{}:{}", data.event(), data.timestamp(), data.component(), data.status(), data.text(), data.data());
    }
}