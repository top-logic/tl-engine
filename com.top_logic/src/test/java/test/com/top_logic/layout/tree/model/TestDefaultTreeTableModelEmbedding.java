/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.table.WrappingTableRowFilter;

import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Filter;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.AbstractTLTreeNode;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.AbstractTreeTableNode;
import com.top_logic.layout.tree.model.AbstractTreeUINodeModel.TreeUINode;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * Test case for the table embedding aspect of {@link DefaultTreeTableModel}.
 * 
 * @see TestDefaultTreeTableModel
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultTreeTableModelEmbedding extends AbstractTreeTableTest {

	private class OddUserObjectFilter<N extends AbstractTLTreeNode<N>> implements Filter<N> {
		@Override
		public boolean accept(N anObject) {
			return ((Integer) anObject.getBusinessObject()) % 2 == 1;
		}
	}

	private class EvenUserObjectFilter<N extends AbstractTLTreeNode<N>> implements Filter<N> {
		@Override
		public boolean accept(N anObject) {
			return ((Integer) anObject.getBusinessObject()) % 2 == 0;
		}
	}

	private final class InverseUserObjectComparator<N extends AbstractTLTreeNode<N>> implements Comparator<N> {
		@Override
		public int compare(N o1, N o2) {
			return -((Comparable) o1.getBusinessObject()).compareTo(o2.getBusinessObject());
		}
	}

	public void testEmptyModel() {
		assertEmbedding();
	}

	public void testRootOnlyModel() {
		_model.setRootVisible(true);
		assertEmbedding();
		assertEquals(1, getTable().getRowCount());
		_model.setRootVisible(false);
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());
	}

	public void testChangingRootVisibility() {
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());
		assertEmbedding();
		_model.setRootVisible(true);
		assertEmbedding();
		_model.setRootVisible(false);
		assertEmbedding();
	}

	public void testInitiallyExpandedModel() {
		getRoot().setExpanded(true);
		assertEmbedding();
		getRoot().setExpanded(false);
		assertEmbedding();
	}

	public void testExpandingRoot() {
		assertEmbedding();
		getRoot().setExpanded(true);
		assertEmbedding();
		getRoot().setExpanded(false);
		assertEmbedding();
	}

	public void testExpandingInvisible() {
		_model.setRootVisible(true);
		getRoot().getChildAt(4).getChildAt(3).setExpanded(true);
		getRoot().getChildAt(4).setExpanded(true);
		assertEmbedding();
		assertEquals("Root is not yet expanded.", 1, getTable().getRowCount());
		getRoot().setExpanded(true);
		assertEmbedding();
		assertEquals(1 + 5 + 4 + 3, getTable().getRowCount());
		assertEquals(1 + 5 + 4 + 3, getTable().getRowCount());

		// Re-expand.
		_model.setRootVisible(true);
		getRoot().setExpanded(true);
		getRoot().getChildAt(4).setExpanded(true);
		getRoot().getChildAt(4).getChildAt(3).setExpanded(true);
		assertEmbedding();
	}

	public void testExpandingVisible() {
		_model.setRootVisible(true);
		assertEmbedding();
		getRoot().setExpanded(true);
		getRoot().getChildAt(4).setExpanded(true);
		getRoot().getChildAt(4).getChildAt(3).setExpanded(true);
		assertEmbedding();

		assertEquals(1 + 5 + 4 + 3, getTable().getRowCount());
	}

	public void testExpandingEmpty() {
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());
		assertEmbedding();
		getRoot().getChildAt(0).setExpanded(true);
		assertEmbedding();

		assertEquals("Expanded empty node.", 5, getTable().getRowCount());
	}

	public void testAddStartCompat() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.before(0), 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(5, 0, 1, 2, 3, 4);
	}

	public void testAddEndCompat() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.before(5), 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(0, 1, 2, 3, 4, 5);
	}

	public void testAddStart() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.START, 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(5, 0, 1, 2, 3, 4);
	}

	public void testAddBefore() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.before(2), 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(0, 1, 5, 2, 3, 4);
	}

	public void testAddAfter() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.after(2), 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(0, 1, 2, 5, 3, 4);
	}

	public void testAddEnd() {
		assertEmbedding();
		assertEquals("An invisible root is always expanded.", 5, getTable().getRowCount());

		enableUpdates();
		getRoot().createChild(IndexPosition.END, 5);
		disableUpdates();
		assertEquals(6, getTable().getRowCount());
		assertNodes(0, 1, 2, 3, 4, 5);
	}

	public void testAddExpanded() {
		DefaultTreeTableNode child = getRoot().createChild(IndexPosition.before(5), 5);
		getRoot().removeChild(5);

		child.setExpanded(true);
		child.getChildAt(4).setExpanded(true);
		child.getChildAt(4).getChildAt(3).setExpanded(true);

		assertEmbedding();
		assertEquals(5, getTable().getRowCount());

		enableUpdates();
		getRoot().addChild(IndexPosition.START, child);
		disableUpdates();
		assertEmbedding();

		assertEquals(6 + 5 + 4 + 3, getTable().getRowCount());
	}

	public void testCollapseExpanded() {
		_model.setRootVisible(true);
		getRoot().setExpanded(true);
		getRoot().getChildAt(3).setExpanded(true);
		assertEmbedding();
		getRoot().getChildAt(3).setExpanded(false);
		assertEmbedding();
	}

	public void testCollapseAdded() {
		getRoot().setExpanded(true);
		getRoot().getChildAt(3).setExpanded(true);
		assertEmbedding();
		enableUpdates();
		getRoot().getChildAt(3).createChild(IndexPosition.END, 7);
		disableUpdates();
		assertEmbedding();
		getRoot().getChildAt(3).setExpanded(false);
		assertEmbedding();
	}

	public void testCollapseAfterRemove() {
		getRoot().setExpanded(true);
		getRoot().getChildAt(3).setExpanded(true);
		assertEmbedding();
		enableUpdates();
		getRoot().getChildAt(3).removeChild(0);
		disableUpdates();
		assertEmbedding();
		getRoot().getChildAt(3).setExpanded(false);
		assertEmbedding();
	}

	public void testRemoveUninitialized() {
		getRoot().removeChild(0);
		assertEmbedding();
		assertEquals(4, getTable().getRowCount());
	}

	public void testRemove() {
		assertEmbedding();

		enableUpdates();
		getRoot().removeChild(0);
		disableUpdates();
		assertEmbedding();
		assertEquals(4, getTable().getRowCount());
	}

	public void testRemoveExpanded() {
		getRoot().getChildAt(4).setExpanded(true);
		getRoot().getChildAt(4).getChildAt(3).setExpanded(true);

		assertEmbedding();
		assertEquals(5 + 4 + 3, getTable().getRowCount());

		enableUpdates();
		getRoot().removeChild(4);
		disableUpdates();
		assertEmbedding();

		assertEquals(4, getTable().getRowCount());
	}

	public void testSorting() {
		_model.setRootVisible(true);
		getTable().setOrder(new InverseUserObjectComparator<>());
		assertEmbedding();

		getRoot().setExpanded(true);
		assertNodes(5, 4, 3, 2, 1, 0);

		getRoot().setExpanded(false);
		assertNodes(5);

		_model.setRootVisible(false);
		assertNodes(4, 3, 2, 1, 0);
	}

	public void testFilter() {
		_model.setRootVisible(true);
		setFilter(new OddUserObjectFilter<>(), getTable().getOrder());
		assertEmbedding();

		getRoot().setExpanded(true);
		assertNodes(5, 1, 3);

		getRoot().getChildAt(3).setExpanded(true);
		assertNodes(5, 1, 3, 1);

		getRoot().setExpanded(false);
		assertNodes(5);
	}

	public void testFilterRootHidden() {
		_model.setRootVisible(true);
		setFilter(new EvenUserObjectFilter<>(),
			getTable().getOrder());
		assertEquals("Visible root is filtered.", 0, getTable().getRowCount());

		getRoot().setExpanded(true);
		assertEquals("Visible root is filtered.", 0, getTable().getRowCount());

		getRoot().setExpanded(false);
		assertEquals(0, getTable().getRowCount());

		assertEquals(set(5, 4, 3, 2, 1, 0), userObjects(getTable().getAllRows()));
	}

	public void testFilterRootExcludedHidden() {
		_model.setRootVisible(false);
		setFilter(new EvenUserObjectFilter<>(),
			new InverseUserObjectComparator<>());

		assertNodes(4, 2, 0);

		getRoot().getChildAt(4).setExpanded(true);
		assertNodes(4, 2, 0, 2, 0);

		getRoot().getChildAt(4).getChildAt(2).setExpanded(true);
		assertNodes(4, 2, 0, 0, 2, 0);

		assertEquals(5 + 4 + 2, getTable().getAllRows().size());
		assertEquals(3 + 2 + 1, getTable().getDisplayedRows().size());

		getRoot().getChildAt(2).setExpanded(true);
		assertNodes(4, 2, 0, 0, 2, 0, 0);

		getRoot().getChildAt(2).setExpanded(false);
		assertNodes(4, 2, 0, 0, 2, 0);

		getRoot().getChildAt(4).setExpanded(false);
		assertNodes(4, 2, 0);

		_model.setRootVisible(true);
		assertNodes();

		getRoot().setExpanded(false);
		assertNodes();

		_model.setRootVisible(false);
		assertNodes(4, 2, 0);
	}


	public void testFilterSyntheticRootNode() {
		TreeBuilder<DefaultTreeTableNode> builder = new DefaultTreeTableBuilder();
		_model = new DefaultTreeTableModel(builder, "R", list("A", "B", "C"), TableConfiguration.table());
		_model.setRootVisible(false);

		DefaultTreeTableNode root = getRoot();

		AbstractTreeTableModel.markSynthetic(root);

		/* Collapsed synthetic node with regular child, that does match filter. */
		DefaultTreeTableNode sCollapsed = syntheticChild(root, "sInvisible");
		regularChild(sCollapsed, "r1");

		TreeTableModel table = getTable();
		assertEquals(list(sCollapsed), table.getDisplayedRows());

		setFilter(new Filter<DefaultTreeTableNode>() {
			@Override
			public boolean accept(DefaultTreeTableNode anObject) {
				return ((String) anObject.getBusinessObject()).startsWith("r");
			}
		}, Equality.INSTANCE);

		assertEquals(list(sCollapsed), table.getDisplayedRows());
	}

	public void testFilterSyntheticNodeNotMatchingFilterWithRegularChild() {
		TreeBuilder<DefaultTreeTableNode> builder = new DefaultTreeTableBuilder();
		_model = new DefaultTreeTableModel(builder, "R", list("A", "B", "C"), TableConfiguration.table());
		_model.setRootVisible(false);

		DefaultTreeTableNode root = getRoot();

		// Collapsed synthetic node with regular child matching filter
		DefaultTreeTableNode sCollapsed = syntheticChild(root, "sCollapsed");
		regularChild(sCollapsed, "r1");

		TreeTableModel table = getTable();
		assertEquals(list(sCollapsed), table.getDisplayedRows());

		setFilter(new Filter<DefaultTreeTableNode>() {
			@Override
			public boolean accept(DefaultTreeTableNode anObject) {
				return !((String) anObject.getBusinessObject()).startsWith("s");
			}
		}, Equality.INSTANCE);

		assertEquals(list(sCollapsed), table.getDisplayedRows());
	}

	public void testFilterSyntheticNodeNotMatchingFilterWithRegularDescendent() {
		TreeBuilder<DefaultTreeTableNode> builder = new DefaultTreeTableBuilder();
		_model = new DefaultTreeTableModel(builder, "R", list("A", "B", "C"), TableConfiguration.table());
		_model.setRootVisible(false);

		DefaultTreeTableNode root = getRoot();

		/* Collapsed synthetic node with synthetic child, that matches filter, and regular child,
		 * that does not match filter. */
		DefaultTreeTableNode sCollapsed = syntheticChild(root, "sCollapsed");
		DefaultTreeTableNode sInvisible = syntheticChild(sCollapsed, "sInvisible");
		regularChild(sInvisible, "r1");

		TreeTableModel table = getTable();
		assertEquals(list(sCollapsed), table.getDisplayedRows());

		setFilter(new Filter<DefaultTreeTableNode>() {
			@Override
			public boolean accept(DefaultTreeTableNode anObject) {
				return !((String) anObject.getBusinessObject()).startsWith("s");
			}
		}, Equality.INSTANCE);

		assertEquals("Ticket #12676: ", list(sCollapsed), table.getDisplayedRows());
	}

	public void testFilterSyntheticNodes() {
		TreeBuilder<DefaultTreeTableNode> builder = new DefaultTreeTableBuilder();
		_model = new DefaultTreeTableModel(builder, "R", list("A", "B", "C"), TableConfiguration.table());
		_model.setRootVisible(false);

		DefaultTreeTableNode root = getRoot();

		// Top-level regular child without any descendants.
		DefaultTreeTableNode r1 = regularChild(root, "r1");

		// Top-level filtered child without any descendants.
		DefaultTreeTableNode f1 = regularChild(root, "f1");

		// Synthetic child without any descendants.
		DefaultTreeTableNode sInvisible = syntheticChild(root, "sInvisible");

		// Synthetic child without a single filtered child.
		DefaultTreeTableNode sFiltered = syntheticChild(root, "sFiltered");
		regularChild(sFiltered, "f");

		// Synthetic child with only synthetic descendants.
		DefaultTreeTableNode sOnlySyntheticChildren = syntheticChild(root, "sOnlySyntheticChildren");
		syntheticChild(sOnlySyntheticChildren, "s");
		syntheticChild(sOnlySyntheticChildren, "s");

		// Expanded synthetic child with only synthetic descendants.
		DefaultTreeTableNode sOnlySyntheticChildrenExpanded =
			expandedSyntheticChild(root, "sOnlySyntheticChildrenExpanded");
		expandedSyntheticChild(sOnlySyntheticChildrenExpanded, "s");
		syntheticChild(sOnlySyntheticChildrenExpanded, "s");

		// Synthetic child with synthetic child that itself has a regular child.
		DefaultTreeTableNode sRegularDescendant = syntheticChild(root, "sRegularDescendant");
		regularChild(syntheticChild(sRegularDescendant, "s"), "r");

		// Synthetic child with synthetic child that itself has a filtered child.
		DefaultTreeTableNode sFilteredDescendant = syntheticChild(root, "sFilteredDescendant");
		regularChild(syntheticChild(sFilteredDescendant, "s"), "f");

		// Expanded synthetic child with synthetic child that itself has a filtered child.
		DefaultTreeTableNode sFilteredDescendantExpanded = expandedSyntheticChild(root, "sFilteredDescendantExpanded");
		DefaultTreeTableNode sFilteredDescendantExpandedChild =
			syntheticChild(sFilteredDescendantExpanded, "sFilteredDescendantExpandedChild");
		regularChild(sFilteredDescendantExpandedChild, "f");

		// Expanded synthetic child with synthetic child that itself has a filtered child.
		DefaultTreeTableNode rExpanded = expandedRegularChild(root, "rExpanded");
		DefaultTreeTableNode sExpandedChild = syntheticChild(rExpanded, "sExpandedChild");
		regularChild(sExpandedChild, "f");

		assertNotNull(sInvisible);
		TreeTableModel table = getTable();

		assertEquals(
			list(r1, f1, sFiltered, sRegularDescendant, sFilteredDescendant,
				sFilteredDescendantExpanded, sFilteredDescendantExpandedChild,
				rExpanded, sExpandedChild),
			table.getDisplayedRows());

		setFilter(new Filter<DefaultTreeTableNode>() {
			@Override
			public boolean accept(DefaultTreeTableNode anObject) {
				return !((String) anObject.getBusinessObject()).startsWith("f");
			}
		}, Equality.INSTANCE);

		assertEquals(list(r1, sRegularDescendant, rExpanded), table.getDisplayedRows());
	}

	private DefaultTreeTableNode expandedRegularChild(DefaultTreeTableNode child, String name) {
		DefaultTreeTableNode result = regularChild(child, name);
		result.setExpanded(true);
		return result;
	}

	private DefaultTreeTableNode regularChild(DefaultTreeTableNode child, String name) {
		DefaultTreeTableNode child2 = child.createChild(name);
		return child2;
	}

	private DefaultTreeTableNode expandedSyntheticChild(DefaultTreeTableNode node, String name) {
		DefaultTreeTableNode result = syntheticChild(node, name);
		result.setExpanded(true);
		return result;
	}

	private DefaultTreeTableNode syntheticChild(DefaultTreeTableNode node, String name) {
		DefaultTreeTableNode child = node.createChild(name);
		AbstractTreeTableModel.markSynthetic(child);
		return child;
	}

	private <N extends TreeUINode<N>> Set<Object> userObjects(Collection<N> nodes) {
		HashSet<Object> result = new HashSet<>();
		for (N node : nodes) {
			result.add(node.getBusinessObject());
		}
		return result;
	}

	private void assertNodes(int... userObjects) {
		TableModel table = getTable();
		for (int n = 0, cnt = userObjects.length; n < cnt; n++) {
			Object businessObject =
				((AbstractMutableTLTreeNode<?>) table.getRowObject(n)).getBusinessObject();
			assertEquals("Row " + n, userObjects[n], ((Integer) businessObject).intValue());
		}
		assertEquals(userObjects.length, table.getRowCount());
		assertSubtreeSize();
	}

	public void assertEmbedding() {
		int row = 0;
		TableModel table = getTable();
		if (_model.isRootVisible()) {
			assertSame(getRoot(), table.getRowObject(row++));
		}
		
		row = checkChildrenEmbedding(table, row, getRoot());

		assertEquals(row, table.getRowCount());
		assertSubtreeSize();
	}

	public void assertSubtreeSize() {
		if (_model.isRootVisible()) {
			/* can not inline parent as javac can not determine correct type */
			DefaultTreeTableModel.DefaultTreeTableNode parent = null;
			assertSubtreeSize(parent, 0, getTable().getRowCount());
		} else {
			assertSubtreeSize(getRoot(), 0, getTable().getRowCount());
		}
	}

	private <N extends AbstractTreeTableNode<N>> void assertSubtreeSize(N parent, int row, int limit) {
		while (row < limit) {
			N node = (N) getTable().getRowObject(row);
			int nextSiblingRow = row + node.getVisibleSubtreeSize();
			assert nextSiblingRow <= limit;
			assert node.getParent() == parent : "Parent missmatch.";
			assertSubtreeSize(node, row + 1, nextSiblingRow);
			row = nextSiblingRow;
		}
	}

	private <N extends AbstractTreeTableNode<N>> int checkChildrenEmbedding(TableModel table, int row, N parent) {
		if (parent.isExpanded()) {
			for (N child : parent.getChildren()) {
				assertSame(child, table.getRowObject(row++));

				row = checkChildrenEmbedding(table, row, child);
			}
		}

		return row;
	}

	private void setFilter(Filter<?> wrappedFilter, Comparator<?> order) {
		getTable().setFilter(new WrappingTableRowFilter(wrappedFilter),
			order);
	}

	public static Test suite() {
		return suite(TestDefaultTreeTableModelEmbedding.class);
	}

}
