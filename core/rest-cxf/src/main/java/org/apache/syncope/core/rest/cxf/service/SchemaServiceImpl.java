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
package org.apache.syncope.core.rest.cxf.service;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.syncope.common.lib.to.AbstractSchemaTO;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.service.SchemaService;
import org.apache.syncope.core.logic.SchemaLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchemaServiceImpl extends AbstractServiceImpl implements SchemaService {

    @Autowired
    private SchemaLogic logic;

    @Override
    public <T extends AbstractSchemaTO> Response create(final SchemaType schemaType, final T schemaTO) {
        T created = logic.create(schemaType, schemaTO);

        URI location = uriInfo.getAbsolutePathBuilder().path(created.getKey()).build();
        return Response.created(location).
                header(RESTHeaders.RESOURCE_KEY, created.getKey()).
                build();
    }

    @Override
    public void delete(final SchemaType schemaType, final String schemaKey) {
        logic.delete(schemaType, schemaKey);
    }

    @Override
    public <T extends AbstractSchemaTO> List<T> list(final SchemaType schemaType) {
        return logic.list(schemaType);
    }

    @Override
    public <T extends AbstractSchemaTO> T read(final SchemaType schemaType, final String schemaKey) {
        return logic.read(schemaType, schemaKey);
    }

    @Override
    public <T extends AbstractSchemaTO> void update(
            final SchemaType schemaType, final String schemaKey, final T schemaTO) {

        schemaTO.setKey(schemaKey);
        logic.update(schemaType, schemaTO);
    }
}
