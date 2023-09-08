/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.message;

import java.text.ParseException;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.message.Message;
import com.top_logic.util.message.MessageStoreFormat;

/**
 * Test case for {@link MessageStoreFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMessageStoreFormat extends TestCase {

	public void testSerialize() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_0", Messages.MSG_0);
		assertFormat("test.com.top_logic.util.message.MSG_0", Messages.MSG_0.fill());
		assertFormat("test.com.top_logic.util.message.MSG_1|I42", Messages.MSG_1.fill(42));
		assertFormat("test.com.top_logic.util.message.MSG_2|I42|I13", Messages.MSG_2.fill(42, 13));
	}
	
	public void testBoolean() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|true", Messages.MSG_1.fill(true));
		assertFormat("test.com.top_logic.util.message.MSG_1|false", Messages.MSG_1.fill(false));
	}
	
	public void testDate() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|2010-04-26T12:35:10.123Z", Messages.MSG_1.fill(XmlDateTimeFormat.INSTANCE.parseObject("2010-04-26T12:35:10.123Z")));
	}
	
	public void testNumbers() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|L42", Messages.MSG_1.fill(42L));
		assertFormat("test.com.top_logic.util.message.MSG_1|F42.13", Messages.MSG_1.fill(42.13F));
		assertFormat("test.com.top_logic.util.message.MSG_1|D42.13", Messages.MSG_1.fill(42.13D));
		assertFormat("test.com.top_logic.util.message.MSG_1|D-42.13", Messages.MSG_1.fill(-42.13D));
	}
	
	public void testNullValue() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|_", Messages.MSG_1.fill(null));
	}
	
	public void testClassValue() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|%" + TestMessageStoreFormat.class.getName(), Messages.MSG_1.fill(TestMessageStoreFormat.class));
	}
	
	public void testList() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|[\"foo\",\"bar\"]", Messages.MSG_1.fill(BasicTestCase.list("foo", "bar")));
	}
	
	public void testMap() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|{\"foo\":I42,I13:I14}", Messages.MSG_1.fill(new MapBuilder<>().put("foo", 42).put(13, 14).toMap()));
	}
	
	public void testInnerMessage() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|(test.com.top_logic.util.message.MSG_0)", Messages.MSG_1.fill(Messages.MSG_0.fill()));
	}

	public void testQuoting() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|\"\\\"foo\\\"\"", Messages.MSG_1.fill("\"foo\""));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"\\\\foo\\\\\"", Messages.MSG_1.fill("\\foo\\"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"|foo|\"", Messages.MSG_1.fill("|foo|"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"-foo-\"", Messages.MSG_1.fill("-foo-"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"_foo_\"", Messages.MSG_1.fill("_foo_"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"%foo%\"", Messages.MSG_1.fill("%foo%"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"{}foo{}\"", Messages.MSG_1.fill("{}foo{}"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"[]foo[]\"", Messages.MSG_1.fill("[]foo[]"));
		assertFormat("test.com.top_logic.util.message.MSG_1|\"()foo()\"", Messages.MSG_1.fill("()foo()"));
	}
	
	public void testQuotingInner() throws ParseException {
		assertFormat("test.com.top_logic.util.message.MSG_1|{\"key{[(_-%$&:,|&$%-_)]}\":\"\\\"\\\\value\\\\\\\"{[(_-%$&:,|&$%-_)]}\"}", Messages.MSG_1.fill(Collections.singletonMap("key{[(_-%$&:,|&$%-_)]}", "\"\\value\\\"{[(_-%$&:,|&$%-_)]}")));
		assertFormat("test.com.top_logic.util.message.MSG_1|[\"\\\"\\\\value\\\\\\\"{[(_-%$&:,|&$%-_)]}\"]", Messages.MSG_1.fill(Collections.singletonList("\"\\value\\\"{[(_-%$&:,|&$%-_)]}")));
		assertFormat("test.com.top_logic.util.message.MSG_1|(test.com.top_logic.util.message.MSG_1|\"\\\"\\\\value\\\\\\\"{[(_-%$&:,|&$%-_)]}\")", Messages.MSG_1.fill(Messages.MSG_1.fill("\"\\value\\\"{[(_-%$&:,|&$%-_)]}")));
	}
	
	private static void assertFormat(String serializedForm, Message message) throws ParseException {
		assertEquals(serializedForm, MessageStoreFormat.toString(message));
		
		assertEquals(serializedForm, MessageStoreFormat.toString(MessageStoreFormat.parseMessage(serializedForm)));
	}

	public static Test suite() {
		return new TestSuite(TestMessageStoreFormat.class);
	}

}
