/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * I18N constants of the chat view elements.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Title of the rail's pane while it shows several objects rather than one.
	 *
	 * <p>
	 * One object titles the pane with its own label; a set of them has no label of its own, and the
	 * count is the one true thing that can be said about it without reading the objects.
	 * </p>
	 *
	 * @en {0} objects
	 * @de {0} Objekte
	 */
	public static ResKey1 PANE_TITLE_OBJECTS__COUNT;

	/**
	 * Title of the rail's pane while it shows a table or a chart of objects of one type, and the
	 * agent named no title of its own.
	 *
	 * @en {0} ({1})
	 * @de {0} ({1})
	 */
	public static ResKey2 PANE_TITLE_TYPE__TYPE_COUNT;

	static {
		initConstants(I18NConstants.class);
	}
}
