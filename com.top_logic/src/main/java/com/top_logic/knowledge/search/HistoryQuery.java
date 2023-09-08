/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Query that searches for through the complete history of a {@link KnowledgeBase}.
 * 
 * @see RevisionQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HistoryQuery extends AbstractQuery<HistoryQuery> {
	
	private final RevisionParam revisionParam;

	/**
	 * Creates a {@link HistoryQuery}.
	 * 
	 * @param branchParam
	 *        See {@link #getBranchParam()}
	 * @param revisionParam
	 *        See {@link #getRevisionParam()}
	 * @param rangeParam
	 *        See {@link #getRangeParam()}
	 * @param searchParams
	 *        See {@link #getSearchParams()}
	 * @param search
	 *        See {@link #getSearch()}
	 */
	HistoryQuery(BranchParam branchParam, RevisionParam revisionParam, RangeParam rangeParam, List<ParameterDeclaration> searchParams, SetExpression search) {
		super(branchParam, rangeParam, searchParams, search);
		this.revisionParam = revisionParam;
	}
	
	/**
	 * Abstract specification of the {@link Revision} that should be searched.
	 * 
	 * @see RevisionParam
	 */
	public RevisionParam getRevisionParam() {
		return revisionParam;
	}

	@Override
	public <R, A> R visit(AbstractQueryVisitor<R, A> v, A arg) {
		return v.visitHistoryQuery(this, arg);
	}

	@Override
	protected HistoryQuery chain() {
		return this;
	}
	
	
}
