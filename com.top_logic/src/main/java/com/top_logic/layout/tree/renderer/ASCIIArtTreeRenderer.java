/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;

/**
 * {@link TreeRenderer} that creates ASCII art trees.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author Klaus Halfmann
 */
public class ASCIIArtTreeRenderer extends TreeRenderer {
	
	class ASCIIArtTreeContentRenderer extends TreeContentRenderer {

		@Override
		public ResourceProvider getResourceProvider() {
			throw new UnsupportedOperationException("The class '" + this.getClass().getSimpleName()
				+ "' does not support the Method 'getResourceProvider'.");
		}

		@Override
		public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext)
				throws IOException {
			ASCIIArtTreeRenderer.this.internalWriteNodeContent(writer, nodeContext);
		}
	}

	private final ASCIIArtTreeContentRenderer _contentRenderer = new ASCIIArtTreeContentRenderer();

	@Override
	protected String getControlTag(TreeControl control) {
		return DIV;
	}

	@Override
	protected String getNodeTag() {
		return PRE;
	}

	void internalWriteNodeContent(TagWriter out, NodeContext nodeContext) throws IOException {
		
		// Write prefix.
		for (int n = 0, cnt = nodeContext.getSize() - 1; n < cnt; n++) {
			if ((nodeContext.getNodePosition(n) & NodeContext.LAST_NODE) != 0) {
				out.writeText("       ");
			} else {
				out.writeText("|      ");
			}
		}
		
		int nodePosition = nodeContext.currentNodePosition();
		
		if ((nodePosition & NodeContext.LAST_NODE) != 0) {
			out.writeText("+-");
		} else {
			out.writeText("|-");
		}
		
		Object node = nodeContext.currentNode();
		
		TreeControl tree = nodeContext.getTree();
		if (! tree.getModel().isLeaf(node)) {
			boolean isExpanded = tree.getModel().isExpanded(node);
			
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, "#");
			out.writeAttribute(CLASS_ATTR, TreeControl.NODE_TOGGLE_BUTTON_CSS);
			out.endBeginTag();
			
			if (isExpanded) {
				out.writeText("[-]");
			} else {
				out.writeText("[+]");
			}
			
			out.endTag(ANCHOR);
			
			out.writeText("-");
		}
		
		out.writeText(" ");
		
		boolean isSelected = tree.getSelectionModel().isSelected(node);
		if (isSelected) {
			out.writeText(">>> ");
		} else {
			out.beginBeginTag(ANCHOR);
			out.writeAttribute(HREF_ATTR, "#");
			out.writeAttribute(CLASS_ATTR, TreeControl.SELECTABLE_NODE_CSS);
			out.endBeginTag();
		}
		
		String nodeText;
		if (node != null) {
			nodeText = node.toString();
		} else {
			nodeText = "no userObject";
		}
		out.writeText(nodeText);
		
		
		if (isSelected) {
			out.writeText(" >>>");
		} else {
			out.endTag(ANCHOR);
		}
		
		out.writeText("\n");
		
		/*  
		 * +-[-]- Root                       // Root node (expanded)
		 *        |-[+]- Node 1              // Collapsed non-leaf node 
		 *        |-[-]- >>> Node 2 <<<      // Selected node
		 *        |      |- Leaf 1           // Leaf node.
		 *        |      |- Leaf 2  
		 *        |      +- Leaf 3  
		 *        +-[+]- Node 3  
		 */
	}

	@Override
	public TreeContentRenderer getTreeContentRenderer() {
		return _contentRenderer;
	}

}

