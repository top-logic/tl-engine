/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendResolver;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.model.AgentSession;

/**
 * {@link ConversationBackendResolver} letting the coding agent of a workspace answer the chats
 * whose subject is an {@link FactoryModel#AGENT_CONVERSATION}.
 *
 * @see WorkspaceAgentService
 */
public class AgentConversationResolver implements ConversationBackendResolver {

	/**
	 * Creates a {@link AgentConversationResolver} from configuration.
	 */
	public AgentConversationResolver(InstantiationContext context, PolymorphicConfiguration<?> config) {
		// Nothing configured.
	}

	@Override
	public ConversationBackend backendFor(AgentSession session) {
		TLObject subject = session.getSubject();
		if (!FactoryModel.isConversation(subject)) {
			return null;
		}
		WorkspaceAgentService agents = WorkspaceAgentService.getInstance();
		return agents == null ? null : agents.agentFor(subject);
	}

}
