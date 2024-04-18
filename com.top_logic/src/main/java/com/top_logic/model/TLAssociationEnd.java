/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.model.impl.generated.TLAssociationEndBase;


/**
 * End points of {@link TLAssociation}s.
 * 
 * <p>
 * An association end point defines a key attribute of links that implement the
 * corresponding {@link TLAssociation}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLAssociationEnd extends TLAssociationEndBase, TLAssociationPart {

	/**
	 * The {@link TLReference}, this {@link TLAssociationEnd} implements.
	 * 
	 * @return The {@link TLReference} of this association end, if this
	 *         association end is the implementation of a reference attribute,
	 *         <code>null</code> otherwise.
	 */
	@Reference(other="end")
	TLReference getReference();

	/**
	 * Whether this reference points to the composite part of an aggregation association.
	 */
	boolean isComposite();
	
	/**
	 * Sets the {@link #isComposite()} property.
	 */
	void setComposite(boolean value);
			
	/**
	 * Whether this reference points to the aggregate part of an aggregation association.	
	 */
	boolean isAggregate();
			
	/**
	 * Sets the {@link #isAggregate()} property.
	 */
	void setAggregate(boolean value);
			
	/**
	 * Whether the navigation to this end is efficient.
	 * 
	 * <p>
	 * A reference is "efficient" if there is e.g. only a simple access. Assume that every project
	 * has one project manager. To go from the project to the corresponding project manager is
	 * therefore a simple reference access.
	 * </p>
	 * 
	 * <p>
	 * The backward reference is essentially inefficient. If you want to know in which project you
	 * are the project manager then all projects are scanned and it is checked if you are the
	 * project manager of this project.
	 * </p>
	 */
	boolean canNavigate();
	
	/**
	 * Sets the {@link #canNavigate()} property.
	 */
	void setNavigate(boolean value);
	
	/** 
	 * The type of history of the values of this reference. The values of this attribute can be
	 * either all current, historized, or a mixture of current and historized values.
	 */
	HistoryType getHistoryType();

	/**
	 * The index of this end in the owning {@link TLAssociation}'s ends.
	 * 
	 * @see TLAssociation#getEnds()
	 */
	default int getEndIndex() {
		return getOwner().getEnds().indexOf(this);
	}

	/**
	 * Setter for {@link #getHistoryType()}.
	 */
	void setHistoryType(HistoryType type);

	@Override
	default ModelKind getModelKind() {
		return ModelKind.END;
	}

	@Override
	default <R, A> R visitAssociationPart(TLAssociationPartVisitor<R, A> v, A arg) {
		return v.visitAssociationEnd(this, arg);
	}
}
