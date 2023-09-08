/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import java.util.regex.Pattern;

import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.knowledge.event.ItemChange;

/**
 * {@link MigrationFilter} matching the {@link ItemChange} which has an attribute whose value
 * matches a given pattern.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MatchesValueFilter extends MigrationFilter<ItemChange, MatchesValueFilter.Config> {

	/**
	 * Configuration of a {@link HasValueFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends MigrationFilter.Config<MatchesValueFilter> {

		/**
		 * The name of the attribute to find the matching value.
		 */
		String getAttribute();

		/**
		 * The value which {@link #getAttribute() the attribute of the event} must have.
		 */
		@Format(RegExpValueProvider.class)
		Pattern getValue();

	}

	/**
	 * Creates a new {@link HasValueFilter}.
	 */
	public MatchesValueFilter(InstantiationContext context, Config config) {
		super(context, config, ItemChange.class);
	}

	@Override
	protected FilterResult matchesTypesafe(ItemChange object) {
		String attribute = getConfig().getAttribute();
		Object attributeValue = object.getValues().get(attribute);
		if (!(attributeValue instanceof CharSequence)) {
			if (attributeValue == null && object.getValues().containsKey(attribute)) {
				return FilterResult.INAPPLICABLE;
			}
			return FilterResult.FALSE;
		}
		return FilterResult.valueOf(getConfig().getValue().matcher((CharSequence) attributeValue).matches());
	}

}

