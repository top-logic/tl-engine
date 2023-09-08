/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.generate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.generate.CodeUtil;

/**
 * Test teh {@link CodeUtil}
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
@SuppressWarnings("javadoc")
public class TestCodeUtil extends TestCase {

    /** 
     * Create a new TestCodeUtil.
     */
    public TestCodeUtil(String name) {
        super(name);
    }

    /**
     * Test method for {@link com.top_logic.basic.generate.CodeUtil#toCamelCase(java.lang.String)}.
     */
    public void testToCamelCase() {
        assertEquals("ThisIsADromedar", CodeUtil.toCamelCase("ThisIsADromedar"));
        assertEquals("ThisIsADromedar", CodeUtil.toCamelCase("this-is.A_dromedar"));
		assertEquals("A", CodeUtil.toCamelCase("a"));
		assertEquals("", CodeUtil.toCamelCase(""));
		assertEquals("", CodeUtil.toCamelCase("_"));
    }

	public void testEnglishLabel() {
		// assertEquals("This is a dromedar", CodeUtil.englishLabel("ThisIsADromedar"));
		assertEquals("This is a dromedar", CodeUtil.englishLabel("this-is.A_dromedar"));
		assertEquals("DB access", CodeUtil.englishLabel("DBAccess"));
		assertEquals("My DB access", CodeUtil.englishLabel("MyDBAccess"));
		assertEquals("Foobar", CodeUtil.englishLabel("FOOBAR"));
		assertEquals("Some kind of identifier", CodeUtil.englishLabel("SomeKindOfIdentifier"));
		assertEquals("Some kind of identifier", CodeUtil.englishLabel("SOME_KIND_OF_IDENTIFIER"));
		assertEquals("Some kind of identifier", CodeUtil.englishLabel("SOME_KIND__OF___IDENTIFIER"));
		assertEquals("I18N string", CodeUtil.englishLabel("I18NString"));
		assertEquals("3 color", CodeUtil.englishLabel("3Color"));
	}

    /**
     * Test method for {@link com.top_logic.basic.generate.CodeUtil#toUpperCaseStart(java.lang.String)}.
     */
    public void testToUpperCaseStart() {
        assertEquals("Alllowercase", CodeUtil.toUpperCaseStart("alllowercase"));
		assertEquals("A", CodeUtil.toUpperCaseStart("a"));
		assertEquals("", CodeUtil.toUpperCaseStart(""));
    }

	public void testToLowerCaseStart() {
		assertEquals("alllowercase", CodeUtil.toLowerCaseStart("Alllowercase"));
		assertEquals("a", CodeUtil.toLowerCaseStart("A"));
		assertEquals("", CodeUtil.toLowerCaseStart(""));
		assertEquals("dbAccess", CodeUtil.toLowerCaseStart("DBAccess"));
		assertEquals("foobar", CodeUtil.toLowerCaseStart("FOOBAR"));
	}

    /**
     * Test method for {@link com.top_logic.basic.generate.CodeUtil#toStringLiteral(java.lang.String)}.
     */
    public void testToStringLiteral() {
        assertEquals("\"This\\nis\\nsome\\nText\"", CodeUtil.toStringLiteral("This\nis\nsome\nText"));
    }

    /**
     * Test method for {@link com.top_logic.basic.generate.CodeUtil#toAllUpperCase(java.lang.String)}.
     */
    public void testToAllUpperCase() {
        assertEquals("SOME_KIND_OF_IDENTIFIER", CodeUtil.toAllUpperCase("SomeKindOfIdentifier"));
		assertEquals("DB_ACCESS", CodeUtil.toAllUpperCase("DBAccess"));
		assertEquals("MY_DB_ACCESS", CodeUtil.toAllUpperCase("MyDBAccess"));
		assertEquals("A", CodeUtil.toAllUpperCase("a"));
		assertEquals("", CodeUtil.toAllUpperCase(""));
    }

	public static Test suite() {
		return new TestSuite(TestCodeUtil.class);
	}

}

