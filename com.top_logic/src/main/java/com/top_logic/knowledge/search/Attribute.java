/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOReference;

/**
 * Reference to the value of a {@link MOAttribute}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Attribute extends ContextExpression {
	private final String ownerTypeName;
	private final String attributeName;
	
	private MOAttribute attribute;
	
	/*package protected*/ Attribute(Expression context, String ownerTypeName, String attributeName) {
		super(context);
		this.ownerTypeName = ownerTypeName;
		this.attributeName = attributeName;
	}
	
	public String getOwnerTypeName() {
		return ownerTypeName;
	}

	public String getAttributeName() {
		return attributeName;
	}
	
	/**
	 * The referenced attribute.
	 */
	public MOAttribute getAttribute() {
		if (attribute == null) {
			throw new IllegalStateException("No type binding yet.");
		}
		return attribute;
	}

	/*package protected*/ void setAttribute(MOAttribute attribute) {
		if (attribute instanceof MOReference) {
			throw new IllegalArgumentException(
				"Attribute " + attribute + " must not be a " + MOReference.class.getSimpleName() + ".");
		}
		this.attribute = attribute;
	}

	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitAttribute(this, arg);
	}

}
