/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.format;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.basic.format.IdentityFormat;
import com.top_logic.basic.format.ListFormat;

/**
 * Test case for {@link ListFormat}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestListFormat extends TestCase {

	public void testParseNumList() throws ParseException {
		ListFormat format = new ListFormat(new DecimalFormat("#.00"), ';');

		assertEquals("1.00; 2.00; 3.42", format.format(Arrays.asList(1, 2, 3.417)));
		assertEquals(Arrays.asList(1L, 2L, 3.42d), parse(format, "1; 2; 3.42"));
	}

	public void testParseStringListWithTrim() throws ParseException {
		ListFormat format = new ListFormat(IdentityFormat.INSTANCE, "\n", "\n", true);
		
		assertEquals("foo\nbar\nfoobar with foo and bar",
			format.format(Arrays.asList("foo", "bar", "foobar with foo and bar")));
		assertEquals(Arrays.asList("foo", "bar", "foobar with foo and bar"),
			parse(format, " foo  \n\t bar \n foobar with foo and bar\t \n\n"));
	}

	public void testParseStringListWithoutTrim() throws ParseException {
		ListFormat format = new ListFormat(IdentityFormat.INSTANCE, "\n", "\n", false);

		assertEquals(Arrays.asList(" foo  ", "\t bar ", " foobar with foo and bar\t ", " \t "),
			parse(format, " foo  \n\t bar \n foobar with foo and bar\t \n \t \n\n"));
	}

	public void testParseError() {
		ListFormat format = new ListFormat(new DecimalFormat("#.00"), ';');

		try {
			format.parseObject("1; 2 foobar; 3");
			fail("Expected parse error.");
		} catch (ParseException ex) {
			assertEquals(5, ex.getErrorOffset());
		}
	}

	public void testInnerParseError() {
		ListFormat format = new ListFormat(new DecimalFormat("#.00"), ';');

		try {
			format.parseObject("1; foobar; 3");
			fail("Expected parse error.");
		} catch (ParseException ex) {
			assertEquals(3, ex.getErrorOffset());
		}
	}

	public void testSingle() throws ParseException {
		ListFormat format = new ListFormat(new DecimalFormat("#.00"), ';');

		assertEquals("1.00", format.format(1));
		assertEquals(Arrays.asList(1L), parse(format, "1"));
	}

	public void testNone() throws ParseException {
		ListFormat format = new ListFormat(new DecimalFormat("#.00"), ';');

		assertEquals("", format.format(null));
		assertEquals(Arrays.asList(), parse(format, ""));
	}

	private Object parse(ListFormat format, String string) throws ParseException {
		ParsePosition pos = new ParsePosition(0);
		Object result1 = format.parseObject(string, pos);
		assertEquals(string.length(), pos.getIndex());

		Object result2 = format.parseObject(string);
		assertEquals(result1, result2);

		return result2;
	}

}
