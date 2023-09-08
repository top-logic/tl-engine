/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormGroup;

/**
 * An basic interface for filters with an modifiable internal configuration.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public interface ConfiguredFilter extends Filter<Object> {

	/**
	 * This method determines the object types, which can be handled by this filter.
	 * 
	 * <p>
	 * <b>Note:</b>
	 * In case the filter is used to handle unsupported object types {@link RuntimeException} may occur.
	 * </p>
	 * 
	 * @return list of supported object types.
	 */
	public List<Class<?>> getSupportedObjectTypes();
	
	/**
	 * This method returns the filter configuration
	 * 
	 * @return filter configuration
	 */
	public FilterConfiguration getFilterConfiguration();

	/**
	 * This method sets the filter configuration
	 * 
	 * @param filterConfig - the filter configuration
	 */
	public void setFilterConfiguration(FilterConfiguration filterConfig);
	
	/**
	 * Resets the {@link ConfiguredFilter}s {@link FilterConfiguration} to it's "empty" state
	 */
	public void clearFilterConfiguration();

	/**
	 * The displayable control (view) of the filter
	 * 
	 * @param context
	 *        The display context
	 * @param form
	 *        The filter form to fill with input elements.
	 * @param filterControlId
	 *        TODO
	 * @return the control based view of the filter
	 */
	public FilterViewControl<?> getDisplayControl(DisplayContext context, FormGroup form, int filterControlId);
	
	/**
	 * This method determines, whether the filter is active, or not
	 * 
	 * @return true, if the filter is active, false otherwise
	 */
	public boolean isActive();

	/**
	 * true, if this {@link ConfiguredFilter} is displayed at GUI, false otherwise
	 */
	public boolean isVisible();

	/**
	 * Called at begin of table model / filter revalidation process, used for preparation purposes
	 * (e.g. allocation of caches).
	 * 
	 * @param countableRevalidation
	 *        - matches of following filter revalidation can be counted
	 */
	public void startFilterRevalidation(boolean countableRevalidation);

	/**
	 * Called at the end of table model / filter revalidation process, used for cleanup of caches
	 * and filter model updates.
	 */
	public void stopFilterRevalidation();

	/**
	 * Called, when internal matching {@link ConfiguredFilter}s, shall increment their match counter
	 * by 1 during filter process.
	 * 
	 * @param value
	 *        for that the internal counter shall be increased
	 */
	public void count(Object value);
}
