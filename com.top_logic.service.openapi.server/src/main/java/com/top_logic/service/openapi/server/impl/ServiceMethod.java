/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.OpenApiServer;

/**
 * Implementation of an operation in the {@link OpenApiServer}.
 * 
 * @see ServiceMethodBuilder#build(String, java.util.List)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ServiceMethod {

	/**
	 * Executes the operation and sends the result back to the given {@link HttpServletResponse}.
	 * 
	 * @param account
	 *        The user in whose context the method must be executed. May be <code>null</code>. In
	 *        this case the operation is executed in system context.
	 * @param arguments
	 *        The arguments to this operation.
	 * @param resp
	 *        The response to send the results to.
	 */
	void handleRequest(Person account, Map<String, Object> arguments, HttpServletResponse resp) throws IOException, ComputationFailure;

}
