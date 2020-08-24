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

package net.tangly.crm.ports;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrmWorkflowsTest {
    private final static String FOLDER = "/Users/Shared/tangly";

    @Test
    @Tag("localTest")
    void writeTsvFilesTestFormCompanyFolder() {
        Crm crm = new Crm();
        CrmWorkflows crmWorkflows = new CrmWorkflows(crm);
        crmWorkflows.importCrmEntities(Paths.get(FOLDER));
        crmWorkflows.exportCrmEntities(Paths.get(FOLDER));
    }
}
