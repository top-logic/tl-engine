/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Test;

import com.top_logic.basic.Logger;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.HTMLFormatConverter;

/**
 * Testcase for the {@link HTMLFormatConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestHTMLConverter extends AbstractFormatConverterTest {

    public static final String HTML_MIMETYPE = HTMLFormatConverter.HTML_MIMETYPE; //XHTML and XML is also available
    
    /**
     * Testing method
     */
    public void testHTMLConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(HTML_MIMETYPE);
        checkFormatConverterBasics(converter, HTMLFormatConverter.class);
        FileInputStream theStream = new FileInputStream(new File(TESTING_DOC_PATH + ".html"));
        assertTestingPurposeInStream(converter, theStream, HTML_MIMETYPE);
    }
    
    
    /**
     * ConvertTestSetup is necessary, since xml properties needs to be loaded 
     */
    public static Test suite () {
    	return createTest(TestHTMLConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
