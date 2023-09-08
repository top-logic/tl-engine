/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.text.ParsePosition;

import junit.framework.TestCase;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.format.FillFormat;

/**
 * @author     <a href="mailto:tma@top-logic.com">tma</a>
 */
public class TestFillFormat extends TestCase {


	/*
	 * Test method for 'com.top_logic.layout.form.format.FillFormat.parseObject(String, ParsePosition)'
	 */
	public void testParseObjectStringParsePosition() {
		FillFormat fillFormat = new FillFormat(5,StringServices.START_POSITION_HEAD,'#');
		
		ParsePosition parsePosition = new ParsePosition(0);
		String input = "123";
		String result = (String) fillFormat.parseObject(input,parsePosition);
		
		// "Parsing" for the FillFormat mean "consuming at most length
		// characters. The filling only occurs during formatting (see below).
		assertEquals(input,result);
		assertEquals(3,parsePosition.getIndex());
		
		parsePosition = new ParsePosition(0);
		input = "123456";
		result = (String) fillFormat.parseObject(input,parsePosition);
		
		assertEquals("12345",result);
		assertEquals(5,parsePosition.getIndex());
	}

	/*
	 * Test method for 'com.top_logic.layout.form.format.FillFormat.format(Object, StringBuffer, FieldPosition)'
	 */
	public void testFormatObjectStringBufferFieldPosition() {
		FillFormat fillFormat = new FillFormat(5,StringServices.START_POSITION_HEAD,'#');
		StringBuffer sb = new StringBuffer();
		fillFormat.format("xxx",sb,null);
		assertEquals("##xxx",sb.toString());
		
		fillFormat = new FillFormat(5,StringServices.START_POSITION_TAIL,'#');
		sb = new StringBuffer();
		fillFormat.format("xxx",sb,null);
		assertEquals("xxx##",sb.toString());
		
		try{
			fillFormat.format("too long",sb,null);
			fail("Expected IllegalArgumentException");
		}catch (IllegalArgumentException e) {
			// expected
		}
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestFillFormat.class);
	}
}
