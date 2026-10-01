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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MinIOContainer;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.basic.io.blob.AbstractBlobStoreContractTest;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MultipartUpload;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.storage.s3.S3BlobStore;
import com.top_logic.storage.s3.ServerSideEncryptionMode;

/**
 * Test of {@link S3BlobStore} against a MinIO server started in a Docker container.
 *
 * <p>
 * The test is skipped if no Docker environment is available. Each test uses a bucket of its own.
 * </p>
 *
 * <p>
 * The MinIO server is started with a static KMS key, so that it accepts the server-side encryption
 * modes SSE-S3 and SSE-KMS. The contract tests run without server-side encryption; the encryption
 * modes are tested separately.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestS3BlobStore extends AbstractBlobStoreContractTest {

	/**
	 * The MinIO image the tests run against.
	 */
	private static final String MINIO_IMAGE = "minio/minio:RELEASE.2025-04-22T22-12-26Z";

	/**
	 * Environment variable of MinIO defining a static KMS key: name and base64 encoded 256 bit key.
	 */
	private static final String MINIO_KMS_SECRET_KEY = "MINIO_KMS_SECRET_KEY";

	/**
	 * Name of the static KMS key of the MinIO server.
	 */
	private static final String KMS_KEY_ID = "test-key";

	private static final String KMS_KEY = "bXktbWluaW8ta2V5LWZvci10ZXN0aW5nLW9ubHktMzI=";

	private static final String STORE_NAME = "s3";

	private static final String PREFIX = "blobs/";

	private static final long MIB = 1024L * 1024;

	private static final AtomicInteger BUCKET_COUNTER = new AtomicInteger();

	private static MinIOContainer _minio;

	private static S3Client _admin;

	private String _bucket;

	@Override
	protected BlobStore createStore() throws Exception {
		_bucket = "test-" + BUCKET_COUNTER.incrementAndGet() + "-" + System.currentTimeMillis();
		_admin.createBucket(request -> request.bucket(_bucket));
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
		for (MultipartUpload upload : _admin.listMultipartUploadsPaginator(request -> request.bucket(bucket))
			.uploads()) {
			_admin.abortMultipartUpload(
				request -> request.bucket(bucket).key(upload.key()).uploadId(upload.uploadId()));
		}
		for (S3Object object : _admin.listObjectsV2Paginator(request -> request.bucket(bucket)).contents()) {
			_admin.deleteObject(request -> request.bucket(bucket).key(object.key()));
		}
		try {
			_admin.deleteBucket(request -> request.bucket(bucket));
		} catch (S3Exception ex) {
			// E.g. uploads not reported by the listing of the server, the container is discarded anyway.
			Logger.warn("Cannot delete test bucket '" + bucket + "'.", ex, TestS3BlobStore.class);
		}
	}

	private S3BlobStore.Config<?> newConfig(String prefix) {
		S3BlobStore.Config<?> config = TypedConfiguration.newConfigItem(S3BlobStore.Config.class);
		config.setName(STORE_NAME);
		config.setEndpoint(_minio.getS3URL());
		config.setBucket(_bucket);
		config.setPrefix(prefix);
		config.setPathStyleAccess(true);
		config.setAccessKey(_minio.getUserName());
		config.setSecretKey(_minio.getPassword());
		config.setServerSideEncryption(ServerSideEncryptionMode.NONE);
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

		HeadObjectResponse head = _admin.headObject(request -> request.bucket(_bucket).key(PREFIX + key));
		assertEquals(Long.valueOf(content.length), head.contentLength());
		assertEquals(CONTENT_TYPE, head.contentType());
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

	private S3BlobStore.Config<?> multipartConfig() {
		S3BlobStore.Config<?> config = newConfig(PREFIX);
		config.setMultipartThreshold(MIB);
		config.setPartSize(S3BlobStore.MIN_PART_SIZE);
		return config;
	}

	private HeadObjectResponse head(String key) {
		return _admin.headObject(request -> request.bucket(_bucket).key(PREFIX + key));
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
		_admin.putObject(request -> request.bucket(_bucket).key(objectName), RequestBody.fromBytes(bytes(5)));
	}

	private void startUpload(String objectName) {
		String uploadId =
			_admin.createMultipartUpload(request -> request.bucket(_bucket).key(objectName)).uploadId();
		_admin.uploadPart(request -> request.bucket(_bucket).key(objectName).uploadId(uploadId).partNumber(1),
			RequestBody.fromBytes(bytes(100)));
	}

	private List<String> objectNames() {
		return _admin.listObjectsV2Paginator(request -> request.bucket(_bucket)).contents().stream()
			.map(S3Object::key)
			.sorted()
			.collect(Collectors.toList());
	}

	private List<String> uploads() {
		return uploads(null);
	}

	private List<String> uploads(String prefix) {
		return _admin.listMultipartUploadsPaginator(request -> request.bucket(_bucket).prefix(prefix)).uploads()
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
	 * Starts the MinIO container for the whole suite.
	 */
	private static class MinIOSetup extends TestSetup {

		MinIOSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();
			_minio = new MinIOContainer(MINIO_IMAGE).withEnv(MINIO_KMS_SECRET_KEY, KMS_KEY_ID + ":" + KMS_KEY);
			_minio.start();
			_admin = S3Client.builder()
				.httpClientBuilder(UrlConnectionHttpClient.builder())
				.endpointOverride(URI.create(_minio.getS3URL()))
				.region(Region.US_EAST_1)
				.forcePathStyle(Boolean.TRUE)
				.credentialsProvider(StaticCredentialsProvider.create(
					AwsBasicCredentials.create(_minio.getUserName(), _minio.getPassword())))
				.build();
		}

		@Override
		protected void tearDown() throws Exception {
			try {
				if (_admin != null) {
					_admin.close();
				}
			} finally {
				_admin = null;
				if (_minio != null) {
					_minio.stop();
				}
				_minio = null;
				super.tearDown();
			}
		}

	}

	/**
	 * The test suite, empty if no Docker environment is available.
	 */
	public static Test suite() {
		if (!DockerClientFactory.instance().isDockerAvailable()) {
			TestSuite skipped = new TestSuite(TestS3BlobStore.class.getName());
			skipped.addTest(SimpleTestFactory.newSuccessfulTest("Skipped: Docker is not available."));
			return skipped;
		}
		return ModuleTestSetup.setupModule(new MinIOSetup(new TestSuite(TestS3BlobStore.class)));
	}

}
