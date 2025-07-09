/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.reactive_tag;

import java.util.List;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.model.annotate.LabelPosition;

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
	 * Customization for the {@link LabelPosition} of the whole group, or <code>null</code> if not
	 * set.
	 * 
	 * @return Custom label position, or <code>null</code> if not set.
	 */
	LabelPosition getLabelPosition();

	/**
	 * Whether a border should be drawn around the group cell. The default value is provided by the
	 * theme.
	 */
	@TemplateVariable("hasBorder")
	boolean hasBorder();

	/**
	 * Whether this group is a pure container of other groups.
	 * 
	 * <p>
	 * A group container has different visual properties that sets it apart from its content groups.
	 * </p>
	 */
	@TemplateVariable("isContainer")
	boolean isContainer();

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
	 * Command buttons rendered directly in the group header.
	 */
	List<CommandModel> getCommands();

	/**
	 * The menu of commands.
	 */
	Menu getMenu();

}
