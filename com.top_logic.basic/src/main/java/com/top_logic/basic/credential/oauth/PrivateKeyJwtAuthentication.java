/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential.oauth;

import java.security.PrivateKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretSourceFormat;

/**
 * {@link ClientAuthentication} with a JSON web token signed by the client's private key
 * (<code>private_key_jwt</code>, RFC 7523 §2.2).
 *
 * <p>
 * For each request a new assertion is created with the client identifier as issuer and subject,
 * the token endpoint as audience, a random identifier and a short lifetime. The public key is
 * registered for the client at the authorization server, the private key never leaves the
 * application.
 * </p>
 *
 * <pre>
 * &lt;client-authentication class="com.top_logic.basic.credential.oauth.PrivateKeyJwtAuthentication"
 *   algorithm="ES256"
 *   key-id="my-app-2026"
 * &gt;
 *   &lt;private-key class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/my-app-key.pem"/&gt;
 * &lt;/client-authentication&gt;
 * </pre>
 *
 * <p>
 * A matching key pair is created with
 * <code>openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out my-app-key.pem</code>
 * (ES256) or <code>openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out my-app-key.pem</code>
 * (RS256), the public key to register with <code>openssl pkey -in my-app-key.pem -pubout</code>.
 * </p>
 */
@Label("Private key JWT authentication")
public class PrivateKeyJwtAuthentication extends AbstractConfiguredInstance<PrivateKeyJwtAuthentication.Config>
		implements ClientAuthentication {

	/**
	 * Configuration options for {@link PrivateKeyJwtAuthentication}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<PrivateKeyJwtAuthentication> {

		/**
		 * Tag name of this configuration in a list of {@link ClientAuthentication}s.
		 */
		String TAG_NAME = "private-key-jwt";

		/**
		 * @see #getPrivateKey()
		 */
		String PRIVATE_KEY = "private-key";

		/**
		 * @see #getKeyId()
		 */
		String KEY_ID = "key-id";

		/**
		 * @see #getAlgorithm()
		 */
		String ALGORITHM = "algorithm";

		/**
		 * @see #getAssertionLifetime()
		 */
		String ASSERTION_LIFETIME = "assertion-lifetime";

		/**
		 * The private key of the client in PKCS#8 PEM format (<code>-----BEGIN PRIVATE
		 * KEY-----</code>).
		 */
		@Name(PRIVATE_KEY)
		@Mandatory
		@Format(SecretSourceFormat.class)
		PolymorphicConfiguration<? extends SecretSource> getPrivateKey();

		/**
		 * @see #getPrivateKey()
		 */
		void setPrivateKey(PolymorphicConfiguration<? extends SecretSource> value);

		/**
		 * Identifier of the key, sent in the header of the assertion.
		 *
		 * <p>
		 * Allows the authorization server to select the public key when several keys are
		 * registered for the client, e.g. during a key rotation.
		 * </p>
		 */
		@Name(KEY_ID)
		String getKeyId();

		/**
		 * @see #getKeyId()
		 */
		void setKeyId(String value);

		/**
		 * The signature algorithm, which determines the type of the private key.
		 */
		@Name(ALGORITHM)
		@FormattedDefault("RS256")
		JwsAlgorithm getAlgorithm();

		/**
		 * @see #getAlgorithm()
		 */
		void setAlgorithm(JwsAlgorithm value);

		/**
		 * Time until the assertion expires.
		 */
		@Name(ASSERTION_LIFETIME)
		@Format(MillisFormat.class)
		@FormattedDefault("60s")
		long getAssertionLifetime();

		/**
		 * @see #getAssertionLifetime()
		 */
		void setAssertionLifetime(long value);

	}

	private final SecretSource _privateKey;

	/**
	 * Creates a {@link PrivateKeyJwtAuthentication} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public PrivateKeyJwtAuthentication(InstantiationContext context, Config config) {
		super(context, config);
		_privateKey = context.getInstance(config.getPrivateKey());
	}

	@Override
	public void authenticate(String clientId, OAuthRequest request) {
		Config config = getConfig();
		JwsAlgorithm algorithm = config.getAlgorithm();
		PrivateKey key = algorithm.parsePrivateKey(_privateKey.getSecret());

		long now = System.currentTimeMillis() / 1000;
		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put(OAuthConstants.CLAIM_ISSUER, clientId);
		claims.put(OAuthConstants.CLAIM_SUBJECT, clientId);
		claims.put(OAuthConstants.CLAIM_AUDIENCE, request.getAudience());
		claims.put(OAuthConstants.CLAIM_JWT_ID, UUID.randomUUID().toString());
		claims.put(OAuthConstants.CLAIM_ISSUED_AT, Long.valueOf(now));
		claims.put(OAuthConstants.CLAIM_EXPIRATION, Long.valueOf(now + config.getAssertionLifetime() / 1000));

		request.setParameter(OAuthConstants.CLIENT_ID, clientId);
		request.setParameter(OAuthConstants.CLIENT_ASSERTION_TYPE, OAuthConstants.CLIENT_ASSERTION_TYPE_JWT_BEARER);
		request.setParameter(OAuthConstants.CLIENT_ASSERTION,
			JsonWebToken.sign(algorithm, key, config.getKeyId(), claims));
	}

}
