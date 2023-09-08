/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import java.util.Set;

import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.convert.TypesConfig;

/**
 * {@link MigrationFilter} matching {@link ItemEvent} with certain type names.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasTypeFilter extends MigrationFilter<ItemEvent, HasTypeFilter.Config> {

	/**
	 * Configuration of a {@link HasTypeFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends MigrationFilter.Config<HasTypeFilter>, TypesConfig {

		// sum interface.

	}

	private Set<String> _typeNames;

	/**
	 * Creates a new {@link HasTypeFilter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link HasTypeFilter}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public HasTypeFilter(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config, ItemEvent.class);
		_typeNames = config.getTypeNames();
	}

	@Override
	protected FilterResult matchesTypesafe(ItemEvent object) {
		return FilterResult.valueOf(_typeNames.contains(object.getObjectType().getName()));
	}


}

