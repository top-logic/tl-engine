/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;

/**
 * Type check that tests an instance of being of a given concrete
 * {@link MetaObject} type (no subclasses are considered).
 * 
 * @see InstanceOf Check of polymorphic type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasType extends TypeCheck {
	
	/*package protected*/ HasType(Expression context, String typeName) {
		super(context, typeName);
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitHasType(this, arg);
	}
	
}
