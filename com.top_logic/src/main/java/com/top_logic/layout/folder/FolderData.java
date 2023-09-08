/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.common.folder.model.FolderFilter;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LayoutContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.SingleSelectionModelProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableDataOwner;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.tree.TreeUpdateAccumulator;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.MutableBreadcrumbData;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeNode;
import com.top_logic.layout.tree.model.FilterTreeModel;
import com.top_logic.layout.tree.model.LazyMappedTreeModel;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TLTreeModelUtil;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeBuilder;
import com.top_logic.layout.tree.model.TreeModelEvent;
import com.top_logic.layout.tree.model.TreeModelListener;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;
import com.top_logic.util.ToBeValidated;

/**
 * The class {@link FolderData} is a holder for the data need by the
 * {@link FolderControl}. Moreover it reacts on changes in the displayed tree
 * and translates them into changes of the displayed table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FolderData implements TableDataOwner, BreadcrumbDataOwner,
		SingleSelectionModelProvider, SingleSelectionListener, TreeModelListener, ToBeValidated, SelectionModelOwner {

	private final LazyMappedTreeModel<FolderNode> treeModel;
	private final TableData tableData;
	private final BreadcrumbData breadData;

	private final List<FolderListener> listeners = new ArrayList<>();
	private final DefaultSingleSelectionModel selectionModel;

	/**
	 * collects all changes in the {@link #treeModel}. During {@link #validate(DisplayContext)} the
	 * changes will be translated to changes in the displayed table.
	 */
	private final TreeUpdateAccumulator<TLTreeModel<FolderNode>> accumulator;

	private boolean _tableValid = true;

	private final FolderDataOwner _owner;

	/**
	 * @param tableModel
	 *        The rows set in the given table model will be recomputed in the constructor and
	 *        therefore don't matter.
	 */
	public FolderData(FolderDataOwner owner, Object rootUserObject, TreeBuilder<FolderNode> treeBuilder,
			EditableRowTableModel tableModel, ConfigKey configKey) {
		_owner = owner;
		this.treeModel = new LazyMappedTreeModel<>(rootUserObject, treeBuilder);
		this.tableData = DefaultTableData.createTableData(this, tableModel, configKey);

		this.selectionModel = new DefaultSingleSelectionModel(this);
		setSingleSelection(treeModel.getRoot());
		updateTable();

		this.accumulator = new TreeUpdateAccumulator<>();

		TLTreeModel<FolderNode> breadcrumbTree = new FilterTreeModel<>(treeModel, FolderFilter.INSTANCE);
		this.breadData = new MutableBreadcrumbData(breadcrumbTree, this.selectionModel, this.selectionModel, this);
		internalAttach();
	}

	/**
	 * The {@link FolderDataOwner} of this {@link FolderData}.
	 */
	public FolderDataOwner getOwner() {
		return _owner;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	/**
	 * Show interest in this {@link FolderData}.
	 * 
	 * @param listener
	 *        Only a handle to know when the last observer is gone.
	 */
	public void addFolderListener(FolderListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	/**
	 * @see #addFolderListener(FolderListener)
	 */
	public void removeFolderListener(FolderListener listener) {
		listeners.remove(listener);
	}

	protected void internalAttach() {
		assert !accumulator.hasUpdates() : "Detach removes all updates and no other has access to the accumulator.";

		getTreeModelInternal().addTreeModelListener(this);
		selectionModel.addSingleSelectionListener(this);
	}

	@Override
	public void validate(DisplayContext context) {
		TableModel tableModel = getTableData().getTableModel();
		FolderNode currentFolder = getSingleSelection();

		if (!this.getTreeModelInternal().containsNode(currentFolder)) {
			// The current folder node was deleted or replaced. Find a
			// corresponding folder in the updated model.

			List<Object> pathToRoot = TLTreeModelUtil.createPathToRootUserObject(currentFolder);
			pathToRoot.remove(pathToRoot.size() - 1);
			Collections.reverse(pathToRoot);

			currentFolder = TLTreeModelUtil.findNode(this.getTreeModelInternal().getRoot(), pathToRoot, true);

			setSingleSelection(currentFolder);
		} else {
			Collection<NodeUpdate> theUpdates = this.accumulator.getUpdates();
			for (NodeUpdate theUpdate : theUpdates) {
				FolderNode updatedNode = (FolderNode) theUpdate.getNode();

				if (updatedNode == currentFolder) {
					// The contents of the folder must be re-fetched.
					_tableValid = false;
				} else {
					Object theParent = this.getTreeModelInternal().getParent(updatedNode);

					if (theParent == currentFolder) {
						// One displayed line must be redrawn.
						int theRow = tableModel.getRowOfObject(updatedNode);

						if (theRow > -1) {
							tableModel.updateRows(theRow, theRow);
						}
					}
				}
			}
		}

		if (!_tableValid) {
			updateTable();
		}

		this.accumulator.clear();
	}

	private FolderNode getSingleSelection() {
		return (FolderNode) this.selectionModel.getSingleSelection();
	}

	private void setSingleSelection(FolderNode node) {
		this.selectionModel.setSingleSelection(node);
	}

	@Override
	public void handleTreeUIModelEvent(TreeModelEvent evt) {
		assert evt.getModel() == getTreeModelInternal() : "No one must attach this "
			+ TreeModelListener.class.getName() + " externally";

		TLTreeModel<FolderNode> eventSource = getTreeModelInternal();
		FolderNode changedNode = (FolderNode) evt.getNode();
		switch (evt.getType()) {
		case TreeModelEvent.NODE_CHANGED: {
			this.accumulator.invalidateNode(eventSource, changedNode);
				notifyModelChanged();
			break;
		}

			case TreeModelEvent.AFTER_STRUCTURE_CHANGE: {
				FolderNode currentSelection = getSingleSelection();
				if (currentSelection != null) {
					final FolderNode bestSelection =
						TLTreeModelUtil.findBestMatch(eventSource, currentSelection, changedNode);
					setSingleSelection(bestSelection);
				}
				break;
			}

			case TreeModelEvent.BEFORE_STRUCTURE_CHANGE:
		case TreeModelEvent.BEFORE_COLLAPSE:
		case TreeModelEvent.BEFORE_EXPAND: {
			this.accumulator.invalidateSubtree(eventSource, changedNode);
				notifyModelChanged();
			break;
		}

		case TreeModelEvent.AFTER_NODE_ADD:
			this.accumulator.notifyAdd(changedNode);
				accumulator.invalidateSubtree(eventSource, getParentOrRoot(eventSource, changedNode));
				notifyModelChanged();
			break;

		case TreeModelEvent.BEFORE_NODE_REMOVE: {
				FolderNode parentInRoot = getParentOrRoot(eventSource, changedNode);
				setSingleSelection(parentInRoot);
				accumulator.invalidateSubtree(eventSource, parentInRoot);
				notifyModelChanged();
			break;
		}
		}

	}

	private <N extends AbstractMutableTLTreeNode<N>> N getParentOrRoot(TLTreeModel<N> model, N changedNode) {
		N parent = model.getParent(changedNode);
		if (parent == null) {
			return model.getRoot();
		} else {
			return parent;
		}
	}

	@Override
	public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject, Object selectedObject) {
		_tableValid = false;
		notifyModelChanged();
	}

	private void notifyModelChanged() {
		LayoutContext context = DefaultDisplayContext.getDisplayContext().getLayoutContext();
		if (context.isInCommandPhase()) {
			context.notifyInvalid(this);
		} else {
			assert false : "Model changes only in command phase.";
		}

	}

	/**
	 * updates the rows in the displayed table depending on the node which is
	 * currently selected: The new rows are the children of the selection in the
	 * handled {@link #treeModel}.
	 * 
	 */
	private void updateTable() {
		FolderNode treeSelection = getSingleSelection();
		List<?> theChildren = computeRows(this.getTreeModelInternal(), treeSelection);
		((EditableRowTableModel) this.getTableData().getTableModel()).setRowObjects(theChildren);

		_tableValid = true;
	}

	public static <N extends TLTreeNode<N>> List<? extends N> computeRows(TLTreeModel<N> treeModel, N treeSelection) {
		List<? extends N> theChildren;
		if (treeSelection != null) {
			theChildren = treeModel.getChildren(treeSelection);
		} else {
			theChildren = Collections.emptyList();
		}
		return theChildren;
	}

	/**
	 * @see LazyMappedTreeModel#updateUserObject(Object)
	 */
	public boolean updateUserObject(Object userObject) {
		return getTreeModelInternal().updateUserObject(userObject);
	}

	/**
	 * This method exists in addition to {@link #getTreeModel()} to hide the implementation detail
	 * what implementation of the {@link TLTreeModel} is used.
	 */
	private LazyMappedTreeModel<FolderNode> getTreeModelInternal() {
		return treeModel;
	}

	/**
	 * Returns the tree model this {@link FolderData} is constructed with.
	 */
	public final TLTreeModel getTreeModel() {
		return getTreeModelInternal();
	}

	@Override
	public TableData getTableData() {
		return tableData;
	}

	@Override
	public BreadcrumbData getBreadcrumbData() {
		return breadData;
	}

	@Override
	public SingleSelectionModel getSingleSelectionModel() {
		return selectionModel;
	}

	@Override
	public SelectionModel getSelectionModel() {
		return selectionModel;
	}

}
