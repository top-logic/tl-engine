/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.screenshots;

import java.util.Date;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;

/**
 * Builds what a {@link Screenshot} needs in tests that do not run the execution store: the session it
 * is a part of.
 */
public final class ScreenshotFixtures {

	/**
	 * Creates an {@link AgentSession} in the caller's transaction.
	 *
	 * @param owner
	 *        The owner of the session; <code>null</code> for the session of an unattended run, which
	 *        is a background session.
	 */
	public static AgentSession newSession(Person owner) {
		AgentSession session = TlAiExecutionFactory.getInstance().createAgentSession();
		session.setKind(owner == null
			? TlAiExecutionFactory.getBACKGROUNDSessionKindClassifier()
			: TlAiExecutionFactory.getCHATSessionKindClassifier());
		if (owner != null) {
			session.setOwner(owner);
		}
		session.setCreatedAt(new Date());
		return session;
	}

	private ScreenshotFixtures() {
		// Static utility.
	}

}
