/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.importer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Importing data...
	 */
	public static ResKey IMPORT_PROGRESS;

	/**
	 * @en Parsing data.
	 */
	public static ResKey PARSING_DATA;

	/**
	 * @en Importing objects.
	 */
	public static ResKey IMPORTING_OBJECTS;

	/**
	 * @en Committing changes.
	 */
	public static ResKey COMMITTING_CHANGES;

	/**
	 * @en Import finished.
	 */
	public static ResKey IMPORT_FINISHED;

	/**
	 * @en Imported objects.
	 */
	public static ResKey IMPORTED_OBJECTS;

	static {
		initConstants(I18NConstants.class);
	}
}
