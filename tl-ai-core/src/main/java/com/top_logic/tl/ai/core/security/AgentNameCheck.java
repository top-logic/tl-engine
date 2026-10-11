/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.knowledge.service.db2.InitialDataSetupService;

/*
 * Why a service of its own (#663): the agent records the policy may name are created by the initial
 * data import, and AiToolAccessService must not depend on that import - every test that starts the
 * access service would then import every seed file of the application into its knowledge base. So
 * the access service leaves the agent names out of its start while the import has not run, and this
 * service, ordered after both, asks for them. A test that wants the check starts this module.
 */

/**
 * Refuses an agent tool access policy that names an agent the application does not have.
 *
 * <p>
 * Starts after the initial data import, so that an agent that the import creates counts as an agent
 * of the application. Stops the start of the application when the policy names an agent that is
 * neither configured nor stored.
 * </p>
 */
@ServiceDependencies({
	AiToolAccessService.Module.class,
	InitialDataSetupService.Module.class,
})
public class AgentNameCheck extends ManagedClass {

	/**
	 * Configuration of {@link AgentNameCheck}.
	 */
	public interface Config extends ServiceConfiguration<AgentNameCheck> {
		// No options.
	}

	/**
	 * Creates an {@link AgentNameCheck} from configuration.
	 */
	public AgentNameCheck(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		AiToolAccessService.Module.INSTANCE.getImplementationInstance().refuseUnknownAgents();
	}

	/**
	 * Module of {@link AgentNameCheck}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentNameCheck> {

		/** Singleton. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<AgentNameCheck> getImplementation() {
			return AgentNameCheck.class;
		}
	}
}
