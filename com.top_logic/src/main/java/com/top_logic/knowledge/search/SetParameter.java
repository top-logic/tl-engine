/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;

/**
 * {@link QueryParameter} that is set-valued.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetParameter extends SetExpression implements QueryParameter {

	private String name;
	private MetaObject declaredType;
	
	SetParameter(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public MetaObject getDeclaredType() {
		return declaredType;
	}
	
	@Override
	public void setDeclaredType(MetaObject declaredType) {
		this.declaredType = declaredType;
	}

	@Override
	public <R, A> R visitSetExpr(SetExpressionVisitor<R, A> v, A arg) {
		return v.visitSetParameter(this, arg);
	}

}
