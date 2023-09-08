/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import java.util.List;

import com.top_logic.basic.config.annotation.External;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.model.impl.generated.TLStructuredTypeBase;

/**
 * {@link TLType} that is structured with {@link TLProperty}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TLStructuredType extends TLStructuredTypeBase {

	/**
	 * Find the part of this class or one of its potential super classes with
	 * the given {@link TLStructuredType#getName() name}.
	 * 
	 * @return The requested part, or <code>null</code> if neither this type nor
	 *         one of its potential super classes has a part with the given
	 *         name.
	 */
	// TODO: Implement "parts" property as union of all local parts.
	// @Indexed(collection = "parts")
	@External
	TLStructuredTypePart getPart(String name);

	/**
	 * The part with the given name.
	 */
	default TLStructuredTypePart getPartOrFail(String name) {
		TLStructuredTypePart result = getPart(name);
		if (result == null) {
			throw new NoSuchAttributeException("No such part '" + name + "' in '" + this + "'.");
		}
		return result;
	}

	/**
	 * Computed collection of all local parts of this type.
	 * 
	 * @see TLAssociation#getAssociationParts() Type safe access in {@link TLAssociation}.
	 * @see TLClass#getLocalClassParts() Type safe access in {@link TLClass}.
	 */
	@External
	List<? extends TLStructuredTypePart> getLocalParts();
	
	/**
	 * Computed collection of all parts of this type.
	 * 
	 * @return A potentially unmodifiable {@link List}.
	 * 
	 * @see TLAssociation#getAssociationParts() Type safe access in {@link TLAssociation}.
	 * @see TLClass#getAllClassParts() Type safe access in {@link TLClass}.
	 */
	@External
	default List<? extends TLStructuredTypePart> getAllParts() {
		return getLocalParts();
	}

}
