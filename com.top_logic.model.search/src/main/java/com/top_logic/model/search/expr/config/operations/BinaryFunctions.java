/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryDataSource;

/**
 * TL-Script functions describing a binary value.
 *
 * <p>
 * A binary value is the contents of an uploaded file, a document attribute, or the result of a
 * function such as <code>binary()</code>. Its name is available through <code>label()</code>, while
 * the functions here give access to the other properties of the value.
 * </p>
 */
@ScriptPrefix("binary")
public class BinaryFunctions extends TLScriptFunctions {

	/**
	 * The content type of the given binary value.
	 *
	 * <p>
	 * The content type is the MIME type of the contents, such as <code>image/png</code> or
	 * <code>application/pdf</code>. It may carry additional parameters, such as
	 * <code>text/plain; charset=utf-8</code>, so a check for a certain kind of contents is best
	 * written as <code>$data.binaryContentType().startsWith("image/")</code>.
	 * </p>
	 *
	 * @param data
	 *        The binary value to inspect.
	 * @return The content type of the value, or <code>null</code> if no value is given.
	 */
	@Label("Content type of a binary value")
	@SideEffectFree
	public static String contentType(@Mandatory BinaryDataSource data) {
		return data == null ? null : data.getContentType();
	}

	/**
	 * The size of the given binary value in bytes.
	 *
	 * <p>
	 * Not every binary value knows its size in advance. A value whose contents are produced on
	 * demand reports <code>-1</code>.
	 * </p>
	 *
	 * @param data
	 *        The binary value to inspect.
	 * @return The number of bytes of the value, <code>-1</code> if the size is not known, or
	 *         <code>null</code> if no value is given.
	 */
	@Label("Size of a binary value")
	@SideEffectFree
	public static Long size(@Mandatory BinaryDataSource data) {
		return data == null ? null : Long.valueOf(data.getSize());
	}

}
