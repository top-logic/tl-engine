/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.Calendar;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Testcase for the {@link com.top_logic.basic.DebugHelper}.
 *
 * @author <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestDebugHelper extends TestCase {

    /**
     * WARNING: This test case requires the code at the right lines. So if you modify this
     * class (e.g. adding or removing import statements), you have to set the lineNumber
     * variable in this method to the right line number so that this test case works
     * correct.
     */
    public void testGetLineNumber() {
		int lineNumber = 33;
        assertEquals(lineNumber + 1, DebugHelper.getLineNumber());
        int number = method3();
        assertEquals(lineNumber + 3, DebugHelper.getLineNumber());
        assertTrue(number == method3());
    }

    private void method0() {
        if (!DebugHelper.calledBy("testCalledBy")) {
            throw new SecurityException("You are not allowed to call this method!");
        }
        assertTrue(DebugHelper.calledBy("testCalledBy"));
        assertFalse(DebugHelper.calledBy("allow"));
        assertFalse(DebugHelper.calledBy("calledBy"));
        assertTrue(method1());
        assertFalse(method2());

        assertTrue(DebugHelper.calledByClass(getClass().getName()));
        assertFalse(DebugHelper.calledByClass(DebugHelper.class.getName()));

        assertTrue(DebugHelper.calledByFile("TestDebugHelper.java"));
        assertFalse(DebugHelper.calledByFile("DebugHelper.java"));

        int lineNumber = DebugHelper.getLineNumber();
        assertTrue(method4(lineNumber + 1));
        assertFalse(method4(lineNumber + 1));
        assertTrue(method4(lineNumber + 3));
        assertTrue(DebugHelper.calledBy("method0", lineNumber + 4));
        assertTrue(DebugHelper.calledBy("method0", DebugHelper.getLineNumber()));
        assertTrue(DebugHelper.calledBy("method0", lineNumber + 6));
        assertFalse(DebugHelper.calledBy("method1", lineNumber + 7));
        assertTrue(DebugHelper.calledBy(lineNumber + 8));
        assertTrue(DebugHelper.calledBy(DebugHelper.getLineNumber()));
    }

    private boolean method1() {
        return method2();
    }

    private boolean method2() {
        return DebugHelper.calledBy("method1");
    }

    private int method3() {
        return DebugHelper.getLineNumber();
    }

    private boolean method4(int lineNumber) {
        return DebugHelper.calledBy("method0", lineNumber);
    }
    
	public void testFormatTime() {
		Calendar cal = CalendarUtil.createCalendar();
		cal.set(2012, Calendar.FEBRUARY, 13, 16, 49, 13);
		cal.set(Calendar.MILLISECOND, 123);
		long time = cal.getTimeInMillis();
		String timeString = DebugHelper.formatTime(time);
		assertEquals("16:49:13.123", timeString);
	}

    public void testCalledBy() {
        method0();
    }

    public void testUnallowedCall() {
        try {
            method0();
            fail("Expected SecurityException was not thrown.");
        }
        catch (SecurityException e) {
            // expected
        }
    }

    public void testIsInstanceOf() throws Exception {
        Object o = new Object();
		Integer i = Integer.valueOf(42);
        String s = new String("Don't Panic!");


        // test isInstanceOf
        assertTrue(DebugHelper.isInstanceOf(i, "java.lang.Integer"));
        assertTrue(DebugHelper.isInstanceOf(i, "java.lang.Number"));
        assertTrue(DebugHelper.isInstanceOf(i, "java.lang.Object"));
        assertFalse(DebugHelper.isInstanceOf(i, "java.lang.Double"));
        assertTrue(DebugHelper.isInstanceOf(i, Integer.class.getName()));
        assertTrue(DebugHelper.isInstanceOf(i, Number.class.getName()));
        assertTrue(DebugHelper.isInstanceOf(i, Object.class.getName()));
        assertFalse(DebugHelper.isInstanceOf(i, Double.class.getName()));

        assertTrue(DebugHelper.isInstanceOf(s, "java.lang.String"));
        assertTrue(DebugHelper.isInstanceOf(s, "java.lang.Object"));
        assertFalse(DebugHelper.isInstanceOf(s, "java.lang.Number"));
        assertTrue(DebugHelper.isInstanceOf(s, String.class.getName()));
        assertTrue(DebugHelper.isInstanceOf(s, Object.class.getName()));
        assertFalse(DebugHelper.isInstanceOf(s, Number.class.getName()));

        assertTrue(DebugHelper.isInstanceOf(o, "java.lang.Object"));
        assertFalse(DebugHelper.isInstanceOf(o, "java.lang.String"));
        assertFalse(DebugHelper.isInstanceOf(null, "java.lang.Object"));
        assertTrue(DebugHelper.isInstanceOf(o, Object.class.getName()));
        assertFalse(DebugHelper.isInstanceOf(o, String.class.getName()));
        assertFalse(DebugHelper.isInstanceOf(null, Object.class.getName()));


        assertTrue(DebugHelper.isInstanceOf(i, Integer.class));
        assertTrue(DebugHelper.isInstanceOf(i, Number.class));
        assertTrue(DebugHelper.isInstanceOf(i, Object.class));
        assertFalse(DebugHelper.isInstanceOf(i, Double.class));

        assertTrue(DebugHelper.isInstanceOf(s, String.class));
        assertTrue(DebugHelper.isInstanceOf(s, Object.class));
        assertFalse(DebugHelper.isInstanceOf(s, Number.class));

        assertTrue(DebugHelper.isInstanceOf(o, Object.class));
        assertFalse(DebugHelper.isInstanceOf(o, String.class));
        assertFalse(DebugHelper.isInstanceOf(null, Object.class));


        // test isDirect InstanceOf
        assertTrue(DebugHelper.isDirectInstanceOf(i, "java.lang.Integer"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "java.lang.Number"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "java.lang.Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "java.lang.Double"));
        assertTrue(DebugHelper.isDirectInstanceOf(i, "Integer"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "Number"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(i, "Double"));

        assertTrue(DebugHelper.isDirectInstanceOf(s, "java.lang.String"));
        assertFalse(DebugHelper.isDirectInstanceOf(s, "java.lang.Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(s, "java.lang.Number"));
        assertTrue(DebugHelper.isDirectInstanceOf(s, "String"));
        assertFalse(DebugHelper.isDirectInstanceOf(s, "Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(s, "Number"));

        assertTrue(DebugHelper.isDirectInstanceOf(o, "java.lang.Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(o, "java.lang.String"));
        assertFalse(DebugHelper.isDirectInstanceOf(null, "java.lang.Object"));
        assertTrue(DebugHelper.isDirectInstanceOf(o, "Object"));
        assertFalse(DebugHelper.isDirectInstanceOf(o, "String"));
        assertFalse(DebugHelper.isDirectInstanceOf(null, "Object"));


        assertTrue(DebugHelper.isDirectInstanceOf(i, Integer.class));
        assertFalse(DebugHelper.isDirectInstanceOf(i, Number.class));
        assertFalse(DebugHelper.isDirectInstanceOf(i, Object.class));
        assertFalse(DebugHelper.isDirectInstanceOf(i, Double.class));

        assertTrue(DebugHelper.isDirectInstanceOf(s, String.class));
        assertFalse(DebugHelper.isDirectInstanceOf(s, Object.class));
        assertFalse(DebugHelper.isDirectInstanceOf(s, Number.class));

        assertTrue(DebugHelper.isDirectInstanceOf(o, Object.class));
        assertFalse(DebugHelper.isDirectInstanceOf(o, String.class));
        assertFalse(DebugHelper.isDirectInstanceOf(null, Object.class));

    }

    public void testGetStackDepth() {
        int depth = DebugHelper.getStackDepth();
        assertEquals(depth, DebugHelper.getStackDepth());
        method5(depth);
        assertEquals(depth, DebugHelper.getStackDepth());
    }

    private void method5(int depth) {
        assertEquals(depth + 1, DebugHelper.getStackDepth());
    }

    /** Test the getTime() function */
    public void testGetTime() {
		assertEquals("-1 ms.", DebugHelper.getTime(-1));
		assertEquals("0 ms.", DebugHelper.getTime(0));
		assertEquals("1 s.", DebugHelper.getTime(1000));
		assertEquals("1 s 999 ms.", DebugHelper.getTime(1999));
		assertEquals("11 s 111 ms.", DebugHelper.getTime(11111));
		assertEquals("2 min 3 s 456 ms.", DebugHelper.getTime(123456));
		assertEquals("2 h 44 min 36 s 543 ms.", DebugHelper.getTime(9876543));

		assertEquals("2 ms.", DebugHelper.getTime(2));
		assertEquals("2 s.", DebugHelper.getTime(2 * 1000));
		assertEquals("2 min.", DebugHelper.getTime(2 * 60 * 1000));
		assertEquals("2 h.", DebugHelper.getTime(2 * 60 * 60 * 1000));
    }

	public void testDuration() {
		assertEquals("00:00,000", DebugHelper.toDuration(0));
		assertEquals("00:01,010", DebugHelper.toDuration(1010));
		assertEquals("00:01,999", DebugHelper.toDuration(1999));
		assertEquals("00:11,111", DebugHelper.toDuration(11111));
		assertEquals("02:03,456", DebugHelper.toDuration(123456));
		assertEquals("164:36,543", DebugHelper.toDuration(9876543));
		assertEquals("-00:00,001", DebugHelper.toDuration(-1));
	}

	public void testFullMessage() {
		assertEquals("", DebugHelper.fullMessage(null, null));
		assertEquals("", DebugHelper.fullMessage("", null));
		assertEquals("", DebugHelper.fullMessage("", new NullPointerException()));
		assertEquals("Something broke down.", DebugHelper.fullMessage("", new Exception("Something broke down.")));
		assertEquals("Something failed.", DebugHelper.fullMessage("Something failed.", null));
		assertEquals("Something failed!", DebugHelper.fullMessage("Something failed!", new NullPointerException()));
		assertEquals("Something failed.", DebugHelper.fullMessage("Something failed.", new Exception()));
		assertEquals("Something failed: Something broke down.",
			DebugHelper.fullMessage("Something failed!", new Exception("Something broke down.")));
		assertEquals("Something failed: Something broke down: Unexpected behavior.",
			DebugHelper.fullMessage("Something failed.", new Exception("Something broke down.", new RuntimeException(
				"Unexpected behavior."))));
	}

    public static Test suite() {
        return new TestSuite(TestDebugHelper.class);
    }

    /**
     * Main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        junit.textui.TestRunner.run(suite());
    }

}
