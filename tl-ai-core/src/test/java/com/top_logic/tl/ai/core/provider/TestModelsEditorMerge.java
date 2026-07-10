/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for the pure dedup-append logic in {@link ModelsEditor#merge(List, List)}.
 */
public class TestModelsEditorMerge extends TestCase {

	/** Discovered models are appended when there are no existing entries. */
	public void testEmptyExisting() {
		List<String> result = ModelsEditor.merge(List.of(), List.of("a", "b", "c"));
		assertEquals(List.of("a", "b", "c"), result);
	}

	/** A {@code null} existing list is treated as empty. */
	public void testNullExisting() {
		List<String> result = ModelsEditor.merge(null, List.of("a", "b"));
		assertEquals(List.of("a", "b"), result);
	}

	/** Only the new discovered entries are appended; order is preserved. */
	public void testOverlapping() {
		List<String> result = ModelsEditor.merge(List.of("a", "b"), List.of("b", "c", "a", "d"));
		assertEquals(List.of("a", "b", "c", "d"), result);
	}

	/** All discovered entries already present yields no additions. */
	public void testAllDuplicate() {
		List<String> existing = List.of("a", "b", "c");
		List<String> result = ModelsEditor.merge(existing, List.of("c", "b", "a"));
		assertEquals(existing, result);
	}

	/** Existing entries are kept first, all discovered entries appended in order. */
	public void testAllNew() {
		List<String> result = ModelsEditor.merge(List.of("x", "y"), List.of("a", "b"));
		assertEquals(List.of("x", "y", "a", "b"), result);
	}

	/** Blank and {@code null} discovered entries are ignored. */
	public void testBlankDiscoveredIgnored() {
		List<String> result = ModelsEditor.merge(List.of("a"), java.util.Arrays.asList("", "  ", null, "b"));
		assertEquals(List.of("a", "b"), result);
	}

	/** Duplicate existing entries are de-duplicated while preserving first occurrence. */
	public void testDuplicateExistingDeduped() {
		List<String> result = ModelsEditor.merge(List.of("a", "a", "b"), List.of("b", "c"));
		assertEquals(List.of("a", "b", "c"), result);
	}

}
