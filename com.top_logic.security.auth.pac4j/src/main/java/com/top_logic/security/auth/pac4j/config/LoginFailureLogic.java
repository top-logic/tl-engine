/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.auth.pac4j.config;

import java.util.function.Supplier;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.AbstractExceptionAwareLogic;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.Logger;

/**
 * Logics of pac4j answering a failed single-sign-on with the page for a new login attempt instead of
 * an internal error.
 *
 * <p>
 * A login with an external identity provider fails e.g. when the provider refuses the client
 * credentials of the application (<code>unauthorized_client</code>), is not reachable, or answers
 * the authentication with an error. pac4j reports such failures as exceptions; the logics log them
 * as warning and send the browser to the {@link ApplicationPages.Config#getLoginRetrySSOPage()
 * login retry page}, using the error URL of {@link AbstractExceptionAwareLogic}.
 * </p>
 */
public class LoginFailureLogic {

	/**
	 * {@link DefaultCallbackLogic} processing the answer of the identity provider.
	 */
	public static class Callback extends DefaultCallbackLogic {

		private final Supplier<String> _errorUrl;

		/**
		 * Creates a {@link Callback} logic.
		 *
		 * @param errorUrl
		 *        The URL the browser is sent to when the login fails, asked for at the failure.
		 */
		public Callback(Supplier<String> errorUrl) {
			_errorUrl = errorUrl;
		}

		@Override
		protected Object handleException(Exception ex, HttpActionAdapter adapter, WebContext context) {
			if (logFailure(ex)) {
				setErrorUrl(_errorUrl.get());
			}
			return super.handleException(ex, adapter, context);
		}

	}

	/**
	 * {@link DefaultSecurityLogic} starting a login with the identity provider.
	 */
	public static class Security extends DefaultSecurityLogic {

		private final Supplier<String> _errorUrl;

		/**
		 * Creates a {@link Security} logic.
		 *
		 * @param errorUrl
		 *        The URL the browser is sent to when the login fails, asked for at the failure.
		 */
		public Security(Supplier<String> errorUrl) {
			_errorUrl = errorUrl;
		}

		@Override
		protected Object handleException(Exception ex, HttpActionAdapter adapter, WebContext context) {
			if (logFailure(ex)) {
				setErrorUrl(_errorUrl.get());
			}
			return super.handleException(ex, adapter, context);
		}

	}

	/**
	 * The URL of the login retry page in the application with the given context path.
	 */
	public static String loginRetryUrl(String contextPath) {
		return contextPath + ApplicationPages.getInstance().getLoginRetrySSOPage();
	}

	/**
	 * Logs the given exception of a login, if it is a failure.
	 *
	 * @return Whether the exception is a failure, not an answer pac4j sends deliberately (e.g. the
	 *         redirect to the identity provider).
	 */
	static boolean logFailure(Exception ex) {
		if (ex instanceof HttpAction) {
			return false;
		}
		Logger.warn("Single sign-on failed: " + ex.getMessage(), ex, LoginFailureLogic.class);
		return true;
	}

}
