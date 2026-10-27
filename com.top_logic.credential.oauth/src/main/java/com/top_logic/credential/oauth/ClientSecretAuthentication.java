/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.net.URI;

import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.ClientSecretPost;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * {@link ClientAuthentication} with a client secret (RFC 6749 §2.3.1).
 *
 * <pre>
 * &lt;client-authentication class="com.top_logic.credential.oauth.ClientSecretAuthentication" method="basic"&gt;
 *   &lt;secret class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/client-secret"/&gt;
 * &lt;/client-authentication&gt;
 * </pre>
 */
public class ClientSecretAuthentication extends AbstractConfiguredInstance<ClientSecretAuthentication.Config>
		implements ClientAuthentication {

	/**
	 * How the client secret is sent to the authorization server.
	 */
	public enum Method implements ExternallyNamed {

		/**
		 * HTTP basic authentication with the form-encoded client identifier and secret as user
		 * and password (<code>client_secret_basic</code>).
		 *
		 * @implNote Sent by {@link ClientSecretBasic}.
		 */
		@Label("HTTP basic authentication")
		BASIC("basic"),

		/**
		 * The client identifier and secret as parameters <code>client_id</code> and
		 * <code>client_secret</code> of the request body (<code>client_secret_post</code>).
		 *
		 * @implNote Sent by {@link ClientSecretPost}.
		 */
		@Label("Request body")
		POST("post");

		private final String _externalName;

		Method(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}

	}

	/**
	 * Configuration options for {@link ClientSecretAuthentication}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ClientSecretAuthentication> {

		/**
		 * Tag name of this configuration in a list of {@link ClientAuthentication}s.
		 */
		String TAG_NAME = "client-secret";

		/**
		 * @see #getSecret()
		 */
		String SECRET = "secret";

		/**
		 * @see #getMethod()
		 */
		String METHOD = "method";

		/**
		 * The client secret.
		 */
		@Name(SECRET)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

		/**
		 * @see #getSecret()
		 */
		void setSecret(PolymorphicConfiguration<? extends SecretSource> value);

		/**
		 * How the secret is sent to the authorization server.
		 */
		@Name(METHOD)
		@FormattedDefault("basic")
		Method getMethod();

		/**
		 * @see #getMethod()
		 */
		void setMethod(Method value);

	}

	private final SecretSource _secret;

	/**
	 * Creates a {@link ClientSecretAuthentication} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ClientSecretAuthentication(InstantiationContext context, Config config) {
		super(context, config);
		_secret = context.getInstance(config.getSecret());
	}

	@Override
	public com.nimbusds.oauth2.sdk.auth.ClientAuthentication createAuthentication(ClientID clientId,
			URI tokenEndpoint) {
		Secret secret = new Secret(_secret.getSecret());
		switch (getConfig().getMethod()) {
			case BASIC:
				return new ClientSecretBasic(clientId, secret);
			case POST:
				return new ClientSecretPost(clientId, secret);
		}
		throw new IllegalStateException("Unknown method: " + getConfig().getMethod());
	}

}
