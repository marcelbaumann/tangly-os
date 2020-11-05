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

package net.tangly.bus.products;

import net.tangly.core.QualifiedEntityImp;

/**
 * An assignment defines connection of an employee to a project for a duration. The start date shall be equal or greater to the start date of project. The end
 * date is optional, if defined it shall smaller or equal to the end date of the project.
 */
public class Assignment extends QualifiedEntityImp {
    private Product product;
    private String collaboratorId;

    public Assignment() {
    }

    public Product product() {
        return product;
    }

    public void product(Product product) {
        this.product = product;
    }

    public String collaboratorId() {
        return collaboratorId;
    }

    public void collaboratorId(String collaboratorId) {
        this.collaboratorId = collaboratorId;
    }
}


