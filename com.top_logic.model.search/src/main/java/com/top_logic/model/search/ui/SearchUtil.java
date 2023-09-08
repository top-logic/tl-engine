/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.layout.form.model.FormContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TLUnionType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.interpreter.TypeResolver;

/**
 * Utilities for the TLScript search.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class SearchUtil {

	/**
	 * The types searched for in the search query.
	 * <p>
	 * Is not allowed to be called when the {@link FormContext} is not yet created and accessible.
	 * </p>
	 * 
	 * @return The types that the user searches for.
	 */
	public static Set<TLClass> getSearchedTypes(SearchExpression expression) {
		TLType type = TypeResolver.getType(expression);

		return type == null ? Collections.emptySet() : createSearchedTypes(type);
	}

	private static Set<TLClass> createSearchedTypes(TLType type) {
		if (type instanceof TLUnionType) {
			return new HashSet<>(((TLUnionType) type).getSpecializations());
		} else if (type instanceof TLClass) {
			return Collections.singleton((TLClass) type);
		} else {
			return Collections.emptySet();
		}
	}
}
