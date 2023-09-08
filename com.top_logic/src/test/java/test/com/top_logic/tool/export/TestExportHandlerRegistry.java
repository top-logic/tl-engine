/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import java.io.ByteArrayOutputStream;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.tool.export.DefaultExportResult;
import com.top_logic.tool.export.ExportHandler;
import com.top_logic.tool.export.ExportHandlerRegistry;
import com.top_logic.tool.export.ExportResult;

/**
 * Test case for {@link ExportHandlerRegistry}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExportHandlerRegistry extends AbstractExportTest {
	
	public static Test suite() {
		return suite(TestExportHandlerRegistry.class);
	}
	 
    public static void main(String[] args) {
        new TestRunner().doRun(suite());
    }
    
    public void testGetExportHandler() throws Exception {
    	ExportHandlerRegistry theReg = ExportHandlerRegistry.getInstance();
    	
    	try {
    		theReg.getHandler(null);
    		fail("expected IllegalArgumentException");
    	} catch (IllegalArgumentException e) {
    	}
    	
    	try {
    		theReg.getHandler("adofgbh023");
    		fail("expected IllegalArgumentException");
    	} catch (IllegalArgumentException e) {
    	}
    	
    	ExportHandler theHandler = theReg.getHandler(EXPORT_HANDLER_ID);
    	assertNotNull(theHandler);
    	assertTrue(theHandler instanceof TestedExportHandler);
    	testHandler(theHandler, false);
    	
    	theHandler = theReg.getHandler(FAILING_EXPORT_HANDLER_ID);
    	assertNotNull(theHandler);
    	assertTrue(theHandler instanceof TestedFailingExportHandler);
    	testHandler(theHandler, true);
    	
    	theHandler = theReg.getHandler(WAITING_EXPORT_HANDLER_ID);
    	assertNotNull(theHandler);
    	assertTrue(theHandler instanceof TestedWaitingExportHandler);
    	testHandler(theHandler, false);
    	
    	theHandler = theReg.getHandler(ERROR_EXPORT_HANDLER_ID);
    	assertNotNull(theHandler);
    	assertTrue(theHandler instanceof TestedErrorExportHandler);
    	try {
    		testHandler(theHandler, false);
    	} catch (RuntimeException r) {
    		assertEquals("test.export.error", r.getMessage());
    	}
    }
    
    private void testHandler(ExportHandler aHandler, boolean expectFailure) throws Exception {
    	Object 				  theModel  = this.getModel();
    	ByteArrayOutputStream theOut    = new ByteArrayOutputStream();
    	ExportResult          theResult = new DefaultExportResult(theOut);
    	aHandler.exportObject(theModel, theResult);
    	theOut.close();
    	
    	
    	if (expectFailure) {
    		assertNotNull(theResult.getFailureKey());
    	}
    	else {
    		assertNotNull(theResult.getFileExtension());
    		assertNotNull(theResult.getFileDisplaynameKey());
    		assertTrue(theOut.size() > 0);
    	}
    }
}
