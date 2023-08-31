/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.config.annotation.LocatorNameFormat;
import com.top_logic.model.TLObject;

/**
 * Generic algorithm for finding derived values based on some start object.
 * 
 * <p>
 * <b>Note:</b> Custom implementations must use {@link CustomAttributeValueLocator} as base class.
 * </p>
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
@Format(LocatorNameFormat.class)
public interface AttributeValueLocator {

	/**
	 * Get the value for the given object.
	 * 
	 * @param anObject
	 *        the attributed
	 * 
	 * @return The value for the given object.
	 */
	public Object locateAttributeValue(Object anObject);

	/**
	 * Searches for the {@link TLObject}s whose {@link #locateAttributeValue(Object) value} is the
	 * given <code>value</code>.
	 * 
	 * @param value
	 *        Not <code>null</code>.
	 * @return The {@link TLObject}s that have the given object as value, or empty set when it is
	 *         not possible to determine referers.
	 */
	default Set<? extends TLObject> locateReferers(Object value){
		return Collections.emptySet();
	}

}
