/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterInput;
import com.top_logic.table.FilterSpec;
import com.top_logic.table.FilterState;
import com.top_logic.table.Row;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Test for {@link TreeRowSource}: expansion-based flattening, sibling sort and
 * ancestor-keeping filter, all through the flat {@link com.top_logic.table.RowSource} API.
 */
public class TestTreeRowSource extends TestCase {

	/** A simple mutable tree node with a name. */
	private static final class Node {
		final String _name;

		final List<Node> _children = new ArrayList<>();

		Node(String name, Node... children) {
			_name = name;
			for (Node child : children) {
				_children.add(child);
			}
		}
	}

	private record Contains(String needle) implements FilterState {
		@Override
		public boolean isEmpty() {
			return needle.isEmpty();
		}
	}

	private final Node _root1 =
		new Node("a", new Node("a2"), new Node("a1", new Node("a1x")));

	private final Node _root2 = new Node("b");

	private final TreeStructure<Node, Node> _structure = new TreeStructure<>() {
		@Override
		public List<Node> roots() {
			return List.of(_root1, _root2);
		}

		@Override
		public List<Node> children(Node node) {
			return node._children;
		}

		@Override
		public boolean isLeaf(Node node) {
			return node._children.isEmpty();
		}

		@Override
		public Node businessObject(Node node) {
			return node;
		}
	};

	private final Column<Node, String> _name =
		DefaultColumn.<Node, String> builder("name", node -> node._name)
			.sort(() -> Comparator.naturalOrder())
			.filter(new ColumnFilter<>() {
				@Override
				public FilterInput input() {
					return new FilterInput.Text();
				}

				@Override
				public Predicate<String> predicate(FilterState state) {
					return value -> value != null && value.contains(((Contains) state).needle());
				}
			})
			.build();

	private List<Column<Node, ?>> columns() {
		return List.of(_name);
	}

	private List<String> names(TreeRowSource<Node, Node> source) {
		List<Row<Node>> rows = source.window(0, source.size());
		return rows.stream().map(r -> r.data()._name).toList();
	}

	public void testRootsOnlyWhenCollapsed() {
		TreeRowSource<Node, Node> source = new TreeRowSource<>(_structure, columns());
		assertEquals(List.of("a", "b"), names(source));
		Row<Node> a = source.window(0, 1).get(0);
		assertTrue(a.expandable());
		assertFalse(a.expanded());
		assertEquals(0, a.depth());
	}

	public void testExpandRevealsChildren() {
		TreeRowSource<Node, Node> source = new TreeRowSource<>(_structure, columns());
		source.setExpanded(_root1, true);
		assertEquals(List.of("a", "a2", "a1", "b"), names(source));

		List<Row<Node>> rows = source.window(0, source.size());
		assertEquals(1, rows.get(1).depth());
		assertTrue(rows.get(0).expanded());
		// "a1" is expandable (has child a1x) but not yet expanded.
		Row<Node> a1 = rows.get(2);
		assertTrue(a1.expandable());
		assertFalse(a1.expanded());
	}

	public void testSiblingSort() {
		TreeRowSource<Node, Node> source = new TreeRowSource<>(_structure, columns());
		source.setExpanded(_root1, true);
		source.withOrder(SortSpec.ascending("name"));
		// Children of "a" sorted: a1 before a2.
		assertEquals(List.of("a", "a1", "a2", "b"), names(source));
	}

	public void testFilterKeepsAncestorsAndRevealsMatches() {
		TreeRowSource<Node, Node> source = new TreeRowSource<>(_structure, columns());
		// No node is expanded, but filtering must reveal the deep match "a1x".
		source.withFilter(new FilterSpec(Map.of("name", new Contains("a1x"))));
		// Path a -> a1 -> a1x is shown; a2 and b are dropped.
		assertEquals(List.of("a", "a1", "a1x"), names(source));
	}

	public void testClearingFilterRestoresCollapsedView() {
		TreeRowSource<Node, Node> source = new TreeRowSource<>(_structure, columns());
		source.withFilter(new FilterSpec(Map.of("name", new Contains("a1x"))));
		source.withFilter(FilterSpec.NONE);
		assertEquals(List.of("a", "b"), names(source));
	}

}
