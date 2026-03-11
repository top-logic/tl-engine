/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

/**
 * {@link FieldModel} for binary/file upload fields.
 *
 * <p>
 * The field value is a {@link com.top_logic.basic.io.binary.BinaryData} (or {@code null} when
 * empty). Upload constraints (accepted types, max size, multiple files) are expressed as properties
 * on the model so that controls can configure their UI accordingly.
 * </p>
 */
public interface DataFieldModel extends FieldModel {

	/**
	 * Accepted MIME types or file extensions (e.g. {@code "image/*,.pdf"}).
	 *
	 * @return The accepted types filter, or {@code null} to accept all types.
	 */
	String getAcceptedTypes();

	/**
	 * Maximum upload size in bytes.
	 *
	 * @return The max upload size, or {@code 0} for no limit.
	 */
	long getMaxUploadSize();

	/**
	 * Whether multiple files can be uploaded simultaneously.
	 */
	boolean isMultipleFiles();
}
