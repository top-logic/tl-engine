/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.factory.git.GitConnectionSettings;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitProviderType;

/**
 * {@link GitProviderType} of the tests: every connection gets a {@link RecordingGitHostingProvider}
 * of its URL and owner, kept for the test to inspect.
 *
 * <p>
 * The access token of a connection is sent as header {@link #TOKEN_HEADER}, read from its secret
 * at each request.
 * </p>
 */
public class RecordingGitProviderType extends AbstractConfiguredInstance<RecordingGitProviderType.Config<?>>
		implements GitProviderType {

	/**
	 * Configuration of {@link RecordingGitProviderType}.
	 */
	public interface Config<I extends RecordingGitProviderType> extends GitProviderType.Config<I> {
		// No further settings.
	}

	/**
	 * The name of the type in the test configuration.
	 */
	public static final String NAME = "recording";

	/**
	 * Header the access token of a connection is sent in.
	 */
	public static final String TOKEN_HEADER = "X-Test-Token";

	private static final Map<String, Created> PROVIDERS = new ConcurrentHashMap<>();

	/**
	 * A provider created for a connection and the settings it was created with.
	 *
	 * @param provider
	 *        The provider.
	 * @param settings
	 *        The settings of the connection.
	 */
	public record Created(RecordingGitHostingProvider provider, GitConnectionSettings settings) {
		// Pure data.
	}

	/**
	 * Creates a {@link RecordingGitProviderType} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public RecordingGitProviderType(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public String getName() {
		return getConfig().getName();
	}

	@Override
	public GitHostingProvider createProvider(GitConnectionSettings settings) {
		RecordingGitHostingProvider provider =
			new RecordingGitHostingProvider(settings.owner(), settings.url(), settings.credentials());
		PROVIDERS.put(key(settings.url(), settings.owner()), new Created(provider, settings));
		return provider;
	}

	@Override
	public CredentialProvider tokenCredentials(String secretName) {
		return request -> request.setHeader(TOKEN_HEADER, SecretStoreService.secret(secretName));
	}

	/**
	 * The provider created last for the given URL and owner.
	 *
	 * @return The provider and its settings, {@code null} if none was created.
	 */
	public static Created created(String url, String owner) {
		return PROVIDERS.get(key(url, owner));
	}

	/**
	 * Forgets all created providers.
	 */
	public static void reset() {
		PROVIDERS.clear();
	}

	private static String key(String url, String owner) {
		return url + " " + owner;
	}

}
