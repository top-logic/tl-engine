/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.net.URI;

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.JWTAuthentication;
import com.nimbusds.oauth2.sdk.id.ClientID;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * {@link ClientAuthentication} with an assertion issued by a third party (RFC 7521 §4.2).
 *
 * <p>
 * This is workload identity: the platform running the application issues a short-lived token
 * identifying it, and the authorization server trusts that token issuer for the client. The
 * application owns no secret. A Kubernetes pod gets such a token as projected service account
 * token, refreshed by the kubelet in a file; with a {@link FileSecretSource} the current content is
 * sent with each token request:
 * </p>
 *
 * <pre>
 * &lt;client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication"&gt;
 *   &lt;assertion class="com.top_logic.basic.credential.FileSecretSource" path="/var/run/secrets/tokens/keycloak"/&gt;
 * &lt;/client-authentication&gt;
 * </pre>
 *
 * <p>
 * In Azure AKS with workload identity, the token file is named by the environment variable
 * <code>AZURE_FEDERATED_TOKEN_FILE</code>, referenced through an alias
 * (<code>path="%AZURE_FEDERATED_TOKEN_FILE%"</code>) defined in the application configuration as
 * <code>&lt;entry name="%AZURE_FEDERATED_TOKEN_FILE%" value="${env:AZURE_FEDERATED_TOKEN_FILE}"/&gt;</code>.
 * </p>
 */
public class ClientAssertionAuthentication extends AbstractConfiguredInstance<ClientAssertionAuthentication.Config>
		implements ClientAuthentication {

	/**
	 * Configuration options for {@link ClientAssertionAuthentication}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ClientAssertionAuthentication> {

		/**
		 * Tag name of this configuration in a list of {@link ClientAuthentication}s.
		 */
		String TAG_NAME = "client-assertion";

		/**
		 * @see #getAssertion()
		 */
		String ASSERTION = "assertion";

		/**
		 * @see #getAssertionType()
		 */
		String ASSERTION_TYPE = "assertion-type";

		/**
		 * The assertion identifying the client, typically a file with a token issued by the
		 * platform.
		 */
		@Name(ASSERTION)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getAssertion();

		/**
		 * @see #getAssertion()
		 */
		void setAssertion(PolymorphicConfiguration<? extends SecretSource> value);

		/**
		 * The format of the assertion.
		 */
		@Name(ASSERTION_TYPE)
		@StringDefault(JWTAuthentication.CLIENT_ASSERTION_TYPE)
		String getAssertionType();

		/**
		 * @see #getAssertionType()
		 */
		void setAssertionType(String value);

	}

	private final SecretSource _assertion;

	/**
	 * Creates a {@link ClientAssertionAuthentication} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ClientAssertionAuthentication(InstantiationContext context, Config config) {
		super(context, config);
		_assertion = context.getInstance(config.getAssertion());
	}

	@Override
	public com.nimbusds.oauth2.sdk.auth.ClientAuthentication createAuthentication(ClientID clientId,
			URI tokenEndpoint) {
		return new ClientAssertion(ClientAuthenticationMethod.PRIVATE_KEY_JWT, clientId,
			getConfig().getAssertionType(), _assertion.getSecret());
	}

	/**
	 * Whether the assertion is available now.
	 *
	 * @see SecretSource#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return _assertion.isAvailable();
	}

}
