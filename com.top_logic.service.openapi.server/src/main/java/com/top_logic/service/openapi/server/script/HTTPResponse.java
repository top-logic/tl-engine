/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.script;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;

/**
 * Creates an HTTP response object to deliver to an {@link HttpServletResponse}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HTTPResponse extends GenericMethod {

	/**
	 * Creates a new {@link HTTPResponse}.
	 */
	protected HTTPResponse(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new HTTPResponse(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		int sc = asInt(arguments[0]);
		Object result = arguments[1];
		String contentType = asString(arguments[2]);
		return new Response(sc, result, contentType);
	}

	/**
	 * {@link AbstractSimpleMethodBuilder} creating {@link HTTPResponse}.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<HTTPResponse> {

		/** Description of parameters for a {@link HTTPResponse}. */
		public static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("statusCode")
			.optional("content")
			.optional("contentType", JsonUtilities.JSON_CONTENT_TYPE_HEADER)
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public HTTPResponse build(Expr expr, SearchExpression[] args)
				throws ConfigurationException {
			return new HTTPResponse(getConfig().getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}

}

