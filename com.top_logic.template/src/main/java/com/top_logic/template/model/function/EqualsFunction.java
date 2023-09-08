/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model.function;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.MetaObject;

/**
 * Template function that checks whether two given Objects are equal.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class EqualsFunction extends AbstractBooleanFunction{
	
	public static final String FUNCTION_NAME = "equals";
	
	public EqualsFunction() {
		super(FUNCTION_NAME);
	}
	
	/**
	 * Checks if the given objects are equal.
	 * 
	 * @param arguments a {@link List} of arguments
	 * @return <code>true</code> if the given objects are equal (or both <code>null</code>),
	 *         <code>false</code> otherwise.
	 */
	@Override
	public Object apply(List<?> arguments) {
		Object a1 = arguments.get(0);
		Object a2 = arguments.get(1);
		if (a1 != null) {
			return a1.equals(a2);
		}
		else {
			return (a2 == null);
		}
	}

	@Override
	protected List<MetaObject> getArgumentTypes() {
		return CollectionUtil.createList(MetaObject.ANY_TYPE, MetaObject.ANY_TYPE);
	}
}
