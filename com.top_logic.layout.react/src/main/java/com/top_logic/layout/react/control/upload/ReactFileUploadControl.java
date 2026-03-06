/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.upload;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A React control that provides a file upload UI and delivers the uploaded file to the server.
 *
 * <p>
 * On the client side, this control renders a {@code TLFileUpload} React component with a file
 * picker (including drag-and-drop support). The selected file is uploaded via the
 * {@code /react-api/upload} multipart endpoint.
 * </p>
 *
 * <p>
 * The uploaded file is written to a shared {@link BinaryDataValue} model, allowing all controls
 * observing the same model to update automatically.
 * </p>
 */
public class ReactFileUploadControl extends ReactControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";

	/** State key for an error message. */
	private static final String ERROR = "error";

	/** State key for the accepted MIME type filter. */
	private static final String ACCEPT = "accept";

	private final BinaryDataValue _model;

	/**
	 * Creates a new {@link ReactFileUploadControl}.
	 *
	 * @param model
	 *        The shared data model to write uploaded files to.
	 */
	public ReactFileUploadControl(BinaryDataValue model) {
		super(null, "TLFileUpload");
		_model = model;
		putState(STATUS, "idle");
	}

	/**
	 * Sets the accepted file types filter.
	 *
	 * @param accept
	 *        A comma-separated list of MIME types or file extensions (e.g. {@code "image/*,.pdf"}).
	 *        Pass {@code null} to accept all file types.
	 */
	public void setAccept(String accept) {
		putState(ACCEPT, accept);
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		putState(STATUS, "received");
		try {
			Part filePart = parts.stream()
				.filter(p -> "file".equals(p.getName()))
				.findFirst()
				.orElse(null);

			if (filePart == null) {
				putState(ERROR, "No file data received.");
				putState(STATUS, "idle");
				return HandlerResult.DEFAULT_RESULT;
			}

			byte[] fileData = filePart.getInputStream().readAllBytes();
			String contentType = filePart.getContentType();
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			String fileName = filePart.getSubmittedFileName();
			if (fileName == null) {
				fileName = "upload.bin";
			}

			BinaryData data = BinaryDataFactory.createBinaryData(fileData, contentType, fileName);
			_model.setData(data);

			putState(ERROR, null);
			putState(STATUS, "idle");
		} catch (IOException ex) {
			Logger.error("Failed to process file upload.", ex, ReactFileUploadControl.class);
			putState(ERROR, ex.getMessage());
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
