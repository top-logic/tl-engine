/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.util.List;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Creates the values of a field from files the user uploads.
 *
 * <p>
 * Offered by a {@link ReactFieldControlProvider} whose values can be created from files, see
 * {@link ReactFieldControlProvider#uploadValues(FieldSpec)}. A field holding several such values
 * takes several files in one gesture - a file picker selecting several files, files dropped onto
 * the field - and appends the values created from them.
 * </p>
 */
@FunctionalInterface
public interface UploadValueFactory {

	/**
	 * The values created from the given files.
	 *
	 * @param files
	 *        The uploaded files, in the order the user selected them.
	 * @return The values to append to the field, in the order they are appended; may be fewer than
	 *         the files where a file makes no value.
	 */
	List<?> createValues(List<BinaryData> files);

}
