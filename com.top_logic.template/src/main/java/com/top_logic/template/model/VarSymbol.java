/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import com.top_logic.dob.MetaObject;

/**
 * This class defines the methods that are necessary to enable the interaction between parser and
 * object model.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class VarSymbol {
	
	private MetaObject type;
	private Object     value;

	/**
	 * Creates a new {@link VarSymbol} with {@link MetaObject type} and {@link Object value} set to
	 * <code>null</code>.
	 */
	public VarSymbol() {
		this(null, null);
	}
	
	/**
	 * Creates a new {@link VarSymbol} with the given type and value.
	 */
	public VarSymbol(MetaObject aType, Object aValue) {
		this.type  = aType;
		this.value = aValue;
	}

	/**
	 * Returns the {@link MetaObject type} of the variable represented by this {@link VarSymbol}.
	 */
	public final MetaObject getType() {
		return this.type;
	}

	/**
	 * Sets the {@link MetaObject type} of the variable represented by this {@link VarSymbol}.
	 * 
	 * @see #getType()
	 */
	public final void setType(MetaObject newType) {
		this.type = newType;
	}

	/**
	 * Returns the {@link Object value} of the variable represented by this {@link VarSymbol}.
	 */
	public final Object getValue() {
		return this.value;
	}

	/**
	 * Sets the {@link Object value} of the variable represented by this {@link VarSymbol}.
	 * 
	 * @see #getValue()
	 */
	public final void setValue(Object newValue) {
		this.value = newValue;
	}

	@Override
	public String toString() {
		return VarSymbol.class.getSimpleName() + "( type = '" + type + "', value = '" + value + "' )";
	}

}
