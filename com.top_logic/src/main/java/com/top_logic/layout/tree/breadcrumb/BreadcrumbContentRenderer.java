/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.tree.TreeControl;
import com.top_logic.layout.tree.TreeRenderer;
import com.top_logic.layout.tree.renderer.AbstractTreeContentRenderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.NoImageResourceProvider;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Renderer for the actual content of a node in a {@link BreadcrumbData}.
 * 
 * @see DefaultBreadcrumbRenderer.Config#getContentRenderer()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class BreadcrumbContentRenderer extends AbstractConfiguredInstance<BreadcrumbContentRenderer.Config>
		implements HTMLConstants {

	private static final String MENU_NODE_CSS = "breadcrumbMenuNode";
	/** CSS-Class of the menu nodes if the mouse currently points to it */
	private static final String MENU_NODE_HOVER_CSS = "breadcrumbMenuNode_hover";
	private static final String MENU_CSS = "menu";
	private static final String MENU_SUFFIX = "_menu";

	private static final String NODE_SEPARATOR_CSS = "breadcrumbNodeSeparator";

	public static final String NODE_TEXT_CSS = "breadcrumbNodeText";

	private static final String NODE_ICON_CSS = "breadcrumbNodeIcon";

	/** {@link ConfigurationItem} for the {@link BreadcrumbContentRenderer}. */
	public interface Config extends PolymorphicConfiguration<BreadcrumbContentRenderer> {

		/** Property name of {@link #isRootVisible()}. */
		String ROOT_VISIBLE_ATTRIBUTE = "root-visible";

		/** Property name of {@link #getRootLabel()}. */
		String ROOT_LABEL = "root-label";

		/** Property name of {@link #getRootIcon()}. */
		String ROOT_ICON = "root-icon";

		/** Property name of {@link #getResourceProvider()}. */
		String RESOURCE_PROVIDER = "resource-provider";

		/** Property name of {@link #shouldWriteMenu()}. */
		String WRITE_MENU = "write-menu";

		/** Property name of {@link #getSeparatorLabel()}. */
		String SEPARATOR_LABEL = "separator-label";

		/** Property name of {@link #getSeparatorIcon()}. */
		String SEPARATOR_ICON = "separator-icon";

		/** @see BreadcrumbContentRenderer#isRootVisible() */
		@Name(ROOT_VISIBLE_ATTRIBUTE)
		@BooleanDefault(true)
		boolean isRootVisible();

		/** @see BreadcrumbContentRenderer#getRootLabel() */
		@Name(ROOT_LABEL)
		ResKey getRootLabel();

		/** @see BreadcrumbContentRenderer#getRootIcon() */
		@Name(ROOT_ICON)
		ThemeImage getRootIcon();

		/** @see BreadcrumbContentRenderer#getResourceProvider() */
		@Name(RESOURCE_PROVIDER)
		@ItemDefault
		@ImplementationClassDefault(MetaResourceProvider.class)
		PolymorphicConfiguration<ResourceProvider> getResourceProvider();

		/** @see BreadcrumbContentRenderer#shouldWriteMenu() */
		@Name(WRITE_MENU)
		boolean shouldWriteMenu();

		/** @see BreadcrumbContentRenderer#getSeparatorLabel() */
		@Name(SEPARATOR_LABEL)
		String getSeparatorLabel();

		/** @see BreadcrumbContentRenderer#getSeparatorIcon() */
		@Name(SEPARATOR_ICON)
		ThemeImage getSeparatorIcon();

	}

	private final boolean _isRootVisible;

	private final ResKey _rootLabel;

	private final ThemeImage _rootIcon;

	private final ResourceProvider _resourceProvider;

	private final boolean _shouldWriteMenu;

	private final String _separatorLabel;

	private final ThemeImage _separatorIcon;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link BreadcrumbContentRenderer}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public BreadcrumbContentRenderer(InstantiationContext context, Config config) {
		super(context, config);
		_isRootVisible = config.isRootVisible();
		_rootLabel = config.getRootLabel();
		_rootIcon = config.getRootIcon();
		_resourceProvider = context.getInstance(config.getResourceProvider());
		_shouldWriteMenu = config.shouldWriteMenu();
		_separatorLabel = config.getSeparatorLabel();
		_separatorIcon = config.getSeparatorIcon();
		checkMenu(context);
	}

	private void checkMenu(InstantiationContext context) {
		if (_shouldWriteMenu && (_separatorIcon == null) && StringServices.isEmpty(_separatorLabel)) {
			context.error("The breadcrumb should write the menu, but neither an icon nor a label"
				+ " on which the user could click to open it. Configure either an icon, a label or disable the menu.");
		}
	}

	/**
	 * Returns the name of the tag for the current node.
	 * 
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * 
	 * @return the name of the tag which is used for the given node
	 */
	public String getNodeTag(BreadcrumbControl breadcrumb, Object currentNode) {
		return HTMLConstants.SPAN;
	}

	/**
	 * Writes attribute of the tag of the current node except the {@link HTMLConstants#ID_ATTR id
	 * attribute}.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	public void writeNodeTagAttributes(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		writeNodeClasses(out, breadcrumb, currentNode);
	}

	/**
	 * Writes the content of the given node together with the menu to select children.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	public void writeNode(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		if (currentNode.equals(breadcrumb.getTree().getRoot()) && !isRootVisible()) {
			return;
		}
		boolean canSelect = breadcrumb.getSelectionModel().isSelectable(currentNode);

		AbstractTreeContentRenderer.writeTextLinkStart(context, out, canSelect, breadcrumb, currentNode);
		{
			writeNodeContent(context, out, breadcrumb, currentNode);
		}
		AbstractTreeContentRenderer.writeTextLinkStop(out, canSelect);

		if (!isLastNode(breadcrumb, currentNode)) {
			writeNodeSeparator(context, out, breadcrumb, currentNode);
		}
	}

	private boolean isLastNode(BreadcrumbControl breadcrumb, Object currentNode) {
		return Objects.equals(currentNode, breadcrumb.getLastNode());
	}

	private void writeNodeSeparator(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb,
			Object currentNode) throws IOException {
		if (!breadcrumb.getTree().hasChildren(currentNode)) {
			return;
		}
		out.beginTag(SPAN, CLASS_ATTR, NODE_SEPARATOR_CSS);
		if (shouldWriteMenu()) {
			writeNodeSeparatorWithMenu(context, out, breadcrumb, currentNode);
		} else {
			writeNodeSeparatorWithoutMenu(context, out, breadcrumb, currentNode);
		}
		out.endTag(SPAN);
	}

	/**
	 * Writes the separator, if {@link #shouldWriteMenu() the child selection menu should be
	 * written}.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeSeparatorWithMenu(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb,
			Object currentNode) throws IOException {
		renderHandleImage(context, out, breadcrumb, currentNode);
		writeNodeSeparatorText(context, out, breadcrumb, currentNode);
		renderChildrenMenu(context, out, breadcrumb, currentNode);
	}

	/**
	 * Writes the separator, if {@link #shouldWriteMenu() the child selection menu should not be
	 * written}.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeSeparatorWithoutMenu(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb,
			Object currentNode) throws IOException {
		if (getSeparatorIcon() != null) {
			getSeparatorIcon().write(context, out);
		}
		String label = getSeparatorLabel();
		if (!StringServices.isEmpty(label)) {
			out.writeText(label);
		}
	}

	/**
	 * Writes the actual content of the node, i.e the
	 * {@link #writeTypeImage(DisplayContext, TagWriter, BreadcrumbControl, Object) type image} and
	 * the {@link #writeNodeText(DisplayContext, TagWriter, BreadcrumbControl, Object) text} of the
	 * node.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeContent(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		writeTypeImage(context, out, breadcrumb, currentNode);
		writeNodeText(context, out, breadcrumb, currentNode);
	}

	/**
	 * Returns the css class for the given node
	 * 
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeClasses(TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		out.beginAttribute(CLASS_ATTR);
		out.append(TreeControl.TREE_NODE_CSS_CLASS);

		boolean isSelected = currentNode.equals(breadcrumb.getSelectionModel().getSingleSelection());
		if (isSelected) {
			out.append(StringServices.BLANK_CHAR);
			out.append(TreeRenderer.TREE_NODE_SELECTED_CSS_CLASS);
		}
		out.endAttribute();
	}

	/**
	 * Write the image representing the type of the currently rendered node. The Image is taken from
	 * the {@link #getResourceProvider() resource provider}.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeTypeImage(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		ThemeImage nodeImage = getNodeIcon(breadcrumb, currentNode);
		if (nodeImage != null) {
			out.beginTag(SPAN, CLASS_ATTR, NODE_ICON_CSS);
			nodeImage.write(context, out);
			out.endTag(SPAN);
		}
	}

	/**
	 * The icon representing the given node.
	 * 
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @return If null, no image is written.
	 */
	public ThemeImage getNodeIcon(BreadcrumbControl breadcrumb, Object currentNode) {
		if (currentNode.equals(breadcrumb.getTree().getRoot()) && getRootIcon() != null) {
			return getRootIcon();
		}
		return getResourceProvider().getImage(currentNode, Flavor.DEFAULT);
	}

	/**
	 * Writes the text of the given node. The text is the {@link ResourceProvider#getLabel(Object)
	 * label} of the node.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeText(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		String nodeText = getNodeLabel(breadcrumb, currentNode);
		if (nodeText != null) {
			out.beginTag(SPAN, CLASS_ATTR, NODE_TEXT_CSS);
			out.writeText(nodeText);
			out.endTag(SPAN);
		}
	}

	/**
	 * The label representing the given node.
	 * 
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @return If null, no label is written.
	 */
	public String getNodeLabel(BreadcrumbControl breadcrumb, Object currentNode) {
		if (currentNode.equals(breadcrumb.getTree().getRoot()) && getRootLabel() != null) {
			return Resources.getInstance().getString(getRootLabel());
		}
		return getResourceProvider().getLabel(currentNode);
	}

	/**
	 * Writes the image to open the menu for selecting the children of the given node, and the
	 * corresponding javascript action. The image is taken from {@link #getSeparatorIcon()}.
	 */
	protected void renderHandleImage(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		if (getSeparatorIcon() == null) {
			return;
		}
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, MENU_CSS);
		out.endBeginTag();
		{
			ThemeImage icon = getSeparatorIcon();
			XMLTag iconButton = icon.toButton();
			iconButton.beginBeginTag(context, out);
			CssUtil.writeCombinedCssClasses(out,
				FormConstants.INPUT_IMAGE_CSS_CLASS,
				FormConstants.MENU_BUTTON_CSS_CLASS);
			writeOnClickToggle(out, breadcrumb, currentNode);
			out.writeAttribute(ALT_ATTR, StringServices.EMPTY_STRING);
			iconButton.endEmptyTag(context, out);
		}
		out.endTag(SPAN);
	}

	/**
	 * Writes the separator text between two nodes.
	 * 
	 * @param context
	 *        The display context for the current user interaction.
	 * @param out
	 *        The {@link TagWriter} to which has to be written.
	 * @param breadcrumb
	 *        The {@link BreadcrumbControl} displaying the breadcrumb.
	 * @param currentNode
	 *        The business object currently that is being written.
	 * @throws IOException
	 *         If writing fails due to an IO problem.
	 */
	protected void writeNodeSeparatorText(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb,
			Object currentNode) throws IOException {
		String label = getSeparatorLabel();
		if (StringServices.isEmpty(label)) {
			return;
		}
		out.beginBeginTag(SPAN);
		CssUtil.writeCombinedCssClasses(out, MENU_CSS, FormConstants.MENU_BUTTON_CSS_CLASS);
		writeOnClickToggle(out, breadcrumb, currentNode);
		out.endBeginTag();
		out.writeText(label);
		out.endTag(SPAN);
	}

	/**
	 * Never null. The empty {@link String} if the given {@link ResKey} is null.
	 */
	private String translate(ResKey i18n) {
		if (i18n == null) {
			return "";
		}
		return Resources.getInstance().getString(i18n);
	}

	private void writeOnClickToggle(TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		out.beginAttribute(ONCLICK_ATTR);
		writeToggleAction(out, breadcrumb, currentNode);
		out.endAttribute();
	}

	/**
	 * Writes the framework of the menu for selecting children.
	 */
	protected void renderChildrenMenu(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		String menuID = breadcrumb.getNodeId(currentNode) + MENU_SUFFIX;

		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, "breadcrumbMenu");
		out.writeAttribute(ID_ATTR, menuID);
		out.endBeginTag();
		{
			Iterator<?> it = breadcrumb.getTree().getChildIterator(currentNode);
			while (it.hasNext()) {
				Object child = it.next();
				out.beginBeginTag(DIV);
				out.writeAttribute(ID_ATTR, menuID + breadcrumb.getNodeId(child));
				out.writeAttribute(CLASS_ATTR, MENU_NODE_CSS);
				out.endBeginTag();
				{
					writeMenuNode(context, out, breadcrumb, child);
				}
				out.endTag(DIV);
			}
		}
		out.endTag(DIV);
	}

	/**
	 * Writes the content of a node in the menu. This method actually calls
	 * {@link #writeNodeContent(DisplayContext, TagWriter, BreadcrumbControl, Object)}
	 */
	protected void writeMenuNode(DisplayContext context, TagWriter out, BreadcrumbControl breadcrumb, Object currentNode) throws IOException {
		writeNodeContent(context, out, breadcrumb, currentNode);
	}

	/**
	 * Returns the action to open the menu for selecting children, written by
	 * {@link #renderHandleImage(DisplayContext, TagWriter, BreadcrumbControl, Object)}
	 */
	protected void writeToggleAction(TagWriter out, BreadcrumbControl breadcrumb, Object currentNode)
			throws IOException {
		out.append("services.form.BreadcrumbControl.showMenu(this, '");
		out.append(breadcrumb.getNodeId(currentNode));
		out.append(MENU_SUFFIX);
		out.append("', '");
		out.append(breadcrumb.getID());
		out.append("', '");
		out.append(breadcrumb.getSelectCommand());
		out.append("');");
	}

	/**
	 * Whether root should be visible.
	 */
	public boolean isRootVisible() {
		return _isRootVisible;
	}

	/**
	 * The label used for the root node.
	 * <p>
	 * If it is null, {@link #getResourceProvider()} is used. Just like for every other node.
	 * </p>
	 * <p>
	 * To suppress the label, use the empty I18N, which is encoded as: <code>/s</code>
	 * </p>
	 */
	public ResKey getRootLabel() {
		return _rootLabel;
	}

	/**
	 * The icon used for the root node.
	 * <p>
	 * If it is null, {@link #getResourceProvider()} is used. Just like for every other node.
	 * </p>
	 * <p>
	 * To suppress the icon, configure the {@link NoImageResourceProvider} as
	 * {@link #getResourceProvider()}.
	 * </p>
	 */
	public ThemeImage getRootIcon() {
		return _rootIcon;
	}

	/**
	 * Returns the {@link ResourceProvider} to get type image and label for a node.
	 */
	protected ResourceProvider getResourceProvider() {
		return _resourceProvider;
	}

	/**
	 * Whether the drop-down-menu for the child-selection should be written.
	 */
	public boolean shouldWriteMenu() {
		return _shouldWriteMenu;
	}

	/**
	 * The label that is used as separator between nodes.
	 * <p>
	 * Null means, no text is written.
	 * </p>
	 */
	public String getSeparatorLabel() {
		return _separatorLabel;
	}

	/**
	 * The icon that is used as separator between nodes.
	 * <p>
	 * Null means, no icon is written.
	 * </p>
	 */
	public ThemeImage getSeparatorIcon() {
		return _separatorIcon;
	}

}
