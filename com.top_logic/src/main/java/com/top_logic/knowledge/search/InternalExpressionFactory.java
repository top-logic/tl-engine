/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;

/**
 * Internal factory for {@link QueryPart} expressions that create nodes with
 * type implementation binding.
 * 
 * <p>
 * This factory must only be used from transformations that have to keep an
 * existing type implementation binding.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InternalExpressionFactory {

	/**
	 * Creates an {@link Reference} for the given {@link MOReference} which contains type
	 * informations.
	 */
	public static Reference referenceTyped(MOReference attribute) {
		return newTypedReference(attribute, null);
	}

	/**
	 * Creates an {@link Expression} for the given {@link ReferencePart} for the given
	 * {@link MOReference} which contains type informations.
	 */
	public static Expression referenceTyped(MOReference attribute, ReferencePart accessPart) {
		return newTypedReference(attribute, accessPart);
	}

	private static Reference newTypedReference(MOReference attribute, ReferencePart accessPart) {
		String ownerType = ExpressionFactory.getOwnerTypeName(attribute);
		String attributeName = attribute.getName();
		Reference reference = new Reference(context(), ownerType, attributeName, accessPart);
		InternalExpressionFactory.setReferenceAttributeImpl(reference, attribute);
		return reference;
	}

	public static Attribute attributeTyped(MOAttribute attribute) {
		Attribute attr = new Attribute(context(), ExpressionFactory.getOwnerTypeName(attribute), attribute.getName());
		InternalExpressionFactory.setAttributeImpl(attr, attribute);
		return attr;
	}

	/**
	 * Constructs a {@link Flex} attributes access expression.
	 */
	public static Flex flexTyped(MetaObject type, String name) {
		Flex flex = new Flex(context(), type.getName(), name);
		flex.setDeclaredType(type);
		return flex;
	}

	public static HasType hasTypeTyped(MetaObject type) {
		HasType hasType = new HasType(context(), type.getName());
		hasType.setDeclaredType(type);
		return hasType;
	}

	public static InstanceOf instanceOfTyped(MetaObject type) {
		InstanceOf instanceOf = new InstanceOf(context(), type.getName());
		instanceOf.setDeclaredType(type);
		return instanceOf;
	}

	public static AllOf allOfTyped(MOClass type) {
		AllOf allOf = new AllOf(type.getName());
		allOf.setDeclaredType(type);
		return allOf;
	}

	public static AnyOf anyOfTyped(MOClass type) {
		AnyOf anyOf = new AnyOf(type.getName());
		anyOf.setDeclaredType(type);
		return anyOf;
	}

	public static Expression attributeRangeTyped(MOAttribute attr, Comparable startValue, Comparable stopValue) {
		if (startValue.compareTo(stopValue) < 0) {
			return 
				and(
					ge(attributeTyped(attr), literal(startValue)),
					lt(attributeTyped(attr), literal(stopValue)));
		}
		return literal(false);
	}

	public static Expression attributeEqBinaryTyped(MOAttribute attr, Object value) {
		return eqBinaryLiteral(attributeTyped(attr), value);
	}

	public static void setAttributeImpl(Attribute expr, MOAttribute attribute) {
		expr.setAttribute(attribute);
	}

	public static void setReferenceAttributeImpl(Reference expr, MOReference referenceAttr) {
		expr.setAttribute(referenceAttr);
	}
	
}
