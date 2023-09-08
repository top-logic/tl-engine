/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Messages for package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {
	
	public static ResKey FAILED_TO_PARSE_SCRIPTED_TEST_TITLE;

	public static ResKey2 FAILED_TO_PARSE_SCRIPTED_TEST__FILE_ERROR;

	public static ResKey SAVE_SCRIPT_ERROR_NO_SCRIPT_FILE;

	public static ResKey SAVE_SCRIPT_ERROR_SCRIPT_NOT_SAVABLE;

	public static ResPrefix VARIABLE_NAME_DIALOG;

	public static ResKey ERROR_NOT_RECORDING;

	public static ResKey CLIPBOARD_EMPTY;

	public static ResKey ERROR_CURRENTLY_RECORDING;

	public static ResKey ERROR_NO_REPLAY_RUNNING;

	public static ResKey ERROR_REPLAY_RUNNING;

	public static ResKey ERROR_DERIVED_NODE_CANNOT_BE_EDITED;

	public static ResKey ERROR_CANNOT_GROUP_ROOT;

	public static ResKey ERROR_CANNOT_UNGROUP_ROOT;

	public static ResKey ERROR_CANNOT_UNGROUP_TOPLEVEL_NODE;

	public static ResKey ERROR_NO_ACTION_SELECTED;

	public static ResKey UPLOAD_DIALOG_NO_FILE_UPLOADED;

	public static ResKey1 UPLOAD_DIALOG_SCRIPT_NOT_VALID__ERROR;

	public static ResKey RESUME_SCRIPT_EXECUTION;

	static {
		initConstants(I18NConstants.class);
	}

}
