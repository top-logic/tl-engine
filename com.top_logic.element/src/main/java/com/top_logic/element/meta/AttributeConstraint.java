/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.Collection;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A constraint of the values of a MetaAttributes. The constraint may depend on the values of other
 * {@link TLStructuredTypePart}s of an {@link TLObject}.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public interface AttributeConstraint {

    /**
     * Get the type of the constraint.
     * 
     * @return the type of the constraint. Must not be <code>null</code> or empty.
     */
    public String getType();

    /**
	 * Check if the update for the given attribute is valid.
	 * 
	 * @param update
	 *        The value to check.
	 * 
	 * @return <code>true</code> if the update for the given attribute is valid, <code>false</code>
	 *         otherwise.
	 */
    public boolean checkValue(AttributeUpdate update);
    
    /**
     * Return a Collection of the required MetaAttributes. 
     * 
     * @param aMetaAttribute the Attribute to get the required Attributes for.
     * 						 Must not be <code>null</code>.
     * @return MetaAttributes are required to check this constraint. 
     *         May be empty but not <code>null</code> if no properties are required.
     */
    public Collection getRequiredAttributes(TLStructuredTypePart aMetaAttribute);

}
