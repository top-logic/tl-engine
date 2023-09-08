/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

/**
 * This interface provides basic methods for providing styles for cells.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public interface POICellStyleProvider {

	/**
	 * Returns the style to be used for the specified cell containing the specified value.
	 * 
	 * <p>
	 * There are times when a cell's style not only depends on the contexts (i.e. the location of
	 * the cell) but also on the cell's content. However, once the cell's content is set, there is
	 * no way to tell what model was used to set the content before it was formatted. That's why the
	 * additional parameter is needed.
	 * </p>
	 * 
	 * @param cell
	 *        the {@link Cell} to retrieve the style for
	 * @param rowObject
	 *        the {@link Object} to retrieve the cell style for
	 * @param property
	 *        the name of the property to retrieve the cell style for
	 * @param value
	 *        the value to retrieve the cell style for or {@code null}
	 * @return the style for the specified cell or {@code null} if no style is available
	 */
	public CellStyle getCellStyle(final Cell cell, final Object rowObject, final String property, final Object value);
}
