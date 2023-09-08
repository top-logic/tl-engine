/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.list.DefaultListRenderer;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.mig.html.ReferencedSelectionModel;
import com.top_logic.mig.html.SelectionModel;

/**
 * A tree renderer, which marks up non-selectable nodes and nodes in referenced
 * selections.
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class MarkupConfigurableTreeRenderer extends ConfigurableTreeRenderer {
	
	/** Constant of CSS class of tree nodes, that are selected by reference */
	public static final String TREE_NODE_REFERENCED_SELECTED_CSS_CLASS = "treeNodeReferencedSelected";
	
	/**
	 * Create a new standard MarkupConfigurableTreeRenderer
	 */
	public MarkupConfigurableTreeRenderer(ResourceProvider resourceProvider) {
		this(DefaultTreeRenderer.CONTROL_TAG, DefaultTreeRenderer.NODE_TAG, new ReferencedSelectionContentRenderer(
			DefaultTreeImageProvider.INSTANCE, resourceProvider));
	}
	
	/**
	 * Create a new customized MarkupConfigurableTreeRenderer
	 * 
	 * @param contentTag - the node content enclosing tag for rendering to use
	 * @param nodeTag - the node tag for rendering to use
	 * @param contentRenderer - the content renderer to use
	 */
	public MarkupConfigurableTreeRenderer(String contentTag, String nodeTag, TreeContentRenderer contentRenderer) {
		super(contentTag, nodeTag, contentRenderer);
	}
	
	
	/**
	 * Determine markup css classes for a single node
	 */
	@Override
	protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
		super.writeNodeClassesContent(out, nodeContext);
	    SelectionModel selectionModel = nodeContext.getTree().getSelectionModel();
	    
		if (!selectionModel.isSelectable(nodeContext.currentNode())) {
			out.append(DefaultListRenderer.FIXED_CSS_CLASS);
		}

	    // Determine, if the model is multi-markup-capable
	    if(selectionModel instanceof ReferencedSelectionModel) {
	    	ReferencedSelectionModel referencedSelectionModel = (ReferencedSelectionModel) selectionModel;
			if(referencedSelectionModel.isReferencedSelection(nodeContext.currentNode())) {
				out.append(TREE_NODE_REFERENCED_SELECTED_CSS_CLASS);
			} 
		}
	}
}
