/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ProxyListModelBuilder} that filters the list provided by its {@link Config#getInner()
 * delegate}.
 * 
 * @see Config#getFilter()
 * @see Config#getInner()
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FilteringListModelBuilder extends ProxyListModelBuilder<FilteringListModelBuilder.Config> {

	/** {@link ConfigurationItem} for the {@link FilteringListModelBuilder}. */
	public interface Config extends ProxyListModelBuilder.Config {

		/** Property name of {@link #getFilter()}. */
		String FILTER = "filter";

		/** Only elements matching this {@link Filter} are returned as model. */
		@Name(FILTER)
		PolymorphicConfiguration<Filter<Object>> getFilter();

	}

	private final Filter<Object> _filter;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link FilteringListModelBuilder}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public FilteringListModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
		_filter = context.getInstance(config.getFilter());
	}

	@Override
	public List<?> getModel(Object businessModel, LayoutComponent component) {
		Collection<?> model = super.getModel(businessModel, component);
		return FilterUtil.filterList(_filter, model);
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		boolean unfilteredSupports = super.supportsListElement(contextComponent, listElement);
		return unfilteredSupports && _filter.accept(listElement);
	}

}
