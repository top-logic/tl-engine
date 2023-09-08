/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Static helpers for querying properties of {@link TLStructuredTypePart}s.
 * 
 * @see AttributeOperations
 * @see MetaElementUtil
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AttributeUtil {

	/**
	 * Check if the given {@link TLStructuredTypePart} is an attribute of the given {@link Wrapper}
	 * object.
	 */
	public static void checkHasAttribute(TLObject self, TLStructuredTypePart attribute) throws AttributeException {
		if (self.tType().getPart(attribute.getName()) == null) {
			throw new AttributeException("Object '" + self + "' does not define attribute '" + attribute + "'.");
		}
	}

	/**
	 * Remove a value from a collection valued attribute.
	 * 
	 * <p>
	 * If the attribute is ordered and the value to remove occurs more than once in the current
	 * collection, the first occurrence is removed.
	 * </p>
	 * 
	 * @param aKey
	 *        the attribute name
	 * @param aValue
	 *        the value to remove
	 */
	public static void removeValue(Wrapper attributed, String aKey, Object aValue) {
		PersistentObjectImpl.removeValue(attributed, aKey, aValue);
	}

}
