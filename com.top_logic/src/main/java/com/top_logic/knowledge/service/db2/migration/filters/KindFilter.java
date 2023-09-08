/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.EventKind;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.KnowledgeEvent;

/**
 * {@link AbstractConfigurableFilter} accepting {@link ItemEvent} of a given {@link EventKind}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KindFilter extends AbstractConfigurableFilter<KnowledgeEvent, KindFilter.Config> {

	/**
	 * Configuration of a {@link KindFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractConfigurableFilter.Config<KindFilter> {

		/**
		 * The only accepted {@link EventKind}.
		 */
		EventKind getKind();

	}

	private EventKind _kind;

	/**
	 * Creates a {@link KindFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public KindFilter(InstantiationContext context, Config config) {
		super(context, config);
		_kind = config.getKind();
	}

	@Override
	public Class<?> getType() {
		return KnowledgeEvent.class;
	}

	@Override
	protected FilterResult matchesTypesafe(KnowledgeEvent event) {
		return FilterResult.valueOf(event.getKind() == _kind);
	}

}