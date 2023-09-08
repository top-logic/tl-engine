/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ObjectCreation;

/**
 * {@link DelegatingMigrationFilter} checking creations that reference an object currently changed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigateBackwardsFilter extends DelegatingMigrationFilter<NavigateBackwardsFilter.Config> {

	private final String _type;

	private final String _reference;

	/**
	 * Configuration of {@link NavigateBackwardsFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends DelegatingMigrationFilter.Config<NavigateBackwardsFilter> {

		/**
		 * The type a creation must have to be checked.
		 */
		String getType();

		/**
		 * Name of the reference attribute whose value must be a changed object to be checked.
		 */
		String getReference();
	}

	/**
	 * Creates a {@link NavigateBackwardsFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NavigateBackwardsFilter(InstantiationContext context, Config config) {
		super(context, config);

		_type = config.getType();
		_reference = config.getReference();
	}

	@Override
	protected FilterResult matchesTypesafe(ItemChange event) {
		ObjectKey selfKey = event.getObjectId().toCurrentObjectKey();

		ChangeSet cs = migration().current();
		for (ObjectCreation creation : cs.getCreations()) {
			if (!_type.equals(creation.getObjectType().getName())) {
				continue;
			}
			
			if (!selfKey.equals(creation.getValues().get(_reference))) {
				continue;
			}

			return filter().matches(creation);
		}
		return FilterResult.FALSE;
	}

}