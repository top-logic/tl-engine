/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.accesstoken.SessionBinding;

/**
 * {@link SessionBinding} of the permissions for the user interface: registers the session a token
 * is created in with {@link AgentAccess}, and names the endpoint of the {@link AgentServlet}.
 */
public class AgentSessionBinding extends AbstractConfiguredInstance<AgentSessionBinding.Config<?>>
		implements SessionBinding {

	/**
	 * Path of the agent endpoint below the application, see the mapping of the {@link AgentServlet}
	 * in the module's <code>web-fragment.xml</code>.
	 */
	public static final String AGENT_API_PATH = "/agent-api";

	/**
	 * Configuration of {@link AgentSessionBinding}.
	 */
	public interface Config<I extends AgentSessionBinding> extends PolymorphicConfiguration<I> {
		// No configuration options.
	}

	/**
	 * Creates a {@link AgentSessionBinding} from configuration.
	 */
	@CalledByReflection
	public AgentSessionBinding(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void bind(String sessionKey, HttpSession session) {
		AgentAccess.getInstance().bindSession(sessionKey, session);
	}

	@Override
	public String endpoint(String applicationURL) {
		return applicationURL + AGENT_API_PATH;
	}

}
