/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;

/**
 * A {@link ConfigurableFilter} that accepts types by matching their module names.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ModuleNameFilter extends AbstractConfigurableFilter<TLClass, ModuleNameFilter.Config> {

	/**
	 * Configuration options for {@link ModuleNameFilter}.
	 */
	public interface Config extends AbstractConfigurableFilter.Config<ModuleNameFilter> {
		/**
		 * The {@link TLModule#getName() module name} of matching types.
		 */
		String getModule();
	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ModuleNameFilter}.
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
	public ModuleNameFilter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<?> getType() {
		return TLClass.class;
	}

	@Override
	public FilterResult matchesTypesafe(TLClass tlType) {
		if (tlType.getModelKind() == ModelKind.ASSOCIATION) {
			/* PersistentAssociation implements TLClass, too, but throws exceptions. */
			return FilterResult.INAPPLICABLE;
		}
		String moduleName = tlType.getModule().getName();
		return FilterResult.valueOf(moduleName.equals(getConfig().getModule()));
	}

	@Override
	public String toString() {
		return new NameBuilder(this).build();
	}

}
