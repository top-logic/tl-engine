/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter.configurable;

import com.top_logic.basic.Named;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * A {@link ConfigurableFilter} that filters {@link Named} objects by the ending of their name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NamedEndingFilter extends StringEndingFilter<Named> {

	/** {@link TypedConfiguration} constructor for {@link NamedEndingFilter}. */
	public NamedEndingFilter(InstantiationContext context, Config config) {
		super(context, config, Named.class);
	}

	@Override
	protected String toString(Named named) {
		return named.getName();
	}

}
