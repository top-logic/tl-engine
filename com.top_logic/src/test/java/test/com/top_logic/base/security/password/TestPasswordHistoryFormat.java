/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.password;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.security.password.PasswordHistoryFormat;

/**
 * Tests for {@link PasswordHistoryFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPasswordHistoryFormat extends BasicTestCase {

	public void testParsePasswordHistory() throws ParseException {
		assertEquals(list(), parsePwdHistory(""));
		assertEquals(list("pwd1"), parsePwdHistory("pwd1"));
		assertEquals(list("pwd1", "pwd2"), parsePwdHistory("pwd1!pwd2"));
		assertEquals(list("", "pwd", ""), parsePwdHistory("!pwd!"));
		assertEquals(list("pwd1", "", "pwd2"), parsePwdHistory("pwd1!!pwd2"));
		assertEquals(list("pwd1", "pwd2!"), parsePwdHistory("pwd1!pwd2:!"));
		assertEquals(list("pwd1", "pwd2:!"), parsePwdHistory("pwd1!pwd2:::!"));
		assertEquals(list("pwd1!", "pwd2"), parsePwdHistory("pwd1:!!pwd2"));
		assertEquals(list("pwd1", "!pwd2"), parsePwdHistory("pwd1!:!pwd2"));
		assertEquals(list(":"), parsePwdHistory("::"));
		assertEquals(list("!"), parsePwdHistory(":!"));
		assertEquals(list("", "!", ""), parsePwdHistory("!:!!"));

		assertIllegal("pwd1:", 5);
		assertIllegal("pwd1!:", 6);
		assertIllegal("pwd1:df", 5);
	}

	private void assertIllegal(String string, int errInd) {
		ParsePosition p = new ParsePosition(0);
		Object result = PasswordHistoryFormat.INSTANCE.parseObject(string, p);
		assertNull("Expected failure", result);
		assertEquals(errInd, p.getErrorIndex());
	}

	@SuppressWarnings("unchecked")
	private static List<String> parsePwdHistory(String pwdHashString) throws ParseException {
		PasswordHistoryFormat format = PasswordHistoryFormat.INSTANCE;
		List<String> result = (List<String>) format.parseObject(pwdHashString);
		assertEquals(pwdHashString, format.format(result));
		return result;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPasswordHistoryFormat}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestPasswordHistoryFormat.class);
	}

}

