/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.function.Function;

import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendResolver;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * A {@link ConversationBackendResolver} whose answer a test installs, registered in the test
 * configuration of this module.
 *
 * <p>
 * Installed statically, like {@link ScriptedModel}'s script, because the resolver is created once at
 * service startup. Without an installed answer it claims no conversation, so every other test runs
 * the framework's own agent.
 * </p>
 */
public class ScriptedBackend implements ConversationBackendResolver {

	private static final Function<AgentSession, ConversationBackend> NONE = session -> null;

	private static volatile Function<AgentSession, ConversationBackend> _backends = NONE;

	/** Creates a {@link ScriptedBackend} from configuration. */
	public ScriptedBackend(InstantiationContext context, PolymorphicConfiguration<?> config) {
		// Nothing configured; see install.
	}

	/** Installs which backend answers which conversation, <code>null</code> for none. */
	public static void install(Function<AgentSession, ConversationBackend> backends) {
		_backends = backends;
	}

	/** Claims no conversation again. */
	public static void reset() {
		_backends = NONE;
	}

	@Override
	public ConversationBackend backendFor(AgentSession session) {
		return _backends.apply(session);
	}
}
