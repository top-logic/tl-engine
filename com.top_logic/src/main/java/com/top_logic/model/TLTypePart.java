/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.model.impl.generated.TLTypePartBase;
import com.top_logic.model.util.TLModelUtil;

/**
 * Common properties of all parts of {@link TLType}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLTypePart extends TLTypePartBase {

	/**
	 * The owner type, this part belongs to.
	 */
	@Container
	TLType getOwner();

	/**
	 * The target type this part references.
	 */
	@Reference(other="")
	TLType getType();
	
	/**
	 * Sets the {@link #getType()} property.
	 */
	void setType(TLType value);

	/**
	 * The uppermost overridden part.
	 * <p>
	 * Finds the part (indirectly) overridden by this part, which is not overriding another part. If
	 * this part is not overriding another part, this method return this part itself.
	 * </p>
	 * <p>
	 * Equivalent definition: Returns this part, if it is not overriding another part. Otherwise,
	 * returns the definition of the part it overrides.
	 * </p>
	 * <p>
	 * {@link TLTypePart}s in a {@link TLModel} have exactly one definition.
	 * </p>
	 * 
	 * @see TLModelUtil#getDefinitions(TLStructuredTypePart)
	 */
	default TLTypePart getDefinition() {
		return this;
	}

	/**
	 * Visits this {@link TLTypePart} with the given {@link TLTypePartVisitor}.
	 * 
	 * @param <R>
	 *        The result type.
	 * @param <A>
	 *        The argument type.
	 * @param v
	 *        The visitor.
	 * @param arg
	 *        The argument to the visit.
	 * @return The result of the visit.
	 */
	<R, A> R visitTypePart(TLTypePartVisitor<R, A> v, A arg);

	@Override
	default <R, A> R visit(TLModelVisitor<R, A> v, A arg) {
		return visitTypePart(v, arg);
	}

}
