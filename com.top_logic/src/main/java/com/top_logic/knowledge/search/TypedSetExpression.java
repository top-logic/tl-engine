/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;

/**
 * {@link SetExpression} with an explicit {@link #getTypeName() type} declaration.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TypedSetExpression extends SetExpression implements TypeSystemDependent {

	private final String typeName;
	
	private MetaObject declaredType;

	/**
	 * Creates a {@link AllOf}.
	 * 
	 * @param typeName
	 *        See {@link #getTypeName()}.
	 */
	TypedSetExpression(String typeName) {
		this.typeName = typeName;
	}

	@Override
	public String getTypeName() {
		return typeName;
	}

	@Override
	public boolean hasTypeBinding() {
		return this.declaredType != null;
	}
	
	@Override
	public MetaObject getDeclaredType() {
		if (declaredType == null) {
			throw new IllegalStateException("No type binding yet.");
		}
		return declaredType;
	}
	
	@Override
	public void setDeclaredType(MetaObject declaredType) {
		this.declaredType = declaredType;
	}
	
	@Override
	public MOClass getPolymorphicType() {
		return (MOClass) super.getPolymorphicType();
	}
	
}
