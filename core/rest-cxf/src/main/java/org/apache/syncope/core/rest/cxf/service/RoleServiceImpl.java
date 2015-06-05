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
import org.apache.syncope.common.lib.to.RoleTO;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.service.RoleService;
import org.apache.syncope.core.logic.RoleLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends AbstractServiceImpl implements RoleService {

    @Autowired
    private RoleLogic logic;

    @Override
    public List<RoleTO> list() {
        return logic.list();
    }

    @Override
    public RoleTO read(final Long roleKey) {
        return logic.read(roleKey);
    }

    @Override
    public Response create(final RoleTO roleTO) {
        RoleTO created = logic.create(roleTO);
        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getKey())).build();
        return Response.created(location).
                header(RESTHeaders.RESOURCE_KEY, created.getKey()).
                build();
    }

    @Override
    public void update(final Long roleKey, final RoleTO roleTO) {
        roleTO.setKey(roleKey);
        logic.update(roleTO);
    }

    @Override
    public void delete(final Long roleKey) {
        logic.delete(roleKey);
    }

}
