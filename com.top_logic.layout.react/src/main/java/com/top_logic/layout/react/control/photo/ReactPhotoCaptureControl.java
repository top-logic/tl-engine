/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.photo;

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
 * A React control that captures a photo from the user's camera and delivers the image data to the
 * server.
 *
 * <p>
 * On the client side, this control renders a {@code TLPhotoCapture} React component that uses the
 * {@code getUserMedia} API to access the camera. The captured photo is uploaded via the
 * {@code /react-api/upload} multipart endpoint.
 * </p>
 *
 * <p>
 * The captured photo is written to a shared {@link BinaryDataValue} model, allowing all controls
 * observing the same model to update automatically.
 * </p>
 */
public class ReactPhotoCaptureControl extends ReactControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";

	/** State key for an error message. */
	private static final String ERROR = "error";

	private final BinaryDataValue _model;

	/**
	 * Creates a new {@link ReactPhotoCaptureControl}.
	 *
	 * @param model
	 *        The shared data model to write captured photos to.
	 */
	public ReactPhotoCaptureControl(BinaryDataValue model) {
		super(null, "TLPhotoCapture");
		_model = model;
		putState(STATUS, "idle");
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		putState(STATUS, "received");
		try {
			Part photoPart = parts.stream()
				.filter(p -> "photo".equals(p.getName()))
				.findFirst()
				.orElse(null);

			if (photoPart == null) {
				putState(ERROR, "No photo data received.");
				putState(STATUS, "idle");
				return HandlerResult.DEFAULT_RESULT;
			}

			byte[] photoData = photoPart.getInputStream().readAllBytes();
			String contentType = photoPart.getContentType();
			if (contentType == null) {
				contentType = "image/jpeg";
			}

			String fileName = photoPart.getSubmittedFileName();
			if (fileName == null) {
				fileName = "capture." + mimeToExtension(contentType);
			}

			BinaryData data = BinaryDataFactory.createBinaryData(photoData, contentType, fileName);
			_model.setData(data);

			putState(ERROR, null);
			putState(STATUS, "idle");
		} catch (IOException ex) {
			Logger.error("Failed to process photo upload.", ex, ReactPhotoCaptureControl.class);
			putState(ERROR, ex.getMessage());
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private static String mimeToExtension(String contentType) {
		if (contentType == null) {
			return "bin";
		}
		// Strip parameters (e.g. "image/jpeg;..." -> "image/jpeg").
		int semicolon = contentType.indexOf(';');
		String mime = semicolon >= 0 ? contentType.substring(0, semicolon).trim() : contentType.trim();

		switch (mime) {
			case "image/jpeg":
				return "jpg";
			case "image/png":
				return "png";
			case "image/webp":
				return "webp";
			default:
				return "bin";
		}
	}

}
