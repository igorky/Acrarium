/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.liquibase;

import liquibase.changelog.ChangeSet;

/**
 * @author lukas
 * @since 01.06.18
 */
public abstract class LiquibaseChangePostProcessor {
    private final String changeId;

    protected LiquibaseChangePostProcessor(String changeId) {
        this.changeId = changeId;
    }

    void handle(ChangeSet changeSet) {
        if (changeId.equals(changeSet.getId())) {
            afterChange();
        }
    }

    protected abstract void afterChange();
}
