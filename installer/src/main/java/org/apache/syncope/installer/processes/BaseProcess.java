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
package org.apache.syncope.installer.processes;

import static org.apache.syncope.installer.processes.ArchetypeProcess.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;

public class BaseProcess {

    protected final static Properties properties = new Properties();

    protected String syncopeInstallDir;

    private static InputStream is = null;

    static {
        try {
            is = BaseProcess.class.getResourceAsStream("/installer.properties");
            properties.load(is);
        } catch (final IOException e) {
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected void setSyncopeInstallDir(final String installPath, final String artifactId) {
        final StringBuilder path = new StringBuilder();
        path.append(installPath);
        path.append("/");
        path.append(artifactId);
        path.append("/");
        syncopeInstallDir = path.toString();
    }
}
