/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dsa.util;

import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.FileManagerTestSetup;
import test.com.top_logic.basic.ModuleLayoutTestConstants;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.dsa.DSATestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DataAccessService;
import com.top_logic.dsa.util.MimeTypes;

/**
 * Testcase for {com.top_logic.util.MimeTypes}.
 *
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestMimeTypes extends BasicTestCase {

	private FileManager _before;

	/**
	 * Constructor for TestMimeTypes.
	 * 
	 * @param name
	 *        name of function to execute
	 */
    public TestMimeTypes(String name) {
        super(name);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_before = FileManager.setInstance(
			MultiFileManager.createMultiFileManager(ModuleLayoutTestConstants.SRC_TEST_WEBAPP_PATH));
	}

	@Override
	protected void tearDown() throws Exception {
		FileManager.setInstance(_before);
		super.tearDown();
	}

    /** Testing implementation of Reloadeable */
    public void testReloadeable() {
        MimeTypes mt = MimeTypes.getInstance();
        mt.reload();
        
        assertNotNull(mt.toString());
        assertNotNull(mt.getName());
        assertNotNull(mt.getDescription());

        assertTrue(mt.usesXMLProperties());
    }

    /** testing implementatiun of Mime-Types 
      */
	public void testTypes() throws Exception {
    	executeInLocale(Locale.ENGLISH, new Execution() {
			
			@Override
			public void run() throws Exception {
				MimeTypes mt = MimeTypes.getInstance();
				assertEquals("application/msword"  , mt.getMimeType("bloedsinn.doc"));
				assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, mt.getMimeType("hufflepuff.rawenclaw"));
				
				assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, mt.getMimeType("hufflepuff.rawenclaw"));
				assertEquals("application/msword"  , mt.getMimeType("haumich.DoC"));
				
				assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, mt.getMimeType(
					new DataAccessProxy("testFile://hufflepuff.rawenclaw")));
				assertEquals("application/vnd.ms-excel", mt.getMimeType( 
					new DataAccessProxy("testFile://wegwerf.xls")));
			}
		});
    }

    /** Return the suite of Tests to perform */
    public static Test suite () {
		Test suite = ServiceTestSetup.createSetup(TestMimeTypes.class, DataAccessService.Module.INSTANCE,
			MimeTypes.Module.INSTANCE);
		return DSATestSetup.createDSATestSetup(new FileManagerTestSetup(suite));
    }

    /** Main function for direct execution */
    public static void main (String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    }

}
