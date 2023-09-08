/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.convert.ConverterMapping;
import com.top_logic.convert.converters.PdfFormatConverter;
import com.top_logic.convert.converters.TextPlainConverter;

/**
 * Testcase for the {@link ConverterMapping}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestConverterMapping extends BasicTestCase {

    public static final String TXT_MIMETYPE = TextPlainConverter.TXT_MIMETYPE;
    
    public static final String PDF_MIMETYPE = PdfFormatConverter.PDF_MIMETYPE;
    
    /**
     * Check equals-method of convertermapping 
     */
    public void testConverterMappingsEquals() {
        ConverterMapping mapping = new ConverterMapping(TXT_MIMETYPE, TXT_MIMETYPE);
        ConverterMapping pdfMapping = new ConverterMapping(PDF_MIMETYPE, PDF_MIMETYPE);
        
        assertTrue(mapping.equals(mapping));
        assertFalse(mapping.equals(null));
        assertFalse(mapping.equals(this));
        assertFalse(mapping.equals(pdfMapping));
    }
    
    public void testToString() {
        ConverterMapping mapping = new ConverterMapping(TXT_MIMETYPE, TXT_MIMETYPE);
        assertEquals("com.top_logic.convert.ConverterMapping[" + TXT_MIMETYPE + "->" + TXT_MIMETYPE + ']', mapping.toString());
    }
    
    /**
     * ConvertTestSetup is necessary, since xml properties needs to be loaded 
     */
    public static Test suite () {

        return ConvertTestSetup.createConvertTestSetup(new TestSuite(TestConverterMapping.class));
    }
    
    /** 
     * main method for direct testing
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
