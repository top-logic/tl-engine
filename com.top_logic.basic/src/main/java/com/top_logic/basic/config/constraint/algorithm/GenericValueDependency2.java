/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

/**
 * {@link ConstraintAlgorithm} that relates exactly three properties (the base property and two
 * reference properties).
 * 
 * @see GenericValueDependency
 * @see GenericValueDependency3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class GenericValueDependency2<A, B1, B2> implements ConstraintAlgorithm {

	private Class<?>[] _signature;

	/**
	 * Creates a {@link GenericValueDependency2}.
	 * 
	 * @param typeA
	 *        The type of the base property.
	 * @param typeB1
	 *        The type of the first reference property.
	 * @param typeB2
	 *        The type of the second reference property.
	 */
	public GenericValueDependency2(Class<A> typeA, Class<B1> typeB1, Class<B2> typeB2) {
		_signature = new Class<?>[] { typeA, typeB1, typeB2 };
	}

	@Override
	public final void check(PropertyModel<?>... models) {
		@SuppressWarnings("unchecked")
		PropertyModel<A> valueModelA = (PropertyModel<A>) models[0];

		@SuppressWarnings("unchecked")
		PropertyModel<B1> valueModelB1 = (PropertyModel<B1>) models[1];

		@SuppressWarnings("unchecked")
		PropertyModel<B2> valueModelB2 = (PropertyModel<B2>) models[2];
		
		checkValue(valueModelA, valueModelB1, valueModelB2);
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
	 * @param propertyB1
	 *        The {@link PropertyModel} of the first reference property.
	 * @param propertyB2
	 *        The {@link PropertyModel} of the second reference property.
	 */
	protected abstract void checkValue(PropertyModel<A> propertyA,
			PropertyModel<B1> propertyB1,
			PropertyModel<B2> propertyB2);

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
