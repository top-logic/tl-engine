/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Instances of implementing classes are responsible for providing the values for cells.
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public interface POICellValueProvider {

	/**
	 * Sets the value of the specified cell to be the specified business object.
	 * 
	 * @param cell
	 *        the {@link Cell} to set the value for
	 * @param value
	 *        the {@link Object} to set or {@code null}
	 */
	public void setCellValue(final Cell cell, final Object value);
}
