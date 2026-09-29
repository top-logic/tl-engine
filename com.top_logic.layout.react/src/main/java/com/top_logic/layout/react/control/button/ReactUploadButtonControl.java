/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.Part;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A button that, on click, opens a native file picker (client-side) and uploads the selected
 * file(s), then hands them to its {@link UploadCommandModel} for processing.
 *
 * <p>
 * Rendered by the {@code TLUploadButton} React component. Unlike a normal {@link ReactButtonControl}
 * the click does not dispatch a server command; instead the selected files are POSTed to the
 * {@code /react-api/upload} endpoint and delivered here via {@link #handleUpload}.
 * </p>
 */
public class ReactUploadButtonControl extends ReactButtonControl implements UploadHandler {

	/** State key for the native file input's {@code accept} filter. */
	private static final String ACCEPT = "accept";

	/** State key for the native file input's {@code multiple} flag. */
	private static final String MULTIPLE = "multiple";

	private final UploadCommandModel _uploadModel;

	private final ReactContext _context;

	/**
	 * Creates a new {@link ReactUploadButtonControl}.
	 *
	 * @param context
	 *        The view context (retained to drive the model's per-file processing, which may open a
	 *        confirmation dialog and resolve channels).
	 * @param model
	 *        The upload command model providing label/icon/executability and the file processing.
	 */
	public ReactUploadButtonControl(ReactContext context, UploadCommandModel model) {
		super(context, model, "TLUploadButton");
		_context = context;
		_uploadModel = model;
		putState(ACCEPT, model.getAccept());
		putState(MULTIPLE, model.isMultiple());
		putState(UploadSupport.MAX_UPLOAD_SIZE, UploadSupport.maxUploadSize());
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * An upload to a button that is not {@link #getOfferedState() offered} is refused like a click
	 * on it; otherwise the files go to {@link UploadCommandModel#upload(ReactContext, List)}, which
	 * decides by the command's state in turn.
	 * </p>
	 */
	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		ExecutableState offered = getOfferedState();
		if (!offered.isExecutable()) {
			return HandlerResult.notExecutable(offered);
		}
		try {
			List<BinaryData> files = UploadSupport.toBinaryData(parts);
			if (!files.isEmpty()) {
				return _uploadModel.upload(_context, files);
			}
		} catch (IOException ex) {
			return CommandErrors.failure(ex, "Reading the files uploaded to '" + _uploadModel.getLabel() + "'",
				ReactUploadButtonControl.class);
		}
		return HandlerResult.DEFAULT_RESULT;
	}
}
