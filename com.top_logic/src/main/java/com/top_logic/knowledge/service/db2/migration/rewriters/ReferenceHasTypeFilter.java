/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;

/**
 * {@link ConfigurableFilter} that accepts {@link ItemChange} events, if they contain a reference to
 * an object of a certain model type.
 */
public class ReferenceHasTypeFilter extends AbstractConfigurableFilter<ItemChange, ReferenceHasTypeFilter.Config<?>> {

	/**
	 * Configuration options for {@link ReferenceHasTypeFilter}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends ReferenceHasTypeFilter> extends ConfigurableFilter.Config<I> {

		/**
		 * Tag name of {@link Config}.
		 */
		String TAG_NAME = "reference-has-type";

		/**
		 * @see #getReference()
		 */
		String REFERENCE = "reference";

		/**
		 * Name of the reference column to test values.
		 */
		@Name(REFERENCE)
		String getReference();

		/**
		 * Fully qualified model type names.
		 * 
		 * <p>
		 * The filter accepts events, if the {@link #getReference()} contains an object that has one
		 * of the given types.
		 * </p>
		 * 
		 * <p>
		 * Note: Sub-types are not matched. All concrete types that should match must be enumerated.
		 * </p>
		 */
		@ListBinding(attribute = "name")
		List<String> getTypes();

	}

	private MigrationModelIndex _modelIndex;

	private String _reference;

	private Set<String> _types;

	/**
	 * Creates a {@link ReferenceHasTypeFilter}.
	 */
	public ReferenceHasTypeFilter(InstantiationContext context, Config<?> config) {
		super(context, config);

		_reference = config.getReference();
		_types = new HashSet<>(config.getTypes());

		context.resolveReference(InstantiationContext.OUTER, MigrationModelIndex.class, x -> _modelIndex = x);
	}

	@Override
	public Class<?> getType() {
		return ItemChange.class;
	}

	@Override
	protected FilterResult matchesTypesafe(ItemChange object) {
		Object value = object.getValues().get(_reference);
		if (!(value instanceof ObjectKey)) {
			return FilterResult.FALSE;
		}

		ObjectKey ref = (ObjectKey) value;
		String typeName = _modelIndex.getTypeOf(ref);
		if (!_types.contains(typeName)) {
			return FilterResult.FALSE;
		}

		return FilterResult.TRUE;
	}

}
