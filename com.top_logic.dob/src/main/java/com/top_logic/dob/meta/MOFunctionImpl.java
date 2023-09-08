/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;

/**
 * Basic meta object for functions.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class MOFunctionImpl extends AbstractMetaObject implements MOFunction {

	private List<MetaObject> argumentTypes;
	private MetaObject returnType;
	private final boolean isVarArg;
	
	public MOFunctionImpl(String name, MetaObject returnType, List<MetaObject> argumentTypes, boolean isVarArg) {
		super(name);
		this.returnType = returnType;
		this.argumentTypes = argumentTypes;
		this.isVarArg = isVarArg;
	}
	
	public MOFunctionImpl(String name, MetaObject returnType, List<MetaObject> argumentTypes) {
		this(name, returnType, argumentTypes, false);
	}

	@Override
	public List<MetaObject> getArgumentTypes() {
		return this.argumentTypes;
	}

	@Override
	public MetaObject getReturnType() {
		return this.returnType;
	}

	@Override
	public Kind getKind() {
		return Kind.function;
	}

	@Override
	public boolean isVarArg() {
		return this.isVarArg;
	}
	
	@Override
	public MetaObject copy() {
		return new MOFunctionImpl(getName(), typeRef(returnType), typesRef(argumentTypes), isVarArg);
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		resolveTypes(context, argumentTypes);
		returnType = returnType.resolve(context);
		return this;
	}

}
