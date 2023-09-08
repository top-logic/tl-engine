/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.IOException;

import junit.framework.Test;

import com.top_logic.basic.Logger;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.MsWordXFormatConverter;

/**
 * Testcase for the {@link MsWordXFormatConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestWordXConverter extends AbstractFormatConverterTest {

    public static final String DOC_MIMETYPE = MsWordXFormatConverter.MS_WORD_X_MIMETYPE;
    
    
    /**
     * Testing method
     */
    public void testWordXConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(DOC_MIMETYPE);
        checkFormatConverterBasics(converter, MsWordXFormatConverter.class);
        checkFiles(converter, ".docx", DOC_MIMETYPE);
    }
    
    public static Test suite () {
        return createTest(TestWordXConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {

        // SHOW_TIME = true;    // for debugging
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
