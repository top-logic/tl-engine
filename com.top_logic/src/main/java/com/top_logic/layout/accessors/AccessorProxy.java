/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Accessor;

/**
 * Proxy for another configured {@link Accessor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AccessorProxy<C extends AccessorProxy<C, T>, T> implements Accessor<T> {

	/**
	 * Configuration of another {@link Accessor}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface AccessorImplConfig<T> extends ConfigurationItem {

		/** Configuration name of {@link #getImpl()}. */
		String IMPL = "impl";

		/**
		 * The {@link Accessor} to delegate to.
		 */
		@Name(IMPL)
		@Mandatory
		PolymorphicConfiguration<Accessor<T>> getImpl();

		/**
		 * Setter for {@link #getImpl()}.
		 */
		void setImpl(PolymorphicConfiguration<Accessor<T>> impl);

	}

	/**
	 * Configuration of an {@link AccessorProxy}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<C extends AccessorProxy<C, T>, T>
			extends PolymorphicConfiguration<C>, AccessorImplConfig<T> {
		// sum interface
	}

	private final Accessor<T> _impl;

	/**
	 * Creates a new {@link AccessorProxy} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AccessorProxy}.
	 */
	public AccessorProxy(InstantiationContext context, Config<C, T> config) {
		_impl = context.getInstance(config.getImpl());
	}

	/**
	 * Creates a new {@link AccessorProxy}.
	 * 
	 * @param impl
	 *        The {@link Accessor} to wrap.
	 */
	public AccessorProxy(Accessor<T> impl) {
		_impl = impl;
	}

	@Override
	public Object getValue(T object, String property) {
		return _impl.getValue(object, property);
	}

	@Override
	public void setValue(T object, String property, Object value) {
		_impl.setValue(object, property, value);
	}

}

