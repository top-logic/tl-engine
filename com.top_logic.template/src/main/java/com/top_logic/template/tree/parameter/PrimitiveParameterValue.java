/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree.parameter;

import com.top_logic.basic.tools.NameBuilder;


/**
 * Represents a primitive (not structured) {@link ParameterValue}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class PrimitiveParameterValue<T> extends ParameterValue<T> {

	private final T _primitiveValue;

	/**
	 * Creates a new {@link PrimitiveParameterValue}.
	 */
	public PrimitiveParameterValue(T primitiveValue) {
		_primitiveValue = primitiveValue;
	}

	/**
	 * Getter for the value represented by this {@link PrimitiveParameterValue}.
	 */
	public T getPrimitiveValue() {
		return _primitiveValue;
	}

	@Override
	public boolean isStructuredValue() {
		return false;
	}

	@Override
	public boolean isPrimitiveValue() {
		return true;
	}

	@Override
	public boolean isListValue() {
		return false;
	}

	@Override
	public StructuredParameterValue<T> asStructuredValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ StructuredParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public PrimitiveParameterValue<T> asPrimitiveValue() {
		return this;
	}

	@Override
	public ListParameterValue<T> asListValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ ListParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public String toString() {
		return new NameBuilder(this).add("value", _primitiveValue.toString()).buildName();
	}

}