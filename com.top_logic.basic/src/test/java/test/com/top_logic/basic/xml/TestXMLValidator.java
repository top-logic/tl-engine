/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ConfigLoaderTestUtil;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.xml.XMLValidator;

/**
 * Validates some XML-Files thereby tsting the {com.top_logic.basic.xml.XMLValidatorTest}
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestXMLValidator extends TestCase {

    /**
     * Constructor for XMLValidatorTest.
     * 
     * @param aName name of function to execute.
     */
    public TestXMLValidator(String aName) {
        super(aName);
    }

    /** validate the top-logix.xml file */
    public void testTopLogic() throws Exception {
		String name = ConfigLoaderTestUtil.getTestConfiguration().getElseError();
		new XMLValidator()
			.validate(FileManager.getInstance().getData(ModuleLayoutConstants.CONF_RESOURCE_PREFIX + "/" + name));
    }
    
	/** validate TestXMLValidator_test.xml file found nearby. */
    public void testtestXML() throws Exception {
		File xml =
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/xml/TestXMLValidator_test.xml");
		new XMLValidator().validate(BinaryDataFactory.createBinaryData(xml));
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
		return ModuleTestSetup.setupModule(TestXMLValidator.class);
    }

}
