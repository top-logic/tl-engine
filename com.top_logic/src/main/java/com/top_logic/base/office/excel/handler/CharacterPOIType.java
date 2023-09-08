/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;

/**
 * Set character values to excel cells via POI.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CharacterPOIType extends StringPOIType {

	@Override
	public Class<?> getHandlerClass() {
		return Character.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		return super.setValue(aCell, aWorkbook, ((Character) aValue).toString(), aSupport);
	}
}

