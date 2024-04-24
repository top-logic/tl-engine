/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.app;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.services.ServletContextService;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.KBSchemaUtil;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Function retrieving the context path of the running application.
 */
public class GetDBConfig extends GenericMethod {

	/**
	 * Creates a {@link GetDBConfig}.
	 */
	protected GetDBConfig(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new GetDBConfig(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ConnectionPool pool = KBUtils.getConnectionPool(kb);
		PooledConnection con = pool.borrowReadConnection();
		try {
			return KBSchemaUtil.loadSchema(con, kb.getName());
		} finally {
			pool.releaseReadConnection(con);
		}
	}

	/**
	 * It may be that the {@link ServletContextService} is not yet started.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

	/**
	 * {@link MethodBuilder} creating {@link GetDBConfig}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<GetDBConfig> {
		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public GetDBConfig build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, args);
			return new GetDBConfig(getConfig().getName(), args);
		}

	}

}
