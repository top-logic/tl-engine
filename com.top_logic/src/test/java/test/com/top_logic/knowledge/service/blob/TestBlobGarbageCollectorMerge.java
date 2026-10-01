/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.blob;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.TestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.io.blob.BlobInfo;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.knowledge.service.blob.BlobGarbageCollector;
import com.top_logic.knowledge.service.blob.BlobGarbageCollector.Result;

/**
 * Test of the sort-merge of {@link BlobGarbageCollector} with in-memory inputs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBlobGarbageCollectorMerge extends TestCase {

	private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

	private static final Duration GRACE = Duration.ofHours(24);

	private static final Instant OLD = NOW.minus(Duration.ofDays(2));

	private static final Instant YOUNG = NOW.minus(Duration.ofHours(1));

	public void testReferencedKeptUnreferencedOldDeleted() throws IOException {
		TestStore store = new TestStore(old("a"), old("b"), old("c"), old("d"), old("e"));

		Result result = collect(store, "b", "d");

		assertFalse(result.aborted());
		assertEquals(5, result.listed());
		assertEquals(2, result.referenced());
		assertEquals(3, result.deleted());
		assertEquals(0, result.keptByGrace());
		assertEquals(0, result.failures());
		assertEquals(Arrays.asList("a", "c", "e"), store._deleted);
		assertTrue(store._cleanedUp);
		assertEquals(NOW.minus(GRACE), store._cleanupBefore);
	}

	public void testUnreferencedYoungKept() throws IOException {
		TestStore store = new TestStore(old("a"), young("b"), old("c"));

		Result result = collect(store, "c");

		assertEquals(1, result.deleted());
		assertEquals(1, result.keptByGrace());
		assertEquals(1, result.referenced());
		assertEquals(Arrays.asList("a"), store._deleted);
	}

	public void testGraceBoundary() throws IOException {
		TestStore store = new TestStore(new BlobInfo("a", 1, NOW.minus(GRACE)));

		Result result = collect(store);

		assertEquals("A blob exactly at the end of the grace period is kept.", 0, result.deleted());
		assertEquals(1, result.keptByGrace());
	}

	public void testUnknownReferences() throws IOException {
		TestStore store = new TestStore(old("b"), old("d"));

		Result result = collect(store, "a", "c", "e", "f");

		assertEquals(0, result.referenced());
		assertEquals(Arrays.asList("b", "d"), store._deleted);
	}

	public void testDuplicateReferences() throws IOException {
		TestStore store = new TestStore(old("a"), old("b"), old("c"));

		Result result = collect(store, "a", "a", "b", "b", "b");

		assertFalse(result.aborted());
		assertEquals(2, result.referenced());
		assertEquals(Arrays.asList("c"), store._deleted);
	}

	public void testEmptyStore() throws IOException {
		TestStore store = new TestStore();

		Result result = collect(store, "a", "b");

		assertFalse(result.aborted());
		assertEquals(0, result.listed());
		assertEquals(0, result.deleted());
		assertTrue(store._cleanedUp);
	}

	public void testNoReferences() throws IOException {
		TestStore store = new TestStore(old("a"), young("b"));

		Result result = collect(store);

		assertEquals(Arrays.asList("a"), store._deleted);
		assertEquals(1, result.keptByGrace());
	}

	public void testBothEmpty() throws IOException {
		Result result = collect(new TestStore());

		assertFalse(result.aborted());
		assertEquals(0, result.listed());
	}

	public void testReferencesOutOfOrder() throws IOException {
		TestStore store = new TestStore(old("a"), old("b"), old("c"));

		BufferingProtocol log = new BufferingProtocol();
		Result result = collect(store, log, "c", "b");

		assertTrue(result.aborted());
		assertEquals(0, result.deleted());
		assertEquals(Collections.emptyList(), store._deleted);
		assertFalse("No cleanup in an aborted run.", store._cleanedUp);
		assertTrue(log.hasErrors());
	}

	public void testReferencesOutOfOrderAfterLastBlob() throws IOException {
		// The out-of-order reference "b" would protect a candidate that has already been passed.
		TestStore store = new TestStore(old("a"), old("b"));

		BufferingProtocol log = new BufferingProtocol();
		Result result = collect(store, log, "a", "x", "b");

		assertTrue(result.aborted());
		assertEquals(Collections.emptyList(), store._deleted);
		assertTrue(log.hasErrors());
	}

	public void testStoreOutOfOrder() throws IOException {
		TestStore store = new TestStore(old("a"), old("c"), old("b"));

		BufferingProtocol log = new BufferingProtocol();
		Result result = collect(store, log, "a");

		assertTrue(result.aborted());
		assertEquals(Collections.emptyList(), store._deleted);
		assertTrue(log.hasErrors());
	}

	public void testCaseOrder() throws IOException {
		// Upper case letters precede lower case letters in the order of String.compareTo().
		TestStore store = new TestStore(old("B"), old("a"), old("b"));

		Result result = collect(store, "B", "b");

		assertFalse(result.aborted());
		assertEquals(Arrays.asList("a"), store._deleted);

		BufferingProtocol log = new BufferingProtocol();
		TestStore store2 = new TestStore(old("B"), old("a"), old("b"));
		Result result2 = collect(store2, log, "a", "B");
		assertTrue("Case-insensitive order is detected.", result2.aborted());
		assertEquals(Collections.emptyList(), store2._deleted);
	}

	public void testDeleteFailureContinues() throws IOException {
		TestStore store = new TestStore(old("a"), old("b"), old("c"));
		store._failingDeletes.add("b");

		BufferingProtocol log = new BufferingProtocol();
		Result result = collect(store, log);

		assertFalse(result.aborted());
		assertEquals(2, result.deleted());
		assertEquals(1, result.failures());
		assertEquals(Arrays.asList("a", "c"), store._deleted);
		assertTrue(log.hasErrors());
	}

	private static Result collect(TestStore store, String... references) throws IOException {
		BufferingProtocol log = new BufferingProtocol();
		Result result = collect(store, log, references);
		assertFalse(log.getErrors().toString(), log.hasErrors());
		return result;
	}

	private static Result collect(TestStore store, BufferingProtocol log, String... references) throws IOException {
		return BlobGarbageCollector.collect(store, Arrays.asList(references).iterator(), GRACE, NOW, log);
	}

	private static BlobInfo old(String key) {
		return new BlobInfo(key, 1, OLD);
	}

	private static BlobInfo young(String key) {
		return new BlobInfo(key, 1, YOUNG);
	}

	/**
	 * {@link BlobStore} listing a fixed sequence of blobs and recording deletions.
	 */
	private static final class TestStore implements BlobStore {

		private final List<BlobInfo> _blobs;

		final List<String> _deleted = new ArrayList<>();

		final Set<String> _failingDeletes = new HashSet<>();

		boolean _cleanedUp;

		Instant _cleanupBefore;

		TestStore(BlobInfo... blobs) {
			_blobs = Arrays.asList(blobs);
		}

		@Override
		public String getName() {
			return "test";
		}

		@Override
		public String put(InputStream content, long size, String contentType) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream get(String key) {
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream get(String key, long offset, long length) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(String key) throws IOException {
			if (_failingDeletes.contains(key)) {
				throw new IOException("Deletion of '" + key + "' fails.");
			}
			_deleted.add(key);
		}

		@Override
		public Stream<BlobInfo> list() {
			return _blobs.stream();
		}

		@Override
		public void cleanup(Instant olderThan) {
			_cleanedUp = true;
			_cleanupBefore = olderThan;
		}
	}

}
