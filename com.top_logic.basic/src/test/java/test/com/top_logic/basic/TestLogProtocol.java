/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.FileUtilities;


/**
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestLogProtocol extends BasicTestCase {

    /** 
     * Creates a {@link TestLogProtocol}.
     */
    public TestLogProtocol(String name) {
        super(name);
    }
    
    /** remove log-file when done */
    public void cleanup() {
        File proto = createNamedTestFile("logAll.log");
        proto.delete();
    }

    /**
     * Single Test for now.
     */
	public void testLogProtocol() throws IOException, URISyntaxException {
        TestLogger4.configureLogAll();
        
        LogProtocol lp = new LogProtocol(TestLogProtocol.class);
        
        lp.info ("Test verbose", LogProtocol.VERBOSE);
        lp.info ("Test debug"  , LogProtocol.DEBUG);
        lp.info ("Test info");
        lp.error("Test error");

        try {
            lp.fatal("Test fatal");
            fail("Expected AbortExecutionException");
        } catch (AbortExecutionException expected) { /* expected */ }
        
        try {
            lp.checkErrors();
            fail("Expected AbortExecutionException");
        } catch (AbortExecutionException expected) { /* expected */ }
        
        Logger.configureStdout();

		try {
			String output = FileUtilities.readLinesFromFile(createNamedTestFile("logAll.log"));

			assertContains("Test verbose", output);
			assertContains("Test debug", output);
			assertContains("Test info", output);
			assertContains("Test error", output);
			assertContains("Test fatal", output);
		} catch (FileNotFoundException exception) {
			throw new RuntimeException("Ticket #26497: Improve the Log4j support after switching to version 2.", exception);
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestLogProtocol.class));
	}


}

