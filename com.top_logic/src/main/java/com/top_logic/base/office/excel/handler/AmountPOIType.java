/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

import com.top_logic.base.office.POIUtil;
import com.top_logic.base.office.excel.POITypeSupporter;
import com.top_logic.knowledge.wrap.currency.Amount;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Set amount values to excel cells via POI.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AmountPOIType extends RichTextStringPOIType {

	@Override
	public Class<?> getHandlerClass() {
		return Amount.class;
	}

	@Override
	public int setValue(Cell aCell, Workbook aWorkbook, Object aValue, POITypeSupporter aSupport) {
		Amount theAmount = (Amount) aValue;
		String theString = MetaLabelProvider.INSTANCE.getLabel(theAmount);

		return super.setValue(aCell, aWorkbook, POIUtil.newRichTextString(aCell, theString), aSupport);
	}
}
