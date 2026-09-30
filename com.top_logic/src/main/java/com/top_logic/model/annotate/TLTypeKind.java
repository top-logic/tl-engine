/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;

/**
 * Coarse-grained classification of model types for deciding applicability of {@link TLAnnotation}s.
 * 
 * @see TargetType
 * @see com.top_logic.model.TLPrimitive.Kind
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum TLTypeKind {

	/**
	 * A boolean value.
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#BOOLEAN
	 */
	BOOLEAN,

	/**
	 * A floating point number.
	 * 
	 * <p>
	 * No precision e.g. <code>float</code>, or <code>double</code> is implied.
	 * </p>
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#FLOAT
	 */
	FLOAT,

	/**
	 * An integer number.
	 * 
	 * <p>
	 * No size constraint e.g. <code>int</code>, or <code>long</code> is implied.
	 * </p>
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#INT
	 */
	INT,

	/**
	 * A string of characters.
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#STRING
	 */
	STRING,

	/**
	 * A date value.
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#DATE
	 */
	DATE,

	/**
	 * A string of <code>byte</code> values.
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#BINARY
	 */
	BINARY,

	/**
	 * A primitive type that does not match any of the above criteria.
	 * 
	 * @see com.top_logic.model.TLPrimitive.Kind#CUSTOM
	 */
	CUSTOM,

	/**
	 * A {@link TLEnumeration} reference.
	 */
	ENUMERATION,

	/**
	 * A general reference.
	 */
	REF,

	/**
	 * A composition reference.
	 * 
	 * @see TLAssociationEnd#isComposite()
	 */
	COMPOSITION;

	/**
	 * Determines the {@link TLTypeKind} of a given {@link TLTypePart}.
	 * 
	 * @param part
	 *        Part to get {@link TLTypeKind} for.
	 * @return The {@link TLTypeKind} for the given type part.
	 * 
	 * @see #getTLTypeKind(TLType)
	 */
	public static TLTypeKind getTLTypeKind(TLTypePart part) {
		if (part instanceof TLReference) {
			if (((TLReference) part).getEnd().isComposite()) {
				return COMPOSITION;
			}
		}

		return getTLTypeKind(part.getType());
	}

	/**
	 * Determines the {@link TLTypeKind} of a given {@link TLType}.
	 * 
	 * @param type
	 *        The type to get {@link TLTypeKind} for.
	 * @return The {@link TLTypeKind} for the given type.
	 */
	public static TLTypeKind getTLTypeKind(TLType type) {
		switch (type.getModelKind()) {
			case CLASS:
				return REF;
			case ENUMERATION:
				return ENUMERATION;
			case DATATYPE:
				Kind primitiveKind = ((TLPrimitive) type).getKind();
				return typeKind(primitiveKind);
			case ASSOCIATION:
			case CLASSIFIER:
			case END:
			case MODEL:
			case MODULE:
			case PROPERTY:
			case REFERENCE:
			case OBJECT:
				throw new IllegalArgumentException("Not an attribute type: " + type);
		}
		throw new UnreachableAssertion("No such kind: " + type.getModelKind());
	}

	private static TLTypeKind typeKind(Kind primitiveKind) {
		switch (primitiveKind) {
			case BINARY:
				return BINARY;
			case BOOLEAN:
				return BOOLEAN;
			case CUSTOM:
				return CUSTOM;
			case DATE:
				return DATE;
			case FLOAT:
				return FLOAT;
			case INT:
				return INT;
			case STRING:
				return STRING;
			case TRISTATE:
				return BOOLEAN;
		}
		throw new UnreachableAssertion("No such type kind: " + primitiveKind);
	}


}
