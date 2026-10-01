/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.blob;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.FileSystemBlobStore;

/**
 * Test of {@link FileSystemBlobStore}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFileSystemBlobStore extends AbstractBlobStoreContractTest {

	private static final String STORE_NAME = "test";

	private File _root;

	@Override
	protected BlobStore createStore() throws Exception {
		_root = createdCleanTestDir("blob-store");
		return newStore(STORE_NAME, _root.getPath());
	}

	@Override
	protected void disposeStore(BlobStore store) throws Exception {
		FileUtilities.deleteR(_root);
	}

	/**
	 * Creates a {@link FileSystemBlobStore} with the given name and root directory.
	 */
	static FileSystemBlobStore newStore(String name, String root) {
		FileSystemBlobStore.Config<?> config = TypedConfiguration.newConfigItem(FileSystemBlobStore.Config.class);
		config.setName(name);
		config.setRoot(root);
		return (FileSystemBlobStore) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private FileSystemBlobStore fsStore() {
		return (FileSystemBlobStore) store();
	}

	private Path tempDir() {
		return fsStore().getRoot().resolve(FileSystemBlobStore.TEMP_DIR_NAME);
	}

	/** A blob is stored in two levels of shard directories named by the key prefix. */
	public void testShardLayout() throws IOException {
		byte[] content = bytes(100);
		String key = put(content);

		Path expected = fsStore().getRoot()
			.resolve(key.substring(0, 2))
			.resolve(key.substring(2, 4))
			.resolve(key);
		assertEquals(expected, fsStore().getFile(key));
		assertTrue(Files.isRegularFile(expected));
		assertTrue(Arrays.equals(content, Files.readAllBytes(expected)));
		try (Stream<Path> temps = Files.list(tempDir())) {
			assertEquals("No temporary file must remain after a put.", 0L, temps.count());
		}
	}

	/** Temporary and foreign files are not reported as blobs. */
	public void testForeignFilesNotListed() throws IOException {
		String key = put(bytes(10));

		Files.createDirectories(tempDir());
		Files.write(tempDir().resolve("blob-partial.tmp"), bytes(5));
		Files.write(fsStore().getRoot().resolve("README"), bytes(5));
		Path shard = fsStore().getFile(key).getParent();
		Files.write(shard.resolve("not-a-key"), bytes(5));
		Files.createDirectories(fsStore().getRoot().resolve("zz").resolve("00"));

		assertEquals(Collections.singletonList(key), keys(listAll()));
	}

	/** The cleanup removes old temporary files and keeps recent ones. */
	public void testCleanupRemovesOldTempFiles() throws IOException {
		Files.createDirectories(tempDir());
		Path old = tempDir().resolve("blob-old.tmp");
		Path recent = tempDir().resolve("blob-recent.tmp");
		Files.write(old, bytes(5));
		Files.write(recent, bytes(5));
		Instant now = Instant.now();
		Files.setLastModifiedTime(old, FileTime.from(now.minus(Duration.ofHours(2))));

		store().cleanup(now.minus(Duration.ofHours(1)));

		assertFalse("An old temporary file must be removed.", Files.exists(old));
		assertTrue("A recent temporary file must be kept.", Files.exists(recent));
	}

	/** A failing upload leaves neither a blob nor a temporary file. */
	public void testFailedPutLeavesNoTraces() throws IOException {
		InputStream failing = new PatternInputStream(Long.MAX_VALUE) {
			private int _reads;

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				if (++_reads > 3) {
					throw new IOException("Connection lost.");
				}
				return super.read(b, off, len);
			}
		};
		try {
			store().put(failing, -1, CONTENT_TYPE);
			fail("A failing upload must fail the put.");
		} catch (IOException ex) {
			assertEquals("Connection lost.", ex.getMessage());
		}
		assertEquals(Collections.emptyList(), listAll());
		try (Stream<Path> temps = Files.list(tempDir())) {
			assertEquals("A failed put must remove its temporary file.", 0L, temps.count());
		}
	}

	/** Keys that are not canonical UUIDs are rejected, so no path outside the root is accessed. */
	public void testInvalidKey() throws IOException {
		String valid = put(bytes(10));
		for (String invalid : Arrays.asList(null, "", "../../etc/passwd", valid.toUpperCase(),
			valid.substring(0, 2) + "/../" + valid.substring(6), valid + "x", FileSystemBlobStore.TEMP_DIR_NAME)) {
			try {
				store().get(invalid).close();
				fail("Invalid key must be rejected: " + invalid);
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
			try {
				store().get(invalid, 0, 1).close();
				fail("Invalid key must be rejected: " + invalid);
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
			try {
				store().delete(invalid);
				fail("Invalid key must be rejected: " + invalid);
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestFileSystemBlobStore.class);
	}

}
