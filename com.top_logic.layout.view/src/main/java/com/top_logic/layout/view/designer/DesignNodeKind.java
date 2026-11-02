/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandSource;

/**
 * The kind of a {@link DesignTreeNode}, which decides the icon and the style of its row in the
 * designer tree.
 *
 * <p>
 * The style is a CSS class of the row. A configuration node of kind {@link #ELEMENT},
 * {@link #COMMAND} or {@link #PART} shows the {@link TypeIcon} of its configuration type in place of
 * the {@link #getIcon() icon of its kind}, see {@link DesignTreeNodeResourceProvider}.
 * </p>
 */
public enum DesignNodeKind {

	/**
	 * The view a file starts with.
	 *
	 * @see ConfigDesignTreeNode#isViewFileRoot()
	 */
	VIEW_FILE("tl-design-node--view-file"),

	/**
	 * The configuration of a {@link UIElement}.
	 */
	ELEMENT("tl-design-node--element"),

	/**
	 * The configuration of a {@link ViewCommand}, a {@link ViewCommandSource} or a
	 * {@link ViewAction}.
	 */
	COMMAND("tl-design-node--command"),

	/**
	 * Any other configuration, e.g. a column, a channel or a binding.
	 */
	PART("tl-design-node--part"),

	/**
	 * A {@link VirtualDesignTreeNode property group}.
	 */
	GROUP("tl-design-node--group"),

	/**
	 * A {@link ViewLinkDesignTreeNode link} to another view file.
	 */
	LINK("tl-design-node--link"),

	/**
	 * An {@link ErrorDesignTreeNode} standing for a view that cannot be loaded.
	 */
	ERROR("tl-design-node--error"),

	;

	private final String _cssClass;

	private DesignNodeKind(String cssClass) {
		_cssClass = cssClass;
	}

	/**
	 * The icon of a node of this kind.
	 *
	 * <p>
	 * The icons are theme images, see {@link Icons}.
	 * </p>
	 */
	public ThemeImage getIcon() {
		return switch (this) {
			case VIEW_FILE -> Icons.DESIGN_NODE_VIEW_FILE;
			case ELEMENT -> Icons.DESIGN_NODE_ELEMENT;
			case COMMAND -> Icons.DESIGN_NODE_COMMAND;
			case PART -> Icons.DESIGN_NODE_PART;
			case GROUP -> Icons.DESIGN_NODE_GROUP;
			case LINK -> Icons.DESIGN_NODE_LINK;
			case ERROR -> Icons.DESIGN_NODE_ERROR;
		};
	}

	/**
	 * The CSS class of the row of a node of this kind.
	 */
	public String getCssClass() {
		return _cssClass;
	}

	/**
	 * The kind of the given node.
	 */
	public static DesignNodeKind of(DesignTreeNode node) {
		if (node instanceof ConfigDesignTreeNode configNode) {
			return configNode.isViewFileRoot() ? VIEW_FILE : of(configNode.getConfig());
		}
		if (node instanceof VirtualDesignTreeNode) {
			return GROUP;
		}
		if (node instanceof ViewLinkDesignTreeNode) {
			return LINK;
		}
		if (node instanceof ErrorDesignTreeNode) {
			return ERROR;
		}
		return PART;
	}

	/**
	 * The kind of a node displaying the given configuration as part of a view file.
	 *
	 * @return {@link #ELEMENT}, {@link #COMMAND} or {@link #PART}.
	 */
	public static DesignNodeKind of(ConfigurationItem config) {
		if (config instanceof UIElement.Config) {
			return ELEMENT;
		}
		if (config instanceof ViewCommand.Config || config instanceof ViewCommandSource.Config<?>) {
			return COMMAND;
		}
		if (config instanceof PolymorphicConfiguration<?> polymorphic) {
			Class<?> implementation = polymorphic.getImplementationClass();
			if (implementation != null && ViewAction.class.isAssignableFrom(implementation)) {
				return COMMAND;
			}
		}
		return PART;
	}

}
