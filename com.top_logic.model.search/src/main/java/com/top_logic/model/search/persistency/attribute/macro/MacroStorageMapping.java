/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro;

import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * {@link StorageMapping} for attributes with {@link MacroAttribute} {@link StorageImplementation}.
 * 
 * <p>
 * A macro attribute displays the evaluation of a configured function with the target object
 * (defining the attribute) as single argument.
 * </p>
 * 
 * @see com.top_logic.model.search.persistency.attribute.macro.MacroAttribute.Config#getExpr()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MacroStorageMapping implements StorageMapping<SearchExpression> {

	/**
	 * Singleton {@link MacroStorageMapping} instance.
	 */
	public static final MacroStorageMapping INSTANCE = new MacroStorageMapping();

	private MacroStorageMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<SearchExpression> getApplicationType() {
		return SearchExpression.class;
	}

	@Override
	public SearchExpression getBusinessObject(Object aStorageObject) {
		return (SearchExpression) aStorageObject;
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return aBusinessObject;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		// Note: This mapping just announces the SearchExpression type, but does not allow storing
		// any values. Macros are derived attributes that retrieve their value only from their
		// configured StorageImplementation.
		return false;
	}

}
