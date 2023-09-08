/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

/**
 * {@link ConstraintAlgorithm} that relates exactly four properties (the base property and three
 * reference properties).
 * 
 * @see GenericValueDependency2
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class GenericValueDependency3<A, B1, B2, B3> implements ConstraintAlgorithm {

	private Class<?>[] _signature;

	/**
	 * Creates a {@link GenericValueDependency3}.
	 * 
	 * @param typeA
	 *        The type of the base property.
	 * @param typeB1
	 *        The type of the first reference property.
	 * @param typeB2
	 *        The type of the second reference property.
	 * @param typeB3
	 *        The type of the third reference property.
	 */
	public GenericValueDependency3(Class<A> typeA, Class<B1> typeB1, Class<B2> typeB2, Class<B3> typeB3) {
		_signature = new Class<?>[] { typeA, typeB1, typeB2, typeB3 };
	}

	@Override
	public final void check(PropertyModel<?>... models) {
		@SuppressWarnings("unchecked")
		PropertyModel<A> valueModelA = (PropertyModel<A>) models[0];

		@SuppressWarnings("unchecked")
		PropertyModel<B1> valueModelB1 = (PropertyModel<B1>) models[1];

		@SuppressWarnings("unchecked")
		PropertyModel<B2> valueModelB2 = (PropertyModel<B2>) models[2];
		
		@SuppressWarnings("unchecked")
		PropertyModel<B3> valueModelB3 = (PropertyModel<B3>) models[3];

		checkValue(valueModelA, valueModelB1, valueModelB2, valueModelB3);
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
	 * @param propertyB3
	 *        The {@link PropertyModel} of the third reference property.
	 */
	protected abstract void checkValue(PropertyModel<A> propertyA,
			PropertyModel<B1> propertyB1,
			PropertyModel<B2> propertyB2,
			PropertyModel<B3> propertyB3);

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
