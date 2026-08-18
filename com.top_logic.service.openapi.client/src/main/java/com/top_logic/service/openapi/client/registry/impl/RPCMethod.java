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
import com.top_logic.model.search.expr.GenericMethodWithSecurity;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script function invoking an external API.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RPCMethod extends GenericMethodWithSecurity {

	private final CallHandler _handler;

	/**
	 * Creates a {@link RPCMethod}.
	 *
	 * @param usesSecurity
	 *        See {@link #usesSecurity()}.
	 */
	public RPCMethod(CallHandler handler, String name, SearchExpression[] arguments, boolean usesSecurity) {
		super(name, arguments, usesSecurity);

		_handler = handler;
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new RPCMethod(_handler, getName(), arguments, usesSecurity());
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	/**
	 * @implNote The call evaluates the configured expressions of the request, of computed values and
	 *           of the response processing. Those expressions apply the access rights of the current
	 *           user, which must not happen when the calling script was executed with definer's
	 *           rights: its author decided that the computation is not subject to the user's rights,
	 *           and an attribute read that silently delivers <code>null</code> would send a request
	 *           with missing values instead of failing. The whole call is therefore performed in an
	 *           {@link ModelAccessRights#uncheckedSecurity(com.top_logic.basic.util.ComputationEx2)
	 *           unchecked scope} in that case.
	 */
	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		try {
			if (usesSecurity()) {
				return _handler.execute(arguments);
			}
			return ModelAccessRights.<Object, Exception, RuntimeException> uncheckedSecurity(
				() -> _handler.execute(arguments));
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

	/**
	 * It might be, that the handler modifies global state.
	 */
	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	/**
	 * The call must be executed each time, not just at compile time.
	 */
	@Override
	public boolean canEvaluateAtCompileTime(Object[] arguments) {
		return false;
	}

}
