/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.tag.js.JSObject;
import com.top_logic.layout.form.tag.js.JSString;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;
import com.top_logic.layout.tree.NodeContext;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.model.TreeUIModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.css.CssUtil;

/**
 * {@link TreeContentRenderer} to be used with {@link LegacyTreeRenderer}.
 * 
 * @deprecated TODO #21888: Replace with tree table rendering.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public abstract class LegacyTreeContentRenderer extends TreeContentRenderer implements HTMLConstants {

	public static final String UNSELECTABLE_NODE_CSS = "unselectableNode";

	public static final String SELECTABLE_NODE_CSS = "selectableNode";

	public static final ResKey COLLAPSE_NODE_KEY = I18NConstants.COLLAPSE_NODE;

	public static final ResKey EXPAND_NODE_KEY = I18NConstants.EXPAND_NODE;

	protected abstract TreeImageProvider getTreeImages();

	@Override
	public void writeNodeContent(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
		writeNodeDecoration(context, writer, nodeContext);
		writeTypeImage(context, writer, nodeContext);
		writeTextSeparator(writer);
		writeNodeText(context, writer, nodeContext);
	}

	protected void writeNodeDecoration(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		writePrefix(context, writer, nodeContext);
		writeNodeHandle(context, writer, nodeContext);
	}

	/**
	 * Output images that represent the depth of the currently rendered node in the tree. The
	 * actually rendered images depend on the positions of the ancestor nodes within their parents
	 * child list.
	 */
	protected void writePrefix(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
		for (int n = 0, cnt = nodeContext.getSize() - 1; n < cnt; n++) {
			ThemeImage image = getTreeImages().getPrefixImage(nodeContext.getNodePosition(n));
			if (image != null) {
				image.write(context, writer);
			}
		}
	}

	/**
	 * Output the image that is the handle for expanding or collapsing the node.
	 */
	protected void writeNodeHandle(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		int nodePosition = nodeContext.currentNodePosition();

		TreeControl tree = nodeContext.getTree();
		Object node = nodeContext.currentNode();

		TreeUIModel model = tree.getModel();
		boolean isExpanded = model.isExpanded(node);
		boolean isLeaf = model.isLeaf(node);

		renderHandleImage(context, writer, tree, node, isLeaf, isExpanded, nodePosition);
	}

	/**
	 * This method is an extension point.
	 * 
	 * @see #writeNodeHandle(DisplayContext, TagWriter, NodeContext)
	 */
	protected void renderHandleImage(DisplayContext context, TagWriter writer, TreeControl tree, Object node,
			boolean isLeaf, boolean isExpanded, int nodePosition) throws IOException {
		XMLTag tag = getTreeImages().getNodeImage(isLeaf, isExpanded, nodePosition).toButton();
		tag.beginBeginTag(context, writer);
		CssUtil.writeCombinedCssClasses(writer,
			FormConstants.INPUT_IMAGE_CSS_CLASS,
			FormConstants.TOGGLE_BUTTON_CSS_CLASS,
			isLeaf ? FormConstants.DISABLED_CSS_CLASS : null);
		if (isLeaf) {
			writer.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		if (!isLeaf) {
			writeToggleOnClick(tree, writer, node);
		}
		writer.writeAttribute(ALT_ATTR, getAltText(context, node, isLeaf, isExpanded, nodePosition));
		tag.endEmptyTag(context, writer);
	}

	public final void writeToggleOnClick(TreeControl tree, TagWriter out, Object node) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		writeToggleOnClickContent(out, tree, node);
		out.endAttribute();
	}

	protected void writeToggleOnClickContent(TagWriter out, TreeControl tree, Object node) throws IOException {
		boolean isExpanded = tree.getModel().isExpanded(node);
		String nodeId = tree.getNodeId(node);
		writeInvokeExpression(out, TreeControl.ToggleExpansion.INSTANCE, tree, nodeId);

		out.append("BAL.eventStopPropagation(event);");
	}

	public void writeInvokeExpression(TagWriter out, TreeControl.ToggleExpansion command, TreeControl tree,
			String nodeID) throws IOException {
		command.writeInvokeExpression(out, tree,
			new JSObject(TreeControl.ToggleExpansion.ID_PARAM, new JSString(nodeID)));
	}

	private String getAltText(DisplayContext context, Object node, boolean isLeaf, boolean isExpanded,
			int nodePosition) {
		if (isLeaf)
			return StringServices.EMPTY_STRING;
		String nodeText = getResourceProvider().getLabel(node);
		return context.getResources().getMessage(isExpanded ? COLLAPSE_NODE_KEY : EXPAND_NODE_KEY, nodeText);
	}

	/**
	 * Write the image representing the type of the currently rendered node.
	 */
	protected void writeTypeImage(DisplayContext context, TagWriter writer, NodeContext nodeContext)
			throws IOException {
		Object node = nodeContext.currentNode();
		boolean isExpanded = nodeContext.getTree().getModel().isExpanded(node);
		ResourceProvider resourceProvider = getResourceProvider();
		ThemeImage nodeImage = resourceProvider.getImage(node, isExpanded ? Flavor.EXPANDED : Flavor.DEFAULT);
		String tooltip = resourceProvider.getTooltip(node);

		if (nodeImage != null) {
			writer.beginBeginTag(SPAN);
			writer.writeAttribute(CLASS_ATTR, "treeType");
			writer.endBeginTag();
			{
				nodeImage.writeWithTooltip(context, writer, tooltip);
			}
			writer.endTag(SPAN);
		}
	}

	/**
	 * Renders a separator between the graphical and textual tree representation.
	 */
	protected void writeTextSeparator(TagWriter writer) throws IOException {
		writer.writeText(HTMLConstants.NBSP);
	}

	/**
	 * Write the textual representation of the currently rendered node. This text may possibly
	 * decorated with a link.
	 * 
	 * This method calls
	 * {@link #renderTextLinkStart(DisplayContext, TagWriter, TreeControl, Object, boolean)},
	 * {@link #renderNodeText(TagWriter, Object)}, and
	 * {@link #renderTextLinkStop(TagWriter, boolean)}.
	 */
	protected void writeNodeText(DisplayContext context, TagWriter writer, NodeContext nodeContext) throws IOException {
		Object node = nodeContext.currentNode();
		TreeControl tree = nodeContext.getTree();

		boolean canSelect = tree.getSelectionModel().isSelectable(node);

		renderTextLinkStart(context, writer, tree, node, canSelect);
		renderNodeText(writer, nodeContext);
		renderTextLinkStop(writer, canSelect);
	}

	protected void renderTextLinkStart(DisplayContext context, TagWriter writer, final TreeControl tree,
			Object node, boolean canSelect) throws IOException {
		String tooltip = getResourceProvider().getTooltip(node);
		writeTextLinkStart(context, writer, canSelect, new OnClickWriter() {
			@Override
			public void writeOnClickSelect(TagWriter out, Object node) throws IOException {
				out.beginAttribute(ONCLICK_ATTR);
				writeOnClickSelectContent(out, node);
				out.endAttribute();
			}

			protected void writeOnClickSelectContent(TagWriter out, Object node) throws IOException {
				out.append("return services.form.TreeControl.selectTreeTableRow(event, '");
				out.append(tree.getID());
				out.append("', '");
				out.append(tree.getNodeId(node));
				out.append("');");
			}
		}, node, tooltip);
	}

	protected void renderNodeText(TagWriter writer, NodeContext nodeContext) throws IOException {
		String nodeText = getResourceProvider().getLabel(nodeContext.currentNode());
		writer.writeText(nodeText);
	}

	protected void renderTextLinkStop(TagWriter writer, boolean canSelect) throws IOException {
		writeTextLinkStop(writer, canSelect);
	}

	/**
	 * Find incompatibilities with code from preliminary fix for Ticket #1945 in DPM1_1.
	 * 
	 * @deprecated Override {@link #writeNodeText(DisplayContext, TagWriter, NodeContext)}.
	 */
	@Deprecated
	protected final void renderNodeText(DisplayContext context, TagWriter writer, Object node, NodeContext nodeContext)
			throws IOException {
		writeNodeText(context, writer, nodeContext);
	}

	/**
	 * Find incompatibilities in code before Ticket #1945.
	 * 
	 * @deprecated Override {@link #writeNodeText(DisplayContext, TagWriter, NodeContext)}.
	 */
	@Deprecated
	protected final void renderNodeText(TagWriter writer, Object node) throws IOException {
		throw new UnsupportedOperationException();
	}

	/**
	 * Writes a start tag. If <code>selectable == true</code> it includes the given select action.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        the writer to write to.
	 * @param selectable
	 *        whether a Node can be selected, or not.
	 * @param selectAction
	 *        the action to execute. Must not be <code>null</code> if
	 *        <code>selectable == true</code>
	 */
	public static void writeTextLinkStart(DisplayContext context, TagWriter out, boolean selectable,
			OnClickWriter selectAction, Object node)
			throws IOException {
		writeTextLinkStart(context, out, selectable, selectAction, node, null);
	}

	/**
	 * Writes a start tag.
	 * 
	 * <p>
	 * Selectable nodes are written with tag {@link HTMLConstants#ANCHOR}.
	 * </p>
	 * 
	 * @see #writeTextLinkStart(DisplayContext, TagWriter, String, boolean, OnClickWriter, Object,
	 *      String)
	 */
	public static void writeTextLinkStart(DisplayContext context, TagWriter out, boolean selectable,
			OnClickWriter selectAction, Object node,
			String tooltip) throws IOException {
		writeTextLinkStart(context, out, ANCHOR, selectable, selectAction, node, tooltip);
	}

	/**
	 * Writes a start tag. If <code>selectable == true</code> it includes the given select action.
	 * 
	 * @param context
	 *        The rendering context.
	 * @param out
	 *        the writer to write to.
	 * @param selectableLinkTag
	 *        Tag of the node if node is selectable.
	 * @param selectable
	 *        whether a Node can be selected, or not.
	 * @param selectAction
	 *        the action to execute. Must not be <code>null</code> if
	 *        <code>selectable == true</code>
	 * @param tooltip
	 *        the tooltip to write over the text
	 * 
	 * @see #writeTextLinkStop(TagWriter, String, boolean)
	 */
	protected static void writeTextLinkStart(DisplayContext context, TagWriter out, String selectableLinkTag,
			boolean selectable,
			OnClickWriter selectAction, Object node, String tooltip) throws IOException {
		if (selectable) {
			out.beginBeginTag(selectableLinkTag);
			if (ANCHOR.equals(selectableLinkTag)) {
				out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
			}
			out.writeAttribute(CLASS_ATTR, SELECTABLE_NODE_CSS);
			selectAction.writeOnClickSelect(out, node);
		} else {
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, UNSELECTABLE_NODE_CSS);
		}
		if (tooltip != null) {
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, tooltip);
		}
		out.endBeginTag();
	}

	/**
	 * Writes the end tag to the corresponding
	 * {@link #writeTextLinkStart(DisplayContext, TagWriter, boolean, OnClickWriter, Object, String)
	 * start link}.
	 * 
	 * @see #writeTextLinkStart(DisplayContext, TagWriter, boolean, OnClickWriter, Object, String)
	 */
	public static void writeTextLinkStop(TagWriter out, boolean selectable) throws IOException {
		writeTextLinkStop(out, ANCHOR, selectable);
	}

	/**
	 * Writes the end tag to the corresponding
	 * {@link #writeTextLinkStart(DisplayContext, TagWriter, String, boolean, OnClickWriter, Object, String)
	 * start link}.
	 * 
	 * @see #writeTextLinkStart(DisplayContext, TagWriter, String, boolean, OnClickWriter, Object,
	 *      String)
	 */
	protected static void writeTextLinkStop(TagWriter out, String selectableLinkTag, boolean selectable) {
		if (selectable) {
			out.endTag(selectableLinkTag);
		} else {
			out.endTag(SPAN);
		}
	}

}
