/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.List;

import org.w3c.dom.Node;

import com.top_logic.layout.processor.Expansion.Buffer;


/**
 * {@link ParameterValue} that can be expanded to a plain string in attribute context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringValue extends ParameterValue {

	private final String _value;

	private boolean _optional;

	/**
	 * Creates a {@link StringValue}.
	 * 
	 * @param paramName
	 *        See {@link #getParamName()}
	 * @param value
	 *        The plain string to which this value is expanded.
	 * @param optional
	 *        Whether the parameter is optional, i.e. whether the parameter needs a non
	 *        <code>null</code> value the finally expanded template.
	 */
	public StringValue(String paramName, String value, boolean optional) {
		super(paramName);

		_value = value;
		_optional = optional;
	}

	/**
	 * Creates a {@link StringValue}.
	 * 
	 * <p>
	 * The {@link StringValue} is an optional value when the <code>value</code> is not
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param paramName
	 *        See {@link #getParamName()}
	 * @param value
	 *        The plain string to which this value is expanded.
	 */
	public StringValue(String paramName, String value) {
		this(paramName, value, value != null);
	}

	/**
	 * value, which is represented by this {@link StringValue}.
	 */
	public String get() {
		return _value;
	}

	@Override
	protected List<? extends Node> internalExpand(Buffer out) throws ElementInAttributeContextError {
		if (_optional && _value == null) {
			return Expansion.NO_NODE_EXPANSION;
		}
		return expansion().expandTextContents(_value, out);
	}
}