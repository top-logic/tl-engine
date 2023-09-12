/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.tree.dnd.TreeDropTarget;
import com.top_logic.layout.tree.model.TLTreeModel;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.layout.tree.renderer.TreeContentRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.SelectionModel;

/**
 * Renderer interface for {@link TreeControl}s.
 * 
 * <p>
 * This interface is realized as class instead of Java interface to allow
 * type-safe adaption to the {@link Renderer} interface.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TreeRenderer extends DefaultControlRenderer<TreeControl> {
	
	public static final String TREE_CONTROL_CSS_CLASS = "treeBody";
    
	public static final String TREE_NODE_SELECTED_CSS_CLASS = "treeSelected";
	
	/**
	 * CSS class to position Tooltips horizontally in trees.
	 */
	public static final String TOOLTIP_HORIZONTAL_CSS_CLASS = "tooltipHorizontal";

	/**
	 * CSS class of the container element containing all children of a certain node.
	 */
	public static final String TREE_CHILDREN_CSS_CLASS = "treeChildren";

	private Renderer<NodeContext> _nodeRendererAdapter = new Renderer<>() {
		@Override
		public void write(DisplayContext context, TagWriter out, NodeContext value) throws IOException {
			internalWriteNode(context, out, value);
		}
	};

	/**
	 * Returns the {@link TreeContentRenderer} of this {@link TreeRenderer}.
	 */
	public abstract TreeContentRenderer getTreeContentRenderer();

	@Override
	protected void writeControlTagAttributes(DisplayContext context, TagWriter out, TreeControl control)
			throws IOException {
		super.writeControlTagAttributes(context, out, control);
		TreeControl treeControl = control;
		out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, "true");
		writeOnClick(out, treeControl);
		writeOnDblClick(out, control);


		if (!treeControl.useLegacyDrag()) {
			TreeData data = treeControl.getData();

			if (data.getDragSource().dragEnabled(data)) {
				out.beginAttribute(ONDRAGSTART_ATTR);
				out.append("return services.form.TreeControl.handleOnDragStart(event, this);");
				out.endAttribute();
				out.beginAttribute(ONDRAGEND_ATTR);
				out.append("return services.form.TreeControl.handleOnDragEnd(event, this);");
				out.endAttribute();
			}

			if (data.getDropTarget().dropEnabled(data)) {
				out.beginAttribute(ONDROP_ATTR);
				out.append("return services.form.TreeControl.handleOnDrop(event, this);");
				out.endAttribute();
				out.beginAttribute(ONDRAGOVER_ATTR);
				out.append("return services.form.TreeControl.handleOnDragOver(event, this);");
				out.endAttribute();
				out.beginAttribute(ONDRAGENTER_ATTR);
				out.append("return services.form.TreeControl.handleOnDragEnter(event, this);");
				out.endAttribute();
				out.beginAttribute(ONDRAGLEAVE_ATTR);
				out.append("return services.form.TreeControl.handleOnDragLeave(event, this);");
				out.endAttribute();

				out.writeAttribute(TreeDropTarget.TL_DROPTYPE_ATTR, data.getDropTarget().getDropType().name());
			}
		} else {
			treeControl.writeLegacyDragAndDropSupport(context, out);
		}

	}

	private void writeOnClick(TagWriter out, TreeControl treeControl) throws IOException {
		if (treeControl.useLegacyDrag()) {
			out.beginAttribute(ONMOUSEDOWN_ATTR);
			out.append("return services.form.TreeControl.handleOnMouseDown(event, this);");
			out.endAttribute();
		} else {
			out.beginAttribute(ONCLICK_ATTR);
			out.append("return services.form.TreeControl.handleOnClick(event, this);");
			out.endAttribute();
		}
	}

	private void writeOnDblClick(TagWriter out, Control control) throws IOException {
		((TreeControl) control).writeOnDblClick(out);
	}

	@Override
	public void appendControlCSSClasses(Appendable out, TreeControl control) throws IOException {
		super.appendControlCSSClasses(out, control);
		HTMLUtil.appendCSSClass(out, TREE_CONTROL_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, AbstractControlBase.IS_CONTROL_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, AbstractControlBase.CAN_INSPECT_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, TOOLTIP_HORIZONTAL_CSS_CLASS);
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
	protected void writeTreeContents(DisplayContext context, TagWriter out, NodeContext nodeContext) throws IOException {
		final TreeControl tree = nodeContext.getTree();
		Object rootNode = tree.getModel().getRoot();
		if (tree.getModel().isRootVisible()) {
			nodeContext.push(rootNode, NodeContext.SINGLE_NODE);
			writeSubtree(context, out, nodeContext);
			nodeContext.pop();
		} else {
			writeChildrenNodes(context, out, nodeContext, rootNode);
		}
	}

	/**
	 * Writes the title of the given tree control (if there is one).
	 */
	protected void writeTitle(DisplayContext context, TagWriter out, TreeControl tree) throws IOException {
		final View titlebar = tree.getTitleBar();
		if (titlebar != null) {
			titlebar.write(context, out);
		}
	}

	/**
	 * Writes the given node and all its visible children.
	 */
	protected void writeSubtree(DisplayContext context, TagWriter out, NodeContext nodeContext) throws IOException {
		String nodeId = getCurrentNodeId(nodeContext);

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, TreeControl.SUBTREE_CSS_CLASS);
		out.writeAttribute(ID_ATTR, subtreeId(nodeId));
		out.endBeginTag();
		{
			writeSubtreeContents(context, out, nodeContext);
		}
		out.endTag(DIV);
	}

	/**
	 * Writes the subtree of a given node (the node itself and all of its children).
	 */
	protected void writeSubtreeContents(DisplayContext context, TagWriter out, NodeContext nodeContext)
			throws IOException {
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
	protected void writeChildren(DisplayContext context, TagWriter out, NodeContext nodeContext, Object parentNode) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, TREE_CHILDREN_CSS_CLASS);
		out.endBeginTag();
		{
			writeChildrenNodes(context, out, nodeContext, parentNode);
		}
		out.endTag(DIV);
	}

	/**
	 * Writes the list of children nodes within the
	 * {@link #writeChildren(DisplayContext, TagWriter, NodeContext, Object) children container}.
	 */
	protected void writeChildrenNodes(DisplayContext context, TagWriter out, NodeContext nodeContext, Object parentNode)
			throws IOException {
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
	 * Renders the node described by the given {@link NodeContext} object by
	 * installing the correct scope and dispatching to
	 * {@link #internalWriteNode(DisplayContext, TagWriter, NodeContext)}
	 */
	public final void writeNode(DisplayContext context, TagWriter writer, TreeControl tree, NodeContext nodeContext) throws IOException {
		context.renderScoped(tree.getOrCreateNodeScope(nodeContext), _nodeRendererAdapter, writer, nodeContext);
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
    protected void internalWriteNode(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
    	String nodeId = getCurrentNodeId(nodeContext);
    	String nodeTag = getNodeTag();
    	
    	writer.beginBeginTag(nodeTag);
    	writer.writeAttribute(ID_ATTR, nodeId);
		if (nodeContext.getTree().hasContextMenu(nodeContext.currentNode())) {
			writer.writeAttribute(TL_CONTEXT_MENU_ATTR, nodeId);
		}
		writeNodeClasses(writer, nodeContext);
		writeNodeAttributes(context, writer, nodeContext);
    	writer.endBeginTag();
    	{
    		writeNodeContent(context, writer, nodeContext);
    	}
    	writer.endTag(nodeTag);
    }
    
	/**
	 * Returns the id of the node currently written
	 * 
	 * @param nodeContext
	 *        context to get current node and additional informations.
	 */
	protected String getCurrentNodeId(NodeContext nodeContext) {
		Object node = nodeContext.currentNode();
    	TreeControl tree = nodeContext.getTree();
		return tree.getNodeId(node);
	}
    
	protected final void writeNodeClasses(TagWriter out, NodeContext nodeContext) throws IOException {
		out.beginCssClasses();
		writeNodeClassesContent(out, nodeContext);
		out.endCssClasses();
	}

	protected void writeNodeClassesContent(TagWriter out, NodeContext nodeContext) throws IOException {
		out.append(TreeControl.NODE_LABEL_CSS_CLASS);
		out.append(TreeControl.TREE_NODE_CSS_CLASS);
	    
		SelectionModel selectionModel = nodeContext.getTree().getSelectionModel();
		Object node = nodeContext.currentNode();
		boolean isSelected = selectionModel.isSelected(node);
		if (isSelected) {
			out.append(TREE_NODE_SELECTED_CSS_CLASS);
		} else if (!selectionModel.isSelectable(node)) {
			out.append(TreeControl.NO_SELECT_CSS);
		}

		TreeUIModel model = nodeContext.getTree().getModel();
		boolean isExpanded = model.isExpanded(node);
		if (isExpanded) {
			out.append(TreeControl.NODE_EXPANDED_CSS_CLASS);
		}

		out.append(getTreeContentRenderer().getResourceProvider().getCssClass(node));
	}

    protected abstract String getNodeTag();
	
	/**
	 * Writes the current {@link NodeContext} by calling
	 * {@link TreeContentRenderer#write(DisplayContext, TagWriter, NodeContext) write}-method of
	 * {@link TreeContentRenderer}.
	 * 
	 * @param nodeContext
	 *        The current node to write.
	 * 
	 * @see #getTreeContentRenderer()
	 */
	protected void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		getTreeContentRenderer().write(context, writer, nodeContext);
	}
	
	protected void writeNodeAttributes(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		TreeData data = nodeContext.getTree().getData();
		if (!nodeContext.getTree().useLegacyDrag()) {
			if (data.getDragSource().canDrag(data, nodeContext.currentNode())) {
				writer.writeAttribute(HTMLConstants.DRAGGABLE_ATTR, HTMLConstants.DRAGGABLE_TRUE_VALUE);
				HTMLUtil.writeDragImage(context, writer, getTreeContentRenderer().getResourceProvider(),
					nodeContext.currentNode());
			}
		} else {
			if (!data.getSelectionModel().isSelectable(nodeContext.currentNode())) {
				writer.writeAttribute(ONMOUSEDOWN_ATTR,
					"return services.form.TreeControl.handleFixed(arguments[0], this)");
				writer.writeAttribute(ONCLICK_ATTR, "return services.form.TreeControl.handleFixed(arguments[0], this)");
				writer.writeAttribute(ONDBLCLICK_ATTR,
					"return services.form.TreeControl.handleFixed(arguments[0], this)");

			}
		}
	}

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
			boolean rootVisible = model.isRootVisible();
			if (!rootVisible) {
				if (nodeID == null) {
					// The tree was empty before, there is no anchor to replace.
					queue.add(new FragmentInsertion(tree.getID(), AJAXConstants.AJAX_POSITION_START_VALUE,
						wholeTreeFragment));
				} else {
					// A range of top-level nodes must be replaced.
					queue.add(new RangeReplacement(subtreeId(nodeID), subtreeId(stopID), wholeTreeFragment));
				}
				return;
			} else {
				replacementFragment = wholeTreeFragment;
			}
		} else {
			final NodeContext nodeContext = new NodeContext(tree);
			setupNodeContext(tree, nodeContext, node);
			replacementFragment = createSubtreeFragment(nodeContext, true);
		}
		queue.add(new ElementReplacement(subtreeId(nodeID), replacementFragment));
	}

	private String subtreeId(String nodeID) {
		return nodeID + "-subtree";
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

