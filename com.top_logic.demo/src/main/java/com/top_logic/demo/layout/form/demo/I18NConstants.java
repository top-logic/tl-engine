/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix CONTEXT = legacyPrefix("context");

	public static ResPrefix CONTROLS = legacyPrefix("controls");

	public static ResPrefix MESSAGES = legacyPrefix("messages");

	public static ResPrefix TEST = legacyPrefix("demo.form.test");

	public static ResPrefix TICKET_2823_TABLE;

	public static ResPrefix TICKET_2470_TABLE;

	/** @see I18NConstantsBase */
	public static ResKey NOT_EXECUTABLE_REASON;
	
	public static ResKey NO_VALUE = legacyKey("controls.upload5IndividualNoValueText.noValue");

	public static ResKey ONLY = legacyKey("controls.upload.xml.only");

	public static ResKey2 ILLEGAL_COLUMN_INDEX__MIN__MAX;

	public static ResKey TEST_ERROR_TITLE;

	public static ResKey TEST_ERROR_DESCRIPTION;

	public static ResKey1 TEST_ERROR_TITLE__VALUE;

	public static ResKey DISPLAY_SELECTED_DATE;

	public static ResKey TABLE_COLUMN_NAME_TITLE;

	public static ResKey TABLE_COLUMN_CUSTOM_TITLE;

	public static ResKey CUSTOM_SELECT_DIALOG_DESCRIPTION;

	public static ResPrefix CUSTOM_SELECT_DIALOG_TABLES;

	public static ResPrefix CUSTOM_TABLE_SELECT_DIALOG_GROUP;

	public static ResPrefix CUSTOM_TABLE_SELECT_DIALOG_NAME_COLUMN_GROUP;

	public static ResPrefix CUSTOM_TABLE_SELECT_DIALOG_CUSTOM_COLUMN_GROUP;

	public static ResPrefix CUSTOM_TREE_SELECT_DIALOG_GROUP;

	public static ResPrefix CUSTOM_TREE_SELECT_DIALOG_NAME_COLUMN_GROUP;

	public static ResPrefix CUSTOM_TREE_SELECT_DIALOG_CUSTOM_COLUMN_GROUP;

	public static ResKey PROGRESS_TITLE;

	public static ResKey1 PROGRESS_MESSAGE__NUM;
	
	public static ResPrefix DROPDOWN_ITEM;

	static {
		initConstants(I18NConstants.class);
	}
}
