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
import com.top_logic.convert.converters.RtfFormatConverter;

/**
 * Testcase for the {@link RtfFormatConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestRtfConverter extends AbstractFormatConverterTest {

    public static final String RTF_MIMETYPE = RtfFormatConverter.RTF_MIMETYPE;
    
    
    /**
     * Testing method
     */
    public void testRtfConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(RTF_MIMETYPE);
        checkFormatConverterBasics(converter, RtfFormatConverter.class);
        checkFiles(converter, ".rtf", RTF_MIMETYPE);
    }
    
    public static Test suite () {
        return createTest(TestRtfConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
