/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree.parameter;

/**
 * Represents a complex parameter value that can consist of {@link PrimitiveParameterValue}s,
 * {@link ListParameterValue}s and {@link StructuredParameterValue}s.
 * 
 * @see PrimitiveParameterValue
 * @see StructuredParameterValue
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ParameterValue<T> {

	ParameterValue() {
		// reduce visibility
	}

	/**
	 * Is this {@link ParameterValue} a {@link StructuredParameterValue}?
	 */
	public abstract boolean isStructuredValue();

	/**
	 * Is this {@link ParameterValue} a {@link PrimitiveParameterValue}?
	 */
	public abstract boolean isPrimitiveValue();

	/**
	 * Is this {@link ParameterValue} a {@link ListParameterValue}?
	 */
	public abstract boolean isListValue();

	/**
	 * Returns this {@link ParameterValue} casted to {@link StructuredParameterValue}.
	 * 
	 * @throws ClassCastException
	 *         If this is not a {@link StructuredParameterValue}.
	 */
	public abstract StructuredParameterValue<T> asStructuredValue();

	/**
	 * Returns this {@link ParameterValue} casted to {@link PrimitiveParameterValue}.
	 * 
	 * @throws ClassCastException
	 *         If this is not a {@link PrimitiveParameterValue}.
	 */
	public abstract PrimitiveParameterValue<T> asPrimitiveValue();

	/**
	 * Returns this {@link ParameterValue} casted to {@link ListParameterValue}.
	 * 
	 * @throws ClassCastException
	 *         If this is not a {@link ListParameterValue}.
	 */
	public abstract ListParameterValue<T> asListValue();

	@Override
	public abstract String toString();

}