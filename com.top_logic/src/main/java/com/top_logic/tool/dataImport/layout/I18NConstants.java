/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("layout.dataimport.assistant.errors")
	public static ResKey ERRORS;

	@CustomKey("layout.dataimport.assistant.importerBusy")
	public static ResKey ERROR_IMPORTER_BUSY;

	@CustomKey("layout.dataimport.assistant.importerRunning")
	public static ResKey ERROR_IMPORTER_RUNNING;

	@CustomKey("layout.dataimport.assistant.infos")
	public static ResKey INFOS;

	@CustomKey("layout.dataimport.assistant.startCommittingDisabled")
	public static ResKey START_COMMITTING_DISABLED;

	@CustomKey("layout.dataimport.assistant.startParsingDisabled")
	public static ResKey START_PARSING_DISABLED;

	@CustomKey("layout.dataimport.assistant.warnings")
	public static ResKey WARNINGS;

	static {
		initConstants(I18NConstants.class);
	}
}
