/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accessors;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Accessor;

/**
 * {@link AccessorProxy} returning <code>null</code> for <code>null</code> input.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NullSafeAccessor<T> extends AccessorProxy<NullSafeAccessor<T>, T> {

	/**
	 * Creates a new {@link NullSafeAccessor} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link NullSafeAccessor}.
	 */
	public NullSafeAccessor(InstantiationContext context, Config<NullSafeAccessor<T>, T> config) {
		super(context, config);
	}

	/**
	 * Creates a new {@link NullSafeAccessor}.
	 * 
	 * @param impl
	 *        The {@link Accessor} to wrap.
	 */
	public NullSafeAccessor(Accessor<T> impl) {
		super(impl);
	}

	@Override
	public Object getValue(T object, String property) {
		if (object == null) {
			return null;
		}
		return super.getValue(object, property);
	}

}

