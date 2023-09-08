/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * Base model for {@link SearchPart}s selecting certain objects from a base set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface SearchFilter extends WithValueContext {

	/**
	 * Visit method for {@link SearchFilter} parts.
	 */
	<R, A> R visitSearchFilter(SearchFilterVisitor<R, A> v, A arg);
	
}
