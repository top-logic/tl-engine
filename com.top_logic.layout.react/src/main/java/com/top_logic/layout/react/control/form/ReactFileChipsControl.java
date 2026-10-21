/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A field holding several files, displayed as one chip per file.
 *
 * <p>
 * The field model holds the collection of files, each a {@link BinaryDataSource}; <code>null</code>
 * and the empty collection both mean no file. Each chip shows the name and the size of its file and
 * downloads it on click. While the field is {@link FieldModel#isEditable() editable}, each chip
 * offers to remove its file, and further files are added through a file picker selecting several
 * at once or by dropping them onto the control. An added file is appended to the collection, so
 * the files keep the order they were added in.
 * </p>
 *
 * <p>
 * The control serves the upload endpoint as an {@link UploadHandler}, taking every
 * {@link UploadSupport#FILE_PART file part} of the request, and the data endpoint as a
 * {@link DataProvider}, delivering the file of the chip whose key is requested. The files
 * themselves never enter the client state.
 * </p>
 *
 * <p>
 * The state carries the {@link #CHIPS}, the file types the file picker offers under
 * {@link UploadSupport#ACCEPT}, the upload limit under {@link UploadSupport#MAX_UPLOAD_SIZE}, by
 * which the client refuses a larger selection before transmitting it, and the message of a failed
 * upload under {@link UploadSupport#ERROR}, cleared by the next successful upload.
 * </p>
 *
 * @see ReactBinaryFieldControl
 */
public class ReactFileChipsControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	private static final String REACT_MODULE = "TLFileChips";

	/** Command sent by the client to remove one of the files, see {@link RemoveChipArguments}. */
	public static final String CMD_REMOVE_CHIP = "removeChip";

	/** State key for the chips, one per file and in file order. */
	public static final String CHIPS = "chips";

	/** Chip property for the key identifying the chip, see {@link RemoveChipArguments#KEY}. */
	public static final String CHIP_KEY = RemoveChipArguments.KEY;

	/** Chip property for the name of the file. */
	public static final String CHIP_NAME = "name";

	/** Chip property for the size of the file in bytes, absent if unknown. */
	public static final String CHIP_SIZE = "size";

	/** Chip property for whether the chip has a file to download. */
	public static final String CHIP_HAS_DATA = "hasData";

	/** The values of the field model, in the order of {@link #_keys}. */
	private List<Object> _values = List.of();

	/** The chip key of each of the {@link #_values}, stable for a value as long as it is displayed. */
	private List<String> _keys = List.of();

	private int _nextKey;

	/**
	 * Creates a {@link ReactFileChipsControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        Holds the collection of files, each a {@link BinaryDataSource}.
	 */
	public ReactFileChipsControl(ReactContext context, FieldModel model) {
		super(context, model, REACT_MODULE);
		// The files are displayed as chips; the collection itself is nothing the client draws.
		putState(FieldState.VALUE__PROP, null);
		putState(UploadSupport.MAX_UPLOAD_SIZE, UploadSupport.maxUploadSize());
		updateChips();
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

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		updateChips();
	}

	/**
	 * Rebuilds the chips from the collection the field model now holds.
	 *
	 * <p>
	 * A value that was displayed before keeps the key of its chip, so the client keeps the identity
	 * of the chip while files are added and removed around it. Values are told apart by identity,
	 * not by equality: two equal files are two chips.
	 * </p>
	 */
	private void updateChips() {
		Map<Object, String> previousKeys = new IdentityHashMap<>();
		for (int n = 0, cnt = _values.size(); n < cnt; n++) {
			previousKeys.put(_values.get(n), _keys.get(n));
		}

		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		List<String> keys = new ArrayList<>(values.size());
		List<Map<String, Object>> chips = new ArrayList<>(values.size());
		for (Object value : values) {
			// Removed, so that the same value occurring twice gets two keys.
			String key = previousKeys.remove(value);
			if (key == null) {
				key = String.valueOf(_nextKey++);
			}
			keys.add(key);
			chips.add(chip(key, value));
		}
		_values = values;
		_keys = keys;
		putState(CHIPS, chips);
	}

	private static Map<String, Object> chip(String key, Object value) {
		Map<String, Object> chip = new LinkedHashMap<>();
		chip.put(CHIP_KEY, key);
		if (value instanceof BinaryDataSource file) {
			chip.put(CHIP_NAME, file.getName());
			long size = file.getSize();
			chip.put(CHIP_SIZE, size >= 0 ? Long.valueOf(size) : null);
			chip.put(CHIP_HAS_DATA, Boolean.TRUE);
		} else {
			chip.put(CHIP_NAME, "");
			chip.put(CHIP_SIZE, null);
			chip.put(CHIP_HAS_DATA, Boolean.FALSE);
		}
		return chip;
	}

	@Override
	public BinaryData getDownloadData(String key) {
		int index = _keys.indexOf(key);
		if (index < 0) {
			return null;
		}
		Object value = _values.get(index);
		return value instanceof BinaryDataSource ? BinaryData.cast(value) : null;
	}

	/**
	 * Appends every uploaded file to the collection, in the order the client sent them.
	 *
	 * <p>
	 * A failed upload leaves the collection unchanged and reports
	 * {@link I18NConstants#FILE_UPLOAD_FAILED} under {@link UploadSupport#ERROR}; a successful upload
	 * clears it.
	 * </p>
	 */
	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		if (!acceptsClientValue()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		List<BinaryData> files;
		try {
			files = UploadSupport.toBinaryData(parts);
		} catch (IOException ex) {
			Logger.error("Failed to process file upload.", ex, ReactFileChipsControl.class);
			putState(UploadSupport.ERROR, Resources.getInstance().getString(I18NConstants.FILE_UPLOAD_FAILED));
			return HandlerResult.DEFAULT_RESULT;
		}
		putState(UploadSupport.ERROR, null);
		if (files.isEmpty()) {
			return HandlerResult.DEFAULT_RESULT;
		}
		List<Object> values = ListElementFieldModel.elementsOf(getFieldModel());
		values.addAll(files);
		getFieldModel().setValue(values);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Removes the file of the chip the client names.
	 */
	@ReactCommandHandler(CMD_REMOVE_CHIP)
	final void handleRemoveChip(RemoveChipArguments args) {
		if (!acceptsClientValue()) {
			return;
		}
		int index = _keys.indexOf(args.getKey());
		if (index < 0) {
			return;
		}
		List<Object> values = new ArrayList<>(_values);
		values.remove(index);
		getFieldModel().setValue(values);
	}

}
