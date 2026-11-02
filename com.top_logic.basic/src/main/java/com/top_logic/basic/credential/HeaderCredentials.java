/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;

/**
 * {@link CredentialProvider} sending a static secret (API key, token) in a request header.
 *
 * <p>
 * The header value is the secret, preceded by the configured scheme and a space, if a scheme is
 * given. The secret is read from its {@link SecretSource} for each request.
 * </p>
 *
 * <p>
 * A bearer token from a mounted secret file (<code>Authorization: Bearer &lt;secret&gt;</code>):
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer"&gt;
 *   &lt;secret class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/openai-api-key"/&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * The same with the token from the secret store of the installation, named by a secret element
 * without <code>class</code> attribute (by default read from the environment variable of that
 * name):
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="Bearer"&gt;
 *   &lt;secret name="openai_api_key"/&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * An API key in a service-specific header (<code>x-api-key: &lt;secret&gt;</code>) from the
 * secret store:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" header="x-api-key"&gt;
 *   &lt;secret name="anthropic_api_key"/&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * Other examples are <code>scheme="DeepL-Auth-Key"</code>,
 * <code>header="X-Subscription-Token"</code> and <code>header="api-key"</code>.
 * </p>
 */
public class HeaderCredentials extends AbstractConfiguredInstance<HeaderCredentials.Config>
		implements CredentialProvider {

	/**
	 * Configuration options for {@link HeaderCredentials}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<HeaderCredentials> {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "header-credentials";

		/**
		 * @see #getHeader()
		 */
		String HEADER = "header";

		/**
		 * @see #getScheme()
		 */
		String SCHEME = "scheme";

		/**
		 * @see #getSecret()
		 */
		String SECRET = "secret";

		/**
		 * Name of the request header carrying the secret.
		 */
		@Name(HEADER)
		@StringDefault(CredentialProvider.AUTHORIZATION)
		String getHeader();

		/**
		 * @see #getHeader()
		 */
		void setHeader(String value);

		/**
		 * Authentication scheme written before the secret, e.g. <code>Bearer</code>.
		 *
		 * <p>
		 * If empty, the header value is the bare secret.
		 * </p>
		 */
		@Name(SCHEME)
		String getScheme();

		/**
		 * @see #getScheme()
		 */
		void setScheme(String value);

		/**
		 * The secret sent in the header.
		 */
		@Name(SECRET)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

		/**
		 * @see #getSecret()
		 */
		void setSecret(PolymorphicConfiguration<? extends SecretSource> value);

	}

	private final SecretSource _secret;

	/**
	 * Creates the configuration of a {@link HeaderCredentials} in code.
	 *
	 * <p>
	 * Used by a consumer that accepts a secret as shorthand for its credentials and knows the
	 * header in which its service expects the secret.
	 * </p>
	 *
	 * @param header
	 *        See {@link Config#getHeader()}.
	 * @param scheme
	 *        See {@link Config#getScheme()}, <code>null</code> for sending the bare secret.
	 * @param secret
	 *        See {@link Config#getSecret()}.
	 * @return The configuration to instantiate.
	 */
	public static Config config(String header, String scheme, PolymorphicConfiguration<? extends SecretSource> secret) {
		Config result = TypedConfiguration.newConfigItem(Config.class);
		result.setHeader(header);
		result.setScheme(scheme);
		result.setSecret(secret);
		return result;
	}

	/**
	 * Creates a {@link HeaderCredentials} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public HeaderCredentials(InstantiationContext context, Config config) {
		super(context, config);
		_secret = context.getInstance(config.getSecret());
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		Config config = getConfig();
		String secret = _secret.getSecret();
		String scheme = config.getScheme();
		String value = StringServices.isEmpty(scheme) ? secret : scheme + " " + secret;
		request.setHeader(config.getHeader(), value);
	}

	/**
	 * Whether the secret is available now.
	 *
	 * @see SecretSource#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return _secret.isAvailable();
	}

	/**
	 * Where the secret is taken from.
	 *
	 * @see SecretSource#reference()
	 */
	@Override
	public ResKey reference() {
		return _secret.reference();
	}

}
