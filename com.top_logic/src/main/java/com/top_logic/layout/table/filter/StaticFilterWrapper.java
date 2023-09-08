/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.form.model.FormGroup;

/**
 * A wrapper for static filters.
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class StaticFilterWrapper extends AbstractConfiguredFilter {

	private StaticFilterWrapperConfiguration config;

	private StaticFilterWrapperViewBuilder viewBuilder;
	private Filter<Object> staticFilter;

	private final List<Class<?>> supportedTypes;
	
	private SingleOptionMatchCounter counter;

	/**
	 * Create a new StaticFilterWrapper with a {@link StaticFilterWrapperView}.
	 * 
	 * @param staticFilter - the static filter, must not be null
	 * @param filterDescription - the name of the filter
	 */
	public StaticFilterWrapper(Filter<Object> staticFilter, DisplayValue filterDescription) {
		this(staticFilter, filterDescription, Collections.singletonList(Object.class),
			new StaticFilterWrapperViewBuilder());
	}
	
	/**
	 * Create a new StaticFilterWrapper.
	 * 
	 * @param staticFilter - the static filter, must not be null
	 * @param filterDescription - the name of the filter
	 * @param supportedTypes - list of types, which the filter can handle, must not be null
	 */
	public StaticFilterWrapper(Filter<Object> staticFilter, DisplayValue filterDescription,
							   List<Class<?>> supportedTypes,
			StaticFilterWrapperViewBuilder viewBuilder) {
		config = new StaticFilterWrapperConfiguration(filterDescription);
		this.viewBuilder = viewBuilder;
		this.supportedTypes = supportedTypes;
		this.staticFilter = staticFilter;
		counter = SingleEmptyValueMatchCounter.INSTANCE;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		return supportedTypes;
	}

	@Override
	public FilterViewControl<?> getDisplayControl(DisplayContext context, FormGroup form, int filterControlId) {
		return viewBuilder.createFilterViewControl(context, config, form, filterControlId);
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public FilterConfiguration getFilterConfiguration() {
		return config;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return config.isActive();
	}

	/** 
	 * {@inheritDoc})
	 */
	@Override
	public void setFilterConfiguration(FilterConfiguration filterConfig) {
		assert filterConfig != null : "Filter configuration must not be null!";
		assert filterConfig instanceof StaticFilterWrapperConfiguration : "Filter configuration must be a valid static filter wrapper configuration";
		config = (StaticFilterWrapperConfiguration)filterConfig;
	}

	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		if (countableRevalidation) {
			counter = new DefaultSingleOptionMatchCounter();
		}
	}

	@Override
	public void stopFilterRevalidation() {
		config.setMatchCount(counter.getMatchCount());
		counter = SingleEmptyValueMatchCounter.INSTANCE;
	}

	@Override
	public void count(Object value) {
		if (accept(value)) {
			counter.increaseCounter();
		}
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean accept(Object anObject) {
		return staticFilter.accept(anObject);
	}

	@Override
	public void clearFilterConfiguration() {
		config.clearConfiguration();
	}
}