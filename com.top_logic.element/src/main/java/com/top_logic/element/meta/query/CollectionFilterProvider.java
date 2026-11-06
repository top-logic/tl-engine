/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Collection;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLObject;

/**
 * Factory for {@link CollectionFilter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CollectionFilterProvider {

	/**
	 * Creates a filter for the given attribute.
	 *
	 * @param anAttribute
	 *        The attribute to filter on.
	 * @param value
	 *        The values to search for.
	 * @param doNegate
	 *        Whether the filter condition is inverted.
	 * @param searchForEmptyValues
	 *        Whether to search for objects with empty value.
	 * @param anAccessPath
	 *        Path to the object whose attribute is filtered, may be <code>null</code>.
	 * @return The filter.
	 */
	CollectionFilter getFilter(TLStructuredTypePart anAttribute, Collection<? extends TLObject> value, boolean doNegate,
			boolean searchForEmptyValues, String anAccessPath);

}
