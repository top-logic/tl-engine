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
 * Tests for {@link TreeTable#getNecessaryRows(Object)} with
 * {@link TreeTable#isFilterIncludeParents()} active but not
 * {@link TreeTable#isFilterIncludeChildren()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestGetNecessaryRowsWithParents extends TreeTableGetNecessaryRowsTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getTable().setFilterDisplayParents(true);
		getTable().setFilterDisplayChildren(false);
	}

	public void testVisibleRoot_NoMatches() {
		setFilter(FilterFactory.falseFilter());
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), singleton(getRoot()));
		assertNecessaryRows(child, singleton(child));
		assertNecessaryRows(grandchild, singleton(grandchild));
	}

	public void testInvisibleRoot_NoMatches() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, singleton(child));
		assertNecessaryRows(grandchild, singleton(grandchild));
	}

	public void testVisibleRoot_RootMatches() {
		setFilter(filter(getRoot()));
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, singleton(child));
		assertNecessaryRows(grandchild, singleton(grandchild));
	}

	public void testInvisibleRoot_RootMatches() {
		setFilter(filter(getRoot()));
		getTree().setRootVisible(false);
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, singleton(child));
		assertNecessaryRows(grandchild, singleton(grandchild));
	}

	public void testVisibleRoot_RootChildMatches() {
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		setFilter(filter(child));
		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, emptyList());
		assertNecessaryRows(grandchild, singleton(grandchild));
	}

	public void testInvisibleRoot_RootChildMatches() {
		DefaultTreeTableNode child = getRoot().getChildAt(1);
		DefaultTreeTableNode grandchild = getRoot().getChildAt(0);
		setFilter(filter(child));
		getTree().setRootVisible(false);
		assertNecessaryRows(getRoot(), emptyList());
		assertNecessaryRows(child, emptyList());
		assertNecessaryRows(grandchild, singleton(grandchild));
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
		return suite(TestGetNecessaryRowsWithParents.class);
	}
}
