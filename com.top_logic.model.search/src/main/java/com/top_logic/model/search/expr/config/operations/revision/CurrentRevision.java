/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.revision;

import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLType;
import com.top_logic.model.core.TlCoreFactory;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;

/**
 * Generic method determining {@link Revision#CURRENT}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CurrentRevision extends GenericMethod {

	/**
	 * Creates a new {@link CurrentRevision}.
	 */
	protected CurrentRevision(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new CurrentRevision(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TlCoreFactory.getRevisionType();
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return Revision.CURRENT;
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link CurrentRevision}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<CurrentRevision> {

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public CurrentRevision build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			checkNoArguments(expr, args);
			return new CurrentRevision(getConfig().getName(), args);
		}

	}

}

