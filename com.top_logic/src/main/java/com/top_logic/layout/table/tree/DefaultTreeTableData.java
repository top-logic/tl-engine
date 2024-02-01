/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.layout.structure.DefaultExpandable;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.DefaultTableData;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.toolbar.DefaultToolBar;
import com.top_logic.layout.tree.TreeDataListener;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.dnd.DefaultTreeDrag;
import com.top_logic.layout.tree.dnd.NoTreeDrop;
import com.top_logic.layout.tree.dnd.TreeDragSource;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.DefaultTreeRenderer;

/**
 * Default holder of the tree table model.
 * 
 * @see TreeTableComponent
 * @see TreeTableModel
 * @see AbstractTreeTableModel
 * @see TableData
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultTreeTableData extends DefaultTableData implements TreeTableData {

	@Inspectable
	private TreeUIModel<?> _treeModel;

	private TreeDragSource _dragSource;

	private List<TreeDropTarget> _dropTargets;

	/**
	 * Creates a default {@link TreeTableData}.
	 */
	public DefaultTreeTableData(Maybe<? extends TreeTableDataOwner> owner, ConfigKey configKey,
			boolean updateSelectionOnTableEvents) {
		super(owner, configKey, updateSelectionOnTableEvents);
	}

	@Override
	public Maybe<? extends TreeTableDataOwner> getOwner() {
		return (Maybe<? extends TreeTableDataOwner>) super.getOwner();
	}

	@Override
	public AbstractTreeTableModel<?> getTree() {
		TreeTableModel tableModel = (TreeTableModel) getTableModel();

		return (AbstractTreeTableModel<?>) tableModel.getTreeModel();
	}

	@Override
	public void setTree(AbstractTreeTableModel<?> treeModel) {
		setTableModel(treeModel.getTable());

		treeModel.setViewModel(() -> getViewModel());

		_treeModel = treeModel;
	}

	/**
	 * Creates a {@link TreeTableData} for the given {@link TreeUIModel}.
	 */
	public static TreeTableData createTreeTableData(Maybe<? extends TreeTableDataOwner> owner,
			AbstractTreeTableModel<?> model,
			ConfigKey key, boolean updateSelectionOnTableEvents) {
		DefaultTreeTableData result = new DefaultTreeTableData(owner, key, updateSelectionOnTableEvents);

		result.setTree(model);
		result.setToolBar(new DefaultToolBar(result, new DefaultExpandable()));

		return result;
	}

	/**
	 * Creates an instance of this class to be used as implementation for the specified object.
	 * 
	 * @param proxy
	 *        the object to create a implementation instance for
	 * @param owner
	 *        the {@link TreeTableData#getOwner() owner} of the new {@link TreeTableData}.
	 * @param key
	 *        The key to store personal table configuration with.
	 * @param updateSelectionOnTableEvents
	 *        True if the {@link TableData} updates his selection when table model events are sent.
	 * 
	 * @return the newly created implementation instance for the specified object
	 */
	public static TreeTableData createTreeTableDataImplementation(TreeTableData proxy,
			Maybe<? extends TreeTableDataOwner> owner, ConfigKey key, boolean updateSelectionOnTableEvents) {
		return new TreeTableDataImplementation(proxy, owner, key, updateSelectionOnTableEvents);
	}

	@Override
	public TreeUIModel<?> getTreeModel() {
		return _treeModel;
	}

	@Override
	public TreeRenderer getTreeRenderer() {
		return DefaultTreeRenderer.INSTANCE;
	}

	@Override
	public boolean addTreeDataListener(TreeDataListener listener) {
		return false; // Immutable, ignore.
	}

	@Override
	public boolean removeTreeDataListener(TreeDataListener listener) {
		return false; // Immutable, ignore.
	}

	@Override
	public List<TreeDropTarget> getTreeDropTargets() {
		if (!CollectionUtilShared.isEmpty(_dropTargets)) {
			return _dropTargets;
		} else {
			return Arrays.asList(NoTreeDrop.INSTANCE);
		}
	}

	@Override
	public TreeDragSource getTreeDragSource() {
		if (_dragSource != null) {
			return _dragSource;
		} else {
			return DefaultTreeDrag.INSTANCE;
		}
	}

	@Override
	public void setTreeDropTargets(List<TreeDropTarget> dropTargets) {
		_dropTargets = CollectionUtilShared.unmodifiableList(dropTargets);
	}

}
