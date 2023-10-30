/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import com.top_logic.layout.template.NoSuchPropertyException;
import com.top_logic.layout.template.WithProperties;

/**
 * Definition of a local variable during a template evaluation.
 */
public class LocalVariable implements WithProperties {
	private final WithProperties _outer;

	private final String _var;

	private Object _value;

	/**
	 * Creates an initialized {@link LocalVariable}.
	 *
	 * @param var
	 *        The name of the variable.
	 * @param value
	 *        The initial value of the variable.
	 * @param properties
	 *        The fallback properties to access, if a global property or a property of an outer
	 *        scope is requested.
	 */
	public LocalVariable(String var, Object value, WithProperties properties) {
		this(var, properties);
		setValue(value);
	}

	/**
	 * Creates a {@link LocalVariable}.
	 *
	 * @param var
	 *        The name of the variable.
	 * @param properties
	 *        The fallback properties to access, if a global property or a property of an outer
	 *        scope is requested.
	 */
	public LocalVariable(String var, WithProperties properties) {
		_var = var;
		_outer = skipBinding(var, properties);
	}

	private static WithProperties skipBinding(String var, WithProperties properties) {
		if (properties instanceof LocalVariable) {
			LocalVariable fallback = (LocalVariable) properties;
			if (fallback.getVar().equals(var)) {
				return fallback.getOuter();
			}
		}
		return properties;
	}

	/**
	 * The name of the bound local variable.
	 */
	public String getVar() {
		return _var;
	}

	/**
	 * Updates the value of the local variable.
	 */
	public void setValue(Object value) {
		_value = value;
	}

	/**
	 * The outer scope with other variable bindings.
	 */
	public WithProperties getOuter() {
		return _outer;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		if (_var.equals(propertyName)) {
			return _value;
		} else {
			return _outer.getPropertyValue(propertyName);
		}
	}

	@Override
	public Optional<Collection<String>> getAvailableProperties() {
		Optional<Collection<String>> result = _outer.getAvailableProperties();
		if (result.isPresent()) {
			HashSet<String> merged = new HashSet<>(result.get());
			merged.add(_var);
			return Optional.of(merged);
		} else {
			return Optional.of(Collections.singleton(_var));
		}
	}

	@Override
	public String toString() {
		return _outer.toString();
	}

}