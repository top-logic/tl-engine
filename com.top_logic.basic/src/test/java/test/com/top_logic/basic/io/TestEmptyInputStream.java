/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.io.EmptyInputStream;

/**
 * Testcase for {@link com.top_logic.basic.io.EmptyInputStream}.
 * 
 * (Just for the sake of completeness :-)
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestEmptyInputStream extends TestCase {

    /**
     * Constructor a Tesaces for the fucntion eith the given name.
     */
    public TestEmptyInputStream(String aName) {
        super(aName);
    }

    /**
     * Test for int read() and such.
     */
    public void testRead() throws IOException {
        InputStream eis = EmptyInputStream.INSTANCE;
        assertEquals(-1, eis.read());
        assertEquals(0, eis.available());
        assertEquals(-1, eis.read(new byte[77]));
        assertEquals(-1, eis.read(new byte[99], 17, 42));
    }

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestEmptyInputStream.class);
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }
}
