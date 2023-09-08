/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.Collection;

/**
 * This {@link AbstractCollectionToNumberCalculator} calculates the values for the collection
 * and stores additionally the total value. The default to show the total value is 
 * <code>true</code>.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractCollectionToNumberCalculator implements CollectionToNumberCalculator {

    private double totalValue;

	/** 
	 * Creates a {@link AbstractCollectionToNumberCalculator}.
	 */
	public AbstractCollectionToNumberCalculator() {
		this.totalValue = 0;
	}

	/** 
     * See {@link #calculateNumberFor(Collection)}.
     */
	public abstract double calculateNumber(Collection<Object> someRiskItems);

	@Override
	public double calculateNumberFor(Collection<Object> someObjects) {
		double theValue = this.calculateNumber(someObjects);
		
		this.addToTotalValue(theValue);
		
		return theValue;
	}
	
	@Override
	public boolean showTotalValue() {
		return true;
	}
	
	@Override
	public double getTotalValue() {
		return this.totalValue;
	}

	/** 
	 * This method sets the total value.
	 * 
	 * @param    aValue    A total value.
	 */
	public void setTotalValue(double aValue) {
		this.totalValue = aValue;
	}
	
	/**
	 * This method adds the given value to the total value. Negative values are
	 * permitted.
	 * 
	 * @param    aValue    A value maybe negative.
	 */
	public void addToTotalValue(double aValue) {
		this.totalValue += aValue;
	}
	
	/** 
	 * This method resets the total value. 
	 */
	@Override
	public void reset() {
		this.setTotalValue(0);
	}
}
