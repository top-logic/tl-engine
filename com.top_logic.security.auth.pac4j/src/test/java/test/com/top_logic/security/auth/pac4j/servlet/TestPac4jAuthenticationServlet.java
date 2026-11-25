/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.servlet;

import java.util.Optional;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.profile.definition.CommonProfileDefinition;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.session.JEESessionStore;
import org.pac4j.oidc.profile.OidcProfile;
import org.pac4j.oidc.profile.OidcProfileDefinition;

import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.SimpleDecoratedTestSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.basic.module.ServletContextServiceDecorator;
import test.com.top_logic.knowledge.monitor.UserSessionCleanup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.base.accesscontrol.ExternalAuthenticationServlet;
import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalUserMapping;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.security.auth.pac4j.config.Pac4jConfigFactory;
import com.top_logic.security.auth.pac4j.servlet.Pac4jAuthenticationServlet;

/**
 * Tests the {@link ExternalIdentity} that {@link Pac4jAuthenticationServlet} hands to the
 * {@link ExternalUserMapping} of a pac4j login, and the pac4j profile a login leaves in the session.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPac4jAuthenticationServlet extends BasicTestCase {

	/** The name of the pac4j client that has authenticated the user. */
	private static final String CLIENT = "test-oidc";

	/** The OpenID Connect subject of the user. */
	private static final String SUBJECT = "3f2b8c1e-7a44-4d0e-9a51-0c6d2e8f1b97";

	/** The login name extracted from the profile. */
	private static final String NAME = "alice";

	/** The e-mail address the identity provider reports. */
	private static final String EMAIL = "alice@example.com";

	/**
	 * The configured client of the logins through the servlet, see
	 * <code>TestPac4jAuthenticationServlet.properties.config.xml</code>.
	 */
	private static final String LOGIN_CLIENT = "test-anonymous";

	/** The servlet name the logins of this test are answered by. */
	private static final String SERVLET_NAME = "openid";

	/** Name of an account that does not exist in this application. */
	private static final String UNKNOWN_ACCOUNT = "no-such-pac4j-account";

	private ServletUnitClient _client;

	private UserSessionCleanup _userSessions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_userSessions = UserSessionCleanup.snapshot(PersistencyLayer.getKnowledgeBase());

		ServletRunner runner = new ServletRunner();
		runner.registerServlet(SERVLET_NAME, Pac4jAuthenticationServlet.class.getName());
		_client = runner.newClient();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;
		// The logins of this test are recorded as user sessions, which must not be seen by later
		// tests that share the knowledge base.
		_userSessions.deleteCreated();

		super.tearDown();
	}

	/**
	 * A pac4j OpenID Connect login yields the client, the subject, the extracted name and the
	 * claims of the identity provider.
	 */
	public void testIdentityOfOidcLogin() {
		ExternalIdentity identity = Pac4jAuthenticationServlet.createIdentity(oidcProfile(), NAME);

		assertEquals(CLIENT, identity.getClientName());
		assertEquals(SUBJECT, identity.getSubject());
		assertEquals(NAME, identity.getName());
		assertEquals(EMAIL, identity.getClaim(CommonProfileDefinition.EMAIL));
		assertEquals(Boolean.TRUE, identity.getClaim(OidcProfileDefinition.EMAIL_VERIFIED));
	}

	/**
	 * A fresh authentication through the re-authentication client of a client yields the identity
	 * of the client it is derived from.
	 */
	public void testIdentityOfReauthentication() {
		OidcProfile profile = oidcProfile();
		profile.setClientName(Pac4jConfigFactory.getReauthenticationName(CLIENT));

		assertEquals(CLIENT, Pac4jAuthenticationServlet.createIdentity(profile, NAME).getClientName());
		VerifiedIdentity verified = Pac4jAuthenticationServlet.createVerifiedIdentity(profile, () -> null);
		assertEquals(CLIENT, verified.getClientName());
		assertEquals(SUBJECT, verified.getSubject());
	}

	/**
	 * The tokens that pac4j keeps among the attributes of the profile are credentials, not claims
	 * about the user, and do not reach the mapping.
	 */
	public void testNoTokensInClaims() {
		ExternalIdentity identity = Pac4jAuthenticationServlet.createIdentity(oidcProfile(), NAME);

		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.ACCESS_TOKEN));
		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.ID_TOKEN));
		assertFalse(identity.getClaims().containsKey(OidcProfileDefinition.REFRESH_TOKEN));
	}

	/**
	 * A login rejected because the application has no account for the identity discards the stored
	 * profile, so that the next login reaches the identity provider again, and keeps the HTTP
	 * session.
	 */
	public void testUnknownAccountDiscardsProfile() throws Exception {
		assertNull("Test requires that no such account exists.", Person.byName(UNKNOWN_ACCOUNT));
		InvocationContext invocation = invocation();
		HttpServletRequest request = invocation.getRequest();
		HttpServletResponse response = invocation.getResponse();
		storeProfile(request, response, UNKNOWN_ACCOUNT);
		HttpSession session = request.getSession(false);

		doPost(invocation);

		assertEquals("An unknown account is redirected to the page explaining that.",
			ApplicationPages.getInstance().getUnknownAccountPage() + "?"
				+ ExternalAuthenticationServlet.LOGIN_NAME_PARAM + "=" + UNKNOWN_ACCOUNT,
			invocation.getServletResponse().getHeaderField("Location"));
		assertFalse("Profile of the rejected login still stored.", storedProfile(request, response).isPresent());
		assertSame("Session must be kept.", session, request.getSession(false));
		try {
			session.getCreationTime();
		} catch (IllegalStateException ex) {
			fail("Session must stay valid.");
		}
	}

	/**
	 * A successful login keeps the stored profile, which carries the tokens of the login.
	 */
	public void testSuccessfulLoginKeepsProfile() throws Exception {
		SessionService service = SessionService.getInstance();
		String accountName = "pac4jSuccessfulUser";
		Person user = createPerson(accountName);
		try {
			InvocationContext invocation = invocation();
			HttpServletRequest request = invocation.getRequest();
			HttpServletResponse response = invocation.getResponse();
			storeProfile(request, response, accountName);

			doPost(invocation);

			HttpSession session = request.getSession(false);
			assertNotNull("Login must establish a session.", session);
			assertEquals("Login did not succeed.", user, service.getUser(session.getId()));
			Optional<UserProfile> profile = storedProfile(request, response);
			assertTrue("Profile of a successful login must be kept.", profile.isPresent());
			assertEquals(accountName, profile.get().getId());

			service.terminateSession(session.getId());
		} finally {
			deleteIfValid(user);
		}
	}

	private InvocationContext invocation() throws Exception {
		return _client.newInvocation(new GetMethodWebRequest("http://ignore/" + SERVLET_NAME));
	}

	/**
	 * Stores a profile of the given account in the session of the given request, like the pac4j
	 * callback does after the identity provider has authenticated the user.
	 */
	private static void storeProfile(HttpServletRequest request, HttpServletResponse response, String accountName) {
		CommonProfile profile = new CommonProfile();
		profile.setId(accountName);
		profile.setClientName(LOGIN_CLIENT);
		new ProfileManager(new JEEContext(request, response), new JEESessionStore()).save(true, profile, false);
	}

	/**
	 * The profile stored in the session of the given request.
	 */
	private static Optional<UserProfile> storedProfile(HttpServletRequest request, HttpServletResponse response) {
		return new ProfileManager(new JEEContext(request, response), new JEESessionStore()).getProfile();
	}

	private static void doPost(InvocationContext invocation) throws Exception {
		ExternalAuthenticationServlet servlet = (ExternalAuthenticationServlet) invocation.getServlet();
		ThreadContext.inSystemContext(TestPac4jAuthenticationServlet.class, () -> {
			servlet.doPost(invocation.getRequest(), invocation.getResponse());
			return null;
		});
	}

	private static Person createPerson(String name) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			Person result = Person.create(kb, name,
				PersonManager.getManager().getRoot().getAuthenticationDevice());
			tx.commit();
			return result;
		}
	}

	private static void deleteIfValid(Person person) {
		if (!person.tValid()) {
			return;
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			person.tDelete();
			tx.commit();
		}
	}

	private static OidcProfile oidcProfile() {
		OidcProfile profile = new OidcProfile();
		profile.setId(SUBJECT);
		profile.setClientName(CLIENT);
		profile.addAttribute(CommonProfileDefinition.EMAIL, EMAIL);
		profile.addAttribute(OidcProfileDefinition.EMAIL_VERIFIED, Boolean.TRUE);
		profile.setAccessToken(new BearerAccessToken("access-secret"));
		profile.setIdTokenString("header.payload.signature");
		profile.setRefreshToken(new RefreshToken("refresh-secret"));
		return profile;
	}

	/**
	 * The suite of tests to execute, with the pac4j configuration of
	 * <code>TestPac4jAuthenticationServlet.properties.config.xml</code>.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(ServletContextServiceDecorator.INSTANCE,
			new TestSuite(TestPac4jAuthenticationServlet.class),
			SessionService.Module.INSTANCE, Login.Module.INSTANCE, Pac4jConfigFactory.Module.INSTANCE);
		test = PersonManagerSetup.createPersonManagerSetup(test);
		// The custom configuration is installed before the knowledge base is set up, and is read
		// with the configuration of the test setup.
		test = new SimpleDecoratedTestSetup(
			CustomPropertiesDecorator.newDecorator(TestPac4jAuthenticationServlet.class, true), test);
		return TestUtils.doNotMerge(TLTestSetup.createTLTestSetup(test));
	}

}
