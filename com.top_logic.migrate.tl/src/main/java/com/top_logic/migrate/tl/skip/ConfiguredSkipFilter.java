/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Set;

import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.config.FilterConfig;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;

/**
 * {@link SkipFilter} with configured {@link TypedFilter} matching the events to skip.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfiguredSkipFilter implements SkipFilter {

	/**
	 * Configuration of a {@link ConfiguredSkipFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<SkipFilter>, FilterConfig {
		// Sum interface

	}

	private final TypedFilter _filter;

	/**
	 * Creates a new {@link ConfiguredSkipFilter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ConfiguredSkipFilter}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ConfiguredSkipFilter(InstantiationContext context, Config config) throws ConfigurationException {
		_filter = Rewriter.toAndFilter(context, config.getFilters());
	}

	@Override
	public boolean skipEvent(ItemCreation event, Set<ObjectBranchId> skippedObjects) {
		return _filter.matches(event) == FilterResult.TRUE;
	}

}

