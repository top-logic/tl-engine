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
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.DataFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A {@link ReactFormFieldControl} for binary ({@link BinaryDataSource}) attributes.
 *
 * <p>
 * The field value may be any {@link BinaryDataSource}, a {@link BinaryData} being the usual case.
 * Another source is converted to a {@link BinaryData} where its content is needed.
 * </p>
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

	/** State key indicating whether download data is available. */
	private static final String HAS_DATA = "hasData";

	/** State key whose value increments each time the data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	/** State key for the suggested file name. */
	private static final String FILE_NAME = "fileName";

	private int _dataRevision;

	/** The size limit of one uploaded file, see {@link #maxUploadSize()}. */
	private long _maxUploadSize;

	/**
	 * Creates a new {@link ReactBinaryFieldControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The field model. Value type is {@link BinaryDataSource}, usually a {@link BinaryData}.
	 *        If the model is a {@link DataFieldModel}, its accepted types and its
	 *        {@link #setMaxUploadSize(long) upload limit} apply.
	 */
	public ReactBinaryFieldControl(ReactContext context, FieldModel model) {
		super(context, model, "TLBinaryField");
		// The base constructor seeded the value with the raw binary value; drop it (not JSON-serializable)
		// and emit the download/upload display state instead.
		putState(FieldState.VALUE__PROP, null);
		putState(STATUS, "idle");
		setMaxUploadSize(0);
		updateDataState(currentData());
		initDataFieldState();
	}

	private void initDataFieldState() {
		if (getFieldModel() instanceof DataFieldModel) {
			DataFieldModel dataModel = (DataFieldModel) getFieldModel();
			setAcceptedTypes(dataModel.getAcceptedTypes());
			setMaxUploadSize(dataModel.getMaxUploadSize());
		}
	}

	/**
	 * Limits the size of each uploaded file to the given value, capped by the application-wide
	 * {@link UploadSupport#maxUploadSize() limit}.
	 *
	 * @param fieldLimit
	 *        The maximum size in bytes of one file the field accepts, <code>0</code> for no limit of
	 *        its own.
	 *
	 * @see UploadSupport#effectiveMaxUploadSize(long)
	 */
	public void setMaxUploadSize(long fieldLimit) {
		_maxUploadSize = UploadSupport.effectiveMaxUploadSize(fieldLimit);
		putState(UploadSupport.MAX_UPLOAD_SIZE, Long.valueOf(_maxUploadSize));
	}

	@Override
	public long maxUploadSize() {
		return _maxUploadSize;
	}

	/**
	 * Restricts the files the file picker offers to the given types.
	 *
	 * @param acceptedTypes
	 *        Comma separated list of MIME types like <code>image/*</code> and file patterns like
	 *        <code>*.xml</code>, see {@link UploadSupport#acceptAttribute(String)}; <code>null</code>
	 *        to offer every file.
	 */
	public void setAcceptedTypes(String acceptedTypes) {
		putState(UploadSupport.ACCEPT, UploadSupport.acceptAttribute(acceptedTypes));
	}

	private BinaryData currentData() {
		return toData(getFieldModel().getValue());
	}

	/**
	 * The {@link BinaryData} of the given field value, <code>null</code> if the value is not a
	 * {@link BinaryDataSource}.
	 */
	private static BinaryData toData(Object value) {
		return value instanceof BinaryDataSource ? BinaryData.cast(value) : null;
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		// Do not emit the raw binary value; refresh the download display state instead.
		_dataRevision++;
		updateDataState(toData(newValue));
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
				putState(UploadSupport.ERROR,
					Resources.getInstance().getString(I18NConstants.ERROR_NO_FILE_DATA_RECEIVED));
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

			putState(UploadSupport.ERROR, null);
			putState(STATUS, "idle");
		} catch (IOException ex) {
			Logger.error("Failed to process file upload.", ex, ReactBinaryFieldControl.class);
			putState(UploadSupport.ERROR, Resources.getInstance().getString(I18NConstants.FILE_UPLOAD_FAILED));
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
