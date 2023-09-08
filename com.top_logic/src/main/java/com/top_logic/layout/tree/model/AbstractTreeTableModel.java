/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import static com.top_logic.basic.StringServices.*;
import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.table.FilterResult;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableRowFilter;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.display.IndexRange;
import com.top_logic.layout.table.display.VisiblePaneRequest;
import com.top_logic.layout.table.model.AbstractObjectTableModel;
import com.top_logic.layout.table.model.NearestDisplayedRowFinder;
import com.top_logic.layout.table.model.NoPrepare;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableModelEvent;
import com.top_logic.model.export.AccessContext;
import com.top_logic.util.ToBeValidated;
import com.top_logic.util.Utils;

/**
 * {@link AbstractTreeUINodeModel} that embeds its expanded nodes into a {@link TableModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTreeTableModel<N extends AbstractTreeTableModel.AbstractTreeTableNode<N>> extends
		AbstractTreeUINodeModel<N> {

	/**
	 * Property to qualify a node as synthetic node.
	 * 
	 * @see #markSynthetic(AbstractMutableTLTreeNode)
	 */
	static final Property<Boolean> SYNTHETIC_NODE =
		TypedAnnotatable.property(Boolean.class, "syntheticNode", false);

	/**
	 * Marks the given node as synthetic.
	 * 
	 * <p>
	 * A synthetic node is displayed in expanded state, if and only if it has any visible children.
	 * In collapsed state, it is displayed, if and only if it had any visible children when being
	 * expanded.
	 * </p>
	 * 
	 * <p>
	 * Regular column filters are not applied to synthetic nodes, even if those nodes provide a
	 * regular value for a filtered column.
	 * </p>
	 */
	public static void markSynthetic(AbstractMutableTLTreeNode<?> node) {
		node.set(AbstractTreeTableModel.SYNTHETIC_NODE, true);
	}

	/**
	 * Determines whether the given node was marked as synthetic.
	 * 
	 * @see #markSynthetic(AbstractMutableTLTreeNode)
	 */
	public static boolean isSynthetic(AbstractMutableTLTreeNode<?> node) {
		return node.get(SYNTHETIC_NODE);
	}

	@SuppressWarnings("rawtypes")
	public class TreeTable extends AbstractObjectTableModel implements TreeTableModel {

		private List<N> _rows;

		private List _unmodifiableRows;

		/** Flag whether to include parent elements to filter results. */
		private boolean _filterDisplayParents;

		/** Flag whether to include child elements to filter results. */
		private boolean _filterDisplayChildren;

		/**
		 * Creates a new {@link TreeTable}.
		 */
		public TreeTable(TableConfiguration config, List<String> columnNames) {
			super(config, columnNames);
			internalSetRows(new ArrayList<>());
			initFilterOptions();
		}

		private void internalSetRows(List<N> rows) {
			_rows = rows;
			_unmodifiableRows = Collections.unmodifiableList(_rows);
		}

		private void initFilterOptions() {
			_filterDisplayParents = getTableConfiguration().getFilterDisplayParents();
			_filterDisplayChildren = getTableConfiguration().getFilterDisplayChildren();
		}

		@Override
		public TreeUIModel<?> getTreeModel() {
			return AbstractTreeTableModel.this;
		}

		@Override
		protected void revalidateFilter(boolean updateDisplayedRows) {
			revalidateRows(updateDisplayedRows);
		}

		@Override
		protected void revalidateOrder() {
			revalidateRowsWithoutFiltering();
		}

		@Override
		public Collection getAllRows() {
			ArrayList<N> buffer = new ArrayList<>();
			N root = getRoot();
			if (isRootVisible()) {
				buffer.add(root);
			}
			addAllDescendents(buffer, root);
			return buffer;
		}

		@Override
		public boolean containsRowObject(Object anObject) {
			return _rows.contains(anObject);
		}

		@Override
		public int getRowOfObject(Object rowObject) {
			return _rows.indexOf(rowObject);
		}

		@Override
		public int findNearestDisplayedRow(Object rowObject) {
			if (isDisplayed(rowObject)) {
				return getRowOfObject(rowObject);
			}
			if (isDisplayedEmpty()) {
				return NO_ROW;
			}
			N node = (N) rowObject;
			if (Utils.equals(node, getRoot())) {
				return 0;
			}
			N invisibleAncestor = findUppermostInvisibleAncestor(node);
			assert !Utils.equals(invisibleAncestor, getRoot()) : "If the table is not empty, root is either displayed or set to invisible (setRootVisible(false)) and therefore not returned as uppermost invisible ancestor.";

			N visibleSibling = findNearestVisibleSibling(invisibleAncestor);
			if (visibleSibling == null) {
				N visibleParent = invisibleAncestor.getParent();
				assert visibleParent != null : "Either a sibling or a parent has to be visible, as the table is not empty.";
				return getRowOfObject(visibleParent);
			}
			return getRowOfObject(visibleSibling);
		}

		private N findUppermostInvisibleAncestor(N invisibleRow) {
			if (Utils.equals(invisibleRow, getRoot())) {
				return invisibleRow;
			}
			N parent = invisibleRow.getParent();
			if (isDisplayed(parent)) {
				return invisibleRow;
			}
			if (Utils.equals(parent, getRoot()) && !isRootVisible()) {
				return invisibleRow;
			}
			return findUppermostInvisibleAncestor(parent);
		}

		private N findNearestVisibleSibling(N invisibleNode) {
			if (invisibleNode == null) {
				return null;
			}
			if (Utils.equals(invisibleNode, getRoot())) {
				return null;
			}
			N parent = invisibleNode.getParent();
			if (parent.getVisibleSubtreeSize() <= 1) {
				return null;
			}
			List<N> visibleSiblings = visibleChildren(parent);
			List<N> allSiblings = invisibleNode.getParent().getChildren();
			NearestDisplayedRowFinder<N> nearestDisplayedRowFinder =
				new NearestDisplayedRowFinder<N>(visibleSiblings, allSiblings, getOrder());
			return nearestDisplayedRowFinder.find(invisibleNode).getElse(null);
		}

		/**
		 * If the root node is set to {@link AbstractTreeTableModel#isRootVisible() invisible}, this
		 * method returns the empty list for the root node.
		 * 
		 * @see com.top_logic.layout.table.TableModel#getNecessaryRows(java.lang.Object)
		 */
		@Override
		public Collection<Object> getNecessaryRows(Object rowObject) {
			return getNecessaryTreeRowCollector().getNecessaryRows((N) rowObject);
		}

		private NecessaryTreeRowCollector<N> getNecessaryTreeRowCollector() {
			return new NecessaryTreeRowCollector<>(AbstractTreeTableModel.this, this);
		}

		@Override
		public AccessContext prepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
			return doPrepareRows(accessedRows, accessedColumns);
		}

		@Override
		public List getDisplayedRows() {
			return _unmodifiableRows;
		}

		public void setRowObjects(List<N> newRows) {
			internalSetRows(newRows);
			fireTableModelEvent(0, 0, TableModelEvent.INVALIDATE);
		}

		public void insertRowObject(int index, N rowObject) {
			_rows.add(index, rowObject);
			fireTableModelEvent(index, index, TableModelEvent.INSERT);
		}

		public void removeRow(int index) {
			_rows.remove(index);
			fireTableModelEvent(index, index, TableModelEvent.INSERT);
		}

		public void insertRowObject(int index, List<N> rowObjects) {
			if (rowObjects.isEmpty()) {
				return;
			}
			_rows.addAll(index, rowObjects);
			fireTableModelEvent(index, index + rowObjects.size() - 1, TableModelEvent.INSERT);
		}

		/**
		 * @param startRow
		 *        Inclusive
		 * @param stopRow
		 *        Exclusive
		 */
		public void removeRows(int startRow, int stopRow) {
			if (stopRow == startRow) {
				return;
			}
			if (stopRow < startRow) {
				throw new IllegalArgumentException("Stop index must not be smaller than start index.");
			}
			int lastRow = stopRow - 1;
			for (int n = lastRow; n >= startRow; n--) {
				_rows.remove(n);
			}
			fireTableModelEvent(startRow, lastRow, TableModelEvent.DELETE);
		}

		/**
		 * Setter for {@link #_filterDisplayParents} and {@link #_filterDisplayChildren} at once.
		 * Don't call this method directly but call
		 * {@link TableViewModel#setFilterOptions(boolean, boolean)} instead!
		 */
		public void internalSetFilterOptions(boolean filterIncludeParents, boolean filterIncludeChildren) {
			boolean changed =
				_filterDisplayParents != filterIncludeParents || _filterDisplayChildren != filterIncludeChildren;
			if (changed) {
				_filterDisplayParents = filterIncludeParents;
				_filterDisplayChildren = filterIncludeChildren;
				revalidateRows(true);
			}
		}

		/**
		 * Setter for {@link #isFilterIncludeParents()}.
		 * 
		 * @see #internalSetFilterOptions(boolean, boolean)
		 */
		public void setFilterDisplayParents(boolean filterDisplayParents) {
			boolean changed = _filterDisplayParents != filterDisplayParents;
			if (changed) {
				_filterDisplayParents = filterDisplayParents;
				revalidateRows(true);
			}
		}

		/**
		 * Setter for {@link #isFilterIncludeChildren()}.
		 * 
		 * @see #internalSetFilterOptions(boolean, boolean)
		 */
		public void setFilterDisplayChildren(boolean filterDisplayChildren) {
			boolean changed = _filterDisplayChildren != filterDisplayChildren;
			if (changed) {
				_filterDisplayChildren = filterDisplayChildren;
				revalidateRows(true);
			}
		}

		/**
		 * @see #_filterDisplayParents
		 */
		public boolean isFilterIncludeParents() {
			return _filterDisplayParents;
		}

		/**
		 * @see #_filterDisplayChildren
		 */
		public boolean isFilterIncludeChildren() {
			return _filterDisplayChildren;
		}

		@Override
		public boolean isFilterCountingEnabled() {
			return isFinite();
		}

	}

	/**
	 * @see #isFiltering()
	 */
	private boolean _isFiltering = false;

	private FilterStateMerger filterStateMerger = null;

	/**
	 * Flag, whether there is a filter operation running.
	 * <p>
	 * This is used to prevent nested filter operations. They would lead to chaos in the filter
	 * match counts. Nested filter operations are started for example when the root node is
	 * initialized or when a part of a lazy created tree is filtered for the first time: The lazy
	 * created nodes are filtered as soon as they are added to the tree (in
	 * {@link AbstractTreeTableModel#handleAddSubtrees(AbstractTreeTableNode, IndexPosition, List)}
	 * ).
	 * </p>
	 */
	protected boolean isFiltering() {
		return _isFiltering;
	}

	/**
	 * @see #isFiltering()
	 */
	protected void setIsFiltering(boolean isFiltering) {
		_isFiltering = isFiltering;
	}

	/**
	 * merger of filter states
	 *
	 * @see FilterResult
	 */
	protected FilterStateMerger getFilterStateMerger() {
		return filterStateMerger;
	}

	/**
	 * Whether the given node is accepted by {@link #getFilter()}.
	 */
	protected final FilterResult checkByFilter(N node) {
		return getFilter().check(node);
	}

	/**
	 * Prepares this model for a following bulk access operation.
	 * 
	 * @param accessedRows
	 *        The row objects that will be accessed.
	 * @param accessedColumns
	 *        The columns that will be accessed.
	 * @return A handle to release resources after access.
	 * 
	 * @see TreeTable#prepareRows(Collection, List)
	 */
	protected AccessContext doPrepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
		return NoPrepare.INSTANCE;
	}

	/**
	 * The current node filter.
	 */
	protected final TableRowFilter getFilter() {
		return _table.getFilter();
	}

	/**
	 * The current node order.
	 */
	@SuppressWarnings("rawtypes")
	protected final Comparator getOrder() {
		return _table.getOrder();
	}

	private final TreeTable _table;

	private boolean _tableInitialized;

	private boolean _sortedInsert;

	private Provider<TableViewModel> _viewModel = NoViewModel.INSTANCE;

	private boolean _filterValidationScheduled;

	/**
	 * Creates a {@link AbstractTreeTableModel}.
	 * 
	 * @param columnNames
	 *        The available columns in {@link #getTable()}.
	 * @param config
	 *        The configuration of {@link #getTable()}.
	 * 
	 * @see AbstractMutableTLTreeModel#AbstractMutableTLTreeModel(TreeBuilder, Object)
	 */
	public AbstractTreeTableModel(TreeBuilder<N> builder, Object rootUserObject, List<String> columnNames,
			TableConfiguration config) {
		super(builder, rootUserObject);

		this._table = new TreeTable(config, columnNames);
	}

	/**
	 * Whether inserted new nodes respect the current sort and filter.
	 * 
	 * <p>
	 * If this option is set to <code>false</code> (default), inserted nodes are always displayed in
	 * the table directly after its last visible preceeding sibling.
	 * </p>
	 */
	public boolean isSortedInsert() {
		return _sortedInsert;
	}

	/**
	 * Setter for {@link #isSortedInsert()}.
	 */
	public void setSortedInsert(boolean sortedInsert) {
		_sortedInsert = sortedInsert;
	}

	/**
	 * The {@link TableModel} in which this {@link AbstractTreeTableModel} embeds its visible nodes.
	 */
	public TreeTableModel getTable() {
		if (!isTableInitialized()) {
			_tableInitialized = true;
			revalidateRows(true);
		}
		return _table;
	}

	/**
	 * Sets the {@link Provider} that delivers the view model to update its client side visibility request when a node was expanded.
	 */
	@FrameworkInternal
	public void setViewModel(Provider<TableViewModel> viewModelProvider) {
		_viewModel = viewModelProvider;
	}

	/**
	 * {@link AbstractTreeUINodeModel.TreeUINode} that keeps track of information required to maintain an
	 * embedding of the expanded part of the tree in a table model.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class AbstractTreeTableNode<N extends AbstractTreeTableNode<N>> extends TreeUINode<N> {

		/** @see #getVisibleSubtreeSize() */
		private int _visibleSubtreeSize = 1;

		/**
		 * See {@link #getFilterMatchState()}.
		 * 
		 * <p>
		 * Initial value is <code>{@link NodeFilterState#DIRECT_MATCH}</code>. Otherwise, if
		 * filtering has not be started yet, no node matches filter and therefore
		 * {@link #isDisplayed() displayed state} of children is not computed.
		 * </p>
		 */
		private NodeFilterState _filterMatchState = NodeFilterState.DIRECT_MATCH;

		private int _filterMatchCount;

		/**
		 * Creates a {@link AbstractTreeTableModel.AbstractTreeTableNode}.
		 * 
		 * @see AbstractMutableTLTreeNode#AbstractMutableTLTreeNode(AbstractMutableTLTreeModel,
		 *      AbstractMutableTLTreeNode, Object)
		 */
		public AbstractTreeTableNode(AbstractMutableTLTreeModel<N> model, N parent, Object businessObject) {
			super(model, parent, businessObject);
		}

		@Override
		public boolean setExpanded(boolean expanded) {
			boolean changed = super.setExpanded(expanded);

			if (changed) {
				if (expanded && !getModel().isFinite()) {
					getModel().filter(true);
				}
				int sizeBefore = getVisibleSubtreeSize();
				int sizeAfter = computeVisibleSubtreeSize();
				updateVisibleSubtreeSize(sizeAfter - sizeBefore);

				if (isDisplayed()) {
					getModel().handleVisibleExpansionChange(self(), sizeBefore);
				}
			}

			return changed;
		}

		@Override
		protected void resetChildren() {
			if (isInitialized()) {
				super.resetChildren();

				if (isDisplayed()) {
					int sizeBefore = getVisibleSubtreeSize();
					int sizeAfter = computeVisibleSubtreeSize();
					updateVisibleSubtreeSize(sizeAfter - sizeBefore);
					getModel().handleVisibleExpansionChange(self(), sizeBefore);
				}
			}
		}

		@Override
		void setDisplayed(boolean displayed) {
			if (displayed && displayed == isDisplayed()) {
				// Recompute the display state to apply filter state.
				displayChildren(isExpanded());
			} else {
				super.setDisplayed(displayed);
			}
		}

		@Override
		protected void displayChildren(boolean displayed) {
			if (isInitialized()) {
				AbstractTreeTableModel<N> model = getModel();
				boolean parentMatchesFilter = model.calcAncestorMatchesFilter(self());
				for (N child : getChildren()) {
					child.setDisplayed(displayed && model.shouldDisplay(child, parentMatchesFilter));
				}
			}
		}

		@Override
		public AbstractTreeTableModel<N> getModel() {
			return (AbstractTreeTableModel<N>) super.getModel();
		}

		/**
		 * Update the {@link #getVisibleSubtreeSize()} of this nodes and all ancestors after locally
		 * modifying the number of children.
		 * 
		 * @param delta
		 *        The number of visible children that were added to this node.
		 */
		void updateVisibleSubtreeSize(int delta) {
			if (delta == 0) {
				return;
			}
			_visibleSubtreeSize += delta;

			N parent = getParent();
			if (parent != null && parent.isExpanded()) {
				parent.updateVisibleSubtreeSize(delta);
			}
		}

		/**
		 * Recomputes the {@link #getVisibleSubtreeSize()} in the whole initialized subtree of this
		 * node.
		 * 
		 * @return The resulting size of this node.
		 */
		int refreshVisibleSubtreeSize() {
			int result = 1;

			if (isInitialized()) {
				if (isExpanded()) {
					AbstractTreeTableModel<N> model = getModel();
					boolean parentMatchesFilter = model.calcAncestorMatchesFilter(self());
					for (N child : getChildren()) {
						int childSize = child.refreshVisibleSubtreeSize();
						if (model.shouldDisplay(child, parentMatchesFilter)) {
							result += childSize;
						}
					}
				} else {
					for (N child : getChildren()) {
						child.refreshVisibleSubtreeSize();
					}
				}
			}
			_visibleSubtreeSize = result;

			return result;
		}

		private int computeVisibleSubtreeSize() {
			int size = 1;
			if (isExpanded()) {
				AbstractTreeTableModel<N> model = getModel();
				boolean parentMatchesFilter = model.calcAncestorMatchesFilter(self());
				for (N child : getChildren()) {
					if (model.shouldDisplay(child, parentMatchesFilter)) {
						size += child.getVisibleSubtreeSize();
					}
				}
			}
			return size;
		}

		/**
		 * The number of visible nodes below (and including) this node depending of the current
		 * expansion state and the filter setting.
		 * 
		 * <p>
		 * The result is independent of the {@link #isDisplayed()} state of this node. This method
		 * returns the size of the displayed subtree routed at this node assuming this node would be
		 * displayed. That means the root node ignores
		 * {@link AbstractTreeTableModel#isRootVisible()} and always counts itself.
		 * </p>
		 */
		public int getVisibleSubtreeSize() {
			return _visibleSubtreeSize;
		}

		/**
		 * The {@link AbstractTreeTableModel#getPosition(AbstractTreeTableNode)} of this node.
		 * <p>
		 * Returns -1 for the {@link AbstractTreeTableModel#isRootVisible() none visible} root node.
		 * </p>
		 */
		public final int getPosition() {
			return getModel().getPosition(self());
		}

		/**
		 * Does the node match the active filter?
		 * <p>
		 * Indirect matches are for example, when a node is displayed, because its parent is
		 * displayed.
		 * </p>
		 */
		public NodeFilterState getFilterMatchState() {
			return _filterMatchState;
		}

		/**
		 * @see #getFilterMatchState()
		 */
		public void setFilterMatchState(NodeFilterState filterMatchState) {
			_filterMatchState = filterMatchState;
		}
		
		/**
		 * Filters the subtree and stores the result for each node in {@link #getFilterMatchState()}
		 * .
		 * <p>
		 * Uses the filter from the {@link #getModel() model}.
		 * </p>
		 */
		protected boolean filterInternal(boolean storeFilterResult, FilterResult pathFilterState) {
			FilterResult rowFilterState = filterThis();
			boolean isRowEvaluated = rowFilterState.hasNoFilters() || rowFilterState.hasApplicableFilters();
			boolean isRowMatch = isRowEvaluated && rowFilterState.doAllFilterAccept();

			getModel().getFilterStateMerger().merge(rowFilterState, pathFilterState);
			FilterResult combinedPathState = rowFilterState;

			boolean isInvisibleRoot = getModel().isRoot(self()) && !getModel().isRootVisible();
			boolean hasMatchingChildren = false;
			/* Root has to be checked explicitly, as isDisplayed() always returns true for it. */
			if ((getModel().isFinite() || isDisplayed()) && !isInvisibleRoot) {
				for (N child : getChildren()) {
					hasMatchingChildren |= child.filterInternal(storeFilterResult, combinedPathState);
				}
			}

			boolean isPathMatch =
				isRowEvaluated && combinedPathState.areAllFilterApplicable() && combinedPathState.doAllFilterAccept();

			if (storeFilterResult) {
				if (isRowMatch && (isPathMatch || hasMatchingChildren)) {
					setFilterMatchState(NodeFilterState.DIRECT_MATCH);
				} else {
					setFilterMatchState(NodeFilterState.NO_DIRECT_MATCH);
				}
			}

			if (isCountableNode(combinedPathState, hasMatchingChildren)) {
				getModel().getFilterStateMerger().updateCountState(rowFilterState, pathFilterState);
				countThis(rowFilterState);
			}

			return isPathMatch || hasMatchingChildren;
		}

		private boolean isCountableNode(FilterResult combinedPathState, boolean hasMatchingChildren) {
			boolean isPotentialPathMatch = combinedPathState.areAllFilterApplicable()
				&& (combinedPathState.doAllFilterAccept() || combinedPathState.isSingleFilterDenial());
			boolean isCountablePath = isPotentialPathMatch || hasMatchingChildren;
			return isCountablePath && combinedPathState.hasCountableValues();
		}

		/**
		 * Apply the filter to this node.
		 */
		private FilterResult filterThis() {
			return getModel().checkByFilter(self());
		}

		private void countThis(FilterResult filterResult) {
			getModel().getFilter().count(filterResult);
		}

		/**
		 * The number of nodes in the subtree that match the current filter.
		 * <p>
		 * Including the node itself.
		 * </p>
		 */
		public int getFilterMatchCount() {
			return _filterMatchCount;
		}

		/**
		 * @see #getFilterMatchCount()
		 */
		private void setFilterMatchCount(int count) {
			_filterMatchCount = count;
		}

		/**
		 * Recalculates the {@link #getFilterMatchCount()} for the whole subtree, recursively, and
		 * updates the ancestors.
		 */
		protected void updateFilterMatchCount() {
			if (getParent() == null) {
				updateFilterMatchCountInternal();
			} else {
				int oldCount = getFilterMatchCount();
				updateFilterMatchCountInternal();
				int newCount = getFilterMatchCount();
				int difference = newCount - oldCount;
				getParent().updateFilterMatchCount(difference);
			}
		}

		private int updateFilterMatchCountInternal() {
			int count = 0;
			if (getFilterMatchState() == NodeFilterState.DIRECT_MATCH) {
				count += 1;
			}
			if (getModel().isFinite() || isExpanded()) {
				for (AbstractTreeTableNode<?> child : getChildren()) {
					count += child.updateFilterMatchCountInternal();
				}
			}
			setFilterMatchCount(count);
			return count;
		}

		/**
		 * Adds the given difference to the current {@link #getFilterMatchCount() filter match
		 * count}.
		 */
		protected void updateFilterMatchCount(int difference) {
			setFilterMatchCount(getFilterMatchCount() + difference);
			if (getParent() != null) {
				getParent().updateFilterMatchCount(difference);
			}
		}

		@Override
		public void updateNodeProperties() {
			super.updateNodeProperties();
			if (isDisplayed()) {
				getModel().updateNode(self());
			}
		}

	}

	/**
	 * Property, that indicates whether a node has been accepted by filter criteria or not.
	 */
	public static enum NodeFilterState {
		/** Is accepted by filter criteria */
		DIRECT_MATCH,

		/**
		 * Is not accepted by filter criteria, but may be visible due to technical reasons (e.g.
		 * show not matching parent node of matching child nodes).
		 */
		NO_DIRECT_MATCH,

		/** Has not been checked by filter */
		NOT_VALIDATED
	}

	/**
	 * Refreshes the complete table.
	 */
	protected void revalidateRows(boolean updateDisplayedRows) {
		filter(updateDisplayedRows);
		if (updateDisplayedRows) {
			getRoot().refreshVisibleSubtreeSize();
			revalidateRowsWithoutFiltering();
		}
	}

	private void scheduleFilterRevalidation() {
		if (_filterValidationScheduled) {
			return;
		}
		_filterValidationScheduled = true;
		DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(new ToBeValidated() {
			@Override
			public void validate(DisplayContext context) {
				executeFilterRevalidation();
			}
		});
	}

	void executeFilterRevalidation() {
		try {
			filter(true);
		} finally {
			_filterValidationScheduled = false;
		}
	}

	@FrameworkInternal
	void revalidateRowsWithoutFiltering() {
		N root = getRoot();
		List<N> newRows;
		if (isRootVisible()) {
			root.setDisplayed(shouldDisplay(root));
			if (root.isDisplayed()) {
				newRows = visibleSubtree(root, false);
			} else {
				newRows = new ArrayList<>();
			}
		} else {
			root.setDisplayed(true);
			newRows = visibleDescendents(root);
		}
		_table.setRowObjects(newRows);
	}

	@Override
	protected void handleRootVisible(boolean newVisibility) {
		super.handleRootVisible(newVisibility);

		if (isTableInitialized()) {
			revalidateRows(true);
		}
	}

	/**
	 * Whether the node is the root of this tree.
	 */
	public boolean isRoot(N node) {
		return Utils.equals(node, getRoot());
	}

	/**
	 * Hook that is called after a visible node has changed its expansion state.
	 * 
	 * @param node
	 *        The node, whose state has changed.
	 * @param sizeBefore
	 *        the size of the visible subtree of the changed node before the state change.
	 */
	protected void handleVisibleExpansionChange(N node, int sizeBefore) {
		if (isTableInitialized()) {
			int position = getPosition(node);

			// Note: A not visible root node must not be updated.
			if (position >= 0) {
				_table.updateRows(position, position);
			}
			int firstChildPosition = position + 1;
			if (node.isExpanded()) {
				List<N> visibleDescendents = visibleDescendents(node);
				_table.insertRowObject(firstChildPosition, visibleDescendents);
			} else {
				int visibleDescendantsSizeBefore = sizeBefore - 1;
				_table.removeRows(firstChildPosition, firstChildPosition + visibleDescendantsSizeBefore);
			}
			updateVisibilityRequest(node);
		}
	}

	/**
	 * Hook that is called after a visible node has changed its properties.
	 * 
	 * @param node
	 *        The node, whose properties have changed.
	 */
	protected void updateNode(N node) {
		if (isTableInitialized()) {
			int position = getPosition(node);

			// Note: A not visible root node must not be updated.
			if (position >= 0) {
				_table.updateRows(position, position);
			}
		}
	}

	/**
	 * If this tree is attached to a {@link TableViewModel}, than its {@link VisiblePaneRequest} is
	 * updated to the given node.
	 * 
	 * @param node
	 *        The node to make visible on the client.
	 * 
	 * @see AbstractTreeTableModel#setViewModel(Provider)
	 */
	protected void updateVisibilityRequest(N node) {
		TableViewModel viewModel = _viewModel.get();
		if (viewModel != null) {
			int firstVisibleRow = getPosition(node);
			int lastVisibleRow = firstVisibleRow + node.getVisibleSubtreeSize() - 1;
			viewModel.getClientDisplayData().getVisiblePaneRequest()
				.setRowRange(IndexRange.multiIndex(firstVisibleRow, lastVisibleRow));
		}
	}

	@Override
	protected void handleRemoveSubtree(N subtreeRootParent, N subtreeRoot) {
		// "isDisplayed" is changed during the call to super.
		boolean wasDisplayed = subtreeRoot.isDisplayed();
		super.handleRemoveSubtree(subtreeRootParent, subtreeRoot);

		if (!isTableInitialized()) {
			return;
		}

		if (wasDisplayed) {
			removeTableRows(subtreeRootParent, subtreeRoot);
		}
		scheduleFilterRevalidation();
	}

	private void removeTableRows(N subtreeRootParent, N subtreeRoot) {
		if (subtreeRootParent.areChildrenDisplayed()) {
			int parentPosition = getPosition(subtreeRootParent);
			int childPosition = parentPosition + 1;

			/* The removed child is no longer part of the tree. To find it in the table, the
			 * remaining children (and their subtrees) have to be skipped. */
			for (int n = 0, cnt = subtreeRootParent.getChildCount(); n < cnt; n++) {
				@SuppressWarnings("unchecked")
				N child = (N) _table.getRowObject(childPosition);
				if (child == subtreeRoot) {
					removeExChild(subtreeRootParent, subtreeRoot, childPosition);
					return;
				}

				childPosition += child.getVisibleSubtreeSize();
			}
			// All remaining children have been skipped, the searched node must be at the end of the
			// children list.
			assert _table.getRowObject(childPosition) == subtreeRoot : "Removed node not found in table: "
				+ debug(subtreeRoot) + ", Ex-Parent:" + debug(subtreeRootParent);
			removeExChild(subtreeRootParent, subtreeRoot, childPosition);
		}
	}

	/**
	 * Returns the visible children, sorted.
	 * <p>
	 * <em>Does not return the ancestors of the children.</em>
	 * </p>
	 */
	protected List<N> visibleChildren(N parent) {
		List<N> children = new ArrayList<>();
		for (N child : parent.getChildren()) {
			if (isDisplayed(child)) {
				children.add(child);
			}
		}
		sort(children);
		return children;
	}

	private void removeExChild(N parent, N child, int tablePositionChild) {
		int removedCnt = child.getVisibleSubtreeSize();
		_table.removeRows(tablePositionChild, tablePositionChild + removedCnt);
		parent.updateVisibleSubtreeSize(-removedCnt);
		updateAncestorVisibilityAfterRemove(parent);
	}

	/**
	 * Checks whether the node {@link #shouldDisplay(AbstractTreeTableNode) should} still be
	 * displayed.
	 * <p>
	 * If not, it is removed from the {@link #_table} and its parents
	 * {@link N#getVisibleSubtreeSize()}.
	 * </p>
	 */
	private void updateAncestorVisibilityAfterRemove(N changedNode) {
		N nodeToRemove = findUppermostAncestorToRemove(changedNode);
		if (nodeToRemove != null) {
			removeFromTable(nodeToRemove);
		}
	}

	/**
	 * Find the uppermost ancestor of the given node (or the node itself) that has to be removed
	 * because it {@link #shouldDisplay(AbstractTreeTableNode, boolean) should not be displayed}
	 * anymore.
	 * 
	 * @return null, if there is no ancestor that needs to be removed.
	 */
	private N findUppermostAncestorToRemove(N node) {
		if (shouldDisplay(node)) {
			return null;
		}
		if (isRoot(node) && !isRootVisible()) {
			return null;
		}
		N nodeToRemove = node;
		N parent = nodeToRemove.getParent();
		while ((parent != null) && !shouldDisplay(parent)) {
			if (isRoot(parent) && !isRootVisible()) {
				break;
			}
			nodeToRemove = parent;
			parent = parent.getParent();
		}
		return nodeToRemove;
	}

	/**
	 * Removes the node from the {@link #_table} and updates its parents
	 * {@link N#getVisibleSubtreeSize()}.
	 */
	private void removeFromTable(N node) {
		int position = getPosition(node);
		_table.removeRows(position, position + node.getVisibleSubtreeSize());
		if (node.getParent() != null) {
			node.getParent().updateVisibleSubtreeSize(-node.getVisibleSubtreeSize());
		}
		node.setDisplayed(false);
	}

	@Override
	protected void handleAddSubtrees(N parent, IndexPosition position, List<N> subtreeRoots) {
		super.handleAddSubtrees(parent, position, subtreeRoots);

		if (!isTableInitialized()) {
			return;
		}
		for (N newNode : subtreeRoots) {
			newNode.refreshVisibleSubtreeSize();
		}
		N ancestorToAdd = findUppermostAncestorToAdd(parent);
		if (ancestorToAdd != null) {
			if (Utils.equals(ancestorToAdd, getRoot()) || ancestorToAdd.getParent().areChildrenDisplayed()) {
				insertIntoTable(ancestorToAdd, position);
			}
			return;
		}
		if (parent.areChildrenDisplayed()) {
			enforceVisibilityOfNewColumns(subtreeRoots);
			insertIntoTable(parent, position, subtreeRoots);
		}
	}

	/**
	 * Filters the tree and calculates {@link AbstractTreeTableNode#getFilterMatchState()} and
	 * {@link AbstractTreeTableNode#getFilterMatchCount()} on each node.
	 */
	protected final void filter(boolean updateDisplayedRows) {
		if (isFiltering()) {
			/* There is already a filter()-operation running. Filtering now would lead to chaos in
			 * the filter match counts. */
			return;
		}
		setIsFiltering(true);
		try {
			getFilter().startFilterRevalidation();
			FilterResult initialState;
			if (_table.isFilterIncludeParents()) {
				initialState = getFilter().getInitialState(true);
				filterStateMerger = IncludeParentsMerger.INSTANCE;
			} else {
				initialState = getFilter().getInitialState(false);
				filterStateMerger = DefaultMerger.INSTANCE;
			}
			if (isRootVisible()) {
				getRoot().filterInternal(updateDisplayedRows, initialState);
			} else {
				getRoot().setFilterMatchState(NodeFilterState.NO_DIRECT_MATCH);
				for (N child : getRoot().getChildren()) {
					child.filterInternal(updateDisplayedRows, initialState);
				}
			}
			getFilter().stopFilterRevalidation();

			getRoot().updateFilterMatchCount();
		} finally {
			setIsFiltering(false);
		}
	}

	private void enforceVisibilityOfNewColumns(List<N> subtreeRoots) {
		/* Workaround to keep new objects visible, even if they don't match the filters. Drawbacks:
		 * 1. Makes objects created from other sessions visible, too. */
		for (N subtreeRoot : subtreeRoots) {
			subtreeRoot.setFilterMatchState(NodeFilterState.NOT_VALIDATED);
		}
	}

	private void insertIntoTable(N parent, IndexPosition position, List<N> subtreeRoots) {
		int index;
		switch (position.getStrategy()) {
			case START:
				index = getPosition(parent) + 1;
				insertIntoTable(parent, index, subtreeRoots);
				break;
			case END:
				index = endIndex(parent);
				insertIntoTable(parent, index, subtreeRoots);
				break;
			case BEFORE:
				int anchorIndex = position.getIndex() + subtreeRoots.size();
				if (anchorIndex == parent.getChildCount()) {
					index = endIndex(parent);
				} else {
					index = getPosition(parent.getChildAt(anchorIndex));
				}
				insertIntoTable(parent, index, subtreeRoots);
				break;
			case AFTER: {
				N anchorNode = parent.getChildAt(position.getIndex());
				index = getPosition(anchorNode) + anchorNode.getVisibleSubtreeSize();
				insertIntoTable(parent, index, subtreeRoots);
				break;
			}
			case AUTO: {
				boolean parentMatchesFilter = calcAncestorMatchesFilter(parent);
				for (N subtreeRoot : subtreeRoots) {
					if (shouldDisplay(subtreeRoot, parentMatchesFilter)) {
						index = findInsertPosition(parent, subtreeRoot);
						insertIntoTable(parent, index, Collections.singletonList(subtreeRoot));
					} else {
						subtreeRoot.setDisplayed(false);
					}
				}
				break;
			}
			default:
				throw new UnreachableAssertion("No such strategy: " + position.getStrategy());
		}
	}

	private int findInsertPosition(N parent, N subtreeRoot) {
		if (parent == null) {
			// Inserting root
			return 0;
		}
		int parentPosition = getPosition(parent);

		int childIndex = 1;
		@SuppressWarnings("unchecked")
		Comparator<? super N> order = getOrder();
		while (childIndex <= parent.getVisibleSubtreeSize() - 1) {
			@SuppressWarnings("unchecked")
			N preceedingSibling = (N) _table.getRowObject(parentPosition + childIndex);
			if (preceedingSibling.getParent() != parent) {
				break;
			}
			if (order.compare(subtreeRoot, preceedingSibling) <= 0) {
				break;
			}
			childIndex += preceedingSibling.getVisibleSubtreeSize();
		}
		return parentPosition + childIndex;
	}

	/**
	 * Find the uppermost ancestor of the given node (or the node itself) that has to be added
	 * because it {@link #shouldDisplay(AbstractTreeTableNode, boolean) should be displayed} again.
	 * 
	 * @return null, if there is no ancestor that needs to be added.
	 */
	private N findUppermostAncestorToAdd(N node) {
		N candidate = node;
		N nodeToAdd = null;
		while ((!candidate.isDisplayed()) && shouldDisplay(candidate)) {
			nodeToAdd = candidate;
			if (candidate.getParent() == null) {
				return nodeToAdd;
			}
			candidate = candidate.getParent();
		}
		return nodeToAdd;
	}

	private void insertIntoTable(N nodeToAdd, IndexPosition position) {
		N parent = nodeToAdd.getParent();
		boolean isAddingRoot = (parent == null);
		assert isAddingRoot == (nodeToAdd == getRoot());
		if (isAddingRoot && !isRootVisible()) {
			insertIntoTable(parent, position, new ArrayList<>(nodeToAdd.getChildren()));
			return;
		}
		insertIntoTable(parent, position, singletonList(nodeToAdd));
	}

	private int endIndex(N parent) {
		return getPosition(parent) + parent.getVisibleSubtreeSize();
	}

	/**
	 * @param parent
	 *        Is null if and only if the given list contains exactly the root node.
	 */
	private void insertIntoTable(N parent, int position, List<N> subtreeRoots) {
		boolean isInsertingRoot = (parent == null);
		boolean ancestorMatchesFilter = isInsertingRoot ? false : calcAncestorMatchesFilter(parent);
		List<N> addedSubtree = visibleSubtrees(subtreeRoots, ancestorMatchesFilter);
		_table.insertRowObject(position, addedSubtree);
		if (!isInsertingRoot) {
			parent.updateVisibleSubtreeSize(addedSubtree.size());
		}
		for (N node : subtreeRoots) {
			node.setDisplayed(true);
		}
	}

	private boolean isTableInitialized() {
		return _tableInitialized;
	}

	/**
	 * The visible subtree including the node itself.
	 */
	private List<N> visibleSubtree(N subtreeRoot, boolean ancestorMatchesFilter) {
		List<N> rows = new ArrayList<>(subtreeRoot.getVisibleSubtreeSize());
		addVisibleSubtree(rows, subtreeRoot, ancestorMatchesFilter);
		return rows;
	}

	/**
	 * The visible subtree without the node itself.
	 */
	private List<N> visibleDescendents(N node) {
		return visibleDescendents(node, calcParentMatchesFilter(node));
	}

	private List<N> visibleDescendents(N node, boolean parentMatchesFilter) {
		return visibleSubtrees(node.getChildren(),
			parentMatchesFilter || node.getFilterMatchState() == NodeFilterState.DIRECT_MATCH);
	}

	private boolean hasExpandableDescendent(N node, boolean parentMatchesFilter) {
		boolean ancestorMatchesFilter =
			parentMatchesFilter || node.getFilterMatchState() == NodeFilterState.DIRECT_MATCH;
		for (N child : node.getChildren()) {
			if (shouldDisplay(child, ancestorMatchesFilter)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Suppose the parent of the given node is visible and expanded. Then this method computes
	 * whether the given node is also visible.
	 * <p>
	 * Use {@link #shouldDisplay(AbstractTreeTableNode, boolean)} instead if it is already known
	 * whether one of the ancestors of the node is a direct filter match. (For performance
	 * optimization.)
	 * </p>
	 * 
	 * @param node
	 *        The node to check.
	 */
	public final boolean shouldDisplay(N node) {
		return shouldDisplay(node, calcParentMatchesFilter(node));
	}

	/**
	 * Suppose the parent of the given node is visible and expanded. Then this method computes
	 * whether the given node is also visible.
	 * 
	 * @param node
	 *        The node to check.
	 * @param parentMatchesFilter
	 *        Whether any of the ancestors of the node is a direct filter match.
	 */
	protected final boolean shouldDisplay(N node, boolean parentMatchesFilter) {
		if (isRoot(node) && !isRootVisible()) {
			return false;
		}
		if (isSynthetic(node)) {
			return hasExpandableDescendent(node, parentMatchesFilter);
		}
		if (node.getFilterMatchState() == NodeFilterState.DIRECT_MATCH
			|| node.getFilterMatchState() == NodeFilterState.NOT_VALIDATED) {
			return true;
		}
		if (_table.isFilterIncludeChildren() && parentMatchesFilter) {
			return true;
		}
		/* In infinite trees, it is (almost) undecidable whether a node has a descendant that
		 * matches the filter, as there are infinite descendants. Therefore, this tree filter option
		 * is not supported for infinite trees. It is explicitly suppressed here, as the filter
		 * match counts can be wrong in infinite trees, as only visible nodes and their direct
		 * children are counted. And they are not always updated when the tree changes. These
		 * inconsistent numbers could cause inconsistent behavior if this tree filter option is
		 * activated. Therefore, it is suppressed here. */
		if (isFinite()) {
			return node.getFilterMatchCount() > 0;
		} else {
			return false;
		}
	}

	void addAllDescendents(Collection<N> buffer, N node) {
		for (N child : node.getChildren()) {
			buffer.add(child);
			if (child.isExpanded()) {
				addAllDescendents(buffer, child);
			}
		}
	}

	private List<N> visibleSubtrees(List<N> children, boolean parentMatchesFilter) {
		// The given list of children might not be mutable therefore, copy it.
		List<N> sortedChildren = new ArrayList<>(children);
		sort(sortedChildren);
		List<N> displayedChildren = new ArrayList<>();
		addVisibleSubtrees(displayedChildren, sortedChildren, parentMatchesFilter);
		return displayedChildren;
	}

	/**
	 * Sorts the siblings.
	 * <p>
	 * <em>All nodes have to have the same parent.</em>
	 * </p>
	 */
	void sort(List<N> siblings) {
		Collections.sort(siblings, getOrder());
	}

	private void addVisibleSubtrees(List<N> buffer, List<N> nodes, boolean parentMatchesFilter) {
		for (N node : nodes) {
			addVisibleSubtree(buffer, node, parentMatchesFilter);
		}
	}

	private void addVisibleSubtree(List<N> buffer, N node, boolean parentMatchesFilter) {
		if (!shouldDisplay(node, parentMatchesFilter)) {
			return;
		}
		buffer.add(node);
		if (!node.isExpanded()) {
			return;
		}
		List<N> children = visibleDescendents(node, parentMatchesFilter);
		buffer.addAll(children);
		return;
	}

	/**
	 * Whether any parent (recursively) of the given node match the current filter.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 */
	protected boolean calcParentMatchesFilter(N node) {
		if (node.getParent() == null) {
			return false;
		}
		return calcAncestorMatchesFilter(node.getParent());
	}

	/**
	 * Whether the given node or one of its parents (recursively) matches the current filter.
	 * 
	 * @param node
	 *        Is not allowed to be null.
	 */
	protected boolean calcAncestorMatchesFilter(N node) {
		// Introduce a new variable to keep the parameter unchanged for easier debugging.
		N ancestor = node;
		do {
			if (ancestor.getFilterMatchState() == NodeFilterState.DIRECT_MATCH) {
				return true;
			}
			ancestor = ancestor.getParent();
		} while (ancestor != null);
		return false;
	}

	/**
	 * Compute the table row id, where the given node is displayed.
	 * <p>
	 * Returns -1 for the {@link AbstractTreeTableModel#isRootVisible() none visible} root node.
	 * </p>
	 */
	protected int getPosition(N node) {
		N parent = node.getParent();
		if (parent == null) {
			return isRootVisible() ? 0 : -1;
		} else {
			int position = getPosition(parent) + 1;
			while (true) {
				assert position < _table.getRowCount() : "Node not child of its parent.";

				@SuppressWarnings("unchecked")
				N preceedingSibling = (N) _table.getRowObject(position);
				if (preceedingSibling == node) {
					return position;
				} else {
					position += preceedingSibling.getVisibleSubtreeSize();
				}
			}
		}
	}

	private interface FilterStateMerger {

		void merge(FilterResult baseState, FilterResult additionalState);

		void updateCountState(FilterResult baseState, FilterResult additionalState);
	}

	private static class DefaultMerger implements FilterStateMerger {

		static final FilterStateMerger INSTANCE = new DefaultMerger();

		@Override
		public void merge(FilterResult baseState, FilterResult additionalState) {
			baseState.or(additionalState);
		}

		@Override
		public void updateCountState(FilterResult baseState, FilterResult additionalState) {
			baseState.updateCountState(additionalState);
		}
	}

	private static class IncludeParentsMerger implements FilterStateMerger {

		static final FilterStateMerger INSTANCE = new IncludeParentsMerger();

		@Override
		public void merge(FilterResult baseState, FilterResult additionalState) {
			baseState.and(additionalState);
		}

		@Override
		public void updateCountState(FilterResult baseState, FilterResult additionalState) {
			baseState.replaceCountStateFrom(additionalState);
		}
	}

}
