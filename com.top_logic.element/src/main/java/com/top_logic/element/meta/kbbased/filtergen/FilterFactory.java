/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.element.meta.form.EditContext;

/**
 * Get Filter for Wrapper based MetaAttributed.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
@Deprecated
@ServiceDependencies({ ApplicationConfig.Module.class })
public class FilterFactory extends ManagedClass implements Reloadable {
    
	private ConcurrentMap<String, AttributedValueFilter> _filterByName = new ConcurrentHashMap<>();

	private Config _config;

    /**
	 * Configuration for {@link FilterFactory}
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<FilterFactory> {
		/**
		 * Configured filters by name.
		 */
		@Key(FilterConfig.NAME_ATTRIBUTE)
		Map<String, FilterConfig> getFilters();

		/**
		 * A named filter configuration.
		 */
		interface FilterConfig extends NamedConfigMandatory {
			@Mandatory
			@DefaultContainer
			PolymorphicConfiguration<AttributedValueFilter> getImpl();
		}
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link FilterFactory}.
	 */
	public FilterFactory(InstantiationContext context, Config config) {
		super(context, config);

		_config = config;

		ReloadableManager.getInstance().addReloadable(this);
	}
    
    /**
     * The singleton {@link FilterFactory}.
     */
    private static FilterFactory getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
    /**
     * Get the Filter configured for the filter name
     * 
     * @param aFilterName the filter name
     * @return the Filter or <code>null</code> if none was configured or creation fails
     */
    public static AttributedValueFilter getFilter(String aFilterName) {
    	return getInstance().internalGetFilter(aFilterName);
    }
    
	/**
	 * All registered filter names.
	 */
	public Collection<String> getFilterNames() {
		return Collections.unmodifiableCollection(_config.getFilters().keySet());
	}

	private AttributedValueFilter internalGetFilter(String filterName) {
		AttributedValueFilter result = _filterByName.get(filterName);
		if (result == null) {
			result = createFilter(filterName);
			result = MapUtil.putIfAbsent(_filterByName, filterName, result);
    	}
		return result;
	}

	private AttributedValueFilter createFilter(String filterName) {
		Config.FilterConfig filterConfig = _config.getFilters().get(filterName);
		if (filterConfig == null) {
			return new AttributedValueFilter() {
				@Override
				public boolean accept(Object value, EditContext editContext) {
					throw new IllegalArgumentException("No such predefined filter: " + filterName);
				}
			};
		}

		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(filterConfig.getImpl());
	}

	@Override
	public synchronized boolean reload() {
	    try {
			_config = (Config) ApplicationConfig.getInstance().getServiceConfiguration(FilterFactory.class);

			_filterByName.clear();
	    } catch (Exception ex) {
            Logger.error("Faield to reload()", ex, FilterFactory.class);
            return false;
        }
		return true;
	}

	@Override
	public String getName() {
		return "FilterFactory";
	}

	@Override
	public String getDescription() {
		return "Filters for attribute values";
	}

	@Override
	public boolean usesXMLProperties() {
		return true;
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		super.shutDown();
	}
	
	/**
	 * {@link BasicRuntimeModule} for {@link FilterFactory}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	@Deprecated
	public static class Module extends TypedRuntimeModule<FilterFactory> {

		/**
		 * Singleton {@link FilterFactory.Module} instance.
		 */
		public static final FilterFactory.Module INSTANCE = new FilterFactory.Module();

		@Override
		public Class<FilterFactory> getImplementation() {
			return FilterFactory.class;
		}
	}
}