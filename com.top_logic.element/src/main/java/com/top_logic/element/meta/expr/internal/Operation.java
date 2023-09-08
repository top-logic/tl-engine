/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomAttributeValueLocator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AttributeValueLocator} operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Operation extends CustomAttributeValueLocator {

	/**
	 * Service method to look up a type part.
	 * 
	 * @param context
	 *        The context object in whose model the attribute is requested.
	 */
	protected static TLReference getAttribute(TLObject context, String typeSpec, String attributeName) {
		TLClass type = type(context, typeSpec);
		TLStructuredTypePart attribute = type.getPart(attributeName);
		if (attribute == null) {
			throw new ConfigurationError("No such attribute '" + attributeName + "' in type '" + typeSpec);
		}
		if (!TLModelUtil.isReference(attribute)) {
			throw new ConfigurationError("Not a reference attribute: " + typeSpec + "#" + attributeName);
		}
		return (TLReference) attribute;
	}

	/**
	 * Service method to look up a type.
	 * 
	 * @param context
	 *        The context object in whose model the type should be resolved.
	 */
	protected static TLClass type(TLObject context, String typeSpec) {
		TLType type = TLModelUtil.findType(context.tType().getModel(), typeSpec);
		if (!(type instanceof TLClass)) {
			throw new ConfigurationError("Not a class type '" + typeSpec + "': " + type);
		}
		return (TLClass) type;
	}
	
}
