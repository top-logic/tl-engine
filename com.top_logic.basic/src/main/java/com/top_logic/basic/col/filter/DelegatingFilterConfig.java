/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.filter;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.DefaultContainer;

/**
 * Base configuration interface for {@link Filter}s delegating to a single other
 * {@link #getFilter()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DelegatingFilterConfig<T, I> extends PolymorphicConfiguration<I> {

	/**
	 * The inner {@link Filter} to delegate to.
	 */
	@DefaultContainer
	PolymorphicConfiguration<? extends Filter<? super T>> getFilter();

}
