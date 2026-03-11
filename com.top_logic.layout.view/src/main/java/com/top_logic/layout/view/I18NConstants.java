/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the view system.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Edit.
	 */
	public static ResKey FORM_EDIT;

	/**
	 * @en Apply.
	 */
	public static ResKey FORM_APPLY;

	/**
	 * @en Save.
	 */
	public static ResKey FORM_SAVE;

	/**
	 * @en Cancel.
	 */
	public static ResKey FORM_CANCEL;

	/**
	 * @en No object selected.
	 */
	public static ResKey FORM_NO_MODEL;

	static {
		initConstants(I18NConstants.class);
	}
}
