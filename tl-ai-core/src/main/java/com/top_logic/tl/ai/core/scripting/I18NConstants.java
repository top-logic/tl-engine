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

	/**
	 * A memory function was given something that is not a memory.
	 *
	 * @en This is not a memory.
	 * @de Das ist keine Erinnerung.
	 */
	public static ResKey ERROR_NOT_A_MEMORY;

	/**
	 * The acting account neither owns the memory nor administers the application.
	 *
	 * @en This memory belongs to someone else.
	 * @de Diese Erinnerung gehört jemand anderem.
	 */
	public static ResKey ERROR_MEMORY_NOT_OWNED;

	/**
	 * A non-administrator asked to clear a model health mark.
	 *
	 * @en Only an administrator can reset the health of a model.
	 * @de Nur eine Administratorin oder ein Administrator kann den Zustand eines Modells zurücksetzen.
	 */
	public static ResKey ERROR_MODEL_HEALTH_NOT_ADMIN;

	/**
	 * A model health function was called while the LLM providers are not running.
	 *
	 * @en The LLM providers are not running, so no model health is known.
	 * @de Die LLM-Anbieter laufen nicht, daher ist kein Modellzustand bekannt.
	 */
	public static ResKey ERROR_MODEL_HEALTH_UNAVAILABLE;

	/**
	 * Error when {@code aiResearch} is called without a question.
	 *
	 * @en A research needs a question.
	 * @de Eine Recherche braucht eine Frage.
	 */
	public static ResKey ERROR_RESEARCH_QUESTION_EMPTY;

	/**
	 * Error when {@code aiResearch} is given an effort level the research skill does not know.
	 *
	 * @en Unknown research effort: {0}. Use "quick", "standard" or "deep".
	 * @de Unbekannter Rechercheumfang: {0}. Erlaubt sind "quick", "standard" und "deep".
	 */
	public static ResKey1 ERROR_RESEARCH_EFFORT__VALUE;

	/**
	 * Error when {@code aiResearch} names an account that does not exist.
	 *
	 * @en There is no account {0}.
	 * @de Es gibt kein Konto {0}.
	 */
	public static ResKey1 ERROR_RESEARCH_UNKNOWN_OWNER__NAME;

	/**
	 * Error when {@code aiResearch} is called without an account and names none.
	 *
	 * @en A research needs an owner. Without a logged-in account, name the owner.
	 * @de Eine Recherche braucht einen Eigentümer. Ohne angemeldetes Konto geben Sie den Eigentümer an.
	 */
	public static ResKey ERROR_RESEARCH_NO_OWNER;

	/**
	 * Error when a logged-in account starts a research for another account.
	 *
	 * @en You can start a research only for yourself.
	 * @de Sie können eine Recherche nur für sich selbst starten.
	 */
	public static ResKey ERROR_RESEARCH_OTHER_OWNER;

	/**
	 * Title of the chat that records a research started without a chat window.
	 *
	 * @en Research: {0}
	 * @de Recherche: {0}
	 */
	public static ResKey1 RESEARCH_SESSION_TITLE__QUESTION;

	static {
		initConstants(I18NConstants.class);
	}
}
