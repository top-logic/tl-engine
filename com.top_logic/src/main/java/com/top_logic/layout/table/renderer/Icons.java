/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import java.awt.Color;

import com.top_logic.gui.ThemeVar;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.DefaultValue;
import com.top_logic.layout.basic.IconsBase;
import com.top_logic.layout.basic.TemplateType;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.table.TableRenderer.RenderState;
import com.top_logic.layout.table.renderer.DefaultTableRenderer.DefaultRenderState;

/**
 * Icon constants for this package.
 *
 * @see ThemeImage
 */
@SuppressWarnings("javadoc")
public class Icons extends IconsBase {

	public static ThemeImage EMPTY;

	/**
	 * Image to filter a table.
	 */
	public static ThemeImage FILTER;

	public static ThemeImage FILTER_GLOBAL;

	public static ThemeImage FILTER_GLOBAL_ACTIVE;

	public static ThemeImage FILTER_OPTIONS;

	/**
	 * Image to show that the table is sortable.
	 */
	public static ThemeImage SORTABLE;

	/**
	 * Image to show descending sort direction when the column is not filterable.
	 */
	public static ThemeImage SORT_DESC;

	/**
	 * Image to show ascending sort direction when the column is not filterable.
	 */
	public static ThemeImage SORT_ASC;

	/**
	 * Image, next to the filter image, to show ascending sort direction.
	 */
	public static ThemeImage SORT_ASC_SMALL;

	/**
	 * Image, next to the filter image, to show descending sort direction.
	 */
	public static ThemeImage SORT_DESC_SMALL;

	public static ThemeImage REMOVE_FILTER;

	public static ThemeImage REMOVE_FILTER_DISABLED;

	public static ThemeImage RESET_TABLE;

	public static ThemeImage SORT_BUTTON_ICON;

	public static ThemeImage TBL_COLUMN_CONFIG;

	public static ThemeImage TBL_FIRST;

	public static ThemeImage TBL_FIRST_DISABLED;

	public static ThemeImage TBL_LAST;

	public static ThemeImage TBL_LAST_DISABLED;

	public static ThemeImage TBL_NEXT;

	public static ThemeImage TBL_NEXT_DISABLED;

	public static ThemeImage TBL_PREV;

	public static ThemeImage TBL_PREV_DISABLED;

	public static ThemeImage TRUE_DISABLED;

	public static ThemeImage LOAD_NAMED_TABLE_SETTINGS;

	public static ThemeImage EDIT_NAMED_TABLE_SETTINGS;

	/**
	 * Width of the border of a table column.
	 */
	@DefaultValue("1px")
	public static ThemeVar<DisplayDimension> TABLE_COLUMN_BORDER_WIDTH;

	/**
	 * Template defining the table container.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_CONTAINER_TEMPLATE;

	/**
	 * Template for the table title row.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_TITLE_TEMPLATE;

	/**
	 * Template defining a table body.
	 */
	@TemplateType(RenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_BODY_TEMPLATE;

	/**
	 * Template defining a table header.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_HEADER_TEMPLATE;

	/**
	 * Template defining a table body row.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_BODY_ROW_TEMPLATE;

	/**
	 * Template defining a table body row cell.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_BODY_CELL_TEMPLATE;

	/**
	 * Template defining a table header row.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_HEADER_ROW_TEMPLATE;

	/**
	 * Template defining a table header row cell.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_HEADER_CELL_TEMPLATE;

	/**
	 * Template defining a table header row cell content.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_HEADER_CELL_CONTENT_TEMPLATE;

	/**
	 * Template defining a table header row group cell content.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_HEADER_GROUP_CELL_CONTENT_TEMPLATE;

	/**
	 * Template defining the table footer.
	 */
	@TemplateType(DefaultRenderState.class)
	public static ThemeVar<HTMLTemplateFragment> TABLE_FOOTER_TEMPLATE;

	/**
	 * Color of the grabber inside a table header cell to resize a table column.
	 */
	@DefaultValue("#FFFFFF")
	public static ThemeVar<Color> TABLE_RESIZE_GRABBER_COLOR;

	/**
	 * Background color of a table cell.
	 */
	@DefaultValue("#FFFFFF")
	public static ThemeVar<Color> TABLE_CELL_BACKGROUND_COLOR;

	/**
	 * Background color of a table.
	 */
	@DefaultValue("#FFFFFF")
	public static ThemeVar<Color> TABLE_BACKGROUND_COLOR;

	/**
	 * Width of the tables separator which separates the tables fixed and flexible area.
	 */
	@DefaultValue("7px")
	public static ThemeVar<DisplayDimension> TABLE_SEPARATOR_WIDTH;


}
