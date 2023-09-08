/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.generator.item;

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.data.category.CategoryDataset;

import com.top_logic.base.chart.dataset.ExtendedCategoryDataset;

/**
 * The ExtendedCategoryItemLabelGenerator works together with the {@link ExtendedCategoryDataset}.
 * The ExtendedCategoryDataset stores objects (e.g. goals, risks, ...). A subclass needs only to implement 
 * this method {@link #getItemLabelFor(Object)}. This object is that from the ExtendedCategoryDataset.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public abstract class ExtendedCategoryItemLabelGenerator implements
        CategoryItemLabelGenerator {

    /** 
     * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateColumnLabel(org.jfree.data.category.CategoryDataset, int)
     */
    @Override
	public String generateColumnLabel(CategoryDataset aDataset, int aColumn) {
        return aDataset.getColumnKey(aColumn).toString();   
    }

    /** 
     * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateLabel(org.jfree.data.category.CategoryDataset, int, int)
     */
    @Override
	public String generateLabel(CategoryDataset aDataset, int aRow, int aColumn) {
        String theItemLabel = "";
        
        if (aDataset instanceof ExtendedCategoryDataset) {
            ExtendedCategoryDataset theDataset = (ExtendedCategoryDataset) aDataset;
            
            theItemLabel = getItemLabelFor(theDataset.getObject(aRow, aColumn));
        } 
        
        return theItemLabel;
    }


    /** 
     * @see org.jfree.chart.labels.CategoryItemLabelGenerator#generateRowLabel(org.jfree.data.category.CategoryDataset, int)
     */
    @Override
	public String generateRowLabel(CategoryDataset aDataset, int aRow) {
        return aDataset.getRowKey(aRow).toString();
    }
    
    /**
     * This method returns the item label for the given object.
     * 
     * @param aObject
     *        A object.
     */
    protected abstract String getItemLabelFor(Object aObject);

}

