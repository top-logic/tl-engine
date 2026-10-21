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
 * {@link DataFieldModel}, its accepted types are sent to the client and its
 * {@link #setMaxUploadSize(long) upload limit} applies.
 * </p>
 */
public class ReactFileUploadControl extends ReactFormFieldControl implements UploadHandler {

	/** State key for the current status. */
	private static final String STATUS = "status";


	/** State key for the accepted MIME type filter. */
	private static final String ACCEPT = "accept";

	/** The size limit of one uploaded file, see {@link #maxUploadSize()}. */
	private long _maxUploadSize;

	/**
	 * Creates a new {@link ReactFileUploadControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param model
	 *        The field model. Value type is {@link BinaryData}. If the model is a
	 *        {@link DataFieldModel}, its accepted types and its {@link #setMaxUploadSize(long) upload
	 *        limit} apply.
	 */
	public ReactFileUploadControl(ReactContext context, FieldModel model) {
		super(context, model, "TLFileUpload");
		putState(STATUS, "idle");
		setMaxUploadSize(0);
		initDataFieldState();
	}

	private void initDataFieldState() {
		if (getFieldModel() instanceof DataFieldModel) {
			DataFieldModel dataModel = (DataFieldModel) getFieldModel();
			putState(ACCEPT, dataModel.getAcceptedTypes());
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

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		putState(STATUS, "received");
		try {
			Part filePart = parts.stream()
				.filter(p -> UploadSupport.FILE_PART.equals(p.getName()))
				.findFirst()
				.orElse(null);

			if (filePart == null) {
				putState(UploadSupport.ERROR, "No file data received.");
				putState(STATUS, "idle");
				return HandlerResult.DEFAULT_RESULT;
			}

			BinaryData data = UploadSupport.toBinaryData(filePart);
			getFieldModel().setValue(data);

			putState(UploadSupport.ERROR, null);
			putState(STATUS, "idle");
		} catch (IOException ex) {
			Logger.error("Failed to process file upload.", ex, ReactFileUploadControl.class);
			putState(UploadSupport.ERROR, ex.getMessage());
			putState(STATUS, "idle");
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
