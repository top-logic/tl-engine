/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.I18NConstants;
import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.base.accesscontrol.LoginCredentials;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.util.Resources;

/**
 * Test for the identity verification branch of {@link ExternalAuthenticationServlet}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExternalAuthenticationServlet extends BasicTestCase {

	/**
	 * {@link ExternalAuthenticationServlet} that opens the hooks under test and authenticates
	 * no one.
	 */
	public static class TestingServlet extends ExternalAuthenticationServlet {

		@Override
		public String getVerificationToken(HttpServletRequest request) {
			return super.getVerificationToken(request);
		}

		@Override
		public void writeVerificationResult(HttpServletResponse response, Outcome outcome)
				throws IOException {
			super.writeVerificationResult(response, outcome);
		}

		@Override
		protected LoginCredentials retrieveLoginCredentials(HttpServletRequest request,
				HttpServletResponse response) {
			throw new UnsupportedOperationException("No external authentication in this test.");
		}

	}

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		_client = runner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;

		super.tearDown();
	}

	/**
	 * A request without the parameter is an ordinary login, one with it names a verification.
	 */
	public void testVerificationToken() throws Exception {
		TestingServlet servlet = new TestingServlet();

		assertNull("Without the parameter the request is an ordinary login.",
			servlet.getVerificationToken(request("http://ignore/lpServlet")));
		assertNull("An empty parameter names no verification.",
			servlet.getVerificationToken(
				request("http://ignore/lpServlet?" + ExternalAuthenticationServlet.VERIFICATION_PARAM + "=")));
		assertEquals("abc-123", servlet.getVerificationToken(
			request("http://ignore/lpServlet?" + ExternalAuthenticationServlet.VERIFICATION_PARAM + "=abc-123")));
	}

	/**
	 * Every outcome produces a page that says what happened, and only the confirmed one tries to
	 * close the window.
	 */
	public void testResultPage() throws Exception {
		assertPage(Outcome.VERIFIED, true);
		assertPage(Outcome.MISMATCH, false);
		assertPage(Outcome.UNKNOWN, false);
	}

	private void assertPage(Outcome outcome, boolean closesWindow) throws Exception {
		InvocationContext invocation = _client.newInvocation(new GetMethodWebRequest("http://ignore/lpServlet"));
		new TestingServlet().writeVerificationResult(invocation.getResponse(), outcome);
		String page = invocation.getServletResponse().getText();

		assertContains("<!DOCTYPE html>", page);
		assertContains(Resources.getInstance().getString(I18NConstants.IDENTITY_VERIFICATION_TITLE), page);
		assertContains(expectedMessage(outcome), page);
		assertEquals("Only a confirmed identity may close the window.", closesWindow,
			page.contains("window.close()"));
	}

	private String expectedMessage(Outcome outcome) {
		Resources resources = Resources.getInstance();
		switch (outcome) {
			case VERIFIED:
				return resources.getString(I18NConstants.IDENTITY_VERIFIED);
			case MISMATCH:
				return resources.getString(I18NConstants.ERROR_IDENTITY_MISMATCH);
			default:
				return resources.getString(I18NConstants.ERROR_IDENTITY_VERIFICATION_UNKNOWN);
		}
	}

	private HttpServletRequest request(String url) throws Exception {
		WebRequest webRequest = new GetMethodWebRequest(url);
		return _client.newInvocation(webRequest).getRequest();
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		return PersonManagerSetup
			.createPersonManagerSetup(new TestSuite(TestExternalAuthenticationServlet.class));
	}

}
