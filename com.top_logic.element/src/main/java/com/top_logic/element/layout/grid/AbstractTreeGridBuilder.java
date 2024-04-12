/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import static com.top_logic.basic.shared.collection.iterator.IteratorUtilShared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.ListComparator;
import com.top_logic.basic.col.MappedList;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TreeView;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.IndexPosition;
import com.top_logic.layout.PositionStrategy;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.table.display.ClientDisplayData;
import com.top_logic.layout.table.display.RowIndexAnchor;
import com.top_logic.layout.table.display.ViewportState;
import com.top_logic.layout.table.dnd.TableDragSource;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.NoPrepare;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.component.TreeModelBuilder;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeUIModelUtil;
import com.top_logic.layout.tree.model.TreeViewConfig;
import com.top_logic.layout.tree.model.UserObjectIndex;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.export.AccessContext;
import com.top_logic.model.export.Preloader;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Common base class for {@link GridBuilder}s creating tree-structured grids.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTreeGridBuilder<R> implements GridBuilder<R> {

	/**
	 * Configuration of an {@link AbstractTreeGridBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PolymorphicConfiguration<AbstractTreeGridBuilder<?>> {

		/**
		 * The name of the attribute describing the actually wrapped builder.
		 */
		String BUILDER_ATTRIBUTE = "builder";

	}

	/**
	 * Commands when {@link #canExpandAll()}.
	 * 
	 * <p>
	 * Note: must not be contained in static block, because {@link CommandHandlerFactory} may not be
	 * started, when class is loaded.
	 * </p>
	 */
	private final Collection<CommandHandler> _allCommands =
		Arrays.<CommandHandler> asList(
			CommandHandlerFactory.getInstance().getHandler(TreeGridExpandCollapseAll.EXPAND_ID),
			CommandHandlerFactory.getInstance().getHandler(TreeGridExpandCollapseAll.COLLAPSE_ID));

	/**
	 * Commands when <em>not</em> {@link #canExpandAll()}.
	 * 
	 * <p>
	 * Note: must not be contained in static block, because {@link CommandHandlerFactory} may not be
	 * started, when class is loaded.
	 * </p>
	 */
	private final Collection<CommandHandler> _restrictedCommands =
		Arrays.<CommandHandler> asList(
			CommandHandlerFactory.getInstance().getHandler(TreeGridExpandCollapseAll.COLLAPSE_ID));

	private boolean _rootVisible;

	private boolean _expandSelectedNode;

	private boolean _expandRoot;
	
	private boolean _adjustSelectionWhenCollapsing;

	/**
	 * Creates a new {@link AbstractTreeGridBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractTreeGridBuilder}.
	 */
	public AbstractTreeGridBuilder(InstantiationContext context, Config config) {
		// Nothing to do.
	}

	/**
	 * Creates a new {@link AbstractTreeGridBuilder}.
	 */
	public AbstractTreeGridBuilder() {
		// no special here
	}

	/**
	 * Retrieve the root element to display from the given component.
	 */
	public final Object getRoot(GridComponent aComponent) {
		return getModel(aComponent.getModel(), aComponent);
	}
	
	/**
	 * @see TreeModelBuilder#getParents(LayoutComponent, Object)
	 */
	protected abstract Collection<? extends Object> getParents(LayoutComponent component, Object rowModel);

	/**
	 * @see TreeModelBuilder#getChildIterator(LayoutComponent, Object)
	 */
	protected abstract Iterable<?> getChildren(LayoutComponent component, Object rowModel);

	/**
	 * @see TreeModelBuilder#isLeaf(LayoutComponent, Object)
	 */
	protected abstract boolean isLeaf(LayoutComponent component, Object childObject);

	/**
	 * @see TreeModelBuilder#getNodesToUpdate(LayoutComponent, Object)
	 */
	protected abstract Collection<? extends Object> getNodesToUpdate(LayoutComponent component, Object object);

	/**
	 * The {@link TLStructuredType}s for which the tree needs to register itself as
	 * {@link ModelListener}.
	 * <p>
	 * The result has to be constant. It cannot be used for temporarily relevant objects.
	 * </p>
	 * 
	 * @see LayoutComponent#getTypesToObserve()
	 * @see TreeModelBuilder#getTypesToObserve()
	 */
	protected abstract Set<TLStructuredType> getTypesToObserve();

	@Override
	public Collection<CommandHandler> getCommands() {
		if (canExpandAll()) {
			return _allCommands;
		} else {
			return _restrictedCommands;
		}
	}
	
	/**
	 * @see TreeModelBuilder#canExpandAll()
	 */
	protected abstract boolean canExpandAll();

	@Override
	public GridHandler<R> createHandler(
			final GridComponent grid, final TableConfiguration tableConfiguration,
			final String[] availableColumns, final Mapping<Object, ? extends R> toRow,
			final Mapping<? super R, ?> toModel) {
		tableConfiguration.setTree(true);
		return new TreeGridHandler(toRow, toModel, grid, tableConfiguration, availableColumns);
	}

	/**
	 * If this {@link GridComponent} displays a tree, the variable states whether the root node of
	 * the tree is visible.
	 * 
	 * @return If <code>true</code>, then the root node of the displayed tree is visible.
	 */
	public boolean isRootVisible() {
		return _rootVisible;
	}

	/**
	 * Returns true if the root node should be expanded by default, otherwise false.
	 * 
	 * @see TreeViewConfig#getExpandRoot()
	 */
	public boolean expandRoot() {
		return _expandRoot;
	}

	/**
	 * Returns true if the selected node should be expanded by default, otherwise false.
	 * 
	 * @see TreeViewConfig#getExpandSelected()
	 */
	public boolean expandSelectedNode() {
		return _expandSelectedNode;
	}

	/**
	 * Returns true if collapsing of a node should adjust selection.
	 * 
	 * @see TreeViewConfig#adjustSelectionWhenCollapsing()
	 */
	public boolean adjustSelectionWhenCollapsing() {
		return _adjustSelectionWhenCollapsing;
	}

	/**
	 * Sets value of {@link #isRootVisible()}.
	 */
	public void setRootVisible(boolean rootVisible) {
		_rootVisible = rootVisible;
	}

	/**
	 * Sets the value of {@link #expandRoot()}.
	 */
	public void setExpandRoot(boolean expandRoot) {
		_expandRoot = expandRoot;
	}

	/**
	 * Sets the value of {@link #expandSelectedNode()}.
	 */
	public void setExpandSelected(boolean expandSelected) {
		_expandSelectedNode = expandSelected;
	}

	/**
	 * Sets the value of {@link #adjustSelectionWhenCollapsing()}.
	 */
	public void adjustSelectionWhenCollapsing(boolean adjustSelectionWhenCollapsing) {
		_adjustSelectionWhenCollapsing = adjustSelectionWhenCollapsing;
	}

	/**
	 * {@link GridBuilder.GridHandler} for tree grids.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	protected class TreeGridHandler extends AbstractGridHandler<R> {

		GridTreeTableModel _treeModel;

		UserObjectIndex<GridTreeTableNode> _index;
		
		TreeTableField table;
		
		/**
		 * Creates a {@link TreeGridHandler}.
		 * 
		 * @param toRow
		 *        {@link Mapping} of application models to grid row objects.
		 * @param toModel
		 *        {@link Mapping} of grid row objects to application models.
		 * @param grid
		 *        The context component.
		 * @param tableConfiguration
		 *        The {@link TableConfiguration} for the table display.
		 * @param availableColumns
		 *        The columns to show.
		 */
		public TreeGridHandler(final Mapping<Object, ? extends R> toRow,
				final Mapping<? super R, ?> toModel,
				final GridComponent grid, TableConfiguration tableConfiguration,
				String[] availableColumns) {
			super(toRow, toModel, grid);
			
			_index = new UserObjectIndex<>();
			
			TreeBuilder<GridTreeTableNode> nodeFactory = new TreeBuilder<>() {
				@Override
				public GridTreeTableNode createNode(
						AbstractMutableTLTreeModel<GridTreeTableNode> model,
						GridTreeTableNode parent, Object userObject) {
					return new GridTreeTableNode(model, parent, userObject);
				}
				
				@Override
				public List<GridTreeTableNode> createChildList(GridTreeTableNode node) {
					ArrayList<GridTreeTableNode> result = new ArrayList<>();
					@SuppressWarnings("unchecked")
					Object rowModel = getGridRowModel((R) node.getBusinessObject());
					if (rowModel == null || isLeaf(grid, rowModel)) {
						return result;
					}
					for (Object childObject : getChildren(grid, rowModel)) {
						R childRow = toGridRow(childObject);
						
						GridTreeTableNode child = createNode(node.getModel(), node, childRow);
						result.add(child);
					}
					return result;
				}

				@Override
				public boolean isFinite() {
					return canExpandAll();
				}

				private R toGridRow(Object rowModel) {
					return toRow.map(rowModel);
				}
				
				private Object getGridRowModel(R gridRow) {
					return toModel.map(gridRow);
				}
			};

			TableDragSource dragSource = tableConfiguration.getDragSource();
			if (!(dragSource instanceof GridDragSource)) {
				tableConfiguration.setDragSource(new ProxyGridDragSource(dragSource, grid));
			}
			Object root = getRoot(grid);
			_treeModel =
				new GridTreeTableModel(grid, nodeFactory, toGridRow(root), Arrays.asList(availableColumns),
					tableConfiguration) {
				@Override
				protected void handleInitNode(GridTreeTableNode node) {
					super.handleInitNode(node);
					_index.handleInitNode(node);
				}
				
				@Override
				protected void handleRemoveNode(GridTreeTableNode subtreeRootParent, GridTreeTableNode node) {
					super.handleRemoveNode(subtreeRootParent, node);
					_index.handleRemoveNode(subtreeRootParent, node);
				}
				
				@Override
				protected void handleVisibleExpansionChange(GridTreeTableNode node, int sizeBefore) {
					Object channelSelection = grid.getSelected();
					
					super.handleVisibleExpansionChange(node, sizeBefore);
					
					if (!node.isExpanded() && node.isDisplayed()) {
						if (adjustSelectionWhenCollapsing()) {
							// Visible node was collapsed, check, whether this hides the current selection.
							if (channelSelection != null) {
								boolean selectedObjectIsInvisible = false;
								Set<Object> newSelection = new HashSet<>();
								
								for(Object selectedObject : CollectionUtil.asSet(channelSelection)) {
									Collection<? extends GridTreeTableNode> selectedRows =
											getTableRows(toGridRow(selectedObject));
									
									boolean allRowsCollapsed = allRowsAreCollapsed(node, selectedRows);
									
									if(!allRowsCollapsed) {
										newSelection.add(selectedObject);
									}

									selectedObjectIsInvisible = selectedObjectIsInvisible || allRowsCollapsed;
								}

								if (selectedObjectIsInvisible) {
									newSelection.add(findValidNodeModel(node));
								}
								grid.setSelected(newSelection);
							}
						}
					}
				}

				private Object findValidNodeModel(GridTreeTableNode node) {
					GridTreeTableNode gridNode = node;

					while (gridNode != null) {
						Object model = getGridModel(gridNode);

						if (ComponentUtil.isValid(model)) {
							return model;
						}
						gridNode = gridNode.getParent();
					}

					return null;
				}

				private boolean allRowsAreCollapsed(GridTreeTableNode node,	Collection<? extends GridTreeTableNode> rows) {
					findVisibleRow:
					for (GridTreeTableNode row : rows) {
						// Check, whether the selected row is still visible.
	
						GridTreeTableNode anchestor = row;
						while (true) {
							anchestor = anchestor.getParent();
							if (anchestor == null) {
									return false;
							}
	
							if (anchestor == node) {
								// Reached collapsed node.
									continue findVisibleRow;
							}
						}
					}

					return true;
				}

				@SuppressWarnings("unchecked")
				private Object getGridModel(GridTreeTableNode node) {
					return getGridRowModel((R) node.getBusinessObject());
				}

				
				@Override
				protected AccessContext doPrepareRows(Collection<?> accessedRows, List<String> accessedColumns) {
					if (accessedRows.size() < 2) {
						return NoPrepare.INSTANCE;
					}

					List<Object> accessedObjects = new ArrayList<>(accessedRows.size());
					for (Object node : accessedRows) {
						if (node instanceof GridTreeTableNode) {
							@SuppressWarnings("unchecked")
							R row = (R) ((GridTreeTableNode) node).getBusinessObject();
							Object obj = getGridRowModel(row);
							if (obj != null) {
								accessedObjects.add(obj);
							}
						}
					}

					TableConfiguration tableConfig = getTable().getTableConfiguration();

					Preloader preloader = new Preloader();
					for (String columnName : accessedColumns) {
						ColumnConfiguration column = tableConfig.getCol(columnName);
						column.getPreloadContribution().contribute(preloader);
					}

					return preloader.prepare(accessedObjects);
				}
			};
			_treeModel.setRootVisible(AbstractTreeGridBuilder.this.isRootVisible() && root != null);
			if (expandRoot()) {
				_treeModel.setExpanded(_treeModel.getRoot(), true);
			}

			table = FormFactory.newTreeTableField(GridComponent.FIELD_TABLE, grid.getConfigKey(), _treeModel);
		}

		/**
		 * The {@link GridTreeTableModel}.
		 * 
		 * @return Never null.
		 */
		protected GridTreeTableModel getTreeModel() {
			return _treeModel;
		}

		@Override
		public TableField getTableField() {
			return table;
		}

		@Override
		public boolean addNewRow(Object rowModel) {
			Iterable<? extends Object> parentModels = getParents(_grid, rowModel);
			boolean rowCreated = false;
			for (Object parentModel : parentModels) {
				rowCreated |= addToParent(toGridRow(parentModel), rowModel);
			}
			return rowCreated;
		}

		private boolean addToParent(R parentRow, Object rowModel) {
			boolean rowCreated = false;
			List<GridTreeTableNode> parentNodes = _index.getNodes(parentRow);

			if (CollectionUtilShared.isEmpty(parentNodes)) {
				addNewRow(parentRow);
				parentNodes = _index.getNodes(parentRow);
			}

			for (GridTreeTableNode parentNode : parentNodes) {
				if (!parentNode.isInitialized()) {
					// Parent not (yet) initialized and adding the new row
					// does not change the isLeaf property of the node, ignore.
					continue;
				}
				
				// Show new child.
				R row = toGridRow(rowModel);
				GridTreeTableNode newChild = parentNode.createChild(row);
				rowCreated |= newChild != null;
			}
			return rowCreated;
		}

		@Override
		public void removeRow(R rowObject) {
			for (GridTreeTableNode node : _index.getNodes(rowObject)) {
				removeNode(node);
			}
		}

		private void removeNode(GridTreeTableNode node) {
			GridTreeTableNode parent = node.getParent();
			if (parent == null) {
				handleNoParent(node);
			} else {
				parent.removeChild(parent.getIndex(node));
			}
		}

		private void handleNoParent(GridTreeTableNode node) {
			AbstractTreeTableModel<GridTreeTableNode> nodeModel = node.getModel();
			Object rowObject = node.getBusinessObject();
			if (nodeModel == null) {
				throw new IllegalArgumentException("Node '" + node + "' for row object '" + rowObject + "' from grid '"
					+ _grid + "' does not belong to any model.");
			}
			if (node != nodeModel.getRoot()) {
				throw new NullPointerException(
					"Node '" + node + "' for row object '" + rowObject + "' from grid '" + _grid
						+ "' has no parant in its model '" + nodeModel + "'.");
			}

			_grid.invalidate();
		}

		@Override
		protected void setSelection(SelectionModel selectionModel, Set<List<?>> selectedPaths) {
			Set<GridTreeTableNode> selectectedTreeNodes = formGroupsToTreeNodes(selectedPaths);

			SelectionUtil.setSelection(selectionModel, selectectedTreeNodes);

			for (GridTreeTableNode selectedNode : selectectedTreeNodes) {
				if (expandSelectedNode()) {
					TreeUIModelUtil.expandSelfAndParents(getTreeModel(), selectedNode);
				} else {
					TreeUIModelUtil.expandParents(getTreeModel(), selectedNode);
				}
			}
		}

		private Set<GridTreeTableNode> formGroupsToTreeNodes(Set<List<?>> paths) {
			Set<GridTreeTableNode> treeNodes = new HashSet<>();

			Map<R, GridTreeTableNode> nonMatchingPathNodes = Collections.emptyMap();
			for (List<?> path : paths) {
				List<R> gridRowPath = MappedList.create(this::toGridRow, path);
				R bo = CollectionUtil.getLast(gridRowPath);
				List<GridTreeTableNode> nodesForPath = _index.getNodes(bo);
				if (nodesForPath.isEmpty()) {
					continue;
				}
				GridTreeTableNode nodeWithDifferentPath = null;
				boolean noNodeFound = true;
				for (GridTreeTableNode node : nodesForPath) {
					if (TLTreeModelUtil.sameBusinessObjectPath(node, gridRowPath)) {
						treeNodes.add(node);
						noNodeFound = false;
					} else {
						nodeWithDifferentPath = node;
					}
				}
				// There are nodes for the business object, but all have different paths.
				if (noNodeFound && nodeWithDifferentPath != null) {
					if (nonMatchingPathNodes.isEmpty()) {
						nonMatchingPathNodes = new HashMap<>();
					}
					nonMatchingPathNodes.put(bo, nodeWithDifferentPath);
				}
			}

			if (!nonMatchingPathNodes.isEmpty()) {
				treeNodes.stream().map(TLTreeNode::getBusinessObject).forEach(nonMatchingPathNodes.keySet()::remove);
				// For the actual selected business objects, the paths are all invalid. Select other
				// nodes which represent the same selection.
				treeNodes.addAll(nonMatchingPathNodes.values());
			}
			return treeNodes;
		}

		@Override
		public void updateTableRow(R rowObject, boolean structureChange) {
			super.updateTableRow(rowObject, structureChange);
			
			if (structureChange) {
				updateRowModel(getGridRowModel(rowObject));
			}
		}

		void updateRowModels(Collection<? extends Object> rowModels) {
			for (Object rowModel : rowModels) {
				updateRowModel(rowModel);
			}
		}

		void updateRowModel(Object rowModel) {
			if (supportsRow(_grid, rowModel)) {
				updateNewParents(rowModel);

				for (GridTreeTableNode node : getTableRows(toGridRow(rowModel))) {
					if (node.isInitialized()) {
						node.updateNodeProperties();

						if (node.isDisplayed()) {
							ClientDisplayData clientDisplayData = table.getViewModel().getClientDisplayData();
							ViewportState viewportState = clientDisplayData.getViewportState();
							RowIndexAnchor oldRowAnchor = viewportState.getRowAnchor();
							int topRow = oldRowAnchor.getIndex();
							int offset = oldRowAnchor.getIndexPixelOffset();

							int changedRow = node.getPosition();
							int sizeBefore = node.getVisibleSubtreeSize();

							boolean scrollUpdateRequired = changedRow + sizeBefore <= topRow;

							updateChildren(node);
							updateOldParents(node);

							if (scrollUpdateRequired) {
								int sizeAfter = node.getVisibleSubtreeSize();

								int delta = sizeAfter - sizeBefore;
								topRow += delta;
								if (topRow < 0) {
									topRow = 0;
									offset = 0;
								}
								viewportState.setRowAnchor(RowIndexAnchor.create(topRow, offset));
							}
						} else {
							updateChildren(node);
							updateOldParents(node);
						}
					}
				}
			}
		}

		private void updateNewParents(Object rowObject) {
			for (Object parentRowObject : getParents(_grid, rowObject)) {
				Collection<? extends GridTreeTableNode> nodes = getTableRows(toGridRow(parentRowObject));

				for (GridTreeTableNode node : nodes) {
					updateChildren(node);
				}
			}
		}

		private void updateOldParents(GridTreeTableNode node) {
			GridTreeTableNode oldParent = node.getParent();

			if (oldParent != null) {
				updateChildren(oldParent);
			}
		}

		private void updateChildren(GridTreeTableNode node) {
			Map<Object, GridTreeTableNode> oldChildrenModels = new HashMap<>();
			for (GridTreeTableNode child : node.getChildren()) {
				Object childModel = getGridRowModel(getGridRow(child));

				// Note: The "new object" has model null.
				if (childModel != null) {
					oldChildrenModels.put(childModel, child);
				}
			}
			R parentRow = getGridRow(node);
			Iterable<?> newChildrenModels = getChildren(_grid, getGridRowModel(parentRow));

			List<Object> addedChildrenModels = new ArrayList<>();
			for (Object newChildModel : newChildrenModels) {
				if (oldChildrenModels.remove(newChildModel) == null) {
					addedChildrenModels.add(newChildModel);
				}
			}

			for (GridTreeTableNode oldChildNode : oldChildrenModels.values()) {
				removeNode(oldChildNode);
			}

			for (Object addedChildModel : addedChildrenModels) {
				addToParent(parentRow, addedChildModel);
			}
		}

		@Override
		public Object createRow(Object contextModel, ContextPosition position, Object newRowModel) {
			List<GridTreeTableNode> contextParents = _index.getNodes(toGridRow(contextModel));
			switch (contextParents.size()) {
				case 0:
					if (contextModel == null) {
						throw new TopLogicException(I18NConstants.NO_CONTEXT_OBJECT_FOR_ROW);
					} else {
						throw new TopLogicException(
							I18NConstants.CONTEXT_NOT_PART_OF_TABLE__CONTEXT.fill(contextModel));
					}
				case 1:
					// expected
					break;
				default:
					throw new TopLogicException(I18NConstants.CONTEXT_NOT_UNIQUE__CONTEXT.fill(contextModel));

			}
			
			GridTreeTableNode parentNode = contextParents.get(0);
			parentNode.setExpanded(true);
			
			IndexPosition insertPosition;
			switch (position.getStrategy()) {
			case START: {
				insertPosition = IndexPosition.START;
				break;
			}
			case END: {
				insertPosition = IndexPosition.END;
				break;
			}
			case AFTER: {
				int contextIndex = getContextIndex(parentNode, position);
				insertPosition = IndexPosition.position(PositionStrategy.AFTER, contextIndex);
				break;
			}
			case BEFORE: {
				int contextIndex = getContextIndex(parentNode, position);
				insertPosition = IndexPosition.position(PositionStrategy.BEFORE, contextIndex);
				break;
			}
			case AUTO: {
				insertPosition = IndexPosition.AUTO;
				break;
			}
			default:
				throw new UnreachableAssertion("No such position: " + position.getStrategy());
			}
			
			return parentNode.createChild(insertPosition, toGridRow(newRowModel));
		}

		private int getContextIndex(GridTreeTableNode parentNode, ContextPosition position) {
			int contextIndex = parentNode.getIndex(getFirstTableRow(toGridRow(position.getContext())));
			if (contextIndex < 0) {
				throw new IllegalArgumentException(
					"Position context object not found in children of parent model: " + parentNode.getBusinessObject());
			}
			return contextIndex;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public R getGridRow(Object tableRow) {
			return (R) ((GridTreeTableNode) tableRow).getBusinessObject();
		}
		
		@Override
		public Object getParentRow(Object tableRow) {
			return ((GridTreeTableNode) tableRow).getParent();
		}

		@Override
		public Collection<? extends GridTreeTableNode> getTableRows(R gridRow) {
			return _index.getNodes(gridRow);
		}
		
		@Override
		public Object getFirstTableRow(R gridRow) {
			List<GridTreeTableNode> rows = _index.getNodes(gridRow);
			if (rows == null || rows.isEmpty()) {
				return null;
			}
			return rows.get(0);
		}

		@Override
		protected Filter<Object> getSelectionFilter() {
			return object -> {
				if (_grid.getRowGroup(object) != null) {
					List<GridTreeTableNode> nodes = _index.getNodes(toGridRow(object));

					if (nodes.isEmpty()) {
						// No row found for the selected object.
						return false;
					} else {
						for (GridTreeTableNode selectedNode : nodes) {
							if (!isSelectableNode(selectedNode)) {
								return false;
							}
						}

						return true;
					}
				}

				return false;
			};
		}

		private boolean isSelectableNode(GridTreeTableNode node) {
			return node != null && _grid.getSelectionModel().isSelectable(node)
				&& (_rootVisible || node != getTreeModel().getRoot());
		}

		@Override
		public List<R> sortGridRows(Collection<R> gridRows) {
			List<List<GridTreeTableNode>> treePaths = getTreePaths(gridRows);
			Collections.sort(treePaths, new TreePathComparator(getTableField().getViewModel().getRowComparator()));
			return getGridRows(treePaths);
		}

		private List<List<GridTreeTableNode>> getTreePaths(Collection<R> rowObjects) {
			List<List<GridTreeTableNode>> sortedTreePaths = new ArrayList<>();
			for (R rowObject : rowObjects) {
				for (GridTreeTableNode tableRow : getTableRows(rowObject)) {
					List<GridTreeTableNode> pathToRoot = tableRow.getModel().createPathToRoot(tableRow);
					Collections.reverse(pathToRoot);
					sortedTreePaths.add(pathToRoot);
				}
			}
			return sortedTreePaths;
		}

		private List<R> getGridRows(List<List<GridTreeTableNode>> sortedTreePaths) {
			List<R> sortedSelection = new ArrayList<>();
			for (List<GridTreeTableNode> treePath : sortedTreePaths) {
				R gridRow = getGridRow(treePath.get(treePath.size() - 1));
				sortedSelection.add(gridRow);
			}
			return sortedSelection;
		}

		@Override
		public Collection<?> getExpansionState() {
			return TreeUIModelUtil.getExpansionUserModel(getTreeModel(), new GridRowModelMapping());
		}

		@Override
		public void setExpansionState(Collection<?> expansionState) {
			/* #22798: Fetch view model here. During creation of the table tree model, there are no
			 * filters available. Therefore no node is not visible due to filtering. The filter are
			 * applied e.g. when the TableViewModel is created. If the expansion state is applied
			 * and the filters are not applied, updates for non visible nodes are created. */
			getTableField().getViewModel();
			TreeUIModelUtil.setExpansionUserModel(expansionState, getTreeModel(), new GridRowModelMapping());
		}

		final class GridRowModelMapping implements Mapping<TLTreeNode<?>, Object> {
			@Override
			public Object map(TLTreeNode<?> input) {
				return getGridRowModel(getGridRow(input));
			}
		}
	}
	
	private static class TreePathComparator extends ListComparator<GridTreeTableNode> {

		Comparator<Object> _nodeComparator;

		TreePathComparator(Comparator<Object> nodeComparator) {
			_nodeComparator = nodeComparator;
		}

		@Override
		protected int elementCompare(GridTreeTableNode o1, GridTreeTableNode o2) {
			return _nodeComparator.compare(o1, o2);
		}

	}

	/**
	 * Retrieve the {@link GridTreeTableModel} from the given {@link GridComponent}.
	 */
	public static GridTreeTableModel getTreeModel(LayoutComponent component) {
		return ((AbstractTreeGridBuilder<?>.TreeGridHandler) ((GridComponent) component)._handler).getTreeModel();
	}
	
	/**
	 * Returns a view to the tree build by this {@link AbstractTreeGridBuilder} and the given
	 * {@link GridComponent}.
	 */
	protected abstract TreeView<Object> asTreeView(GridComponent grid);

	@Override
	public boolean handleTLObjectCreations(GridComponent grid, Stream<? extends TLObject> creations) {
		boolean changes = false;
		AbstractTreeGridBuilder<FormGroup>.TreeGridHandler handler = getTreeGridHandler(grid);

		for (TLObject newObject : toIterable(creations)) {
			changes = true;
			handler.updateRowModels(getNodesToUpdate(grid, newObject));

			FormGroup rowGroup = grid.getRowGroup(newObject);
			if (rowGroup == null || handler.getTableRows(rowGroup).isEmpty()) {
				handler.updateRowModel(newObject);
			}
		}
		return changes;
	}

	@Override
	public void receiveModelChangedEvent(GridComponent grid, Object model) {
		getTreeGridHandler(grid).updateRowModels(getNodesToUpdate(grid, model));
	}

	private AbstractTreeGridBuilder<FormGroup>.TreeGridHandler getTreeGridHandler(GridComponent grid) {
		return (AbstractTreeGridBuilder<FormGroup>.TreeGridHandler) grid.getHandler();
	}

	@Override
	public Collection<? extends Object> getParentsForRow(GridComponent grid, Object row) {
		return getParents(grid, row);
	}

}
