/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ContentDisposition;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.storage.azure.AzureBlobStore;

/**
 * Test of the URLs with shared access signature for direct downloads issued by
 * {@link AzureBlobStore}.
 *
 * <p>
 * The signature is computed locally from the account key, so the test needs no storage.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAzureDirectDownload extends BasicTestCase {

	private static final String STORE_TAG = "store";

	private static final String ACCOUNT = "devstoreaccount1";

	private static final String KEY = Base64.getEncoder().encodeToString("account-key-for-testing".getBytes());

	private static final String ENDPOINT = "http://127.0.0.1:10000/" + ACCOUNT;

	private static final String CONTAINER = "data";

	private static final String PREFIX = "blobs/";

	private static final long MIB = 1024L * 1024;

	private static final String PDF = "application/pdf";

	private static final String PARAM_PERMISSIONS = "sp";

	private static final String PARAM_RESOURCE = "sr";

	private static final String PARAM_EXPIRY = "se";

	private static final String PARAM_SIGNATURE = "sig";

	private static final String PARAM_VERSION = "sv";

	private static final String PARAM_CONTENT_TYPE = "rsct";

	private static final String PARAM_CONTENT_DISPOSITION = "rscd";

	private static final String PARAM_CACHE_CONTROL = "rscc";

	/** Tolerance for comparing the expiry with the local clock. */
	private static final Duration CLOCK_TOLERANCE = Duration.ofSeconds(10);

	private final String _key = UUID.randomUUID().toString();

	public void testParseConfig() throws Exception {
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "' container='data'"
				+ " account-name='" + ACCOUNT + "' account-key='unencrypted:" + KEY + "'"
				+ " direct-download='true'"
				+ " direct-download-min-size='2MB'"
				+ " direct-download-lifetime='5min'"
				+ " public-endpoint='https://files.example.com'"
				+ "/>");
		assertTrue(config.getDirectDownload());
		assertEquals(2 * MIB, config.getDirectDownloadMinSize());
		assertEquals(5L * 60 * 1000, config.getDirectDownloadLifetime());
		assertEquals("https://files.example.com", config.getPublicEndpoint());
	}

	public void testDefaults() throws Exception {
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "' container='data'"
				+ " account-name='" + ACCOUNT + "' account-key='unencrypted:" + KEY + "'/>");
		assertFalse(config.getDirectDownload());
		assertEquals(AzureBlobStore.DEFAULT_DIRECT_DOWNLOAD_MIN_SIZE, config.getDirectDownloadMinSize());
		assertEquals(MIB, config.getDirectDownloadMinSize());
		assertEquals(AzureBlobStore.DEFAULT_DIRECT_DOWNLOAD_LIFETIME, config.getDirectDownloadLifetime());
		assertEquals("", config.getPublicEndpoint());
	}

	/** Without enabling, no URL is issued. */
	public void testDisabled() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setDirectDownload(false);
		try (AzureBlobStore store = create(config)) {
			assertNull(store.createDownloadUrl(_key, 100 * MIB, PDF, "a.pdf"));
		}
	}

	/** Content below the minimum size is streamed. */
	public void testMinSize() throws Exception {
		try (AzureBlobStore store = create(newConfig())) {
			assertNull(store.createDownloadUrl(_key, MIB - 1, PDF, "a.pdf"));
			assertNull("Content of unknown size is streamed.", store.createDownloadUrl(_key, -1, PDF, "a.pdf"));
			assertNotNull(store.createDownloadUrl(_key, MIB, PDF, "a.pdf"));
		}
	}

	/** Without account key, no URL can be signed. */
	public void testNoAccountKey() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setAccountName("");
		config.setAccountKey("");
		config.setEndpoint("");
		config.setConnectionString("BlobEndpoint=" + ENDPOINT + ";SharedAccessSignature=sv=2024-08-04&ss=b&srt=sco"
			+ "&sp=rwdlac&se=2030-01-01T00:00:00Z&sig=abc");
		try (AzureBlobStore store = create(config)) {
			assertNull(store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf"));
		}
	}

	/** The URL addresses the blob, grants read access only and forces the response headers. */
	public void testSignedUrl() throws Exception {
		String fileName = "Übersicht März \"final\".pdf";
		try (AzureBlobStore store = create(newConfig())) {
			Instant before = Instant.now();
			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, fileName);
			Instant after = Instant.now();

			assertEquals("http", url.getScheme());
			assertEquals("127.0.0.1", url.getHost());
			assertEquals(10000, url.getPort());
			assertEquals("/" + ACCOUNT + "/" + CONTAINER + "/" + PREFIX + _key, url.getPath());

			Map<String, String> query = query(url);
			assertEquals("r", query.get(PARAM_PERMISSIONS));
			assertEquals("b", query.get(PARAM_RESOURCE));
			assertNotNull(query.get(PARAM_SIGNATURE));
			assertNotNull(query.get(PARAM_VERSION));
			assertExpiry(query, before, after, Duration.ofSeconds(60));
			assertEquals(PDF, query.get(PARAM_CONTENT_TYPE));
			assertEquals(AzureBlobStore.DIRECT_DOWNLOAD_CACHE_CONTROL, query.get(PARAM_CACHE_CONTROL));
			assertEquals("private, no-store", query.get(PARAM_CACHE_CONTROL));

			String disposition = query.get(PARAM_CONTENT_DISPOSITION);
			assertEquals(ContentDisposition.headerValue(ContentDisposition.INLINE, fileName), disposition);
			assertEquals(
				"inline; filename=\"_bersicht M_rz _final_.pdf\"; filename*=UTF-8''%C3%9Cbersicht%20M%C3%A4rz%20%22final%22.pdf",
				disposition);
		}
	}

	/** The signature depends on the key and the forced headers. */
	public void testSignatureDiffers() throws Exception {
		try (AzureBlobStore store = create(newConfig())) {
			String pdf = query(store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf")).get(PARAM_SIGNATURE);
			String other = query(store.createDownloadUrl(_key, 2 * MIB, PDF, "b.pdf")).get(PARAM_SIGNATURE);
			assertFalse("The file name must be part of the signature.", pdf.equals(other));
		}
	}

	/** Without file name, the disposition carries no file name. */
	public void testNoFileName() throws Exception {
		try (AzureBlobStore store = create(newConfig())) {
			Map<String, String> query = query(store.createDownloadUrl(_key, 2 * MIB, null, null));
			assertEquals(ContentDisposition.INLINE, query.get(PARAM_CONTENT_DISPOSITION));
			assertEquals("application/octet-stream", query.get(PARAM_CONTENT_TYPE));
		}
	}

	/** URLs are issued for the public endpoint, if configured. */
	public void testPublicEndpoint() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setPublicEndpoint("https://files.example.com/");
		try (AzureBlobStore store = create(config)) {
			assertEquals(URI.create("https://files.example.com/"), store.getPublicEndpoint());

			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf");
			assertEquals("https", url.getScheme());
			assertEquals("files.example.com", url.getHost());
			assertEquals(-1, url.getPort());
			assertEquals("/" + CONTAINER + "/" + PREFIX + _key, url.getPath());
			assertNotNull(query(url).get(PARAM_SIGNATURE));
		}
	}

	/** Without public endpoint, URLs are issued for the endpoint of the account. */
	public void testAccountEndpoint() throws Exception {
		try (AzureBlobStore store = create(newConfig())) {
			assertEquals(URI.create(ENDPOINT), store.getPublicEndpoint());
		}
	}

	/** The configured lifetime is the validity of the signature. */
	public void testLifetime() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setDirectDownloadLifetime(5L * 60 * 1000);
		try (AzureBlobStore store = create(config)) {
			assertEquals(Duration.ofMinutes(5), store.getDirectDownloadLifetime());
			Instant before = Instant.now();
			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf");
			Instant after = Instant.now();
			assertExpiry(query(url), before, after, Duration.ofMinutes(5));
		}
	}

	/** The lifetime is limited. */
	public void testInvalidLifetime() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setDirectDownloadLifetime(AzureBlobStore.MAX_DIRECT_DOWNLOAD_LIFETIME + 1);
		assertConfigError(config);

		config.setDirectDownloadLifetime(0);
		assertConfigError(config);
	}

	/** A public endpoint must be an absolute URL. */
	public void testInvalidPublicEndpoint() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setPublicEndpoint("files.example.com");
		assertConfigError(config);
	}

	private static void assertExpiry(Map<String, String> query, Instant before, Instant after, Duration lifetime) {
		Instant expiry = OffsetDateTime.parse(query.get(PARAM_EXPIRY)).toInstant();
		assertFalse("Expiry too early: " + expiry, expiry.isBefore(before.plus(lifetime).minus(CLOCK_TOLERANCE)));
		assertFalse("Expiry too late: " + expiry, expiry.isAfter(after.plus(lifetime).plus(CLOCK_TOLERANCE)));
	}

	private static Map<String, String> query(URI url) {
		Map<String, String> result = new HashMap<>();
		for (String param : url.getRawQuery().split("&")) {
			int separator = param.indexOf('=');
			String name = URLDecoder.decode(param.substring(0, separator), StandardCharsets.UTF_8);
			String value = URLDecoder.decode(param.substring(separator + 1), StandardCharsets.UTF_8);
			assertNull("Duplicate parameter: " + name, result.put(name, value));
		}
		return result;
	}

	private static void assertConfigError(AzureBlobStore.Config<?> config) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new SimpleInstantiationContext(log);
		BlobStore store = context.getInstance(config);
		if (store != null) {
			store.close();
		}
		assertTrue("Configuration error expected.", log.hasErrors());
	}

	private static AzureBlobStore.Config<?> newConfig() {
		AzureBlobStore.Config<?> config = TypedConfiguration.newConfigItem(AzureBlobStore.Config.class);
		config.setName("azure");
		config.setContainer(CONTAINER);
		config.setPrefix(PREFIX);
		config.setEndpoint(ENDPOINT);
		config.setAccountName(ACCOUNT);
		config.setAccountKey(KEY);
		config.setDirectDownload(true);
		return config;
	}

	@SuppressWarnings("unchecked")
	private static AzureBlobStore.Config<?> parse(String xml) throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(BlobStore.Config.class);
		return (AzureBlobStore.Config<?>) new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, Collections.singletonMap(STORE_TAG, descriptor))
				.setSource(CharacterContents.newContent(xml))
				.read();
	}

	private static AzureBlobStore create(AzureBlobStore.Config<?> config) {
		return (AzureBlobStore) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestAzureDirectDownload.class);
	}

}
