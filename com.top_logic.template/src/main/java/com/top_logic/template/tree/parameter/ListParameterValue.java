/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree.parameter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.tools.NameBuilder;

/**
 * Represents a list of {@link ParameterValue}s.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class ListParameterValue<T> extends ParameterValue<T> {

	private final List<ParameterValue<T>> _values;

	/**
	 * Creates a new {@link PrimitiveParameterValue}.
	 */
	public ListParameterValue(List<ParameterValue<T>> values) {
		_values = new ArrayList<>(values);
	}

	/**
	 * Getter for the {@link List} storing the values of this list of {@link ParameterValue}s.
	 * <p>
	 * Returns the internal used list.
	 * </p>
	 */
	public List<ParameterValue<T>> getListValue() {
		return _values;
	}

	@Override
	public boolean isStructuredValue() {
		return false;
	}

	@Override
	public boolean isPrimitiveValue() {
		return false;
	}

	@Override
	public boolean isListValue() {
		return true;
	}

	@Override
	public StructuredParameterValue<T> asStructuredValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ StructuredParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public PrimitiveParameterValue<T> asPrimitiveValue() {
		throw new ClassCastException("This is a " + getClass().getSimpleName() + " and not a "
			+ PrimitiveParameterValue.class.getSimpleName() + ".");
	}

	@Override
	public ListParameterValue<T> asListValue() {
		return this;
	}

	@Override
	public String toString() {
		return new NameBuilder(this).add("values", _values).buildName();
	}

}
