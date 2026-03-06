/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.audio;

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
 * A React control that records audio from the user's microphone and delivers the recorded data to
 * the server.
 *
 * <p>
 * On the client side, this control renders a {@code TLAudioRecorder} React component that uses the
 * {@code MediaRecorder} API to capture audio. The recorded audio is uploaded via the
 * {@code /react-api/upload} multipart endpoint.
 * </p>
 *
 * <p>
 * The recorded audio is written to a shared {@link BinaryDataValue} model, allowing all controls
 * observing the same model to update automatically.
 * </p>
 */
public class ReactAudioRecorderControl extends ReactControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";

	/** State key for an error message. */
	private static final String ERROR = "error";

	private final BinaryDataValue _model;

	/**
	 * Creates a new {@link ReactAudioRecorderControl}.
	 *
	 * @param model
	 *        The shared data model to write recorded audio to.
	 */
	public ReactAudioRecorderControl(BinaryDataValue model) {
		super(null, "TLAudioRecorder");
		_model = model;
		putState(STATUS, "idle");
	}

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		putState(STATUS, "received");
		try {
			Part audioPart = parts.stream()
				.filter(p -> "audio".equals(p.getName()))
				.findFirst()
				.orElse(null);

			if (audioPart == null) {
				putState(ERROR, "No audio data received.");
				putState(STATUS, "idle");
				return HandlerResult.DEFAULT_RESULT;
			}

			byte[] audioData = audioPart.getInputStream().readAllBytes();
			String contentType = audioPart.getContentType();
			if (contentType == null) {
				contentType = "audio/webm";
			}

			String fileName = audioPart.getSubmittedFileName();
			if (fileName == null) {
				fileName = "recording." + mimeToExtension(contentType);
			}

			BinaryData data = BinaryDataFactory.createBinaryData(audioData, contentType, fileName);
			_model.setData(data);

			putState(ERROR, null);
			putState(STATUS, "idle");
		} catch (IOException ex) {
			Logger.error("Failed to process audio upload.", ex, ReactAudioRecorderControl.class);
			putState(ERROR, ex.getMessage());
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private static String mimeToExtension(String contentType) {
		if (contentType == null) {
			return "bin";
		}
		// Strip parameters (e.g. "audio/webm;codecs=opus" -> "audio/webm").
		int semicolon = contentType.indexOf(';');
		String mime = semicolon >= 0 ? contentType.substring(0, semicolon).trim() : contentType.trim();

		switch (mime) {
			case "audio/webm":
				return "webm";
			case "audio/ogg":
				return "ogg";
			case "audio/mp4":
				return "m4a";
			case "audio/wav":
			case "audio/wave":
				return "wav";
			case "audio/mpeg":
				return "mp3";
			default:
				return "bin";
		}
	}

}
