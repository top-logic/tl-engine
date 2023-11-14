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
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptorBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.service.openapi.client.registry.conf.MethodDefinition;
import com.top_logic.service.openapi.client.registry.conf.ParameterDefinition;
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

	private ArgumentDescriptor _descriptor;

	/**
	 * Creates a {@link ServiceMethodBuilder}.
	 */
	public ServiceMethodBuilder(MethodDefinition method, CallHandler handler) {
		_name = method.getName();
		_handler = handler;
		_descriptor = createDescriptor(method);
	}

	private ArgumentDescriptor createDescriptor(MethodDefinition method) {
		ArgumentDescriptorBuilder builder = ArgumentDescriptor.builder();
		for (ParameterDefinition param : method.getParameters()) {
			String name = param.getName();
			if (param.isRequired()) {
				builder.mandatory(name);
			} else {
				Expr defaultValue = param.getDefaultValue();
				if (defaultValue == null) {
					builder.optional(name);
				} else {
					builder.optional(name, () -> QueryExecutor.compileExpr(defaultValue));
				}
			}
		}
		return builder.build();
	}

	public void init() {
		_handler.init();
	}

	@Override
	public SearchExpression build(Expr expr, SearchExpression[] args)
			throws ConfigurationException {
		return new RPCMethod(_handler, _name, args);
	}

	@Override
	public Object create(Object type, Object[] children) {
		SearchExpression[] args = new SearchExpression[children.length];
		for (int n = 0; n < args.length; n++) {
			args[n] = (SearchExpression) children[n];
		}
		return new RPCMethod(_handler, _name, args);
	}

	@Override
	public Object getId() {
		return _name;
	}

	@Override
	public ArgumentDescriptor descriptor() {
		return _descriptor;
	}

}