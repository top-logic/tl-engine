/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * File-chip presentation of an inline-edited composition reference within a form.
 *
 * <p>
 * Renders each composed object as a chip (file-type icon, name, size, download) via the
 * {@code TLFileChips} React component. In edit mode, each chip carries a remove button and the
 * control accepts uploads (file picker and drag-and-drop); every uploaded file becomes a new
 * composed object with the file stored in its binary attribute. The editing lifecycle is handled
 * by {@link AbstractCompositionControl}.
 * </p>
 *
 * <p>
 * The binary attribute of the composed type is configurable; without configuration, the single
 * binary-typed property of the composition target type is used. A chip's name is the composed
 * object's label.
 * </p>
 */
public class FileListControl extends AbstractCompositionControl implements UploadHandler, DataProvider {

	private static final String REACT_MODULE = "TLFileChips";

	/** State key for the chip list. */
	private static final String CHIPS = "chips";

	/** State key for the edit-mode flag. */
	private static final String EDITABLE = "editable";

	/** Chip property for the stable chip key. */
	private static final String CHIP_KEY = "key";

	/** Chip property for the display name. */
	private static final String CHIP_NAME = "name";

	/** Chip property for the file size in bytes. */
	private static final String CHIP_SIZE = "size";

	/** Chip property for the download availability. */
	private static final String CHIP_HAS_DATA = "hasData";

	/** Command name for {@link #handleRemoveChip(Map)}. */
	private static final String CMD_REMOVE_CHIP = "removeChip";

	/** Name of the multipart part carrying an uploaded file. */
	private static final String PART_FILE = "file";

	private final String _dataAttributeName;

	/** Stable chip keys per row object, so client identity survives state updates. */
	private final Map<TLObject, String> _rowKeys = new HashMap<>();

	/** Reverse lookup from chip key to row object, for download and remove requests. */
	private final Map<String, TLObject> _keyRows = new HashMap<>();

	private int _nextKey;

	/** The rows currently shown, in both view and edit mode. */
	private List<TLObject> _currentRows = List.of();

	/** The chrome wrapping this control, whose label follows the composition attribute. */
	private ReactFormFieldChromeControl _chrome;

	/**
	 * Creates a new {@link FileListControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param formControl
	 *        The parent form control managing the editing lifecycle.
	 * @param compositionAttributeName
	 *        The name of the composition reference attribute on the parent object.
	 * @param dataAttributeName
	 *        The name of the binary attribute on the composed type, or {@code null} to use the
	 *        single binary-typed property of the composition target type.
	 */
	public FileListControl(ReactContext context, FormControl formControl,
			String compositionAttributeName, String dataAttributeName) {
		super(context, formControl, compositionAttributeName, REACT_MODULE);
		_dataAttributeName = dataAttributeName;
	}

	/**
	 * Sets the chrome wrapping this control. Its label is kept in sync with the composition
	 * attribute's display label.
	 */
	public void setChrome(ReactFormFieldChromeControl chrome) {
		_chrome = chrome;
		updateChromeLabel();
	}

	@Override
	protected void buildContent(List<? extends TLObject> rows, boolean editMode) {
		_currentRows = new ArrayList<>(rows);
		putState(EDITABLE, editMode);
		updateChromeLabel();
		updateChips();
	}

	private void updateChromeLabel() {
		if (_chrome == null) {
			return;
		}
		TLStructuredTypePart part = compositionPart();
		String label = part != null ? MetaLabelProvider.INSTANCE.getLabel(part) : null;
		_chrome.setLabel(label != null ? label : compositionAttributeName());
	}

	@Override
	protected void refreshRows() {
		_currentRows = new ArrayList<>(fieldModel().getCurrentList());
		updateChips();
	}

	private void updateChips() {
		pruneKeys();
		List<Map<String, Object>> chips = new ArrayList<>();
		for (TLObject row : _currentRows) {
			BinaryData data = rowData(row);
			Map<String, Object> chip = new LinkedHashMap<>();
			chip.put(CHIP_KEY, keyFor(row));
			chip.put(CHIP_NAME, MetaLabelProvider.INSTANCE.getLabel(row));
			chip.put(CHIP_SIZE, data != null ? data.getSize() : null);
			chip.put(CHIP_HAS_DATA, data != null);
			chips.add(chip);
		}
		putState(CHIPS, chips);
	}

	/** Drops key mappings of rows no longer shown. */
	private void pruneKeys() {
		_rowKeys.keySet().retainAll(_currentRows);
		_keyRows.values().retainAll(_currentRows);
	}

	private String keyFor(TLObject row) {
		return _rowKeys.computeIfAbsent(row, r -> {
			String key = String.valueOf(_nextKey++);
			_keyRows.put(key, r);
			return key;
		});
	}

	/**
	 * The binary value of the given row, or {@code null} if the row has none.
	 */
	private BinaryData rowData(TLObject row) {
		TLStructuredTypePart part = dataPart(row);
		if (part == null) {
			return null;
		}
		Object value = row.tValue(part);
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

	/**
	 * The binary attribute on the given row's type: the configured attribute, or the single
	 * binary-typed property of the type.
	 */
	private TLStructuredTypePart dataPart(TLObject row) {
		if (_dataAttributeName != null) {
			return row.tType().getPart(_dataAttributeName);
		}
		TLStructuredTypePart found = null;
		for (TLStructuredTypePart part : ((TLClass) row.tType()).getAllParts()) {
			if (isBinary(part)) {
				if (found != null) {
					// Ambiguous - require explicit configuration.
					return null;
				}
				found = part;
			}
		}
		return found;
	}

	private static boolean isBinary(TLStructuredTypePart part) {
		return part.getType() instanceof TLPrimitive primitive
			&& primitive.getKind() == TLPrimitive.Kind.BINARY;
	}

	// -- Download --

	@Override
	public BinaryData getDownloadData(String key) {
		TLObject row = _keyRows.get(key);
		return row != null ? rowData(row) : null;
	}

	// -- Upload --

	@Override
	public HandlerResult handleUpload(DisplayContext context, Collection<Part> parts) {
		if (fieldModel() == null) {
			// Not in edit mode - ignore stray uploads.
			return HandlerResult.DEFAULT_RESULT;
		}
		try {
			for (Part filePart : parts) {
				if (!PART_FILE.equals(filePart.getName())) {
					continue;
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

				addRow(row -> {
					TLStructuredTypePart dataPart = dataPart(row);
					if (dataPart != null) {
						row.tUpdate(dataPart, data);
					}
				});
			}
		} catch (IOException ex) {
			Logger.error("Failed to process file upload.", ex, FileListControl.class);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	// -- Remove --

	/**
	 * Removes the chip identified by the {@value #CHIP_KEY} argument from the composition.
	 */
	@ReactCommandHandler(CMD_REMOVE_CHIP)
	void handleRemoveChip(Map<String, Object> arguments) {
		if (fieldModel() == null) {
			return;
		}
		Object key = arguments.get(CHIP_KEY);
		TLObject row = key != null ? _keyRows.get(key.toString()) : null;
		if (row == null) {
			return;
		}
		int index = fieldModel().getCurrentList().indexOf(row);
		if (index >= 0) {
			deleteRow(row, index);
		}
	}
}
