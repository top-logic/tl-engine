/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.designer;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypeIcons;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * Displays a {@link DesignTreeNode} as the row of the designer tree standing for it.
 *
 * <p>
 * The label and the tooltip are the ones of the row, see
 * {@link DesignTreeDisplay#displayLabel(DesignTreeNode)} and
 * {@link DesignTreeDisplay#tooltipHtml(DesignTreeNode)}. Icon and CSS class follow the
 * {@link DesignNodeKind} of the node. A configuration node shows the {@link TypeIcon} of its
 * configuration type in place of the icon of its kind, except for the view a file starts with,
 * which is marked as a file.
 * </p>
 *
 * <p>
 * A row a property group shares with its single entry stands for the entry, so it shows the icon
 * and the style of the entry.
 * </p>
 */
public class DesignTreeNodeResourceProvider extends DefaultResourceProvider {

	/** Singleton {@link DesignTreeNodeResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final DesignTreeNodeResourceProvider INSTANCE = new DesignTreeNodeResourceProvider();

	/**
	 * Creates a {@link DesignTreeNodeResourceProvider}.
	 */
	protected DesignTreeNodeResourceProvider() {
		super();
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof DesignTreeNode node) {
			return DesignTreeDisplay.displayLabel(node);
		}
		return super.getLabel(object);
	}

	@Override
	public String getTooltip(Object object) {
		if (object instanceof DesignTreeNode node) {
			return DesignTreeDisplay.tooltipHtml(node);
		}
		return super.getTooltip(object);
	}

	@Override
	public ThemeImage getImage(Object object, Flavor flavor) {
		if (object instanceof DesignTreeNode node) {
			return icon(node);
		}
		return super.getImage(object, flavor);
	}

	@Override
	public String getCssClass(Object object) {
		if (object instanceof DesignTreeNode node) {
			return DesignNodeKind.of(node).getCssClass();
		}
		return super.getCssClass(object);
	}

	/**
	 * The icon of the row standing for the given node.
	 */
	public static ThemeImage icon(DesignTreeNode node) {
		DesignNodeKind kind = DesignNodeKind.of(node);
		if (kind != DesignNodeKind.VIEW_FILE && node instanceof ConfigDesignTreeNode configNode) {
			return icon(configNode.getConfig());
		}
		return kind.getIcon();
	}

	/**
	 * The icon of a node displaying the given configuration as part of a view file.
	 *
	 * @return The {@link TypeIcon} of the configuration type, the icon of its
	 *         {@link DesignNodeKind#of(ConfigurationItem) kind} if the type has none.
	 */
	public static ThemeImage icon(ConfigurationItem config) {
		String key = TypeIcons.iconKey(config.descriptor());
		return key != null ? ThemeImage.internalDecode(key) : DesignNodeKind.of(config).getIcon();
	}

}
