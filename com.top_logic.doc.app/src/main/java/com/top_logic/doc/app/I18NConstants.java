/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.TLObject;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** The prefix for the HTML input dialog. */
	public static ResPrefix IMPORT_DIALOG;

	/** {@link ResKey} of error message when an import was started without selecting a document */
	public static ResKey IMPORT_DOCUMENT_NO_DOCUMENT_SELECTED;

	/** {@link ResKey} of error message if there are errors during the import of a file */
	public static ResKey1 PAGE_ERRORS__FILE;

	/** {@link ResKey} of error message if an icon could not be written. */
	public static ResKey WRITE_ICON_ERROR;

	/** {@link ResKey} of error message if a {@link TLObject} of a link was not found */
	public static ResKey2 TLOBJECT_NOT_FOUND;
	
	/** {@link ResKey} of a hint text if a {@link TLObject} of a link was not found */
	public static ResKey TLOBJECT_NOT_FOUND_HINT;

	/** {@link ResKey} of error message if an image was not found */
	public static ResKey2 IMAGE_NOT_FOUND;
	
	/** {@link ResKey} of a hint text if an image was not found */
	public static ResKey IMAGE_NOT_FOUND_HINT;

	/** {@link ResKey} of error message if a table with a ticket list was found */
	public static ResKey TICKET_TABLE_FOUND;

	/**
	 * {@link ResKey} of error message if HTML documents containing images where imported without
	 * the images.
	 */
	public static ResKey IMAGES_NOT_IMPORTED;

	/** {@link ResKey} of an info message saying the import was without errors. */
	public static ResKey IMPORT_VALID;

	static {
		initConstants(I18NConstants.class);
	}
}
