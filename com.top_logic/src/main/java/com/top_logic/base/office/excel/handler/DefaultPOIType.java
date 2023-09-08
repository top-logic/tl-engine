/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Default implementation for all {@link Object} values.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DefaultPOIType extends StringPOIType {

	@Override
	public Class<?> getHandlerClass() {
		return Object.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		if (aValue != null) {
			return super.setValue(aCell, aWorkbook, MetaLabelProvider.INSTANCE.getLabel(aValue), aSupport);
		}
		else { 
			return super.setValue(aCell, aWorkbook, "", aSupport);
		}
	}
}

