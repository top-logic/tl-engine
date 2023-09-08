/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.CellSizeFormular;
import com.top_logic.base.office.excel.Formula;
import com.top_logic.base.office.excel.POITypeSupporter;

/**
 * The {@link FormulaPOIType} stores {@link Formula}s in excel sheets.
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class FormulaPOIType implements PoiTypeHandler {

	private Class<?> clazz;

	/**
	 * Creates a new {@link FormulaPOIType}.
	 * 
	 * @param clazz
	 *            The class for this handler. The class of the value (e.g.
	 *            Formular, String,...). Must NOT be <code>null</code>.
	 */
	public FormulaPOIType(Class<?> clazz) {
		super();
		this.clazz = clazz;
	}

	@Override
	public Class<?> getHandlerClass() {
		return this.clazz;
	}

	@Override
	public int setValue(Cell cell, Workbook workbook, Object value, POITypeSupporter aSupport) {
		cell.setCellType(CellType.FORMULA);
		int width = 10;

		Formula formula = (Formula) value;

		if (value != null) {
			cell.setCellFormula(formula.getFormulaAsString());
		}

		if (formula instanceof CellSizeFormular) {
			width = ((CellSizeFormular) formula).getSize();
		}

		return width;
	}

}
