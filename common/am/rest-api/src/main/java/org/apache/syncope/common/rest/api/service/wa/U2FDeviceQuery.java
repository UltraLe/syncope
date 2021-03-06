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

package org.apache.syncope.common.rest.api.service.wa;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.syncope.common.rest.api.beans.AbstractQuery;
import org.apache.syncope.common.rest.api.service.JAXRSService;

import javax.ws.rs.QueryParam;

import java.util.Date;

public class U2FDeviceQuery extends AbstractQuery {
    private static final long serialVersionUID = -7381828286332101171L;

    private Long id;

    private String entityKey;

    private Date expirationDate;

    private String owner;

    @Parameter(name = JAXRSService.PARAM_ENTITY_KEY, in = ParameterIn.QUERY,
        schema = @Schema(implementation = String.class, example = "50592942-73ec-44c4-a377-e859524245e4"))
    public String getEntityKey() {
        return entityKey;
    }

    @QueryParam(JAXRSService.PARAM_ENTITY_KEY)
    public void setEntityKey(final String entityKey) {
        this.entityKey = entityKey;
    }

    @Parameter(name = "id", in = ParameterIn.QUERY, schema = @Schema(implementation = Long.class))
    public Long getId() {
        return id;
    }

    @QueryParam("id")
    public void setId(final Long id) {
        this.id = id;
    }

    @Parameter(name = "expirationDate", in = ParameterIn.QUERY, schema = @Schema(implementation = Date.class))
    public Date getExpirationDate() {
        return expirationDate;
    }

    @QueryParam("expirationDate")
    public void setExpirationDate(final Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Parameter(name = "owner", in = ParameterIn.QUERY, schema = @Schema(implementation = String.class))
    public String getOwner() {
        return owner;
    }

    @QueryParam("owner")
    public void setOwner(final String owner) {
        this.owner = owner;
    }

    public static class Builder extends AbstractQuery.Builder<U2FDeviceQuery, U2FDeviceQuery.Builder> {
        @Override
        protected U2FDeviceQuery newInstance() {
            return new U2FDeviceQuery();
        }

        public U2FDeviceQuery.Builder entityKey(final String entityKey) {
            getInstance().setEntityKey(entityKey);
            return this;
        }

        public U2FDeviceQuery.Builder owner(final String owner) {
            getInstance().setOwner(owner);
            return this;
        }

        public U2FDeviceQuery.Builder id(final Long id) {
            getInstance().setId(id);
            return this;
        }

        public U2FDeviceQuery.Builder expirationDate(final Date date) {
            getInstance().setExpirationDate(date);
            return this;
        }
    }
}
