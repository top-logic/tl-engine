/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import com.google.inject.Inject;

import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter.Config;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.db2.migration.Migration;

/**
 * {@link AbstractConfigurableFilter} for migration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MigrationFilter<T, C extends Config<?>> extends AbstractConfigurableFilter<T, C> {

	private Migration _migration;

	private final Class<T> _type;

	/**
	 * Creates a new {@link MigrationFilter}.
	 * 
	 * @see AbstractConfigurableFilter#AbstractConfigurableFilter(InstantiationContext, Config)
	 */
	public MigrationFilter(InstantiationContext context, C config, Class<T> type) {
		super(context, config);
		_type = type;
	}

	/**
	 * Installs the migration for this filter.
	 * 
	 * @see #migration()
	 */
	@Inject
	public void init(Migration migration) {
		_migration = migration;
	}

	/**
	 * The {@link Migration} in which this filter is used.
	 */
	public Migration migration() {
		return _migration;
	}

	@Override
	public Class<?> getType() {
		return _type;
	}

}
