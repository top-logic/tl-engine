/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.oauth;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.JWTAuthentication;
import com.nimbusds.oauth2.sdk.auth.JWTAuthenticationClaimsSet;
import com.nimbusds.oauth2.sdk.auth.PrivateKeyJWT;
import com.nimbusds.oauth2.sdk.id.Audience;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.JWTID;

import com.top_logic.basic.StringServices;
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
import com.top_logic.basic.exception.I18NRuntimeException;

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
 * &lt;client-authentication class="com.top_logic.credential.oauth.PrivateKeyJwtAuthentication"
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
 * Instead of a PEM file, the private key can be given as JSON web key (JWK) with its private
 * part, as exported e.g. by a key management service.
 * </p>
 *
 * @implNote The assertion is created and signed by {@link PrivateKeyJWT} of the Nimbus OAuth 2.0
 *           SDK.
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
		 * KEY-----</code>) or as JSON web key (JWK) including its private part.
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
		 * registered for the client, e.g. during a key rotation. If empty, the identifier of a
		 * JSON web key is used.
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

	private static final String JSON_OBJECT_START = "{";

	private static final String PEM_BEGIN = "-----BEGIN PRIVATE KEY-----";

	private static final String PEM_END = "-----END PRIVATE KEY-----";

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

	/**
	 * Whether the private key is available now.
	 *
	 * @see SecretSource#isAvailable()
	 */
	@Override
	public boolean isAvailable() {
		return _privateKey.isAvailable();
	}

	@Override
	public com.nimbusds.oauth2.sdk.auth.ClientAuthentication createAuthentication(ClientID clientId,
			URI tokenEndpoint) {
		Config config = getConfig();
		JwsAlgorithm algorithm = config.getAlgorithm();
		String text = _privateKey.getSecret().trim();
		String keyId = StringServices.nonEmpty(config.getKeyId());
		PrivateKey key;
		if (text.startsWith(JSON_OBJECT_START)) {
			JWK jwk = parseJwk(algorithm, text);
			key = toPrivateKey(algorithm, jwk);
			if (keyId == null) {
				keyId = jwk.getKeyID();
			}
		} else {
			key = parsePem(algorithm, text);
		}
		checkKey(algorithm, key);

		// Second precision, as in the assertion.
		long now = System.currentTimeMillis() / 1000 * 1000;
		JWTAuthenticationClaimsSet claims = new JWTAuthenticationClaimsSet(clientId,
			Collections.singletonList(new Audience(tokenEndpoint)),
			new Date(now + config.getAssertionLifetime()), null, new Date(now), new JWTID());
		PrivateKeyJWT jwt;
		try {
			jwt = new PrivateKeyJWT(claims, algorithm.getAlgorithm(), key, keyId, null);
		} catch (JOSEException | IllegalArgumentException ex) {
			// The cause is not reported, its message could quote parts of the key.
			throw new I18NRuntimeException(I18NConstants.ERROR_SIGNING_FAILED__ALGORITHM.fill(algorithm.name()));
		}
		return new ClientAssertion(ClientAuthenticationMethod.PRIVATE_KEY_JWT, clientId,
			JWTAuthentication.CLIENT_ASSERTION_TYPE, jwt.getClientAssertion().serialize());
	}

	private static JWK parseJwk(JwsAlgorithm algorithm, String json) {
		try {
			return JWK.parse(json);
		} catch (java.text.ParseException ex) {
			throw invalidKey(algorithm);
		}
	}

	private static PrivateKey toPrivateKey(JwsAlgorithm algorithm, JWK jwk) {
		try {
			PrivateKey key;
			if (jwk instanceof RSAKey) {
				key = ((RSAKey) jwk).toPrivateKey();
			} else if (jwk instanceof ECKey) {
				key = ((ECKey) jwk).toPrivateKey();
			} else {
				key = null;
			}
			if (key == null) {
				throw invalidKey(algorithm);
			}
			return key;
		} catch (JOSEException ex) {
			throw invalidKey(algorithm);
		}
	}

	private static PrivateKey parsePem(JwsAlgorithm algorithm, String pem) {
		int begin = pem.indexOf(PEM_BEGIN);
		int end = pem.indexOf(PEM_END);
		if (begin < 0 || end < begin) {
			throw invalidKey(algorithm);
		}
		String base64 = pem.substring(begin + PEM_BEGIN.length(), end);
		try {
			byte[] der = Base64.getMimeDecoder().decode(base64);
			return KeyFactory.getInstance(algorithm.getKeyType().getValue())
				.generatePrivate(new PKCS8EncodedKeySpec(der));
		} catch (IllegalArgumentException | GeneralSecurityException ex) {
			// The cause is not reported, its message could quote parts of the key.
			throw invalidKey(algorithm);
		}
	}

	/**
	 * Checks that the key fits the algorithm, including the curve of an elliptic curve key, which
	 * the signer does not check.
	 */
	private static void checkKey(JwsAlgorithm algorithm, PrivateKey key) {
		KeyType keyType = algorithm.getKeyType();
		if (!keyType.getValue().equals(key.getAlgorithm())) {
			throw invalidKey(algorithm);
		}
		if (KeyType.EC.equals(keyType)) {
			if (!(key instanceof ECPrivateKey)) {
				throw invalidKey(algorithm);
			}
			Curve curve = Curve.forECParameterSpec(((ECPrivateKey) key).getParams());
			Set<Curve> curves = Curve.forJWSAlgorithm(algorithm.getAlgorithm());
			if (curve == null || curves == null || !curves.contains(curve)) {
				throw invalidKey(algorithm);
			}
		}
	}

	private static I18NRuntimeException invalidKey(JwsAlgorithm algorithm) {
		return new I18NRuntimeException(I18NConstants.ERROR_INVALID_PRIVATE_KEY__ALGORITHM.fill(algorithm.name()));
	}

}
