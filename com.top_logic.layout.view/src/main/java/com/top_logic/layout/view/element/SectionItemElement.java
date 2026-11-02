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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.sidebar.SectionItem;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.SidebarElement.ItemSite;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemConfig;
import com.top_logic.layout.view.element.SidebarElement.SidebarItemElement;
import com.top_logic.layout.view.security.AccessChecks;
import com.top_logic.layout.view.security.UnitAccess;
import com.top_logic.layout.view.security.WithAccessControl;
import com.top_logic.util.Resources;

/**
 * A section of a {@link SidebarElement}: items set off from the ones around them by a divider and
 * headed by the section's label where it has one.
 *
 * <p>
 * A section does not fold, its items stand in the sidebar as if they were written at the level of
 * the section. A section none of whose items is currently shown is left out together with its
 * divider and heading, so the sidebar never shows a dividing line with nothing beneath it.
 * </p>
 *
 * <p>
 * A section is addressed by its id: a same-path overlay of the view names the id of an existing
 * section to add items to it, or a new one to add a section.
 * </p>
 */
public class SectionItemElement implements SidebarItemElement {

	/**
	 * Configuration for {@link SectionItemElement}.
	 */
	@TagName("section")
	@TypeIcon("css:bi bi-distribute-vertical")
	public interface Config extends SidebarItemConfig, WithAccessControl {

		@Override
		@ClassDefault(SectionItemElement.class)
		Class<? extends SidebarItemElement> getImplementationClass();

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getItems()}. */
		String ITEMS = "items";

		/**
		 * The heading shown above the section's items; none when unset.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The CSS icon class shown beside the heading (e.g. "bi bi-gear").
		 *
		 * <p>
		 * The icon is what remains of the heading while the sidebar is folded to a narrow strip.
		 * </p>
		 */
		@Name(ICON)
		String getIcon();

		/**
		 * The items of the section.
		 *
		 * @implNote Written directly as {@code <nav-item>}, {@code <group>}, {@code <section>} or
		 *           {@code <command-item>} children of the {@code <section>} (the {@code <items>}
		 *           wrapper is optional), keyed by {@link SidebarItemConfig#getId()} as the items
		 *           of the sidebar itself are.
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

	private final UnitAccess _access;

	private final List<SidebarItemElement> _items;

	/**
	 * Creates a {@link SectionItemElement} from configuration.
	 */
	@CalledByReflection
	public SectionItemElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_label = config.getLabel();
		_icon = config.getIcon();
		_access = UnitAccess.create(context, config.getAccessControl());
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
		if (!AccessChecks.isOffered(_access)) {
			// Access denied for the current user: the section and everything in it is omitted.
			return null;
		}
		List<SidebarItem> children = SidebarElement.createItems(_items, context, site.guardedBy(_access));
		String label = _label == null ? null : Resources.getInstance().getString(_label);
		String icon = _icon == null || _icon.isEmpty() ? null : _icon;
		SectionItem result = new SectionItem(_id, label, icon, children);
		SidebarElement.withholdWhileDenied(result, _access, context, site);
		return result;
	}

}
