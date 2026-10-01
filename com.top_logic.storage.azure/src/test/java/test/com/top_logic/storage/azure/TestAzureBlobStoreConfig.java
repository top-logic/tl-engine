/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure;

import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.storage.azure.AzureBlobStore;

/**
 * Test of the configuration and the name mapping of {@link AzureBlobStore}, without accessing a
 * storage.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAzureBlobStoreConfig extends BasicTestCase {

	private static final String STORE_TAG = "store";

	private static final String ACCOUNT = "myaccount";

	private static final String KEY = Base64.getEncoder().encodeToString("account-key-for-testing".getBytes());

	private static final String ENDPOINT = "http://127.0.0.1:10000/" + ACCOUNT;

	/** All settings are read from XML. */
	public void testParseConfig() throws Exception {
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "'"
				+ " endpoint='" + ENDPOINT + "'"
				+ " account-name='" + ACCOUNT + "'"
				+ " account-key='" + ConfigurationEncryption.encrypt(KEY) + "'"
				+ " container='data'"
				+ " prefix='blobs/'"
				+ " single-upload-threshold='1MB'"
				+ " block-size='4MB'"
				+ "/>");

		assertEquals("azure", config.getName());
		assertEquals(ENDPOINT, config.getEndpoint());
		assertEquals(ACCOUNT, config.getAccountName());
		assertEquals("The account key is decrypted.", KEY, config.getAccountKey());
		assertEquals("data", config.getContainer());
		assertEquals("blobs/", config.getPrefix());
		assertEquals(1024L * 1024, config.getSingleUploadThreshold());
		assertEquals(4L * 1024 * 1024, config.getBlockSize());

		try (AzureBlobStore store = create(config)) {
			assertEquals(ENDPOINT, store.getAccount().getAccountUrl());
			assertEquals(ACCOUNT, store.getAccount().getCredential().getAccountName());
			assertEquals("data", store.getContainer());
			assertEquals("blobs/", store.getPrefix());
		}
	}

	/** The account is given by an encrypted connection string. */
	public void testConnectionString() throws Exception {
		String connectionString = connectionString();
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "' container='data'"
				+ " connection-string='" + ConfigurationEncryption.encrypt(connectionString) + "'/>");
		assertEquals("The connection string is decrypted.", connectionString, config.getConnectionString());

		try (AzureBlobStore store = create(config)) {
			assertEquals(ENDPOINT, store.getAccount().getAccountUrl());
			assertNotNull("The account key is taken from the connection string.", store.getAccount().getCredential());
			assertEquals(ACCOUNT, store.getAccount().getCredential().getAccountName());
		}
	}

	/** A connection string with a shared access signature grants access without account key. */
	public void testConnectionStringWithSas() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setAccountName("");
		config.setAccountKey("");
		config.setEndpoint("");
		config.setConnectionString("BlobEndpoint=" + ENDPOINT + ";SharedAccessSignature=sv=2024-08-04&ss=b&srt=sco"
			+ "&sp=rwdlac&se=2030-01-01T00:00:00Z&sig=abc");
		try (AzureBlobStore store = create(config)) {
			assertEquals(ENDPOINT, store.getAccount().getAccountUrl());
			assertNull(store.getAccount().getCredential());
		}
	}

	/** A plain text key is given with the prefix for unencrypted values. */
	public void testUnencryptedSecret() throws Exception {
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "' container='data'"
				+ " account-name='" + ACCOUNT + "' account-key='unencrypted:" + KEY + "'/>");
		assertEquals(KEY, config.getAccountKey());
	}

	/** Defaults of the optional settings. */
	public void testDefaults() throws Exception {
		AzureBlobStore.Config<?> config = parse(
			"<store name='azure' class='" + AzureBlobStore.class.getName() + "' container='data'"
				+ " account-name='" + ACCOUNT + "' account-key='unencrypted:" + KEY + "'/>");

		assertEquals("", config.getConnectionString());
		assertEquals("", config.getEndpoint());
		assertEquals("", config.getPrefix());
		assertEquals(AzureBlobStore.DEFAULT_SINGLE_UPLOAD_THRESHOLD, config.getSingleUploadThreshold());
		assertEquals(AzureBlobStore.DEFAULT_BLOCK_SIZE, config.getBlockSize());

		try (AzureBlobStore store = create(config)) {
			assertEquals("The endpoint is derived from the account name.", "https://myaccount.blob.core.windows.net",
				store.getAccount().getAccountUrl());
		}
	}

	/** Blob names are the prefix followed by the key. */
	public void testBlobNames() throws Exception {
		String key = UUID.randomUUID().toString();

		AzureBlobStore.Config<?> config = newConfig();
		config.setPrefix("blobs/");
		try (AzureBlobStore store = create(config)) {
			assertEquals("blobs/" + key, store.getBlobName(key));
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
		try (AzureBlobStore store = create(config)) {
			assertEquals(key, store.getBlobName(key));
			assertEquals(key, store.getKey(key));
			assertNull(store.getKey("blobs/" + key));
		}
	}

	/** Invalid keys are rejected. */
	public void testInvalidKeys() throws Exception {
		try (AzureBlobStore store = create(newConfig())) {
			String valid = UUID.randomUUID().toString();
			for (String invalid : new String[] { null, "", "../" + valid, valid.toUpperCase(), valid + "x" }) {
				try {
					store.getBlobName(invalid);
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

	/** Block IDs are distinct base64 strings of equal length. */
	public void testBlockIds() {
		Set<String> ids = new HashSet<>();
		int length = AzureBlobStore.blockId(0).length();
		for (int n : new int[] { 0, 1, 9, 10, 999, AzureBlobStore.MAX_BLOCKS - 1 }) {
			String id = AzureBlobStore.blockId(n);
			assertEquals("All block IDs of a blob must have the same length.", length, id.length());
			assertTrue("Duplicate block ID.", ids.add(id));
			assertEquals(id, Base64.getEncoder().encodeToString(Base64.getDecoder().decode(id)));
		}
	}

	/** Without connection string, account name and account key are required. */
	public void testMissingCredentials() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setAccountKey("");
		assertConfigError(config);

		config = newConfig();
		config.setAccountName("");
		assertConfigError(config);
	}

	/** A connection string and account settings are not given together. */
	public void testConnectionStringAndAccount() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setConnectionString(connectionString());
		assertConfigError(config);
	}

	/** An invalid connection string is reported. */
	public void testInvalidConnectionString() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setAccountName("");
		config.setAccountKey("");
		config.setEndpoint("");
		config.setConnectionString("no connection string");
		assertConfigError(config);
	}

	/** The block size must be at least the minimum block size. */
	public void testBlockSizeTooSmall() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setBlockSize(AzureBlobStore.MIN_BLOCK_SIZE - 1);
		assertConfigError(config);
	}

	/** An endpoint must be an absolute URL. */
	public void testInvalidEndpoint() throws Exception {
		AzureBlobStore.Config<?> config = newConfig();
		config.setEndpoint("localhost:10000/x y");
		assertConfigError(config);
	}

	private static String connectionString() {
		return "DefaultEndpointsProtocol=http;AccountName=" + ACCOUNT + ";AccountKey=" + KEY + ";BlobEndpoint="
			+ ENDPOINT + ";";
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
		config.setContainer("data");
		config.setEndpoint(ENDPOINT);
		config.setAccountName(ACCOUNT);
		config.setAccountKey(KEY);
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
		return ModuleTestSetup.setupModule(TestAzureBlobStoreConfig.class);
	}

}
