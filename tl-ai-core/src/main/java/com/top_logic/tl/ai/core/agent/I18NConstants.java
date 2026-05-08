/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * I18N constants for the agent package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when an agent or skill name uses the reserved {@code TL_} prefix.
	 *
	 * @en The prefix 'TL_' is reserved for TopLogic platform definitions: {0}
	 */
	public static ResKey1 RESERVED_PREFIX_ERROR__NAME;

	/**
	 * Error message when an agent or skill name is already in use.
	 *
	 * @en The name '{0}' is already in use.
	 * @de Der Name '{0}' wird bereits verwendet.
	 */
	public static ResKey1 DUPLICATE_NAME_ERROR__NAME;

	/**
	 * Success message after exporting instances to the data folder.
	 *
	 * @en Instances exported to development environment.
	 * @de Instanzen in die Entwicklungsumgebung übernommen.
	 */
	public static ResKey EXPORT_TO_DEV_ENV_SUCCESS;

	/**
	 * Error message when exporting instances to the data folder fails.
	 *
	 * @en Failed to export instances to development environment.
	 * @de Fehler beim Übernehmen der Instanzen in die Entwicklungsumgebung.
	 */
	public static ResKey EXPORT_TO_DEV_ENV_ERROR;

	static {
		initConstants(I18NConstants.class);
	}
}
