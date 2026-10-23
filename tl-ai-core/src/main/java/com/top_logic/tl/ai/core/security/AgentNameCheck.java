/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.knowledge.service.db2.InitialDataSetup;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.util.StartupHooks;

/*
 * Why a check of its own (#663): the agent records the policy may name are created by the initial
 * data import, which runs as startup hook after all services are started. AiToolAccessService
 * therefore leaves the agent names out of its start, and this check, a startup hook configured after
 * the import, asks for them. A test that wants the check performs it.
 */

/**
 * Refuses an agent tool access policy that names an agent the application does not have.
 *
 * <p>
 * Runs as {@link StartupHooks startup hook} after the {@link InitialDataSetup initial data import},
 * so that an agent that the import creates counts as an agent of the application. Stops the start
 * of the application when the policy names an agent that is neither configured nor stored.
 * </p>
 */
public class AgentNameCheck implements StartupAction {

	@Override
	public void perform() {
		if (!AiToolAccessService.Module.INSTANCE.isActive()) {
			return;
		}
		AiToolAccessService.Module.INSTANCE.getImplementationInstance().refuseUnknownAgents();
	}

}
