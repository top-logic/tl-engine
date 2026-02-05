/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix LANGUAGE = legacyPrefix("language.");

	public static ResKey TRANSLATE;

	public static ResKey1 OPEN_SORT_DIALOG__PROPERTY;

	public static ResKey LESS_THAN_TWO_SELECTED;

	public static ResKey REMOVE_ELEMENT;

	public static ResKey1 ADD_ELEMENT__PROPERTY;

	public static ResKey CREATE_OBJECT;

	@CalledByReflection
	public static ResKey DELETE_OBJECT;

	public static ResKey NO_CONTENTS;

	public static ResKey ITEM_SELECTION_POPUP_EMPTY_ENTRY;

	public static ResKey2 JOIN__A_B;

	public static ResKey MAP_KEY_LABEL;

	public static ResKey1 KEY_NOT_UNIQUE__KEY;

	public static ResKey MAP_VALUE_LABEL;

	public static ResKey1 CHANGES_IN__ATTRIBUTE;

	public static ResPrefix CREATE_MAP_ENTRY_DIALOG;

	public static ResKey DISPLAY_DERIVED_RESOURCES;

	public static ResKey HIDE_DERIVED_RESOURCES;

	public static ResKey DERIVED_RESOURCE_TOOLTIP;

	/**
	 * @en Illegal file type {0}. Allowed types: {1}
	 */
	public static ResKey2 ILLEGAL_FILE_TYPE__FILENAME__ALLOWED_TYPES;

	public static ResKey1 ERROR_MANDATORY_PROPERTY_NOT_SET__PROPERTY;

	/**
	 * @en Collapse all
	 */
	public static ResKey COLLAPSE_ALL_ENTRIES_IN_LIST;

	/**
	 * @en Expand all
	 */
	public static ResKey EXPAND_ALL_ENTRIES_IN_LIST;

	/**
	 * @en In the descriptor {0} there is no title property with name {1}.
	 */
	public static ResKey2 ERROR_NO_TITLE_PROPERTY_IN_DESCRIPTOR__DESCRIPTOR_PROPERTY;

	/**
	 * @en No value set
	 */
	public static ResKey TOKEN_FIELD_NO_VALUE;

	/**
	 * @en Reset token
	 */
	public static ResKey CLEAR_TOKEN_FIELD_LABEL;

	/**
	 * @en Update token
	 */
	public static ResKey UPDATE_TOKEN_DIALOG_TITLE;

	/**
	 * @en You update the configuration with the token displayed. Make sure you copy it now. The
	 *     token will no longer be visible afterwards!
	 */
	public static ResKey UPDATE_TOKEN_DIALOG_MESSAGE;

	/**
	 * @en Token
	 */
	public static ResKey UPDATE_TOKEN_DIALOG_TOKEN_FIELD_LABEL;

	static {
		initConstants(I18NConstants.class);
	}
}
