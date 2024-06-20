/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.List;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.conf.Operation;

/**
 * Factory for {@link ServiceMethod}s for a given parameter list.
 * 
 * @see Operation#getImplementation()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ServiceMethodBuilder {

	/**
	 * Creates a {@link ServiceMethod} instance.
	 * 
	 * @param path
	 *        The path the created {@link ServiceMethod} will be registered for.
	 * @param parameters
	 *        Parameter names that describe the arguments that will be passed to invocations, see
	 *        {@link ServiceMethod#handleRequest(Person, java.util.Map, javax.servlet.http.HttpServletResponse)}.
	 */
	ServiceMethod build(String path, List<String> parameters);

}
