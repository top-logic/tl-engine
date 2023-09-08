/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.ppt.Powerpoint;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ListBuilder;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestPOIPowerpoint extends BasicTestCase {

    /**
     * Constructor for TestPowerpoint.
     */
    public TestPOIPowerpoint(String aName) {
        super(aName);
    }

    public void testGetValues() throws Exception {
		File pptFile = getPPTFile();
		File tmpFile = File.createTempFile("PowerpointExportHandler", ".ppt", pptFile.getParentFile());
		InputStream theIS = new FileInputStream(pptFile);
		try {
			Map<String, Object> theResult = new HashMap<>();
			theResult.put("VALUE_TITLE", "PowerPoint mit POI");
			theResult.put("VALUE_TOPIC", "Arbeiten mit POI");
			theResult.put("VALUE_FIXED_TABLE_TEST", "Tabellen mit fester Spaltenanzahl");
			List<Object> theTable = new ListBuilder<>().add("aaaaa1")
				.add("aaaaa2")
				.add("aaaaa3")
				.add("aaaaa4")
				.add("bbbbb1")
				.add("bbbbb2")
				.add(TestPOIPowerpointX.getTestImageFile())
				.add(TestPOIPowerpointX.getTestImage()).toList();
			theResult.put("FIXEDTABLE_4x2", theTable);
			theResult.put("VALUE_CELL_1", "Zelle 1");
			theResult.put("VALUE_CELL_2", "Zelle 2");
			theResult.put("VALUE_CELL_3", "Zelle 3");
			theResult.put("VALUE_CELL_4", "Zelle 4");
			theResult.put("VALUE_CELL_5", "Zelle 5");
			theResult.put("VALUE_CELL_6", "Zelle 6");
			theResult.put("VALUE_CELL_7", "Zelle 7");
			theResult.put("VALUE_CELL_8", "Zelle 8");

			Powerpoint.getInstance(Powerpoint.isXmlFormat(tmpFile)).setValues(theIS, tmpFile, theResult);
		} finally {
			theIS.close();
		}
    }

    /**
     * Test, if the given map contains the given value.
     * 
     * @param    aMap      The map containing the values to be checked.
     * @param    aValue    The value to be found in the map.
     * @param    isTrue    Flag, if the value has to be in the map.
     */
    protected void doTest(Map aMap, Object aValue, boolean isTrue) {
        if (isTrue) {
            assertTrue("Generated Powerpoint file doesn't contain value '" +  
                       aValue + "'!", aMap.containsValue(aValue));
        }
        else {
            assertTrue("Generated Powerpoint file contain value '" +  
                       aValue + "'!", !aMap.containsValue(aValue));
        }
    }

	/**
	 * The powerpoint file to be used for testing.
	 */
    protected File getPPTFile() throws Exception {
		File theFile =
			FileManager.getInstance().getIDEFile(
				ModuleLayoutConstants.PATH_TO_MODULE_ROOT
					+ "/" + ModuleLayoutConstants.SRC_TEST_DIR
					+ "/test/com/top_logic/base/office/data/TestPoiPowerPoint.ppt");

        if (theFile.exists()) {
            return (theFile);
        }
        else {
            return (null);
        }
    }

    /**
     * The filename for the resulting powerpoint presentation.
     */
    protected File getTargetFile() throws IOException {
		return BasicTestCase.createTestFile("TestPoiPowerpoint", Powerpoint.PPT_EXT);
    }

    public static Test suite () {
		TestSuite theSuite = new TestSuite(TestPOIPowerpoint.class);
		return TLTestSetup.createTLTestSetup(theSuite);
    }

    public static void main(String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite ());
    }
}

