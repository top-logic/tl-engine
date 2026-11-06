/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.auth.pac4j.config;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.security.auth.pac4j.config.ClientConfigurator;
import com.top_logic.security.auth.pac4j.config.DefaultOidcClientConfigurator;
import com.top_logic.security.auth.pac4j.config.keycloak.KeycloakClientConfigurator;

/**
 * Tests that a {@link DefaultOidcClientConfigurator} is
 * {@link ClientConfigurator#isAvailable() available} only with a source of provider metadata and an
 * available client secret.
 */
@SuppressWarnings("javadoc")
public class TestOidcClientAvailability extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property a client secret is referenced by, never set in this test. */
	private static final String MISSING_VARIABLE = "test_oidc_availability_missing_secret";

	private static final String SECRET_ATTRIBUTE =
		" " + DefaultOidcClientConfigurator.Config.SECRET + "='unencrypted:the-client-secret'";

	private static final String DISCOVERY_URI_ATTRIBUTE = " " + DefaultOidcClientConfigurator.Config.DISCOVERY_URI
		+ "='https://provider.test/.well-known/openid-configuration'";

	private static final String PROVIDER_METADATA_ATTRIBUTE = " "
		+ DefaultOidcClientConfigurator.Config.PROVIDER_METADATA + "='{"
		+ "\"issuer\":\"https://provider.test\","
		+ "\"authorization_endpoint\":\"https://provider.test/auth\","
		+ "\"token_endpoint\":\"https://provider.test/token\","
		+ "\"jwks_uri\":\"https://provider.test/jwks\","
		+ "\"response_types_supported\":[\"code\"],"
		+ "\"subject_types_supported\":[\"public\"],"
		+ "\"id_token_signing_alg_values_supported\":[\"RS256\"]"
		+ "}'";

	public interface Config extends ConfigurationItem {

		String CLIENT = "client";

		@Name(CLIENT)
		DefaultOidcClientConfigurator.Config<?> getClient();

	}

	/** A client without discovery URI and provider metadata is not offered. */
	public void testNoProviderMetadataSource() throws Exception {
		assertFalse(configurator(DefaultOidcClientConfigurator.class, SECRET_ATTRIBUTE, "").isAvailable());
	}

	/** A client with a discovery URI is offered. */
	public void testDiscoveryURI() throws Exception {
		assertTrue(configurator(DefaultOidcClientConfigurator.class, SECRET_ATTRIBUTE + DISCOVERY_URI_ATTRIBUTE, "")
			.isAvailable());
	}

	/** A client with provider metadata is offered. */
	public void testProviderMetadata() throws Exception {
		assertTrue(
			configurator(DefaultOidcClientConfigurator.class, SECRET_ATTRIBUTE + PROVIDER_METADATA_ATTRIBUTE, "")
				.isAvailable());
	}

	/** A Keycloak client derives its discovery URI from base URI and realm and is offered. */
	public void testKeycloak() throws Exception {
		assertTrue(configurator(KeycloakClientConfigurator.class,
			SECRET_ATTRIBUTE + " base-uri='https://keycloak.test' realm='test-realm'", "").isAvailable());
	}

	/** A client with discovery URI but without available secret is not offered. */
	public void testMissingSecret() throws Exception {
		assertFalse(configurator(DefaultOidcClientConfigurator.class, DISCOVERY_URI_ATTRIBUTE,
			"<" + DefaultOidcClientConfigurator.Config.SECRET + " name='" + MISSING_VARIABLE + "'/>").isAvailable());
	}

	private ClientConfigurator configurator(Class<?> implementation, String attributes, String content)
			throws Exception {
		Config config = read("<" + ROOT_TAG + "><" + Config.CLIENT + " class='" + implementation.getName() + "'"
			+ " name='test-oidc' client-id='the-client-id'" + attributes + ">"
			+ content
			+ "</" + Config.CLIENT + "></" + ROOT_TAG + ">");
		return getInstance(config.getClient());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestOidcClientAvailability.class, SecretStoreService.Module.INSTANCE);
	}

}
