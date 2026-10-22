/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

/**
 * A protocol error with its JSON-RPC code (A2A v1.0, section 5.4).
 */
public class A2AError extends RuntimeException {

	/** The request is not valid JSON. */
	public static final int PARSE_ERROR = -32700;

	/** The JSON is not a valid JSON-RPC request. */
	public static final int INVALID_REQUEST = -32600;

	/** Unknown method. */
	public static final int METHOD_NOT_FOUND = -32601;

	/** Invalid method parameters. */
	public static final int INVALID_PARAMS = -32602;

	/** Server-side failure. */
	public static final int INTERNAL_ERROR = -32603;

	/** {@code TaskNotFoundError}. */
	public static final int TASK_NOT_FOUND = -32001;

	/** {@code TaskNotCancelableError}. */
	public static final int TASK_NOT_CANCELABLE = -32002;

	/** {@code PushNotificationNotSupportedError}. */
	public static final int PUSH_NOT_SUPPORTED = -32003;

	/** {@code UnsupportedOperationError}. */
	public static final int UNSUPPORTED_OPERATION = -32004;

	/** {@code InvalidAgentResponseError}: the client's answer to a peer that answers nonsense. */
	public static final int INVALID_AGENT_RESPONSE = -32006;

	/** {@code VersionNotSupportedError}. */
	public static final int VERSION_NOT_SUPPORTED = -32009;

	private final int _code;

	/**
	 * Creates an {@link A2AError}.
	 */
	public A2AError(int code, String message) {
		super(message);
		_code = code;
	}

	/**
	 * The JSON-RPC error code.
	 */
	public int code() {
		return _code;
	}

	/**
	 * The JSON-RPC {@code error} object.
	 */
	public Object toJson() {
		return Json.map("code", _code, "message", getMessage());
	}

	/**
	 * Reads an error object a peer sent.
	 */
	public static A2AError fromJson(Object error) {
		Integer code = Json.integer(error, "code");
		String message = Json.str(error, "message");
		return new A2AError(code == null ? INTERNAL_ERROR : code, message == null ? "Remote agent error." : message);
	}
}
