/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * Adapter wrapping a {@link DataField} as a {@link DataFieldModel}.
 *
 * <p>
 * Extends {@link FormFieldAdapter} to inherit value, editability, and validation delegation. Adds
 * data-specific delegation for {@link DataFieldModel#getAcceptedTypes()},
 * {@link DataFieldModel#getMaxUploadSize()}, and {@link DataFieldModel#isMultipleFiles()}.
 * </p>
 */
public class DataFieldAdapter extends FormFieldAdapter implements DataFieldModel {

	private final DataField _dataField;

	/**
	 * Creates an adapter wrapping the given {@link DataField}.
	 *
	 * @param field
	 *        The data field to wrap.
	 */
	public DataFieldAdapter(DataField field) {
		super(field);
		_dataField = field;
	}

	@Override
	public String getAcceptedTypes() {
		return _dataField.getAcceptedTypes();
	}

	@Override
	public long getMaxUploadSize() {
		return _dataField.getMaxUploadSize();
	}

	@Override
	public boolean isMultipleFiles() {
		return _dataField.isMultiple();
	}

	/**
	 * The wrapped {@link DataField}.
	 */
	public DataField getDataField() {
		return _dataField;
	}
}
