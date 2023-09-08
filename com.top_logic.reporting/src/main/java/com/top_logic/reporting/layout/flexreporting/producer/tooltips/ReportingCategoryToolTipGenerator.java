/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.producer.tooltips;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.CategoryDataset;


/**
 * ToolTipGenerator for Charts in Reporting.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class ReportingCategoryToolTipGenerator extends ReportingTooltipGenerator implements CategoryToolTipGenerator {

	@Override
	public String generateToolTip(CategoryDataset dataset, int row, int column) {
		String rowKey = (String) dataset.getRowKey(row);
		String colKey = (String) dataset.getColumnKey(column);
		Number theVal = dataset.getValue(row, column);
		if(theVal == null) {
			theVal = Double.valueOf(0.0);
		}
		
		return buildTooltip(rowKey, colKey, theVal);
	}
}
