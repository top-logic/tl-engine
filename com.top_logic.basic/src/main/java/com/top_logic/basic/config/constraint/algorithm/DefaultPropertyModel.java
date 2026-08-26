/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;

/**
 * Default {@link PropertyModel} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultPropertyModel<T> implements PropertyModel<T> {

	private final ConfigurationItem _model;

	private final PropertyDescriptor _property;

	private final ResKey _label;

	private ResKey _message;

	/**
	 * Creates a {@link DefaultPropertyModel}.
	 * 
	 * @param model
	 *        The context model owning the {@link #getProperty()}.
	 * @param property
	 *        See {@link #getProperty()}.
	 * @param label
	 *        See {@link #getLabel()}.
	 */
	public DefaultPropertyModel(ConfigurationItem model, PropertyDescriptor property, ResKey label) {
		_model = model;
		_property = property;
		_label = label;
	}

	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public T getValue() {
		if (_property.isMandatory() && !isValueSet()) {
			return null;
		}
		@SuppressWarnings("unchecked")
		T result = (T) _model.value(_property);
		return result;
	}

	@Override
	public boolean isValueSet() {
		return _model.valueSet(_property);
	}

	@Override
	public PropertyDescriptor getProperty() {
		return _property;
	}

	/**
	 * The {@link ConfigurationItem} that owns {@link #getProperty()}.
	 * 
	 * <p>
	 * Not necessarily the item whose constraint is being checked: a constraint argument declared
	 * with a multi-step {@link com.top_logic.basic.config.annotation.Ref} resolves to a property of
	 * a different item, and a problem reported on that argument belongs to <em>that</em> item.
	 * </p>
	 */
	public ConfigurationItem getItem() {
		return _model;
	}

	@Override
	public void setProblemDescription(ResKey message) {
		_message = message;
	}

	/**
	 * The problem description reported by {@link #setProblemDescription(ResKey)}, or
	 * <code>null</code> if no problem was reported.
	 */
	public ResKey getProblemDescription() {
		return _message;
	}

}
