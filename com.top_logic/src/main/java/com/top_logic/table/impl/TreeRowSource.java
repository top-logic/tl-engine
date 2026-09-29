/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowSource;
import com.top_logic.table.RowSourceListener;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TreeStructure;

/**
 * {@link RowSource} that flattens the expanded, filtered subtree of a
 * {@link TreeStructure} into the windowed row sequence, so that flat and tree tables share
 * one {@link RowSource} API.
 *
 * <p>
 * Behaviour:
 * </p>
 * <ul>
 * <li>Children are revealed only for {@link #setExpanded expanded} nodes (lazily loaded
 * via {@link TreeStructure#children}).</li>
 * <li>Sorting orders siblings by the columns' comparators.</li>
 * <li>Filtering keeps a node when it matches or has a matching descendant (ancestors of
 * matches stay visible); while a filter is active all nodes are treated as expanded so
 * deep matches are revealed. Descendant matching is only applied for
 * {@link TreeStructure#isFinite() finite} trees.</li>
 * </ul>
 *
 * <p>
 * Grouping over a tree is not supported.
 * </p>
 *
 * @param <N>
 *        The tree node type.
 * @param <R>
 *        The business object type exposed as row data.
 */
public class TreeRowSource<N, R> implements RowSource<R> {

	private final TreeStructure<N, R> _structure;

	private final Map<String, Column<R, ?>> _byName = new LinkedHashMap<>();

	private final List<RowSourceListener> _listeners = new ArrayList<>();

	private final Set<Object> _expanded = new HashSet<>();

	private SortSpec _sort = SortSpec.NONE;

	private FilterSpec _filter = FilterSpec.NONE;

	private List<Row<R>> _displayed;

	/**
	 * Creates a {@link TreeRowSource}.
	 *
	 * @param structure
	 *        The tree structure.
	 * @param columns
	 *        The column definitions used for sorting and filtering.
	 */
	public TreeRowSource(TreeStructure<N, R> structure, List<Column<R, ?>> columns) {
		_structure = structure;
		for (Column<R, ?> column : columns) {
			_byName.put(column.name(), column);
		}
		recompute();
	}

	@Override
	public int size() {
		return _displayed.size();
	}

	@Override
	public List<Row<R>> window(int from, int to) {
		int lo = Math.max(0, from);
		int hi = Math.min(_displayed.size(), to);
		if (lo >= hi) {
			return List.of();
		}
		return List.copyOf(_displayed.subList(lo, hi));
	}

	@Override
	public RowSource<R> withOrder(SortSpec sort) {
		_sort = sort;
		recompute();
		fireInvalidated();
		return this;
	}

	@Override
	public RowSource<R> withFilter(FilterSpec filter) {
		_filter = filter;
		recompute();
		fireInvalidated();
		return this;
	}

	@Override
	public RowSource<R> withGrouping(GroupSpec grouping) {
		if (!grouping.columns().isEmpty()) {
			throw new UnsupportedOperationException("Grouping over a tree is not supported.");
		}
		return this;
	}

	@Override
	public void setExpanded(Object rowKey, boolean expanded) {
		boolean changed = expanded ? _expanded.add(rowKey) : _expanded.remove(rowKey);
		if (changed) {
			recompute();
			fireInvalidated();
		}
	}

	@Override
	public void addListener(RowSourceListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removeListener(RowSourceListener listener) {
		_listeners.remove(listener);
	}

	private void recompute() {
		Predicate<R> predicate = ColumnLogic.predicate(_filter, _byName);
		Comparator<R> order = ColumnLogic.comparator(_sort, _byName);
		Comparator<N> nodeOrder = order == null ? null : Comparator.comparing(_structure::businessObject, order);
		boolean filterActive = predicate != null;

		List<Row<R>> displayed = new ArrayList<>();
		for (N root : sortedSiblings(_structure.roots(), nodeOrder)) {
			visit(root, 0, displayed, predicate, nodeOrder, filterActive);
		}
		_displayed = displayed;
	}

	private void visit(N node, int depth, List<Row<R>> out, Predicate<R> predicate, Comparator<N> nodeOrder,
			boolean filterActive) {
		if (predicate != null && !isVisible(node, predicate)) {
			return;
		}
		boolean leaf = _structure.isLeaf(node);
		boolean expanded = !leaf && (filterActive || _expanded.contains(node));
		out.add(new TreeRow<>(node, _structure.businessObject(node), depth, !leaf, expanded));
		if (expanded) {
			for (N child : sortedSiblings(_structure.children(node), nodeOrder)) {
				visit(child, depth + 1, out, predicate, nodeOrder, filterActive);
			}
		}
	}

	/**
	 * Whether a node should be shown under the active filter: it matches, or (for finite
	 * trees) has a matching descendant.
	 */
	private boolean isVisible(N node, Predicate<R> predicate) {
		if (predicate.test(_structure.businessObject(node))) {
			return true;
		}
		if (!_structure.isFinite() || _structure.isLeaf(node)) {
			return false;
		}
		for (N child : _structure.children(node)) {
			if (isVisible(child, predicate)) {
				return true;
			}
		}
		return false;
	}

	private List<N> sortedSiblings(List<N> nodes, Comparator<N> nodeOrder) {
		if (nodeOrder == null) {
			return nodes;
		}
		List<N> sorted = new ArrayList<>(nodes);
		sorted.sort(nodeOrder);
		return sorted;
	}

	private void fireInvalidated() {
		for (RowSourceListener listener : List.copyOf(_listeners)) {
			listener.rowsInvalidated(0, Integer.MAX_VALUE);
		}
	}

}
