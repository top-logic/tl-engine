/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;


import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.WithSecurityCheck;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;

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
	 * When <code>withSecurity</code> is requested, the access is denied (an empty value is returned)
	 * if the user must not read the attribute on the base object {@code self}.
	 * </p>
	 *
	 * <p>
	 * The looked-up value itself is returned unfiltered: a reference is not a query, so the
	 * referenced object is delivered even if the user must not read it, consistent with the GUI,
	 * which shows the label of a referenced object and only gates the navigation into it. Reading
	 * the attributes of such an object is denied by the access check of that attribute access. The
	 * result of a whole script execution is a different matter: it is filtered for the read rights
	 * of the user, see {@link SearchExpression#filterSecurity(Object, SecurityFilterReport)}, and a
	 * {@link SecurityFilterReport} tells the caller what that filter removed.
	 * </p>
	 *
	 * @param self
	 *        The object to access.
	 * @param part
	 *        The attribute to look up.
	 * @param withSecurity
	 *        Whether the read access to {@code self} must be checked.
	 *
	 * @return The value of the given attribute.
	 */
	static Object lookupValue(TLObject self, TLStructuredTypePart part, boolean withSecurity) {
		if (withSecurity) {
			boolean hasReadRights = ModelAccessRights.getInstance().isReadAllowed(self, part);
			if (!hasReadRights) {
				return TLModelUtil.getEmptyValue(part);
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
		// Note: The looked-up value is returned unfiltered. Security is enforced on the base object
		// (the access above is denied if the user must not read the attribute), not on the
		// referenced value - consistent with the GUI, which always shows a referenced object (its
		// label) and only gates navigation into it. The final result of a script must be secured by
		// the caller.
		return SearchExpression.normalizeValue(value);
	}

}
