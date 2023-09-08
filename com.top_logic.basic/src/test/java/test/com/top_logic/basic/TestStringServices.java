/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.StringServices.*;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.charsize.FontCharSizeMap;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Tests for {@link com.top_logic.basic.StringServices}
 * the same directory as the source file.
 *
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestStringServices extends BasicTestCase {

	/**
	 * Taken from http://openfontlibrary.org/
	 * 
	 * <p>
	 * Font from "2012-07-10" under "Apache License Version 2.0, January 2004"
	 * </p>
	 */
	private static final File FONT1 = new File(
		ModuleLayoutConstants.SRC_TEST_DIR
			+ "/test/com/top_logic/basic/TestStringServices_Profaisal-EliteTahreerV1.0.ttf");

	/**
	 * Taken from http://openfontlibrary.org/
	 * 
	 * <p>
	 * Font from "2012-09-22" under "no licence"
	 * </p>
	 */
	private static final File FONT2 = new File(
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestStringServices_voynich-1.23-webfont.ttf");

	/**
     * Constructor for having the name of this suite.
     *
     * @param    aName    The name of this suite.
     */
    public TestStringServices(String aName) {
        super(aName);
    }

	public void testStartsWith() {
		StringBuilder composite = new StringBuilder("my funny content");
		assertTrue(StringServices.startsWith(composite, "my funn", 0));
		assertTrue(StringServices.startsWithIgnoreCase(composite, "my funn", 0));
		assertTrue(StringServices.startsWithIgnoreCase(composite, "mY fUNn", 0));
		assertFalse(StringServices.startsWith(composite, "mY fUNn", 0));

		assertTrue(StringServices.startsWith(composite, "my funny content"));
		assertTrue(StringServices.startsWithIgnoreCase(composite, "my funny content"));
		assertTrue(StringServices.startsWithIgnoreCase(composite, "my fUNny content"));
		assertFalse(StringServices.startsWith(composite, "my fUNny content"));

		assertTrue(StringServices.startsWith(composite, " funny", 2));
		assertTrue(StringServices.startsWith(composite, "", 6));
		assertTrue(StringServices.startsWith(composite, "", 12));
		assertFalse(StringServices.startsWith(composite, "my funny", 3));
		assertFalse(StringServices.startsWith(composite, "my funny content plus extra"));

		CharSequence same = "sequence1";
		assertTrue(StringServices.startsWith(same, same));
		assertTrue(StringServices.startsWith("", ""));
		assertFalse(StringServices.startsWith("", " "));

		try {
			StringServices.startsWith(" small ", " large     ", -1);
			fail("Negative start index");
		} catch (IndexOutOfBoundsException ex) {
			// expected
		}
	}

	public void testAppendInt() {
		doTestAppendInt(0);
		doTestAppendInt(1);
		doTestAppendInt(-1);
		doTestAppendInt(100);
		doTestAppendInt(Integer.MAX_VALUE);
		doTestAppendInt(Integer.MIN_VALUE);
	}

	private void doTestAppendInt(int value) {
		StringBuilder buffer = new StringBuilder();
		try {
			StringServices.append(buffer, value);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		assertEquals(Integer.toString(value), buffer.toString());
	}

	public void testBenchmarkAppendInt() throws IOException {
		boolean output = false;

		int capacity = 1000000;
		int cnt = capacity / 11;

		StringBuilder buffer1 = new StringBuilder(capacity);
		StringBuilder buffer2 = new StringBuilder(capacity);

		Random rnd1 = new Random(42);
		Random rnd2 = new Random(42);
		for (int round = 0; round < 10; round++) {
			{
				long elapsed = -System.nanoTime();
				for (int n = 0; n < cnt; n++) {
					buffer1.append(rnd1.nextInt());
				}
				elapsed += System.nanoTime();

				if (output) {
					System.out.println("Java (ns): " + elapsed);
				}
			}

			{
				long elapsed = -System.nanoTime();
				for (int n = 0; n < cnt; n++) {
					StringServices.append(buffer2, rnd2.nextInt());
				}
				elapsed += System.nanoTime();

				if (output) {
					System.out.println("TL   (ns): " + elapsed);
				}
			}

			assertEquals(buffer1.toString(), buffer2.toString());
			buffer1.setLength(0);
			buffer2.setLength(0);
		}
	}

	public void testAppendLong() {
		doTestAppendLong(0);
		doTestAppendLong(1);
		doTestAppendLong(-1);
		doTestAppendLong(100);
		doTestAppendLong(Integer.MAX_VALUE);
		doTestAppendLong(Integer.MIN_VALUE);
		doTestAppendLong(10000000L);
		doTestAppendLong(100000000L);
		doTestAppendLong(1000000000L);
		doTestAppendLong(10000000000L);
		doTestAppendLong(100000000000L);
		doTestAppendLong(Long.MAX_VALUE);
		doTestAppendLong(Long.MIN_VALUE);
	}

	private void doTestAppendLong(long value) {
		StringBuilder buffer = new StringBuilder();
		try {
			StringServices.append(buffer, value);
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		}
		assertEquals(Long.toString(value), buffer.toString());
	}

	public void testBenchmarkAppendLong() throws IOException {
		boolean output = false;

		int capacity = 1000000;
		int cnt = capacity / 20;

		StringBuilder buffer1 = new StringBuilder(capacity);
		StringBuilder buffer2 = new StringBuilder(capacity);

		Random rnd1 = new Random(42);
		Random rnd2 = new Random(42);
		for (int round = 0; round < 10; round++) {
			{
				long elapsed = -System.nanoTime();
				for (int n = 0; n < cnt; n++) {
					buffer1.append(rnd1.nextLong());
				}
				elapsed += System.nanoTime();

				if (output) {
					System.out.println("Java long (ns): " + elapsed);
				}
			}

			{
				long elapsed = -System.nanoTime();
				for (int n = 0; n < cnt; n++) {
					StringServices.append(buffer2, rnd2.nextLong());
				}
				elapsed += System.nanoTime();

				if (output) {
					System.out.println("TL long   (ns): " + elapsed);
				}
			}

			assertEquals(buffer1.toString(), buffer2.toString());
			buffer1.setLength(0);
			buffer2.setLength(0);
		}
	}

    /** Test the {@link StringServices#escape(String)} and unEscape Method
     */
    public void testEscape() {
        try {
            StringServices.escape(null);
            fail("Expected NullPointerException here");
        }
        catch (NullPointerException expected) { /* expected */ }
        assertEquals(
            "This needs no escape",
            StringServices.escape("This needs no escape"));
        assertEquals("\\0"                    , StringServices.escape("\0"));
        assertEquals("\\n\\r\\u00e4"          , StringServices.escape("\n\r\u00e4"));
        assertEquals("\\u1235\\u4567"         , StringServices.escape("\u1235\u4567"));
        assertEquals("\\0\\b\\t\\f\\\"\\'\\\\", StringServices.escape("\u0000\b\t\f\"\'\\"));

        assertEquals("\0"           , StringServices.unEscape("\\0"));
        assertEquals("\n\r\u00e4"   , StringServices.unEscape("\\n\\r\\u00e4"));
        assertEquals("\u1235\u4567" , StringServices.unEscape("\\u1235\\u4567"));
        assertEquals("\b\t\f\"\'\\" , StringServices.unEscape("\\b\\t\\f\\\"\\'\\\\"));
        
        assertEquals("\n\r\u00e4"   , StringServices.unQuoteAndEscape("A\\n\\r\\u00e4A"));
        assertEquals("\u1235\u4567" , StringServices.unQuoteAndEscape("\"\\u1235\\u4567\""));
        assertEquals("\b\t\f\"\'\\" , StringServices.unQuoteAndEscape("~\\b\\t\\f\\\"\\'\\\\~"));
    }

    /** 
     * Test the {@link com.top_logic.basic.StringServices#stripNewlines(CharSequence)} 
     * method. 
     */
    public void testStripNewlines() throws IOException {

        assertEquals("aaa bbb", StringServices.stripNewlines("aaa\n bbb"));
        assertEquals("aaa bbb", StringServices.stripNewlines("aaa \rbbb"));
        assertEquals("aaa bbb", StringServices.stripNewlines("\raaa bbb\n"));

        // Just to test the speed
        String input =
            FileUtilities.readFileToString(
				ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/basic/TestStringServices.java");
        startTime();
        String result = null;
        for (int i=0; i < 20; i++) {
            result = StringServices.stripNewlines(input);
        }
        logTime("Stripping 20x String of Size " + input.length());
        assertTrue(input.length() > result.length());
    }

    /** 
     * Test the {@link com.top_logic.basic.StringServices#toArray(CharSequence)} 
     * method. It is expected, that it can work with empty strings and also
     * with any kind of strings.
     */
    public void testToArray() {
        assertEquals(3, StringServices.toArray("abc,def,ghj").length);
        assertNull(StringServices.toArray(""));
        assertNull(StringServices.toArray(" "));
        assertEquals(1, StringServices.toArray("abc").length);
        String[] result = StringServices.toArray(" abc,,def , g h i,  ,jkl");
        assertEquals(4, result.length);
        assertEquals("abc", result[0]);
        assertEquals("def", result[1]);
        assertEquals("g h i", result[2]);
        assertEquals("jkl", result[3]);
    }

    /** 
     * Test the {@link com.top_logic.basic.StringServices#toArray(String, String)} 
     * method. It is expected, that it can work with empty strings and also
     * with any kind of strings.
     */
    public void testToArrayString() {
        assertEquals(0, StringServices.toArray((String) null, (String) null).length);
        assertEquals(0, StringServices.toArray(null,"--").length);
        assertEquals(0, StringServices.toArray("","--").length);
        assertEquals(0, StringServices.toArray("","").length);
        String array[] = StringServices.toArray("abc--def--ghj","--");
        assertEquals(3, array.length);
        assertEquals("abc", array[0]);
        assertEquals("def", array[1]);
        assertEquals("ghj", array[2]);
    }
    
    /** 
     * Test the method {@link com.top_logic.basic.StringServices#toArray(Collection, Format)}.
     * 
     */
    public void testToArrayFormat() {
        DecimalFormat format = new DecimalFormat(
                "#,##0.##", 
                new DecimalFormatSymbols(Locale.GERMAN));
        ArrayList<Double> list = new ArrayList<>(3);
        list.add(625.40);
        list.add(2901.80);
        list.add(1703.56);
        
        assertEquals(0, StringServices.toArray(null, format).length);
        String array[] = StringServices.toArray(list, format);
        assertEquals(3, array.length);
        assertEquals(array[0], "625,4");
        assertEquals(array[1], "2.901,8");
        assertEquals(array[2], "1.703,56");
    }

    /** 
     * Test the {@link com.top_logic.basic.StringServices#parseBoolean(String)} 
     * method.
     */
    public void testParseBoolean() {
        assertFalse(StringServices.parseBoolean(null));
        assertFalse(StringServices.parseBoolean(""));
        assertFalse(StringServices.parseBoolean("f"));
        assertFalse(StringServices.parseBoolean("F"));
        assertFalse(StringServices.parseBoolean("\n"));
        
        assertTrue(StringServices.parseBoolean("y"));
        assertTrue(StringServices.parseBoolean("Y"));
        assertTrue(StringServices.parseBoolean("t"));
        assertTrue(StringServices.parseBoolean("T"));
    }
    
    /** 
     * Test the method {@link com.top_logic.basic.StringServices#join(Object[])}.
     * 
     */
    public void testJoin() {
        assertEquals(StringServices.join(null), "");
        assertEquals(StringServices.join(new Object[0]), "");
        Object objs[] = {"This", "is", "a", "Test"};
        assertEquals(StringServices.join(objs), "ThisisaTest");
    }
    
    /** 
     * Test the {@link com.top_logic.basic.StringServices#toHexString(CharSequence)} 
     * method. It is expected, that it can work with empty strings and also
     * with any kind of strings.
     */
    public void testToHexString() {

        assertEquals(charToHex(' '), StringServices.toHexString(" "));
        assertEquals(charToHex('d'), StringServices.toHexString("d"));
        assertEquals(
            charToHex('t') + charToHex('e') + charToHex('s') + charToHex('t'),
            StringServices.toHexString("test"));
        assertEquals(charToHex('*'), StringServices.toHexString("*"));
        assertEquals(charToHex('\''), StringServices.toHexString("\'"));
        assertEquals(charToHex('@'), StringServices.toHexString("@"));
        assertEquals(charToHex('?'), StringServices.toHexString("?"));
        assertEquals(charToHex('{'), StringServices.toHexString("{"));
        assertEquals(charToHex('%'), StringServices.toHexString("%"));
        assertEquals(charToHex(';'), StringServices.toHexString(";"));
        assertEquals(charToHex('µ'), StringServices.toHexString("µ"));
        assertEquals(charToHex('\"'), StringServices.toHexString("\""));
        assertEquals(charToHex('&'), StringServices.toHexString("&"));
        assertEquals(charToHex('/'), StringServices.toHexString("/"));
        assertEquals(charToHex('ß'), StringServices.toHexString("ß"));
        assertEquals(charToHex('²'), StringServices.toHexString("²"));
        assertEquals(charToHex('´'), StringServices.toHexString("´"));
        assertEquals(charToHex(' '), StringServices.toHexString(" "));
        assertEquals(charToHex('\uaaaa'), StringServices.toHexString("\uaaaa"));
        assertEquals(charToHex('\ubbbb'), StringServices.toHexString("\ubbbb"));
        assertEquals(
            charToHex(' ') + charToHex(';') + charToHex('g'),
            StringServices.toHexString(" ;g"));
    }

    /** 
     * Test the {@link StringServices#hexStringToString(String)} 
     * method.
     */
    public void testHexStringToString() {

        assertEquals(
            "abcde",
            StringServices.hexStringToString("00610062006300640065"));
        assertEquals(
            "\uffff\u0000\udead\uBEAF",
            StringServices.hexStringToString("FFFF0000DEADbeaf"));
        try {
            StringServices.hexStringToString("01234567890abcdefABCDEFG");
            fail("Expected NumberFormatException");
        }
        catch (NumberFormatException expected) {// expected 
        }
    }

    private String byteToHex(byte b) {
        // Returns hex String representation of byte b
        char hexDigit[] =
            {
                '0',
                '1',
                '2',
                '3',
                '4',
                '5',
                '6',
                '7',
                '8',
                '9',
                'a',
                'b',
                'c',
                'd',
                'e',
                'f' };
        char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
        return new String(array);
    }

    private String charToHex(char c) {
        // Returns hex String representation of char c
        byte hi = (byte) (c >>> 8);
        byte lo = (byte) (c & 0xff);
        return byteToHex(hi) + byteToHex(lo);
    }

    /** 
     * Test the {@link com.top_logic.basic.StringServices#toArray(CharSequence)} 
     * method. It is expected, that it can work with empty strings and also
     * with any kind of strings.
     */
    public void testTimeToArray() {

        int count = 5000;
        startTime();
        for (int i = 0; i < count; i++) {
            assertEquals(
                3,
                StringServices.toArray(
                    "root,tl.admin,a.long.name",
                    ",").length);
            assertEquals(0, StringServices.toArray("", ",").length);
            assertEquals(
                5,
                StringServices.toArray(
                    "user,bos.head,admin,bos.team,editor.tree",
                    ",").length);
            assertEquals(
                4,
                StringServices.toArray(" abc,def , g h i ,jkl", ",").length);
        }
        long l1 = super.logTime("toArray with String " + (count * 3));
        for (int i = 0; i < count; i++) {
            assertEquals(
                3,
                StringServices.toArray(
                    "root,tl.admin,a.long.name",
                    ',').length);
            assertNull(StringServices.toArray("", ','));
            assertEquals(
                5,
                StringServices.toArray(
                    "user,bos.head,admin,bos.team,editor.tree",
                    ',').length);
            assertEquals(
                4,
                StringServices.toArray(" abc,def , g h i ,jkl", ',').length);
        }
        long l2 = super.logTime("toArray with char   " + (count * 3));
        if (SHOW_TIME) {
            System.out.println();
            System.out.print("Speedup is " + (100 * l1 / l2) + "%");
        }
        // Puh, JDK 1,4 is to fast ....
        // assertTrue("toArray with char should be faster", l2 < l1);
    }

    /** Test the {@link com.top_logic.basic.StringServices#unEscape(String)} Method
     */
    public void testUnEscape() {
        assertEquals(
            "This needs no escape",
            StringServices.unEscape("This needs no escape"));
        assertEquals("\n\rä\\x\\y", StringServices.unEscape("\\n\\rä\\x\\y"));
        assertEquals("\u1235\u4567", StringServices.unEscape("\\u1235\\u4567"));
    }

    /** Cobines some escape() and unEscape().
     */
    public void testBoth() {

        String str = "This needs no escape";
        assertEquals(str, StringServices.unEscape(StringServices.escape(str)));

        str = "ÄÖÜ äöüß ^ô°áà~µ@|";
        assertEquals(str, StringServices.unEscape(StringServices.escape(str)));

        str = "\u1234\u2345\u3456\u4567\u5678\u6789\u7890";
        assertEquals(str, StringServices.unEscape(StringServices.escape(str)));
    }

    /** 
     * Test the method cutString.
     */
	public void testCutString() throws FontFormatException, IOException {
        assertEquals(null, StringServices.cutString(null, 3));
        assertEquals("", StringServices.cutString("", 3));
        assertEquals("Test", StringServices.cutString("Test", 5));
        assertEquals("Test", StringServices.cutString("Test", 10));
        assertEquals("Test", StringServices.cutString("Testing", 4));
        assertEquals("", StringServices.cutString("Testing", 0));
        
        String text, expect, result;
        CharSizeMap map = ProportionalCharSizeMap.INSTANCE;

        assertEquals(null, StringServices.cutString(null, 1, 3, map));
        assertEquals("", StringServices.cutString("", 1, 3, map));
        assertEquals("Test", StringServices.cutString("Test", 1, 5, map));
        assertEquals("Test", StringServices.cutString("Test", 2, 10, map));
        assertEquals("Test", StringServices.cutString("Testing", 1, 4, map));
        assertEquals("", StringServices.cutString("Testing", 1, 0, map));
        assertEquals("Test   Test", StringServices.cutString("Test   Test", 1, 11, map));

        text = "My funny string with a last word";
        expect = "My funny string\nwith a last word";
        result = StringServices.cutString(text, 3, 16, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 3, 16, map);
        assertEquals(expect, result);

        text = "My funny string with a last word";
        expect = "My funny\nstring\nwith a\nlast word";
        result = StringServices.cutString(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 4, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n";
        expect = "My funny\nstring\nwith a\nlast word";
        result = StringServices.cutString(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 4, 9, map);
        assertEquals(expect, result);
        expect = "My funny\nstring\nwith a\nlast word\n";
        result = StringServices.cutString(text, 5, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 5, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n\nHide this\n";
        expect = "My funny\nstring\nwith a\nlast word";
        result = StringServices.cutString(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 4, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n\nHide this\n";
        expect = "My funny\nstring\nwith a\nlast word\n";
        result = StringServices.cutString(text, 5, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 5, 9, map);
        assertEquals(expect, result);

        text = "MyFunnyString with";
        expect = "MyFun\nnyStr\ning\nwith";
        result = StringServices.cutString(text, 10, 5, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 10, 5, map);
        assertEquals(expect, result);
        
        text = "My funny    string with a last word";
        expect = "My\nfunny\nstrin\ng\nwith\na\nlast\nword";
        result = StringServices.cutString(text, 10, 5, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 10, 5, map);
        assertEquals(expect, result);
        
        text = "My   funny string";
        expect = "My   funny\nstring";
        result = StringServices.cutString(text, 2, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 2, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(text, 3, 15, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 3, 15, map);
        assertEquals(expect, result);

        text = "My   \n   funny string";
        expect = "My   \n   funny\nstring";
        result = StringServices.cutString(text, 3, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 3, 10, map);
        assertEquals(expect, result);

        text = "Test:\n  * Pt 1\n  * Pt 2\n  * Extra Punkt";
        expect = "Test:\n  * Pt 1\n  * Pt 2\n  * Extra\nPunkt";
        result = StringServices.cutString(text, 10, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 10, 10, map);
        assertEquals(expect, result);

        text = "Hi world\nHello hi hello";
        expect = "Hi world\nHello hi\nhello";
        result = StringServices.cutString(text, 4, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 4, 10, map);
        assertEquals(expect, result);

		text = "Ein\tText\tgetrennt\tmit\tTabs.";
		expect = "Ein Text getrennt mit Tabs.";
		result = StringServices.cutString(text, 10, 50, map);
		assertEquals(expect, result);

        text = "Derzeit stehen ca. 40 Ressourcen zur Verfügung.";
        expect = "Derzeit\nstehen ca.\n40\nRessourcen\nzur\nVerfügung.";
        result = StringServices.cutString(text, 20, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 20, 10, map);
        assertEquals(expect, result);

		map = newFontSizeMap(FONT1);
		expect = "Derzeit\nstehen ca.\n40\nRessource\nn zur\nVerfügung.";
        result = StringServices.cutString(text, 20, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutString(result, 20, 10, map);
        assertEquals(expect, result);

		map = newFontSizeMap(FONT2);
		expect = "Derzeit stehen\nca. 40\nRessourcen zur";
		result = StringServices.cutString(text, 3, 20, map);
        assertEquals(expect, result);
		result = StringServices.cutString(result, 3, 20, map);
        assertEquals(expect, result);
    }

	private FontCharSizeMap newFontSizeMap(File fontFile) throws FontFormatException, IOException {
		Font font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
		return new FontCharSizeMap(font);
	}

    /** 
     * Test the method cutStringKeepSpaces.
     */
	public void testCutStringKeepSpaces() throws FontFormatException, IOException {

        String text, expect, result;
        CharSizeMap map = ProportionalCharSizeMap.INSTANCE;

        assertEquals(null, StringServices.cutStringKeepSpaces(null, 1, 3, map));
        assertEquals("", StringServices.cutStringKeepSpaces("", 1, 3, map));
        assertEquals("Test", StringServices.cutStringKeepSpaces("Test", 1, 5, map));
        assertEquals("Test", StringServices.cutStringKeepSpaces("Test", 2, 10, map));
        assertEquals("Test", StringServices.cutStringKeepSpaces("Testing", 1, 4, map));
        assertEquals("", StringServices.cutStringKeepSpaces("Testing", 1, 0, map));
        assertEquals("Test   Test", StringServices.cutStringKeepSpaces("Test   Test", 1, 11, map));

        text = "My funny string with a last word";
        expect = "My funny string \nwith a last word";
        result = StringServices.cutStringKeepSpaces(text, 3, 16, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 3, 16, map);
        assertEquals(expect, result);

        text = "My funny string with a last word";
        expect = "My funny \nstring \nwith a \nlast word";
        result = StringServices.cutStringKeepSpaces(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 4, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n";
        expect = "My funny \nstring\nwith a \nlast word";
        result = StringServices.cutStringKeepSpaces(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 4, 9, map);
        assertEquals(expect, result);
        expect = "My funny \nstring\nwith a \nlast word\n";
        result = StringServices.cutStringKeepSpaces(text, 5, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 5, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n\nHide this\n";
        expect = "My funny \nstring\nwith a \nlast word";
        result = StringServices.cutStringKeepSpaces(text, 4, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 4, 9, map);
        assertEquals(expect, result);

        text = "My funny string\nwith a last word\n\nHide this\n";
        expect = "My funny \nstring\nwith a \nlast word\n";
        result = StringServices.cutStringKeepSpaces(text, 5, 9, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 5, 9, map);
        assertEquals(expect, result);

        text = "MyFunnyString with";
        expect = "MyFun\nnyStr\ning \nwith";
        result = StringServices.cutStringKeepSpaces(text, 10, 5, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 10, 5, map);
        assertEquals(expect, result);

        text = "My funny    string with a last word";
        expect = "My \nfunny \n   \nstrin\ng \nwith \na \nlast \nword";
        result = StringServices.cutStringKeepSpaces(text, 10, 5, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 10, 5, map);
        assertEquals(expect, result);

        text = "My   funny string";
        expect = "My   funny \nstring";
        result = StringServices.cutStringKeepSpaces(text, 2, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 2, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(text, 3, 15, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 3, 15, map);
        assertEquals(expect, result);

        text = "My   \n   funny string";
        expect = "My   \n   funny \nstring";
        result = StringServices.cutStringKeepSpaces(text, 3, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 3, 10, map);
        assertEquals(expect, result);

        text = "Test:\n  * Pt 1\n  * Pt 2\n  * Extra Punkt";
        expect = "Test:\n  * Pt 1\n  * Pt 2\n  * Extra \nPunkt";
        result = StringServices.cutStringKeepSpaces(text, 10, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 10, 10, map);
        assertEquals(expect, result);

        text = "Hi world\nHello hi hello";
        expect = "Hi world\nHello hi \nhello";
        result = StringServices.cutStringKeepSpaces(text, 4, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 4, 10, map);
        assertEquals(expect, result);

        text = "Derzeit stehen ca. 40 Ressourcen zur Verfügung.";
        expect = "Derzeit \nstehen ca. \n40 \nRessourcen \nzur \nVerfügung.";
        result = StringServices.cutStringKeepSpaces(text, 20, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 20, 10, map);
        assertEquals(expect, result);

		map = newFontSizeMap(FONT1);
		expect = "Derzeit \nstehen ca. \n40 \nRessource\nn zur \nVerfügung.";
        result = StringServices.cutStringKeepSpaces(text, 20, 10, map);
        assertEquals(expect, result);
        result = StringServices.cutStringKeepSpaces(result, 20, 10, map);
        assertEquals(expect, result);

		expect = "Derzeit stehen \nca. 40 \nRessourcen zur ";
		map = newFontSizeMap(FONT2);
		result = StringServices.cutStringKeepSpaces(text, 3, 20, map);
		assertEquals(expect, result);
		result = StringServices.cutStringKeepSpaces(result, 30, 20, map);
		assertEquals(expect, result);
    }


    /**
     * Test the variants of minimizeString.
     */
    public void testMinimizeString() {
        String small = "a small String";
        String large = "A rather Large String that surely needs some truncation";
        String largeStart = large.substring(0, 8);
        String largeEnd = large.substring(large.length() - 8);

        assertNull(StringServices.minimizeString(null, 0));
        assertNull(StringServices.minimizeString(null, 0, 0));

        assertEquals(small, StringServices.minimizeString(small, 16));
        assertEquals(small, StringServices.minimizeString(small, 16, 0));

        String trunc = StringServices.minimizeString(large, 38, 16);

        assertTrue(trunc.endsWith(largeEnd));
        assertTrue(trunc.startsWith(largeStart));

        trunc = StringServices.minimizeString(large, 32);

        assertTrue(trunc.endsWith(largeEnd));
        assertTrue(trunc.startsWith(largeStart));

    }

    /**
     * Test the simple variant of toList()
     */
    public void testToList()  {

        assertNull(StringServices.toList(null, 'x'));
        assertTrue(StringServices.toList(""  , 'x').isEmpty());
        
        List<String> theList = StringServices.toList("ABCDEFG"  , (char) 0);
        assertEquals(1, theList.size());
        assertEquals("ABCDEFG", theList.get(0));
        
		theList = StringServices.toList("AxBxCxDxExFxG", 'x');
        assertEquals(7, theList.size());
        assertEquals("A", theList.get(0));
        assertEquals("G", theList.get(6));

        theList = StringServices.toList("xAxBxCxDxExFxGx"  , 'x');
        assertEquals(7, theList.size());
        assertEquals("A", theList.get(0));
        assertEquals("G", theList.get(6));     

        theList = StringServices.toList(" x Ax B xC xDxExFxG x"  , 'x');
        assertEquals(7, theList.size());
        assertEquals("A", theList.get(0));
        assertEquals("B", theList.get(1));
        assertEquals("G", theList.get(6));

		/* Test that white spaces are removed correctly */
		theList = StringServices.toList(" , A, \rB ,C\n ,D,\tE,F,G \n,\t", ',');
		assertEquals(list("A", "B", "C", "D", "E", "F", "G"), theList);

    }
    
    /**
     * Test the simple variant of toList()
     */
    public void testToSet()  {
        
        assertNull(StringServices.toSet(null, 'x'));
        assertTrue(StringServices.toSet(""  , 'x').isEmpty());
        
        Set <String>theSet = StringServices.toSet("ABCDEFG"  , (char) 0);
        assertEquals(1, theSet.size());
        assertTrue(theSet.contains("ABCDEFG"));
        
        theSet = StringServices.toSet("AxBxCxDxExFxG"  , 'x');
        assertEquals(7, theSet.size());
        assertTrue(theSet.contains("A"));
        assertTrue(theSet.contains("B"));
        assertTrue(theSet.contains("C"));
        assertTrue(theSet.contains("D"));
        assertTrue(theSet.contains("E"));
        assertTrue(theSet.contains("F"));
        assertTrue(theSet.contains("G"));

        theSet = StringServices.toSet("xAxBxCxDxExFxGx"  , 'x');
        assertEquals(7, theSet.size());
        assertTrue(theSet.contains("A"));
        assertTrue(theSet.contains("B"));
        assertTrue(theSet.contains("C"));
        assertTrue(theSet.contains("D"));
        assertTrue(theSet.contains("E"));
        assertTrue(theSet.contains("F"));
        assertTrue(theSet.contains("G"));

        theSet = StringServices.toSet(" x Ax B xC xDxExFxG x"  , 'x');
        assertEquals(7, theSet.size());
        assertTrue(theSet.contains("A"));
        assertTrue(theSet.contains("B"));
        assertTrue(theSet.contains("G"));
    }
    
    public void testAddStringsToCollection() {

        assertNull(StringServices.addStringsToCollection(null, 'x', null));
        assertNull(StringServices.addStringsToCollection("ABCDEFG", 'x', null));
        
        Collection<String>theCol = new ArrayList<>();
        assertTrue(StringServices.addStringsToCollection(null, 'x', theCol).isEmpty());
        assertTrue(StringServices.addStringsToCollection("", 'x', theCol).isEmpty());
        assertTrue(StringServices.addStringsToCollection("x xx   x", 'x', theCol).isEmpty());
        
        assertEquals(4, StringServices.addStringsToCollection("AxBxCxD", 'x', theCol).size());
        assertTrue(theCol.contains("A"));
        assertTrue(theCol.contains("B"));
        assertTrue(theCol.contains("C"));
        assertTrue(theCol.contains("D"));
        assertEquals(theCol, StringServices.addStringsToCollection("x xx   x", 'x', theCol));
        
        theCol.clear();
        theCol.add("This");
        theCol.add("is");
        theCol.add("a");
        theCol.add("Test");
        assertEquals(8, StringServices.addStringsToCollection("AxBxCxD", 'x', theCol).size());
        assertTrue(theCol.contains("This"));
        assertTrue(theCol.contains("is"));
        assertTrue(theCol.contains("a"));
        assertTrue(theCol.contains("Test"));
        assertTrue(theCol.contains("A"));
        assertTrue(theCol.contains("B"));
        assertTrue(theCol.contains("C"));
        assertTrue(theCol.contains("D"));
    }

    /**
     * Test the allowEmpty variant of toList and toArray()
     */
    public void testToListAllowEmpty()  {
        
        assertTrue(StringServices.toListAllowEmpty(null, 'x').isEmpty());
        
        List <String> theList = StringServices.toListAllowEmpty(""  , 'x');
        assertEquals(1 , theList.size());
        assertEquals("", theList.get(0));
        
        theList = StringServices.toListAllowEmpty("ABCDEFG"  , (char) 0);
        assertEquals(1        , theList.size());
        assertEquals("ABCDEFG", theList.get(0));
        
        theList = StringServices.toListAllowEmpty("AxBxCxDxExFxG"  , 'x');
        assertEquals(7  , theList.size());
        assertEquals("A", theList.get(0));
        assertEquals("G", theList.get(6));

        theList = StringServices.toListAllowEmpty("xAxBxCxDxExFxGx"  , 'x');
        assertEquals(9, theList.size());
        assertEquals("" , theList.get(0));
        assertEquals("A", theList.get(1));
        assertEquals("G", theList.get(7));
        assertEquals("" , theList.get(8));
        
        theList = StringServices.toListAllowEmpty(" x Ax B xC xDxExFxG x" , 'x');
        assertEquals(9, theList.size());
        assertEquals(" "  , theList.get(0));
        assertEquals(" A" , theList.get(1));
        assertEquals(" B ", theList.get(2));
        assertEquals("G " , theList.get(7));
        assertEquals(""   , theList.get(8));

        String array[];
        
        assertEquals(0, StringServices.split(null, 'x').length);
        
        array = StringServices.split(""  , 'x');
        assertEquals(1 , array.length);
        assertEquals("", array[0]);
        
        array = StringServices.split("ABCDEFG"  , (char) 0);
        assertEquals(1        , array.length);
        assertEquals("ABCDEFG", array[0]);
        
        array = StringServices.split("AxBxCxDxExFxG"  , 'x');
        assertEquals(7  , array.length);
        assertEquals("A", array[0]);
        assertEquals("G", array[6]);

        array = StringServices.split("xAxBxCxDxExFxGx"  , 'x');
        assertEquals(9, array.length);
        assertEquals("" , array[0]);
        assertEquals("A", array[1]);
        assertEquals("G", array[7]);
        assertEquals("" , array[8]);
        
        array = StringServices.split(" x Ax B xC xDxExFxG x" , 'x');
        assertEquals(9, theList.size());
        assertEquals(" "  , array[0]);
        assertEquals(" A" , array[1]);
        assertEquals(" B ", array[2]);
        assertEquals("G " , array[7]);
        assertEquals(""   , array[8]);

    }

    /** This methods tests the method {@link StringServices#toFileName(String, char)} */
    public void testToFilenameWithReplacementChar() {
        String goodName1 = "my document.doc";
        assertEquals(goodName1, StringServices.toFileName(goodName1, ' '));
        
        String goodName2 = "my docu~.xls";
        assertEquals(goodName2, StringServices.toFileName(goodName2, ' '));
        
        String badName1 = "my\\document.doc";
        assertEquals("my document.doc", StringServices.toFileName(badName1, ' '));
        
        String badName2 = "<my document.doc>";
        assertEquals(" my document.doc ", StringServices.toFileName(badName2, ' '));
        
        String badName3 = "my: document.doc";
        assertEquals("my  document.doc", StringServices.toFileName(badName3, ' '));
    }
    
    /**
     * Test the cleanName() method(s).
     */
    public void testCleanName() throws Exception {
        assertEquals(
            "______________UVWXYZ________________abcdefg12345",
            StringServices.cleanName(
                "!@#$%^&*()_+=-UVWXYZ[]\"{}\\|'/?.>,<|/abcdefg12345"));
        assertEquals(
            "ThisStringIsClean",
            StringServices.cleanName("ThisStringIsClean"));
        assertEquals(
            "This__String__Is_Dirty__",
            StringServices.cleanName("This 'String\" Is Dirty !"));
    }

    /**
     * Test the cleanName() method(s).
     */
    public void testCleanHTMLName() throws Exception {
        assertEquals(
            "!@#$%^&*()_+=-UVWXYZ[]\"{}\\|'/?.,|/abcdefg12345",
            StringServices.cleanHTMLName(
                "!@#$%^&*()_+=-UVWXYZ[]\"{}\\|'/?.>,<|/abcdefg12345"));
        assertEquals(
            "ThisStringIsClean",
            StringServices.cleanHTMLName("ThisStringIsClean"));
        assertEquals(
            "This String Is Dirty",
            StringServices.cleanHTMLName("This String <Is> Dirty"));
        assertEquals("", StringServices.cleanHTMLName(null));
        assertEquals("", StringServices.cleanHTMLName(""));
    }

    /**
     * Test the null/empty Methods.
     */
    public void testNullEmpty() {
        assertEquals("", StringServices.nonNull(null));
        assertEquals("", StringServices.nonNull(""));
        assertEquals("änything", StringServices.nonNull("änything"));

        assertNull(StringServices.nonEmpty(null));
        assertNull(StringServices.nonEmpty(""));
        assertEquals("änything", StringServices.nonEmpty("änything"));
    }

    /**
     * Tests the contains methods.
     */
    public void testContains() {
        String array[] = null;

        assertTrue(!StringServices.contains(array, ""));

        array = new String[] { "Am", "Anfang", "war" };

        assertTrue(!StringServices.contains(array, "Blub"));
        assertTrue(StringServices.contains(array, "war"));
        assertTrue(StringServices.contains(array, "Am"));

    }

    /**
     * Tesst the getValueWithClass methods.
     */
    public void testGetValueWithClass() throws Exception {

        String s = "any String that matters";

        assertSame(
            s,
            StringServices.getValueWithClass(s, String.class.getName()));

        assertEquals(
			Integer.valueOf(Integer.MAX_VALUE),
            StringServices.getValueWithClass(
                Integer.toString(Integer.MAX_VALUE),
                Integer.class.getName()));

        assertEquals(
            Boolean.TRUE,
            StringServices.getValueWithClass("true", Boolean.class.getName()));
    }

   /** 
    * Tests the {@link com.top_logic.basic.StringServices#toString(Object[])} method.
    */
    public void testToString() {
        Integer i = null;
        assertEquals(StringServices.EMPTY_STRING, StringServices.toString(i));
		i = Integer.valueOf(42);
        assertEquals("42", StringServices.toString(i));
        
        Object[] theObjects1 = new Object[] { "Holger" };
        Object[] theObjects2 = new Object[] { "Holger" , "Borchard"};
        Object[] theObjects3 = new Object[] { "Holger" , null, "Borchard"};

        assertEquals("{}"       , StringServices.toString((String[]) null));
        assertEquals("{}"       , StringServices.toString(new Object[0]));
        assertEquals("{null}"   , StringServices.toString(new Object[1]));
        assertEquals("{'Holger'}", StringServices.toString(theObjects1));
        assertEquals(
            "{'Holger','Borchard'}",
            StringServices.toString(theObjects2));
        assertEquals(
            "{'Holger',null,'Borchard'}",
            StringServices.toString(theObjects3));
    }

    /**
     * Tests the {@link com.top_logic.basic.StringServices#toString(Object[])} method.
     */
     public void testToString2() {
    	 String[] names = new String[]{"a","b","c"};
    	 assertEquals("a:b:c",StringServices.toString(names,':'));
		assertEquals("b:c", StringServices.toString(names, 1, 3, ':'));
		assertEquals("b", StringServices.toString(names, 1, 2, ':'));
		assertEquals("", StringServices.toString(names, 1, 1, ':'));
    	 
    	 names[1] = null;
    	 assertEquals("a:null:c",StringServices.join(names,':'));
    	 
         names[0] = null;
         assertEquals("null:null:c",StringServices.join(names,':'));

         assertEquals("",StringServices.toString(new String[]{},':'));
         
         names = new String[] {"a", "b", "c", "d"};
         assertEquals("a, b, c, d", StringServices.toString(names, ", "));
         assertEquals("a-b-c-d", StringServices.toString(names, "-"));
         assertEquals("a + b + c + d", StringServices.toString(names, " + "));
		assertEquals("a + b = c", StringServices.toString(names, 0, 3, " + ", " = "));
         assertEquals("a + b + c = d", StringServices.toString(names, " + ", " = "));
         assertEquals("a, b, c and d", StringServices.toString(names, ", ", " and "));
     }


     /** 
      * Tests the {@link com.top_logic.basic.StringServices#toNonNullList(CharSequence, char)} method.
      */
      public void testToNonNullList() {
          assertTrue (StringServices.toNonNullList("", 'x').isEmpty());   
          assertFalse(StringServices.toNonNullList("axa", 'x').isEmpty());   
      }
     
    /** 
     * Tests the {@link com.top_logic.basic.StringServices#toString(java.util.Collection, String)} method.
     */
     public void testToStringCollection() {
         String[]   namesA = new String[]{"a","b","c"};
         Collection<String> names  = Arrays.asList(namesA);
         assertEquals("a, b, c",StringServices.toString(names));
         assertEquals("a/=/b/=/c",StringServices.toString(names,"/=/"));
         
         names = new LinkedHashSet<>(names);
         assertEquals("a/=/b/=/c",StringServices.toString(names,"/=/"));
         
         names = Arrays.asList(new String[]{"a",null,"c"});
         assertEquals("a/=/null/=/c",StringServices.toString(names,"/=/"));
         
         assertEquals("",StringServices.toString(Collections.EMPTY_SET,"/=/"));
     }

    /** 
     * Tests the {@link com.top_logic.basic.StringServices#toString(java.util.Iterator, String)} method.
     */
     public void testToStringIter() {
         String[]   namesA = new String[]{"a","b","c"};
         Collection<String> names  = Arrays.asList(namesA);
         assertEquals("a%#%b%#%c",StringServices.toString(names.iterator(),"%#%"));
         
         namesA[1] = null;
         assertEquals("a%#%null%#%c",StringServices.toString(names.iterator(),"%#%"));
         
         assertEquals("",StringServices.toString(Collections.EMPTY_SET.iterator(),"%#%"));
     }

    /** 
     * Test the {@link StringServices#containsChar(String, String)} Method
     */
    public void testContainsChar() {
        assertFalse(StringServices.containsChar(null, null));
        assertFalse(StringServices.containsChar("a", null));
        assertFalse(StringServices.containsChar(null, "a"));
        
        assertTrue(StringServices.containsChar("Hallo", "ax"));
        assertTrue(StringServices.containsChar("Hallo", "xa"));
        assertTrue(StringServices.containsChar("Hallo", "Hallo"));
        assertTrue(StringServices.containsChar("Hallo", "aloGFH"));
        assertFalse(StringServices.containsChar("Hallo", "x"));
        
        assertTrue(StringServices.containsChar("ax", "Hallo"));
        assertTrue(StringServices.containsChar("xa", "Hallo"));
        assertTrue(StringServices.containsChar("Hallo", "Hallo"));
        assertTrue(StringServices.containsChar("aloGFH", "Hallo"));
        assertFalse(StringServices.containsChar("x", "Hallo"));
    }

    /** 
     * Test the {@link StringServices#containsChar(String, String)} Method
     */
    public void testContainsOnlyChars() {
        assertTrue(StringServices.containsOnlyChars(null, null));
        assertFalse(StringServices.containsOnlyChars("a", null));
        assertTrue(StringServices.containsOnlyChars(null, "a"));
        
        assertFalse(StringServices.containsOnlyChars("Hallo", "ax"));
        assertFalse(StringServices.containsOnlyChars("Hallo", "xa"));
        assertTrue(StringServices.containsOnlyChars("Hallo", "Hallo"));
        assertTrue(StringServices.containsOnlyChars("Hallo", "aloGFH"));
        assertFalse(StringServices.containsOnlyChars("Hallo", "x"));
        
        assertFalse(StringServices.containsOnlyChars("ax", "Hallo"));
        assertFalse(StringServices.containsOnlyChars("xa", "Hallo"));
        assertTrue(StringServices.containsOnlyChars("la", "Hallo"));
        assertTrue(StringServices.containsOnlyChars("Hallo", "Hallo"));
        assertFalse(StringServices.containsOnlyChars("aloGFH", "Hallo"));
        assertFalse(StringServices.containsOnlyChars("x", "Hallo"));
    }

	public void testCountChar() {
		String s = "Haaaaaaallloooa!";
		assertEquals(0, StringServices.count(null, 'H'));
		assertEquals(0, StringServices.count("", 'H'));

		assertEquals(1, StringServices.count(s, 'H'));
		assertEquals(8, StringServices.count(s, 'a'));
		assertEquals(0, StringServices.count(s, 'x'));
		assertEquals(0, StringServices.count(s, 'A'));
	}

    public void testCount() {
        String s = "Haaaaaaallloooa!";
        assertEquals(0, StringServices.count(null, "H"));
        assertEquals(0, StringServices.count("", "H"));

        assertEquals(1, StringServices.count(s, "H"));
        assertEquals(1, StringServices.count(s, "Ha"));
        assertEquals(8, StringServices.count(s, "a"));
        assertEquals(0, StringServices.count(s, "x"));
        assertEquals(0, StringServices.count(s, "A"));
        assertEquals(3, StringServices.count(s, "aa"));

        assertEquals(1, StringServices.count(s, "H", 0));
        assertEquals(1, StringServices.count(s, "Ha", 0));
        assertEquals(8, StringServices.count(s, "a", 0));
        assertEquals(0, StringServices.count(s, "x", 0));
        assertEquals(0, StringServices.count(s, "A", 0));
        assertEquals(3, StringServices.count(s, "aa", 0));

        assertEquals(0, StringServices.count(s, "H", 1));
        assertEquals(0, StringServices.count(s, "Ha", 1));
        assertEquals(3, StringServices.count(s, "a", 6));
        assertEquals(0, StringServices.count(s, "x", 3));
        assertEquals(0, StringServices.count(s, "A", 3));
        assertEquals(3, StringServices.count(s, "aa", 1));
        assertEquals(3, StringServices.count(s, "aa", 2));
        assertEquals(2, StringServices.count(s, "aa", 3));
        assertEquals(2, StringServices.count(s, "aa", 4));

        try {
            assertEquals(0, StringServices.count(s, null));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        try {
            assertEquals(0, StringServices.count(s, ""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        try {
            assertEquals(0, StringServices.count("", ""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
        try {
            assertEquals(0, StringServices.count(null, null));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

	public void testIndexOf() {
		assertIndexOf(0, "foobar", "");
		assertIndexOf(0, "foobar", "", -1);
		assertIndexOf(6, "foobar", "", 6);
		assertIndexOf(6, "foobar", "", 7);
		assertIndexOf(5, "foobar", "r", 5);
		assertIndexOf(-1, "foobar", "r", 6);
		assertIndexOf(-1, "foobar", "r", 7);
		
		assertIndexOf(0, "foobar", "foo");
		assertIndexOf(3, "foobar", "bar");
		assertIndexOf(-1, "foobar", "baz");

		assertIndexOf(-1, "foobar", "foo", 2);
		assertIndexOf(3, "foobar", "bar", 2);
		assertIndexOf(-1, "foobar", "baz", 2);

		assertIndexOf(0, "fo" + "oba" + "r", 2, 3, "o", 0);
		assertIndexOf(4, "fx" + "oxxoxobar", 2, 9, "xo", 3);
		assertIndexOf(-1, "fx" + "oxxox" + "obar", 2, 5, "xo", 3);
		assertIndexOf(1, "fo" + "obar", 2, 4, "bar", 0);
	}

	private void assertIndexOf(int expectedResult, String source, String target) {
		// Test the expectation to be compatible with String.indexOf()
		assertEquals(expectedResult, source.indexOf(target));
		
		assertEquals(expectedResult, StringServices.indexOf(source, target));
		assertEquals(expectedResult, StringServices.indexOf(source.toCharArray(), target.toCharArray()));
		assertEquals(expectedResult, StringServices.indexOf(new StringBuilder(source), target));

		assertIndexOf(expectedResult, source, target, 0);
	}

	private void assertIndexOf(int expectedResult, String source, String target, int fromIndex) {
		// Test the expectation to be compatible with String.indexOf()
		assertEquals(expectedResult, source.indexOf(target, fromIndex));
		
		assertEquals(expectedResult, StringServices.indexOf(source, target, fromIndex));
		assertEquals(expectedResult, StringServices.indexOf(source.toCharArray(), target.toCharArray(), fromIndex));
		assertEquals(expectedResult, StringServices.indexOf(new StringBuilder(source), target, fromIndex));
		
		assertIndexOf(expectedResult, source, 0, source.length(), target, fromIndex);
	}

	private void assertIndexOf(int expectedResult, String source, int sourcePos, int sourceLen, String target, int fromIndex) {
		// Test the expectation to be compatible with String.indexOf()
		assertEquals(expectedResult, source.substring(sourcePos, sourcePos + sourceLen).indexOf(target, fromIndex));

		assertEquals(expectedResult, StringServices.indexOf(source, sourcePos, sourceLen, target, 0, target.length(), fromIndex));
		assertEquals(expectedResult, StringServices.indexOf(source.toCharArray(), sourcePos, sourceLen, target.toCharArray(), 0, target.length(), fromIndex));
	}

    /** 
     * Test the Byte to HexString family of functions.
     */
    public void testHexBytes() {
        byte b1[] = new byte[1];
        for (int b = 0; b < 256; b++) {
            b1[0] = (byte) b;
            String hexHex = StringServices.toHexString(b1);
            byte   b2[]   = StringServices.hexStringToBytes(hexHex);
            String hex2   = StringServices.toHexString(b2);
            assertEquals(b1.length, b2.length);
            assertEquals(b1[0], b2[0]);
            assertEquals(hexHex, hex2);
        }
        b1 = StringServices.hexStringToBytes("ABCDEF");
        assertEquals(3, b1.length);
        assertEquals(b1[0] , (byte) 0xAB);
        assertEquals(b1[1] , (byte) 0xCD);
        assertEquals(b1[2] , (byte) 0xEF);
        try {
            b1 = StringServices.hexStringToBytes("DUMMY");
            fail("Expected NumberFormatException");
        } catch (NumberFormatException nfx) { /* expected */ }
    }
    
    /** 
     * Test the toString
     */
    public void testToStringList() {
        assertEquals("", StringServices.toString(Collections.EMPTY_LIST, ";"));
        
        List<Object> theList = new ArrayList<>();
        theList.add("A");
        assertEquals("A", StringServices.toString(theList , "+"));
        theList.add("B");
        assertEquals("A+B", StringServices.toString(theList , "+"));
        theList.add(null);
        assertEquals("A B null", StringServices.toString(theList , " "));
    }
    
    /** 
     * Tests depending on <code>ArrayList.toString()</code>
     */
    public void testToStringArrayList() {
        assertEquals("", StringServices.toString(Collections.EMPTY_LIST, ";"));
        
        List<Object> theList = new ArrayList<>();
        theList.add("A");
        assertEquals("A", StringServices.toString(theList , "+"));
        // Admitted, depends on ArrayList.toString(), but is to much fun !
        theList.add(theList);           // Cantor please don't look to close ;-)
        assertEquals("A+[A, (this Collection)]", StringServices.toString(theList  , "+"));
        theList.add("Z");
        assertEquals("A|[A, (this Collection), Z]|Z", StringServices.toString(theList , "|"));
    }
    
    /** 
     * Test the toString methods with Map
     */
    public void testToStringMap() {
        
        assertEquals("", StringServices.toString(Collections.emptyMap(), ';', ':'));
        
        Map<String, String> theMap = new LinkedHashMap<>();
        theMap.put("a", "A");
        
        assertEquals("a:A", StringServices.toString(theMap));

        theMap.put("b", "B");
        assertEquals("a:A,b:B", StringServices.toString(theMap));
        
        theMap.put(null, null);
        assertEquals("a:A,b:B,null:null", StringServices.toString(theMap));
    }

    /** 
     * Test the toString
     */
    public void testToStringCollectionFormat() {
        Format theFormat = CalendarUtil.getDateInstance();

        assertEquals("", StringServices.toString(Collections.EMPTY_LIST, ";", theFormat));
        assertEquals("", StringServices.toString(Collections.EMPTY_SET , "+", theFormat));
        
        List<Date> theList = new ArrayList<>();
        Date theDate = new Date();
        theList.add(theDate);
        String fd = theFormat.format(theDate);
        assertEquals(fd, StringServices.toString(theList , "+", theFormat));
        theList.add(theDate);
        assertEquals(fd + "+" + fd, StringServices.toString(theList , "+", theFormat));
    }

    /** Check some variants of isEmpty */
    public void testIsEmpty() {
        assertTrue (StringServices.isEmpty((String) null));
        assertTrue (StringServices.isEmpty(""));
        assertFalse(StringServices.isEmpty("~"));

        assertTrue (StringServices.isEmpty((Object) null));
        assertTrue (StringServices.isEmpty((Object) ""));
        assertFalse(StringServices.isEmpty((Object) "~"));
        assertFalse(StringServices.isEmpty(new Object()));
		assertFalse(StringServices.isEmpty(Double.valueOf(1.0)));

        assertTrue (StringServices.isEmpty((String[]) null));
        assertTrue (StringServices.isEmpty(new String[0]));
        assertFalse(StringServices.isEmpty(new String[1]));
        assertFalse(StringServices.isEmpty(new String[] {"Plimm", "Plamm"}));
    }

    public void testStartsWithChar() {
    	assertTrue(StringServices.startsWithChar("A", 'A'));
    	assertTrue(StringServices.startsWithChar("ABC", 'A'));
    	assertFalse(StringServices.startsWithChar("", 'a'));
    	assertFalse(StringServices.startsWithChar("A", 'a'));
    }
    
    public void testEndsWithChar() {
    	assertTrue(StringServices.endsWithChar("A", 'A'));
    	assertTrue(StringServices.endsWithChar("ABC", 'C'));
    	assertFalse(StringServices.endsWithChar("", 'a'));
    	assertFalse(StringServices.endsWithChar("A", 'a'));
    }
    
    /** Check some trivial methods not checked elsewhere. */
    public void testTrivial() {
        assertEquals(""  , StringServices.checkOnNullAndTrim(null));
        assertEquals(""  , StringServices.checkOnNullAndTrim(""));
        assertEquals(""  , StringServices.checkOnNullAndTrim("  "));
        assertEquals("A" , StringServices.checkOnNullAndTrim(" A "));
        assertEquals("B" , StringServices.checkOnNullAndTrim("B "));
        assertEquals("C" , StringServices.checkOnNullAndTrim(" C"));
    }
    
    public void testReplace() {
    	assertEquals(StringServices.replace("aabcdaeaadeaa", 'a', "xx"), "xxxxbcdxxexxxxdexxxx");
    	assertEquals(StringServices.replace("aabcdaeaadeaa", 'a', "xax"), "xaxxaxbcdxaxexaxxaxdexaxxax");
    	assertEquals(StringServices.replace("xbcdaeaadex", 'a', "xax"), "xbcdxaxexaxxaxdex");

    	// Returns the same string instance, if the character to search is not found.
    	String source = "bcdede";
		assertTrue(source == StringServices.replace(source, 'a', "a"));
		assertTrue(source == StringServices.replace(source, "ab", "ab"));

    	assertEquals(StringServices.replace("ababbcdabeababdeabab", "ab", "xx"), "xxxxbcdxxexxxxdexxxx");
    	
    	StringServicesRunnable runnable = new StringServicesRunnable("a", "", "");
    	Thread replacer = new Thread(runnable);
    	replacer.start();
    	try {
			replacer.join(2000);
		} catch (InterruptedException e) {}
		if(!replacer.isAlive()) {
			assertEquals("a", runnable.getResult());
		}
		else {
			replacer.stop();
			throw new AssertionFailedError("String replacement is in infinity loop!");
		}
		
		runnable = new StringServicesRunnable("", "", "");
		replacer = new Thread(runnable);
		replacer.start();
		try {
			replacer.join(2000);
		} catch (InterruptedException e) {}
		if(!replacer.isAlive()) {
			assertEquals("", runnable.getResult());
		}
		else {
			replacer.stop();
			throw new AssertionFailedError("String replacement is in infinity loop!");
		}
		
		runnable = new StringServicesRunnable("", "", "a");
		replacer = new Thread(runnable);
		replacer.start();
		try {
			replacer.join(2000);
		} catch (InterruptedException e) {}
		if(!replacer.isAlive()) {
			assertEquals("a", runnable.getResult());
		}
		else {
			replacer.stop();
			throw new AssertionFailedError("String replacement is in infinity loop!");
		}
		
		runnable = new StringServicesRunnable("abc", "", ";");
		replacer = new Thread(runnable);
		replacer.start();
		try {
			replacer.join(2000);
		} catch (InterruptedException e) {}
		if(!replacer.isAlive()) {
			assertEquals("a;b;c", runnable.getResult());
		}
		else {
			replacer.stop();
			throw new AssertionFailedError("String replacement is in infinity loop!");
		}
    }
    
    /**
	 * Test method for 'com.top_logic.layout.form.format.FillFormat.fillString(String, int, char, int)'
	 */
	public void testFillString() {
		String in = "xxx";
		
		assertEquals("##xxx",StringServices.fillString(in,5,'#',StringServices.START_POSITION_HEAD));
		assertEquals("xxx##",StringServices.fillString(in,5,'#',StringServices.START_POSITION_TAIL));

		assertEquals("xxx",StringServices.fillString(in,3,'#',StringServices.START_POSITION_TAIL));
		assertEquals("xxx",StringServices.fillString(in,1,'#',StringServices.START_POSITION_TAIL));
		
	}
	
	/** 
	 * test for {@link StringServices#stripString(String, char, int, String)} 
	 */
	public void testStripString() {
		String in = "##xxx##";
		
        assertEquals("emptyOnly",StringServices.stripString(""  ,'#',StringServices.START_POSITION_HEAD,"emptyOnly"));
        assertEquals("emptyOnly",StringServices.stripString(null,'#',StringServices.START_POSITION_HEAD,"emptyOnly"));
		assertEquals("xxx##",StringServices.stripString(in,'#',StringServices.START_POSITION_HEAD,""));
		assertEquals("##xxx",StringServices.stripString(in,'#',StringServices.START_POSITION_TAIL,""));

		in = "xxx";
		assertEquals("xxx",StringServices.stripString(in,'#',StringServices.START_POSITION_HEAD,""));
		assertEquals("xxx",StringServices.stripString(in,'#',StringServices.START_POSITION_TAIL,""));

		in = "##";
		assertEquals("xxx",StringServices.stripString(in,'#',StringServices.START_POSITION_HEAD,"xxx"));
		assertEquals("xxx",StringServices.stripString(in,'#',StringServices.START_POSITION_TAIL,"xxx"));

	}
    
    /**
     * Test method for getSpaces
     */
    public void testGetSpaces() {
        
        assertEquals(""   , StringServices.getSpaces(0));
        assertEquals(256  , StringServices.getSpaces(256).length());
        assertEquals("   ", StringServices.getSpaces(3));
        try {
             StringServices.getSpaces(-1);
        } catch (StringIndexOutOfBoundsException expected) { /* expected */ }
    }

    /**
     * No comments for test methods.
     */
    public void testRandomString() {
        assertEquals(1, StringServices.getRandomString(   1).length());
       
        String randomStr = StringServices.getRandomString(5000);
        assertEquals(5000, randomStr.length());
        for (int i = 0; i < randomStr.length(); i++) {
            char value = randomStr.charAt(i);
           
            assertTrue(Character.isDigit(value) || Character.isLetter(value));
        }
    }

    /**
	 * Test {@link StringServices#getRandomWords(int)}
	 */
    public void testRandomWords() {
        assertEquals(0   , StringServices.getRandomWords(0).length());
        assertEquals(1   , StringServices.getRandomWords(1).length());
        assertEquals(10  , StringServices.getRandomWords(10).length());
        assertEquals(100 , StringServices.getRandomWords(100).length());
        assertEquals(1000, StringServices.getRandomWords(1000).length());
    }

    /**
     * test {@link StringServices#durationStringToLong(String)}
     *
     */
    public void testDurationStringToLong() {
        assertValidDurationStringToLong(0L,"");
        assertValidDurationStringToLong(1000L,"1s");
        assertValidDurationStringToLong(60000L,"1m");
        assertValidDurationStringToLong(3600000L,"1h");
        assertValidDurationStringToLong(86400000L,"1d");
        assertValidDurationStringToLong(2592000000L,"1M");
        assertValidDurationStringToLong(31536000000L,"1y");
        
        try {
            StringServices.durationStringToLong("1");
            fail();
        } catch(IllegalArgumentException iae) {
            /* expected */
        }
    }

    /**
     * private method for testcase testDurationStringToLong.
     * @param expected  milliseconds as long   
     * @param parameter string to be converted
     */
    private void assertValidDurationStringToLong( long expected, String parameter) {
        assertEquals(expected, StringServices.durationStringToLong(parameter));
    }
    
    
    /**
     * test {@link StringServices#capitalizeString(String)}
     */
    public void testCapitalizeString() {
        assertEquals("Abc", StringServices.capitalizeString("abc"));
        assertEquals("Abc", StringServices.capitalizeString("Abc"));
        assertEquals("ABC", StringServices.capitalizeString("aBC"));
        assertEquals("ABC", StringServices.capitalizeString("ABC"));
        assertEquals("123Abc", StringServices.capitalizeString("123Abc"));
        assertEquals("", StringServices.capitalizeString(""));
        assertEquals(null, StringServices.capitalizeString(null));
    }

    public void testEquals() {
		assertTrue(StringServices.equals(null, null));
		assertFalse(StringServices.equals("A", null));
		assertFalse(StringServices.equals(null, "A"));
		assertFalse(StringServices.equals("A", "B"));
		assertTrue(StringServices.equals("A", "A"));
		assertTrue(StringServices.equals("A", new String("A")));
		assertFalse(StringServices.equals("", null));
		assertTrue(StringServices.equals("", ""));
    }

	public void testEqualsCharSequence() {
		assertTrue(StringServices.equalsCharSequence(null, null));
		assertFalse(StringServices.equalsCharSequence("A", null));
		assertFalse(StringServices.equalsCharSequence(null, "A"));
		assertFalse(StringServices.equalsCharSequence("A", "B"));
		assertTrue(StringServices.equalsCharSequence("A", "A"));
		assertTrue(StringServices.equalsCharSequence("A", new String("A")));
		assertFalse(StringServices.equalsCharSequence("A", "AA"));
		assertFalse(StringServices.equalsCharSequence("AA", "A"));
		assertFalse(StringServices.equalsCharSequence("", null));
		assertTrue(StringServices.equalsCharSequence("", ""));
	}

    public void testEqualsEmpty() {
        // test with Strings
        assertTrue(StringServices.equalsEmpty(null, null));
        assertTrue(StringServices.equalsEmpty("A", "A"));
        assertTrue(StringServices.equalsEmpty("", null));
        assertTrue(StringServices.equalsEmpty(null, ""));
        assertTrue(StringServices.equalsEmpty("", ""));
        
        assertFalse(StringServices.equalsEmpty(null, "A"));
        assertFalse(StringServices.equalsEmpty("A", null));
        assertFalse(StringServices.equalsEmpty("A", ""));
        assertFalse(StringServices.equalsEmpty("", "A"));
        assertFalse(StringServices.equalsEmpty("A", "B"));
        
        // test with non-String Objects
        Integer i1 = null, i2 = null;
        assertTrue(StringServices.equalsEmpty(i1, i2));
		i1 = Integer.valueOf(3);
        assertFalse(StringServices.equalsEmpty(i1, i2));
		i2 = Integer.valueOf(42);
        assertFalse(StringServices.equalsEmpty(i1, i2));
		i2 = Integer.valueOf(3);
        assertTrue(StringServices.equalsEmpty(i1, i2));
        i2 = null;
        assertFalse(StringServices.equalsEmpty(i1, i2));
    }
    
    /** 
     * Test the method {@link com.top_logic.basic.StringServices#trim(String)}.
     * 
     */
    public void testTrim() {
        assertEquals(StringServices.trim(null), null);
        String testString = " \t\rTesting   \n";
        assertEquals(StringServices.trim(testString), testString.trim());
    }
    
    public void testCutLeadingAndTrailing() {
        char c = '0';
        String s = null;
        String s1 = "1111";
        String s2 = "0111";
        String s3 = "0011";
        String s4 = "0001";
        String s5 = "0000";
        String s6 = "1000";
        String s7 = "1100";
        String s8 = "1110";
        String s9 = "0110";
        String s0 = "1001";

        assertEquals(null, StringServices.cutLeading(c, s));
        assertEquals("1111", StringServices.cutLeading(c, s1));
        assertEquals("111", StringServices.cutLeading(c, s2));
        assertEquals("11", StringServices.cutLeading(c, s3));
        assertEquals("1", StringServices.cutLeading(c, s4));
        assertEquals("", StringServices.cutLeading(c, s5));
        assertEquals("1000", StringServices.cutLeading(c, s6));
        assertEquals("1100", StringServices.cutLeading(c, s7));
        assertEquals("1110", StringServices.cutLeading(c, s8));
        assertEquals("110", StringServices.cutLeading(c, s9));
        assertEquals("1001", StringServices.cutLeading(c, s0));

        assertEquals(null, StringServices.cutTrailing(c, s));
        assertEquals("1111", StringServices.cutTrailing(c, s1));
        assertEquals("0111", StringServices.cutTrailing(c, s2));
        assertEquals("0011", StringServices.cutTrailing(c, s3));
        assertEquals("0001", StringServices.cutTrailing(c, s4));
        assertEquals("", StringServices.cutTrailing(c, s5));
        assertEquals("1", StringServices.cutTrailing(c, s6));
        assertEquals("11", StringServices.cutTrailing(c, s7));
        assertEquals("111", StringServices.cutTrailing(c, s8));
        assertEquals("011", StringServices.cutTrailing(c, s9));
        assertEquals("1001", StringServices.cutTrailing(c, s0));

        assertEquals(null, StringServices.trim(c, s));
        assertEquals("1111", StringServices.trim(c, s1));
        assertEquals("111", StringServices.trim(c, s2));
        assertEquals("11", StringServices.trim(c, s3));
        assertEquals("1", StringServices.trim(c, s4));
        assertEquals("", StringServices.trim(c, s5));
        assertEquals("1", StringServices.trim(c, s6));
        assertEquals("11", StringServices.trim(c, s7));
        assertEquals("111", StringServices.trim(c, s8));
        assertEquals("11", StringServices.trim(c, s9));
        assertEquals("1001", StringServices.trim(c, s0));
    }



    public static final String EMPTY_ATTRIBS = "";
    
    public static final String ONE_KEY_VALUE_PAIR = "key:value";
    
    public static final String LONG_KEY_VALUE_PAIR = "thisisthekey:of.com.top-logic.basic.metaattributes";
    
    public static final String KEY_VALUE_IS_EMPTY_STRING = ":";
    
    public static final String COLON_AND_SEMICOLON = ":;";
    
    public static final String MISSING_SEMICOLON = "keyone:value;keytwo:value:keythree:value;";
    
    /**
     * test {@link StringServices#getSemicolonSeparatedValue(String, String)} 
     */
    public void testGetSemicolonSeparatedValues() {

        final String ATTRIBUTES = 
            "klaus:halfmann;thomas:dickhut;daniel:navarro;christian:braun;tim:majunke;peter:alexander;steffen:kopmeier";
        
        assertEquals("halfmann", StringServices.getSemicolonSeparatedValue(ATTRIBUTES, "klaus"));
        assertEquals("navarro",  StringServices.getSemicolonSeparatedValue(ATTRIBUTES, "daniel"));
        assertEquals("kopmeier", StringServices.getSemicolonSeparatedValue(ATTRIBUTES, "steffen"));
        assertNull(StringServices.getSemicolonSeparatedValue(ATTRIBUTES, "keydoesnotexist"));
        assertNull(StringServices.getSemicolonSeparatedValue(EMPTY_ATTRIBS, ""));
        assertNull(StringServices.getSemicolonSeparatedValue(ATTRIBUTES, null));
        assertNull(StringServices.getSemicolonSeparatedValue(null, "key"));
        assertEquals("value", StringServices.getSemicolonSeparatedValue(ONE_KEY_VALUE_PAIR, "key"));
        assertEquals("of.com.top-logic.basic.metaattributes", StringServices.getSemicolonSeparatedValue(LONG_KEY_VALUE_PAIR, "thisisthekey"));
        assertEquals("", StringServices.getSemicolonSeparatedValue(KEY_VALUE_IS_EMPTY_STRING, ""));
        assertEquals("", StringServices.getSemicolonSeparatedValue(COLON_AND_SEMICOLON, ""));
        assertEquals("value:keythree:value", StringServices.getSemicolonSeparatedValue(MISSING_SEMICOLON, "keytwo"));
    }
    
    /**
     * test {@link StringServices#getAllSemicolonSeparatedValues(String)} 
     */
    public void testGetAllSemicolonSeparatedValuesAsProperty() {

        final String ATTRIBUTES = 
            "klaus:halfmann;thomas:dickhut;daniel:navarro;christian:braun;tim:majunke;peter:alexander;steffen:kopmeier";
        
        assertTrue(StringServices.getAllSemicolonSeparatedValues(ATTRIBUTES).containsKey("klaus"));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(ATTRIBUTES).containsValue("kopmeier"));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(null).isEmpty());
        assertTrue(StringServices.getAllSemicolonSeparatedValues(KEY_VALUE_IS_EMPTY_STRING).containsKey(""));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(LONG_KEY_VALUE_PAIR).containsValue("of.com.top-logic.basic.metaattributes"));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(EMPTY_ATTRIBS).isEmpty());
        assertTrue(StringServices.getAllSemicolonSeparatedValues(COLON_AND_SEMICOLON).containsKey(""));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(COLON_AND_SEMICOLON).containsValue(""));
        assertTrue(StringServices.getAllSemicolonSeparatedValues(MISSING_SEMICOLON).containsValue("value:keythree:value"));
    }
    
    public void testgetStringOrDefault() {
        assertTrue(StringServices.getStringOrDefault("", null) == null);
        assertEquals("default", StringServices.getStringOrDefault(null, "default"));
        assertEquals("default", StringServices.getStringOrDefault("", "default"));
        assertEquals("aString", StringServices.getStringOrDefault("aString", "default"));
    }
    
    public void testNormalizeWhiteSpace() {
        // Basic case when input is null
        assertEquals(null, StringServices.normalizeWhiteSpace(null));
        
    	// Non space white space is replaced by spaces.
    	assertEquals("Hello World !", StringServices.normalizeWhiteSpace("Hello\f\t\n\rWorld !"));
    	
    	// Leading an tailing white space is removed.
    	assertEquals("Hello World !", StringServices.normalizeWhiteSpace(" \f\t\n\rHello \f\t\n\r World\f\t\n\r \f\t\n\r!\f\t\n\r "));
    	
    	// A string that consists of only white space is replaced by the empty string.
    	assertEquals("", StringServices.normalizeWhiteSpace("\f\t\n\r \f\t\n\r"));
    	
    	// Tailing white space from an otherwise already normalized string is removed.
    	assertEquals("Hello World !", StringServices.normalizeWhiteSpace("Hello World ! "));
    	assertEquals("Hello World !", StringServices.normalizeWhiteSpace("Hello World !    "));
    	assertEquals("Hello World !", StringServices.normalizeWhiteSpace("Hello World !\f\t\n\r"));

    	// A string that is already in normalized form is not copied.
    	assertSame("Hello World!", StringServices.normalizeWhiteSpace("Hello World!"));
    }
    
    public void testConcatenate2Args() {
    	assertEquals("foobar", StringServices.concatenate("foo", "bar"));
    	
    	assertEquals("foo", StringServices.concatenate("foo", null));
    	assertEquals("bar", StringServices.concatenate(null, "bar"));
    	
    	assertEquals("", StringServices.concatenate(null, null));
    }
    
    public void testConcatenate() {
    	assertEquals("foobartzu", StringServices.concatenate("foo", "bar", "tzu"));
    	
    	assertEquals("", StringServices.concatenate());

    	assertEquals("bartzu", StringServices.concatenate(null, "bar", "tzu"));
    	assertEquals("footzu", StringServices.concatenate("foo", null, "tzu"));
    	assertEquals("foobar", StringServices.concatenate("foo", "bar", null));

    	assertNotNull(StringServices.concatenate(null, null, null));
    	
    }
    
	public void testDebug() {
		assertEquals("'null' (Class: null)", debug(null));
		assertEquals("'42' (Class: java.lang.Integer)", debug(42));
		/* ConfigItems are implemented via "java.lang.reflect.Proxy". Their class name is useless
		 * ("com.sun.proxy.$Proxy406"). It should therefore be enriched with the implemented config
		 * interface. */
		PolymorphicConfiguration<?> configItem = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		String debugText = debug(configItem);
		assertContains("Proxy", debugText);
		assertContains(PolymorphicConfiguration.class.getSimpleName(), debugText);
	}

	public void testGetClassNameNullsafe() {
		assertEquals("null", getClassNameNullsafe(null));
		assertEquals("java.lang.Integer", getClassNameNullsafe(42));
		/* See testDebug() for an explanation. */
		PolymorphicConfiguration<?> configItem = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		String debugText = getClassNameNullsafe(configItem);
		assertContains("Proxy", debugText);
		assertContains(PolymorphicConfiguration.class.getSimpleName(), debugText);
	}

	public void testGetNameNullsafe() {
		assertEquals("null", getNameNullsafe(null));
		assertEquals("java.lang.Integer", getNameNullsafe(Integer.class));
		/* See testDebug() for an explanation. */
		PolymorphicConfiguration<?> configItem = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		String debugText = getNameNullsafe(configItem.getClass());
		assertContains("Proxy", debugText);
		assertContains(PolymorphicConfiguration.class.getSimpleName(), debugText);
	}

	/**
	 * Test that {@link StringServices#insert(String, CharSequence, int)} throws an
	 * {@link NullPointerException} if any of the arguments is <code>null</code>.
	 */
	public void testInsertNull() {
		assertInsertThrowsNullPointerException("", null);
		assertInsertThrowsNullPointerException(null, "");
		assertInsertThrowsNullPointerException(null, null);
	}

	private void assertInsertThrowsNullPointerException(String text, String toInsert) {
		try {
			StringServices.insert(text, toInsert, 0);
			fail("Expected a NullPointerException, but method returned normally!");
		} catch (NullPointerException ex) {
			// Correct
		}
	}

	/**
	 * Test that {@link StringServices#insert(String, CharSequence, int)} works correctly for the
	 * "normal" use cases. (Where no exception should be thrown.)
	 */
	public void testInsertNormal() {
		assertEquals("", StringServices.insert("", "", 0));
		assertEquals("A", StringServices.insert("A", "", 0));
		assertEquals("A", StringServices.insert("", "A", 0));
		assertEquals("AB", StringServices.insert("B", "A", 0));
		assertEquals("AB", StringServices.insert("A", "B", 1));
		assertEquals("ABC", StringServices.insert("BC", "A", 0));
		assertEquals("ABC", StringServices.insert("AC", "B", 1));
		assertEquals("ABC", StringServices.insert("AB", "C", 2));
	}

	/**
	 * Tests that {@link StringServices#insert(String, CharSequence, int)} throws an
	 * {@link IndexOutOfBoundsException} if the index is out of bounds.
	 */
	public void testInsertIndexOutOfBounds() {
		assertInsertThrowsIndexOutOfBoundsException("", "", -1);
		assertInsertThrowsIndexOutOfBoundsException("", "", 1);
		assertInsertThrowsIndexOutOfBoundsException("A", "", -1);
		assertInsertThrowsIndexOutOfBoundsException("A", "", 2);
		assertInsertThrowsIndexOutOfBoundsException("AB", "", -1);
		assertInsertThrowsIndexOutOfBoundsException("AB", "", 3);

		assertInsertThrowsIndexOutOfBoundsException("", "X", -1);
		assertInsertThrowsIndexOutOfBoundsException("", "X", 1);
		assertInsertThrowsIndexOutOfBoundsException("A", "X", -1);
		assertInsertThrowsIndexOutOfBoundsException("A", "X", 2);
		assertInsertThrowsIndexOutOfBoundsException("AB", "X", -1);
		assertInsertThrowsIndexOutOfBoundsException("AB", "X", 3);
	}

	private void assertInsertThrowsIndexOutOfBoundsException(String text, String toInsert, int index) {
		try {
			StringServices.insert(text, toInsert, index);
			fail("Inserting at an index that is out of bounds did not throw an exception!");
		} catch (IndexOutOfBoundsException exception) {
			// Correct
		}
	}

	/**
	 * The method constructing a test suite for this class.
	 * 
	 * @return The test to be executed.
	 */
    public static Test suite() {
         return BasicTestSetup.createBasicTestSetup(new TestSuite(TestStringServices.class));
         // return new TestStringServices("testRandomWords");
    }


    /**
     * The main program for executing this test also from console.
     *
     * @param    args    Will be ignored.
     */
    public static void main(String[] args) {
        SHOW_TIME = true;

        TestRunner.run(suite());
    }
    
    /**
     * Runnable to string replacement in seperate thread.
     * 
     *
     * @author    <a href=mailto:sts@top-logic.com>sts</a>
     */
    private class StringServicesRunnable implements Runnable {
    	
    	private String result;
    	private String target;
    	private String searchString;
    	private String replacementString;
    	
    	/*package protected*/ StringServicesRunnable(String target, String searchString, String replacementString) {
    		this.result = "";
    		this.target = target;
    		this.searchString = searchString;
    		this.replacementString = replacementString;
    	}

		/** 
		 * {@inheritDoc}
		 */
		@Override
		public void run() {
			result = StringServices.replace(target, searchString, replacementString);
		}
    	
		public String getResult() {
			return result;
		}
    }
}
