/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.knowledge.wrap.person.Person;

/**
 * The outcome of authenticating a non-browser caller of the {@link AgentServlet}: who it acts as,
 * what it may do, and which session it acts in.
 *
 * @param account
 *        The account the caller acts as. Its permissions govern every command the caller
 *        dispatches, exactly as they govern that account's own user interface.
 * @param mayAct
 *        Whether the caller may change anything. A caller without this permission may only read
 *        the observation; every command dispatch is refused.
 * @param sessionKey
 *        The key of the session the caller acts in, as registered with
 *        {@link AgentAccess#bindSession(String, jakarta.servlet.http.HttpSession)}.
 */
public record AgentCredential(Person account, boolean mayAct, String sessionKey) {
	// Data record.
}
