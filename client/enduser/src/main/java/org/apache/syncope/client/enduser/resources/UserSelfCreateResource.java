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
package org.apache.syncope.client.enduser.resources;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.syncope.client.enduser.SyncopeWebApplication;
import org.apache.syncope.client.enduser.SyncopeEnduserConstants;
import org.apache.syncope.client.enduser.SyncopeEnduserSession;
import org.apache.syncope.client.enduser.annotations.Resource;
import org.apache.syncope.client.enduser.util.Validation;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.request.UserCR;
import org.apache.syncope.common.lib.Attr;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.rest.api.service.UserSelfService;

@Resource(key = "userSelfCreate", path = "/api/self/create")
public class UserSelfCreateResource extends BaseUserSelfResource {

    private static final long serialVersionUID = -2721621682300247583L;

    private boolean isSelfRegistrationAllowed() {
        Boolean result = null;
        try {
            result = SyncopeEnduserSession.get().getPlatformInfo().isSelfRegAllowed();
        } catch (SyncopeClientException e) {
            LOG.error("While seeking if self registration is allowed", e);
        }

        return result == null
                ? false
                : result;
    }

    @Override
    protected ResourceResponse newResourceResponse(final Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        response.setContentType(MediaType.TEXT_PLAIN);
        try {
            HttpServletRequest request = (HttpServletRequest) attributes.getRequest().getContainerRequest();

            if (!xsrfCheck(request)) {
                LOG.error("XSRF TOKEN is not matching");
                response.setError(Response.Status.BAD_REQUEST.getStatusCode(), "XSRF TOKEN is not matching");
                return response;
            }

            String jsonString = request.getReader().readLine();
            final UserTO userTO = MAPPER.readValue(jsonString, UserTO.class);

            if (!captchaCheck(
                    request.getHeader("captcha"),
                    request.getSession().getAttribute(SyncopeEnduserConstants.CAPTCHA_SESSION_KEY))) {

                throw new IllegalArgumentException("Entered captcha is not matching");
            }

            if (isSelfRegistrationAllowed() && userTO != null) {
                LOG.debug("User self registration request for [{}]", userTO.getUsername());
                LOG.trace("Request is [{}]", userTO);

                // check if request is compliant with customization form rules
                if (Validation.isCompliant(userTO,
                        SyncopeWebApplication.get().getCustomFormAttributes(), true)) {

                    // 1. membership attributes management
                    Set<Attr> membAttrs = new HashSet<>();
                    userTO.getPlainAttrs().stream().
                            filter(attr -> (attr.getSchema().
                            contains(SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR))).
                            forEach(attr -> {
                                String[] simpleAttrs = attr.getSchema().split(
                                        SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR);
                                MembershipTO membership = userTO.getMemberships().stream().
                                        filter(memb -> simpleAttrs[0].equals(memb.getGroupName())).
                                        findFirst().orElse(null);
                                if (membership == null) {
                                    membership = new MembershipTO.Builder(null).groupName(simpleAttrs[0]).build();
                                    userTO.getMemberships().add(membership);
                                }

                                Attr clone = SerializationUtils.clone(attr);
                                clone.setSchema(simpleAttrs[1]);
                                membership.getPlainAttrs().add(clone);
                                membAttrs.add(attr);
                            });
                    userTO.getPlainAttrs().removeAll(membAttrs);

                    // 2. millis -> Date conversion for PLAIN attributes of USER and its MEMBERSHIPS
                    SyncopeEnduserSession.get().getDatePlainSchemas().stream().map(plainSchema -> {
                        millisToDate(userTO.getPlainAttrs(), plainSchema);
                        return plainSchema;
                    }).forEach(plainSchema -> {
                        userTO.getMemberships().forEach(membership -> {
                            millisToDate(membership.getPlainAttrs(), plainSchema);
                        });
                    });

                    membAttrs.clear();
                    userTO.getDerAttrs().stream().
                            filter(attr -> (attr.getSchema().
                            contains(SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR))).
                            forEach(attr -> {
                                String[] simpleAttrs = attr.getSchema().split(
                                        SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR);
                                MembershipTO membership = userTO.getMemberships().stream().
                                        filter(memb -> simpleAttrs[0].equals(memb.getGroupName())).
                                        findFirst().orElse(null);
                                if (membership == null) {
                                    membership = new MembershipTO.Builder(null).groupName(simpleAttrs[0]).build();
                                    userTO.getMemberships().add(membership);
                                }

                                Attr clone = SerializationUtils.clone(attr);
                                clone.setSchema(simpleAttrs[1]);
                                membership.getDerAttrs().add(clone);
                                membAttrs.add(attr);
                            });
                    userTO.getDerAttrs().removeAll(membAttrs);

                    membAttrs.clear();
                    userTO.getVirAttrs().stream().
                            filter(attr -> (attr.getSchema().
                            contains(SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR))).
                            forEach(attr -> {
                                String[] simpleAttrs = attr.getSchema().split(
                                        SyncopeEnduserConstants.MEMBERSHIP_ATTR_SEPARATOR);
                                MembershipTO membership = userTO.getMemberships().stream().
                                        filter(memb -> simpleAttrs[0].equals(memb.getGroupName())).
                                        findFirst().orElse(null);
                                if (membership == null) {
                                    membership = new MembershipTO.Builder(null).groupName(simpleAttrs[0]).build();
                                    userTO.getMemberships().add(membership);
                                }

                                Attr clone = SerializationUtils.clone(attr);
                                clone.setSchema(simpleAttrs[1]);
                                membership.getVirAttrs().add(clone);
                                membAttrs.add(attr);
                            });
                    userTO.getVirAttrs().removeAll(membAttrs);

                    LOG.debug("Received user self registration request for user: [{}]", userTO.getUsername());
                    LOG.trace("Received user self registration request is: [{}]", userTO);

                    // adapt request and create user
                    UserCR req = new UserCR.Builder(userTO.getRealm(), userTO.getUsername()).
                            password(userTO.getPassword()).
                            mustChangePassword(userTO.isMustChangePassword()).
                            securityQuestion(userTO.getSecurityQuestion()).
                            securityAnswer(userTO.getSecurityAnswer()).
                            auxClasses(userTO.getAuxClasses()).
                            plainAttrs(userTO.getPlainAttrs()).
                            virAttrs(userTO.getVirAttrs()).
                            memberships(userTO.getMemberships()).
                            relationships(userTO.getRelationships()).
                            roles(userTO.getRoles()).
                            resources(userTO.getResources()).
                            build();
                    Response res = SyncopeEnduserSession.get().getService(UserSelfService.class).create(req);

                    buildResponse(response, res.getStatus(),
                            Response.Status.Family.SUCCESSFUL.equals(res.getStatusInfo().getFamily())
                            ? "User[ " + userTO.getUsername() + "] successfully created"
                            : "ErrorMessage{{ " + res.getStatusInfo().getReasonPhrase() + " }}");
                } else {
                    LOG.warn(
                            "Incoming create request [{}] is not compliant with form customization rules. "
                            + "Create NOT allowed", userTO.getUsername());
                    buildResponse(response, Response.Status.OK.getStatusCode(),
                            "User: " + userTO.getUsername() + " successfully created");
                }
            } else {
                response.setError(Response.Status.FORBIDDEN.getStatusCode(), new StringBuilder().
                        append("ErrorMessage{{").append(userTO == null
                        ? "Request received is not valid }}"
                        : "Self registration not allowed }}").toString());
            }
        } catch (Exception e) {
            LOG.error("Unable to create userTO", e);
            response.setError(Response.Status.BAD_REQUEST.getStatusCode(),
                    new StringBuilder().
                            append("ErrorMessage{{ ").
                            append(e.getMessage()).
                            append(" }}").
                            toString());
        }
        return response;
    }
}
