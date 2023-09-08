/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.List;

/**
 * Default {@link TreeUIModel} adapter for {@link TLTreeModel}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultStructureTreeUIModel<N> extends AbstractStructureTreeUIModel<N> {

	private final boolean rootVisible;
	
	/**
	 * Creates a {@link DefaultStructureTreeUIModel} with a visible root node.
	 *
	 * @param applicationModel See {@link TreeModelAdapter#TreeModelAdapter(TreeModelBase)}
	 */
	public DefaultStructureTreeUIModel(TLTreeModel<N> applicationModel) {
		this(applicationModel, /* rootVisible */ true);
	}
	
	/**
	 * Creates a {@link DefaultStructureTreeUIModel}.
	 *
	 * @param applicationModel See {@link TreeModelAdapter#TreeModelAdapter(TreeModelBase)}
	 * @param rootVisible See {@link #isRootVisible()}.
	 */
	public DefaultStructureTreeUIModel(TLTreeModel<N> applicationModel, boolean rootVisible) {
		super(applicationModel);
		
		this.rootVisible      = rootVisible;
	}
	
	@Override
	public boolean isRootVisible() {
		return rootVisible;
	}

	/**
	 * Expand or collapse all nodes.
	 * 
	 * @param expanded Whether to expand. 
	 */
	public void setExpandAll(boolean expanded) {
		TLTreeModel<N> baseModel = getTreeModelImpl();
		N root = baseModel.getRoot();
	    if (isRootVisible()) {
	        setExpandAll(baseModel, root, expanded);
	    } else {
			List<? extends N> children = baseModel.getChildren(root);
	        for (int i=0, n=children.size(); i < n; i++) {
	            setExpandAll(baseModel, children.get(i), expanded);
	        }
	    }
	}

	private void setExpandAll(TLTreeModel<N> model, N node, boolean expanded) {
	    setExpanded(node, expanded);
		List<? extends N> children = model.getChildren(node);
        for (int i=0, n=children.size(); i < n; i++) {
            setExpandAll(model, children.get(i), expanded);
        }
	}
	

}
