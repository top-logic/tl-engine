/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.ExcelFormatConverter;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.HTMLFormatConverter;
import com.top_logic.convert.converters.MsWordFormatConverter;
import com.top_logic.convert.converters.PdfFormatConverter;
import com.top_logic.convert.converters.PowerPointFormatConverter;
import com.top_logic.convert.converters.RtfFormatConverter;
import com.top_logic.convert.converters.TextPlainConverter;

/** This is a collection of Test for the implementation of
 *  {@link FormatConverterFactory}.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class TestFormatConverterFactory extends BasicTestCase {

    public static final String TXT_MIMETYPE = "text/plain";
    
    public static final String PDF_MIMETYPE = PdfFormatConverter.PDF_MIMETYPE;
    
    public static final String WORD_MIMETYPE = MsWordFormatConverter.MSWORD_MIMETYPE;
    
    public static final String EXCEL_MIMETYPE = ExcelFormatConverter.EXCEL_MIMETYPE;
    
    public static final String PPT_MIMETYPE = PowerPointFormatConverter.POWER_POINT_MIMETYPE;
    
    public static final String HTML_MIMETYPE = HTMLFormatConverter.HTML_MIMETYPE;
    
    public static final String RTF_MIMETYPE = RtfFormatConverter.RTF_MIMETYPE;

    public static final String TESTING_PURPOSE = "just for testing purpose";
    
	public static final String TESTING_DOC_PATH =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/convert/templates/testing";
    
    /**
     * Constructor for TestFormatConverterFactory.
     */
    public TestFormatConverterFactory(String name) {
        super(name);
    }

    public void testGetConverter() {
        FormatConverterFactory factory = FormatConverterFactory.getInstance();
        assertNotNull(factory);
        int configuredConverterCount = 12;
		assertEquals("Adopt test configuration", configuredConverterCount, factory.getConverterCount());
        
        // Test if all standard converters are present
        FormatConverter txtConverter = factory.getFormatConverter(TXT_MIMETYPE, TXT_MIMETYPE);
        checkConverters(txtConverter, TextPlainConverter.class);
        
        FormatConverter pdfConverter = factory.getFormatConverter(PDF_MIMETYPE, TXT_MIMETYPE);
        checkConverters(pdfConverter, PdfFormatConverter.class);
        
        FormatConverter wordConverter = factory.getFormatConverter(WORD_MIMETYPE, TXT_MIMETYPE);
        checkConverters(wordConverter, MsWordFormatConverter.class);
        
        FormatConverter excelConverter = factory.getFormatConverter(EXCEL_MIMETYPE, TXT_MIMETYPE);
        checkConverters(excelConverter, ExcelFormatConverter.class);
        
        FormatConverter pptConverter = factory.getFormatConverter(PPT_MIMETYPE, TXT_MIMETYPE);
        checkConverters(pptConverter, PowerPointFormatConverter.class);
        
        FormatConverter htmlConverter = factory.getFormatConverter(HTML_MIMETYPE, TXT_MIMETYPE);
        checkConverters(htmlConverter, HTMLFormatConverter.class);
        
        FormatConverter rtfConverter = factory.getFormatConverter(RTF_MIMETYPE, TXT_MIMETYPE);
        checkConverters(rtfConverter, RtfFormatConverter.class);
        
        assertNull(factory.getFormatConverter("", ""));
    }
    
    
    public void testConvertPlainText() throws Exception {
        FormatConverterFactory factory = FormatConverterFactory.getInstance();
        FormatConverter converter = factory.getFormatConverter(TXT_MIMETYPE, TXT_MIMETYPE);
        InputStream is = new ByteArrayInputStream(TESTING_PURPOSE.getBytes());
        StringBuffer buf = new StringBuffer();
        int theChar = 0;
        Reader conSR = new InputStreamReader(converter.convert(is,TXT_MIMETYPE, TXT_MIMETYPE));
        while (conSR.ready()) {
            theChar = conSR.read();
            if (theChar < 0) {
                break;
            }
            buf.append((char) theChar);
        }
        assertTrue(TESTING_PURPOSE.equals(buf.toString()));
    }

    
    public void testConvertPDF() throws Exception {
        FormatConverterFactory factory = FormatConverterFactory.getInstance();
        FormatConverter pdfConverter = factory.getFormatConverter(PDF_MIMETYPE, TXT_MIMETYPE);
        FileInputStream thePDF = new FileInputStream(new File(TESTING_DOC_PATH + ".pdf"));
        assertTestingPurposeInStream(pdfConverter, thePDF, PDF_MIMETYPE);
    }
    
    
    public void testConvertWord() throws Exception{
        FormatConverterFactory factory = FormatConverterFactory.getInstance();
        FormatConverter pdfConverter = factory.getFormatConverter(WORD_MIMETYPE, TXT_MIMETYPE);
        FileInputStream thePDF = new FileInputStream(new File(TESTING_DOC_PATH + ".doc"));
        assertTestingPurposeInStream(pdfConverter, thePDF, WORD_MIMETYPE);
    }
    
    /**
     * Helper method
     */
    private void assertTestingPurposeInStream(FormatConverter aConverter, InputStream aStream, String aFromMIME) throws Exception{
    	InputStream instream = aConverter.convert(aStream, aFromMIME, TextPlainConverter.TXT_MIMETYPE);
        try {
        	ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
			try {
				StreamUtilities.copyStreamContents(instream, bout);
				assertTrue(bout.toString().indexOf(TESTING_PURPOSE) >=0);
			} finally {
				bout.close();
			}
		}
		finally {
		    instream.close();
		}
    }
    
    /**
     * Private helper method to check converters not null and
     * equality to an expected class.
     */
    private void checkConverters(FormatConverter aConverter, Class anExpectedClass) {
        assertNotNull(aConverter);
        assertEquals(anExpectedClass, aConverter.getClass());
    }

    /**
     * the suite of test to execute.
     */
    public static Test suite () {
        Test innerTest = ServiceTestSetup.createSetup(TestFormatConverterFactory.class, FormatConverterFactory.Module.INSTANCE);
		return ConvertTestSetup.createConvertTestSetup(innerTest);
    }

    /**
     * main function for direct testing.
     */
    public static void main (String[] args)  throws IOException  {
        Logger.configureStdout();
    	
        junit.textui.TestRunner.run (suite ());
    }
    
}
