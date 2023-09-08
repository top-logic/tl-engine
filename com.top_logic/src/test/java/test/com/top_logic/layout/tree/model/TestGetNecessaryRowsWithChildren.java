/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static java.util.Collections.*;

import junit.framework.Test;

import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;

/**
 * Tests for {@link TreeTable#getNecessaryRows(Object)} with
 * {@link TreeTable#isFilterIncludeChildren()} active but not
 * {@link TreeTable#isFilterIncludeParents()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestGetNecessaryRowsWithChildren extends TreeTableGetNecessaryRowsTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getTable().setFilterDisplayParents(false);
		getTable().setFilterDisplayChildren(true);
	}

	public void testVisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		assertNecessaryRows(getRoot(), singleton(getRoot()));
		assertNecessaryRows(getRoot().getChildAt(1), singleton(getRoot()));
		assertNecessaryRows(getRoot().getChildAt(1).getChildAt(0), singleton(getRoot()));
	}

	public void testInvisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		getTree().setRootVisible(false);
		assertNecessaryRows(getRoot(), emptyList());

		DefaultTreeTableNode child2 = getRoot().getChildAt(2);
		assertNecessaryRows(child2, singleton(child2));
		assertNecessaryRows(child2.getChildAt(1), singleton(child2));
		assertNecessaryRows(child2.getChildAt(1).getChildAt(0), singleton(child2));
	}

	// Tests using synthetic nodes

	public void testSyntheticVisibleRoot() {
		setFilter(FilterFactory.falseFilter());
		markSynthetic(getRoot());

		DefaultTreeTableNode child0 = getRoot().getChildAt(0);
		assertNecessaryRows(getRoot(), singleton(child0));
		assertNecessaryRows(child0, singleton(child0));

		DefaultTreeTableNode child2 = getRoot().getChildAt(2);
		assertNecessaryRows(child2, singleton(child2));
		assertNecessaryRows(child2.getChildAt(1), singleton(child2));
		assertNecessaryRows(child2.getChildAt(1).getChildAt(0), singleton(child2));
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

	public void testSyntheticAncestors() {
		setFilter(FilterFactory.falseFilter());
		markSynthetic(getRoot());
		DefaultTreeTableNode child = getRoot().getChildAt(2);

		markSynthetic(child);
		DefaultTreeTableNode grandchild = child.getChildAt(1);
		assertNecessaryRows(child, singleton(child.getChildAt(0)));
		assertNecessaryRows(grandchild, singleton(grandchild));
		assertNecessaryRows(grandchild.getChildAt(0), singleton(grandchild));
	}

	public void testIntermediateSyntheticAncestors() {
		setFilter(FilterFactory.falseFilter());

		DefaultTreeTableNode child = getRoot().getChildAt(1);
		markSynthetic(child);
		assertNecessaryRows(getRoot(), singleton(getRoot()));
		assertNecessaryRows(child, singleton(getRoot()));
		assertNecessaryRows(child.getChildAt(0), singleton(getRoot()));
	}

	public static Test suite() {
		return suite(TestGetNecessaryRowsWithChildren.class);
	}
}
