/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.credential;

import java.net.URI;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.BasicCredentials;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.NoCredentials;
import com.top_logic.basic.credential.RequestAuthentication;

/**
 * Test case for the static {@link CredentialProvider}s {@link HeaderCredentials},
 * {@link BasicCredentials} and {@link NoCredentials}.
 */
@SuppressWarnings("javadoc")
public class TestCredentialProvider extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	public interface Config extends ConfigurationItem {

		String CREDENTIALS = "credentials";

		String PROVIDERS = "providers";

		@Name(CREDENTIALS)
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		@Name(PROVIDERS)
		List<PolymorphicConfiguration<? extends CredentialProvider>> getProviders();

	}

	public void testBearer() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("Authorization", "Bearer s3cr3t"), headers);
	}

	public void testCustomHeaderWithoutScheme() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' header='x-api-key' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("x-api-key", "s3cr3t"), headers);
	}

	public void testCustomScheme() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + HeaderCredentials.class.getName()
				+ "' scheme='DeepL-Auth-Key' secret='unencrypted:s3cr3t'/>"));

		assertEquals(Collections.singletonMap("Authorization", "DeepL-Auth-Key s3cr3t"), headers);
	}

	public void testBasic() throws ConfigurationException {
		Map<String, String> headers = authenticate(provider(
			"<credentials class='" + BasicCredentials.class.getName()
				+ "' user='service-user' password='unencrypted:pässwörd'/>"));

		String expected = "Basic " + Base64.getEncoder()
			.encodeToString("service-user:pässwörd".getBytes(StandardCharsets.UTF_8));
		assertEquals(Collections.singletonMap("Authorization", expected), headers);
	}

	public void testNoCredentials() throws ConfigurationException {
		Map<String, String> headers =
			authenticate(provider("<credentials class='" + NoCredentials.class.getName() + "'/>"));

		assertEquals(Collections.emptyMap(), headers);
	}

	public void testProviderList() throws ConfigurationException {
		Config config = read("<config><providers>"
			+ "<" + HeaderCredentials.Config.TAG_NAME + " header='api-key' secret='unencrypted:key'/>"
			+ "<" + BasicCredentials.Config.TAG_NAME + " user='u' password='unencrypted:p'/>"
			+ "<" + NoCredentials.Config.TAG_NAME + "/>"
			+ "</providers></config>");

		List<CredentialProvider> providers = TypedConfiguration.getInstanceList(context, config.getProviders());
		assertEquals(3, providers.size());
		assertEquals(Collections.singletonMap("api-key", "key"), authenticate(providers.get(0)));
	}

	public void testHttpRequestBuilderAdapter() throws ConfigurationException {
		CredentialProvider provider = provider(
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer' secret='unencrypted:s3cr3t'/>");
		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost/"));

		provider.authenticate(RequestAuthentication.of(builder));

		assertEquals("Bearer s3cr3t", builder.build().headers().firstValue("Authorization").orElse(null));
	}

	private CredentialProvider provider(String xml) throws ConfigurationException {
		Config config = read("<config>" + xml + "</config>");
		return getInstance(config.getCredentials());
	}

	private static Map<String, String> authenticate(CredentialProvider provider) {
		Map<String, String> headers = new HashMap<>();
		provider.authenticate(headers::put);
		return headers;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestCredentialProvider.class);
	}

}
