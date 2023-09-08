/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.component.NullAccessor;

/**
 * {@link TypeSafeAccessor} whose {@link #getType()} is configured.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfiguredTypeSafeAccessor<T, C extends ConfiguredTypeSafeAccessor.Config<?, T>>
		extends TypeSafeAccessor<T> implements ConfiguredInstance<C> {

	/**
	 * Configuration of a {@link ConfiguredTypeSafeAccessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends ConfiguredTypeSafeAccessor<?, ?>, T> extends PolymorphicConfiguration<I> {

		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * The expected type of instances to access.
		 */
		@Name(TYPE)
		@Mandatory
		Class<? extends T> getType();

		/**
		 * @see #getType()
		 */
		void setType(Class<? extends T> value);

		/**
		 * Chained {@link Accessor} that is used if the current object does not match the
		 * {@link #getType()}.
		 */
		@Name("defaultAccessor")
		PolymorphicConfiguration<Accessor<?>> getDefaultAccessor();

	}

	private final Class<? extends T> _type;

	private Accessor<Object> _defaultImpl;

	private C _config;

	/**
	 * Creates a new {@link ConfiguredTypeSafeAccessor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ConfiguredTypeSafeAccessor}.
	 */
	public ConfiguredTypeSafeAccessor(InstantiationContext context, C config) {
		_config = config;
		_type = config.getType();
		_defaultImpl = createDefaultAccessor(context, config);
	}

	@Override
	public C getConfig() {
		return _config;
	}

	private Accessor<Object> createDefaultAccessor(InstantiationContext context, Config<?, T> config) {
		PolymorphicConfiguration<Accessor<?>> defautConfig = config.getDefaultAccessor();
		if (defautConfig == null) {
			return NullAccessor.INSTANCE;
		}
		@SuppressWarnings("unchecked")
		Accessor<Object> defaultAccessor = (Accessor<Object>) context.getInstance(defautConfig);
		return defaultAccessor;
	}

	@Override
	protected Class<? extends T> getType() {
		return _type;
	}

	@Override
	protected Object getDefaultValue(Object object, String property) {
		return _defaultImpl.getValue(object, property);
	}

}

