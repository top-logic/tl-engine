/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.text;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.importer.text.TextImportParser;
import com.top_logic.importer.text.TextImportParser.Date;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestTextImportParser extends AbstractTypedConfigurationTestCase {

    @Override
    protected Map<String, ConfigurationDescriptor> getDescriptors() {
        return Collections.singletonMap("parser", TypedConfiguration.getConfigurationDescriptor(TextImportParser.Config.class));
    }

    public void testText() throws Exception {
        TextImportParser.Text theParser = this.getParser("text.xml");

        assertEquals("Failed to parse null",         "",      theParser.map(null));
        assertEquals("Failed to parse empty string", "",      theParser.map(""));
        assertEquals("Failed to parse \"Hallo\"",    "Hallo", theParser.map("Hallo"));
    }
    
    public void testBoolean() throws Exception {
        TextImportParser.Boolean theParser = this.getParser("boolean.xml");
        
        assertEquals("Failed to parse null",         null,          theParser.map(null));
        assertEquals("Failed to parse empty string", Boolean.FALSE, theParser.map(""));
        assertEquals("Failed to parse \"true\"",     Boolean.TRUE,  theParser.map("true"));
        assertEquals("Failed to parse \"false\"",    Boolean.FALSE, theParser.map("false"));
        assertEquals("Failed to parse \"Hallo\"",    Boolean.FALSE, theParser.map("Hallo"));
    }

    public void testDate() throws Exception {
        String         theString = "20160101-12:00:00";
        java.util.Date theDate   = CalendarUtil.newSimpleDateFormat("yyyyMMdd-hh:mm:ss").parse(theString);
        Date           theParser = this.getParser("date.xml");

        assertEquals("Failed to parse null",         null,    theParser.map(null));
        assertEquals("Failed to parse empty string", null,    theParser.map(""));
        assertEquals("Failed to parse \"1.234\"",    theDate, theParser.map("20160101-12:00:00"));

        this.doTestFailExpected(theParser, "Hallo");
    }

    public void testLong() throws Exception {
        TextImportParser.Long theParser = this.getParser("long.xml");

        assertEquals("Failed to parse null",         null,                theParser.map(null));
        assertEquals("Failed to parse empty string", null,                theParser.map(""));
        assertEquals("Failed to parse \"1.234\"",    Long.valueOf(1234l), theParser.map("1.234"));
        assertEquals("Failed to parse \"1,234\"",    Long.valueOf(1l),    theParser.map("1,234"));

        this.doTestFailExpected(theParser, "Hallo");
    }

    public void testLongEN() throws Exception {
        TextImportParser.Long theParser = this.getParser("longEN.xml");
        
        assertEquals("Failed to parse null",         null,                theParser.map(null));
        assertEquals("Failed to parse empty string", null,                theParser.map(""));
        assertEquals("Failed to parse \"1,234\"",    Long.valueOf(1234l), theParser.map("1,234"));
        assertEquals("Failed to parse \"1.234\"",    Long.valueOf(1l),    theParser.map("1.234"));

        this.doTestFailExpected(theParser, "Hallo");
    }

    public void testInteger() throws Exception {
        TextImportParser.Integer theParser = this.getParser("integer.xml");

        assertEquals("Failed to parse null",         null,                  theParser.map(null));
        assertEquals("Failed to parse empty string", null,                  theParser.map(""));
        assertEquals("Failed to parse \"1.234\"",    Integer.valueOf(1234), theParser.map("1.234"));
        assertEquals("Failed to parse \"1,234\"",    Integer.valueOf(1),    theParser.map("1,234"));

        this.doTestFailExpected(theParser, "Hallo");
    }

    public void testIntegerEN() throws Exception {
        TextImportParser.Integer theParser = this.getParser("integerEN.xml");
        
        assertEquals("Failed to parse null",         null,                  theParser.map(null));
        assertEquals("Failed to parse empty string", null,                  theParser.map(""));
        assertEquals("Failed to parse \"1,234\"",    Integer.valueOf(1234), theParser.map("1,234"));
        assertEquals("Failed to parse \"1.234\"",    Integer.valueOf(1),    theParser.map("1.234"));

        this.doTestFailExpected(theParser, "Hallo");
    }
    
    public void testFloat() throws Exception {
        TextImportParser.Float theParser = this.getParser("float.xml");
        
        assertEquals("Failed to parse null",         null,                  theParser.map(null));
        assertEquals("Failed to parse empty string", null,                  theParser.map(""));
        assertEquals("Failed to parse \"1.234\"",    Float.valueOf(1234f),  theParser.map("1.234"));
        assertEquals("Failed to parse \"1,234\"",    Float.valueOf(1.234f), theParser.map("1,234"));
        
        this.doTestFailExpected(theParser, "Hallo");
    }
    
    public void testFloatEN() throws Exception {
        TextImportParser.Float theParser = this.getParser("floatEN.xml");
        
        assertEquals("Failed to parse null",         null,                theParser.map(null));
        assertEquals("Failed to parse empty string", null,                theParser.map(""));
        assertEquals("Failed to parse \"1,234\"",    Float.valueOf(1234f),  theParser.map("1,234"));
        assertEquals("Failed to parse \"1.234\"",    Float.valueOf(1.234f), theParser.map("1.234"));
        
        this.doTestFailExpected(theParser, "Hallo");
    }

    protected void doTestFailExpected(TextImportParser<?> aParser, String aString) {
        try {
            Object theResult = aParser.map(aString);

            fail("Parsing \"" + aString + "\" returned '" + theResult + "' for parser " + aParser);
        }
        catch (IllegalArgumentException ex) {
            Logger.info("Parser '" + aParser.toString() + "' failed, expected!", TestTextImportParser.class);
            // expected
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends TextImportParser<?>> T getParser(String aKey) throws IOException, ConfigurationException {
        PolymorphicConfiguration<T> theConfig = (PolymorphicConfiguration<T>) readConfiguration(TestTextImportParser.class, this.getDescriptors(), aKey, null);

        return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(theConfig);
    }

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestTextImportParser.class);
    }
}

