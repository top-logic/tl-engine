/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the workspace a request of the {@link PreviewServlet preview route} names, deciding
 * whether the client of the request may see its application.
 *
 * @see StoredPreviewWorkspaces
 */
public interface PreviewWorkspaces {

	/**
	 * The application of the given workspace, if the client of the request may see it.
	 *
	 * @param request
	 *        The request, identifying the client by its session.
	 * @param workspaceId
	 *        The identifier of the workspace the request names.
	 * @return The application to forward to, or why the request is refused.
	 */
	PreviewLookup lookup(HttpServletRequest request, String workspaceId);

}
