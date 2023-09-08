/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.tool.export.ExportUtil;

/**
 * Test support for {@link ExportUtil}.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class TestExportUtil extends BasicTestCase {

	/**
	 * Main method for executing this test directly.
	 */
	public static void main(String[] args) {
		SHOW_TIME = true;
		TestRunner.run(suite());
	}

	/**
	 * Constructs a test suite for this class.
	 * 
	 * @return The test to be executed.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestExportUtil.class);
	}

	/**
	 * Test the cutting function in general, e.g. it does not return null, the cutted text is
	 * shorter than the original and the cutted text ends with dots.
	 */
	public void testCutTextGeneral() {
		String text = "aaaaaaaaaa";
		String cuttedText = ExportUtil.cutText(text, 1, 4);

		assertTrue(cuttedText != null);
		if (cuttedText != null) {
			assertTrue(cuttedText.length() < text.length());
			assertTrue(cuttedText.endsWith(ExportUtil.DOTS));
		}
	}

	/**
	 * Test specific cutting functions.
	 */
	public void testCutTextSpecific() {
		String text;
		String expected;
		String cuttedText;
		CharSizeMap fontSizeMap = ProportionalCharSizeMap.INSTANCE;

		text = "aaaaaaaa";
		expected = "aaa...";
		cuttedText = ExportUtil.cutText(text, 1, 6, fontSizeMap, true);
		assertEquals(cuttedText, expected);
		assertTrue(ExportUtil.needsToBeCut(text, 1, 6, fontSizeMap));
		
		text = "Masterblaster";
		expected = "Master...";
		cuttedText = ExportUtil.cutText(text, 1, 9, fontSizeMap, true);
		assertEquals(cuttedText, expected);
	}

	/**
	 * Test dot insertion for text with linebreaks and more available columns than text length. Must
	 * not cut text to add dots if there are more columns available than text plus dots will need.
	 */
	public void testCutTextLong() {
		String text = "aaaaaaaaaa\naaaaaaaaaa";

		String cuttedTextSingleLine = ExportUtil.cutText(text, 1, 100, ProportionalCharSizeMap.INSTANCE, false);
		assertEquals("aaaaaaaaaa...", cuttedTextSingleLine);

		String cuttedTextMultiline = ExportUtil.cutText(text, 2, 100, ProportionalCharSizeMap.INSTANCE, false);
		assertEquals(text, cuttedTextMultiline);

	}

	/**
	 * Test dot insertion for text with line breaks and less available columns than text length.
	 * Must not append dots to cutted string if this will be bigger than available space.
	 */
	public void testCutTextShort() {
		String text = "aaaaaaaaaa\naaaaaaaaaa";

		String cuttedTextSingleLine = ExportUtil.cutText(text, 1, 2, ProportionalCharSizeMap.INSTANCE, false);
		assertEquals("...", cuttedTextSingleLine);

		String cuttedTextMultiLine = ExportUtil.cutText(text, 2, 2, ProportionalCharSizeMap.INSTANCE, false);
		assertEquals("aa\n...", cuttedTextMultiLine);

	}

}
