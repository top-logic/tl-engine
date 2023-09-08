/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Accessor;

/**
 * {@link ConfiguredTypeSafeAccessor} accessing value by dispatching to another {@link Accessor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeSafeAccessorProxy<T> extends ConfiguredTypeSafeAccessor<T, TypeSafeAccessorProxy.Config<T>> {

	/**
	 * Configuration of a {@link TypeSafeAccessorProxy}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<T> extends ConfiguredTypeSafeAccessor.Config<TypeSafeAccessorProxy<T>, T>,
			AccessorProxy.AccessorImplConfig<T> {

		// sum interface

	}

	private final Accessor<? super T> _impl;

	/**
	 * Creates a new {@link TypeSafeAccessorProxy} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link TypeSafeAccessorProxy}.
	 */
	public TypeSafeAccessorProxy(InstantiationContext context, Config<T> config) {
		super(context, config);
		_impl = context.getInstance(config.getImpl());
	}

	@Override
	protected Object getValueTyped(T object, String property) {
		return _impl.getValue(object, property);
	}

}

