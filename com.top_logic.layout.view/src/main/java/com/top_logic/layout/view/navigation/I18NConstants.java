/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for displaying an object where the application shows objects of
 * its type.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Objects cannot be displayed from here.
	 */
	public static ResKey ERROR_CANNOT_DISPLAY_HERE;

	/**
	 * @en Only a model object can be displayed, but this is: {0}
	 */
	public static ResKey1 ERROR_NOT_A_MODEL_OBJECT__VALUE;

	/**
	 * @en The application displays no objects of type ''{0}''.
	 */
	public static ResKey1 ERROR_NO_DISPLAY_TARGET__TYPE;

	/**
	 * @en The view ''{0}'' is displayed as a dialog, so nothing can be displayed after it.
	 */
	public static ResKey1 ERROR_DIALOG_IS_NOT_LAST__VIEW;

	/**
	 * @en The view ''{0}'' is displayed as a drilled-down frame, but nothing here holds a stack of
	 *     them.
	 */
	public static ResKey1 ERROR_NO_TILE_STACK__VIEW;

	/**
	 * @en On the way to the view ''{1}'', ''{0}'' is not displayed.
	 */
	public static ResKey2 ERROR_CONTAINER_NOT_DISPLAYED__CONTAINER_VIEW;

	/**
	 * @en The view ''{0}'' is not displayed where it is expected.
	 */
	public static ResKey1 ERROR_VIEW_NOT_DISPLAYED__VIEW;

	/**
	 * @en The view ''{1}'' has no channel ''{0}'' to receive the object.
	 */
	public static ResKey2 ERROR_UNKNOWN_CHANNEL__CHANNEL_VIEW;

	/**
	 * @en The object cannot be displayed.
	 */
	public static ResKey ERROR_CANNOT_SHOW_OBJECT;

	static {
		initConstants(I18NConstants.class);
	}
}
