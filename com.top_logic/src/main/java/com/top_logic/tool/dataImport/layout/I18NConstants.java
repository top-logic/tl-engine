/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERRORS = legacyKey("layout.dataimport.assistant.errors");

	public static ResKey ERROR_IMPORTER_BUSY = legacyKey("layout.dataimport.assistant.importerBusy");

	public static ResKey ERROR_IMPORTER_RUNNING = legacyKey("layout.dataimport.assistant.importerRunning");

	public static ResKey INFOS = legacyKey("layout.dataimport.assistant.infos");

	public static ResKey START_COMMITTING_DISABLED = legacyKey("layout.dataimport.assistant.startCommittingDisabled");

	public static ResKey START_PARSING_DISABLED = legacyKey("layout.dataimport.assistant.startParsingDisabled");

	public static ResKey WARNINGS = legacyKey("layout.dataimport.assistant.warnings");

	static {
		initConstants(I18NConstants.class);
	}
}
