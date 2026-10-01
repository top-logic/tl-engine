/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.blob;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.NoSuchBlobException;

/**
 * Contract test for {@link BlobStore} implementations.
 *
 * <p>
 * A test for a concrete implementation subclasses this test and creates the store under test in
 * {@link #createStore()}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBlobStoreContractTest extends BasicTestCase {

	/** Content type passed to {@link BlobStore#put(InputStream, long, String)}. */
	protected static final String CONTENT_TYPE = "application/octet-stream";

	/** Size of the content in {@link #testLargeContent()}. */
	private static final long LARGE_SIZE = 8L * 1024 * 1024 + 17;

	/** Tolerance for comparing modification times with the local clock. */
	private static final Duration CLOCK_TOLERANCE = Duration.ofSeconds(5);

	private BlobStore _store;

	/**
	 * Creates the store under test.
	 *
	 * <p>
	 * Each call must deliver a store that contains no blobs.
	 * </p>
	 */
	protected abstract BlobStore createStore() throws Exception;

	/**
	 * Releases the store created by {@link #createStore()} and all its content.
	 *
	 * @param store
	 *        The store to release.
	 */
	protected void disposeStore(BlobStore store) throws Exception {
		// Nothing to release by default.
	}

	/**
	 * The store under test.
	 */
	protected BlobStore store() {
		return _store;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = createStore();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (_store != null) {
				disposeStore(_store);
			}
		} finally {
			_store = null;
			super.tearDown();
		}
	}

	/** Stored content is read back unchanged. */
	public void testRoundtrip() throws IOException {
		byte[] content = "Hello blob store!".getBytes(StandardCharsets.UTF_8);
		String key = put(content);
		assertNotNull(key);
		assertTrue(Arrays.equals(content, read(key)));
		assertTrue("Content must be readable more than once.", Arrays.equals(content, read(key)));
	}

	/** Empty content can be stored. */
	public void testEmptyContent() throws IOException {
		String key = put(new byte[0]);
		assertEquals(0, read(key).length);
		assertEquals(0, read(key, 0, 10).length);
		assertEquals(0L, info(key).size());
	}

	/** Content of unknown size can be stored. */
	public void testUnknownSize() throws IOException {
		byte[] content = bytes(10000);
		String key = store().put(new ByteArrayInputStream(content), -1, CONTENT_TYPE);
		assertTrue(Arrays.equals(content, read(key)));
		assertEquals(content.length, info(key).size());
	}

	/** Storing fails if the content does not have the announced size, and leaves no blob. */
	public void testSizeMismatch() throws IOException {
		byte[] content = bytes(100);
		try {
			store().put(new ByteArrayInputStream(content), content.length + 1, CONTENT_TYPE);
			fail("Content shorter than announced must be rejected.");
		} catch (IOException ex) {
			// Expected.
		}
		try {
			store().put(new ByteArrayInputStream(content), content.length - 1, CONTENT_TYPE);
			fail("Content longer than announced must be rejected.");
		} catch (IOException ex) {
			// Expected.
		}
		assertEquals("A failed put must not leave a blob.", 0, listAll().size());
	}

	/** Large content is stored and read as stream. */
	public void testLargeContent() throws IOException {
		String key = store().put(new PatternInputStream(LARGE_SIZE), LARGE_SIZE, CONTENT_TYPE);
		try (InputStream in = store().get(key)) {
			assertPattern(in, 0, LARGE_SIZE);
		}
		assertEquals(LARGE_SIZE, info(key).size());

		long offset = LARGE_SIZE / 2 + 3;
		long length = 1024 * 1024 + 5;
		try (InputStream in = store().get(key, offset, length)) {
			assertPattern(in, offset, length);
		}
	}

	/** Large content of unknown size is stored. */
	public void testLargeContentUnknownSize() throws IOException {
		String key = store().put(new PatternInputStream(LARGE_SIZE), -1, CONTENT_TYPE);
		try (InputStream in = store().get(key)) {
			assertPattern(in, 0, LARGE_SIZE);
		}
		assertEquals(LARGE_SIZE, info(key).size());
	}

	/** Ranges of the content are read. */
	public void testRangeReads() throws IOException {
		byte[] content = bytes(1000);
		String key = put(content);

		assertRange(content, key, 0, 10);
		assertRange(content, key, 100, 50);
		assertRange(content, key, 990, 10);
		assertRange(content, key, 0, 1000);
		assertRange(content, key, 0, 0);

		assertTrue("A range beyond the end delivers the bytes up to the end.",
			Arrays.equals(Arrays.copyOfRange(content, 995, 1000), read(key, 995, 100)));
		assertTrue(Arrays.equals(Arrays.copyOfRange(content, 500, 1000), read(key, 500, Long.MAX_VALUE)));
		assertEquals("An offset at the end delivers no bytes.", 0, read(key, 1000, 10).length);
		assertEquals("An offset beyond the end delivers no bytes.", 0, read(key, 5000, 10).length);
	}

	/** Negative range arguments are rejected. */
	public void testInvalidRange() throws IOException {
		String key = put(bytes(10));
		try {
			store().get(key, -1, 5).close();
			fail("Negative offset must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
		try {
			store().get(key, 0, -1).close();
			fail("Negative length must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	/** Reading a missing key fails with a {@link NoSuchBlobException}. */
	public void testGetMissing() throws IOException {
		String missing = UUID.randomUUID().toString();
		try {
			store().get(missing).close();
			fail("Reading a missing key must fail.");
		} catch (NoSuchBlobException ex) {
			assertEquals(missing, ex.getKey());
			assertEquals(store().getName(), ex.getStoreName());
		}
		try {
			store().get(missing, 0, 10).close();
			fail("Reading a range of a missing key must fail.");
		} catch (NoSuchBlobException ex) {
			assertEquals(missing, ex.getKey());
		}
	}

	/** A deleted blob is gone, other blobs are not affected. */
	public void testDelete() throws IOException {
		byte[] content = bytes(100);
		String key = put(content);
		String other = put(content);

		store().delete(key);
		try {
			store().get(key).close();
			fail("A deleted blob must not be readable.");
		} catch (NoSuchBlobException ex) {
			// Expected.
		}
		assertEquals(Collections.singletonList(other), keys(listAll()));
		assertTrue(Arrays.equals(content, read(other)));
	}

	/** Deleting a missing key is not an error. */
	public void testDeleteMissing() throws IOException {
		store().delete(UUID.randomUUID().toString());

		String key = put(bytes(10));
		store().delete(key);
		store().delete(key);
	}

	/** Each put creates a new key, also for equal content. */
	public void testKeysUnique() throws IOException {
		byte[] content = bytes(10);
		Set<String> keys = new HashSet<>();
		for (int n = 0; n < 100; n++) {
			assertTrue("Duplicate key.", keys.add(put(content)));
		}
		assertEquals(keys, new HashSet<>(keys(listAll())));
	}

	/** The listing reports all blobs in key order with their sizes and modification times. */
	public void testList() throws IOException {
		Instant before = Instant.now().minus(CLOCK_TOLERANCE);
		List<String> expectedKeys = new ArrayList<>();
		List<Integer> sizes = new ArrayList<>();
		for (int n = 0; n < 50; n++) {
			int size = n * 7;
			String key = put(bytes(size));
			expectedKeys.add(key);
			sizes.add(size);
		}
		Instant after = Instant.now().plus(CLOCK_TOLERANCE);

		List<BlobInfo> infos = listAll();
		List<String> keys = keys(infos);
		List<String> sortedKeys = new ArrayList<>(expectedKeys);
		Collections.sort(sortedKeys);
		assertEquals("The listing must deliver all keys in lexicographic order.", sortedKeys, keys);

		for (BlobInfo info : infos) {
			int size = sizes.get(expectedKeys.indexOf(info.key()));
			assertEquals("Size of " + info.key(), size, info.size());
			assertNotNull(info.lastModified());
			assertTrue("Implausible modification time: " + info.lastModified(),
				!info.lastModified().isBefore(before) && !info.lastModified().isAfter(after));
		}
	}

	/** The listing of an empty store is empty. */
	public void testListEmpty() throws IOException {
		assertEquals(Collections.emptyList(), listAll());
	}

	/** Several threads store content concurrently. */
	public void testConcurrentPuts() throws Exception {
		int threads = 8;
		int putsPerThread = 20;
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		try {
			List<Future<List<String>>> results = new ArrayList<>();
			for (int t = 0; t < threads; t++) {
				int thread = t;
				Callable<List<String>> task = () -> {
					List<String> keys = new ArrayList<>();
					for (int n = 0; n < putsPerThread; n++) {
						byte[] content = content(thread, n);
						String key = put(content);
						assertTrue(Arrays.equals(content, read(key)));
						keys.add(key);
					}
					return keys;
				};
				results.add(executor.submit(task));
			}

			Set<String> allKeys = new HashSet<>();
			for (int t = 0; t < threads; t++) {
				List<String> keys = results.get(t).get(5, TimeUnit.MINUTES);
				for (int n = 0; n < putsPerThread; n++) {
					String key = keys.get(n);
					assertTrue("Duplicate key.", allKeys.add(key));
					assertTrue(Arrays.equals(content(t, n), read(key)));
				}
			}
			assertEquals(allKeys, new HashSet<>(keys(listAll())));
		} finally {
			executor.shutdownNow();
		}
	}

	/** The cleanup does not affect stored blobs. */
	public void testCleanup() throws IOException {
		store().cleanup(Instant.now());

		byte[] content = bytes(100);
		String key = put(content);
		store().cleanup(Instant.now().plus(Duration.ofDays(1)));
		assertTrue(Arrays.equals(content, read(key)));
		assertEquals(Collections.singletonList(key), keys(listAll()));
	}

	/**
	 * Stores the given content with its size.
	 */
	protected String put(byte[] content) throws IOException {
		return store().put(new ByteArrayInputStream(content), content.length, CONTENT_TYPE);
	}

	/**
	 * Reads the complete content of the given key.
	 */
	protected byte[] read(String key) throws IOException {
		try (InputStream in = store().get(key)) {
			return in.readAllBytes();
		}
	}

	/**
	 * Reads a range of the content of the given key.
	 */
	protected byte[] read(String key, long offset, long length) throws IOException {
		try (InputStream in = store().get(key, offset, length)) {
			return in.readAllBytes();
		}
	}

	/**
	 * The complete listing of the store.
	 */
	protected List<BlobInfo> listAll() throws IOException {
		try (Stream<BlobInfo> infos = store().list()) {
			return infos.collect(Collectors.toList());
		}
	}

	/**
	 * The listed {@link BlobInfo} of the given key.
	 */
	protected BlobInfo info(String key) throws IOException {
		for (BlobInfo info : listAll()) {
			if (info.key().equals(key)) {
				return info;
			}
		}
		fail("Key not listed: " + key);
		return null;
	}

	/**
	 * The keys of the given listing.
	 */
	protected static List<String> keys(List<BlobInfo> infos) {
		return infos.stream().map(BlobInfo::key).collect(Collectors.toList());
	}

	/**
	 * Deterministic content of the given size.
	 */
	protected static byte[] bytes(int size) {
		byte[] result = new byte[size];
		for (int n = 0; n < size; n++) {
			result[n] = PatternInputStream.byteAt(n);
		}
		return result;
	}

	private static byte[] content(int thread, int n) {
		return ("thread " + thread + ", blob " + n).getBytes(StandardCharsets.UTF_8);
	}

	private void assertRange(byte[] content, String key, int offset, int length) throws IOException {
		assertTrue("Range " + offset + "+" + length,
			Arrays.equals(Arrays.copyOfRange(content, offset, offset + length), read(key, offset, length)));
	}

	private static void assertPattern(InputStream in, long offset, long length) throws IOException {
		byte[] buffer = new byte[64 * 1024];
		long position = offset;
		long end = offset + length;
		while (true) {
			int count = in.read(buffer);
			if (count < 0) {
				break;
			}
			for (int n = 0; n < count; n++) {
				assertEquals("Content at position " + position, PatternInputStream.byteAt(position), buffer[n]);
				position++;
			}
		}
		assertEquals("Content length.", end, position);
	}

	/**
	 * {@link InputStream} generating deterministic content of a given size without holding it in
	 * memory.
	 */
	protected static class PatternInputStream extends InputStream {

		private final long _size;

		private long _position;

		/**
		 * Creates a {@link PatternInputStream}.
		 *
		 * @param size
		 *        The number of bytes to deliver.
		 */
		public PatternInputStream(long size) {
			_size = size;
		}

		/**
		 * The byte delivered at the given position.
		 */
		public static byte byteAt(long position) {
			return (byte) (position * 31 + (position >>> 9));
		}

		@Override
		public int read() throws IOException {
			if (_position >= _size) {
				return -1;
			}
			return byteAt(_position++) & 0xFF;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (len == 0) {
				return 0;
			}
			if (_position >= _size) {
				return -1;
			}
			int count = (int) Math.min(len, _size - _position);
			for (int n = 0; n < count; n++) {
				b[off + n] = byteAt(_position++);
			}
			return count;
		}

	}

}
