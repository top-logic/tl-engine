/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfig;

/**
 * Renderer that is used for Excel export.
 * 
 * @see ColumnConfig#getExcelRenderer()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExcelCellRenderer {
	
	/**
	 * Context object delivering all necessary informations for the {@link ExcelCellRenderer} about
	 * the value to create the result {@link ExcelValue}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface RenderContext {

		/**
		 * The table being exported.
		 */
		TableModel model();

		/**
		 * The {@link Column} of the cell to export.
		 */
		Column modelColumn();

		/**
		 * The column in the resulting Excel sheet to write to.
		 */
		int excelColumn();

		/**
		 * The row in the table model.
		 */
		int modelRow();

		/**
		 * The row in the resulting Excel sheet to write to.
		 */
		int excelRow();

		/**
		 * The custom context object for the using {@link ExcelCellRenderer}.
		 * 
		 * <p>
		 * The returned object is the object formerly requested by
		 * {@link ExcelCellRenderer#newCustomContext(TableModel, Column)}.
		 * </p>
		 * 
		 * @see ExcelCellRenderer#newCustomContext(TableModel, Column)
		 */
		Object getCustomContext();

		/**
		 * value, stored in cell of defined row and column
		 */
		Object getCellValue();

	}


	/**
	 * Renders a table cell into an {@link ExcelValue}.
	 * 
	 * @param context
	 *        The {@link RenderContext context} object to receive all necessary informations from.
	 * 
	 * @return The Excel cell description.
	 */
	ExcelValue renderCell(RenderContext context);

	/**
	 * Determines how many columns in the excel are used by the values written by this renderer.
	 * 
	 * @param model
	 *        The table currently exported.
	 * @param modelColumn
	 *        The column which is exported by this renderer.
	 * 
	 * @return A value &gt;0.
	 */
	int colSpan(TableModel model, Column modelColumn);

	/**
	 * Returns a custom context object that can be accessed in
	 * {@link ExcelCellRenderer#renderCell(RenderContext)} from the given {@link RenderContext}.
	 * 
	 * <p>
	 * This method is called once during export process to create the {@link RenderContext} later
	 * given in {@link #renderCell(RenderContext)}. The {@link ExcelCellRenderer} is free to use it
	 * or not.
	 * </p>
	 * 
	 * @param model
	 *        The {@link TableViewModel} that is exported.
	 * @param modelColumn
	 *        The column which is exported by this renderer
	 * 
	 * @return May be <code>null</code>.
	 * 
	 * @see RenderContext#getCustomContext()
	 */
	Object newCustomContext(TableModel model, Column modelColumn);

}
