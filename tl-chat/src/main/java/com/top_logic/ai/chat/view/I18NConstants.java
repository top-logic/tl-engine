/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
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

	/**
	 * Title of the rail's pane while it shows a chart or a table of data the agent computed, and the
	 * agent named no title of its own.
	 *
	 * @en Analysis
	 * @de Auswertung
	 */
	public static ResKey PANE_TITLE_DATA;

	/**
	 * Header of the column of a data table that names each row, for data whose labels carry no
	 * name of their own.
	 *
	 * @en Label
	 * @de Bezeichnung
	 */
	public static ResKey PANE_COLUMN_LABEL;

	/**
	 * Header of the column of a data table holding the one series of numbers, when nothing names it.
	 *
	 * @en Value
	 * @de Wert
	 */
	public static ResKey PANE_COLUMN_VALUE;

	/**
	 * A file was not attached to a message because a message carries only images and PDF.
	 *
	 * @en {0}: only images and PDF can be attached.
	 * @de {0}: Nur Bilder und PDF können angehängt werden.
	 */
	public static ResKey1 ERROR_ATTACHMENT_TYPE__FILE;

	/**
	 * A file was not attached to a message because it is larger than a message may carry.
	 *
	 * @en {0}: larger than 10 MB.
	 * @de {0}: Größer als 10 MB.
	 */
	public static ResKey1 ERROR_ATTACHMENT_TOO_LARGE__FILE;

	/**
	 * A file was not attached to a message because the message already carries as many files as it
	 * may.
	 *
	 * @en {0}: at most {1} files can be attached to one message.
	 * @de {0}: An eine Nachricht können höchstens {1} Dateien angehängt werden.
	 */
	public static ResKey2 ERROR_TOO_MANY_ATTACHMENTS__FILE_MAX;

	static {
		initConstants(I18NConstants.class);
	}
}
