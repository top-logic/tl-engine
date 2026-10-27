/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.DevelopmentCategory;
import com.top_logic.factory.server.ServerProviderConfig;
import com.top_logic.util.error.TopLogicException;

/**
 * Service holding the {@link CIProvider} of the CI server building the applications of the
 * projects.
 *
 * <p>
 * CI is optional: without a {@link Config#getProvider() provider}, or with one switched off by an
 * empty {@link ServerProviderConfig#getBaseUrl() base URL}, the service is {@link #isEnabled()
 * disabled}, the projects have no CI job, and creating and working in a
 * workspace does not involve a CI server.
 * </p>
 *
 * <p>
 * Starting the service does not contact the CI server, so the application starts also when it is
 * not reachable; the first operation reports that.
 * </p>
 */
@ServiceCategory.Of(DevelopmentCategory.class)
@ServiceDependencies({
	// The credentials of the provider read named secrets.
	SecretStoreService.Module.class,
})
public class CIService extends ConfiguredManagedClass<CIService.Config<?>> {

	/**
	 * Configuration of {@link CIService}.
	 */
	public interface Config<I extends CIService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProvider()}.
		 *
		 * @see #getProvider()
		 */
		String PROVIDER = "provider";

		/**
		 * Configuration property name of {@link #getPipelineTemplate()}.
		 *
		 * @see #getPipelineTemplate()
		 */
		String PIPELINE_TEMPLATE = "pipeline-template";

		/**
		 * The CI server the jobs of the projects run on.
		 *
		 * <p>
		 * If not set, CI is switched off; so it is with a provider whose
		 * {@link ServerProviderConfig#getBaseUrl() base URL} is empty, which is not created then.
		 * </p>
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@Nullable
		PolymorphicConfiguration<? extends CIProvider> getProvider();

		/**
		 * The web application resource whose content a new application gets as its pipeline
		 * definition, the {@link CIProvider#pipelineFile() pipeline file} of its first commit.
		 *
		 * <p>
		 * An application overrides the default by a resource of the same path in its own web
		 * application, or names another resource here.
		 * </p>
		 *
		 * @see #PIPELINE_TEMPLATE
		 */
		@Name(PIPELINE_TEMPLATE)
		@StringDefault(DEFAULT_PIPELINE_TEMPLATE)
		String getPipelineTemplate();

	}

	/**
	 * Default of {@link Config#getPipelineTemplate()}: a Jenkins pipeline building the application
	 * with Maven.
	 */
	public static final String DEFAULT_PIPELINE_TEMPLATE = "/WEB-INF/factory/ci/Jenkinsfile";

	private final CIProvider _provider;

	/**
	 * Creates a {@link CIService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CIService(InstantiationContext context, Config<?> config) {
		super(context, config);
		PolymorphicConfiguration<? extends CIProvider> provider = config.getProvider();
		_provider = ServerProviderConfig.isSwitchedOn(provider) ? context.getInstance(provider) : null;
	}

	/**
	 * Whether a CI server is configured.
	 */
	public boolean isEnabled() {
		return _provider != null;
	}

	/**
	 * The configured {@link CIProvider}.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_NO_CI} if the service is not {@link #isEnabled()
	 *         enabled}.
	 */
	public CIProvider getProvider() {
		if (_provider == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_CI);
		}
		return _provider;
	}

	/**
	 * The pipeline definition of a new application, the content of the
	 * {@link Config#getPipelineTemplate() template}.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_MISSING_TEMPLATE__NAME} if the template cannot be
	 *         read.
	 */
	public String pipelineTemplate() {
		return readResource(getConfig().getPipelineTemplate());
	}

	/**
	 * Reads a text resource of the web application, encoded in UTF-8.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_MISSING_TEMPLATE__NAME} if the resource does not
	 *         exist or cannot be read.
	 */
	public static String readResource(String name) {
		try (InputStream in = FileManager.getInstance().getStreamOrNull(name)) {
			if (in == null) {
				throw new TopLogicException(I18NConstants.ERROR_MISSING_TEMPLATE__NAME.fill(name));
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_TEMPLATE__NAME.fill(name), ex);
		}
	}

	/**
	 * The provider of the running {@link CIService}, {@code null} if the service is not running or
	 * has none.
	 */
	public static CIProvider configuredProvider() {
		if (!Module.INSTANCE.isActive()) {
			return null;
		}
		CIService service = getInstance();
		return service.isEnabled() ? service.getProvider() : null;
	}

	/**
	 * The {@link CIService} of the running application.
	 */
	public static CIService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link CIService}.
	 */
	public static final class Module extends TypedRuntimeModule<CIService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CIService> getImplementation() {
			return CIService.class;
		}
	}

}
