/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;


/**
 * {@link ConstraintAlgorithm} checking a single property without dependencies to other properties.
 * 
 * @see ValueDependency
 * @see GenericValueDependency
 * @see GenericPropertyConstraint
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueConstraint<T> implements ConstraintAlgorithm {

	private Class<?>[] _signature;

	/**
	 * Creates a {@link ValueConstraint}.
	 * 
	 * @param type
	 *        The expected type of the property to check.
	 */
	public ValueConstraint(Class<T> type) {
		_signature = new Class<?>[] { type };
	}

	@Override
	public final void check(PropertyModel<?>... models) {
		@SuppressWarnings("unchecked")
		PropertyModel<T> valueModel = (PropertyModel<T>) models[0];
		
		checkValue(valueModel);
	}

	@Override
	public boolean isChecked(int index) {
		return index == 0;
	}

	@Override
	public Class<?>[] signature() {
		return _signature;
	}

	/**
	 * Implementation of {@link #check(PropertyModel...)}.
	 * 
	 * @param propertyModel
	 *        A reference to the property value to check.
	 */
	protected abstract void checkValue(PropertyModel<T> propertyModel);

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
