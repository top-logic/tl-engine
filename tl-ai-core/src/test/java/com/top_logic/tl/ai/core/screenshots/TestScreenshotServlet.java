/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.util.model.ModelService;

/**
 * That {@code /screenshot/{id}} hands a captured picture to the account that captured it, and to
 * nobody else (issue #216).
 *
 * <p>
 * The three answers that matter are asserted here, because the route delivers a picture of a page
 * somebody was logged in to somewhere: the owner gets the bytes, a colleague gets 404, and a
 * request nobody is signed in for gets 401. The fourth - an id that does not exist - has to be
 * indistinguishable from the third, or the route says which ids exist.
 * </p>
 *
 * <p>
 * The servlet's own dispatch is driven directly rather than over HTTP, as
 * {@code TestChatServletOwnership} does: the request is a real one from the servlet test container
 * with a real {@code HttpSession} logged in through {@link SessionService}, because that - and not
 * the thread context - is where the servlet reads the acting account from.
 * </p>
 */
public class TestScreenshotServlet extends BasicTestCase {

	/** A minimal but valid 1x1 PNG. */
	private static final byte[] PNG_BYTES = {
		(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
		0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
		0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01,
		0x08, 0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4,
		(byte) 0x89, 0x00, 0x00, 0x00, 0x0A, 0x49, 0x44, 0x41,
		0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
		0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00,
		0x00, 0x00, 0x00, 0x49, 0x45, 0x4E, 0x44, (byte) 0xAE,
		0x42, 0x60, (byte) 0x82
	};

	/** The two accounts and the one capture, made once: no test here changes them. */
	private static Person anna;

	private static Person ben;

	private static Screenshot annasShot;

	private static ServletRunner runner;

	private Probe _servlet;

	/** Exposes the servlet's own dispatch to the test. */
	static final class Probe extends ScreenshotServlet {

		void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doGet(req, resp);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-with-a-screenshot");
			ben = TestPerson.createPerson("ben-with-none");
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				annasShot = ScreenshotService.getInstance()
					.createScreenshot("annas-page.png", PNG_BYTES, "https://example.com/", anna);
				tx.commit();
			}
			runner = new ServletRunner();
			runner.registerServlet("screenshot/*", ScreenshotServlet.class.getName());
		}
		_servlet = new Probe();
	}

	/** The owner gets the picture, with its stored content type and the stored bytes. */
	public void testOwnerGetsTheImage() throws Exception {
		Recording response = get(annasShot.getId(), anna);

		assertEquals(HttpServletResponse.SC_OK, response._status);
		assertEquals(ScreenshotService.PNG_CONTENT_TYPE, response._contentType);
		assertTrue("the stored PNG is delivered", Arrays.equals(PNG_BYTES, response._body.toByteArray()));
	}

	/** An immutable picture, and one no shared cache may keep: it depends on who asked. */
	public void testDeliveryIsCacheableButPrivate() throws Exception {
		Recording response = get(annasShot.getId(), anna);

		String cacheControl = response._headers.get("Cache-Control");
		assertNotNull("the answer says how long it may be kept", cacheControl);
		assertTrue("no shared cache may hold it: " + cacheControl, cacheControl.contains("private"));
		assertEquals("the declared type is the only type", "nosniff",
			response._headers.get("X-Content-Type-Options"));
	}

	/** A colleague does not get somebody else's screenshot. */
	public void testForeignScreenshotIsNotFound() throws Exception {
		Recording response = get(annasShot.getId(), ben);

		assertEquals(HttpServletResponse.SC_NOT_FOUND, response._status);
		assertEquals("nothing is delivered", 0, response._body.size());
	}

	/**
	 * An id nobody stored is answered exactly like one that is not the caller's.
	 *
	 * <p>
	 * Otherwise the difference between the two answers tells a caller which ids exist.
	 * </p>
	 */
	public void testUnknownIdLooksTheSameAsAForeignOne() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND, get("AAAAAAAAAAAAAAAAAAAAAA", ben)._status);
		assertEquals("and so does no id at all", HttpServletResponse.SC_NOT_FOUND, get("", ben)._status);
	}

	/** A request nobody is signed in for is refused, not answered. */
	public void testUnauthenticatedRequestIsRefused() throws Exception {
		Recording response = new Recording();
		// No SessionService.loginUser: the request carries no signed-in session.
		InvocationContext invocation =
			client().newInvocation(new GetMethodWebRequest("http://localhost/screenshot/" + annasShot.getId()));
		_servlet.get(invocation.getRequest(), response.asResponse());

		assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response._status);
		assertEquals("nothing is delivered", 0, response._body.size());
	}

	private Recording get(String id, Person caller) throws Exception {
		Recording response = new Recording();
		_servlet.get(request(id, caller, response), response.asResponse());
		return response;
	}

	/** A request for {@code /screenshot/<id>} whose {@code HttpSession} is signed in as the caller. */
	private static HttpServletRequest request(String id, Person caller, Recording response) throws Exception {
		InvocationContext invocation =
			client().newInvocation(new GetMethodWebRequest("http://localhost/screenshot/" + id));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		});
		return request;
	}

	private static ServletUnitClient client() {
		return runner.newClient();
	}

	/** Records what the servlet answered. */
	static final class Recording {

		int _status = HttpServletResponse.SC_OK;

		String _contentType;

		final Map<String, String> _headers = new HashMap<>();

		final ByteArrayOutputStream _body = new ByteArrayOutputStream();

		private final StringWriter _text = new StringWriter();

		private HttpServletResponse _view;

		HttpServletResponse asResponse() {
			if (_view == null) {
				_view = (HttpServletResponse) Proxy.newProxyInstance(
					HttpServletResponse.class.getClassLoader(),
					new Class<?>[] { HttpServletResponse.class },
					new Handler());
			}
			return _view;
		}

		private final class Handler implements InvocationHandler {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
				switch (method.getName()) {
					case "sendError":
					case "setStatus":
						_status = ((Integer) args[0]).intValue();
						return null;
					case "setContentType":
						_contentType = (String) args[0];
						return null;
					case "setHeader":
						_headers.put((String) args[0], (String) args[1]);
						return null;
					case "getOutputStream":
						return new Sink(_body);
					case "getWriter":
						return new PrintWriter(_text);
					default:
						return null;
				}
			}
		}
	}

	/** The response body, collected. */
	static final class Sink extends ServletOutputStream {

		private final ByteArrayOutputStream _target;

		Sink(ByteArrayOutputStream target) {
			_target = target;
		}

		@Override
		public void write(int b) {
			_target.write(b);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener listener) {
			// Never asynchronous here.
		}
	}

	/**
	 * Creates the test suite with the services the route uses: accounts and login (the acting
	 * account), the model and the screenshot store.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestScreenshotServlet.class),
				ModelService.Module.INSTANCE,
				ScreenshotService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
