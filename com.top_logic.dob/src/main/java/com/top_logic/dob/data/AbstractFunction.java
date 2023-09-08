/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import java.util.List;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOFunction;

/**
 * Base class for {@link Function} implementations.
 * 
 * @author    <a href=mailto:till.bentz@top-logic.com>Till Bentz</a>
 */
public abstract class AbstractFunction implements Function {

	protected MOFunction moFunction;
	
	@Override
	public abstract Object apply(List<?> arguments);

	@Override
	public MOFunction getType() {
		return this.moFunction;
	}
	
	/**
	 * the list of the types of the arguments of this function
	 */
	protected abstract List<MetaObject> getArgumentTypes();

	/**
	 * the return type of this function
	 */
	protected abstract MetaObject getReturnType();

}
