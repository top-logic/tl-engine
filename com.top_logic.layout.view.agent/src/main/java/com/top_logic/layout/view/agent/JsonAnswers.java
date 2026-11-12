/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Logger;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.react.scripting.ScriptingNodeView;

/**
 * Answers of a servlet whose every answer is a JSON document, such as the {@link AgentServlet}.
 *
 * <p>
 * An answer is built completely before the first byte is written, see
 * {@link #answer(HttpServletResponse, String, int, boolean, Producer)}. A failure while building it - even an
 * {@link Error} such as an {@link OutOfMemoryError} - is therefore still answered with an error
 * status and a JSON object whose {@link #FIELD_ERROR} member holds the message.
 * </p>
 */
public final class JsonAnswers {

	/** Member of an error answer holding the message. */
	public static final String FIELD_ERROR = "error";

	/** Message of the {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR} answer. */
	public static final String ERROR_INTERNAL = "Internal error.";

	/** Content type of every answer. */
	private static final String CONTENT_TYPE_JSON = "application/json";

	/** Character encoding of every answer. */
	private static final String ENCODING_UTF8 = StandardCharsets.UTF_8.name();

	private JsonAnswers() {
		// Static utility.
	}

	/**
	 * Computes the answer to a request.
	 */
	@FunctionalInterface
	public interface Producer {

		/**
		 * The complete answer, a {@link Map}/{@link List}/scalar structure as written by
		 * {@link ScriptingNodeView#writeValue(JsonWriter, Object)}.
		 *
		 * @throws RequestRejected
		 *         To answer with a status of its own.
		 * @throws IllegalArgumentException
		 *         To answer with the status for a request the servlet cannot process.
		 */
		Object answer() throws IOException;

	}

	/**
	 * Answers a request with the JSON document the given {@link Producer} builds.
	 *
	 * <p>
	 * The answer is streamed into the response only after the {@link Producer} has returned. A
	 * {@link RequestRejected} is answered with its {@link RequestRejected#status() status}, an
	 * {@link IllegalArgumentException} with the given status. Every other problem, including an
	 * {@link Error}, is logged with its stack trace and, if no part of the answer is sent yet,
	 * answered with {@link HttpServletResponse#SC_INTERNAL_SERVER_ERROR}; otherwise the answer
	 * ends where it broke off. No problem is passed on to the servlet container: it would only
	 * break the connection without a status, which a proxy reports as a bad gateway. The memory
	 * that building a too large answer took is released when the handling unwinds, so even an
	 * {@link OutOfMemoryError} leaves the server able to serve the next request.
	 * </p>
	 *
	 * @param response
	 *        The response to write to.
	 * @param context
	 *        A description of the request for the log, e.g. its path.
	 * @param illegalArgumentStatus
	 *        The status answering an {@link IllegalArgumentException} of the {@link Producer}.
	 * @param indent
	 *        Whether to write the answer indented for a human reader, or compact JSON.
	 * @param producer
	 *        Builds the answer.
	 */
	public static void answer(HttpServletResponse response, String context, int illegalArgumentStatus,
			boolean indent, Producer producer) {
		try {
			Object answer = producer.answer();
			response.setContentType(CONTENT_TYPE_JSON);
			response.setCharacterEncoding(ENCODING_UTF8);
			write(response, answer, indent);
		} catch (RequestRejected ex) {
			sendErrorAfterFailure(response, context, ex.status(), ex.getMessage());
		} catch (IllegalArgumentException ex) {
			sendErrorAfterFailure(response, context, illegalArgumentStatus, ex.getMessage());
		} catch (Throwable ex) {
			Logger.error("Error handling request: " + context, ex, JsonAnswers.class);
			sendErrorAfterFailure(response, context, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ERROR_INTERNAL);
		}
	}

	private static void sendErrorAfterFailure(HttpServletResponse response, String context, int status,
			String message) {
		if (response.isCommitted()) {
			return;
		}
		try {
			response.resetBuffer();
			sendError(response, status, message);
		} catch (IOException | RuntimeException ex) {
			Logger.error("Failed to answer request: " + context, ex, JsonAnswers.class);
		}
	}

	/**
	 * Answers with the given status and a JSON object whose {@link #FIELD_ERROR} member holds the
	 * given message.
	 */
	public static void sendError(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(CONTENT_TYPE_JSON);
		response.setCharacterEncoding(ENCODING_UTF8);
		write(response, Map.of(FIELD_ERROR, message == null ? "" : message), false);
	}

	/**
	 * Streams the given answer as JSON document into the body of the response.
	 *
	 * @param answer
	 *        The answer, see {@link ScriptingNodeView#writeValue(JsonWriter, Object)}.
	 * @param indent
	 *        Whether to indent the document.
	 */
	private static void write(HttpServletResponse response, Object answer, boolean indent) throws IOException {
		// The output stream, unlike the response's print writer, reports a failed write.
		Writer out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8));
		ScriptingNodeView.writeJson(out, answer, indent);
		out.flush();
	}

	/**
	 * A request a {@link Producer} refuses with the given {@link #status() status}.
	 */
	public static final class RequestRejected extends RuntimeException {

		private final int _status;

		/**
		 * Creates a {@link RequestRejected}.
		 *
		 * @param status
		 *        The HTTP status of the answer.
		 * @param message
		 *        The message of the JSON error object of the answer.
		 */
		public RequestRejected(int status, String message) {
			super(message);
			_status = status;
		}

		/**
		 * The HTTP status of the answer.
		 */
		public int status() {
			return _status;
		}
	}
}
