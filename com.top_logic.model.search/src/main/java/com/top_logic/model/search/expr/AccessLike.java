/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collections;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.WithSecurityCheck;
import com.top_logic.model.security.ModelAccessRights;

/**
 * Mix-in interface with common implementations for expressions accessing a model value.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AccessLike extends WithFlatMapSemantics<TLStructuredTypePart>, WithSecurityCheck {

	@Override
	default Object evalDirect(EvalContext definitions, Object base, TLStructuredTypePart part) {
		if (base == null) {
			return null;
		}
		TLObject self = SearchExpression.asTLObject(this, base);
		Object value = lookupValue(definitions, self, part);
		if (value instanceof KnowledgeItem) {
			// Convert to TLObject for later processing
			value = SearchExpression.asTLObject(this, value);
		}
		return value;
	}

	/**
	 * Looks up the given attribute from the given object.
	 * 
	 * @param definitions
	 *        The {@link EvalContext}.
	 * @param self
	 *        The object to access.
	 * @param part
	 *        The attribute to look up.
	 * @return The value of the given attribute.
	 */
	default Object lookupValue(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
		return lookupValue(self, part, usesSecurity());
	}

	/**
	 * Looks up the given attribute from the given object.
	 * 
	 * <p>
	 * When <code>withSecurity</code> is requested, then only those elements which the user is
	 * allowed to see are returned.
	 * </p>
	 * 
	 * @param self
	 *        The object to access.
	 * @param part
	 *        The attribute to look up.
	 * @param withSecurity
	 *        Whether only those elements must be returned that the user is allowed to see.
	 * 
	 * @return The value of the given attribute.
	 */
	static Object lookupValue(TLObject self, TLStructuredTypePart part, boolean withSecurity) {
		if (withSecurity) {
			boolean hasReadRights = ModelAccessRights.getInstance().isReadAllowed(self, part);
			if (!hasReadRights) {
				if (part.isMultiple()) {
					if (part.isOrdered() || part.isBag()) {
						return Collections.emptyList();
					} else {
						return Collections.emptySet();
					}
				} else {
					return null;
				}
			}
		}

		Object value;
		if (part.isAbstract()) {
			// The concrete reference must be used to ensure that the correct storage implementation
			// is used.
			value = self.tValueByName(part.getName());
		} else {
			value = self.tValue(part);
		}
		Object returnValue = SearchExpression.normalizeValue(value);
		if (withSecurity) {
			returnValue = SearchExpression.filterSecurity(returnValue);
		}
		return returnValue;
	}

}
