/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.agent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.scripting.ScriptingNodeView;
import com.top_logic.layout.view.agent.AgentServlet;
import com.top_logic.layout.view.agent.JsonAnswers;

/**
 * Test of {@link JsonAnswers}, the answering of the {@link AgentServlet}: a built answer is
 * streamed as JSON, a failure while building it - even an {@link Error} - is answered with a status
 * and a JSON error object.
 */
public class TestJsonAnswers extends TestCase {

	private static final String SERVLET_NAME = "answers";

	private static final String BASE_URL = "http://localhost/" + SERVLET_NAME;

	/** Path answering {@link #SAMPLE}. */
	static final String PATH_OK = "/ok";

	/** Path whose answer fails with an {@link OutOfMemoryError}. */
	static final String PATH_OUT_OF_MEMORY = "/oom";

	/** Path whose answer fails with a {@link RuntimeException}. */
	static final String PATH_FAILURE = "/failure";

	/** Path whose answer is rejected with {@link HttpServletResponse#SC_CONFLICT}. */
	static final String PATH_REJECTED = "/rejected";

	/** Path whose answer fails with an {@link IllegalArgumentException}. */
	static final String PATH_ILLEGAL = "/illegal";

	/** Parameter requesting the indented answer. */
	static final String PARAM_PRETTY = "pretty";

	/** Message of the rejection and the illegal argument. */
	static final String MESSAGE = "Not like this.";

	/** The answer of {@link #PATH_OK}. */
	static final Map<String, Object> SAMPLE =
		Map.of("windows", List.of("w1", "w2"), "text", "äöü€");

	/**
	 * Servlet answering through {@link JsonAnswers} as its path says.
	 */
	public static class AnswersServlet extends HttpServlet {

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) {
			String path = request.getPathInfo();
			boolean indent = Boolean.parseBoolean(request.getParameter(PARAM_PRETTY));
			JsonAnswers.answer(response, path, HttpServletResponse.SC_NOT_FOUND, indent, () -> {
				switch (path) {
					case PATH_OK:
						return SAMPLE;
					case PATH_OUT_OF_MEMORY:
						throw new OutOfMemoryError("Simulated while projecting.");
					case PATH_FAILURE:
						throw new IllegalStateException("Simulated while projecting.");
					case PATH_REJECTED:
						throw new JsonAnswers.RequestRejected(HttpServletResponse.SC_CONFLICT, MESSAGE);
					case PATH_ILLEGAL:
						throw new IllegalArgumentException(MESSAGE);
					default:
						throw new IOException("Unexpected path: " + path);
				}
			});
		}
	}

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ServletRunner runner = new ServletRunner();
		runner.registerServlet(SERVLET_NAME + "/*", AnswersServlet.class.getName());
		_client = runner.newClient();
		_client.setExceptionsThrownOnErrorStatus(false);
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;
		super.tearDown();
	}

	/**
	 * A built answer is the compact JSON of the answer.
	 */
	public void testAnswer() throws Exception {
		WebResponse response = get(PATH_OK);
		assertEquals(HttpServletResponse.SC_OK, response.getResponseCode());
		assertEquals("application/json", response.getContentType());
		StringBuilder compact = new StringBuilder();
		ScriptingNodeView.writeJson(compact, SAMPLE, false);
		assertEquals(compact.toString(), response.getText());
		assertEquals(SAMPLE, JSON.fromString(response.getText()));
	}

	/**
	 * The indented answer is the same JSON value as the compact one.
	 */
	public void testPretty() throws Exception {
		WebResponse compact = get(PATH_OK);
		WebResponse pretty = get(PATH_OK + "?" + PARAM_PRETTY + "=true");
		assertEquals(ScriptingNodeView.toJson(SAMPLE), pretty.getText());
		assertEquals(JSON.fromString(compact.getText()), JSON.fromString(pretty.getText()));
		assertFalse(compact.getText().contains("\n"));
		assertTrue(pretty.getText().contains("\n"));
	}

	/**
	 * An {@link Error} while building the answer is answered with
	 * {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} and a JSON error object.
	 */
	public void testErrorWhileBuilding() throws Exception {
		assertError(get(PATH_OUT_OF_MEMORY), HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			JsonAnswers.ERROR_INTERNAL);
	}

	/**
	 * An exception while building the answer is answered with
	 * {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} and a JSON error object.
	 */
	public void testExceptionWhileBuilding() throws Exception {
		assertError(get(PATH_FAILURE), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, JsonAnswers.ERROR_INTERNAL);
	}

	/**
	 * A rejected request is answered with the status of the rejection.
	 */
	public void testRejected() throws Exception {
		assertError(get(PATH_REJECTED), HttpServletResponse.SC_CONFLICT, MESSAGE);
	}

	/**
	 * An illegal argument is answered with the status given for it.
	 */
	public void testIllegalArgument() throws Exception {
		assertError(get(PATH_ILLEGAL), HttpServletResponse.SC_NOT_FOUND, MESSAGE);
	}

	private WebResponse get(String path) throws Exception {
		return _client.getResponse(new GetMethodWebRequest(BASE_URL + path));
	}

	private static void assertError(WebResponse response, int status, String message) throws Exception {
		assertEquals(status, response.getResponseCode());
		assertEquals("application/json", response.getContentType());
		Object content = JSON.fromString(response.getText());
		assertEquals(Map.of(JsonAnswers.FIELD_ERROR, message), content);
	}

}
