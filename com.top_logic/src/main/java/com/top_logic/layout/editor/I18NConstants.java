/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NODE_HAS_NO_TEMPLATE_NAME;

	public static ResKey1 DEFINITION_NOT_FOUND__TEMPLATE;

	public static ResKey2 TABBAR_UPDATE_ERROR__TAB_TABBAR;

	public static ResKey DEFAULT_DYNAMIC_COMPONENT_LABEL;

	public static ResKey1 ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT;

	/**
	 * Error message if a layout could not be resolved for a given layout key.
	 */
	public static ResKey1 LAYOUT_RESOLVE_ERROR;

	/**
	 * Error message if a layout file could not be read for a given path.
	 */
	public static ResKey LAYOUT_FILE_ERROR;

	/**
	 * {@link ResKey} when the model type could not be resolved.
	 */
	public static ResKey MODEL_TYPE_NOT_RESOLVED_ERROR;

	/**
	 * Error message if the template arguments {@link ConfigurationItem} could not be deserialized.
	 */
	public static ResKey3 TEMPLATE_DESERIALIZATION_ERROR__ARGUMENTS_TEMPLATE_KEY;

	/**
	 * Error message if a layout component could not be replaced.
	 */
	public static ResKey1 REPLACE_COMPONENT_ERROR__LAYOUT_KEY;

	/**
	 * Error message if the layout template body configuration could not be parsed.
	 */
	public static ResKey INVALID_LAYOUT_TEMPLATE_BODY;

	/**
	 * Error detail message for {@link #INVALID_LAYOUT_TEMPLATE_BODY}.
	 */
	public static ResKey1 INVALID_LAYOUT_TEMPLATE_BODY_DETAILS__TEMPLATE;

	/**
	 * Dynamic components have a {@link ResPrefix} dependent on the typed template from which they
	 * were instantiated.
	 */
	public static ResPrefix DYNAMIC_COMPONENT;

	static {
		initConstants(I18NConstants.class);
	}
}
