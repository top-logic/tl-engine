/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.generate;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.generate.JavaGenerator;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test the {@link JavaGenerator}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestJavaGenerator extends TestCase {

    /** 
     * Create a new TestJavaGenerator.
     */
    public TestJavaGenerator(String name) {
        super(name);
    }

    /**
     * Test basic usage of a JavaGenerator.
     */
    public void testGenerate() throws IOException {
        JavaGenerator jg = new TestedJavaGenerator("test.com.top_logic.basic.generate");
        File theFile = BasicTestCase.createNamedTestFile("TestJavaGenerator.java");
        jg.generate(theFile);
		FileUtilities.copyFile(
			new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/generate/TestJavaGenerator.java"),
                               theFile);
        jg.generate(theFile); // Generate again ...
    }

    /**
     * Subclass JavaGenerator for testing.
     */
    class TestedJavaGenerator extends JavaGenerator {
        
        public TestedJavaGenerator(String aPackageName) {
            super(aPackageName);
        }

        @Override
        protected void writeBody() {
            commentStart();
            writeCvsTags();
            commentStop();
            comment(" This is the Body of the generated file");
        }
        
        @Override
        public String className() {
            return "TestJavaGenerator";
        }
    }

	public static Test suite() {
		return new TestSuite(TestJavaGenerator.class);
	}

}

