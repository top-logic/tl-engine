/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.top_logic.table.Column;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowHierarchy;
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
 * The rows are keyed by {@link TreeStructure#key(Object)}, and the source is its own
 * {@link #hierarchy() hierarchy}: it answers which row holds a row, and which rows a row holds
 * whether it is expanded or not, in the order of the displayed siblings.
 * </p>
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
public class TreeRowSource<N, R> implements RowSource<R>, RowHierarchy<R> {

	private final TreeStructure<N, R> _structure;

	private final Map<String, Column<R, ?>> _byName = new LinkedHashMap<>();

	private final List<RowSourceListener> _listeners = new ArrayList<>();

	private final Set<Object> _expanded = new HashSet<>();

	private SortSpec _sort = SortSpec.NONE;

	private FilterSpec _filter = FilterSpec.NONE;

	private List<Row<R>> _displayed;

	/** The filter of the current {@link #_filter}, {@code null} when nothing is filtered. */
	private Predicate<R> _predicate;

	/** The sibling order of the current {@link #_sort}, {@code null} for the structure's order. */
	private Comparator<N> _nodeOrder;

	/**
	 * The nodes of the rows handed out since the last {@link #recompute()}, by {@link Row#key() row
	 * key}.
	 */
	private final Map<Object, N> _nodes = new HashMap<>();

	/**
	 * The node holding a node of {@link #_nodes} among its children, by the row key of the held
	 * node; absent for a root node.
	 */
	private final Map<Object, N> _parents = new HashMap<>();

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

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * The data rows are the nodes of the {@link TreeStructure}, whatever the filter and the collapsed
	 * nodes display of them. A {@link TreeStructure#isFinite() finite} tree is searched completely;
	 * of a tree that is not, only the nodes reachable through expanded nodes are searched, since
	 * its complete node set cannot be enumerated.
	 * </p>
	 */
	@Override
	public Set<Object> containedKeys(Collection<?> keys) {
		Set<Object> result = new LinkedHashSet<>();
		if (keys.isEmpty()) {
			return result;
		}
		Set<Object> missing = new HashSet<>(keys);
		boolean complete = _structure.isFinite();
		for (N root : _structure.roots()) {
			collectContained(root, missing, complete);
			if (missing.isEmpty()) {
				break;
			}
		}
		for (Object key : keys) {
			if (!missing.contains(key)) {
				result.add(key);
			}
		}
		return result;
	}

	/**
	 * Removes the given node and its searched descendants from the given missing keys.
	 *
	 * @param complete
	 *        Whether the descendants of collapsed nodes are searched, too.
	 */
	private void collectContained(N node, Set<Object> missing, boolean complete) {
		missing.remove(_structure.key(node));
		if (missing.isEmpty() || _structure.isLeaf(node) || !(complete || _expanded.contains(_structure.key(node)))) {
			return;
		}
		for (N child : _structure.children(node)) {
			collectContained(child, missing, complete);
			if (missing.isEmpty()) {
				return;
			}
		}
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

	/**
	 * Takes up a change of the {@link TreeStructure}: the displayed rows are computed again from
	 * the nodes it holds now.
	 *
	 * <p>
	 * A node whose {@link TreeStructure#key(Object) key} is still there keeps being expanded.
	 * </p>
	 */
	public void structureChanged() {
		recompute();
		fireInvalidated();
	}

	@Override
	public RowHierarchy<R> hierarchy() {
		return this;
	}

	@Override
	public List<Row<R>> roots() {
		return rows(null, _structure.roots(), 0);
	}

	@Override
	public Object rootParent() {
		return _structure.rootParent();
	}

	@Override
	public Row<R> parent(Row<R> row) {
		N parent = _parents.get(row.key());
		if (parent == null) {
			return null;
		}
		return row(_parents.get(_structure.key(parent)), parent, Math.max(0, row.depth() - 1));
	}

	@Override
	public List<Row<R>> children(Row<R> row) {
		N node = _nodes.get(row.key());
		if (node == null || _structure.isLeaf(node)) {
			return List.of();
		}
		return rows(node, _structure.children(node), row.depth() + 1);
	}

	/**
	 * The rows of the given siblings that the filter lets through, in display order.
	 *
	 * @param parent
	 *        The node holding the siblings, {@code null} for the roots.
	 */
	private List<Row<R>> rows(N parent, List<N> siblings, int depth) {
		List<Row<R>> result = new ArrayList<>(siblings.size());
		for (N node : sortedSiblings(siblings, _nodeOrder)) {
			if (_predicate != null && !isVisible(node, _predicate)) {
				continue;
			}
			result.add(row(parent, node, depth));
		}
		return result;
	}

	/**
	 * The row of the given node, remembered for the {@link #hierarchy() hierarchy}.
	 *
	 * @param parent
	 *        The node holding the given one, {@code null} for a root.
	 */
	private Row<R> row(N parent, N node, int depth) {
		Object key = _structure.key(node);
		_nodes.put(key, node);
		if (parent != null) {
			_parents.put(key, parent);
		}
		boolean leaf = _structure.isLeaf(node);
		boolean expanded = !leaf && (_predicate != null || _expanded.contains(key));
		return new TreeRow<>(key, _structure.businessObject(node), depth, !leaf, expanded);
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
		Comparator<R> order = ColumnLogic.comparator(_sort, _byName);
		_predicate = ColumnLogic.predicate(_filter, _byName);
		_nodeOrder = order == null ? null : Comparator.comparing(_structure::businessObject, order);
		_nodes.clear();
		_parents.clear();

		List<Row<R>> displayed = new ArrayList<>();
		for (N root : sortedSiblings(_structure.roots(), _nodeOrder)) {
			visit(null, root, 0, displayed);
		}
		_displayed = displayed;
	}

	private void visit(N parent, N node, int depth, List<Row<R>> out) {
		if (_predicate != null && !isVisible(node, _predicate)) {
			return;
		}
		Row<R> row = row(parent, node, depth);
		out.add(row);
		if (row.expanded()) {
			for (N child : sortedSiblings(_structure.children(node), _nodeOrder)) {
				visit(node, child, depth + 1, out);
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
