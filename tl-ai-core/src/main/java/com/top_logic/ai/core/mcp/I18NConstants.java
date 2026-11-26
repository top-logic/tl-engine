/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.mcp;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;

/**
 * I18N constants for the MCP client package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error when the URL of an MCP server is not an absolute http or https URL with a host.
	 *
	 * @en The URL "{0}" of the MCP server is not valid. Enter an absolute http or https URL with a
	 *     host, e.g. https://host/app/mcp.
	 * @de Die URL "{0}" des MCP-Servers ist ungültig. Geben Sie eine absolute http- oder
	 *     https-URL mit einem Host an, z. B. https://host/app/mcp.
	 */
	public static ResKey1 ERROR_INVALID_URL__URL;

	/**
	 * Error when the credentials of an MCP server are personal.
	 *
	 * @en The MCP client cannot use personal credentials for the MCP server "{0}", because one
	 *     connection serves all persons. Configure credentials of the application for it.
	 * @de Der MCP-Client kann für den MCP-Server "{0}" keine persönlichen Zugangsdaten verwenden,
	 *     weil eine Verbindung allen Personen dient. Konfigurieren Sie dafür Zugangsdaten der
	 *     Anwendung.
	 */
	public static ResKey1 ERROR_PERSONAL_CREDENTIALS__URL;

	static {
		initConstants(I18NConstants.class);
	}
}
