/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.login;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;

import jakarta.servlet.http.HttpServletRequest;

import org.pac4j.oidc.profile.OidcProfile;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.UserTokens;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.security.auth.pac4j.login.Pac4jLoginMethod;
import com.top_logic.security.auth.pac4j.servlet.Pac4jAuthenticationServlet;
import com.top_logic.security.auth.pac4j.servlet.Pac4jUserTokens;

/**
 * Tests which sessions {@link Pac4jLoginMethod} offers to have authenticated again.
 *
 * <p>
 * A login method answers a re-authentication URL only for a session it established itself, so that
 * the browser is never sent to an identity provider the session never authenticated against. The
 * URL a session of its own is answered with needs the running pac4j configuration and is therefore
 * left to the application.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPac4jLoginMethod extends TestCase {

	/** The name of the pac4j client of the login method under test. */
	private static final String CLIENT = "test-oidc";

	/** The token of the verification a re-authentication would confirm. */
	private static final String TOKEN = "abc-123";

	/** The servlet name the requests of this test are answered by. */
	private static final String SERVLET_NAME = "openid";

	/**
	 * {@link UserTokens} of a session established by some other authentication.
	 */
	private static class ForeignTokens implements UserTokens {

		@Override
		public String getAccessToken() {
			return null;
		}

		@Override
		public Date getExpiration() {
			return null;
		}

		@Override
		public boolean isExpired() {
			return false;
		}

		@Override
		public String getIdToken() {
			return null;
		}

		@Override
		public boolean refreshTokens(DisplayContext displayContext) {
			return false;
		}

		@Override
		public String getCSFRToken() {
			return null;
		}

	}

	private ExternalAuthenticationServlet _servlet;

	private HttpServletRequest _request;

	private DisplayContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet(SERVLET_NAME, Pac4jAuthenticationServlet.class.getName());
		ServletUnitClient client = runner.newClient();
		InvocationContext invocation = client.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET_NAME));

		_servlet = (ExternalAuthenticationServlet) invocation.getServlet();
		_request = invocation.getRequest();
		_context = new DefaultDisplayContext(null, _request, invocation.getResponse());
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_request = null;
		_servlet = null;

		super.tearDown();
	}

	/**
	 * Tests that a login hands the browser to this method's client, naming it in the parameter the
	 * pac4j security filter selects a client by, and appends the page to return to.
	 */
	public void testInitiationUrl() {
		DefaultDisplayContext.setupDisplayContext(_request, _context);
		try {
			String servletUrl = _request.getContextPath() + "/servlet/openid";

			assertEquals(servletUrl + "?force_client=" + CLIENT, loginMethod().getInitiationUrl(null));
			assertEquals(servletUrl + "?force_client=" + CLIENT + "&startPage=%2Fview%2Fw1%2F",
				loginMethod().getInitiationUrl("/view/w1/"));
		} finally {
			DefaultDisplayContext.teardownDisplayContext(_request);
		}
	}

	/**
	 * Tests that a request without a session - and therefore without any authentication to repeat -
	 * is offered nothing.
	 */
	public void testNoSession() {
		assertNull(loginMethod().getReauthenticationUrl(TOKEN));
	}

	/**
	 * Tests that a session established by another authentication mechanism is offered nothing.
	 */
	public void testForeignAuthentication() {
		ThreadContextManager.inSystemInteraction(TestPac4jLoginMethod.class, () -> {
			_servlet.installUserTokens(new ForeignTokens());

			assertNull("A session this login method did not establish is not confirmed by its provider.",
				loginMethod().getReauthenticationUrl(TOKEN));
		});
	}

	/**
	 * Tests that a session established by another pac4j client is offered nothing.
	 */
	public void testOtherClient() {
		ThreadContextManager.inSystemInteraction(TestPac4jLoginMethod.class, () -> {
			_servlet.installUserTokens(new Pac4jUserTokens(_context, profile("other-oidc")));

			assertNull("A session logged in with another client is not confirmed by this one's provider.",
				loginMethod().getReauthenticationUrl(TOKEN));
		});
	}

	private static Pac4jLoginMethod loginMethod() {
		return new Pac4jLoginMethod(CLIENT, ResKey.text(CLIENT), null);
	}

	private static OidcProfile profile(String clientName) {
		OidcProfile result = new OidcProfile();
		result.setClientName(clientName);
		return result;
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestPac4jLoginMethod.class, ApplicationConfig.Module.INSTANCE));
	}

}
