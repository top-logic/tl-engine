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
 * Tests for {@link TreeTable#getNecessaryRows(Object)} with both
 * {@link TreeTable#isFilterIncludeParents()} and {@link TreeTable#isFilterIncludeChildren()}
 * active.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestGetNecessaryRowsWithBoth extends TreeTableGetNecessaryRowsTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getTable().setFilterDisplayParents(true);
		getTable().setFilterDisplayChildren(true);
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
		assertNecessaryRows(child, Arrays.asList(child));
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
		assertNecessaryRows(grandchild, Arrays.asList(grandchild));
	}

	public void testRootGrandchild_RootInvisible() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		assertNecessaryRows(grandchild, Arrays.asList(grandchild));
	}

	public void testVisibleParent() {
		DefaultTreeTableNode child = getRoot().getChildAt(2);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		setFilter(filter(
			getRoot(),
			child));

		// Make 'child' visible without making 'grandchild' visible:
		DefaultTreeTableNode otherGrandchild = child.getChildAt(1);
		setFilter(filter(otherGrandchild));

		assertNecessaryRows(grandchild, Arrays.asList(grandchild));
	}

	public void testVisibleGrandParent() {
		DefaultTreeTableNode child = getRoot().getChildAt(3);
		DefaultTreeTableNode grandchild = child.getChildAt(1);
		DefaultTreeTableNode grandgrandchild = grandchild.getChildAt(0);
		setFilter(filter(
			getRoot(),
			child));

		// Make 'child' visible without making 'grandchild' visible:
		DefaultTreeTableNode otherGrandchild = child.getChildAt(2);
		setFilter(filter(otherGrandchild));

		assertNecessaryRows(grandgrandchild, Arrays.asList(grandgrandchild));
	}

	// Tests using synthetic nodes

	public void testSyntheticVisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		markSynthetic(getRoot());
		DefaultTreeTableNode child = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), Arrays.asList(child));
	}

	/**
	 * This case is not very important, the main point is that no exception is thrown.
	 */
	public void testSyntheticInvisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		markSynthetic(getRoot());
		assertNecessaryRows(getRoot(), emptyList());
	}

	public void testSyntheticRootChild_RootVisible() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = child.getChildAt(0);
		markSynthetic(child);
		assertNecessaryRows(grandchild, Arrays.asList(grandchild));
	}

	public static Test suite() {
		return suite(TestGetNecessaryRowsWithBoth.class);
	}
}
