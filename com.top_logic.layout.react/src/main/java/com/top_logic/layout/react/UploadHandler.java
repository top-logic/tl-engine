/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.Collection;

import jakarta.servlet.http.Part;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.ReactServlet;
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

	/**
	 * The maximum size in bytes of one file this control accepts, <code>0</code> if it accepts
	 * files of any size.
	 *
	 * <p>
	 * The {@link ReactServlet} refuses an upload holding a larger file before it reaches
	 * {@link #handleUpload(DisplayContext, Collection)}. The limit applies to each file on its own:
	 * several files uploaded at once each may have this size.
	 * </p>
	 *
	 * <p>
	 * By default the application-wide {@link UploadSupport#maxUploadSize() limit}. A control with a
	 * limit of its own reports the {@link UploadSupport#effectiveMaxUploadSize(long) smaller} of
	 * both and publishes it to its client under {@link UploadSupport#MAX_UPLOAD_SIZE}.
	 * </p>
	 */
	default long maxUploadSize() {
		return UploadSupport.maxUploadSize();
	}

}
