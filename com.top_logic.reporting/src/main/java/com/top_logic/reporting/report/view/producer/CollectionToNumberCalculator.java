/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.view.producer;

import java.util.Collection;

import com.top_logic.reporting.report.view.name.Namable;

/**
 * A {@link CollectionToNumberCalculator} calculates for a collection of objects 
 * one number.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface CollectionToNumberCalculator extends Namable {

	/**
	 * This method returns <code>true</code> the total value should be shown,
	 * otherwise <code>false</code>.
	 * 
	 * @return Returns is the total value should be shown.
	 */
	public boolean showTotalValue();
	
	/**
	 * This method returns the total value. Check before this method is calod if
	 * the total value should be shown with the method {@link #showTotalValue()}.
	 * 
	 * @return Returns the total value.
	 */
	public double getTotalValue();
	
	/** 
	 * This method returns the x-axis label. 
	 * 
	 * @return Returns the x-axis label.
	 */
	public String getXAxisLabel();
	
	/** 
	 * This method returns the y-axis label. 
	 * 
	 * @return Returns the y-axis label.
	 */
	public String getYAxisLabel();
	
	/**
	 * This method calculates for a collection of objects one number.
	 * 
	 * @param someObjects
	 *            A collection of objects must NOT be <code>null</code> but
	 *            maybe empty.
	 * @return Returns a number for the collection.
	 */
	public double calculateNumberFor(Collection<Object> someObjects);

	/** 
	 * This method resets the calculator to reuse it. 
	 */
	public void reset();
}
