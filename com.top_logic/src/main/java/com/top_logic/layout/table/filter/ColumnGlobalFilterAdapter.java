/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.TableViewModel;

/**
 * Adapter for {@link ConfiguredFilter}s of columns to global table {@link Filter}s. It uses the UI
 * of the {@link ConfiguredFilter} of a column, but overrides its value checking.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class ColumnGlobalFilterAdapter<T extends ConfiguredFilter> extends AbstractGlobalFilter implements
		ConfiguredFilter {

	private T columnFilter;

	/**
	 * @param columnFilter
	 *        column filter, whose UI shall be used
	 */
	public ColumnGlobalFilterAdapter(TableViewModel tableViewModel, T columnFilter) {
		super(tableViewModel);
		this.columnFilter = columnFilter;
	}

	@Override
	public final List<Class<?>> getSupportedObjectTypes() {
		return Collections.<Class<?>> singletonList(Object.class);
	}

	@Override
	public final FilterConfiguration getFilterConfiguration() {
		return columnFilter.getFilterConfiguration();
	}

	@Override
	public final void setFilterConfiguration(FilterConfiguration filterConfig) {
		columnFilter.setFilterConfiguration(filterConfig);
	}

	@Override
	public final FilterViewControl<?> getDisplayControl(DisplayContext context, FormGroup form, int filterControlId) {
		return columnFilter.getDisplayControl(context, form, filterControlId);
	}

	@Override
	public final boolean isActive() {
		return columnFilter.isActive();
	}

	@Override
	public void clearFilterConfiguration() {
		columnFilter.clearFilterConfiguration();
	}

	/**
	 * true, if the column filter, used for UI, accepts the given value, false otherwise.
	 */
	public final boolean acceptedByColumnFilter(Object value) {
		return columnFilter.accept(value);
	}

	/**
	 * column filter, whose UI is used for filter display.
	 */
	public final T getColumnFilter() {
		return columnFilter;
	}

	@Override
	public boolean isVisible() {
		return columnFilter.isVisible();
	}

	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		columnFilter.startFilterRevalidation(countableRevalidation);
	}

	@Override
	public void stopFilterRevalidation() {
		columnFilter.stopFilterRevalidation();
	}

	@Override
	public void count(Object value) {
		// Global text filter does not count
	}
}
