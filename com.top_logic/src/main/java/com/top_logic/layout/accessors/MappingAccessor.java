/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import java.util.Objects;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Accessor;

/**
 * {@link Accessor} that first maps the object using a given {@link Mapping} and then use a given
 * {@link Accessor} to get (and set) value from the mapped object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MappingAccessor extends AccessorProxy<MappingAccessor, Object> {

	/**
	 * Configuration of a {@link MappingAccessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AccessorProxy.Config<MappingAccessor, Object> {

		/** Configuration name of {@link #getMapping()}. */
		String MAPPING = "mapping";

		/**
		 * The mapping to use to map object.
		 */
		@Name(MAPPING)
		@Mandatory
		PolymorphicConfiguration<Mapping<Object, Object>> getMapping();

		/**
		 * Setter for {@link #getMapping()}
		 */
		void setMapping(PolymorphicConfiguration<Mapping<Object, Object>> mapping);

	}

	private final Mapping<Object, Object> _mapping;

	/**
	 * Creates a new {@link MappingAccessor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link MappingAccessor}.
	 */
	public MappingAccessor(InstantiationContext context, Config config) {
		super(context, config);
		_mapping = context.getInstance(config.getMapping());
	}

	/**
	 * Creates a new {@link MappingAccessor}.
	 */
	public MappingAccessor(Accessor<Object> delegate, Mapping<Object, Object> objectMapping) {
		super(delegate);
		_mapping = Objects.requireNonNull(objectMapping);
	}

	@Override
	public Object getValue(Object object, String property) {
		return super.getValue(_mapping.map(object), property);
	}

	@Override
	public void setValue(Object object, String property, Object value) {
		super.setValue(_mapping.map(object), property, value);
	}

}

