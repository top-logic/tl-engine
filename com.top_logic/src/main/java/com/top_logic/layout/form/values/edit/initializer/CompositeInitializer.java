/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.initializer;

import static com.top_logic.basic.CollectionUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * A composite {@link Initializer}, consisting of the given collection of inner {@link Initializer}
 * s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class CompositeInitializer implements Initializer {

	private final List<Initializer> _initializers;

	/**
	 * Creates a {@link CompositeInitializer}.
	 * <p>
	 * The {@link Initializer}s will be called in the given order.
	 * </p>
	 * 
	 * @param initializers
	 *        Is allowed to be null and is copied to prevent side effect modifications.
	 */
	public CompositeInitializer(Collection<Initializer> initializers) {
		_initializers = new ArrayList<>(nonNull(initializers));
	}

	@Override
	public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
		for (Initializer part : _initializers) {
			part.init(model, property, value);
		}
	}

}
