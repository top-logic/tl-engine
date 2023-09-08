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
 * Template function that checks whether a given object exists (i.e. is not <code>null</code>).
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class ExistsFunction extends AbstractBooleanFunction {
	
	public static final String FUNCTION_NAME = "exists";
	

	public ExistsFunction() {
		super(FUNCTION_NAME);
	}

	/**
	 * Checks if the first object given {@link List} not <code>null</code>.
	 * 
	 * @param arguments a {@link List} of arguments
	 * @return <code>true</code> if the given object is not <code>null</code>, <code>false</code>
	 *         otherwise.
	 */
	@Override
	public Object apply(List<?> arguments) {
		return arguments.get(0) != null;
	}

	@Override
	protected List<MetaObject> getArgumentTypes() {
		return CollectionUtil.createList(MetaObject.ANY_TYPE);
	}
}
