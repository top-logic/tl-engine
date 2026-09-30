/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.slot;

import java.util.ArrayList;
import java.util.List;

/**
 * Position of a {@link SlotElement} or {@link SlotContentElement} in the rendered view tree.
 *
 * <p>
 * Each container that supports slot routing extends the path by one segment when creating each of
 * its children's controls. Two elements with paths {@code 0/1} and {@code 0/2/3} have the lowest
 * common ancestor at path {@code 0} (common prefix length 1).
 * </p>
 */
public final class SlotPath {

	/** The empty path, used for the root context. */
	public static final SlotPath ROOT = new SlotPath(List.of());

	private final List<String> _segments;

	private SlotPath(List<String> segments) {
		_segments = segments;
	}

	/**
	 * Returns a path with the given segment appended.
	 */
	public SlotPath append(String segment) {
		List<String> next = new ArrayList<>(_segments.size() + 1);
		next.addAll(_segments);
		next.add(segment);
		return new SlotPath(List.copyOf(next));
	}

	/**
	 * Number of segments in this path.
	 */
	public int depth() {
		return _segments.size();
	}

	/**
	 * Length of the longest common prefix between two paths.
	 */
	public static int commonPrefixLength(SlotPath a, SlotPath b) {
		int n = Math.min(a._segments.size(), b._segments.size());
		int i = 0;
		while (i < n && a._segments.get(i).equals(b._segments.get(i))) {
			i++;
		}
		return i;
	}

	/**
	 * Tree distance between two elements: sum of depths from their lowest common ancestor.
	 */
	public static int distance(SlotPath a, SlotPath b) {
		int lca = commonPrefixLength(a, b);
		return (a.depth() - lca) + (b.depth() - lca);
	}

	@Override
	public String toString() {
		return _segments.isEmpty() ? "/" : "/" + String.join("/", _segments);
	}
}
