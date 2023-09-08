/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static com.top_logic.layout.table.TableModel.*;

import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Test;

import org.apache.commons.collections4.comparators.ComparableComparator;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.UserObjectNodeComparator;

/**
 * Tests for {@link TreeTable#findNearestDisplayedRow(Object)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTreeTableFindNearestDisplayedRow extends AbstractTreeTableTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		getTree().setRootVisible(true);
		expandAll(getRoot());
		setOrder(getNodesByValuesComparator());
		getTable().setFilterDisplayParents(false);
		getTable().setFilterDisplayChildren(false);
	}

	public void testEmptyTable() {
		setFilter(FilterFactory.falseFilter());

		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot()));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
		getTree().setRootVisible(false);
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot()));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
	}

	public void testOnlyRoot() {
		setFilter(filter(getRoot()));

		assertEquals(0, findNearestDisplayedRow(getRoot()));
		assertEquals(0, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(0, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
		getTree().setRootVisible(false);
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot()));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(NO_ROW, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
	}

	public void testRootAndChild() {
		setFilter(filter(getRoot(), getRoot().getChildAt(0)));

		assertEquals(0, findNearestDisplayedRow(getRoot()));
		assertEquals(1, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(1, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
		assertEquals(1, findNearestDisplayedRow(getRoot().getChildAt(2).getChildAt(1).getChildAt(0)));
		getTree().setRootVisible(false);
		assertEquals(0, findNearestDisplayedRow(getRoot()));
		assertEquals(0, findNearestDisplayedRow(getRoot().getChildAt(0)));
		assertEquals(0, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
		assertEquals(0, findNearestDisplayedRow(getRoot().getChildAt(2).getChildAt(1).getChildAt(0)));
	}

	public void testDisplayedRow() {
		setFilter(filter(
			getRoot(),
			getRoot().getChildAt(1),
			getRoot().getChildAt(1).getChildAt(0)));

		assertEquals(2, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
	}

	public void testCorrectParentBetweenItsSiblings() {
		setFilter(filter(
			getRoot(),
			getRoot().getChildAt(0),
			getRoot().getChildAt(1),
			getRoot().getChildAt(2)));

		assert getRowOfObject(getRoot().getChildAt(1)) == 2;
		assertEquals(2, findNearestDisplayedRow(getRoot().getChildAt(1).getChildAt(0)));
	}

	public void testPredecessorNotSuccessor() {
		setFilter(filter(
			getRoot(),
			getRoot().getChildAt(2),
			getRoot().getChildAt(3),
			getRoot().getChildAt(3).getChildAt(0),
			getRoot().getChildAt(3).getChildAt(2),
			getRoot().getChildAt(4)));

		assert getRowOfObject(getRoot().getChildAt(3).getChildAt(0)) == 3;
		assertEquals(3, findNearestDisplayedRow(getRoot().getChildAt(3).getChildAt(1)));
	}

	public void testSuccessorNotParent() {
		setFilter(filter(
			getRoot(),
			getRoot().getChildAt(2),
			getRoot().getChildAt(3),
			getRoot().getChildAt(3).getChildAt(2),
			getRoot().getChildAt(4)));

		assert getRowOfObject(getRoot().getChildAt(3).getChildAt(2)) == 3;
		assertEquals(3, findNearestDisplayedRow(getRoot().getChildAt(3).getChildAt(1)));
	}

	public void testOnlyParent() {
		setFilter(filter(
			getRoot(),
			getRoot().getChildAt(2),
			getRoot().getChildAt(3),
			getRoot().getChildAt(4)));

		assert getRowOfObject(getRoot().getChildAt(3)) == 2;
		assertEquals(2, findNearestDisplayedRow(getRoot().getChildAt(3).getChildAt(1)));
	}

	private Filter<Object> filter(Object... matches) {
		return new SetFilter(Arrays.asList(matches));
	}

	private Comparator<TLTreeNode> getNodesByValuesComparator() {
		return new UserObjectNodeComparator(ComparableComparator.INSTANCE);
	}

	public static Test suite() {
		return suite(TestTreeTableFindNearestDisplayedRow.class);
	}
}
