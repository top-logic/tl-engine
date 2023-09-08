/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import test.com.top_logic.layout.AbstractLayoutTest;
import test.com.top_logic.layout.table.WrappingTableRowFilter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.TreeView;
import com.top_logic.layout.table.TableRowFilter;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TreeTableModel;

/**
 * Common base class for {@link TestCase}s of the {@link TreeTable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class AbstractTreeTableTest extends AbstractLayoutTest {

	protected DefaultTreeTableModel _model;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		DefaultTreeTableBuilder builder = new DefaultTreeTableBuilder() {
			@Override
			public List<DefaultTreeTableNode> createChildList(DefaultTreeTableNode node) {
				int childCount = (Integer) node.getBusinessObject();
				if (childCount < 0) {
					childCount = 0;
				}

				ArrayList<DefaultTreeTableNode> children = new ArrayList<>(childCount);
				for (int n = 0; n < childCount; n++) {
					children.add(createNode(node.getModel(), node, n));
				}

				return children;
			}
		};

		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.setFilterDisplayChildren(false);
		tableConfig.setFilterDisplayParents(false);
		_model = new DefaultTreeTableModel(builder, 5, list("A", "B", "C"), tableConfig);
	}

	@Override
	protected void tearDown() throws Exception {
		_model = null;

		super.tearDown();
	}

	/**
	 * @see TreeTable#findNearestDisplayedRow(Object)
	 */
	protected int findNearestDisplayedRow(Object rowObject) {
		return getTable().findNearestDisplayedRow(rowObject);
	}

	/**
	 * @see TreeTable#getRowOfObject(Object)
	 */
	protected int getRowOfObject(Object rowObject) {
		return getTable().getRowOfObject(rowObject);
	}

	/**
	 * Expand all nodes, recursively.
	 * <p>
	 * If the tree is not {@link TreeView#isFinite() finite}, an exception will be thrown.
	 * </p>
	 */
	protected void expandAll(DefaultTreeTableNode node) {
		if (!getTree().isFinite()) {
			throw new RuntimeException("Expanding a tree that is potentially infinite.");
		}
		getTree().setExpanded(node, true);
		for (DefaultTreeTableNode child : node.getChildren()) {
			expandAll(child);
		}
	}

	/**
	 * @see TreeTable#setFilter(TableRowFilter, Comparator)
	 */
	protected void setFilter(Filter<?> filter) {
		WrappingTableRowFilter tableRowFilter = new WrappingTableRowFilter(filter);
		Comparator<?> comparator = getTable().getOrder();

		getTable().setFilter(tableRowFilter, comparator);
	}

	/**
	 * @see TreeTable#setOrder(Comparator)
	 */
	protected void setOrder(Comparator<?> comparator) {
		getTable().setOrder(comparator);
	}

	/**
	 * @see DefaultTreeTableModel#getRoot()
	 */
	protected DefaultTreeTableNode getRoot() {
		return getTree().getRoot();
	}

	/**
	 * Getter for the {@link TreeTableModel}.
	 * <p>
	 * It is created and setup for each test.
	 * </p>
	 */
	protected DefaultTreeTableModel.TreeTable getTable() {
		return (DefaultTreeTableModel.TreeTable) _model.getTable();
	}

	/**
	 * Getter for the {@link DefaultTreeTableModel}.
	 * <p>
	 * It is created and setup for each test.
	 * </p>
	 */
	protected DefaultTreeTableModel getTree() {
		return _model;
	}

}
