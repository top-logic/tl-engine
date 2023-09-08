/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import junit.framework.Test;

import com.top_logic.basic.Logger;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.PowerPointXFormatConverter;

/**
 * Testcase for the {@link PowerPointXFormatConverter}.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestPowerPointXConverter extends AbstractFormatConverterTest {

    public static final String PPTX_MIMETYPE = PowerPointXFormatConverter.POWER_POINT_X_MIMETYPE;
    
    /**
     * Testing method
     */
    public void testPPTXConverter() throws Exception {
        FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(PPTX_MIMETYPE);
        checkFormatConverterBasics(converter, PowerPointXFormatConverter.class);
        checkFiles(converter, ".pptx", PPTX_MIMETYPE);
    }

    public void testPPTXConverter2() throws Exception {
    	FormatConverter converter = FormatConverterFactory.getInstance().getFormatConverter(PPTX_MIMETYPE);
    	checkFormatConverterBasics(converter, PowerPointXFormatConverter.class);
    	String text1 = "Ganz wichtiges Text-Dokument";
    	String text2 = "Dies ist bloß ein simpler Test-Text, anhand dem man die Funktionalität des PowerPoint Konverters testen kann. Ansonsten wurde nicht weiter über den Inhalt dieses Textes nachgedacht. Das Wort Text, sollte in diesem Text jedoch am häufigsten vorkommen, weil Text öfters als andere Wörter verwendet wurde.";
    	FileInputStream input = new FileInputStream(new File(TEMPLATES_DOC_PATH + "complexTest.pptx"));
    	try {
    		Reader convert = converter.convert(input, PPTX_MIMETYPE);
    		try {
    			StringWriter buffer = new StringWriter();
    			try {
    				while (true) {
    					int read = convert.read();
    					if (read == -1) {
    						break;
    					}
						buffer.write(read);
    				}
    				assertTrue("'" + buffer.toString() + "' does not contain '" + text1 + "'", buffer.toString().contains(text1));
    				assertTrue("'" + buffer.toString() + "' does not contain '" + text2 + "'", buffer.toString().contains(text2));
				} finally {
					buffer.close();
				}
			} finally {
				convert.close();
			}
		} finally {
			input.close();
		}
    }
    
    public static Test suite () {
        return createTest(TestPowerPointXConverter.class);
    }
    
    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
