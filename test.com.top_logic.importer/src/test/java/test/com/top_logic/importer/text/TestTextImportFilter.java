/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.importer.text;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.importer.text.TextFileImportParser.MappingConfig;
import com.top_logic.importer.text.TextFileImportParser.TextLine;
import com.top_logic.importer.text.TextImportFilter;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestTextImportFilter extends AbstractTypedConfigurationTestCase {

    public interface Config extends PolymorphicConfiguration<TestTextImportFilter> {

        @Mandatory
        @InstanceFormat
        ConfigurableFilter<?> getFilter();

        @Mandatory
        @Key(MappingConfig.NAME_ATTRIBUTE)
        Map<String, MappingConfig> getMappings();
    }

    @Override
    protected Map<String, ConfigurationDescriptor> getDescriptors() {
        return Collections.singletonMap("test-filter", TypedConfiguration.getConfigurationDescriptor(TestTextImportFilter.Config.class));
    }

    public void testEquals() throws Exception {
        ConfigurableFilter<?> theFilter = this.getFilter("equals.xml");

        assertEquals("Failed to filter null",         FilterResult.FALSE, this.matches(theFilter, null));
        assertEquals("Failed to filter empty string", FilterResult.FALSE, this.matches(theFilter, ""));
        assertEquals("Failed to filter \"Hallo\"",    FilterResult.TRUE,  this.matches(theFilter, "Hallo"));
        assertEquals("Failed to filter \"Huhu\"",     FilterResult.FALSE, this.matches(theFilter, "Huhu"));

        this.doTestInvalidValue(theFilter);
    }

    public void testEmpty() throws Exception {
        ConfigurableFilter<?> theFilter = this.getFilter("empty.xml");
        
        assertEquals("Failed to filter null",         FilterResult.TRUE,  this.matches(theFilter, null));
        assertEquals("Failed to filter empty string", FilterResult.TRUE,  this.matches(theFilter, ""));
        assertEquals("Failed to filter \"Hallo\"",    FilterResult.FALSE, this.matches(theFilter, "Hallo"));

        this.doTestInvalidValue(theFilter);
    }

    public void testLength() throws Exception {
        ConfigurableFilter<?> theFilter = this.getFilter("length.xml");

        assertEquals("Failed to filter null",         FilterResult.FALSE, this.matches(theFilter, null));
        assertEquals("Failed to filter empty string", FilterResult.FALSE, this.matches(theFilter, ""));
        assertEquals("Failed to filter \"Hallo\"",    FilterResult.TRUE,  this.matches(theFilter, "Hallo"));
        assertEquals("Failed to filter \"Huhu\"",     FilterResult.FALSE, this.matches(theFilter, "Huhu"));

        this.doTestInvalidValue(theFilter);
    }

    public void testLarger() throws Exception {
        ConfigurableFilter<?> theFilter = this.getFilter("larger.xml");
        
        assertEquals("Failed to filter null",         FilterResult.INAPPLICABLE, this.matches(theFilter, null));
        assertEquals("Failed to filter empty string", FilterResult.FALSE, this.matches(theFilter, ""));
        assertEquals("Failed to filter \"Hallo\"",    FilterResult.TRUE,  this.matches(theFilter, "Hallo"));
        assertEquals("Failed to filter \"Haallo\"",   FilterResult.FALSE, this.matches(theFilter, "Haallo"));

        this.doTestInvalidValue(theFilter);
    }
    
    public void testSmaller() throws Exception {
        ConfigurableFilter<?> theFilter = this.getFilter("smaller.xml");
        
        assertEquals("Failed to filter null",         FilterResult.INAPPLICABLE, this.matches(theFilter, null));
        assertEquals("Failed to filter empty string", FilterResult.TRUE,  this.matches(theFilter, ""));
        assertEquals("Failed to filter \"Hallo\"",    FilterResult.TRUE,  this.matches(theFilter, "Hallo"));
        assertEquals("Failed to filter \"Huhu\"",     FilterResult.FALSE, this.matches(theFilter, "Huhu"));

        this.doTestInvalidValue(theFilter);
    }

    protected void doTestInvalidValue(ConfigurableFilter<?> aFilter) {
        try {
            this.matches(aFilter, Boolean.TRUE);

            fail("Failed to filter Boolean.TRUE");
        }
        catch (IllegalArgumentException ex) {
            // Expected
            Logger.info("Filter '" + aFilter.toString() + "' failed, expected!", TestTextImportFilter.class);
        }
    }

    protected FilterResult matches(ConfigurableFilter<?> aFilter, Object aString) {
        return aFilter.matches(this.getText(aString));
    }

    protected TextLine getText(final Object aString) {
        Map<String, MappingConfig> theMap = Collections.emptyMap();

        return new TextLine(theMap, 0) {
            @Override
            protected Object getValue(MappingConfig aConfig) throws ParseException {
                if ((aString instanceof String) || (aString == null)) { 
                    return aString;
                }
                else {
                    throw new ParseException("Value is no string but " + aString.getClass(), 0);
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    protected <T extends ConfigurableFilter<?>> T getFilter(String aKey) throws IOException, ConfigurationException {
        TestTextImportFilter.Config theConfig = (Config) this.readConfiguration(TestTextImportFilter.class, this.getDescriptors(), aKey, null);
        T                           theFilter = (T) theConfig.getFilter();
        Map<String, MappingConfig>  theMap    = theConfig.getMappings();

        TextImportFilter.initFilter(theFilter, theMap);

        return theFilter;
    }

    public static Test suite() {
        return TLTestSetup.createTLTestSetup(TestTextImportFilter.class);
    }
}

