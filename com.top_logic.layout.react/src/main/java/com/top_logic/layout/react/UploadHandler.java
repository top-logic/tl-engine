/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.Collection;

import jakarta.servlet.http.Part;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Interface for React controls that accept multipart file uploads.
 *
 * <p>
 * Controls implementing this interface can receive binary data from the React client via the
 * {@code /react-api/upload} endpoint. The {@link ReactServlet} dispatches incoming multipart
 * requests to the appropriate control based on the {@code controlId} parameter.
 * </p>
 */
public interface UploadHandler {

	/**
	 * Handles an incoming multipart upload.
	 *
	 * @param context
	 *        The display context.
	 * @param parts
	 *        The multipart parts from the request.
	 * @return The result of handling the upload.
	 */
	HandlerResult handleUpload(DisplayContext context, Collection<Part> parts);

}
