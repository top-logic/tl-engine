/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.s3;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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
import com.top_logic.storage.s3.S3BlobStore;

/**
 * Test of the presigned URLs for direct downloads issued by {@link S3BlobStore}.
 *
 * <p>
 * Presigning is computed locally, so the test needs no storage.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestS3DirectDownload extends BasicTestCase {

	private static final String STORE_TAG = "store";

	private static final String ENDPOINT = "http://localhost:9000";

	private static final String BUCKET = "data";

	private static final String PREFIX = "blobs/";

	private static final long MIB = 1024L * 1024;

	private static final String PDF = "application/pdf";

	private static final String PARAM_EXPIRES = "X-Amz-Expires";

	private static final String PARAM_CREDENTIAL = "X-Amz-Credential";

	private static final String PARAM_SIGNATURE = "X-Amz-Signature";

	private static final String PARAM_CONTENT_TYPE = "response-content-type";

	private static final String PARAM_CONTENT_DISPOSITION = "response-content-disposition";

	private static final String PARAM_CACHE_CONTROL = "response-cache-control";

	private final String _key = UUID.randomUUID().toString();

	public void testParseConfig() throws Exception {
		S3BlobStore.Config<?> config = parse(
			"<store name='s3' class='" + S3BlobStore.class.getName() + "' bucket='data'"
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
		S3BlobStore.Config<?> config =
			parse("<store name='s3' class='" + S3BlobStore.class.getName() + "' bucket='data'/>");
		assertFalse(config.getDirectDownload());
		assertEquals(S3BlobStore.DEFAULT_DIRECT_DOWNLOAD_MIN_SIZE, config.getDirectDownloadMinSize());
		assertEquals(MIB, config.getDirectDownloadMinSize());
		assertEquals(S3BlobStore.DEFAULT_DIRECT_DOWNLOAD_LIFETIME, config.getDirectDownloadLifetime());
		assertEquals("", config.getPublicEndpoint());
	}

	/** Without enabling, no URL is issued. */
	public void testDisabled() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setDirectDownload(false);
		try (S3BlobStore store = create(config)) {
			assertNull(store.createDownloadUrl(_key, 100 * MIB, PDF, "a.pdf"));
		}
	}

	/** Content below the minimum size is streamed. */
	public void testMinSize() throws Exception {
		try (S3BlobStore store = create(newConfig())) {
			assertNull(store.createDownloadUrl(_key, MIB - 1, PDF, "a.pdf"));
			assertNull("Content of unknown size is streamed.", store.createDownloadUrl(_key, -1, PDF, "a.pdf"));
			assertNotNull(store.createDownloadUrl(_key, MIB, PDF, "a.pdf"));
		}
	}

	/** The URL addresses the object and forces the response headers. */
	public void testPresignedUrl() throws Exception {
		String fileName = "Übersicht März \"final\".pdf";
		try (S3BlobStore store = create(newConfig())) {
			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, fileName);

			assertEquals("http", url.getScheme());
			assertEquals("localhost", url.getHost());
			assertEquals(9000, url.getPort());
			assertEquals("/" + BUCKET + "/" + PREFIX + _key, url.getPath());

			Map<String, String> query = query(url);
			assertEquals("60", query.get(PARAM_EXPIRES));
			assertNotNull(query.get(PARAM_SIGNATURE));
			assertTrue(query.get(PARAM_CREDENTIAL).startsWith("admin/"));
			assertEquals(PDF, query.get(PARAM_CONTENT_TYPE));
			assertEquals(S3BlobStore.DIRECT_DOWNLOAD_CACHE_CONTROL, query.get(PARAM_CACHE_CONTROL));
			assertEquals("private, no-store", query.get(PARAM_CACHE_CONTROL));

			String disposition = query.get(PARAM_CONTENT_DISPOSITION);
			assertEquals(ContentDisposition.headerValue(ContentDisposition.INLINE, fileName), disposition);
			assertEquals(
				"inline; filename=\"_bersicht M_rz _final_.pdf\"; filename*=UTF-8''%C3%9Cbersicht%20M%C3%A4rz%20%22final%22.pdf",
				disposition);
		}
	}

	/** Without file name, the disposition carries no file name. */
	public void testNoFileName() throws Exception {
		try (S3BlobStore store = create(newConfig())) {
			Map<String, String> query = query(store.createDownloadUrl(_key, 2 * MIB, null, null));
			assertEquals(ContentDisposition.INLINE, query.get(PARAM_CONTENT_DISPOSITION));
			assertEquals("application/octet-stream", query.get(PARAM_CONTENT_TYPE));
		}
	}

	/** URLs are signed for the public endpoint, if configured. */
	public void testPublicEndpoint() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setPublicEndpoint("https://files.example.com");
		try (S3BlobStore store = create(config)) {
			assertEquals(URI.create("https://files.example.com"), store.getPublicEndpoint());
			assertEquals(URI.create(ENDPOINT), store.getEndpoint());

			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf");
			assertEquals("https", url.getScheme());
			assertEquals("files.example.com", url.getHost());
			assertEquals("/" + BUCKET + "/" + PREFIX + _key, url.getPath());
		}
	}

	/** Without path-style access, the bucket is part of the host name. */
	public void testVirtualHostedStyle() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setPathStyleAccess(false);
		try (S3BlobStore store = create(config)) {
			URI url = store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf");
			assertEquals(BUCKET + ".localhost", url.getHost());
			assertEquals("/" + PREFIX + _key, url.getPath());
		}
	}

	/** The configured lifetime is the validity of the signature. */
	public void testLifetime() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setDirectDownloadLifetime(5L * 60 * 1000);
		try (S3BlobStore store = create(config)) {
			assertEquals(Duration.ofMinutes(5), store.getDirectDownloadLifetime());
			assertEquals("300", query(store.createDownloadUrl(_key, 2 * MIB, PDF, "a.pdf")).get(PARAM_EXPIRES));
		}
	}

	/** The lifetime is limited by the maximum validity of a signature in S3. */
	public void testInvalidLifetime() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setDirectDownloadLifetime(S3BlobStore.MAX_DIRECT_DOWNLOAD_LIFETIME + 1);
		assertConfigError(config);

		config.setDirectDownloadLifetime(0);
		assertConfigError(config);
	}

	/** A public endpoint must be an absolute URL. */
	public void testInvalidPublicEndpoint() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setPublicEndpoint("files.example.com");
		assertConfigError(config);
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

	private static void assertConfigError(S3BlobStore.Config<?> config) throws Exception {
		BufferingProtocol log = new BufferingProtocol();
		InstantiationContext context = new SimpleInstantiationContext(log);
		BlobStore store = context.getInstance(config);
		if (store != null) {
			store.close();
		}
		assertTrue("Configuration error expected.", log.hasErrors());
	}

	private static S3BlobStore.Config<?> newConfig() {
		S3BlobStore.Config<?> config = TypedConfiguration.newConfigItem(S3BlobStore.Config.class);
		config.setName("s3");
		config.setBucket(BUCKET);
		config.setPrefix(PREFIX);
		config.setEndpoint(ENDPOINT);
		config.setPathStyleAccess(true);
		config.setAccessKey("admin");
		config.setSecretKey("secret");
		config.setDirectDownload(true);
		return config;
	}

	@SuppressWarnings("unchecked")
	private static S3BlobStore.Config<?> parse(String xml) throws Exception {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(BlobStore.Config.class);
		return (S3BlobStore.Config<?>) new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			Collections.singletonMap(STORE_TAG, descriptor))
				.setSource(CharacterContents.newContent(xml))
				.read();
	}

	private static S3BlobStore create(S3BlobStore.Config<?> config) {
		return (S3BlobStore) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestS3DirectDownload.class);
	}

}
