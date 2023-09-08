/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.ui.model.options.structure.FilterOptions;
import com.top_logic.model.search.ui.model.structure.SearchFilter;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * {@link SearchPart} that defines an object base set and therefore can apply {@link SearchFilter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface FilterContainer extends NamedDefinition {

	/**
	 * Property name of {@link #getFilters()}.
	 */
	String FILTERS = "filters";

	/**
	 * The name for the sub-search is filled automatically.
	 */
	@Override
	@ReadOnly
	String getName();

	/**
	 * List of {@link SearchFilter} to apply to the context objects.
	 */
	@Name(FILTERS)
	@CustomOptionOrder
	@Options(fun = FilterOptions.class, args = { @Ref(CONTEXT), @Ref(VALUE_TYPE) })
	Collection<? extends SearchFilter> getFilters();

}
