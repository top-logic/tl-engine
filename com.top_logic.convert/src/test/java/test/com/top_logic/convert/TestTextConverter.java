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
import com.top_logic.convert.converters.TextPlainConverter;

/**
 * Testcase for the {@link TextPlainConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestTextConverter extends AbstractFormatConverterTest {
    
    public static final String TXT_MIMETYPE = TextPlainConverter.TXT_MIMETYPE;
    
    /**
     * Testing method
     */
    public void testTextConversion() throws Exception{
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(TXT_MIMETYPE);
        checkFormatConverterBasics(converter, TextPlainConverter.class);
        FileInputStream theStream = new FileInputStream(new File(TESTING_DOC_PATH + ".txt"));
        assertTestingPurposeInStream(converter, theStream, TXT_MIMETYPE);
    }
    
    public static Test suite () {
        return createTest(TestTextConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
