/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl;

/**
 * Implementation of a {@link RPCMethod}.
 * 
 * @see RPCMethod#RPCMethod(CallHandler, String, com.top_logic.model.search.expr.SearchExpression[])
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CallHandler {

	/**
	 * Performs the call operation.
	 */
	Object execute(Object[] arguments) throws Exception;

	/**
	 * Initializes this {@link CallHandler}.
	 */
	void init();

}
