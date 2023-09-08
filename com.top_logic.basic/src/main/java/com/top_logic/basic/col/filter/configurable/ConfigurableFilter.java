/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * A {@link TypedFilter} that can be configured.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface ConfigurableFilter<C extends ConfigurableFilter.Config<?>> extends TypedFilter, ConfiguredInstance<C> {

	/**
	 * The {@link TypedConfiguration} of the {@link ConfigurableFilter}.
	 */
	public interface Config<S extends ConfigurableFilter<?>> extends PolymorphicConfiguration<S> {
		// Nothing needed
	}

}
