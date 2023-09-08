/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model.function;

import java.util.List;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.data.AbstractFunction;
import com.top_logic.dob.meta.MOFunctionImpl;

/**
 * Abstract base class of functions whose return type is {@link MOPrimitive#BOOLEAN}.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class AbstractBooleanFunction extends AbstractFunction {

	
	public AbstractBooleanFunction(String aName) {
		this.moFunction = new MOFunctionImpl(aName, getReturnType(), getArgumentTypes());	
	}
	
	@Override
	public Object apply(List<?> arguments) {
		//  TODO tbe Automatically created
		return null;
	}

	@Override
	protected MetaObject getReturnType() {
		return MOPrimitive.BOOLEAN;
	}

}
