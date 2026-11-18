/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.discovery;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;

/**
 * Texts that tell a client which A2A agents the application exposes and how to call them.
 *
 * @see A2ADiscoveryContribution
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Title of the section of the A2A agents in <code>llms.txt</code>.
	 *
	 * @en A2A agents
	 * @de A2A-Agenten
	 */
	public static ResKey LLMS_TITLE;

	/**
	 * First paragraph of the section of the A2A agents in <code>llms.txt</code>.
	 *
	 * @en This application exposes agents over the Agent2Agent protocol (A2A), version 1.0. An agent
	 *     answers JSON-RPC 2.0 requests at its endpoint. Its agent card is readable without a
	 *     credential and names the endpoint, the skills and the authentication.
	 * @de Diese Anwendung stellt Agenten über das Agent2Agent-Protokoll (A2A) in Version 1.0 bereit.
	 *     Ein Agent beantwortet JSON-RPC-2.0-Anfragen an seinem Endpunkt. Seine Agent-Card ist ohne
	 *     Zugangsdaten lesbar und nennt den Endpunkt, die Fähigkeiten und die Authentifizierung.
	 */
	public static ResKey LLMS_INTRO;

	/**
	 * An agent without a description in the list of agents in <code>llms.txt</code>.
	 *
	 * @en [{0}]({1}): endpoint {2}
	 * @de [{0}]({1}): Endpunkt {2}
	 */
	public static ResKey3 LLMS_AGENT__NAME_CARD_ENDPOINT;

	/**
	 * An agent with a description in the list of agents in <code>llms.txt</code>.
	 *
	 * @en [{0}]({1}): {3} (endpoint {2})
	 * @de [{0}]({1}): {3} (Endpunkt {2})
	 */
	public static ResKey4 LLMS_AGENT__NAME_CARD_ENDPOINT_DESCRIPTION;

	/**
	 * Introduces the list of accepted credentials.
	 *
	 * @en Every request to an agent endpoint needs one of these credentials:
	 * @de Jede Anfrage an einen Agenten-Endpunkt muss sich auf eine dieser Arten ausweisen:
	 */
	public static ResKey CREDENTIALS_INTRO;

	/**
	 * Said when no credential is accepted at all.
	 *
	 * @en The agents accept no credential, so they serve no request.
	 * @de Die Agenten akzeptieren keine Zugangsdaten und bedienen daher keine Anfrage.
	 */
	public static ResKey NO_CREDENTIALS;

	static {
		initConstants(I18NConstants.class);
	}
}
