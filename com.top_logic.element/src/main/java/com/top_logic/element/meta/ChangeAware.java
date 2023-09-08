/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import com.top_logic.model.TLObject;

/**
 * Optional interface for {@link TLObject} implementations to receive change notifications.
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public interface ChangeAware {

    /**
	 * Set all updates for this attributed contained in the given update at once.
	 * 
	 * This allows for updates of attributes that have dependencies. Such attributes can not always
	 * be set one after the other with the
	 * {@link TLObject#tUpdate(com.top_logic.model.TLStructuredTypePart, Object)} method.
	 * 
	 * @param anAUC
	 *        the updates to be performed
	 */
    public void updateValues(AttributeUpdateContainer anAUC);
    
    /**
	 * Notify an object of a forthcoming change. The method should be called by implementations of
	 * the {@link TLObject#tUpdate(com.top_logic.model.TLStructuredTypePart, Object)} and
	 * {@link #updateValues(AttributeUpdateContainer)} methods.
	 * 
	 * @param anAttributeName
	 *        the attribute whose value will be changed
	 * @param aNewValue
	 *        the new value of the attribute
	 */
    public void notifyPreChange (String anAttributeName, Object aNewValue);
}
