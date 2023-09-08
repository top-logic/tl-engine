/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Generic method transforming a given {@link TLObject} to a different revision.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InRevision extends GenericMethod implements WithFlatMapSemantics<Revision> {

	/**
	 * Creates a new {@link InRevision}.
	 */
	protected InRevision(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new InRevision(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object singletonValue, Revision revision) {
		TLObject tlObject = asTLObject(singletonValue);
		if (tlObject == null) {
			return null;
		}
		if (revision == Revision.INITIAL) {
			return null;
		}
		if (revision == Revision.CURRENT) {
			return WrapperHistoryUtils.getCurrent(tlObject);
		}
		return WrapperHistoryUtils.getWrapper(revision, tlObject);
	}

	@Override
	protected Object eval(Object self, Object[] arguments, EvalContext definitions) {
		Object arg = asSingleElement(arguments[0]);
		if (arg == null) {
			throw new TopLogicException(I18NConstants.ERROR_REVISION_ARGUMENT_NULL__EXPR.fill(this));
		} else if (arg instanceof Revision) {
			Revision revision = (Revision) arg;
			return evalPotentialFlatMap(definitions, self, revision);
		} else {
			long commitNumber = asLong(arg);

			if (commitNumber < 0) {
				throw new TopLogicException(
					I18NConstants.ERROR_NEGATIVE_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
			}
			if (commitNumber == Revision.INITIAL.getCommitNumber()) {
				return evalPotentialFlatMap(definitions, self, Revision.INITIAL);
			}
			if (commitNumber == Revision.CURRENT.getCommitNumber()) {
				return evalPotentialFlatMap(definitions, self, Revision.CURRENT);
			}
			if (commitNumber > HistoryUtils.getHistoryManager().getSessionRevision()) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
			}
			return evalPotentialFlatMap(definitions, self, HistoryUtils.getRevision(commitNumber));
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link InRevision}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<InRevision> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public InRevision build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			checkSingleArg(expr, args);
			return new InRevision(getConfig().getName(), self, args);
		}

	}

}

