/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.model.impl.generated.TLReferenceBase;

/**
 * A reference attribute of a {@link TLClass}.
 * 
 * @see TLProperty for a primitive attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Reference")
public interface TLReference extends TLReferenceBase, TLClassPart {

	/**
	 * The association endpoint that implements this reference attribute.
	 */
	@Reference(other="reference")
	TLAssociationEnd getEnd();

	/**
	 * Sets the {@link #getEnd()} property.
	 */
	void setEnd(TLAssociationEnd value);
	
	/**
	 * Same as {@link TLAssociationEnd#getType() type} of the implemented
	 * {@link TLReference#getEnd() end}.
	 * 
	 * @see com.top_logic.model.TLTypePart#getType()
	 */
	@Override
	TLType getType();

	/**
	 * Same as {@link TLAssociationEnd#isMandatory() mandatory} of the implemented
	 * {@link TLReference#getEnd() end}.
	 * 
	 * @see com.top_logic.model.TLStructuredTypePart#isMandatory()
	 */
	@Override
	default boolean isMandatory() {
		return getEnd().isMandatory();
	}

	@Override
	default void setMandatory(boolean value) {
		getEnd().setMandatory(value);
	}

	@Override
	default boolean isOrdered() {
		return getEnd().isOrdered();
	}

	@Override
	default void setOrdered(boolean value) {
		getEnd().setOrdered(value);
	}

	@Override
	default boolean isAbstract() {
		return getEnd().isAbstract();
	}

	@Override
	default void setAbstract(boolean value) {
		getEnd().setAbstract(value);
	}

	@Override
	default boolean isBag() {
		return getEnd().isBag();
	}

	@Override
	default void setBag(boolean value) {
		getEnd().setBag(value);
	}

	@Override
	default boolean isMultiple() {
		return getEnd().isMultiple();
	}

	@Override
	default void setMultiple(boolean value) {
		getEnd().setMultiple(value);
	}

	/**
	 * Whether this is a reverse reference to a regular forwards reference.
	 * 
	 * @see #getOpposite()
	 */
	default boolean isBackwards() {
		return getEnd().getEndIndex() == 0;
	}

	/**
	 * The {@link TLReference} in the {@link #getType() target type} that points in reverse
	 * direction.
	 * 
	 * @return The opposite reference if such reference exists in the target type, <code>null</code>
	 *         otherwise.
	 * 
	 * @see TLAssociation#getEnds()
	 */
	default TLReference getOpposite() {
		TLAssociationEnd selfEnd = getEnd();
		List<TLAssociationEnd> ends = selfEnd.getOwner().getEnds();
		int selfIndex = ends.indexOf(selfEnd);
		int oppositeIndex = 1 - selfIndex;

		return ends.get(oppositeIndex).getReference();
	}

	/**
	 * The type of history of the values of this reference. The values of this attribute can be
	 * either all current, historized, or a mixture of current and historized values.
	 */
	default HistoryType getHistoryType() {
		return getEnd().getHistoryType();
	}

	/**
	 * Setter for {@link #getHistoryType()}.
	 */
	default void setHistoryType(HistoryType type) {
		getEnd().setHistoryType(type);
	}

	/**
	 * Resolves all objects pointing to the given object through this reference.
	 * 
	 * @param element
	 *        The element that is searched in the contents of this {@link TLReference}.
	 * @return All base objects that contain the given element in the value of this reference.
	 */
	Set<? extends TLObject> getReferers(TLObject element);

	@Override
	default ModelKind getModelKind() {
		return ModelKind.REFERENCE;
	}

	@Override
	default <R, A> R visitClassPart(TLClassPartVisitor<R, A> v, A arg) {
		return v.visitReference(this, arg);
	}

}
