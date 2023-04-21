/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey COMMAND_MODEL_DEACTIVATED_IN_DESIGN_MODE;

	public static ResKey FORM_EDITOR__NO_MODEL_TYPE;

	public static ResKey FORM_EDITOR__TOOL_NEW_TABLE;

	public static ResKey MACRO_PART_LABEL;

	public static ResKey FOREIGN_ATTRIBUTE_LABEL;

	public static ResKey1 FOREIGN_ATTRIBUTE_DISABLED_NO_MODEL__ATTRIBUTE;

	public static ResKey FOREIGN_OBJECTS_LABEL;

	public static ResKey FOREIGN_OBJECTS_NO_DISPLAY_DEFINED;

	public static ResKey FOREIGN_OBJECTS_NO_TYPE_DEFINED;

	public static ResKey1 FOREIGN_OBJECTS_LEGEND_KEY_PREVIEW__TYPE;

	/**
	 * @en Rendered objects
	 */
	public static ResKey RENDERED_OBJECTS_LABEL;

	static {
		initConstants(I18NConstants.class);
	}

}
