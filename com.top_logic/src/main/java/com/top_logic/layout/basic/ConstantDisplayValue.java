/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;

/**
 * The most simple {@link DisplayValue} implementation that does actually not
 * depend on the context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantDisplayValue implements DisplayValue {
	
	public static DisplayValue EMPTY_STRING = new ConstantDisplayValue("");

	public static DisplayValue SPACE = new ConstantDisplayValue(" ");
	public static DisplayValue TRUE = new ConstantDisplayValue("true");
	public static DisplayValue FALSE = new ConstantDisplayValue("false");

	private final String value;
	
	public ConstantDisplayValue(String value) {
		this.value = value;
	}

	@Override
	public String get(DisplayContext context) {
		return value;
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		out.append(value);
	}

	/**
	 * Returns a {@link ConstantDisplayValue} which constantly
	 * {@link ConstantDisplayValue#get(DisplayContext) returns } a String
	 * representation of the given value.
	 */
	public static DisplayValue valueOf(boolean value) {
		return value ? TRUE : FALSE;
	}
	
	/**
	 * Returns a {@link ConstantDisplayValue} which constantly
	 * {@link ConstantDisplayValue#get(DisplayContext) returns} the given value.
	 */
	public static DisplayValue valueOf(String value) {
		if (value == null || value.length() == 0) {
			return EMPTY_STRING;
		} else {
			return new ConstantDisplayValue(value);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + value + "]";
	}
}
