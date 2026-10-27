/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
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

		/**
		 * Configuration property name of {@link #getProtectedFiles()}.
		 *
		 * @see #getProtectedFiles()
		 */
		String PROTECTED_FILES = "protected-files";

		/**
		 * Glob patterns of the files of a project repository that no push may change, e.g. the
		 * build pipeline and the deployment of the application.
		 *
		 * <p>
		 * The default branch of every repository of a project is protected with these patterns
		 * when a workspace of the project has the sources in it: a change of such a file reaches
		 * the branch only through a reviewed pull request, neither the agent nor the control
		 * application pushes one. Patterns are relative to the root of the repository, {@code *}
		 * matches within a path segment, {@code **} across segments.
		 * </p>
		 *
		 * @see #PROTECTED_FILES
		 * @see GitHostingProvider#protectBranch(GitRepository, String, List)
		 */
		@Name(PROTECTED_FILES)
		@Format(CommaSeparatedStrings.class)
		@FormattedDefault(DEFAULT_PROTECTED_FILES)
		List<String> getProtectedFiles();

	}

	/**
	 * Default of {@link Config#getProtectedFiles()}.
	 */
	public static final String DEFAULT_PROTECTED_FILES = "Jenkinsfile, deploy/**";

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
	 * The glob patterns of the files no push to the default branch of a project repository may
	 * change.
	 *
	 * @see Config#getProtectedFiles()
	 */
	public List<String> getProtectedFiles() {
		return getConfig().getProtectedFiles();
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
