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

package net.tangly.bdd.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * The publisher creates a <i>AsciiDoc</i> report of the behaviour driven design tests documentation generated as JSON during JUnit 5 runs. The
 * publisher is provided as an example for realising developer driven living documentation.
 */
public class StoryAsciiDocPublisher {
    private final PrintWriter writer;

    public StoryAsciiDocPublisher(@NotNull Path bddReport, @NotNull Path report) throws IOException {
        try (Reader reader = Files.newBufferedReader(bddReport, StandardCharsets.UTF_8)) {
            JSONArray features = new JSONArray(new JSONTokener(reader));
            writer = new PrintWriter(report.toFile(), StandardCharsets.UTF_8);
            documentHeader();
            for (var item : features) {
                publishFeature((JSONObject) item);
            }
            writer.flush();
            writer.close();
        }
    }

    private void documentHeader() {
        writer.println("---");
        writer.println("title: \"bdd Report\"");
        writer.println("date: 2020-01-01");
        writer.println("weight: 60");
        writer.println("draft: false");
        writer.println("---");
        writer.println();
    }

    private void publishFeature(@NotNull JSONObject feature) {
        header("Feature: " + feature.getString(BddConstants.NAME), 2);
        paragraph(feature.getString(BddConstants.DESCRIPTION));
        publishTags(feature);
        for (var item : feature.getJSONArray(BddConstants.STORIES)) {
            publishStory((JSONObject) item);
        }
    }

    private void publishStory(@NotNull JSONObject story) {
        header("Story: " + story.getString(BddConstants.NAME), 3);
        paragraph(story.getString(BddConstants.DESCRIPTION));
        publishTags(story);
        for (var item : story.getJSONArray(BddConstants.SCENARIOS)) {
            publishScenario((JSONObject) item);
        }
    }

    private void publishTags(@NotNull JSONObject object) {
        if (!object.isNull(BddConstants.TAGS)) {
            writer.append("*tags:*");
            for (var tag : object.getJSONArray(BddConstants.TAGS)) {
                writer.append(" '").append((String) tag).append("'");
            }
            writer.println();
            writer.println();
        }
    }

    private void publishScenario(@NotNull JSONObject scenario) {
        writer.append(".Scenario: ").append(scenario.getString(BddConstants.NAME));
        writer.println();
        writer.println("[%hardbreaks]");
        clause(scenario, BddConstants.GIVEN);
        clause(scenario, BddConstants.WHEN);
        clause(scenario, BddConstants.THEN);
        writer.println();
    }

    private void clause(@NotNull JSONObject scenario, @NotNull String clause) {
        JSONObject segment = (JSONObject) scenario.get(clause);
        writer.append("*").append(clause).append("* ").append(segment.getString(BddConstants.TEXT));
        JSONArray ands = segment.optJSONArray(BddConstants.AND);
        if (ands != null) {
            for (var item : ands) {
                writer.append(" *and* ").append((String) item);
            }
        }
        writer.println();
    }

    private void header(@NotNull String text, int level) {
        writer.append("=".repeat(level)).append(" ").append(text).println();
        writer.println();
    }

    private void paragraph(@NotNull String text) {
        writer.append(text).println();
        writer.println();
    }
}
