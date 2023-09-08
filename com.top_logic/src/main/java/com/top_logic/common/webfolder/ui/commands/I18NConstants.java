/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

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
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {


	public static ResPrefix ZIP_DOWNLOAD_FOLDER_DIALOG;
	public static ResKey ZIP_DOWNLOAD_FOLDER;

	public static ResKey ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED_EMPTY;

	public static ResKey1 ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED__LIMIT;

	public static ResKey2 ZIP_DOWNLOAD_FOLDER_DIALOG_MESSAGE__DOCUMENTS_SIZE;

	public static ResKey CREATE_FOLDER;
	
	public static ResKey1 CONFIRM_DELETE_FOLDER__NAME;
	public static ResKey1 CONFIRM_DELETE_DOCUMENT__NAME;
	public static ResKey1 CONFIRM_DELETE_LINK__NAME;

	public static ResKey LOCK_NOT_POSSIBLE_BECAUSE_ALREADY_LOCKED;

	/**
	 * Resource prefix for elements in the dialog for creating a new folder.
	 */
	public static ResPrefix NEW_FOLDER_DIALOG;

	/**
	 * Resource prefix for elements in the dialog for updating a document.
	 */
	public static ResPrefix UPDATE_DIALOG;
	public static ResKey UPDATE_DOCUMENT;
	public static ResKey UPLOAD_DOCUMENT_NO_DOCUMENT_SELECTED;

	public static ResKey UPDATE_DIALOG_CLOSING_FAILED_BECAUSE_UNLOCKING_FAILED;

	public static ResKey UPDATE_NOT_POSSIBLE_BECAUSE_LOCKED;

	public static ResKey UPDATE_NOT_POSSIBLE_BECAUSE_LOCKING_FAILED;

	/**
	 * Resource prefix for elements in the dialog for uploading a document.
	 */
	public static ResPrefix UPLOAD_DIALOG;
	public static ResKey UPLOAD_DOCUMENT;

	/**
	 * Resource prefix for elements in the dialog for showing the versions of a document.
	 */
	public static ResPrefix VERSION_DIALOG;

	public static ResKey1 ERROR_FOLDER_EXISTS__NAME;
	public static ResKey1 ERROR_FOLDER_CREATE__NAME;
	public static ResKey1 ERROR_DOCUMENT_EXISTS__NAME;

	public static ResKey MSG_NOT_LOCKED;
	public static ResKey1 MSG_ALREADY_LOCKED__USER;

	public static ResKey MSG_ALREADY_IN_CLIPBOARD;

	public static ResKey MSG_DOCUMENT_LOCKED;

	public static ResKey MSG_FOLDER_NOT_EMPTY;
	
	static {
		initConstants(I18NConstants.class);
	}
}
