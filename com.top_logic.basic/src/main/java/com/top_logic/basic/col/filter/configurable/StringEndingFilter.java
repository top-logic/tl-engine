/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import java.util.List;

import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * A {@link ConfigurableFilter} that filters objects by the ending of their {@link String}
 * representation.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class StringEndingFilter<T> extends AbstractConfigurableFilter<T, StringEndingFilter.Config> {

	/** {@link ConfigurationItem} for the {@link StringEndingFilter}. */
	public interface Config extends AbstractConfigurableFilter.Config<StringEndingFilter<?>> {

		/** Property name of {@link #getAllowedEndings()}. */
		String ALLOWED_ENDINGS = "allowed-endings";

		/** The endings which are accepted by the filter. */
		@Name(ALLOWED_ENDINGS)
		@Mandatory
		@Format(CommaSeparatedStrings.class)
		List<String> getAllowedEndings();

	}

	private final List<String> _endings;

	private Class<T> _type;

	/** {@link TypedConfiguration} constructor for {@link StringEndingFilter}. */
	public StringEndingFilter(InstantiationContext context, Config config, Class<T> type) {
		super(context, config);
		_type = type;
		_endings = config.getAllowedEndings();
	}

	@Override
	public Class<?> getType() {
		return _type;
	}

	@Override
	protected FilterResult matchesTypesafe(T object) {
		String string = toString(object);
		return FilterResult.valueOf(_endings.stream().anyMatch(string::endsWith));
	}

	/** The {@link String} representation of the given object. */
	protected abstract String toString(T object);

}
