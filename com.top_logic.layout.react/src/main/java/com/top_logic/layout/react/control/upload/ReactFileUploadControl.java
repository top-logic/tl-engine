/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.upload;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.UploadHandler;
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
 * The {@code _dataHandler} callback receives the uploaded file as {@link BinaryData}, allowing the
 * embedding component to store it (e.g. in a {@code DataField}).
 * </p>
 */
public class ReactFileUploadControl extends ReactControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";

	/** State key for an error message. */
	private static final String ERROR = "error";

	/** State key for the accepted MIME type filter. */
	private static final String ACCEPT = "accept";

	private final Consumer<BinaryData> _dataHandler;

	/**
	 * Creates a new {@link ReactFileUploadControl}.
	 *
	 * @param dataHandler
	 *        Callback that receives the uploaded file as {@link BinaryData}.
	 */
	public ReactFileUploadControl(Consumer<BinaryData> dataHandler) {
		super(null, "TLFileUpload");
		_dataHandler = dataHandler;
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
			_dataHandler.accept(data);

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
