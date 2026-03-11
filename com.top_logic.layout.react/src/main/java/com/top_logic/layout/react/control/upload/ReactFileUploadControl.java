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
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.DataFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactFormFieldControl} that provides a file upload UI via the {@code TLFileUpload} React
 * component.
 *
 * <p>
 * The selected file is uploaded via the {@code /react-api/upload} multipart endpoint. The uploaded
 * {@link BinaryData} is stored as the {@link FieldModel} value. When the model is a
 * {@link DataFieldModel}, the accepted types and max upload size are sent to the client
 * automatically.
 * </p>
 */
public class ReactFileUploadControl extends ReactFormFieldControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";

	/** State key for an error message. */
	private static final String ERROR = "error";

	/** State key for the accepted MIME type filter. */
	private static final String ACCEPT = "accept";

	/** State key for the maximum upload size. */
	private static final String MAX_SIZE = "maxSize";

	/**
	 * Creates a new {@link ReactFileUploadControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The field model. Value type is {@link BinaryData}. If the model is a
	 *        {@link DataFieldModel}, accepted types and max upload size are sent to the client.
	 */
	public ReactFileUploadControl(ReactContext context, FieldModel model) {
		super(context, model, "TLFileUpload");
		putState(STATUS, "idle");
		initDataFieldState();
	}

	private void initDataFieldState() {
		if (getFieldModel() instanceof DataFieldModel) {
			DataFieldModel dataModel = (DataFieldModel) getFieldModel();
			putState(ACCEPT, dataModel.getAcceptedTypes());
			long maxSize = dataModel.getMaxUploadSize();
			if (maxSize > 0) {
				putState(MAX_SIZE, maxSize);
			}
		}
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
			getFieldModel().setValue(data);

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
