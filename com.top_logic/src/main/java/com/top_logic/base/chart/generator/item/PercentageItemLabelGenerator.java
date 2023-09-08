/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.item;

import java.text.DecimalFormat;
import java.text.Format;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

/**
 * The PercentageItemLabelGenerator appends to the standard item label 
 * a space (optional) and a percentage character 
 * (<code>item label = 5,35 => 5,35 %</code>).
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class PercentageItemLabelGenerator implements CategoryItemLabelGenerator {

    /**
     * Indicates whether a space is appended behind the standard item label
     * (e.g. <code>5,36 %</code>). In the other case the the percentage
     * character is appended directly (e.g. <code>5,36%</code>).
     */
    private boolean       space;

    /** The decimal format is used to format the value. */
	private Format format;

    /**
     * Creates a {@link PercentageItemLabelGenerator} with the
     * given parameters.
     * 
     * @param withSpace
     *        Indicates whether a space is appended behind the standard item
     *        label.
     */
    public PercentageItemLabelGenerator(boolean withSpace) {
        this(withSpace, new DecimalFormat("##0.##"));
    }
    
    /**
     * Creates a {@link PercentageItemLabelGenerator} with the
     * given parameters.
     * 
     * @param withSpace
     *        Indicates whether a space is appended behind the standard item
     *        label.
     * @param format
     *        A format which is used to format the values.
     *        Must not be <code>null</code>.
     */
    public PercentageItemLabelGenerator(boolean withSpace, Format format) {
        this.space  = withSpace;
        this.format = format;
    }

    @Override
	public String generateColumnLabel(CategoryDataset aDataset, int aColumn) {
        return aDataset.getColumnKey(aColumn).toString();
    }

    @Override
	public String generateLabel(CategoryDataset aDataset, int aRow, int aColumn) {
        StringBuffer theBuffer = new StringBuffer();
        Number       theValue  = getValue(aDataset, aRow, aColumn);
        theBuffer.append(format.format(theValue));
        if (space) {
            theBuffer.append(' ');
        }
        theBuffer.append('%');
        
        return theBuffer.toString();
    }

    /**
     * This method returns the value for the given dataset, row and column.
     * 
     * @param aDataset
     *        A {@link CategoryDataset}.
     * @param aRow
     *        A row index.
     * @param aColumn
     *        A column index.
     */
    public Number getValue(CategoryDataset aDataset, int aRow, int aColumn) {
        return aDataset.getValue(aRow, aColumn);
    }

    @Override
	public String generateRowLabel(CategoryDataset aDataset, int aRow) {
        return aDataset.getRowKey(aRow).toString();
    }

}

