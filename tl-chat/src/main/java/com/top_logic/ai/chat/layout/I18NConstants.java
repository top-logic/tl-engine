/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.i18n.I18NConstantsBase;

/**
 * Messages of the chat's runs: of a chat started on the server ({@link ChatStart}) and of a send
 * that waits for a free worker ({@link ChatProcessor}).
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en A chat cannot be started without a message.
	 * @de Ein Chat kann nicht ohne Nachricht begonnen werden.
	 */
	public static ResKey ERROR_EMPTY_MESSAGE;

	/**
	 * @en The chat could not be created.
	 * @de Der Chat konnte nicht angelegt werden.
	 */
	public static ResKey ERROR_CHAT_NOT_CREATED;

	/**
	 * @en A chat cannot be started from a background run. Nobody watches a background run, and the
	 *     background allowlist does not bound a chat.
	 * @de Aus einem Hintergrundlauf kann kein Chat gestartet werden. Niemand beobachtet einen
	 *     Hintergrundlauf, und die Hintergrund-Zulassungsliste begrenzt einen Chat nicht.
	 */
	public static ResKey ERROR_STARTED_IN_BACKGROUND_RUN;

	/**
	 * @en The message was not sent: {0}
	 * @de Die Nachricht wurde nicht gesendet: {0}
	 */
	public static ResKey1 ERROR_NOT_SENT__REASON;

	/**
	 * @en Waiting for a free worker - position {0}
	 * @de Warte auf einen freien Bearbeiter - Position {0}
	 */
	public static ResKey1 WAITING_FOR_WORKER__POSITION;

	static {
		initConstants(I18NConstants.class);
	}
}
