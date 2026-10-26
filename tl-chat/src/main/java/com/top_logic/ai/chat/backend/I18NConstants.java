/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.backend;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Why the chat refuses a request in a conversation a {@link ConversationBackend} answers.
 *
 * @see BackendFeatures
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Regenerating an answer was asked in a conversation whose answerer cannot do it.
	 *
	 * @en This conversation cannot answer a message again. Send it once more instead.
	 * @de Diese Unterhaltung kann eine Nachricht nicht erneut beantworten. Senden Sie sie stattdessen
	 *     noch einmal.
	 */
	public static ResKey REGENERATE_NOT_SUPPORTED;

	/**
	 * Editing a sent message was asked in a conversation whose answerer cannot drop turns.
	 *
	 * @en Messages of this conversation cannot be changed once they are sent.
	 * @de Nachrichten dieser Unterhaltung lassen sich nach dem Senden nicht mehr ändern.
	 */
	public static ResKey EDIT_NOT_SUPPORTED;

	/**
	 * A message with files was sent in a conversation whose answerer accepts none.
	 *
	 * @en This conversation does not accept files. Send the message without attachments.
	 * @de Diese Unterhaltung nimmt keine Dateien an. Senden Sie die Nachricht ohne Anhänge.
	 */
	public static ResKey ATTACHMENTS_NOT_SUPPORTED;

	static {
		initConstants(I18NConstants.class);
	}
}
