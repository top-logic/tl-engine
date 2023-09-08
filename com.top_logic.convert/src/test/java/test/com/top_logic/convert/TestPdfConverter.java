/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.IOException;

import junit.framework.Test;

import com.top_logic.basic.Logger;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.PdfFormatConverter;

/**
 * Testcase for the {@link PdfFormatConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestPdfConverter extends AbstractFormatConverterTest {

    public static final String PDF_MIMETYPE = PdfFormatConverter.PDF_MIMETYPE;
    
    /**
     * Testing method
     */
    public void testPDFConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(PDF_MIMETYPE);
        checkFormatConverterBasics(converter, PdfFormatConverter.class);
        checkFiles(converter, ".pdf", PDF_MIMETYPE);
    }
    
    public static Test suite () {
    	return createTest(TestPdfConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }
    
}
