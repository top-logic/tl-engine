/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.impl.ComputationFailure;
import com.top_logic.service.openapi.server.impl.ServiceMethod;
import com.top_logic.service.openapi.server.parameter.ConcreteRequestParameter;
import com.top_logic.service.openapi.server.parameter.InvalidValueException;

/**
 * Algorithm to actually handle a request.
 * 
 * @see #handleRequest(HttpServletRequest, HttpServletResponse, Map)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class PathHandler {

	private String _path;

	private List<ConcreteRequestParameter<?>> _parameterParsers;

	private ServiceMethod _implementation;

	private Authenticator _authenticator;

	/**
	 * Creates a {@link PathHandler}.
	 */
	public PathHandler(String path, List<ConcreteRequestParameter<?>> parameterParsers, ServiceMethod implementation,
			Authenticator authenticator) {
		_path = path;
		_parameterParsers = parameterParsers;
		_implementation = implementation;
		_authenticator = authenticator;
	}

	/**
	 * The path to which this {@link PathHandler} is bound.
	 */
	public String getPath() {
		return _path;
	}

	/**
	 * Actually performs the actions associated with the request.
	 */
	public void handleRequest(HttpServletRequest req, HttpServletResponse resp, Map<String, String> rawPathParams)
			throws IOException, InvalidValueException, AuthenticationFailure, ComputationFailure {
		Person localAccount = _authenticator.authenticate(req, resp);
		HashMap<String, Object> parameters = new HashMap<>();
		for (ConcreteRequestParameter<?> parser : _parameterParsers) {
			parser.parse(parameters, req, rawPathParams);
		}

		_implementation.handleRequest(localAccount, parameters, resp);
	}

}
