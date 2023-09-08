/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.LiberalAccessChecker;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.impl.AbstractStructuredTypePart;
import com.top_logic.model.impl.generated.TLStructuredTypePartBase;

/**
 * Part of a {@link TLStructuredType}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLStructuredTypePart extends DerivedTLTypePart, TLStructuredTypePartBase {

	@Override
	@Container
	TLStructuredType getOwner();

	/**
	 * Whether this attribute may not be empty.
	 * 
	 * <p>
	 * If this is a reference, it must point to at least one object.
	 * </p>
	 */
	boolean isMandatory();
			
	/**
	 * Sets the {@link #isMandatory()} property.
	 */
	void setMandatory(boolean value);

	/**
	 * Whether this part can hold multiple values of {@link #getType()} (collection valued).
	 */
	boolean isMultiple();

	/**
	 * Sets the {@link #isMultiple()} property.
	 */
	void setMultiple(boolean value);

	/**
	 * If {@link #isMultiple()}, whether the same value may appear more than once in the values of
	 * this part (not set valued).
	 */
	boolean isBag();

	/**
	 * Sets the {@link #isBag()} property.
	 */
	void setBag(boolean value);

	/**
	 * If {@link #isMultiple()}, whether the order of values in this part is significant (list or
	 * ordered set valued).
	 */
	boolean isOrdered();

	/**
	 * Sets the {@link #isOrdered()} property.
	 */
	void setOrdered(boolean value);

	/**
	 * Returns an {@link AccessChecker} for this type part.
	 * 
	 * <p>
	 * Return {@link LiberalAccessChecker} when no restriction must be considered.
	 * </p>
	 */
	AccessChecker getAccessChecker();

	@Override
	TLStructuredTypePart getDefinition();

	/**
	 * Framework internal. Don't use outside the {@link TLModel} itself.
	 */
	@FrameworkInternal
	void setDefinition(TLStructuredTypePart newDefinition);

	/**
	 * Recalculate {@link #getDefinition()}.
	 */
	void updateDefinition();

	/**
	 * Whether this {@link TLStructuredTypePart} overrides another.
	 */
	boolean isOverride();

	/**
	 * Encapsulation of operational semantics of this attribute.
	 */
	StorageDetail getStorageImplementation();

	@Override
	default <T extends TLAnnotation> T getAnnotation(Class<T> annotationInterface) {
		T attributeAnnotation = getAnnotationLocal(annotationInterface);
		if (attributeAnnotation != null) {
			return attributeAnnotation;
		}
		return AbstractStructuredTypePart.getDefaultsFromAttribute(annotationInterface, this);
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
	<R, A> R visitStructuredTypePart(TLStructuredTypePartVisitor<R, A> v, A arg);

	@Override
	default <R, A> R visitTypePart(TLTypePartVisitor<R, A> v, A arg) {
		return visitStructuredTypePart(v, arg);
	}

}
