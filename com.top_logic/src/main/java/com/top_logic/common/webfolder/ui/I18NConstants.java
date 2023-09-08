/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Resources of this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Key to show when button to search for keywords is not executable
	 */
	public static ResKey KEYWORDS_SEARCH_NOT_POSSIBLE;

	/**
	 * Key to show when button to search for similar documents is not executable
	 */
	public static ResKey SIMILAR_DOCUMENT_SEARCH_NOT_POSSIBLE;

	public static ResKey FIELD_DISABLED;

	public static ResKey ZIP_DOWNLOAD_FOLDER_TOOLTIP;

	/** Constant used to to print the label of a document enlarged by the version of the document. */
	public static ResKey2 DOC_VERSION_LABEL__DOCLABEL_VERSION;

	/**
	 * Key to show an error message when an uploaded file name already exists
	 */
	public static ResKey1 FILE_NAME_ALREADY_EXISTS__NAME;

	/**
	 * Key to show an error message when an uploaded folder name already exists
	 */
	public static ResKey1 FOLDER_NAME_ALREADY_EXISTS__NAME;

	/**
	 * Key to show an error message when the maximum size of uploaded files was exceeded.
	 */
	public static ResKey1 MAXIMUM_SIZE_EXCEEDED__SIZE;

	/**
	 * Resource prefix for elements in the webfolder table.
	 */
	public static ResPrefix TABLE;

	static {
		initConstants(I18NConstants.class);
	}

}
