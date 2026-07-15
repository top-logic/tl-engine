/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.DataFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactFormFieldControl} for binary ({@link BinaryData}) attributes.
 *
 * <p>
 * The same control covers both editing and display: in edit mode it renders a file upload
 * (drag-and-drop plus button), in view mode a download link. It therefore serves both the
 * {@code /react-api/upload} endpoint (as an {@link UploadHandler}, writing the uploaded
 * {@link BinaryData} to the {@link FieldModel}) and the {@code /react-api/data} endpoint (as a
 * {@link DataProvider}, streaming the field's current {@link BinaryData}).
 * </p>
 *
 * <p>
 * Unlike a text-valued field, the {@link BinaryData} value is never serialized into the React state
 * (it may be large and is not JSON-representable). Instead the control emits a {@link #FILE_NAME},
 * a {@link #HAS_DATA} flag and a {@link #DATA_REVISION} counter; the client fetches the actual bytes
 * on demand from the data endpoint.
 * </p>
 */
public class ReactBinaryFieldControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	/** State key for the upload status ({@code "idle"} / {@code "received"}). */
	private static final String STATUS = "status";

	/** State key for an upload error message. */
	private static final String ERROR = "error";

	/** State key for the accepted MIME type filter. */
	private static final String ACCEPT = "accept";

	/** State key for the maximum upload size. */
	private static final String MAX_SIZE = "maxSize";

	/** State key indicating whether download data is available. */
	private static final String HAS_DATA = "hasData";

	/** State key whose value increments each time the data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	/** State key for the suggested file name. */
	private static final String FILE_NAME = "fileName";

	private int _dataRevision;

	/**
	 * Creates a new {@link ReactBinaryFieldControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The field model. Value type is {@link BinaryData}. If the model is a
	 *        {@link DataFieldModel}, accepted types and max upload size are sent to the client.
	 */
	public ReactBinaryFieldControl(ReactContext context, FieldModel model) {
		super(context, model, "TLBinaryField");
		// The base constructor seeded VALUE with the raw BinaryData; drop it (not JSON-serializable)
		// and emit the download/upload display state instead.
		putState(VALUE, null);
		putState(STATUS, "idle");
		updateDataState(currentData());
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

	private BinaryData currentData() {
		Object value = getFieldModel().getValue();
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		// Do not emit the raw BinaryData as VALUE; refresh the download display state instead.
		_dataRevision++;
		updateDataState(newValue instanceof BinaryData ? (BinaryData) newValue : null);
	}

	private void updateDataState(BinaryData data) {
		putState(HAS_DATA, data != null);
		putState(DATA_REVISION, _dataRevision);
		putState(FILE_NAME, data != null ? data.getName() : null);
	}

	@Override
	public BinaryData getDownloadData(String key) {
		return currentData();
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
			Logger.error("Failed to process file upload.", ex, ReactBinaryFieldControl.class);
			putState(ERROR, ex.getMessage());
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
