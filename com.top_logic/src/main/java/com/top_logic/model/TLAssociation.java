/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.model.impl.generated.TLAssociationBase;

/**
 * An association type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLAssociation extends TLAssociationBase {
	
	/**
	 * The set of {@link TLAssociation}s from which this one is derived by being a strict union of
	 * their links.
	 * 
	 * <p>
	 * An {@link TLAssociation} that is not derived from other {@link TLAssociation}s has an empty
	 * {@link #getSubsets() set of subset}.
	 * </p>
	 * 
	 * <p>
	 * The result cannot be modified.
	 * </p>
	 * 
	 * <p>
	 * A {@link TLAssociation} is in the resulting set if and only if this association is within its
	 * {@link #getUnions()} set.
	 * </p>
	 * 
	 * @return The non-empty union set for a derived association, or an empty set for a regular
	 *         association.
	 * 
	 * @see #getUnions()
	 */
	@Reference(other = "unions")
	Set<TLAssociation> getSubsets();
	
	/**
	 * The derived {@link TLAssociation}s this one is a subset of.
	 * 
	 * <p>
	 * The result can be modified.
	 * </p>
	 * 
	 * @see #getSubsets()
	 */
	@Reference(other = "subsets")
	Set<TLAssociation> getUnions();
	
	/**
	 * Type safe access to {@link TLStructuredType#getLocalParts()}.
	 * 
	 * @return Same value as {@link TLStructuredType#getLocalParts()}.
	 * 
	 * @see TLStructuredType#getLocalParts()
	 * @see TLClass#getLocalClassParts()
	 */
	List<TLAssociationPart> getAssociationParts();

	@Override
	default ModelKind getModelKind() {
		return ModelKind.ASSOCIATION;
	}

	@Override
	default <R, A> R visitType(TLTypeVisitor<R, A> v, A arg) {
		return v.visitAssociation(this, arg);
	}

}
