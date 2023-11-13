/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.Date;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLType;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Generic method resolving the newest revision not later than a given {@link Date}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionForCommit extends GenericMethod {

	/**
	 * Creates a new {@link RevisionForCommit}.
	 */
	protected RevisionForCommit(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new RevisionForCommit(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return TlCoreFactory.getRevisionType();
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		long commitNumber = asLong(arguments[0]);
		if (commitNumber < 0) {
			throw new TopLogicException(I18NConstants.ERROR_NEGATIVE_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
		} else if (commitNumber == 0) {
			return Revision.INITIAL;
		} else if (commitNumber == Revision.CURRENT_REV) {
			return Revision.CURRENT;
		} else if (commitNumber > HistoryUtils.getHistoryManager().getSessionRevision()) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
		} else {
			return HistoryUtils.getRevision(commitNumber);
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link RevisionForCommit}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<RevisionForCommit> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public RevisionForCommit build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new RevisionForCommit(getConfig().getName(), self, args);
		}

	}

}

