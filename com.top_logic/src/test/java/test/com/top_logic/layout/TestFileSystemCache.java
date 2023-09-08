/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.collections4.CollectionUtils;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * Tests the {@link FileSystemCache}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@DeactivatedTest("This test randomly fails on the build server.")
public class TestFileSystemCache extends TestCase {

	/**
	 * Milliseconds a thread should timeout before polling for filesystem events.
	 */
	private static final int TIMEOUT_BEFORE_POLLING = 10;

	/**
	 * Maximal milliseconds a thread should wait for filesystem events.
	 */
	private static final int MAX_TIMEOUT_FOR_EVENTS = 5000;

	private static final String TEST_FILENAME_1 = "testFile1.xml";

	private static final String TEST_FILENAME_2 = "testFile2.xml";

	private static final String TEST_XML_CONTENT = "<test/>";

	private PathUpdate _update;

	private Iterator<PathUpdate> _updates;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Clean up garbage that might have been left from previous test run.
		cleanupFile(TEST_FILENAME_1);
		cleanupFile(TEST_FILENAME_2);
		FileSystemCache.getCache().fetchUpdates();

		_updates = FileSystemCache.getCache().getUpdates();
	}

	@Override
	protected void tearDown() throws Exception {
		_updates = null;

		super.tearDown();
	}

	/**
	 * Test file creation, change and deletion.
	 */
	public void testBasicFileOperations() throws InterruptedException, IOException {
		File file = toFile(TEST_FILENAME_1);

		try {
			testFileCreation(file);
			testFileChange(file);
			testFileDeletion(file);
		} finally {
			if (file.exists()) {
				delete(file);

				// Consume update.
				getUpdate();
			}
		}
	}

	/**
	 * Tests the creation and deletion of the same file.
	 */
	public void testCreationAndDeletionOnSameFile() throws IOException, InterruptedException {
		File file = toFile(TEST_FILENAME_1);

		try {
			file.createNewFile();
			file.delete();
			file.createNewFile();

			PathUpdate createUpdate = getUpdate();
			assertEquals(0, createUpdate.getDeletions().size());
			assertEquals(1, createUpdate.getCreations().size());
			assertTrue(createUpdate.getChanges().size() <= 1);
			Path creations = CollectionUtils.extractSingleton(createUpdate.getCreations());
			assertEquals(TEST_FILENAME_1, creations.getFileName().toString());

			file.delete();
			file.createNewFile();
			file.delete();

			PathUpdate deleteUpdate = getUpdate();
			assertEquals(0, deleteUpdate.getCreations().size());
			assertEquals(1, deleteUpdate.getDeletions().size());
			assertTrue(createUpdate.getChanges().size() <= 1);
			Path deletions = CollectionUtils.extractSingleton(deleteUpdate.getDeletions());
			assertEquals(TEST_FILENAME_1, deletions.getFileName().toString());
		} finally {
			if (file.exists()) {
				delete(file);

				// Consume update.
				getUpdate();
			}
		}
	}

	/**
	 * Tests the creation of two files.
	 */
	public void testMultipleCreations() throws IOException, InterruptedException {
		File file1 = toFile(TEST_FILENAME_1);
		File file2 = toFile(TEST_FILENAME_2);

		try {
			file1.createNewFile();
			file2.createNewFile();

			int creations = 0;
			while (true) {
				PathUpdate createUpdate = getUpdate();
				assertEquals(0, createUpdate.getChanges().size());
				assertEquals(0, createUpdate.getDeletions().size());

				creations += createUpdate.getCreations().size();
				assertTrue(creations <= 2);

				if (creations == 2) {
					break;
				}
			}
		} finally {
			boolean cleanupRequired = false;
			if (file1.exists()) {
				delete(file1);
				cleanupRequired = true;
			}
			if (file2.exists()) {
				delete(file2);
				cleanupRequired = true;
			}

			if (cleanupRequired) {
				getUpdate();
			}
		}
	}

	private void delete(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}

	private File toFile(String child) {
		return new File(FileManager.getInstance().getIDEFile(LayoutConstants.LAYOUT_BASE_RESOURCE), child);
	}

	private void cleanupFile(String child) {
		new File(FileManager.getInstance().getIDEFile(LayoutConstants.LAYOUT_BASE_RESOURCE), child).delete();
	}

	private void testFileDeletion(File file) throws InterruptedException, IOException {
		boolean isDeleted = file.delete();
		PathUpdate deleteUpdate = getUpdate();

		Collection<Path> deletions = deleteUpdate.getDeletions();
		Collection<Path> changes = deleteUpdate.getChanges();

		assertEquals(1, deletions.size());
		assertTrue(changes.size() <= 1);
		assertEquals(0, deleteUpdate.getCreations().size());

		Path deletion = CollectionUtils.extractSingleton(deletions);

		assertEquals(file, deletion);
		assertEquals(isDeleted, !deletion.toFile().exists());
	}

	private void testFileChange(File file) throws IOException, InterruptedException {
		FileUtilities.writeStringToFile(TEST_XML_CONTENT, file);

		int changeCnt = 0;
		while (true) {
			PathUpdate changeUpdate = getUpdate();
			assertEquals(0, changeUpdate.getDeletions().size());
			assertEquals(0, changeUpdate.getCreations().size());

			Collection<Path> changes = changeUpdate.getChanges();
			changeCnt += changes.size();

			if (changes.contains(file.toPath())) {
				assertEquals(TEST_XML_CONTENT, FileUtilities.readFileToString(file));
				break;
			}

			if (changeCnt < 2) {
				// The directory is also changed and this change may be reported first in its own
				// chunk.
				continue;
			}

			assertEquals(2, changeCnt);
		}
	}

	private void testFileCreation(File newFile) throws IOException, InterruptedException {
		if (newFile.createNewFile()) {
			PathUpdate createUpdate = getUpdate();

			Collection<Path> creations = createUpdate.getCreations();
			Collection<Path> changes = createUpdate.getChanges();

			assertEquals(1, creations.size());
			assertTrue(changes.size() <= 1);
			assertEquals(0, createUpdate.getDeletions().size());

			Path creation = CollectionUtils.extractSingleton(creations);

			assertEquals(newFile, creation);
		} else {
			fail("File could not be created.");
		}
	}

	private void assertEquals(File file, Path path) throws IOException {
		assertEquals(file.getCanonicalPath(), path.toFile().getCanonicalPath());
	}

	private PathUpdate getUpdate() throws InterruptedException {
		assertFileManagerHasUpdates();
		return _update;
	}

	/**
	 * Validates the {@link FileManager}. Checks every
	 * {@value TestFileSystemCache#TIMEOUT_BEFORE_POLLING} milliseconds for filesystem updates,
	 * maximal {@value TestFileSystemCache#MAX_TIMEOUT_FOR_EVENTS} milliseconds.
	 */
	public void assertFileManagerHasUpdates() throws InterruptedException {
		_update = null;
		for (int i = 0; i <= MAX_TIMEOUT_FOR_EVENTS / TIMEOUT_BEFORE_POLLING; i++) {
			FileSystemCache.getCache().fetchUpdates();
			if (_updates.hasNext()) {
				_update = _updates.next();
				return;
			}

			Thread.sleep(TIMEOUT_BEFORE_POLLING);
		}
		
		fail("Missing update.");
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(ServiceTestSetup.createSetup(
			WebappFileManagerTestSetup.setup(TestFileSystemCache.class), FileSystemCache.Module.INSTANCE));
	}
}
