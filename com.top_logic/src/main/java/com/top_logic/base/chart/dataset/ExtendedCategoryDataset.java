/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart.dataset;

import java.util.HashMap;

import org.jfree.data.category.DefaultCategoryDataset;

/**
 * The ExtendedCategoryDataset extends the {@link org.jfree.data.category.DefaultCategoryDataset}. The 
 * ExtendedCategoryDataset stores additionally objects.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExtendedCategoryDataset extends DefaultCategoryDataset {

    /**
     * The map contains objects. The keys can be generated with the {@link #getKey(Comparable, Comparable)} 
     * method. The values are {@link Object}s.
     */
    private HashMap objects;
    
    /** 
     * Creates a {@link ExtendedCategoryDataset}.
     */
    public ExtendedCategoryDataset () {
        this.objects = new HashMap();
    }
    
    /**
     * This method works like the {@link DefaultCategoryDataset#addValue(double, java.lang.Comparable, java.lang.Comparable)}
     * method, except it stores additionally a objects for the given series and category.
     */
    public void addValue(Number aValue, Comparable aSeries, Comparable aCategory, Object anObject) {
        this.objects.put(getKey(aSeries, aCategory), anObject);
        super.addValue(aValue, aSeries, aCategory);
    }
    
    /**
     * This method works like the {@link DefaultCategoryDataset#addValue(double, java.lang.Comparable, java.lang.Comparable)}
     * method, except it stores additionally a objects for the given series and category.
     */
    public void addValue(double aValue, Comparable aSeries, Comparable aCategory, Object anObject) {
        addValue(Double.valueOf(aValue), aSeries, aCategory, anObject);
        //super.addValue(aValue, aSeries, aCategory);
    }
    
    /**
     * This method returns for the given series and category the stored object.
     */
    public Object getObject (int aSeries, int aCategory) {
        Comparable theSeriesKey   = getRowKey(aSeries);
        Comparable theCategoryKey = getColumnKey(aCategory);
        
        return getObject(theSeriesKey, theCategoryKey);
    }
    
    /**
     * This method returns for the given series and category the stored object.
     */
    public Object getObject (Comparable aSeries, Comparable aCategory) {
        return this.objects.get(getKey(aSeries, aCategory));
    }
    
    /**
     * This method returns a key for the given series and category. The keys are
     * used for the {@link #objects}.
     */
    private String getKey (Comparable aSeriers, Comparable aCategory) {
        return aSeriers.toString() + '-' + aCategory.toString();
    }
    
}

