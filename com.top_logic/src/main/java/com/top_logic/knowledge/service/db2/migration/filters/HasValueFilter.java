/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import com.google.inject.Inject;

import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.Utils;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.service.db2.migration.formats.DumpValueSpec;
import com.top_logic.knowledge.service.db2.migration.formats.MigrationValueParser;

/**
 * {@link MigrationFilter} matching {@link ItemChange} which have a given special attribute value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HasValueFilter extends MigrationFilter<ItemChange, HasValueFilter.Config> {

	/**
	 * Configuration of a {@link HasValueFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends MigrationFilter.Config<HasValueFilter> {

		/**
		 * The name of the attribute to find the matching value.
		 */
		String getAttribute();

		/**
		 * The value which {@link #getAttribute() the attribute of the event} must have.
		 */
		DumpValueSpec getValue();

	}

	private String _attribute;
	private Object _value;

	/**
	 * Creates a new {@link HasValueFilter}.
	 */
	public HasValueFilter(InstantiationContext context, Config config) {
		super(context, config, ItemChange.class);
		_attribute = config.getAttribute();
	}

	/**
	 * Initialises the {@link MORepository} of this {@link HasValueFilter}.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		_value = getConfig().getValue().resolve(new MigrationValueParser(types));
	}

	@Override
	protected FilterResult matchesTypesafe(ItemChange object) {
		return FilterResult.valueOf(Utils.equals(_value, object.getValues().get(_attribute)));
	}

}
