/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.time.Instant;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Scheduler task removing expired authorization codes and tokens, authorizations that can no
 * longer be used, and dynamically registered clients nobody uses.
 *
 * <p>
 * Does nothing while the {@link OAuthAuthorizationServer} is not active.
 * </p>
 *
 * @see OAuthAuthorizationServer#cleanUp(Instant)
 */
@Label("Clean up OAuth authorizations")
public class OAuthCleanUpTask extends TaskImpl<TaskImpl.Config<?>> {

	/**
	 * Creates a {@link OAuthCleanUpTask} from configuration.
	 */
	@CalledByReflection
	public OAuthCleanUpTask(InstantiationContext context, TaskImpl.Config<?> config) {
		super(context, config);
	}

	@Override
	public void run() {
		runWithResultProtocol(this::cleanUp);

		super.run();
	}

	@Override
	public boolean isNodeLocal() {
		// Deletes persistent data: running once in the cluster at a time is enough.
		return false;
	}

	private void cleanUp() {
		OAuthAuthorizationServer server = OAuthAuthorizationServer.getInstance();
		if (server == null) {
			return;
		}
		OAuthAuthorizationServer.CleanUpResult result = server.cleanUp(Instant.now());
		getLog().taskEnded(ResultType.SUCCESS,
			I18NConstants.CLEAN_UP_RESULT__PENDING_CODES_TOKENS_GRANTS_CLIENTS.fill(
				Integer.valueOf(result.pendingAuthorizations()), Integer.valueOf(result.codes()),
				Integer.valueOf(result.tokens()), Integer.valueOf(result.grants()), Integer.valueOf(result.clients())));
	}

}
