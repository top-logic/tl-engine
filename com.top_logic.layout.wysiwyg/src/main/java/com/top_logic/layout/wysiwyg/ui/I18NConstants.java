/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey CANCEL_COMMAND_LABEL;

	public static ResKey ACCEPT_COMMAND_LABEL;

	public static ResKey OPTIONS_FIELD_LABEL;

	public static ResKey SEARCH_FIELD_LABEL;

	public static ResKey TITLE_FIELD_LABEL;

	public static ResKey CREATE_TL_OBJECT_LINK;

	public static ResKey OPEN_TL_OBJECT_LINK;

	public static ResKey OBJECT_NOT_FOUND;

	public static ResKey WRITE_LINK_ERROR;

	public static ResKey MULTIPLE_OBJECTS_SELECTED_ERROR;

	public static ResKey JSON_PARSING_ERROR;

	public static ResKey EDITOR_MERGE_ERROR;

	public static ResKey1 JSON_DEEP_COPY_ERROR__OBJ;

	public static ResKey UNSAFE_HTML_ERROR;

	public static ResPrefix CREATE_TL_OBJECT_LINK_PREFIX;

	public static ResKey1 IMAGE_NOT_FOUND__IMAGE_NAME;

	/**
	 * @en The images of an HTML text must be given as binary data, a list of binary data, or a
	 *     dictionary from image names to binary data, but got: {0}
	 */
	public static ResKey1 ERROR_INVALID_HTML_IMAGES__VALUE;

	static {
		initConstants(I18NConstants.class);
	}
}
