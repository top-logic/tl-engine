/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.basic.io.FileUtilities;

/**
 * This Checks if the validation in ActionTestcase is in itself correct.
 * 
 * (XSD is not supported with JDK.1.4 though ...)
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestSchema extends ActionTestcase {

    public TestSchema(String aName) {
        super(aName);
    }
    
    public void testValidate() throws Exception {
        File   eFile = new File("test/xml/simpleajax/example.xml");
        String example = FileUtilities.readLinesFromFile(eFile);
        validate(example);
    }
    
    /** 
     * Return the suite of Tests to execute.
     */
    public static Test suite() {
		return suite(TestSchema.class);
    }

    /** 
     * Main function for direct execution.
     */
    public static void main (String[] args) throws IOException {

        TestRunner.run(suite());
    }


}

