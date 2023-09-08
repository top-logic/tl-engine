/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;

/**
 * Base interface for configuring a composite {@link Filter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface CompositeFilterConfig<T, I> extends PolymorphicConfiguration<I> {
	/**
	 * The inner filters to combine with the boolean and operation.
	 */
	@DefaultContainer
	List<PolymorphicConfiguration<? extends Filter<? super T>>> getFilters();
}
