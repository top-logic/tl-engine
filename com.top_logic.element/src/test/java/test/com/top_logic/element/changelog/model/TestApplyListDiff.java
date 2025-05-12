/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.changelog.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.col.diff.CollectionDiff;
import com.top_logic.basic.col.diff.op.DiffOp;
import com.top_logic.element.changelog.model.ApplyListDiff;

/**
 * Test case for {@link ApplyListDiff}.
 */
@SuppressWarnings("javadoc")
public class TestApplyListDiff extends TestCase {

	public void testMoveLeftWithChanged() {
		List<String> left = Arrays.asList("a", "b", "c", "d");
		List<String> right = Arrays.asList("a", "d", "b", "c");
		List<String> now = Arrays.asList("x", "b", "c", "d");
		List<String> expected = Arrays.asList("x", "d", "b", "c");

		doTest(left, right, now, expected);
	}

	public void testMoveRightWithChanged() {
		List<String> left = Arrays.asList("a", "b", "c", "d");
		List<String> right = Arrays.asList("b", "c", "d", "a");
		List<String> now = Arrays.asList("a", "b", "x", "d");
		List<String> expected = Arrays.asList("b", "x", "d", "a");

		doTest(left, right, now, expected);
	}

	public void testAddWithAdded() {
		List<String> left = Arrays.asList("a", "b", "c");
		List<String> right = Arrays.asList("a", "b", "x", "c");
		List<String> now = Arrays.asList("y1", "b", "c", "y2");
		List<String> expected = Arrays.asList("y1", "b", "x", "c", "y2");

		doTest(left, right, now, expected);
	}

	public void testAddWithRemoved() {
		List<String> left = Arrays.asList("a", "b", "c");
		List<String> right = Arrays.asList("a", "x", "b", "c");
		List<String> now = Arrays.asList("a", "c");
		List<String> expected = Arrays.asList("a", "c", "x");

		doTest(left, right, now, expected);
	}

	public void testRemoveWithRemoved() {
		List<String> left = Arrays.asList("a", "b", "c");
		List<String> right = Arrays.asList("a", "c");
		List<String> now = Arrays.asList("b", "c");
		List<String> expected = Arrays.asList("c");

		doTest(left, right, now, expected);
	}

	private void doTest(List<String> left, List<String> right, List<String> now, List<String> expected) {
		List<DiffOp<String>> diff = CollectionDiff.diffList(x -> x, left, right);

		List<String> merged = new ArrayList<>(now);
		for (DiffOp<String> patch : diff) {
			patch.visit(ApplyListDiff.getInstance(), merged);
		}

		assertEquals(expected, merged);
	}

}
