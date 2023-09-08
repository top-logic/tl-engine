/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.tree.TreeData;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.TableDeclaration;
import com.top_logic.layout.tree.renderer.TreeTableRenderer;

/**
 * {@link AssertionTreeNode} based on a {@link TreeData}
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AssertionTreeNodeImpl<N> implements AssertionTreeNode<N> {

	private final TreeData _data;

	private final N _treeNode;

	/**
	 * Creates a new {@link AssertionTreeNodeImpl}.
	 */
	public AssertionTreeNodeImpl(TreeData data, N treeNode) {
		_data = data;
		_treeNode = treeNode;
	}

	@Override
	public boolean isSelected() {
		return _data.getSelectionModel().isSelected(_treeNode);
	}

	@Override
	public N getNode() {
		return _treeNode;
	}

	@Override
	public Object getContext() {
		return _data;
	}

	@Override
	public TreeUIModel<N> treeModel() {
		return _data.getTreeModel();
	}

	@Override
	public String getNodeLabel() {
		TreeRenderer renderer = _data.getTreeRenderer();
		if (renderer instanceof TreeTableRenderer) {
			/* Quirks in TreeTable renderer: User object in the tree model is the form group of the
			 * table row. The label of the actual node object is taken directly. */
			TableDeclaration tableDeclaration = ((TreeTableRenderer) renderer).getTableDeclaration();
			ResourceProvider resourceProvider = tableDeclaration.getResourceProvider();
			return resourceProvider.getLabel(_treeNode);
		}
		return MetaLabelProvider.INSTANCE.getLabel(treeModel().getUserObject(_treeNode));
	}

}

