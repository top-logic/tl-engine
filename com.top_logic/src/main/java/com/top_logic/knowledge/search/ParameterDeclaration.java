/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;

/**
 * Declaration of a query parameter.
 * 
 * @see Parameter
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ParameterDeclaration extends AbstractQueryPart implements TypeSystemDependent {
	
	private final String typeName;
	private final String name;
	
	private MetaObject declaredType;
	
	ParameterDeclaration(String typeName, String name) {
		this.typeName = typeName;
		this.name = name;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}
	
	@Override
	public void setDeclaredType(MetaObject declaredType) {
		this.declaredType = declaredType;
	}
	
	/**
	 * the type of value filled into this parameter, e.g. if the parameter represents a
	 *         String valued attribute, the returned type is {@link MOPrimitive#STRING}.
	 */
	@Override
	public MetaObject getDeclaredType() {
		if (declaredType == null) {
			throw new IllegalStateException("No type binding yet.");
		}
		return declaredType;
	}
	
	@Override
	public boolean hasTypeBinding() {
		return declaredType != null;
	}

	/**
	 * the name of this parameter
	 */
	public String getName() {
		return name;
	}

	@Override
	public <R, A> R visit(AbstractQueryVisitor<R,A> v, A arg) {
		return v.visitParameterDeclaration(this, arg);
	}

}
