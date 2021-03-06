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
package org.apache.syncope.fit.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.util.UUID;
import javax.ws.rs.core.Response;
import org.apache.syncope.client.lib.AnonymousAuthenticationHandler;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.OIDCJWKSTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.common.lib.types.JWSAlgorithm;
import org.apache.syncope.common.rest.api.service.wa.WAOIDCJWKSService;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class OIDCJWKSITCase extends AbstractITCase {

    private static WAOIDCJWKSService waOIDCJWKSService;

    @BeforeAll
    public static void setup() {
        assumeTrue(clientFactory.getContentType() == SyncopeClientFactoryBean.ContentType.JSON);

        SyncopeClient anonymous = clientFactory.create(
                new AnonymousAuthenticationHandler(ANONYMOUS_UNAME, ANONYMOUS_KEY));
        waOIDCJWKSService = anonymous.getService(WAOIDCJWKSService.class);
    }

    @Test
    public void deleteGetSet() {
        try {
            oidcJWKSService.delete();

            waOIDCJWKSService.get();
            fail("Should not locate an OIDC JWKS");
        } catch (SyncopeClientException e) {
            assertEquals(ClientExceptionType.NotFound, e.getType());
        }

        Response response = waOIDCJWKSService.set(2048, JWSAlgorithm.RS256);
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        try {
            waOIDCJWKSService.set(2048, JWSAlgorithm.RS512);
            fail("Should not recreate an OIDC JWKS");
        } catch (SyncopeClientException e) {
            assertEquals(ClientExceptionType.EntityExists, e.getType());
        }
    }

    private static OIDCJWKSTO getCurrentJwksTO() {
        try {
            return waOIDCJWKSService.get();
        } catch (final SyncopeClientException e) {
            if (e.getType() == ClientExceptionType.NotFound) {
                Response response = waOIDCJWKSService.set(2048, JWSAlgorithm.RS256);
                assertEquals(HttpStatus.CREATED.value(), response.getStatus());
                return waOIDCJWKSService.get();
            }
        }
        throw new NotFoundException("Unable to locate current OIDC JWKS");
    }

    @Test
    public void update() throws Exception {
        oidcJWKSService.delete();

        RSAKey jwk = new RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .generate();
        String json = new JWKSet(jwk).toString();

        assertDoesNotThrow(() -> {
            OIDCJWKSTO currentTO = getCurrentJwksTO();
            currentTO.setJson(json);
            oidcJWKSService.update(currentTO);
        });
        OIDCJWKSTO currentTO = getCurrentJwksTO();
        assertEquals(json, currentTO.getJson());
    }
}
