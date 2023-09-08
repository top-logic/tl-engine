/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.tree.model;

import static com.top_logic.basic.StringServices.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.comparators.ComparableComparator;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel.TreeTable;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.UserObjectNodeComparator;

/**
 * Base class for tests of {@link TreeTable#getNecessaryRows(Object)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TreeTableGetNecessaryRowsTest extends AbstractTreeTableTest {

	private static final class AssertNoSyntheticNodeFilter implements Filter<Object> {

		public static final AssertNoSyntheticNodeFilter INSTANCE = new AssertNoSyntheticNodeFilter();

		@Override
		public boolean accept(Object object) {
			if (!(object instanceof AbstractMutableTLTreeNode)) {
				return true;
			}
			AbstractMutableTLTreeNode<?> node = (AbstractMutableTLTreeNode<?>) object;
			assertFalse("Tried to filter a synthetic node: " + debug(object), isSynthetic(node));
			return true;
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		expandAll(getRoot());
		setOrder(getNodesByValuesComparator());
		getTree().setRootVisible(true);
		getTable().setFilterDisplayParents(false);
		getTable().setFilterDisplayChildren(false);
	}

	/**
	 * Assert that the wantedObject has the neededRows as {@link TreeTable#getNecessaryRows(Object)
	 * necessary rows}.
	 */
	protected void assertNecessaryRows(Object wantedRow, Collection<?> neededRows) {
		Set<Object> actualNecessaryRows = getNecessaryRows(wantedRow);
		Set<Object> expectedNeededRows = new HashSet<>(neededRows);
		assertEquals(expectedNeededRows, actualNecessaryRows);
	}

	/**
	 * A {@link Set} of the {@link TreeTable#getNecessaryRows(Object) necessary rows}.
	 */
	protected HashSet<Object> getNecessaryRows(Object wantedRow) {
		return new HashSet<>(getTable().getNecessaryRows(wantedRow));
	}

	/**
	 * Creates a filter that accepts exactly the given objects and asserts that no synthetic node is
	 * filtered.
	 */
	protected Filter<? super Object> filter(Object... matches) {
		return FilterFactory.and(
			AssertNoSyntheticNodeFilter.INSTANCE,
			new SetFilter(Arrays.asList(matches)));
	}

	/**
	 * @see AbstractTreeTableModel#isSynthetic(AbstractMutableTLTreeNode)
	 */
	protected static boolean isSynthetic(AbstractMutableTLTreeNode<?> node) {
		return AbstractTreeTableModel.isSynthetic(node);
	}

	/**
	 * @see AbstractTreeTableModel#markSynthetic(AbstractMutableTLTreeNode)
	 */
	protected static void markSynthetic(DefaultTreeTableNode node) {
		AbstractTreeTableModel.markSynthetic(node);
	}

	private Comparator<TLTreeNode> getNodesByValuesComparator() {
		return new UserObjectNodeComparator(ComparableComparator.INSTANCE);
	}

}