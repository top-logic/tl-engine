/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Set;

import com.top_logic.dob.MetaObject;

/**
 * Test {@link Expression} that checks the context object to be of the {@link #getTypeName()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypeCheck extends TypedExpression {
	
	private Set<? extends MetaObject> concreteTrueTypes;

	TypeCheck(Expression context, String typeName) {
		super(context, typeName);
	}

	public Set<? extends MetaObject> getConcreteTrueTypes() {
		return concreteTrueTypes;
	}
	
	public void setConcreteTrueTypes(Set<? extends MetaObject> concreteTypes) {
		this.concreteTrueTypes = concreteTypes;
	}
	
}
