/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;

/**
 * Service holding the {@link WorkspaceProvider} that manages the workspaces of the application.
 *
 * <p>
 * Starting the service does not contact the container platform, so the application starts also
 * when the platform is not reachable; the first workspace operation reports that.
 * </p>
 */
public class WorkspaceService extends ConfiguredManagedClass<WorkspaceService.Config<?>> {

	/**
	 * Configuration of {@link WorkspaceService}.
	 */
	public interface Config<I extends WorkspaceService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProvider()}.
		 *
		 * @see #getProvider()
		 */
		String PROVIDER = "provider";

		/**
		 * The container platform the workspaces are created in.
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@ItemDefault(DockerWorkspaceProvider.Config.class)
		PolymorphicConfiguration<? extends WorkspaceProvider> getProvider();

	}

	private final WorkspaceProvider _provider;

	/**
	 * Creates a {@link WorkspaceService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public WorkspaceService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_provider = context.getInstance(config.getProvider());
	}

	/**
	 * The configured {@link WorkspaceProvider}.
	 */
	public WorkspaceProvider getProvider() {
		return _provider;
	}

	@Override
	protected void shutDown() {
		_provider.close();
		super.shutDown();
	}

	/**
	 * The {@link WorkspaceService} of the running application.
	 */
	public static WorkspaceService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link WorkspaceService}.
	 */
	public static final class Module extends TypedRuntimeModule<WorkspaceService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<WorkspaceService> getImplementation() {
			return WorkspaceService.class;
		}
	}

}
