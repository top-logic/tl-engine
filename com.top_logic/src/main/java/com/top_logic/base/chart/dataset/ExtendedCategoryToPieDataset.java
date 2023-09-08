/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.dataset;

import org.jfree.chart.util.TableOrder;
import org.jfree.data.category.CategoryToPieDataset;

/**
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExtendedCategoryToPieDataset extends CategoryToPieDataset {

    private ExtendedCategoryDataset dataset;
    private TableOrder              tableOrder;
    private int                     fixIndex; 

    /** Creates a {@link ExtendedCategoryToPieDataset}. */
    public ExtendedCategoryToPieDataset(ExtendedCategoryDataset aSource, TableOrder aExtract, int aIndex) {
        super(aSource, aExtract, aIndex);
        
        this.dataset    = aSource;
        this.tableOrder = aExtract;
        this.fixIndex   = aIndex;
    }

    /**
     * This method returns for the given series and category the stored object.
     */
    public Object getObject (int aSeries) {
        Object result = null;
        if (this.dataset != null) {
            if (this.tableOrder == TableOrder.BY_ROW) {
                result = dataset.getObject(this.fixIndex, aSeries);
            }
            else if (this.tableOrder == TableOrder.BY_COLUMN) {
                result = dataset.getObject(aSeries, this.fixIndex);
            }
        }
        return result;
    }
    
}

