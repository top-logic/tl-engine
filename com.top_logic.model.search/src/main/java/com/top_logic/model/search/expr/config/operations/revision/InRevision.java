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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.string.Translate;
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
	protected InRevision(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new InRevision(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return argumentTypes.get(0);
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
	protected Object eval(Object[] arguments, EvalContext definitions) {
		Object arg = asSingleElement(arguments[1]);
		if (arg == null) {
			throw new TopLogicException(I18NConstants.ERROR_REVISION_ARGUMENT_NULL__EXPR.fill(this));
		} else if (arg instanceof Revision) {
			Revision revision = (Revision) arg;
			return evalPotentialFlatMap(definitions, arguments[0], revision);
		} else {
			long commitNumber = asLong(arg);

			if (commitNumber < 0) {
				throw new TopLogicException(
					I18NConstants.ERROR_NEGATIVE_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
			}
			if (commitNumber == Revision.INITIAL.getCommitNumber()) {
				return evalPotentialFlatMap(definitions, arguments[0], Revision.INITIAL);
			}
			if (commitNumber == Revision.CURRENT.getCommitNumber()) {
				return evalPotentialFlatMap(definitions, arguments[0], Revision.CURRENT);
			}
			if (commitNumber > HistoryUtils.getHistoryManager().getSessionRevision()) {
				throw new TopLogicException(
					I18NConstants.ERROR_UNKNOWN_COMMIT_NR__EXPR_COMMIT.fill(this, commitNumber));
			}
			return evalPotentialFlatMap(definitions, arguments[0], HistoryUtils.getRevision(commitNumber));
		}
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link InRevision}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<InRevision> {

		/** Description of parameters for a {@link Translate}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("item")
			.mandatory("revision")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public InRevision build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new InRevision(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}

