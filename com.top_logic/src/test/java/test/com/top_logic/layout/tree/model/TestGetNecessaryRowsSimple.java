/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static java.util.Collections.*;

import java.util.Arrays;

import junit.framework.Test;

import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * Tests for {@link TreeTable#getNecessaryRows(Object)} with neither
 * {@link TreeTable#isFilterIncludeParents()} nor {@link TreeTable#isFilterIncludeChildren()}
 * active.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestGetNecessaryRowsSimple extends TreeTableGetNecessaryRowsTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getTable().setFilterDisplayParents(false);
		getTable().setFilterDisplayChildren(false);
	}

	public void testVisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		assertNecessaryRows(getRoot(), Arrays.asList(getRoot()));
	}

	public void testInvisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		assertNecessaryRows(getRoot(), emptyList());
	}

	public void testRootChild_RootVisible() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().getChildAt(0);
		assertNecessaryRows(child, Arrays.asList(getRoot(), child));
	}

	public void testRootChild_RootInvisible() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		DefaultTreeTableNode child = getRoot().getChildAt(0);
		assertNecessaryRows(child, Arrays.asList(child));
	}

	public void testRootGrandchild_RootVisible() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		assertNecessaryRows(grandchild, Arrays.asList(getRoot(), child, grandchild));
	}

	public void testRootGrandchild_RootInvisible() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		assertNecessaryRows(grandchild, Arrays.asList(child, grandchild));
	}

	public void testVisibleParent() {
		DefaultTreeTableNode child = getRoot().getChildAt(2);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		setFilter(filter(
			getRoot(),
			child));
		assertNecessaryRows(grandchild, Arrays.asList(grandchild));
	}

	public void testVisibleGrandParent() {
		DefaultTreeTableNode child = getRoot().getChildAt(3);
		DefaultTreeTableNode grandchild = child.getChildAt(1);
		DefaultTreeTableNode grandgrandchild = grandchild.getChildAt(0);
		setFilter(filter(
			getRoot(),
			child));
		assertNecessaryRows(grandgrandchild, Arrays.asList(grandchild, grandgrandchild));
	}

	// Tests using synthetic nodes

	public void testSyntheticVisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		markSynthetic(getRoot());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);

		assertNecessaryRows(getRoot(), Arrays.asList(getRoot().getChildAt(0)));
		assertNecessaryRows(child, Arrays.asList(child));
		assertNecessaryRows(grandchild, Arrays.asList(child, grandchild));
	}

	/**
	 * This case is not very important, the main point is that no exception is thrown.
	 */
	public void testSyntheticInvisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		markSynthetic(getRoot());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);

		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, Arrays.asList(child));
		assertNecessaryRows(grandchild, Arrays.asList(child, grandchild));
	}

	public void testSyntheticRootChild_RootVisible() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		markSynthetic(child);

		assertNecessaryRows(child, Arrays.asList(getRoot(), grandchild));
		assertNecessaryRows(grandchild, Arrays.asList(getRoot(), grandchild));
	}

	public void testSyntheticNode() {
		setFilter(FilterFactory.falseFilter());

		DefaultTreeTableNode node = getRoot().getChildAt(4).getChildAt(3);
		markSynthetic(node);
		assertNecessaryRows(node, Arrays.asList(node.getChildAt(0), node.getParent(), getRoot()));
		markSynthetic(node.getParent());
		assertNecessaryRows(node, Arrays.asList(node.getChildAt(0), getRoot()));
		markSynthetic(node.getChildAt(0));
		assertNecessaryRows(node, Arrays.asList(node.getChildAt(1), getRoot()));
		markSynthetic(node.getChildAt(1));
		markSynthetic(node.getChildAt(2));
		assert node.getChildCount() == 3;
		assertNecessaryRows(node, Arrays.asList());
	}

	public void testOnlySyntheticAncestors() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().createChild(-1);
		DefaultTreeTableNode grandchild = child.createChild(-1);
		DefaultTreeTableNode grandgrandchild = grandchild.createChild(-1);

		markSynthetic(getRoot());
		assertNecessaryRows(getRoot(), Arrays.asList(child));
		assertNecessaryRows(child, Arrays.asList(child));

		markSynthetic(child);
		assertNecessaryRows(child, Arrays.asList(grandchild));
		assertNecessaryRows(grandchild, Arrays.asList(grandchild));

		markSynthetic(grandchild);
		assertNecessaryRows(grandchild, Arrays.asList(grandgrandchild));
		assertNecessaryRows(grandgrandchild, Arrays.asList(grandgrandchild));
	}

	public static Test suite() {
		return suite(TestGetNecessaryRowsSimple.class);
	}
}
