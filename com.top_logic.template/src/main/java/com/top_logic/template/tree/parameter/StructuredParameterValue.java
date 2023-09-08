/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree.parameter;

import java.util.Map;

import com.top_logic.basic.tools.NameBuilder;

/**
 * Represents a structured {@link ParameterValue}.
 * <p>
 * The value is a {@link Map} from {@link String}s to {@link ParameterValue}s.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class StructuredParameterValue<T> extends ParameterValue<T> {

	private final Map<String, ParameterValue<T>> _structuredValue;

	/**
	 * Creates a new {@link StructuredParameterValue}.
	 */
	public StructuredParameterValue(Map<String, ParameterValue<T>> structuredValue) {
		_structuredValue = structuredValue;
	}

	/**
	 * Getter for the {@link Map} storing the values of this structured parameter.
	 * <p>
	 * Returns the internal used map.
	 * </p>
	 */
	public Map<String, ParameterValue<T>> getStructuredValue() {
		return _structuredValue;
	}

	@Override
	public boolean isStructuredValue() {
		return true;
	}

	@Override
	public boolean isPrimitiveValue() {
		return false;
	}

	@Override
	public boolean isListValue() {
		return false;
	}

	@Override
	public StructuredParameterValue<T> asStructuredValue() {
		return this;
	}

	@Override
	public PrimitiveParameterValue<T> asPrimitiveValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ PrimitiveParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public ListParameterValue<T> asListValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ ListParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public String toString() {
		return new NameBuilder(this).add("attributes", _structuredValue).buildName();
	}

}