/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.List;
import java.util.Map;

/**
 * A section of the sidebar: items set off from their neighbours by a divider and headed by a label
 * where the section has one.
 *
 * <p>
 * A section has no button and does not fold; its items appear as if they stood at the level of the
 * section. A section none of whose items is currently drawn is not drawn either: neither its
 * heading nor a divider remains, so a sidebar never shows a divider in front of nothing.
 * </p>
 */
public class SectionItem extends ContainerItem {

	/** Type discriminator for section items. */
	public static final String TYPE_SECTION = "section";

	private final String _label;

	private final String _icon;

	/**
	 * Creates a new {@link SectionItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The heading, or {@code null} for a section without heading.
	 * @param icon
	 *        The icon CSS class of the heading, or {@code null} for no icon.
	 * @param children
	 *        The items within this section.
	 */
	public SectionItem(String id, String label, String icon, List<SidebarItem> children) {
		super(id, children);
		_label = label;
		_icon = icon;
	}

	/**
	 * The heading, or {@code null}.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * The icon CSS class of the heading, or {@code null}.
	 */
	public String getIcon() {
		return _icon;
	}

	@Override
	public String getType() {
		return TYPE_SECTION;
	}

	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		if (_label != null) {
			map.put(LABEL, _label);
		}
		if (_icon != null) {
			map.put(ICON, _icon);
		}
		return map;
	}

}
