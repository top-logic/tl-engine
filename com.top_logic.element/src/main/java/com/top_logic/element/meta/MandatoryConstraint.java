/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.Logger;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Check SimpleMetaAttributes if they are not <code>null</code>.
 * Check CollectionMetaAttributes if they are not empty.
 * Used to implement mandatory attributes.
 * 
 * @author    <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class MandatoryConstraint implements AttributeConstraint {
	/** The constraint type. */
	public static final String CONSTRAINT_TYPE = "mandatory";
	
	/**
	 * Default CTor
	 */
	public MandatoryConstraint() {
		super();
	}

	/**
	 * @see com.top_logic.element.meta.AttributeConstraint#getType()
	 */
	@Override
	public String getType() {
		return CONSTRAINT_TYPE;
	}

	@Override
	public boolean checkValue(AttributeUpdate update) {
        try {
        	Object theUpdateValue = AttributeOperations.getUpdateValue(update);

			TLStructuredTypePart attribute = update.getAttribute();
			if (!AttributeOperations.isCollectionValued(attribute)) {
        		return theUpdateValue != null;
        	}
        	else {
				return !((Collection<?>) theUpdateValue).isEmpty();
        	}
        } catch (Exception e) {
            Logger.error("Could not get value to check constraint.", e, this);
            return false;
        }
	}

	/**
	 * @see com.top_logic.element.meta.AttributeConstraint#getRequiredAttributes(com.top_logic.model.TLStructuredTypePart)
	 */
	@Override
	public Collection getRequiredAttributes(TLStructuredTypePart aMetaAttribute) {
		return Collections.EMPTY_LIST;
	}

}
