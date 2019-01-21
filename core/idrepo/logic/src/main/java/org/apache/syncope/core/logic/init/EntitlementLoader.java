/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.logic.init;

import javax.sql.DataSource;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.syncope.core.provisioning.api.EntitlementsHolder;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.apache.syncope.core.persistence.api.SyncopeCoreLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EntitlementLoader implements SyncopeCoreLoader {

    @Autowired
    private EntitlementAccessor entitlementAccessor;

    @Override
    public int getOrder() {
        return 900;
    }

    @Override
    public void load() {
        EntitlementsHolder.getInstance().init(StandardEntitlement.values());
    }

    @Override
    public void load(final String domain, final DataSource datasource) {
        AuthContextUtils.execWithAuthContext(domain, () -> {
            entitlementAccessor.addEntitlementsForAnyTypes();
            return null;
        });
    }
}
