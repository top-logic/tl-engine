/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl;

import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script function invoking an external API.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RPCMethod extends GenericMethod {

	private final CallHandler _handler;

	/**
	 * Creates a {@link RPCMethod}.
	 */
	public RPCMethod(CallHandler handler, String name, SearchExpression[] arguments) {
		super(name, arguments);

		_handler = handler;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RPCMethod(_handler, getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		try {
			return _handler.execute(arguments);
		} catch (I18NRuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_REQUEST_FAILED__METHOD_ARGS_MESSAGE.fill(getName(), Arrays.asList(arguments),
					ex.getMessage()),
				ex);
		}
	}

	@Override
	public Object getId() {
		return getName();
	}

}
