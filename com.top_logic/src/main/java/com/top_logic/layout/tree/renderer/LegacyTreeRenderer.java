/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.FragmentInsertion;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.RangeReplacement;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Old version of {@link TreeRenderer} using range updates.
 * 
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public abstract class LegacyTreeRenderer extends TreeRenderer {
	
    public static final String TREE_CONTROL_CSS_CLASS = "treeBody";
    
	/**
	 * Class identifying tree node.
	 * 
	 * <p>
	 * This constant is also used in "inspect.js".
	 * </p>
	 */
	public static final String TREE_NODE_CSS_CLASS = "treeNode";

	public static final String TREE_NODE_SELECTED_CSS_CLASS = "treeSelected";
	
	private Renderer<NodeContext> nodeRendererAdaptar = new Renderer<>() {
		
		@Override
		public void write(DisplayContext context, TagWriter out, NodeContext value) throws IOException {
			internalWriteNode(context, out, value);
		}
	};

	/**
	 * Returns the {@link TreeContentRenderer} of this {@link LegacyTreeRenderer}.
	 */
	@Override
	public abstract TreeContentRenderer getTreeContentRenderer();

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TreeControl control)
			throws IOException {
		out.writeAttribute(ID_ATTR, control.getID());
		writeCssClass(out);
		out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, "true");
	}

	/**
	 * Writes the {@link HTMLConstants#CLASS_ATTR} of the control.
	 * 
	 * <p>
	 * Call {@link #appendControlClasses(TagWriter)} to append additional CSS classes.
	 * </p>
	 * 
	 * @see #appendControlClasses(TagWriter)
	 */
	protected void writeCssClass(TagWriter out) throws IOException {
		out.beginCssClasses();
		appendControlClasses(out);
		out.endCssClasses();
	}

	/**
	 * Appends the CSS classes for the control tag.
	 * 
	 * @see HTMLUtil#appendCSSClass(Appendable, String)
	 */
	protected void appendControlClasses(TagWriter out) throws IOException {
		HTMLUtil.appendCSSClass(out, TREE_CONTROL_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, AbstractControlBase.IS_CONTROL_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, AbstractControlBase.CAN_INSPECT_CSS_CLASS);
	}
    
    @Override
	protected void writeControlContents(DisplayContext context, TagWriter out, TreeControl control) throws IOException {
		final TreeControl tree = control;
		writeTitle(context, out, tree);
		
    	NodeContext nodeContext = new NodeContext(tree);
    	writeTreeContents(context, out, nodeContext);
    }
    
	/**
	 * Renders the whole tree in the given {@link NodeContext} beginning with
	 * the {@link TLTreeModel#getRoot() root node}.
	 * 
	 * @param nodeContext
	 *        the context to get the tree from.
	 */
	@Override
	protected void writeTreeContents(DisplayContext context, TagWriter out, NodeContext nodeContext) throws IOException {
		final TreeControl tree = nodeContext.getTree();
		Object rootNode = tree.getModel().getRoot();
		if (tree.getModel().isRootVisible()) {
			nodeContext.push(rootNode, NodeContext.SINGLE_NODE);
			writeSubtree(context, out, nodeContext);
			nodeContext.pop();
		} else {
			writeChildren(context, out, nodeContext, rootNode);
		}
	}

	/**
	 * Writes the title of the given tree control (if there is one).
	 */
	@Override
	protected void writeTitle(DisplayContext context, TagWriter out, TreeControl tree) throws IOException {
		final View titlebar = tree.getTitleBar();
		if (titlebar != null) {
			titlebar.write(context, out);
		}
	}

	/**
	 * Writes the given node and all its visible children.
	 */
	@Override
	protected void writeSubtree(DisplayContext context, TagWriter out, NodeContext nodeContext) throws IOException {
		final Object node = nodeContext.currentNode();
		final TreeControl tree = nodeContext.getTree();
		final TreeUIModel treeModel = tree.getModel();
		
		writeNode(context, out, tree, nodeContext);
		
		if (treeModel.isExpanded(node) && !treeModel.isLeaf(node)) {
			writeChildren(context, out, nodeContext, node);
		}
	}
	
	/**
	 * Writes only the visible children of the given parent node (not the node
	 * itself).
	 * 
	 * @param nodeContext
	 *        context to get tree from. {@link NodeContext#currentNode()} may
	 *        not be set, especially
	 *        <code>parentNode.equals(nodeContext.currentNode())</code> may be
	 *        <code>false</code>
	 * @param parentNode
	 *        the node whose children should be written. must not be
	 *        <code>null</code>
	 */
	@Override
	protected void writeChildren(DisplayContext context, TagWriter out, NodeContext nodeContext, Object parentNode) throws IOException {
		boolean isFirst = true;
		Iterator<?> it = nodeContext.getTree().getModel().getChildIterator(parentNode);
		boolean hasNext = it.hasNext();
		while (hasNext) {
			Object child = it.next();
			hasNext = it.hasNext();
			boolean isLast = !hasNext;
			int theType = (isFirst ? NodeContext.FIRST_NODE : 0) | (isLast ? NodeContext.LAST_NODE : 0);

			nodeContext.push(child, theType);
			writeSubtree(context, out, nodeContext);
			nodeContext.pop();
		}
	}
	
	/**
	 * Is called indirectly by
	 * {@link #writeNode(DisplayContext, TagWriter, TreeControl, NodeContext)}
	 * and writes the current node of the {@link NodeContext}.
	 * 
	 * <p>
	 * <b>NOTE:</b> this method must not be called directly
	 * </p>
	 */
    @Override
	protected void internalWriteNode(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
    	String nodeId = getCurrentNodeId(nodeContext);
    	String nodeTag = getNodeTag();
    	
    	writer.beginBeginTag(nodeTag);
    	writer.writeAttribute(ID_ATTR, nodeId);
		writeNodeClasses(writer, nodeContext);
		writeColumnHeightStyle(writer);
		writeNodeAttributes(context, writer, nodeContext);
    	writer.endBeginTag();
    	{
    		writeNodeContent(context, writer, nodeContext);
    	}
    	writer.endTag(nodeTag);
    }
    
	/**
	 * Append the CSS height-attribute.
	 */
	protected void writeColumnHeightStyle(TagWriter out) throws IOException {
		// Hook for subclasses
	}

	/**
	 * Returns the id of the node currently written
	 * 
	 * @param nodeContext
	 *        context to get current node and additional informations.
	 */
	@Override
	protected String getCurrentNodeId(NodeContext nodeContext) {
		Object node = nodeContext.currentNode();
    	TreeControl tree = nodeContext.getTree();
		return tree.getNodeId(node);
	}
    
	@Override
	protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
		out.append(TREE_NODE_CSS_CLASS);
	    
		boolean isSelected = nodeContext.getTree().getSelectionModel().isSelected(nodeContext.currentNode());
		if (isSelected) {
			out.append(TREE_NODE_SELECTED_CSS_CLASS);
		}

		Object node = nodeContext.currentNode();
		String cssClass = getTreeContentRenderer().getResourceProvider().getCssClass(node);
		if (!StringServices.isEmpty(cssClass)) {
			out.append(cssClass);
		}
	}

    @Override
	protected abstract String getNodeTag();
	
	/**
	 * Writes the current {@link NodeContext} by calling
	 * {@link TreeContentRenderer#write(DisplayContext, TagWriter, NodeContext) write-}method of
	 * {@link TreeContentRenderer}.
	 * 
	 * @param nodeContext
	 *        The current node to write.
	 * 
	 * @see #getTreeContentRenderer()
	 */
	@Override
	protected void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		getTreeContentRenderer().write(context, writer, nodeContext);
	}
	
	@Override
	protected void writeNodeAttributes(DisplayContext context, TagWriter writer, NodeContext nodeContext) {
		// Hook for sub classes
	};
	/**
	 * Add {@link ClientAction} to the given {@link UpdateQueue} to bring the
	 * GUI in sync with the server.
	 * 
	 * @param queue
	 *        the queue to add the updates
	 * @param tree
	 *        the tree which requests the update
	 * @param node
	 *        the node whose client side representation has to be updated
	 * @param nodeID
	 *        the client side id of the node for which an update is requested
	 */
	@Override
	public void addNodeUpdateActions(UpdateQueue queue, TreeControl tree, Object node, String nodeID) {
		final NodeContext nodeContext = new NodeContext(tree);
		setupNodeContext(tree, nodeContext, node);
		queue.add(new ElementReplacement(nodeID, createSubtreeFragment(nodeContext, false)));
	}

	/**
	 * Adds {@link ClientAction}s to the given {@link UpdateQueue} to bring the
	 * client side view of the given node and the whole subtree of the given
	 * node in sync with the server model.
	 * 
	 * @param queue
	 *        the {@link UpdateQueue} to add the actions
	 * @param tree
	 *        the tree which requests the updates for the subtree
	 * @param node
	 *        the node whose subtree needs updates. This includes the whole
	 *        subtree starting with that node.
	 * @param nodeID
	 *        the client side id of the node for which an update is requested
	 * @param stopID
	 *        the client side id of the last node which has to be updated
	 */
	@Override
	public void addSubtreeUpdateActions(UpdateQueue queue, final TreeControl tree, Object node, String nodeID, String stopID) {
		final HTMLFragment replacementFragment;
		TreeUIModel model = tree.getModel();
		if (node.equals(model.getRoot())) {
			HTMLFragment wholeTreeFragment = new HTMLFragment() {
				
				@Override
				public void write(DisplayContext context, TagWriter out) throws IOException {
					writeTreeContents(context, out, new NodeContext(tree));
				}
				
			};
			if (!model.isRootVisible() && nodeID == null) {
				queue.add(new FragmentInsertion(tree.getID(), AJAXConstants.AJAX_POSITION_START_VALUE, wholeTreeFragment));
				return;
			} else {
				replacementFragment = wholeTreeFragment;
			}
		} else {
			final NodeContext nodeContext = new NodeContext(tree);
			setupNodeContext(tree, nodeContext, node);
			replacementFragment = createSubtreeFragment(nodeContext, true);
		}
		queue.add(new RangeReplacement(nodeID, stopID, replacementFragment));
	}
	
	/**
	 * Return a {@link HTMLFragment} that contains a view of this tree from the
	 * given subtree root and optionally including all of its currently visible
	 * child nodes.
	 * 
	 * @param recursive
	 *        If <code>true</code>, the complete subtree starting from the given
	 *        root is rendered with all its currently visible children.
	 *        Otherwise, only the subtree root is rendered.
	 * @return A {@link HTMLFragment} showing the requested part of this tree.
	 */
	@Override
	protected HTMLFragment createSubtreeFragment(final NodeContext nodeContext, final boolean recursive) {
		HTMLFragment fragment = new HTMLFragment() {
			@Override
			public void write(DisplayContext aContext, TagWriter out) throws IOException {
				if (recursive) {
					writeSubtree(aContext, out, nodeContext);
				} else {
					writeNode(aContext, out, nodeContext.getTree(), nodeContext);
				}
			}
		};
		return fragment;
	}
	
	/**
	 * Installs a state for a renderer, in which it can render the given node
	 * object.
	 * 
	 * @param tree
	 *        The tree to compute the state from
	 * @param nodeContext
	 *        The {@link NodeContext} to install the state
	 * @param node
	 *        The node object for which the renderer state should be computed.
	 */
	@Override
	protected void setupNodeContext(TreeControl tree, NodeContext nodeContext, Object node) {
		// Compute a list of ancestor-or-self nodes of the given node. The last
		// node in the list is the root node. The first node is the original
		// node itself.
		Stack<Object> ancestors = new Stack<>();
		{
			Object ancestorOrSelf = node;
			while (ancestorOrSelf != null) {
				ancestors.push(ancestorOrSelf);
				ancestorOrSelf = tree.getModel().getParent(ancestorOrSelf);
			}
		}

		Object ancestorParent = null;
		
		// Exclude the root node, if it not displayed.
		if (! tree.getModel().isRootVisible()) {
			// Ancestors might be empty, if the root node is last one to remain
			// after a deletion.
			if (! ancestors.isEmpty()) { 
				ancestorParent = ancestors.pop();
			}
		}

		// update the node context to have correct stack for given node
		while (! ancestors.isEmpty()) {
			Object ancestor = ancestors.pop();
			nodeContext.push(ancestor, computeNodePosition(tree, ancestorParent, ancestor));
			ancestorParent = ancestor;
		}
	}
    

    /**
	 * Compute the kind of a given node.
	 * 
	 * <p>
	 * A node kind determines the visual attributes of the node's handle image.
	 * Depending, Depending on the fact that a node is the first, last, or
	 * middle child of its parent, the handle image must have the corresponding
	 * connecting lines.
	 * </p>
	 * 
	 * @param parent
	 *     The parent of the given node.
	 * @param node
	 *     The node, for witch the kind should be computed.
	 * @return One of {@link NodeContext#FIRST_NODE},
	 *     {@link NodeContext#LAST_NODE}, {@link NodeContext#MIDDLE_NODE},
	 *     and {@link NodeContext#SINGLE_NODE}.
	 */
	@Override
	protected int computeNodePosition(TreeControl tree, Object parent, Object node) {
		int position;
		if (node.equals(tree.getModel().getRoot())) {
			// The root node has no siblings.
			position = NodeContext.SINGLE_NODE;
		} else {
		    // Note: Be sure to handle even equal instead of identical tree nodes.
		    
		    List children = tree.getModel().getChildren(parent);
		    
		    boolean isFirst = node.equals(children.get(0));
			boolean isLast = node.equals(children.get(children.size() - 1));
			
            position = 
				(isFirst ? NodeContext.FIRST_NODE : 0) | 
				(isLast ? NodeContext.LAST_NODE : 0);
		}
		return position;
	}
}

