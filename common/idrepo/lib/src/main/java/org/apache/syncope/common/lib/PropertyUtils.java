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
package org.apache.syncope.common.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for manipulating properties files.
 */
public final class PropertyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyUtils.class);

    public static Properties read(
            final Class<?> clazz, final String propertiesFileName, final String confDirProp) {

        Properties props = new Properties();

        try (InputStream is = clazz.getResourceAsStream("/" + propertiesFileName)) {
            props.load(is);

            String confDirName = props.getProperty(confDirProp);
            if (confDirName != null) {
                File confDir = new File(confDirName);
                if (confDir.exists() && confDir.canRead() && confDir.isDirectory()) {
                    File confDirProps = new File(confDir, propertiesFileName);
                    if (confDirProps.exists() && confDirProps.canRead() && confDirProps.isFile()) {
                        props.clear();
                        props.load(new FileInputStream(confDirProps));
                    }
                } else {
                    confDir = null;
                    LOG.warn("{} not existing, unreadable or not a directory, ignoring", confDirName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not read " + propertiesFileName, e);
        }

        return props;
    }

    /**
     * Private default constructor, for static-only classes.
     */
    private PropertyUtils() {
    }
}
