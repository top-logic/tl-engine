/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;

/**
 * Named parameter value in an {@link Expression}. The value of that parameter must not be
 * <code>null</code>.
 * 
 * @see ParameterDeclaration
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Parameter extends Expression implements QueryParameter {
	
	private String name;
	private MetaObject declaredType;
	
	Parameter(String name) {
		super();
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setDeclaredType(MetaObject declaredType) {
		this.declaredType = declaredType;
	}
	
	@Override
	public MetaObject getDeclaredType() {
		return declaredType;
	}

	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitParameter(this, arg);
	}

}
