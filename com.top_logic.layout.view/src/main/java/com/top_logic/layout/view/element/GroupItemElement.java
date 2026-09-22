/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.sidebar.GroupItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.SidebarElement.ItemSite;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemConfig;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemElement;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.AccessControl;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.util.Resources;

/**
 * A collapsible group of further items of a {@link SidebarElement}.
 *
 * <p>
 * The group gathers items under a heading the user folds away. Whether it is folded is remembered
 * per user under the group's id, so the sidebar opens the way it was left.
 * </p>
 *
 * <p>
 * A group holds items, not content: the navigation items inside it lead to the same places they
 * would lead to at the top level of the sidebar, and are addressed at the sidebar itself. A group
 * therefore nests - a group within a group - without changing where anything is displayed.
 * </p>
 */
public class GroupItemElement implements SidebarItemElement {

	/**
	 * Configuration for {@link GroupItemElement}.
	 */
	@TagName("group")
	public interface Config extends SidebarItemConfig, WithAccessControl {

		@Override
		@ClassDefault(GroupItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getExpanded()}. */
		String EXPANDED = "expanded";

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/**
		 * The heading the group is folded away under.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * The CSS icon class shown beside the heading (e.g. "bi bi-gear").
		 */
		@Name(ICON)
		String getIcon();

		/**
		 * Whether the group is unfolded for a user who has not folded it before.
		 */
		@Name(EXPANDED)
		@BooleanDefault(true)
		boolean getExpanded();

		/**
		 * The items gathered under the heading.
		 *
		 * @implNote Written directly as {@code <nav-item>}, {@code <group>}, {@code <header-item>},
		 *           {@code <command-item>} or {@code <separator>} children of the {@code <group>}
		 *           (the {@code <items>} wrapper is optional), keyed by
		 *           {@link SidebarItemConfig#getId()} as the items of the sidebar itself are.
		 */
		@Name(ITEMS)
		@Key(SidebarItemConfig.ID)
		@DefaultContainer
		@TreeProperty
		List<SidebarItemConfig> getItems();
	}

	private final String _id;

	private final ResKey _label;

	private final String _icon;

	private final boolean _expanded;

	private final AccessControl _accessControl;

	private final List<SidebarItemElement> _items;

	/**
	 * Creates a {@link GroupItemElement} from configuration.
	 */
	@CalledByReflection
	public GroupItemElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_label = config.getLabel();
		_icon = config.getIcon();
		_expanded = config.getExpanded();
		_accessControl = config.getAccessControl();
		_items = new ArrayList<>();
		for (SidebarItemConfig itemConfig : config.getItems()) {
			_items.add(context.getInstance(itemConfig));
		}
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		List<ChildGroup> result = new ArrayList<>();
		for (SidebarItemElement item : _items) {
			result.addAll(item.getChildGroups());
		}
		return result;
	}

	@Override
	public SidebarItem createSidebarItem(ViewContext context, ItemSite site) {
		if (!AccessChecks.isAccessible(_accessControl)) {
			// Access denied for the current user: the group and everything in it is omitted.
			return null;
		}
		List<SidebarItem> children = SidebarElement.createItems(_items, context, site);
		String label = Resources.getInstance().getString(_label);
		return new GroupItem(_id, label, _icon, children, _expanded);
	}

}
