/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;

/**
 * I18N keys for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The title of a {@link MessageBox} dialog of type {@link MessageType#INFO}.
	 */
	public static ResKey INFO_TITLE;
	
	/**
	 * The title of a {@link MessageBox} dialog of type {@link MessageType#ERROR}.
	 */
	public static ResKey ERROR_TITLE;
	
	/**
	 * The title of a {@link MessageBox} dialog of type {@link MessageType#CONFIRM}.
	 */
	public static ResKey CONFIRM_TITLE;
	
	/**
	 * The title of a {@link MessageBox} dialog of type {@link MessageType#WARNING}.
	 */
	public static ResKey WARNING_TITLE;
	
	/**
	 * The button label a {@link ButtonType#OK} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_OK_LABEL;
	
	/**
	 * The button label a {@link ButtonType#YES} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_YES_LABEL;
	
	/**
	 * The button label a {@link ButtonType#CONTINUE} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_CONTINUE_LABEL;
	
	/**
	 * The button label a {@link ButtonType#CANCEL} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_CANCEL_LABEL;
	
	/**
	 * The button label a {@link ButtonType#NO} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_NO_LABEL;
	
	/**
	 * The button label a {@link ButtonType#CLOSE} button in a {@link MessageBox} dialog.
	 */
	public static ResKey BUTTON_CLOSE_LABEL;

	/**
	 * The theme image key a {@link MessageBox} dialog of type {@link MessageType#INFO}.
	 */
	public static ResPrefix INFO_TYPE;

	/**
	 * The theme image key a {@link MessageBox} dialog of type {@link MessageType#ERROR}.
	 */
	public static ResPrefix ERROR_TYPE;
	
	/**
	 * The theme image key a {@link MessageBox} dialog of type {@link MessageType#ERROR}.
	 */
	public static ResPrefix SYSTEM_FAILURE_TYPE;
	
	/**
	 * The theme image key a {@link MessageBox} dialog of type {@link MessageType#CONFIRM}.
	 */
	public static ResPrefix CONFIRM_TYPE;
	
	/**
	 * The theme image key a {@link MessageBox} dialog of type {@link MessageType#WARNING}.
	 */
	public static ResPrefix WARNING_TYPE;
	
	/**
	 * Text alternative for the type image in a {@link MessageBox} dialog of type {@link MessageType#INFO}.
	 */
	public static ResKey INFO_TYPE_NAME;
	
	/**
	 * Text alternative for the type image in a {@link MessageBox} dialog of type {@link MessageType#ERROR}.
	 */
	public static ResKey ERROR_TYPE_NAME;
	
	/**
	 * Text alternative for the type image in a {@link MessageBox} dialog of type {@link MessageType#CONFIRM}.
	 */
	public static ResKey CONFIRM_TYPE_NAME;
	
	/**
	 * Text alternative for the type image in a {@link MessageBox} dialog of type {@link MessageType#WARNING}.
	 */
	public static ResKey WARNING_TYPE_NAME;

	public static ResKey SYSTEM_FAILURE_TITLE;

	public static ResKey SYSTEM_FAILURE_TYPE_NAME;

	public static ResKey ERROR_TASK_FAILED;

	/**
	 * @en The background task has not finished within the specified wait timeout.
	 */
	public static ResKey ERROR_PROGRESS_NOT_FINISHED_WITHIN_TIMEOUT;

	static {
		initConstants(I18NConstants.class);
	}
}
