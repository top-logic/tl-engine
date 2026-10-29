/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

/**
 * Why the {@link PreviewServlet preview route} does not forward a request.
 *
 * <p>
 * The {@link #message() message} is a fixed text, never something of the request.
 * </p>
 */
public enum PreviewRefusal {

	/**
	 * The request names no stored workspace, or its path is not below the context path of the
	 * application of the workspace.
	 */
	NOT_FOUND(404, "Not found."),

	/**
	 * The request belongs to no session logged into the control application.
	 */
	NOT_LOGGED_IN(403, "Log in to the control application to see the preview."),

	/**
	 * The user of the session may not read the project of the workspace.
	 */
	FORBIDDEN(403, "No access to this workspace."),

	/**
	 * The application of the workspace is not running, or it is not published.
	 */
	NOT_RUNNING(503, "The application of the workspace is not running."),

	/**
	 * The application of the workspace does not accept the connection.
	 */
	NOT_ANSWERING(503, "The application of the workspace does not answer."),

	;

	private final int _status;

	private final String _message;

	PreviewRefusal(int status, String message) {
		_status = status;
		_message = message;
	}

	/**
	 * The HTTP status of the answer.
	 */
	public int status() {
		return _status;
	}

	/**
	 * The text of the answer.
	 */
	public String message() {
		return _message;
	}

}
