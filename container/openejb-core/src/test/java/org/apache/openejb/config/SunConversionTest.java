/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import static org.apache.openejb.config.SunConversion.extractPortId;
import junit.framework.TestCase;

public class SunConversionTest extends TestCase {
    public void testExtractPortId() throws Exception {
        assertEquals("dir/file", extractPortId("file:repo1/dir", "META-INF/wsdl/file.wsdl"));
        assertEquals("dir/file", extractPortId("file:repo1/dir", "WEB-INF/wsdl/file.wsdl"));
        assertEquals("dir/META-INF/file", extractPortId("file:repo1/dir", "META-INF/file.wsdl"));
        assertEquals("dir/WEB-INF/file", extractPortId("file:repo1/dir", "WEB-INF/file.wsdl"));
        assertEquals("dir/file", extractPortId("file:repo1/dir", "file.wsdl"));
        assertEquals("dir/file.xml", extractPortId("file:repo1/dir", "META-INF/wsdl/file.xml"));
        assertEquals("dir/file.xml", extractPortId("file:repo1/dir", "WEB-INF/wsdl/file.xml"));

        String x = "file:repo1/dir";
        assertEquals("dir", SunConversion.extractPortId(x));
        assertEquals("dir/file", SunConversion.extractPortId("file:repo1/dir/file.wsdl"));
        assertEquals("dir/file.xml", SunConversion.extractPortId("file:repo1/dir/file.xml"));
        assertEquals(null, SunConversion.extractPortId("file:repo1/"));

        assertEquals("dir/file", SunConversion.extractPortId("http://server.com:port/dir/file?WSDL"));
        assertEquals("dir/file", SunConversion.extractPortId("http://server.com:port/dir/file?wsdl"));
        assertEquals("dir/file", SunConversion.extractPortId("http://server.com:port/dir/file?cheese"));
        assertEquals("dir/file", SunConversion.extractPortId("http://server.com:port/dir/file"));
        assertEquals("file", SunConversion.extractPortId("http://server.com:port/file?WSDL"));
        assertEquals("file", SunConversion.extractPortId("http://server.com:port/file?wsdl"));
        assertEquals("file", SunConversion.extractPortId("http://server.com:port/file?cheese"));
        assertEquals("file", SunConversion.extractPortId("http://server.com:port/file"));
        assertEquals(null, SunConversion.extractPortId("http://server.com:port/?WSDL"));
        assertEquals(null, SunConversion.extractPortId("http://server.com:port/?wsdl"));
        assertEquals(null, SunConversion.extractPortId("http://server.com:port/?cheese"));
        assertEquals(null, SunConversion.extractPortId("http://server.com:port/"));
        assertEquals("dir/file/", SunConversion.extractPortId("http://server.com:port/dir/file/?WSDL"));
        assertEquals("dir/file/", SunConversion.extractPortId("http://server.com:port/dir/file/?wsdl"));
        assertEquals("dir/file/", SunConversion.extractPortId("http://server.com:port/dir/file/?cheese"));
        assertEquals("dir/file/", SunConversion.extractPortId("http://server.com:port/dir/file/"));
        assertEquals("dir/file", SunConversion.extractPortId("https://server.com:port/dir/file?WSDL"));
        assertEquals("dir/file", SunConversion.extractPortId("https://server.com:port/dir/file?wsdl"));
        assertEquals("dir/file", SunConversion.extractPortId("https://server.com:port/dir/file?cheese"));
        assertEquals("dir/file", SunConversion.extractPortId("https://server.com:port/dir/file"));
        assertEquals("file", SunConversion.extractPortId("https://server.com:port/file?WSDL"));
        assertEquals("file", SunConversion.extractPortId("https://server.com:port/file?wsdl"));
        assertEquals("file", SunConversion.extractPortId("https://server.com:port/file?cheese"));
        assertEquals("file", SunConversion.extractPortId("https://server.com:port/file"));
        assertEquals(null, SunConversion.extractPortId("https://server.com:port/?WSDL"));
        assertEquals(null, SunConversion.extractPortId("https://server.com:port/?wsdl"));
        assertEquals(null, SunConversion.extractPortId("https://server.com:port/?cheese"));
        assertEquals(null, SunConversion.extractPortId("https://server.com:port/"));
        assertEquals("dir/file/", SunConversion.extractPortId("https://server.com:port/dir/file/?WSDL"));
        assertEquals("dir/file/", SunConversion.extractPortId("https://server.com:port/dir/file/?wsdl"));
        assertEquals("dir/file/", SunConversion.extractPortId("https://server.com:port/dir/file/?cheese"));
        assertEquals("dir/file/", SunConversion.extractPortId("https://server.com:port/dir/file/"));

        assertEquals(null, SunConversion.extractPortId(null));
    }
}
