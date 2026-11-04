/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.agent;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletResponse;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.json.JSON;
import com.top_logic.layout.view.agent.AgentAccess;
import com.top_logic.layout.view.agent.AgentAuthenticator;
import com.top_logic.layout.view.agent.AgentCredential;
import com.top_logic.layout.view.agent.AgentServlet;
import com.top_logic.util.CachePolicy;

/**
 * Test that {@link AgentServlet} answers every request it admits to no session with
 * {@link HttpServletResponse#SC_UNAUTHORIZED} and a JSON error object, never with a redirect.
 */
public class TestAgentServletUnauthorized extends BasicTestCase {

	/**
	 * The one credential {@link EndedSessionAuthenticator} recognizes.
	 */
	static final String ENDED_SESSION_CREDENTIAL = "tlagt_ended_session";

	/**
	 * The session key of the credential {@link EndedSessionAuthenticator} recognizes, bound to no
	 * session in {@link AgentAccess}.
	 */
	static final String ENDED_SESSION_KEY = "ended-session";

	/**
	 * {@link AgentAuthenticator} recognizing {@link #ENDED_SESSION_CREDENTIAL} as the credential of
	 * a session that has ended.
	 */
	public static class EndedSessionAuthenticator extends AbstractConfiguredInstance<EndedSessionAuthenticator.Config>
			implements AgentAuthenticator {

		/**
		 * Configuration of {@link EndedSessionAuthenticator}.
		 */
		public interface Config extends AgentAuthenticator.Config<EndedSessionAuthenticator> {
			// No properties.
		}

		/**
		 * Creates a {@link EndedSessionAuthenticator} from configuration.
		 */
		@CalledByReflection
		public EndedSessionAuthenticator(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public AgentCredential authenticate(String credential) {
			if (ENDED_SESSION_CREDENTIAL.equals(credential)) {
				return new AgentCredential(null, true, ENDED_SESSION_KEY);
			}
			return null;
		}

	}

	private static final String SERVLET_NAME = "agent-api";

	private static final String BASE_URL = "http://localhost/" + SERVLET_NAME;

	private static final String PATH_WINDOWS = "/windows";

	private static final String PATH_ACT = "/act";

	private static final String FIELD_ERROR = "error";

	private static final String LOCATION_HEADER = "Location";

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet(SERVLET_NAME + "/*", AgentServlet.class.getName());
		_client = runner.newClient();
		_client.getClientProperties().setAutoRedirect(false);
		_client.setExceptionsThrownOnErrorStatus(false);
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;

		super.tearDown();
	}

	/**
	 * A request without a credential is unauthorized.
	 */
	public void testNoCredential() throws Exception {
		assertUnauthorized(get(PATH_WINDOWS));
		assertUnauthorized(post(PATH_ACT));
	}

	/**
	 * A request with a credential no authenticator recognizes is unauthorized.
	 */
	public void testForgedCredential() throws Exception {
		assertUnauthorized(withCredential(get(PATH_WINDOWS), "tlagt_x_forged"));
		assertUnauthorized(withCredential(post(PATH_ACT), "tlagt_x_forged"));
	}

	/**
	 * A request with the credential of an ended session is unauthorized.
	 */
	public void testEndedSessionCredential() throws Exception {
		WebRequest probe = withCredential(get(PATH_WINDOWS), ENDED_SESSION_CREDENTIAL);
		AgentCredential credential =
			AgentAccess.getInstance().authenticate(_client.newInvocation(probe).getRequest());
		assertNotNull("The test credential must be recognized.", credential);
		assertNull("The session of the test credential must have ended.",
			AgentAccess.getInstance().session(credential.sessionKey()));

		assertUnauthorized(withCredential(get(PATH_WINDOWS), ENDED_SESSION_CREDENTIAL));
		assertUnauthorized(withCredential(post(PATH_ACT), ENDED_SESSION_CREDENTIAL));
	}

	private static WebRequest get(String path) {
		return new GetMethodWebRequest(BASE_URL + path);
	}

	private static WebRequest post(String path) {
		return new PostMethodWebRequest(BASE_URL + path);
	}

	private static WebRequest withCredential(WebRequest request, String credential) {
		request.setHeaderField(AgentAccess.AUTHORIZATION_HEADER, AgentAccess.BEARER_PREFIX + credential);
		return request;
	}

	private void assertUnauthorized(WebRequest request) throws Exception {
		WebResponse response = _client.getResponse(request);
		String url = request.getURL().toString();

		assertEquals("Status of " + url, HttpServletResponse.SC_UNAUTHORIZED, response.getResponseCode());
		assertNull("Redirect of " + url, response.getHeaderField(LOCATION_HEADER));

		Object content = JSON.fromString(response.getText());
		assertTrue("JSON object expected from " + url + ": " + content, content instanceof Map);
		assertNotNull("Error message expected from " + url + ": " + content,
			((Map<?, ?>) content).get(FIELD_ERROR));
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestAgentServletUnauthorized.class);
		innerTest = ServiceTestSetup.createSetup(innerTest, SessionService.Module.INSTANCE,
			CachePolicy.Module.INSTANCE, AgentAccess.Module.INSTANCE);
		String customFile = CustomPropertiesDecorator.createFileName(TestAgentServletUnauthorized.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}
