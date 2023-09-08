/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.compare;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.diff.LongestCommonSubsequence;

/**
 * Test case for {@link LongestCommonSubsequence}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLongestCommonSubsequence extends TestCase {

	public void testFindSubSequence() {
		assertEquals("MJAU", compute("MZJAWXU", "XMJYAUZ"));
		assertEquals("ABCDEFG", compute("ABC_DEFG", "_AB_CDE_FG_"));
	}

	public void testSame() {
		assertEquals("ABC", compute("ABC", "ABC"));
	}

	public void testPrefixSuffix() {
		assertEquals("ABC", compute("ABC", "ABCDEF"));
		assertEquals("ABC", compute("ABCDEF", "ABC"));
	}

	public void testCommonPrefixSuffix() {
		assertEquals("ABCD", compute("AxByCD", "ABzCD"));
		assertEquals("BCD", compute("xByCD", "ABzCD"));
		assertEquals("AB", compute("AxByCD", "ABz"));
	}

	public void testEmpty() {
		assertEquals("", compute("AAA", "BBBBBB"));
		assertEquals("", compute("", "A"));
		assertEquals("", compute("A", ""));
		assertEquals("", compute("", ""));
	}

	public void testNonUnique() {
		assertEquals("BA", compute("ABA", "BAB"));
		assertEquals("GC", compute("AGCAT", "GAC"));
	}

	public void testPureInsertion() {
		assertEquals("ABE", compute("ABE", "ABCDE"));
	}

	public void testPureDeletion() {
		assertEquals("ABE", compute("ABCDE", "ABE"));
	}

	private String compute(String left, String right) {
		return flatten(LongestCommonSubsequence.compute(chars(left), chars(right)));
	}

	private String flatten(List<? extends Character> chars) {
		StringBuilder buffer = new StringBuilder();
		for (Character ch : chars) {
			buffer.append(ch.charValue());
		}
		return buffer.toString();
	}

	private List<Character> chars(String string) {
		ArrayList<Character> result = new ArrayList<>(string.length());
		for (int n = 0, lenth = string.length(); n < lenth; n++) {
			result.add(Character.valueOf(string.charAt(n)));
		}
		return result;
	}

}
