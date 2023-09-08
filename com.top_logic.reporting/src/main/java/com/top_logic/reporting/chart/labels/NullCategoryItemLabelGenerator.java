/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.labels;

import java.text.NumberFormat;

import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * @author     <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
public class NullCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {

    public NullCategoryItemLabelGenerator() {
        super();
    }
    
    public NullCategoryItemLabelGenerator(NumberFormat aNumberFormat) {
        super(DEFAULT_LABEL_FORMAT_STRING, aNumberFormat);
    }
    
    public NullCategoryItemLabelGenerator(String aLabelFormat, NumberFormat aNumberFormat) {
        super(aLabelFormat, aNumberFormat);
    }
    
    @Override
	public String generateColumnLabel(CategoryDataset dataset, int column) {
        return super.generateColumnLabel(dataset, column);
    }

    @Override
	public String generateLabel(CategoryDataset dataset, int row, int column) {
        
        Object theValue = dataset.getValue(row, column);
        if (theValue == null) {
            return "";
        }
        else {
            return super.generateLabel(dataset, row, column);
        }
    }

    @Override
	public String generateRowLabel(CategoryDataset dataset, int row) {
        return super.generateRowLabel(dataset, row);
    }

}
