/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.accesstoken;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import jakarta.servlet.http.HttpSession;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokenPermissions;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.knowledge.gui.layout.TLLayoutServlet;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.view.accesstoken.AccessTokenFunctions;
import com.top_logic.layout.view.accesstoken.I18NConstants;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link AccessTokenFunctions}: creating a token with and without a permission that binds
 * to the session, and who may revoke a token.
 *
 * <p>
 * The permissions {@link #PLAIN} and {@link #SESSION} are contributed by the test configuration;
 * {@link #SESSION} binds to the session through {@link RecordingSessionBinding}.
 * </p>
 */
public class TestAccessTokenFunctions extends BasicTestCase {

	private static final String PLAIN = "test-plain";

	private static final String SESSION = "test-session";

	private final List<AccessToken> _tokens = new ArrayList<>();

	private Person _actor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_actor = TLContextManager.getSubSession().getPerson();
	}

	@Override
	protected void tearDown() throws Exception {
		actAs(PersonManager.getManager().getRoot());
		for (AccessToken token : _tokens) {
			if (token.tValid()) {
				AccessTokens.revoke(token);
			}
		}
		_tokens.clear();
		RecordingSessionBinding.SESSIONS.clear();
		actAs(_actor);
		super.tearDown();
	}

	/**
	 * A token without a permission that binds to the session is created anywhere, binds no
	 * session, and has no invitation.
	 */
	public void testCreateWithoutSession() {
		Person owner = TestPerson.createPerson("tokenOwnerPlain");
		actAs(owner);

		String plain = AccessTokenFunctions.create("plain", null, List.of(PLAIN));

		AccessToken token = track(AccessTokens.find(plain));
		assertEquals(owner, token.getOwner());
		assertEquals(List.of(PLAIN), AccessTokens.permissions(token));
		assertNull(token.getSessionKey());
		assertTrue(RecordingSessionBinding.SESSIONS.isEmpty());
		assertNull(AccessTokenFunctions.invitation(plain));
	}

	/**
	 * A permission may be passed as offered by {@link AccessTokenFunctions#permissions()}.
	 */
	public void testCreateWithOfferedPermission() {
		actAs(TestPerson.createPerson("tokenOwnerOffered"));
		Object offered = AccessTokenFunctions.permissions().stream()
			.filter(permission -> permission.getName().equals(PLAIN))
			.findFirst()
			.orElseThrow();

		AccessToken token = track(AccessTokens.find(AccessTokenFunctions.create("offered", 1, List.of(offered))));

		assertEquals(List.of(PLAIN), AccessTokens.permissions(token));
	}

	/**
	 * A token with a permission that binds to the session registers the session it is created in
	 * under the key it stores, and its invitation names the endpoint.
	 */
	public void testCreateWithSession() throws Throwable {
		Person owner = TestPerson.createPerson("tokenOwnerSession");
		actAs(owner);
		TLSubSessionContext subSession = TLContextManager.getSubSession();

		ServletRunner runner = new ServletRunner();
		runner.registerServlet("lpServlet", TLLayoutServlet.class.getName());
		InvocationContext invocation =
			runner.newClient().newInvocation(new PostMethodWebRequest("http://localhost/lpServlet"));
		HttpSession session = invocation.getRequest().getSession(true);

		String[] result = new String[2];
		Throwable[] problem = new Throwable[1];
		Thread request = new Thread(() -> {
			try {
				ThreadContextManager.setupInteractionContext(subSession, null, invocation.getRequest(),
					invocation.getResponse());
				try {
					result[0] = AccessTokenFunctions.create("session", null, List.of(PLAIN, SESSION));
					result[1] = AccessTokenFunctions.invitation(result[0]);
				} finally {
					ThreadContextManager.getManager().removeInteraction();
				}
			} catch (Throwable ex) {
				problem[0] = ex;
			}
		});
		request.start();
		request.join();
		if (problem[0] != null) {
			throw problem[0];
		}
		HistoryUtils.updateSessionRevision();

		AccessToken token = track(AccessTokens.find(result[0]));
		assertNotNull(token.getSessionKey());
		assertSame(session, RecordingSessionBinding.SESSIONS.get(token.getSessionKey()));
		assertTrue(result[1], result[1].endsWith(RecordingSessionBinding.ENDPOINT_PATH + " " + result[0]));
	}

	/**
	 * A token with a permission that binds to the session cannot be created outside of a session.
	 */
	public void testCreateWithSessionOutsideSession() {
		actAs(TestPerson.createPerson("tokenOwnerNoSession"));
		try {
			AccessTokenFunctions.create("no session", null, List.of(SESSION));
			fail("A session binding permission needs a session.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_NO_SESSION, ex.getErrorKey());
		}
	}

	/**
	 * The owner revokes their token, a stranger does not, an administrator does.
	 */
	public void testRevoke() {
		Person owner = TestPerson.createPerson("tokenOwnerRevoke");
		Person stranger = TestPerson.createPerson("tokenStranger");
		Person admin = PersonManager.getManager().getRoot();
		assertTrue(admin.isAdmin());
		assertFalse(stranger.isAdmin());

		actAs(owner);
		AccessToken byOwner = track(AccessTokens.find(AccessTokenFunctions.create("by owner", null, List.of(PLAIN))));
		AccessToken byStranger =
			track(AccessTokens.find(AccessTokenFunctions.create("by stranger", null, List.of(PLAIN))));
		AccessToken byAdmin = track(AccessTokens.find(AccessTokenFunctions.create("by admin", null, List.of(PLAIN))));

		assertTrue(AccessTokenFunctions.revoke(byOwner));
		assertFalse("The owner revokes their token.", byOwner.tValid());

		actAs(stranger);
		try {
			AccessTokenFunctions.revoke(byStranger);
			fail("A stranger must not revoke the token.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_TOKEN_NOT_OWNED, ex.getErrorKey());
		}
		assertTrue(byStranger.tValid());

		actAs(admin);
		assertTrue(AccessTokenFunctions.revoke(byAdmin));
		assertFalse("An administrator revokes any token.", byAdmin.tValid());
	}

	private AccessToken track(AccessToken token) {
		assertNotNull(token);
		_tokens.add(token);
		return token;
	}

	private static void actAs(Person person) {
		TLContextManager.getSubSession().setPerson(person);
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(new TestSuite(TestAccessTokenFunctions.class),
			ModelService.Module.INSTANCE, AccessTokenPermissions.Module.INSTANCE);
		String customFile = CustomPropertiesDecorator.createFileName(TestAccessTokenFunctions.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return PersonManagerSetup.createPersonManagerSetup(innerTest);
	}

}
