/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.http.adapter.HttpActionAdapter;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oidc.exceptions.OidcTokenException;

import com.top_logic.base.accesscontrol.ExternalLoginFailure;
import com.top_logic.base.accesscontrol.I18NConstants;
import com.top_logic.security.auth.pac4j.config.LoginFailureLogic;

/**
 * Tests {@link LoginFailureLogic}: a failed single-sign-on leads to the login retry page instead of
 * an internal error, and the page is told about the failure.
 */
@SuppressWarnings("javadoc")
public class TestLoginFailureLogic extends TestCase {

	private static final String ERROR_URL = "/app/login-retry";

	private final List<HttpAction> _adapted = new ArrayList<>();

	private final Map<String, Object> _sessionAttributes = new HashMap<>();

	private final HttpSession _session = (HttpSession) Proxy.newProxyInstance(
		TestLoginFailureLogic.class.getClassLoader(), new Class<?>[] { HttpSession.class },
		(proxy, method, args) -> switch (method.getName()) {
			case "getAttribute" -> _sessionAttributes.get(args[0]);
			case "setAttribute" -> _sessionAttributes.put((String) args[0], args[1]);
			case "removeAttribute" -> _sessionAttributes.remove(args[0]);
			default -> null;
		});

	private final HttpActionAdapter _adapter = (action, context) -> {
		_adapted.add(action);
		return null;
	};

	/**
	 * The identity provider refusing the client credentials of the application in the token
	 * request (here: a wrong client secret) sends the browser to the login retry page.
	 */
	public void testRefusedClientCredentials() {
		new TestableCallback().handle(
			new OidcTokenException("Bad token response, error=unauthorized_client"), _adapter, webContext());

		assertRedirectedToErrorUrl();
	}

	/** The login page is told about the failure, so that it can show the visitor a message. */
	public void testFailureReportedToLoginPage() {
		new TestableCallback().handle(
			new OidcTokenException("Bad token response, error=unauthorized_client"), _adapter, webContext());

		assertEquals(I18NConstants.ERROR_EXTERNAL_LOGIN_FAILED, ExternalLoginFailure.take(_session));
		assertNull("Failure reported twice.", ExternalLoginFailure.take(_session));
	}

	/** A failure when the login starts sends the browser to the login retry page as well. */
	public void testFailureAtLoginStart() {
		new TestableSecurity().handle(new OidcTokenException("Provider not reachable."), _adapter, webContext());

		assertRedirectedToErrorUrl();
	}

	/** An answer pac4j sends deliberately (e.g. the redirect to the provider) is kept. */
	public void testHttpActionKept() {
		FoundAction toProvider = new FoundAction("https://provider.test/auth");
		new TestableCallback().handle(toProvider, _adapter, webContext());

		assertEquals(List.of(toProvider), _adapted);
		assertNull("Redirect reported as failure.", ExternalLoginFailure.take(_session));
	}

	private void assertRedirectedToErrorUrl() {
		assertEquals(1, _adapted.size());
		HttpAction action = _adapted.get(0);
		assertTrue(action instanceof FoundAction);
		assertEquals(ERROR_URL, ((FoundAction) action).getLocation());
	}

	private WebContext webContext() {
		HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
			TestLoginFailureLogic.class.getClassLoader(), new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getMethod" -> "GET";
				case "getSession" -> _session;
				default -> null;
			});
		HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
			TestLoginFailureLogic.class.getClassLoader(), new Class<?>[] { HttpServletResponse.class },
			(proxy, method, args) -> null);
		return new JEEContext(request, response);
	}

	private static class TestableCallback extends LoginFailureLogic.Callback {

		TestableCallback() {
			super(() -> ERROR_URL);
		}

		Object handle(Exception ex, HttpActionAdapter adapter, WebContext context) {
			return handleException(ex, adapter, context);
		}

	}

	private static class TestableSecurity extends LoginFailureLogic.Security {

		TestableSecurity() {
			super(() -> ERROR_URL);
		}

		Object handle(Exception ex, HttpActionAdapter adapter, WebContext context) {
			return handleException(ex, adapter, context);
		}

	}

}
