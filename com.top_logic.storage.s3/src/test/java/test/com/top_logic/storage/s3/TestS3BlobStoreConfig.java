/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.s3;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.storage.s3.S3BlobStore;
import com.top_logic.storage.s3.ServerSideEncryptionMode;

/**
 * Test of the configuration, the key mapping and the request formatting of {@link S3BlobStore},
 * without accessing a storage.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestS3BlobStoreConfig extends BasicTestCase {

	private static final String STORE_TAG = "store";

	/** All settings are read from XML. */
	public void testParseConfig() throws Exception {
		String secret = "s3cr3t";
		S3BlobStore.Config<?> config = parse(
			"<store name='s3' class='" + S3BlobStore.class.getName() + "'"
				+ " endpoint='http://localhost:9000'"
				+ " region='eu-central-1'"
				+ " bucket='data'"
				+ " prefix='blobs/'"
				+ " path-style-access='true'"
				+ " access-key='admin'"
				+ " secret-key='" + ConfigurationEncryption.encrypt(secret) + "'"
				+ " server-side-encryption='sse-kms'"
				+ " kms-key-id='my-key'"
				+ " multipart-threshold='1MB'"
				+ " part-size='6MB'"
				+ "/>");

		assertEquals("s3", config.getName());
		assertEquals("http://localhost:9000", config.getEndpoint());
		assertEquals("eu-central-1", config.getRegion());
		assertEquals("data", config.getBucket());
		assertEquals("blobs/", config.getPrefix());
		assertTrue(config.getPathStyleAccess());
		assertEquals("admin", config.getAccessKey());
		assertEquals("The secret key is decrypted.", secret, config.getSecretKey());
		assertEquals(ServerSideEncryptionMode.SSE_KMS, config.getServerSideEncryption());
		assertEquals("my-key", config.getKmsKeyId());
		assertEquals(1024L * 1024, config.getMultipartThreshold());
		assertEquals(6L * 1024 * 1024, config.getPartSize());

		try (S3BlobStore store = create(config)) {
			assertEquals(URI.create("http://localhost:9000"), store.getEndpoint());
			assertEquals(Region.EU_CENTRAL_1, store.getRegion());
			assertEquals("data", store.getBucket());
			assertEquals("blobs/", store.getPrefix());
			assertEquals(ServerSideEncryption.AWS_KMS, store.getServerSideEncryption());
			assertEquals("my-key", store.getKmsKeyId());

			AwsCredentials credentials = store.getCredentialsProvider().resolveCredentials();
			assertEquals("admin", credentials.accessKeyId());
			assertEquals(secret, credentials.secretAccessKey());
		}
	}

	/** A plain text secret is given with the prefix for unencrypted values. */
	public void testUnencryptedSecret() throws Exception {
		S3BlobStore.Config<?> config = parse(
			"<store name='s3' class='" + S3BlobStore.class.getName() + "' bucket='data'"
				+ " access-key='admin' secret-key='unencrypted:plain'/>");
		assertEquals("plain", config.getSecretKey());
	}

	/** Defaults of the optional settings. */
	public void testDefaults() throws Exception {
		S3BlobStore.Config<?> config =
			parse("<store name='s3' class='" + S3BlobStore.class.getName() + "' bucket='data'/>");

		assertEquals(S3BlobStore.DEFAULT_REGION, config.getRegion());
		assertEquals("", config.getEndpoint());
		assertEquals("", config.getPrefix());
		assertFalse(config.getPathStyleAccess());
		assertEquals(ServerSideEncryptionMode.SSE_S3, config.getServerSideEncryption());
		assertEquals(S3BlobStore.DEFAULT_MULTIPART_THRESHOLD, config.getMultipartThreshold());
		assertEquals(S3BlobStore.DEFAULT_PART_SIZE, config.getPartSize());

		try (S3BlobStore store = create(config)) {
			assertNull("AWS S3 is addressed through the region.", store.getEndpoint());
			assertEquals(Region.US_EAST_1, store.getRegion());
			assertEquals(ServerSideEncryption.AES256, store.getServerSideEncryption());
			assertNull(store.getKmsKeyId());
			assertTrue("Without keys, the default credentials provider chain is used.",
				store.getCredentialsProvider() instanceof DefaultCredentialsProvider);
		}
	}

	/** The mapping of encryption modes to the encryption requested from the storage. */
	public void testEncryptionModes() throws Exception {
		S3BlobStore.Config<?> config = newConfig();

		config.setServerSideEncryption(ServerSideEncryptionMode.NONE);
		config.setKmsKeyId("ignored");
		try (S3BlobStore store = create(config)) {
			assertNull(store.getServerSideEncryption());
			assertNull("The key ID is only used for SSE-KMS.", store.getKmsKeyId());
		}

		config.setServerSideEncryption(ServerSideEncryptionMode.SSE_S3);
		try (S3BlobStore store = create(config)) {
			assertEquals(ServerSideEncryption.AES256, store.getServerSideEncryption());
			assertNull("The key ID is only used for SSE-KMS.", store.getKmsKeyId());
		}

		config.setServerSideEncryption(ServerSideEncryptionMode.SSE_KMS);
		config.setKmsKeyId("");
		try (S3BlobStore store = create(config)) {
			assertEquals(ServerSideEncryption.AWS_KMS, store.getServerSideEncryption());
			assertNull("Without key ID, the default key is used.", store.getKmsKeyId());
		}
	}

	/** Object names are the prefix followed by the key. */
	public void testObjectNames() throws Exception {
		String key = UUID.randomUUID().toString();

		S3BlobStore.Config<?> config = newConfig();
		config.setPrefix("blobs/");
		try (S3BlobStore store = create(config)) {
			assertEquals("blobs/" + key, store.getObjectName(key));
			assertEquals(key, store.getKey("blobs/" + key));

			assertNull("Not below the prefix.", store.getKey(key));
			assertNull("Not below the prefix.", store.getKey("other/" + key));
			assertNull("In a deeper directory.", store.getKey("blobs/sub/" + key));
			assertNull("Not a key.", store.getKey("blobs/README"));
			assertNull("Not a canonical key.", store.getKey("blobs/" + key.toUpperCase()));
			assertNull(store.getKey("blobs/"));
			assertNull(store.getKey(null));
		}

		config.setPrefix("");
		try (S3BlobStore store = create(config)) {
			assertEquals(key, store.getObjectName(key));
			assertEquals(key, store.getKey(key));
			assertNull(store.getKey("blobs/" + key));
		}
	}

	/** Invalid keys are rejected. */
	public void testInvalidKeys() throws Exception {
		try (S3BlobStore store = create(newConfig())) {
			String valid = UUID.randomUUID().toString();
			for (String invalid : new String[] { null, "", "../" + valid, valid.toUpperCase(), valid + "x" }) {
				try {
					store.getObjectName(invalid);
					fail("Invalid key must be rejected: " + invalid);
				} catch (IllegalArgumentException ex) {
					// Expected.
				}
				try {
					store.get(invalid).close();
					fail("Invalid key must be rejected: " + invalid);
				} catch (IllegalArgumentException ex) {
					// Expected.
				}
				try {
					store.delete(invalid);
					fail("Invalid key must be rejected: " + invalid);
				} catch (IllegalArgumentException ex) {
					// Expected.
				}
			}
		}
	}

	/** The format of the <code>Range</code> header. */
	public void testRangeHeader() {
		assertEquals("bytes=0-9", S3BlobStore.rangeHeader(0, 10));
		assertEquals("bytes=100-149", S3BlobStore.rangeHeader(100, 50));
		assertEquals("bytes=5-5", S3BlobStore.rangeHeader(5, 1));
		assertEquals("bytes=500-", S3BlobStore.rangeHeader(500, Long.MAX_VALUE));
		assertEquals("bytes=" + (Long.MAX_VALUE - 1) + "-" + (Long.MAX_VALUE - 1),
			S3BlobStore.rangeHeader(Long.MAX_VALUE - 1, 1));
	}

	/** Access key and secret key must be given together. */
	public void testIncompleteCredentials() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setAccessKey("admin");
		assertConfigError(config);

		config.setAccessKey("");
		config.setSecretKey("secret");
		assertConfigError(config);
	}

	/** The part size must be at least the minimum part size of S3. */
	public void testPartSizeTooSmall() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setPartSize(S3BlobStore.MIN_PART_SIZE - 1);
		assertConfigError(config);
	}

	/** An endpoint must be an absolute URL. */
	public void testInvalidEndpoint() throws Exception {
		S3BlobStore.Config<?> config = newConfig();
		config.setEndpoint("localhost:9000/x y");
		assertConfigError(config);
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
		config.setBucket("data");
		config.setEndpoint("http://localhost:9000");
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
		return ModuleTestSetup.setupModule(TestS3BlobStoreConfig.class);
	}

}
