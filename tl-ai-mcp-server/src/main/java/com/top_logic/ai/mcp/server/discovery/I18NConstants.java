/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;

/**
 * Texts that tell a client how to find and connect to the MCP endpoint.
 *
 * @see McpConnectionGuide
 * @see McpServerCard
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Description of the server in its server card when none is configured.
	 *
	 * @en MCP server of {0}
	 * @de MCP-Server von {0}
	 */
	public static ResKey1 CARD_DESCRIPTION__SERVER;

	/**
	 * Title of the section of the MCP endpoint in <code>llms.txt</code>.
	 *
	 * @en MCP server
	 * @de MCP-Server
	 */
	public static ResKey LLMS_TITLE;

	/**
	 * First sentence of the section of the MCP endpoint in <code>llms.txt</code>.
	 *
	 * @en {0} is a Model Context Protocol (MCP) server with the transport streamable HTTP.
	 * @de {0} ist ein Model-Context-Protocol-Server (MCP) mit dem Transport Streamable HTTP.
	 */
	public static ResKey1 LLMS_INTRO__SERVER;

	/**
	 * The endpoint URL in <code>llms.txt</code>.
	 *
	 * @en Endpoint: {0} (the form with a trailing slash, {1}, works as well)
	 * @de Endpunkt: {0} (die Form mit abschließendem Schrägstrich, {1}, funktioniert ebenso)
	 */
	public static ResKey2 LLMS_ENDPOINT__URL_SLASH_URL;

	/**
	 * The server card URL in <code>llms.txt</code>.
	 *
	 * @en Server card: {0}
	 * @de Server-Card: {0}
	 */
	public static ResKey1 LLMS_CARD__URL;

	/**
	 * Introduces the list of accepted credentials.
	 *
	 * @en Every request needs one of these credentials:
	 * @de Jede Anfrage braucht eine dieser Zugangsdaten:
	 */
	public static ResKey CREDENTIALS_INTRO;

	/**
	 * Said when no credential is accepted at all.
	 *
	 * @en This server accepts no credential, so it serves no request.
	 * @de Dieser Server akzeptiert keine Zugangsdaten und bedient daher keine Anfrage.
	 */
	public static ResKey NO_CREDENTIALS;

	/**
	 * Introduces the command that connects Claude Code.
	 *
	 * @en Connect Claude Code:
	 * @de Claude Code verbinden:
	 */
	public static ResKey LLMS_CONNECT_CLAUDE;

	/**
	 * Introduces the command that connects Claude Code to a server offering a sign-in.
	 *
	 * @en Connect Claude Code without a credential; it signs you in through the browser when it
	 *     first connects:
	 * @de Claude Code ohne Zugangsdaten verbinden; es meldet Sie beim ersten Verbinden im Browser
	 *     an:
	 */
	public static ResKey LLMS_CONNECT_CLAUDE_SIGN_IN;

	/**
	 * Introduces the command that connects Claude Code with a credential header, after the command
	 * without credential ({@link #LLMS_CONNECT_CLAUDE_SIGN_IN the command without credential}).
	 *
	 * @en Alternatively, connect Claude Code with a credential in a header:
	 * @de Alternativ Claude Code mit Zugangsdaten in einem Header verbinden:
	 */
	public static ResKey LLMS_CONNECT_CLAUDE_HEADER_ALTERNATIVE;

	/**
	 * Note on where to keep the credential of an MCP client.
	 *
	 * @en Prefer --scope user, so that the credential stays in your own configuration. Never commit a
	 *     credential in a project file .mcp.json; there, write the secret part of the header as a
	 *     reference to an environment variable, such as {0}, and set the variable outside the
	 *     project.
	 * @de Verwenden Sie bevorzugt --scope user, damit die Zugangsdaten in Ihrer eigenen Konfiguration
	 *     bleiben. Checken Sie niemals Zugangsdaten in einer Projektdatei .mcp.json ein; schreiben Sie
	 *     dort den geheimen Teil des Headers als Verweis auf eine Umgebungsvariable, etwa {0}, und setzen
	 *     Sie die Variable außerhalb des Projekts.
	 */
	public static ResKey1 LLMS_CREDENTIAL_STORAGE__VARIABLE;

	/**
	 * Introduces the instructions of the server.
	 *
	 * @en Instructions of the server:
	 * @de Anweisungen des Servers:
	 */
	public static ResKey LLMS_INSTRUCTIONS;

	/**
	 * Added to the answer to a request without a valid credential.
	 *
	 * @en How to connect to this MCP server: {0} (server card: {1}).
	 * @de So verbinden Sie sich mit diesem MCP-Server: {0} (Server-Card: {1}).
	 */
	public static ResKey2 UNAUTHORIZED_HOW_TO_CONNECT__LLMS_CARD;

	static {
		initConstants(I18NConstants.class);
	}
}
