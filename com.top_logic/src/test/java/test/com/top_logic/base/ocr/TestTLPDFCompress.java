/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.ocr;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.ocr.TLPDFCompress;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test {@link TLPDFCompress}.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestTLPDFCompress extends BasicTestCase {

    /**
     * @param aTest name of function to Test.
     */
    public TestTLPDFCompress(String aTest) {
        super(aTest);
    }
    
    /** 
     * The boring part of the test suite.
     */
    public void testBoring() throws IOException, InterruptedException {
        TLPDFCompress compress = new TLPDFCompress();

		if (compress.isInstalled()) {
			fail(
				"Test should fail due to the known bug in ticket #9920: TLPDFCompress is not installed / configured correctly.");

			Collection<?> col = compress.supportedLanguages();
			assertEquals(3, col.size());
			assertTrue("Language de not contained", col.contains("de"));
			assertTrue("Language en not contained", col.contains("en"));
			assertTrue("Language fr not contained", col.contains("fr"));
		} else {
			/* Exptected due to the known bug in ticket #9920: TLPDFCompress is not installed /
			 * configured correctly. */
		}
    }

    /** 
     * Example for scanning an English document.
     * 
     * This Example was supplied by CVSion, dont blame me ;-)
     */
    public void testLevinski() throws InterruptedException, IOException {
        TLPDFCompress compress = new TLPDFCompress();
        if (!compress.isInstalled())
            return; // No need to chocke on this ...

		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/STR_039.pdf");
        File out = BasicTestCase.createTestFile("TestTLPDFCompressEN", ".pdf");
        assertTrue(out.delete());
        // compress.run(in,out);
        assertEquals(0, compress.run(in, out, "en"));
        assertTrue(out.exists());
        File txtFile = BasicTestCase.createNamedTestFile(out.getName() + ".txt");
        assertTrue(txtFile.exists());
        String txt = FileUtilities.readFileToString(txtFile);
        assertContains("Ms. Lewinsky"  , txt);
        assertContains("President"     , txt);
        assertContains("Oval Office"   , txt);
    }
    
    /** 
     * Example for scanning a Germa document.
     * 
     * This is a much duller example, sorry. 
     */
    public void testGerman() throws InterruptedException, IOException {
        TLPDFCompress compress = TLPDFCompress.getInstance();
        if (!compress.isInstalled())
            return; // No need to chocke on this ...

		File in = new File(ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/base/ocr/V-Modell.pdf");
        File out = BasicTestCase.createTestFile("TestTLPDFCompressDE", ".pdf");
        assertTrue(out.delete());
        // compress.run(in,out);
        // compress.setIgnoreOutput(false);
        assertEquals(0, compress.run(in, out, "de"));
        assertTrue(out.exists());
        File txtFile = BasicTestCase.createNamedTestFile(out.getName() + ".txt");
        assertTrue(txtFile.exists());
        String txt = FileUtilities.readFileToString(txtFile);
        assertContains("Systemerstellung"   , txt);
        assertContains("Aktivitäten"        , txt);
        assertContains("Begriffsklärungen"  , txt);
    }

    /**
     * the suite of test to execute.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestTLPDFCompress.class));
        
        // return new TLTestSetup(new TestTLPDFCompress("testGerman"));
        
    }

    /**
     * Used for direct testing.
     */
    public static void main(String[] args) {
        TestRunner.run(suite ());
    }


}
