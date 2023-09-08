/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.structure;

import com.top_logic.model.search.ui.model.AssociationFilter;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.search.ui.model.CombinedFilter;
import com.top_logic.model.search.ui.model.ContextFilter;
import com.top_logic.model.search.ui.model.IncomingReferenceFilter;
import com.top_logic.model.search.ui.model.operator.TypeCheck;

/**
 * Visitor interface for {@link SearchFilter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SearchFilterVisitor<R, A> {

	/**
	 * Visit case {@link AttributeFilter}.
	 */
	R visitAttributeFilter(AttributeFilter filter, A arg);

	/**
	 * Visit case {@link AssociationFilter}.
	 */
	R visitAssociationFilter(AssociationFilter filter, A arg);

	/**
	 * Visit case {@link ContextFilter}.
	 */
	R visitContextFilter(ContextFilter filter, A arg);

	/**
	 * Visit case {@link IncomingReferenceFilter}.
	 */
	R visitIncomingReferenceFilter(IncomingReferenceFilter filter, A arg);

	/**
	 * Visit case {@link TypeCheck}.
	 */
	R visitTypeCheck(TypeCheck filter, A arg);

	/**
	 * Visit case {@link CombinedFilter}.
	 */
	R visitCombinedFilter(CombinedFilter filter, A arg);

}
