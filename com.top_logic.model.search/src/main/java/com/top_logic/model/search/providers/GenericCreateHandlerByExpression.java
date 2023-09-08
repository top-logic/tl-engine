/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.create.GenericCreateHandler;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link GenericCreateHandler} that allows linking newly created objects to their context using a
 * TL-Script operation.
 * 
 * @see Config#getLinkOperation()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericCreateHandlerByExpression extends GenericCreateHandler {

	/**
	 * Configuration options for {@link GenericCreateHandlerByExpression}.
	 */
	public interface Config extends GenericCreateHandler.Config {

		/**
		 * @see #getLinkOperation()
		 */
		String LINK_OPERATION = "linkOperation";

		/**
		 * Optional operation linking the newly created object to its create context.
		 * 
		 * <p>
		 * A function with three arguments is expected. The first argument is the container of the
		 * new object, the second argument is the newly created object itself and the third argument
		 * is the creation context. The creation context is the model on which this create handler
		 * is executed. Normally, this is the model of the creation component on which this
		 * component where this handler is registered. In special cases, the creation context can be
		 * customised by Setting {@link #getTarget()} to indicate where the creation context should
		 * be obtained from.
		 * </p>
		 */
		@Name(LINK_OPERATION)
		Expr getLinkOperation();
	}

	private QueryExecutor _linkOperation;

	/**
	 * Creates a {@link GenericCreateHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GenericCreateHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_linkOperation = QueryExecutor.compileOptional(config.getLinkOperation());
	}

	@Override
	protected void linkNewObject(TLObject container, TLObject newObject, Object model) {
		if (_linkOperation != null) {
			_linkOperation.execute(container, newObject, model);
		}
	}

}
