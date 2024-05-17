/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.awt.Dimension;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.util.Utils;

/**
 * Testcases for the methods in {@link Utils}
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class TestUtils extends BasicTestCase {

	public void testEquals() throws Exception {
        Object nullA = null;
        Object nullB = null;
        Object objA = new String("Test");
        Object objB = new String("Test");
        Object Bjbo = new String("tseT");
        assertTrue(objA != objB);
        assertTrue(Utils.equals(nullA, nullB));
        assertTrue(!Utils.equals(nullA, objB));
        assertTrue(!Utils.equals(objA, nullB));
        assertTrue(Utils.equals(objA, objA));
        assertTrue(Utils.equals(objA, objB));
        assertTrue(!Utils.equals(objB, Bjbo));
    }
    
	public void testIsTrue() throws Exception {
        assertFalse(Utils.isTrue(null));
		assertFalse(Utils.isTrue(Boolean.FALSE));
		assertTrue(Utils.isTrue(Boolean.TRUE));
    }
    
	public void testIsFalse() throws Exception {
        assertFalse(Utils.isFalse(null));
		assertFalse(Utils.isFalse(Boolean.TRUE));
		assertTrue(Utils.isFalse(Boolean.FALSE));
    }

	public void testIsEmpty() {
		assertTrue(Utils.isEmpty(null));
		assertTrue(Utils.isEmpty(Collections.emptyList()));
		assertTrue(Utils.isEmpty(Collections.emptyMap()));
		assertTrue(Utils.isEmpty(Collections.emptySet()));
		assertTrue(Utils.isEmpty(""));
		assertTrue(Utils.isEmpty(new int[0]));
		assertTrue(Utils.isEmpty(new boolean[0]));

		assertFalse(Utils.isEmpty(Integer.valueOf(2)));
		assertFalse(Utils.isEmpty(Collections.singletonList(null)));
		assertFalse(Utils.isEmpty(Collections.singletonMap(null, null)));
		assertFalse(Utils.isEmpty(Collections.singleton(Collections.emptyList())));
		assertFalse(Utils.isEmpty("   "));
		assertFalse(Utils.isEmpty(new Object[] { null }));
		assertFalse(Utils.isEmpty(new boolean[] { false }));
	}
    
    public void testGetImageDimension() {
    	
    	assertEquals(new Dimension(2, 1), Utils.getImageDimension(new Dimension(4, 1), 2f));
    	assertEquals(new Dimension(4, 2), Utils.getImageDimension(new Dimension(4, 3), 2f));
    	assertEquals(new Dimension(1, 2), Utils.getImageDimension(new Dimension(8, 2), 0.5f));
    	assertEquals(new Dimension(3, 6), Utils.getImageDimension(new Dimension(8, 6), 0.5f));
    	assertEquals(new Dimension(2, 2), Utils.getImageDimension(new Dimension(4, 2), 1f));
    	assertEquals(new Dimension(2, 2), Utils.getImageDimension(new Dimension(2, 4), 1f));
    	
    	assertEquals(new Dimension(2, 1), Utils.getImageDimension(new Dimension(4, 1), new Dimension(2, 1), false));
    	assertEquals(new Dimension(4, 2), Utils.getImageDimension(new Dimension(4, 3), new Dimension(2, 1), false));
    	assertEquals(new Dimension(1, 2), Utils.getImageDimension(new Dimension(8, 2), new Dimension(1, 2), false));
    	assertEquals(new Dimension(3, 6), Utils.getImageDimension(new Dimension(8, 6), new Dimension(1, 2), false));
    	assertEquals(new Dimension(2, 2), Utils.getImageDimension(new Dimension(4, 2), new Dimension(1, 1), false));
    	assertEquals(new Dimension(2, 2), Utils.getImageDimension(new Dimension(2, 4), new Dimension(1, 1), false));
    	
    	assertEquals(new Dimension(2, 1), Utils.getImageDimension(new Dimension(4, 1), new Dimension(2, 1), true));
    	assertEquals(new Dimension(2, 1), Utils.getImageDimension(new Dimension(4, 3), new Dimension(2, 1), true));
    	assertEquals(new Dimension(1, 2), Utils.getImageDimension(new Dimension(8, 2), new Dimension(1, 2), true));
    	assertEquals(new Dimension(1, 2), Utils.getImageDimension(new Dimension(8, 6), new Dimension(1, 2), true));
    	assertEquals(new Dimension(1, 1), Utils.getImageDimension(new Dimension(4, 2), new Dimension(1, 1), true));
    	assertEquals(new Dimension(1, 1), Utils.getImageDimension(new Dimension(2, 4), new Dimension(1, 1), true));
    	
    }

	public void testFormat() throws Exception {
		Date date = CalendarUtil.getDateInstance(DateFormat.LONG, Locale.GERMAN).parse("26. November 2014");
		checkCorrectFormat(Locale.GERMAN, "Mittwoch, 26. November 2014", "{0,date,full}", date);
		checkCorrectFormat(Locale.ENGLISH, "Wednesday, November 26, 2014", "{0,date,full}", date);
		checkCorrectFormat(Locale.GERMAN, "1.000", "{0}", 1000);
		checkCorrectFormat(Locale.ENGLISH, "1,000", "{0}", 1000);
	}

	private void checkCorrectFormat(Locale locale, final String expected, final String pattern,
			final Object... arguments) throws Exception {
		executeInLocale(locale, new Execution() {

			@Override
			public void run() throws Exception {
				String format = Utils.format(pattern, arguments);
				assertEquals(expected, format);
			}
		});
	}

	public void testLongToBytes() {
		byte[] out = new byte[12];

		long l = 457;
		Utils.longToBytes(out, 2, l);
		assertEquals(l, Utils.bytesToLong(out, 2));

		l = Long.MAX_VALUE;
		Utils.longToBytes(out, 1, l);
		assertEquals(l, Utils.bytesToLong(out, 1));

		l = Long.MAX_VALUE - 1;
		Utils.longToBytes(out, 1, l);
		assertEquals(l, Utils.bytesToLong(out, 1));

		l = Long.MIN_VALUE;
		Utils.longToBytes(out, 4, l);
		assertEquals(l, Utils.bytesToLong(out, 4));

		l = Long.MIN_VALUE + 1;
		Utils.longToBytes(out, 4, l);
		assertEquals(l, Utils.bytesToLong(out, 4));

		try {
			Utils.longToBytes(new byte[7], 0, l);
			fail("Array to short.");
		} catch (Exception ex) {
			// expected
		}

		try {
			Utils.longToBytes(new byte[8], 2, l);
			fail("End index to large.");
		} catch (Exception ex) {
			// expected
		}

		try {
			Utils.longToBytes(new byte[8], 8, l);
			fail("Index to large.");
		} catch (Exception ex) {
			// expected
		}

	}

    /** Return the suite of Tests to perform */
    public static Test suite () {
		return TLTestSetup.createTLTestSetup(TestUtils.class);
    }

}
