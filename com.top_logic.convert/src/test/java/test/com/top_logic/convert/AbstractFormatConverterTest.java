/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.convert;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.convert.converters.FormatConverter;
import com.top_logic.convert.converters.TextPlainConverter;

/**
 * Use this abstract class to gain some standard tests for a FormatConverter
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public abstract class AbstractFormatConverterTest extends BasicTestCase {

	public static final String TEMPLATES_DOC_PATH =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/convert/templates/";

	public static final String TESTING_DOC_PATH = TEMPLATES_DOC_PATH + "testing";
    
    public static final String TESTING_PURPOSE = "just for testing purpose";
    
    /**
     * Helpermethod to check several basics about the converters
     */
    public void checkFormatConverterBasics(FormatConverter aConverter, Class aClass) {
        assertNotNull(aConverter);
        assertEquals(aClass, aConverter.getClass());
        try {
            aConverter.convert(null, "no/mime");
            fail("There should be no mathing converter");
        }catch (Exception e) {
            /* expected */
        }
        try {
            aConverter.convert(null, "no/mime", "no/mime");
            fail("There should be no mathing converter");
        }catch (Exception e) {
            /* expected */
        }
    }
    
    public void checkFiles(FormatConverter aConverter, String aFileSuffix, String aFromMIME) throws Exception {
        checkBrokenFile(aConverter, aFileSuffix, aFromMIME);
        checkGoodFile(aConverter, aFileSuffix, aFromMIME);
    }
    
    public void checkBrokenFile(FormatConverter aConverter, String aFileSuffix, String  aFromMIME) throws Exception {
        FileInputStream theFile = new FileInputStream(new File(TESTING_DOC_PATH + "-broken" + aFileSuffix));
        try {
            assertTestingPurposeInStream(aConverter, theFile, aFromMIME);
            fail("Conversion must fail, since File is broken.");
        }catch(Exception e) {
            /* expected*/
        }
    }
    
    public void checkGoodFile(FormatConverter aConverter, String aFileSuffix, String aFromMIME) throws Exception{
        FileInputStream theFile = new FileInputStream(new File(TESTING_DOC_PATH + aFileSuffix));
        assertTestingPurposeInStream(aConverter, theFile, aFromMIME);
    }
    
    /**
     * Helper method
     */
    public void assertTestingPurposeInStream(FormatConverter aConverter, InputStream aStream, String aFromMIME) throws Exception {
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
     * ConvertTestSetup is necessary, since xml properties needs to be loaded 
     */
    protected static Test createTest(Class<? extends AbstractFormatConverterTest> testClass) {
        Test innerTest = ServiceTestSetup.createSetup(testClass, FormatConverterFactory.Module.INSTANCE);
		return ConvertTestSetup.createConvertTestSetup(innerTest);

    }
    
}
