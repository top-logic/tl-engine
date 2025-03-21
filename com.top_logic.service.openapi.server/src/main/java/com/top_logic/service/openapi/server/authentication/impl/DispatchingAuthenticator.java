/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;

/**
 * {@link Authenticator} trying multiple {@link Authenticator}s in order and authenticates the
 * request, if one of them is able to authenticate.
 */
public class DispatchingAuthenticator implements Authenticator {

	private final Authenticator _first;

	private final Authenticator _second;

	/**
	 * Creates a new {@link DispatchingAuthenticator}.
	 */
	public DispatchingAuthenticator(Authenticator first, Authenticator second) {
		_first = first;
		_second = second;
	}

	@Override
	public Person authenticate(HttpServletRequest req, HttpServletResponse resp)
			throws AuthenticationFailure, IOException {
		try {
			return _first.authenticate(req, resp);
		} catch (AuthenticationFailure ex1) {
			try {
				return _second.authenticate(req, resp);
			} catch (AuthenticationFailure ex2) {
				// Both failed, report the problem.
				List<Object> problems = new ArrayList<>();
				addProblems(problems, ex1.getErrorKey());
				addProblems(problems, ex2.getErrorKey());

				throw new AuthenticationFailure(
					I18NConstants.ERROR_AUTH_FAILED__REASONS.fill(problems));
			}
		}
	}

	private static void addProblems(List<Object> problems, ResKey error) {
		if (error.plain() == I18NConstants.ERROR_AUTH_FAILED__REASONS) {
			problems.addAll((Collection<?>) error.arguments()[0]);
		} else {
			problems.add(error);
		}
	}

}

