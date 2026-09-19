/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;

/**
 * I18N constants for the environment block.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Heading of the application-context section inside the environment block. Read by the model,
	 * in the language of the account the run acts for.
	 *
	 * @en Application context - what this application states about the situation:
	 * @de Anwendungskontext - was diese Anwendung über die Situation angibt:
	 */
	public static ResKey ENVIRONMENT_CONTEXT_HEADING;

	/**
	 * Heading of the user-preferences section inside the environment block. Read by the model, in
	 * the language of the account the run acts for.
	 *
	 * @en User preferences - what this user asked for:
	 * @de Benutzerpräferenzen - worum dieser Benutzer gebeten hat:
	 */
	public static ResKey ENVIRONMENT_PREFERENCES_HEADING;

	/**
	 * Label of the turn number inside the per-turn context block. Read by the model, in the
	 * language of the account the run acts for.
	 *
	 * @en Turn
	 * @de Turn
	 */
	public static ResKey TURN_CONTEXT_TURN;

	/**
	 * Label of the time of day inside the per-turn context block. Read by the model, in the
	 * language of the account the run acts for.
	 *
	 * @en Time
	 * @de Uhrzeit
	 */
	public static ResKey TURN_CONTEXT_TIME;

	/**
	 * Error message when a preference text is longer than the configured cap.
	 *
	 * @en Your preferences are {0} characters long; at most {1} are allowed. They are sent with
	 *     every request, so keep them short.
	 * @de Ihre Präferenzen sind {0} Zeichen lang; erlaubt sind höchstens {1}. Sie werden mit jeder
	 *     Anfrage gesendet, halten Sie sie daher kurz.
	 */
	public static ResKey2 PREFERENCES_TOO_LONG__LENGTH_LIMIT;

	/**
	 * Error message when a system prompt uses a placeholder the harness cannot fill.
	 *
	 * @en Unknown placeholder(s) in the system prompt: {0}. Known are: {1}
	 * @de Unbekannte Platzhalter im System-Prompt: {0}. Bekannt sind: {1}
	 */
	public static ResKey2 UNKNOWN_PLACEHOLDER__NAMES_KNOWN;

	static {
		initConstants(I18NConstants.class);
	}
}
