/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.config.FilterConfig;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;

/**
 * {@link MigrationFilter} with configured {@link TypedFilter} to delegate to.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DelegatingMigrationFilter<C extends DelegatingMigrationFilter.Config<?>> extends MigrationFilter<ItemChange, C> {

	/**
	 * Configuration of a {@link DelegatingMigrationFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<T extends DelegatingMigrationFilter<?>> extends MigrationFilter.Config<T>, FilterConfig {
		// Pure sum interface.
	}

	private TypedFilter _filter;

	/**
	 * Creates a new {@link DelegatingMigrationFilter}.
	 * 
	 * @see MigrationFilter#MigrationFilter(InstantiationContext,
	 *      com.top_logic.basic.col.filter.configurable.ConfigurableFilter.Config, Class)
	 */
	public DelegatingMigrationFilter(InstantiationContext context, C config) {
		super(context, config, ItemChange.class);

		_filter = Rewriter.toAndFilter(context, config.getFilters());
	}

	/**
	 * Returns the configured {@link TypedFilter} to delegate to.
	 */
	public TypedFilter filter() {
		return _filter;
	}
}
