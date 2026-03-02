/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Message of a failing enqueue operation.
	 */
	public static ResKey1 ENQUEUE_FAILED__MSG;

	@CustomKey("export.table.dialog")
	public static ResKey EXPORT_TABLE_COMMAND;

	@CustomKey("export.failed.progress.exception")
	public static ResKey ERROR_INTERNAL_ERROR;

	@CustomKey("tl.command.export")
	public static ResKey EXPORT_COMMAND;

	@CustomKey("export.failed.ioexeption")
	public static ResKey ERROR_WRITE_FAILED;

	@CustomKey("export.disabled.download.noDocument")
	public static ResKey ERROR_NO_DOCUMENT;

	@CustomKey("export.failed.unexpected")
	public static ResKey ERROR_UNKNOWN_ERROR;

	@CustomKey("export.disabled.state.QUEUED")
	public static ResKey ERROR_IN_QUEUED_STATE;

	@CustomKey("export.disabled.state.RUNNING")
	public static ResKey ERROR_IN_RUNNING_STATE;

	static {
		initConstants(I18NConstants.class);
	}

}
