/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.Function0;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.element.meta.kbbased.filtergen.FilterFactory;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * Configured {@link AttributedValueFilter} that dispatches to the {@link FilterFactory}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Deprecated
public final class NamedConstraint implements AttributedValueFilter, ConfiguredInstance<NamedConstraint.Config> {

	/**
	 * Configuration options for {@link NamedConstraint}.
	 */
	@TagName("config-reference")
	public interface Config extends PolymorphicConfiguration<NamedConstraint> {

		/**
		 * Constraint name in the {@link FilterFactory}.
		 */
		@Options(fun = AllFilterNames.class)
		@Mandatory
		String getName();

		/**
		 * All available {@link AttributedValueFilter}s in the {@link FilterFactory}.
		 */
		class AllFilterNames extends Function0<List<String>> {
			@Override
			public List<String> apply() {
				List<String> result = new ArrayList<>();
				result.addAll(FilterFactory.Module.INSTANCE.getImplementationInstance().getFilterNames());
				Collections.sort(result);
				return result;
			}
		}

	}

	private final Config _config;

	private final AttributedValueFilter _impl;

	/**
	 * Creates a {@link NamedConstraint} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NamedConstraint(InstantiationContext context, Config config) throws ConfigurationException {
		_config = config;
		AttributedValueFilter filter = FilterFactory.getFilter(config.getName());
		if (filter == null) {
			throw new ConfigurationException("No such global filter: " + config.getName());
		}
		_impl = filter;
	}

	@Override
	public boolean accept(Object value, EditContext editContext) {
		return _impl.accept(value, editContext);
	}

	@Override
	public Config getConfig() {
		return _config;
	}

}
