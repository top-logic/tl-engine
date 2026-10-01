/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.azure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.basic.io.blob.AbstractBlobStoreContractTest;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlockListType;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ContentDisposition;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.NoSuchBlobException;
import com.top_logic.storage.azure.AzureBlobStore;

/**
 * Test of {@link AzureBlobStore} against the storage emulator Azurite started in a Docker
 * container.
 *
 * <p>
 * The test is skipped if no Docker environment is available. Each test uses a container of its
 * own.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAzureBlobStore extends AbstractBlobStoreContractTest {

	private static final String STORE_NAME = "azure";

	private static final String PREFIX = "blobs/";

	private static final long KIB = 1024L;

	private static final long BLOCK_SIZE = AzureBlobStore.MIN_BLOCK_SIZE;

	private static final long THRESHOLD = 4 * BLOCK_SIZE;

	private BlobContainerClient _container;

	@Override
	protected BlobStore createStore() throws Exception {
		_container = AzuriteSetup.createContainer();
		return newStore(newConfig(PREFIX));
	}

	@Override
	protected void disposeStore(BlobStore store) throws Exception {
		try {
			store.close();
		} finally {
			_container.deleteIfExists();
		}
	}

	private AzureBlobStore.Config<?> newConfig(String prefix) {
		AzureBlobStore.Config<?> config = TypedConfiguration.newConfigItem(AzureBlobStore.Config.class);
		config.setName(STORE_NAME);
		config.setEndpoint(AzuriteSetup.endpoint());
		config.setAccountName(AzuriteSetup.ACCOUNT_NAME);
		config.setAccountKey(AzuriteSetup.ACCOUNT_KEY);
		config.setContainer(_container.getBlobContainerName());
		config.setPrefix(prefix);
		return config;
	}

	private AzureBlobStore.Config<?> blockConfig() {
		AzureBlobStore.Config<?> config = newConfig(PREFIX);
		config.setSingleUploadThreshold(THRESHOLD);
		config.setBlockSize(BLOCK_SIZE);
		return config;
	}

	private static AzureBlobStore newStore(AzureBlobStore.Config<?> config) {
		return (AzureBlobStore) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/** Blobs are stored under the configured prefix with the given content type. */
	public void testBlobNameWithPrefix() throws IOException {
		byte[] content = bytes(100);
		String key = put(content);

		BlobProperties properties = _container.getBlobClient(PREFIX + key).getProperties();
		assertEquals(content.length, properties.getBlobSize());
		assertEquals(CONTENT_TYPE, properties.getContentType());
		assertEquals(Collections.singletonList(PREFIX + key), blobNames());
	}

	/** Stores with different prefixes share a container without seeing each other's blobs. */
	public void testSharedContainer() throws IOException {
		String key = put(bytes(10));
		try (AzureBlobStore other = newStore(newConfig("other/"))) {
			String otherKey = other.put(new ByteArrayInputStream(bytes(20)), 20, CONTENT_TYPE);

			assertEquals(Collections.singletonList(key), keys(listAll()));
			assertEquals(Collections.singletonList(otherKey), keys(list(other)));

			try {
				other.get(key).close();
				fail("A blob of another store must not be visible.");
			} catch (NoSuchBlobException ex) {
				// Expected.
			}
		}
	}

	/** Blobs under the prefix that are no blobs of the store are not listed. */
	public void testForeignBlobsNotListed() throws IOException {
		String key = put(bytes(10));
		putBlob(PREFIX + "README");
		putBlob(PREFIX + "sub/" + UUID.randomUUID());
		putBlob(UUID.randomUUID().toString());
		putBlob("other/" + UUID.randomUUID());

		assertEquals(Collections.singletonList(key), keys(listAll()));
	}

	/** A store without prefix keeps its blobs at the top level of the container. */
	public void testEmptyPrefix() throws IOException {
		try (AzureBlobStore top = newStore(newConfig(""))) {
			String key = top.put(new ByteArrayInputStream(bytes(10)), 10, CONTENT_TYPE);
			putBlob(PREFIX + UUID.randomUUID());

			assertEquals(Collections.singletonList(key), keys(list(top)));
			assertTrue(blobNames().contains(key));
		}
	}

	/** A store configured with a connection string accesses the account. */
	public void testConnectionString() throws IOException {
		AzureBlobStore.Config<?> config = TypedConfiguration.newConfigItem(AzureBlobStore.Config.class);
		config.setName(STORE_NAME);
		config.setConnectionString(AzuriteSetup.connectionString());
		config.setContainer(_container.getBlobContainerName());
		config.setPrefix(PREFIX);
		try (AzureBlobStore store = newStore(config)) {
			byte[] content = bytes(1000);
			String key = store.put(new ByteArrayInputStream(content), content.length, CONTENT_TYPE);
			try (InputStream in = store.get(key)) {
				assertTrue(Arrays.equals(content, in.readAllBytes()));
			}
			assertEquals(Collections.singletonList(key), keys(list(store)));
		}
	}

	/** Content of known size above the threshold is uploaded in blocks. */
	public void testBlocksKnownSize() throws IOException {
		long size = 5 * BLOCK_SIZE + 3;
		try (AzureBlobStore store = newStore(blockConfig())) {
			String key = store.put(new PatternInputStream(size), size, CONTENT_TYPE);
			assertBlocks(key, 6);
			assertContent(store, key, size);
			assertEquals(CONTENT_TYPE, _container.getBlobClient(PREFIX + key).getProperties().getContentType());
		}
	}

	/** Content of unknown size above the threshold is uploaded in blocks. */
	public void testBlocksUnknownSize() throws IOException {
		long size = 6 * BLOCK_SIZE;
		try (AzureBlobStore store = newStore(blockConfig())) {
			String key = store.put(new PatternInputStream(size), -1, CONTENT_TYPE);
			assertBlocks(key, 6);
			assertContent(store, key, size);
			assertEquals(size, info(store, key).size());
		}
	}

	/** Content of unknown size slightly above the threshold is uploaded in blocks. */
	public void testBlocksUnknownSizeAboveThreshold() throws IOException {
		long size = THRESHOLD + 1;
		try (AzureBlobStore store = newStore(blockConfig())) {
			String key = store.put(new PatternInputStream(size), -1, CONTENT_TYPE);
			assertBlocks(key, 5);
			assertContent(store, key, size);
		}
	}

	/** Content of unknown size up to the threshold is uploaded in a single request. */
	public void testSingleUploadUnknownSize() throws IOException {
		long size = THRESHOLD;
		try (AzureBlobStore store = newStore(blockConfig())) {
			String key = store.put(new PatternInputStream(size), -1, CONTENT_TYPE);
			assertContent(store, key, size);
			assertEquals(size, info(store, key).size());
		}
	}

	/** An upload in blocks of content not matching the announced size leaves no blob. */
	public void testBlocksSizeMismatch() throws IOException {
		try (AzureBlobStore store = newStore(blockConfig())) {
			long size = 7 * BLOCK_SIZE + 5;
			try {
				store.put(new PatternInputStream(size), size + 1, CONTENT_TYPE);
				fail("Content shorter than announced must be rejected.");
			} catch (IOException ex) {
				// Expected.
			}
			try {
				store.put(new PatternInputStream(size), size - 1, CONTENT_TYPE);
				fail("Content longer than announced must be rejected.");
			} catch (IOException ex) {
				// Expected.
			}
			assertEquals(Collections.emptyList(), list(store));
			assertEquals(Collections.emptyList(), blobNames());
		}
	}

	/** An upload in blocks failing while reading the content leaves no blob. */
	public void testBlocksFailingContent() throws IOException {
		long failAt = 5 * BLOCK_SIZE;
		InputStream failing = new PatternInputStream(Long.MAX_VALUE) {
			private long _read;

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				if (_read > failAt) {
					throw new IOException("Connection lost.");
				}
				int result = super.read(b, off, len);
				_read += result;
				return result;
			}
		};
		try (AzureBlobStore store = newStore(blockConfig())) {
			try {
				store.put(failing, -1, CONTENT_TYPE);
				fail("A failing upload must fail the put.");
			} catch (IOException ex) {
				assertEquals("Connection lost.", ex.getMessage());
			}
			assertEquals(Collections.emptyList(), list(store));
			assertEquals(Collections.emptyList(), blobNames());
		}
	}

	/** An existing blob is never overwritten, neither by a single upload nor by blocks. */
	public void testNoOverwrite() throws IOException {
		String fixedKey = UUID.randomUUID().toString();
		try (AzureBlobStore store = new AzureBlobStore(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			blockConfig()) {
			@Override
			protected String newKey() {
				return fixedKey;
			}
		}) {
			byte[] content = bytes(100);
			assertEquals(fixedKey, store.put(new ByteArrayInputStream(content), content.length, CONTENT_TYPE));

			byte[] other = new byte[200];
			try {
				store.put(new ByteArrayInputStream(other), other.length, CONTENT_TYPE);
				fail("An existing blob must not be overwritten.");
			} catch (IOException ex) {
				// Expected.
			}
			long large = 5 * BLOCK_SIZE;
			try {
				store.put(new PatternInputStream(large), large, CONTENT_TYPE);
				fail("An existing blob must not be overwritten by blocks.");
			} catch (IOException ex) {
				// Expected.
			}
			try (InputStream in = store.get(fixedKey)) {
				assertTrue("Content must be unchanged.", Arrays.equals(content, in.readAllBytes()));
			}
		}
	}

	/** A missing container is reported as error, not as missing blob. */
	public void testMissingContainer() throws IOException {
		AzureBlobStore.Config<?> config = newConfig(PREFIX);
		config.setContainer("missing-" + System.currentTimeMillis());
		try (AzureBlobStore store = newStore(config)) {
			try {
				store.get(UUID.randomUUID().toString()).close();
				fail("Reading from a missing container must fail.");
			} catch (NoSuchBlobException ex) {
				fail("A missing container must not be reported as missing blob.");
			} catch (IOException ex) {
				// Expected.
			}
			try {
				store.put(new ByteArrayInputStream(bytes(10)), 10, CONTENT_TYPE);
				fail("Storing in a missing container must fail.");
			} catch (IOException ex) {
				// Expected.
			}
		}
	}

	/**
	 * A URL with shared access signature delivers the content with the response headers taken
	 * from the metadata given to the store, and supports range requests.
	 */
	public void testDirectDownload() throws Exception {
		AzureBlobStore.Config<?> config = newConfig(PREFIX);
		config.setDirectDownload(true);
		config.setDirectDownloadMinSize(0);
		try (AzureBlobStore store = newStore(config)) {
			byte[] content = bytes(1000);
			String key = store.put(new ByteArrayInputStream(content), content.length, CONTENT_TYPE);
			String fileName = "Übersicht März.pdf";
			URI url = store.createDownloadUrl(key, content.length, "application/pdf", fileName);
			assertNotNull(url);

			HttpClient http = HttpClient.newHttpClient();
			HttpResponse<byte[]> response =
				http.send(HttpRequest.newBuilder(url).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
			assertEquals(200, response.statusCode());
			assertTrue(Arrays.equals(content, response.body()));
			assertEquals("application/pdf", response.headers().firstValue("Content-Type").orElse(null));
			assertEquals(ContentDisposition.headerValue(ContentDisposition.INLINE, fileName),
				response.headers().firstValue(ContentDisposition.HEADER).orElse(null));
			assertEquals(AzureBlobStore.DIRECT_DOWNLOAD_CACHE_CONTROL,
				response.headers().firstValue("Cache-Control").orElse(null));

			HttpResponse<byte[]> range = http.send(
				HttpRequest.newBuilder(url).header("Range", "bytes=10-29").GET().build(),
				HttpResponse.BodyHandlers.ofByteArray());
			assertEquals(206, range.statusCode());
			assertTrue(Arrays.equals(Arrays.copyOfRange(content, 10, 30), range.body()));

			URI other = store.createDownloadUrl(UUID.randomUUID().toString(), content.length, null, null);
			HttpResponse<byte[]> missing =
				http.send(HttpRequest.newBuilder(other).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
			assertEquals(404, missing.statusCode());
		}
	}

	/**
	 * Checks the number of committed blocks of the blob with the given key.
	 */
	private void assertBlocks(String key, int expectedBlocks) {
		int blocks = _container.getBlobClient(PREFIX + key).getBlockBlobClient().listBlocks(BlockListType.COMMITTED)
			.getCommittedBlocks().size();
		assertEquals("Number of blocks", expectedBlocks, blocks);
	}

	private static void assertContent(AzureBlobStore store, String key, long size) throws IOException {
		try (InputStream in = store.get(key)) {
			long position = 0;
			byte[] buffer = new byte[(int) (64 * KIB)];
			int count;
			while ((count = in.read(buffer)) >= 0) {
				for (int n = 0; n < count; n++) {
					assertEquals("Content at position " + position, PatternInputStream.byteAt(position), buffer[n]);
					position++;
				}
			}
			assertEquals(size, position);
		}
	}

	private void putBlob(String blobName) {
		_container.getBlobClient(blobName).upload(BinaryData.fromBytes(bytes(5)));
	}

	private List<String> blobNames() {
		return _container.listBlobs().stream()
			.map(BlobItem::getName)
			.sorted()
			.collect(Collectors.toList());
	}

	private static List<BlobInfo> list(BlobStore store) throws IOException {
		return store.list().collect(Collectors.toList());
	}

	private static BlobInfo info(BlobStore store, String key) throws IOException {
		return list(store).stream().filter(info -> info.key().equals(key)).findFirst().orElseThrow();
	}

	/**
	 * The test suite, empty if no Docker environment is available.
	 */
	public static Test suite() {
		return AzuriteSetup.suite(TestAzureBlobStore.class);
	}

}
