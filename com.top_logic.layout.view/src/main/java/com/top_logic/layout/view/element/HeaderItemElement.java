/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.sidebar.HeaderItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.SidebarElement.ItemSite;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemConfig;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemElement;
import com.top_logic.util.Resources;

/**
 * A caption naming the items that follow it in a {@link SidebarElement}.
 *
 * <p>
 * The caption is a label, not a place to go: it leads nowhere and cannot be activated. It divides a
 * long navigation into named sections that all stay visible, where a group would fold them away.
 * </p>
 */
public class HeaderItemElement implements SidebarItemElement {

	/**
	 * Configuration for {@link HeaderItemElement}.
	 */
	@TagName("header-item")
	public interface Config extends SidebarItemConfig {

		@Override
		@ClassDefault(HeaderItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * The caption naming the section.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The CSS icon class shown beside the caption (e.g. "bi bi-gear").
		 */
		@Name(ICON)
		String getIcon();
	}

	private final String _id;

	private final ResKey _label;

	private final String _icon;

	/**
	 * Creates a {@link HeaderItemElement} from configuration.
	 */
	@CalledByReflection
	public HeaderItemElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_label = config.getLabel();
		_icon = config.getIcon();
	}

	@Override
	public SidebarItem createSidebarItem(ViewContext context, ItemSite site) {
		return new HeaderItem(_id, Resources.getInstance().getString(_label), _icon);
	}

}
