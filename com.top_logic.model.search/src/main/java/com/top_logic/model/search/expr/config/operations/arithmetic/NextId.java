/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.arithmetic;

import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.element.structured.util.SequenceIdGenerator;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.db2.RowLevelLockingSequenceManager;
import com.top_logic.knowledge.service.db2.SequenceManager;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Operation generating a new sequence number for a given sequence in transaction context.
 * 
 * <p>
 * The function expects the sequence name as single argument.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NextId extends GenericMethod {

	private static final SequenceManager SEQUENCE_MANAGER = new RowLevelLockingSequenceManager();

	/**
	 * Creates a {@link NextId}.
	 */
	protected NextId(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new NextId(getName(), self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		return selfType;
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		StringBuilder result = new StringBuilder("nextId");
		for (Object arg : arguments) {
			SequenceIdGenerator.addNames(result, arg);
		}
		String sequenceName = result.toString();

		CommitContext currentContext = KBUtils.getCurrentContext(definitions.getKnowledgeBase());
		if (currentContext == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_TRANSACTION__EXPR.fill(this));
		}

		PooledConnection connection = currentContext.getConnection();
		try {
			return toNumber(
				SEQUENCE_MANAGER.nextSequenceNumber(connection.getSQLDialect(), connection, 3, sequenceName));
		} catch (SQLException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_FAILED_TO_GENERATE_ID__EXPR_MSG.fill(this, ex.getMessage()), ex);
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link NextId}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<NextId> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public NextId build(Expr expr, SearchExpression self, SearchExpression[] args)
				throws ConfigurationException {
			return new NextId(getConfig().getName(), self, args);
		}

	}

}
