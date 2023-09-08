/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ListDefault;

/**
 * {@link SubSearch} searching for the union of some {@link #getUnions() search expressions}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface UnionSearch extends SubSearch {

	/**
	 * Property name of {@link #getUnions()}.
	 */
	String UNIONS = "unions";

	/**
	 * The result of this {@link Search} is the union of the results of the {@link TypeSearch}es.
	 */
	@Name(UNIONS)
	@ListDefault({ TypeSearch.class })
	Collection<UnionSearchPart> getUnions();

}
