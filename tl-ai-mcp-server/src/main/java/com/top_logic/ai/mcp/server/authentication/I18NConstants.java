/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.authentication;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * Messages the MCP endpoint sends back to a caller it will not serve.
 *
 * <p>
 * These reach an MCP client over HTTP, not a browser, so each one says what the operator has to
 * change rather than merely that access was denied.
 * </p>
 *
 * @see McpAuthenticationFilter
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The caller authenticated, but the credential is not mapped to an application account.
	 *
	 * @en Authenticated, but this credential names no application account. The MCP endpoint acts as
	 *     the account it authenticated and never in system context, so give the secret a user-id -
	 *     preferably a dedicated service account.
	 * @de Authentifiziert, aber diese Zugangsdaten benennen kein Anwendungskonto. Der MCP-Endpunkt
	 *     handelt als das authentifizierte Konto und niemals im Systemkontext. Vergeben Sie daher
	 *     eine user-id für das Secret - am besten ein eigenes Dienstkonto.
	 */
	public static ResKey ERROR_NO_ACCOUNT_FOR_CREDENTIAL;

	/**
	 * A second account tried to use an MCP session another account opened.
	 *
	 * @en This mcp-session-id belongs to another account. An MCP session stays with the account
	 *     that opened it; start your own session with an initialize request.
	 * @de Diese mcp-session-id gehört zu einem anderen Konto. Eine MCP-Sitzung bleibt bei dem
	 *     Konto, das sie eröffnet hat. Eröffnen Sie mit einer initialize-Anfrage eine eigene
	 *     Sitzung.
	 */
	public static ResKey ERROR_SESSION_BELONGS_TO_ANOTHER_ACCOUNT;

	/**
	 * The request arrived on a thread that already carries a TopLogic interaction.
	 *
	 * @en Refusing this request: its thread already carries a TopLogic interaction, so the
	 *     authenticated account could not be bound to it and the request would have acted as
	 *     somebody else.
	 * @de Diese Anfrage wird abgewiesen: Ihr Thread trägt bereits eine TopLogic-Interaktion, so
	 *     dass das authentifizierte Konto nicht gebunden werden konnte und die Anfrage als jemand
	 *     anderes gehandelt hätte.
	 */
	public static ResKey ERROR_INTERACTION_ALREADY_BOUND;

	static {
		initConstants(I18NConstants.class);
	}
}
