/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.gitea;

import java.time.Duration;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.factory.git.GitConnectionSettings;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitProviderType;
import com.top_logic.factory.git.GitRepository;

/**
 * {@link GitProviderType} of <a href="https://about.gitea.com/">Gitea</a>: its connections get a
 * {@link GiteaHostingProvider}.
 *
 * <p>
 * The settings of the type apply to all connections to a Gitea server. An access token is sent as
 * {@code Authorization: token <token>} ({@link GiteaHostingProvider#TOKEN_SCHEME}).
 * </p>
 */
public class GiteaProviderType extends AbstractConfiguredInstance<GiteaProviderType.Config<?>>
		implements GitProviderType {

	/**
	 * Configuration of {@link GiteaProviderType}.
	 */
	public interface Config<I extends GiteaProviderType> extends GitProviderType.Config<I> {

		/**
		 * Configuration property name of {@link #getTimeout()}.
		 */
		String TIMEOUT = "timeout";

		/**
		 * Configuration property name of {@link #getRequiredApprovals()}.
		 */
		String REQUIRED_APPROVALS = "required-approvals";

		/**
		 * How long a connection to Gitea and each request may take.
		 *
		 * @see #TIMEOUT
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(30 * 1000)
		long getTimeout();

		/**
		 * The number of approvals a pull request to a protected branch needs.
		 *
		 * @see #REQUIRED_APPROVALS
		 * @see GiteaHostingProvider#protectBranch(GitRepository, String, List)
		 */
		@Name(REQUIRED_APPROVALS)
		@IntDefault(1)
		int getRequiredApprovals();

	}

	/**
	 * The {@link #getName() name} the type is registered under in the configuration of
	 * {@code tl-factory-gitea}.
	 */
	public static final String NAME = "gitea";

	/**
	 * Name of the secret holding the access token of the built-in Gitea connection in the
	 * configuration of {@code tl-factory-app}.
	 */
	public static final String TOKEN_SECRET = "tl_factory_gitea_token";

	/**
	 * Creates a {@link GiteaProviderType} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GiteaProviderType(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

	@Override
	public GitHostingProvider createProvider(GitConnectionSettings settings) {
		return new GiteaHostingProvider(settings, Duration.ofMillis(getConfig().getTimeout()),
			getConfig().getRequiredApprovals());
	}

	@Override
	public CredentialProvider tokenCredentials(String secretName) {
		return credentials(secretName);
	}

	/**
	 * The credentials sending the access token in the named secret of the given name as
	 * {@code Authorization: token <token>}.
	 */
	public static CredentialProvider credentials(String secretName) {
		NamedSecretSource.Config token = TypedConfiguration.newConfigItem(NamedSecretSource.Config.class);
		token.setName(secretName);
		return TypedConfigUtil.createInstance(
			HeaderCredentials.config(CredentialProvider.AUTHORIZATION, GiteaHostingProvider.TOKEN_SCHEME, token));
	}

}
