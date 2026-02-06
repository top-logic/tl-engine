/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.table.component;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Filter} accepting objects whose {@link TLObject#tType() type} is compatible with one of
 * the given types.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CorrectTypeFilter implements Filter<Object> {

	private Set<? extends TLType> _types;

	/**
	 * Creates a new {@link CorrectTypeFilter}.
	 * 
	 * @param types
	 *        Objects whose {@link TLObject#tType() type} is compatible with one of the given are
	 *        accepted.
	 */
	public CorrectTypeFilter(Set<? extends TLType> types) {
		_types = types;
	}

	/**
	 * Creates a filter that allows only {@link TLObject} whose {@link TLObject#tType()} is
	 * compatible to one of the given {@link TLType}.
	 * 
	 * <p>
	 * When the given types are empty, then all elements are accepted, also elements that are not
	 * {@link TLObject}.
	 * </p>
	 */
	public static Filter<Object> newTypeFilter(Collection<? extends TLType> types) {
		if (types == null || types.isEmpty()) {
			return FilterFactory.trueFilter();
		}
		return new CorrectTypeFilter(CollectionUtil.toSet(types));
		
	}

	@Override
	public boolean accept(Object obj) {
		if (!(obj instanceof TLObject)) {
			return false;
		}
		TLStructuredType itemType = ((TLObject) obj).tType();
		if (itemType == null) {
			return false;
		}
		if (_types.contains(itemType)) {
			// fast return when the item type is configured
			return true;
		}
		for (TLType type : _types) {
			/* getReflexiveTransitiveSpecializations is cached whereas getTransitiveSpecializations
			 * is computed! */
			if (type instanceof TLClass clazz) {
				if (TLModelUtil.getReflexiveTransitiveSpecializations(clazz).contains(itemType)) {
					return true;
				}
			}
		}
		return false;
	}

}

