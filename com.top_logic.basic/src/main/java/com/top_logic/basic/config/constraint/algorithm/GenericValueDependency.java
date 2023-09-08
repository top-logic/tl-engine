/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;


/**
 * {@link ConstraintAlgorithm} that relates exactly two properties (the base property and a
 * reference property).
 * 
 * @see GenericPropertyConstraint
 * @see ValueConstraint
 * @see GenericValueDependency2
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericValueDependency<A, B> implements ConstraintAlgorithm {

	private Class<?>[] _signature;

	/**
	 * Creates a {@link GenericValueDependency}.
	 * 
	 * @param typeA
	 *        The type of the base property.
	 * @param typeB
	 *        The type of the reference property.
	 */
	public GenericValueDependency(Class<A> typeA, Class<B> typeB) {
		_signature = new Class<?>[] { typeA, typeB };
	}

	@Override
	public final void check(PropertyModel<?>... models) {
		@SuppressWarnings("unchecked")
		PropertyModel<A> valueModelA = (PropertyModel<A>) models[0];

		@SuppressWarnings("unchecked")
		PropertyModel<B> valueModelB = (PropertyModel<B>) models[1];
		
		checkValue(valueModelA, valueModelB);
	}

	@Override
	public boolean isChecked(int index) {
		return true;
	}

	@Override
	public Class<?>[] signature() {
		return _signature;
	}

	/**
	 * Implementation of {@link #check(PropertyModel...)}.
	 * 
	 * @param propertyA
	 *        The {@link PropertyModel} of the base property.
	 * @param propertyB
	 *        The {@link PropertyModel} of the reference property.
	 */
	protected abstract void checkValue(PropertyModel<A> propertyA, PropertyModel<B> propertyB);

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
