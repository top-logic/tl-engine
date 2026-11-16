/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * The commit messages the view tools record their calls with.
 *
 * @see com.top_logic.tl.ai.tool.api.Tool#commitMessage(java.util.Map,
 *      com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Commit message of an agent tool call: view written.
	 *
	 * @en View ''{0}'' saved
	 * @de Ansicht „{0}“ gespeichert
	 * @see ViewTools#writeView
	 */
	public static ResKey1 VIEW_WRITTEN__PATH;

	/**
	 * Commit message of an agent tool call: view edited.
	 *
	 * @en View ''{0}'' edited
	 * @de Ansicht „{0}“ bearbeitet
	 * @see ViewTools#editView
	 */
	public static ResKey1 VIEW_EDITED__PATH;

	/**
	 * Commit message of an agent tool call: view deleted.
	 *
	 * @en View ''{0}'' deleted
	 * @de Ansicht „{0}“ gelöscht
	 * @see ViewTools#deleteView
	 */
	public static ResKey1 VIEW_DELETED__PATH;

	/**
	 * Commit message of an agent tool call: view change reverted.
	 *
	 * @en View change undone
	 * @de Änderung an Ansichten zurückgenommen
	 * @see ViewTools#revertViewChange
	 */
	public static ResKey VIEW_CHANGE_REVERTED;

	static {
		initConstants(I18NConstants.class);
	}
}
