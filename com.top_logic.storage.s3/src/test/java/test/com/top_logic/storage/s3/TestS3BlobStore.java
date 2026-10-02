/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.storage.s3;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import junit.framework.Test;

import test.com.top_logic.basic.io.blob.AbstractBlobStoreContractTest;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MultipartUpload;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ContentDisposition;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.storage.s3.S3BlobStore;
import com.top_logic.storage.s3.ServerSideEncryptionMode;

/**
 * Test of {@link S3BlobStore} against a SeaweedFS server started in a Docker container.
 *
 * <p>
 * The test is skipped if no Docker environment is available. Each test uses a bucket of its own.
 * </p>
 *
 * <p>
 * The server accepts the server-side encryption modes SSE-S3 and SSE-KMS, see
 * {@link SeaweedFSSetup}. The contract tests run with the default encryption mode SSE-S3; the
 * other modes are tested separately.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestS3BlobStore extends AbstractBlobStoreContractTest {

	/**
	 * Name of the KMS key for SSE-KMS, created by the server on first use.
	 */
	private static final String KMS_KEY_ID = "test-key";

	private static final String STORE_NAME = "s3";

	private static final String PREFIX = "blobs/";

	private static final long MIB = 1024L * 1024;

	private static final AtomicInteger BUCKET_COUNTER = new AtomicInteger();

	private String _bucket;

	private static S3Client admin() {
		return SeaweedFSSetup.admin();
	}

	@Override
	protected BlobStore createStore() throws Exception {
		_bucket = "test-" + BUCKET_COUNTER.incrementAndGet() + "-" + System.currentTimeMillis();
		admin().createBucket(request -> request.bucket(_bucket));
		return newStore(newConfig(PREFIX));
	}

	@Override
	protected void disposeStore(BlobStore store) throws Exception {
		try {
			store.close();
		} finally {
			deleteBucket(_bucket);
		}
	}

	private static void deleteBucket(String bucket) {
		for (MultipartUpload upload : admin().listMultipartUploadsPaginator(request -> request.bucket(bucket))
			.uploads()) {
			admin().abortMultipartUpload(
				request -> request.bucket(bucket).key(upload.key()).uploadId(upload.uploadId()));
		}
		for (S3Object object : admin().listObjectsV2Paginator(request -> request.bucket(bucket)).contents()) {
			admin().deleteObject(request -> request.bucket(bucket).key(object.key()));
		}
		try {
			admin().deleteBucket(request -> request.bucket(bucket));
		} catch (S3Exception ex) {
			// E.g. uploads not reported by the listing of the server, the container is discarded anyway.
			Logger.warn("Cannot delete test bucket '" + bucket + "'.", ex, TestS3BlobStore.class);
		}
	}

	private S3BlobStore.Config<?> newConfig(String prefix) {
		S3BlobStore.Config<?> config = TypedConfiguration.newConfigItem(S3BlobStore.Config.class);
		config.setName(STORE_NAME);
		config.setEndpoint(SeaweedFSSetup.endpoint());
		config.setBucket(_bucket);
		config.setPrefix(prefix);
		config.setPathStyleAccess(true);
		config.setAccessKey(SeaweedFSSetup.ACCESS_KEY);
		config.setSecretKey(SeaweedFSSetup.SECRET_KEY);
		return config;
	}

	private static S3BlobStore newStore(S3BlobStore.Config<?> config) {
		return (S3BlobStore) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private S3BlobStore s3Store() {
		return (S3BlobStore) store();
	}

	/** Blobs are stored under the configured prefix. */
	public void testObjectNameWithPrefix() throws IOException {
		byte[] content = bytes(100);
		String key = put(content);

		HeadObjectResponse head = admin().headObject(request -> request.bucket(_bucket).key(PREFIX + key));
		assertEquals(Long.valueOf(content.length), head.contentLength());
		assertEquals(CONTENT_TYPE, head.contentType());
		assertEquals("Encrypted with the default mode SSE-S3.", ServerSideEncryption.AES256,
			head.serverSideEncryption());
		assertEquals(Collections.singletonList(PREFIX + key), objectNames());
	}

	/** Stores with different prefixes share a bucket without seeing each other's blobs. */
	public void testSharedBucket() throws IOException {
		String key = put(bytes(10));
		try (S3BlobStore other = newStore(newConfig("other/"))) {
			String otherKey = other.put(new ByteArrayInputStream(bytes(20)), 20, CONTENT_TYPE);

			assertEquals(Collections.singletonList(key), keys(listAll()));
			assertEquals(Collections.singletonList(otherKey), keys(list(other)));

			try {
				other.get(key).close();
				fail("A blob of another store must not be visible.");
			} catch (IOException ex) {
				// Expected.
			}
		}
	}

	/** Objects under the prefix that are no blobs of the store are not listed. */
	public void testForeignObjectsNotListed() throws IOException {
		String key = put(bytes(10));
		putObject(PREFIX + "README");
		putObject(PREFIX + "sub/" + UUID.randomUUID());
		putObject(UUID.randomUUID().toString());
		putObject("other/" + UUID.randomUUID());

		assertEquals(Collections.singletonList(key), keys(listAll()));
	}

	/** A store without prefix keeps its blobs at the top level of the bucket. */
	public void testEmptyPrefix() throws IOException {
		try (S3BlobStore top = newStore(newConfig(""))) {
			String key = top.put(new ByteArrayInputStream(bytes(10)), 10, CONTENT_TYPE);
			putObject(PREFIX + UUID.randomUUID());

			assertEquals(Collections.singletonList(key), keys(list(top)));
			assertTrue(objectNames().contains(key));
		}
	}

	/** Content of known size above the threshold is uploaded in parts. */
	public void testMultipartKnownSize() throws IOException {
		long size = 11 * MIB + 3;
		try (S3BlobStore store = newStore(multipartConfig())) {
			String key = store.put(new PatternInputStream(size), size, CONTENT_TYPE);
			assertParts(key, 3);
			assertContent(store, key, size);
		}
	}

	/** Content of unknown size above the threshold is uploaded in parts. */
	public void testMultipartUnknownSize() throws IOException {
		long size = 10 * MIB;
		try (S3BlobStore store = newStore(multipartConfig())) {
			String key = store.put(new PatternInputStream(size), -1, CONTENT_TYPE);
			assertParts(key, 2);
			assertContent(store, key, size);
			assertEquals(size, info(store, key).size());
		}
	}

	/** Content of unknown size below the threshold is uploaded in a single request. */
	public void testSinglePartUnknownSize() throws IOException {
		long size = MIB - 1;
		try (S3BlobStore store = newStore(multipartConfig())) {
			String key = store.put(new PatternInputStream(size), -1, CONTENT_TYPE);
			assertParts(key, 1);
			assertContent(store, key, size);
		}
	}

	/** A multipart upload of content not matching the announced size is aborted. */
	public void testMultipartSizeMismatch() throws IOException {
		try (S3BlobStore store = newStore(multipartConfig())) {
			long size = 7 * MIB;
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
			assertEquals("Failed uploads must be aborted.", Collections.emptyList(), uploads());
		}
	}

	/** A multipart upload failing while reading the content is aborted. */
	public void testMultipartFailingContent() throws IOException {
		InputStream failing = new PatternInputStream(Long.MAX_VALUE) {
			private long _read;

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				if (_read > 6 * MIB) {
					throw new IOException("Connection lost.");
				}
				int result = super.read(b, off, len);
				_read += result;
				return result;
			}
		};
		try (S3BlobStore store = newStore(multipartConfig())) {
			try {
				store.put(failing, -1, CONTENT_TYPE);
				fail("A failing upload must fail the put.");
			} catch (IOException ex) {
				assertEquals("Connection lost.", ex.getMessage());
			}
			assertEquals(Collections.emptyList(), list(store));
			assertEquals("Failed uploads must be aborted.", Collections.emptyList(), uploads());
		}
	}

	/**
	 * The cleanup aborts stale multipart uploads of the store and keeps others.
	 *
	 * <p>
	 * Some servers (e.g. MinIO) list multipart uploads only for an exact object name, not for a
	 * prefix, so the cleanup cannot find stale uploads there; such servers remove stale uploads
	 * themselves. On these servers, the test only checks that no upload is aborted by mistake.
	 * </p>
	 */
	public void testCleanupAbortsStaleUpload() throws IOException {
		String staleName = PREFIX + UUID.randomUUID();
		String foreignName = "other/" + UUID.randomUUID();
		startUpload(staleName);
		startUpload(foreignName);

		Instant now = Instant.now();
		s3Store().cleanup(now.minus(Duration.ofHours(1)));
		assertEquals("Recent uploads must be kept.", Collections.singletonList(staleName), uploads(staleName));
		assertEquals(Collections.singletonList(foreignName), uploads(foreignName));

		boolean prefixListing = uploads(PREFIX).contains(staleName);

		s3Store().cleanup(now.plus(Duration.ofMinutes(1)));
		assertEquals("Uploads of other prefixes must be kept.", Collections.singletonList(foreignName),
			uploads(foreignName));
		if (prefixListing) {
			assertEquals("Stale uploads must be aborted.", Collections.emptyList(), uploads(staleName));
		} else {
			Logger.info("Server lists no multipart uploads by prefix, abort of stale uploads not checked.",
				TestS3BlobStore.class);
		}
	}

	/** Content is stored without server-side encryption. */
	public void testServerSideEncryptionNone() throws IOException {
		S3BlobStore.Config<?> config = multipartConfig();
		config.setServerSideEncryption(ServerSideEncryptionMode.NONE);
		assertEncryption(config, null);
	}

	/** Content is encrypted with SSE-S3. */
	public void testServerSideEncryptionS3() throws IOException {
		S3BlobStore.Config<?> config = multipartConfig();
		config.setServerSideEncryption(ServerSideEncryptionMode.SSE_S3);
		assertEncryption(config, ServerSideEncryption.AES256);
	}

	/** Content is encrypted with SSE-KMS and the configured key. */
	public void testServerSideEncryptionKms() throws IOException {
		S3BlobStore.Config<?> config = multipartConfig();
		config.setServerSideEncryption(ServerSideEncryptionMode.SSE_KMS);
		config.setKmsKeyId(KMS_KEY_ID);
		assertEncryption(config, ServerSideEncryption.AWS_KMS);
	}

	private void assertEncryption(S3BlobStore.Config<?> config, ServerSideEncryption expected) throws IOException {
		try (S3BlobStore store = newStore(config)) {
			byte[] small = bytes(1000);
			String smallKey = store.put(new ByteArrayInputStream(small), small.length, CONTENT_TYPE);
			try (InputStream in = store.get(smallKey)) {
				assertTrue(Arrays.equals(small, in.readAllBytes()));
			}
			try (InputStream in = store.get(smallKey, 10, 20)) {
				assertTrue(Arrays.equals(Arrays.copyOfRange(small, 10, 30), in.readAllBytes()));
			}
			assertEquals(expected, head(smallKey).serverSideEncryption());

			long size = 6 * MIB;
			String largeKey = store.put(new PatternInputStream(size), size, CONTENT_TYPE);
			assertContent(store, largeKey, size);
			assertEquals(expected, head(largeKey).serverSideEncryption());
		}
	}

	/**
	 * A presigned URL delivers the content with the response headers taken from the metadata given
	 * to the store, and supports range requests.
	 */
	public void testDirectDownload() throws Exception {
		S3BlobStore.Config<?> config = newConfig(PREFIX);
		config.setDirectDownload(true);
		config.setDirectDownloadMinSize(0);
		try (S3BlobStore store = newStore(config)) {
			byte[] content = bytes(1000);
			String key = store.put(new ByteArrayInputStream(content), content.length, CONTENT_TYPE);
			String fileName = "\u00DCbersicht M\u00E4rz.pdf";
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
			assertEquals(S3BlobStore.DIRECT_DOWNLOAD_CACHE_CONTROL,
				response.headers().firstValue("Cache-Control").orElse(null));

			HttpResponse<byte[]> range = http.send(
				HttpRequest.newBuilder(url).header("Range", S3BlobStore.rangeHeader(10, 20)).GET().build(),
				HttpResponse.BodyHandlers.ofByteArray());
			assertEquals(206, range.statusCode());
			assertTrue(Arrays.equals(Arrays.copyOfRange(content, 10, 30), range.body()));
		}
	}

	private S3BlobStore.Config<?> multipartConfig() {
		S3BlobStore.Config<?> config = newConfig(PREFIX);
		config.setMultipartThreshold(MIB);
		config.setPartSize(S3BlobStore.MIN_PART_SIZE);
		return config;
	}

	private HeadObjectResponse head(String key) {
		return admin().headObject(request -> request.bucket(_bucket).key(PREFIX + key));
	}

	/**
	 * Checks the number of parts the object of the given key was uploaded in, as reported in its
	 * ETag (<code>&lt;hash&gt;-&lt;parts&gt;</code> for multipart uploads).
	 */
	private void assertParts(String key, int expectedParts) {
		String etag = head(key).eTag().replace("\"", "");
		int separator = etag.indexOf('-');
		int parts = separator < 0 ? 1 : Integer.parseInt(etag.substring(separator + 1));
		assertEquals("Number of parts (ETag " + etag + ")", expectedParts, parts);
	}

	private static void assertContent(S3BlobStore store, String key, long size) throws IOException {
		try (InputStream in = store.get(key)) {
			long position = 0;
			byte[] buffer = new byte[64 * 1024];
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

	private void putObject(String objectName) {
		admin().putObject(request -> request.bucket(_bucket).key(objectName), RequestBody.fromBytes(bytes(5)));
	}

	private void startUpload(String objectName) {
		String uploadId =
			admin().createMultipartUpload(request -> request.bucket(_bucket).key(objectName)).uploadId();
		admin().uploadPart(request -> request.bucket(_bucket).key(objectName).uploadId(uploadId).partNumber(1),
			RequestBody.fromBytes(bytes(100)));
	}

	private List<String> objectNames() {
		return admin().listObjectsV2Paginator(request -> request.bucket(_bucket)).contents().stream()
			.map(S3Object::key)
			.sorted()
			.collect(Collectors.toList());
	}

	private List<String> uploads() {
		return uploads(null);
	}

	private List<String> uploads(String prefix) {
		return admin().listMultipartUploadsPaginator(request -> request.bucket(_bucket).prefix(prefix)).uploads()
			.stream()
			.map(MultipartUpload::key)
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
	 * The test suite, a skip placeholder if no Docker environment is available.
	 */
	public static Test suite() {
		return SeaweedFSSetup.suite(TestS3BlobStore.class);
	}

}
