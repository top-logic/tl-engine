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
import com.top_logic.convert.converters.ExcelFormatConverter;
import com.top_logic.convert.converters.FormatConverter;

/**
 * Testcase for the {@link ExcelFormatConverter}
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestExcelConverter extends AbstractFormatConverterTest{
    
    public static final String XLS_MIMETYPE = ExcelFormatConverter.EXCEL_MIMETYPE;
    
    /**
     * Testing method
     */
    public void testEXCELConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(XLS_MIMETYPE);
        checkFormatConverterBasics(converter, ExcelFormatConverter.class);
        checkFiles(converter, ".xls", XLS_MIMETYPE);
    }
    
    public static Test suite () {
        return createTest(TestExcelConverter.class);
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
