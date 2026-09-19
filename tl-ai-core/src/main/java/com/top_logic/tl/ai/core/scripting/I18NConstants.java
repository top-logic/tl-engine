/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * I18N constants of the {@code ai...} TL-Script functions.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * A session function was given something that is not a chat session.
	 *
	 * @en This is not a chat session.
	 * @de Das ist keine Chat-Sitzung.
	 */
	public static ResKey ERROR_NOT_A_SESSION;

	/**
	 * The acting account neither owns the session nor administers the application.
	 *
	 * @en This chat belongs to someone else.
	 * @de Dieser Chat gehört jemand anderem.
	 */
	public static ResKey ERROR_SESSION_NOT_OWNED;

	/**
	 * A rename was asked for without a new title.
	 *
	 * @en A chat needs a title.
	 * @de Ein Chat braucht einen Titel.
	 */
	public static ResKey ERROR_SESSION_TITLE_EMPTY;

	/**
	 * An AI run ended without an answer, and the provider said why.
	 *
	 * @en The AI request failed ({0}): {1}
	 * @de Die KI-Anfrage ist fehlgeschlagen ({0}): {1}
	 */
	public static ResKey2 ERROR_RUN_FAILED__KIND_MESSAGE;

	/**
	 * An AI run ended without an answer, with nothing said about why.
	 *
	 * @en The AI request produced no answer ({0}).
	 * @de Die KI-Anfrage hat keine Antwort geliefert ({0}).
	 */
	public static ResKey1 ERROR_RUN_WITHOUT_ANSWER__REASON;

	/**
	 * The response format of an {@code aiChat} call is neither "text", "json" nor a schema map.
	 *
	 * @en Unknown response format: {0}. Expected "text", "json" or a map with a "schema".
	 * @de Unbekanntes Antwortformat: {0}. Erwartet werden "text", "json" oder eine Map mit
	 *     "schema".
	 */
	public static ResKey1 ERROR_UNKNOWN_RESPONSE_FORMAT__VALUE;

	/**
	 * The response format of an {@code aiChat} call is a map, but carries no JSON schema.
	 *
	 * @en A response format map needs a "schema" entry holding the JSON schema of the answer.
	 * @de Eine Antwortformat-Map braucht einen Eintrag "schema" mit dem JSON-Schema der Antwort.
	 */
	public static ResKey ERROR_RESPONSE_FORMAT_WITHOUT_SCHEMA;

	static {
		initConstants(I18NConstants.class);
	}
}
