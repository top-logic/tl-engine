/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.treexf.TreeMaterializer.Factory;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractMethodBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.service.openapi.client.registry.impl.CallHandler;
import com.top_logic.service.openapi.client.registry.impl.RPCMethod;

/**
 * {@link MethodBuilder} creating {@link SearchExpression} implementations that call external APIs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class ServiceMethodBuilder implements MethodBuilder<SearchExpression>, Factory {
	private final String _name;

	private final CallHandler _handler;

	private int _minArgs;

	/** 
	 * Creates a {@link ServiceMethodBuilder}.
	 */
	public ServiceMethodBuilder(String name, int minArgs, CallHandler handler) {
		_name = name;
		_minArgs = minArgs;
		_handler = handler;
	}

	public void init() {
		_handler.init();
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression self, SearchExpression[] args)
			throws ConfigurationException {
		AbstractMethodBuilder.checkMinArgs(expr, args, _minArgs);

		return new RPCMethod(_handler, _name, self, args);
	}

	@Override
	public Object create(Object type, Object[] children) {
		SearchExpression[] args = new SearchExpression[children.length];
		for (int n = 0; n < args.length; n++) {
			args[n] = (SearchExpression) children[n];
		}
		return new RPCMethod(_handler, _name, null, args);
	}

	@Override
	public boolean hasSelf() {
		return false;
	}

	@Override
	public Object getId() {
		return _name;
	}

}