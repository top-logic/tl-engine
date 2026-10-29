/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

/**
 * The application of a workspace the {@link PreviewServlet preview route} forwards to.
 *
 * @param contextPath
 *        The context path the application is served under, the browser-visible path of the
 *        application: starts with a slash, has none at its end.
 * @param origin
 *        Scheme, host and port the application is reached at, e.g.
 *        {@code http://127.0.0.1:20001}.
 */
public record PreviewTarget(String contextPath, String origin) {

	// Pure data.

}
