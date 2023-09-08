/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.MetaObject;

/**
 * Type description for a function. A function has a return type, a list of argument types and a
 * flag defining whether it can take a variable number of parameters for the last argument in the
 * argument list.
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public interface MOFunction extends MetaObject {
	
	/** 
	 * the return type of this function.
	 */
	public MetaObject getReturnType();
	
	/** 
	 * a {@link List} of {@link MetaObject}s for this functions arguments.
	 */
	public List<MetaObject> getArgumentTypes();
	
	/**
	 * If <code>true</code>, this function can take a variable number of parameters for the last
	 * argument in the parameter list.
	 */
	public boolean isVarArg();

}
