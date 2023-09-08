/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;


/**
 * Hull for binding JFreeChart with items represented in the different series and categories together.
 * 
 * This object will be used by the {@link ExtendedProducerChartComponent} to get a mapping between
 * the light weighted JFreeChart model and the values represented in the different chart elements.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ChartData<T> {

    /** The chart to be displayed. */
    private JFreeChart chart;

    /** The values mapped to the series and categories. */
	protected final Map<String, Set<T>> values;

    /** The type of held objects in {@link #values}. */
    private final Class<T> clazz;

    /** 
     * Creates a {@link ChartData}.
     */
    public ChartData() {
        this(null);
    }

    /** 
     * Creates a {@link ChartData}.
     * 
     * @param    aClass    The type of held objects in {@link #values}, must not be <code>null</code>.
     */
    public ChartData(Class<T> aClass) {
        this(null, aClass);
    }

    /** 
     * Creates a {@link ChartData}.
     * 
     * @param    aChart    The represented chart, may be set later on via {@link #setChart(JFreeChart)}.
     * @param    aClass    The type of held objects in {@link #values}, must not be <code>null</code>.
     */
    public ChartData(JFreeChart aChart, Class<T> aClass) {
        this.chart  = aChart;
        this.values = new HashMap<>();
        this.clazz  = aClass;
    }

    /** 
     * Return the held chart.
     * 
     * @return    The requested chart, may be <code>null</code>, when not set before.
     */
    public JFreeChart getChart() {
        return this.chart;
    }

    /** 
     * Set the chart to be used in this data object.
     * 
     * @param    aChart    The represented chart, must not be <code>null</code>.
     */
    public void setChart(JFreeChart aChart) {
        this.chart = aChart;
    }

    /** 
     * Return the items located at the given position.
     * 
     * This position string will be provided by the {@link #getID(int, int)} method.
     * 
     * @param    anID    The string representation of the requested position, must not be <code>null</code>
     * @return   The requested set of items, may be <code>null</code>.
     */
    public Set<T> getItems(String anID) {
        return this.values.get(anID);
    }

    /** 
     * Return the items located at the given position.
     * 
     * This position string will be provided by the {@link #getID(int, int)} method.
     * @param    aSeries      The series to get the ID for.
     * @param    aCategory    The category to get the ID for.
     * @return   The requested set of items, may be <code>null</code>.
     */
    public Set<T> getItems(int aSeries, int aCategory) {
    	return this.values.get(getID(aSeries, aCategory));
    }

    /** 
     * Return the available keys in the held {@link #values}.
     * 
     * @return    The set of known keys.
     */
    public Set<String> getKeys() {
        return this.values.keySet();
    }

    /** 
     * Add an item to the {@link #values}.
     * 
     * @param    aSeries      The series the items belong to.
     * @param    aCategory    The category the items belong to.
     * @param    someItems    The items to be added, must not be <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public void addItems(int aSeries, int aCategory, Collection<?> someItems) {
        String theID  = this.getID(aSeries, aCategory);
        Set<T> theSet = this.values.get(theID);

        if (theSet == null) {
            theSet = new HashSet<>();

            this.values.put(theID, theSet);
        }

        if (this.clazz != null) {
            for (Object theObject : someItems) {
                if (this.clazz.isAssignableFrom(theObject.getClass())) {
                    theSet.add((T) theObject);
                }
            }
        }
        else {
            theSet.addAll((Collection<? extends T>) someItems);
        }
    }

    /** 
     * Return a unique ID for the given series and category.
     * 
     * @param    aSeries      The series to get the ID for.
     * @param    aCategory    The category to get the ID for.
     * @return   The requested ID, never <code>null</code>.
     */
    public String getID(int aSeries, int aCategory) {
        return Integer.toString(aSeries) + ':' + Integer.toString(aCategory);
    }

}
