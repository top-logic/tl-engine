/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.DevelopmentCategory;
import com.top_logic.util.error.TopLogicException;

/**
 * Service holding the {@link GitHostingProvider} of the git hosting service the repositories of
 * the projects live on.
 *
 * <p>
 * Git hosting is optional: without a {@link Config#getProvider() provider}, the service is
 * {@link #isEnabled() disabled}, and the projects have no repository on a hosting service.
 * </p>
 *
 * <p>
 * Starting the service does not contact the hosting service, so the application starts also when
 * it is not reachable; the first operation reports that.
 * </p>
 */
@ServiceCategory.Of(DevelopmentCategory.class)
@ServiceDependencies({
	// The credentials of the provider read named secrets.
	SecretStoreService.Module.class,
})
public class GitHostingService extends ConfiguredManagedClass<GitHostingService.Config<?>> {

	/**
	 * Configuration of {@link GitHostingService}.
	 */
	public interface Config<I extends GitHostingService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProvider()}.
		 *
		 * @see #getProvider()
		 */
		String PROVIDER = "provider";

		/**
		 * The git hosting service the repositories of the projects are created on.
		 *
		 * <p>
		 * If not set, git hosting is switched off.
		 * </p>
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@Nullable
		PolymorphicConfiguration<? extends GitHostingProvider> getProvider();

	}

	private final GitHostingProvider _provider;

	/**
	 * Creates a {@link GitHostingService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public GitHostingService(InstantiationContext context, Config<?> config) {
		super(context, config);
		_provider = context.getInstance(config.getProvider());
	}

	/**
	 * Whether a git hosting service is configured.
	 */
	public boolean isEnabled() {
		return _provider != null;
	}

	/**
	 * The configured {@link GitHostingProvider}.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_NO_GIT_HOSTING} if the service is not
	 *         {@link #isEnabled() enabled}.
	 */
	public GitHostingProvider getProvider() {
		if (_provider == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_GIT_HOSTING);
		}
		return _provider;
	}

	/**
	 * The {@link GitHostingService} of the running application.
	 */
	public static GitHostingService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link GitHostingService}.
	 */
	public static final class Module extends TypedRuntimeModule<GitHostingService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<GitHostingService> getImplementation() {
			return GitHostingService.class;
		}
	}

}
