/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.template.WithProperties;

/**
 * Settings for the {@link GroupCellControl}.
 */
public interface GroupSettings extends WithProperties {

	/**
	 * The maximum number of columns of the group cell.
	 */
	@TemplateVariable("columns")
	int getColumns();

	/**
	 * The custom CSS class to add to the generated group cell.
	 */
	@TemplateVariable("cssClass")
	String getCssClass();

	/**
	 * CSS style to add to the generated group cell.
	 */
	@TemplateVariable("style")
	String getStyle();

	/**
	 * Whether the form group is rendered over the whole line, even when it is in a multi-column
	 * container.
	 */
	@TemplateVariable("wholeLine")
	boolean isWholeLine();

	/**
	 * Whether the label of all inner elements are rendered above their input.
	 * 
	 * @see #getLabelInline()
	 */
	@TemplateVariable("labelAbove")
	boolean getLabelAbove();

	/**
	 * Whether the label of all inner elements are rendered in the same line as their input.
	 * 
	 * @see #getLabelAbove()
	 */
	@TemplateVariable("labelInline")
	boolean getLabelInline();

	/**
	 * Whether a border should be drawn around the group cell. The default value is provided by the
	 * theme.
	 */
	@TemplateVariable("hasBorder")
	boolean hasBorder();

	/**
	 * Whether the group cell has a legend to be rendered.
	 */
	@TemplateVariable("hasLegend")
	boolean hasLegend();

	/**
	 * Whether the group is draggable.
	 */
	@TemplateVariable("draggable")
	boolean isDraggable();

	/**
	 * The data ID for a custom <code>data-id</code> HTML attribute.
	 */
	@TemplateVariable("dataId")
	String getDataId();

	/**
	 * Whether the group should never be collapsed.
	 */
	@TemplateVariable("collapsible")
	boolean isCollapsible();

	/**
	 * The number of rows over which the group should stretch.
	 */
	@TemplateVariable("rows")
	int getRows();

	/**
	 * The width of the group.
	 */
	@TemplateVariable("width")
	String getWidth();

	/**
	 * The menu of commands.
	 */
	Menu getMenu();

	/**
	 * Whether there is a menu.
	 */
	@TemplateVariable("hasMenu")
	default boolean hasMenu() {
		return getMenu() != null;
	}

	/**
	 * The {@link ThemeImage} to open the menu.
	 */
	@TemplateVariable("menuButton")
	ThemeImage getCommandButton();

}
