/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.IDEFileSystemCache;
import com.top_logic.basic.io.PathUpdate;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * Tests that {@link FileManager#refresh(String)}, {@link FileManager#createData(String, BinaryContent)}
 * and {@link FileManager#delete(String)} make changes visible through an {@link IDEFileSystemCache}
 * immediately, without waiting for file system events.
 *
 * <p>
 * The {@link FileManager} under test is a {@link MultiFileManager} over two fresh root
 * directories, an application root overlaying a library root. No assertion depends on file system
 * events arriving within some time: Where the effect of late events is checked, the events that
 * arrive within a bounded time are consumed, and the test does not fail if none arrive.
 * </p>
 */
public class TestFileSystemCacheRefresh extends TestCase {

	/**
	 * Milliseconds to wait between two polls for file system events.
	 */
	private static final int POLL_INTERVAL = 10;

	/**
	 * Maximum milliseconds to wait for file system events.
	 */
	private static final int MAX_EVENT_WAIT = 2000;

	private static final String APP_CONTENT = "<app/>";

	private static final String LIB_CONTENT = "<lib/>";

	private static final String RESOURCE = LayoutConstants.LAYOUT_BASE_RESOURCE + "/refreshTest.xml";

	private static final String DIR_RESOURCE = LayoutConstants.LAYOUT_BASE_RESOURCE + "/refreshDir/";

	private static final String DIR_FILE_1 = DIR_RESOURCE + "file1.xml";

	private static final String DIR_FILE_2 = DIR_RESOURCE + "sub/file2.xml";

	private Iterator<PathUpdate> _updates;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		assertTrue("Test requires an " + IDEFileSystemCache.class.getSimpleName() + ".",
			FileSystemCache.getCache() instanceof IDEFileSystemCache);

		FileSystemCache.getCache().fetchUpdates();
		_updates = FileSystemCache.getCache().getUpdates();
	}

	@Override
	protected void tearDown() throws Exception {
		_updates = null;

		super.tearDown();
	}

	/**
	 * Tests that a file written directly to the file system is visible after
	 * {@link FileManager#refresh(String)}, in the order of the overlaid roots, and that the events
	 * arriving later do not duplicate the overlays.
	 */
	public void testRefreshCreatedFile() throws IOException, InterruptedException {
		FileManager fm = FileManager.getInstance();
		File appFile = fm.getIDEFile(RESOURCE);
		File libFile = libRoot().resolve(relative(RESOURCE)).toFile();
		try {
			FileUtilities.writeStringToFile(LIB_CONTENT, libFile);
			fm.refresh(RESOURCE);
			assertEquals(list(LIB_CONTENT), contents(fm.getDataOverlays(RESOURCE)));
			assertEquals(LIB_CONTENT, content(fm.getDataOrNull(RESOURCE)));

			FileUtilities.writeStringToFile(APP_CONTENT, appFile);
			fm.refresh(RESOURCE);
			assertEquals(list(APP_CONTENT, LIB_CONTENT), contents(fm.getDataOverlays(RESOURCE)));
			assertEquals(APP_CONTENT, content(fm.getDataOrNull(RESOURCE)));

			drainEvents(PathUpdate::getCreations, libFile, appFile);
			assertEquals(list(APP_CONTENT, LIB_CONTENT), contents(fm.getDataOverlays(RESOURCE)));
		} finally {
			cleanup(RESOURCE, appFile, libFile);
		}
	}

	/**
	 * Tests that a file deleted directly in the file system is no longer visible after
	 * {@link FileManager#refresh(String)}.
	 */
	public void testRefreshDeletedFile() throws IOException, InterruptedException {
		FileManager fm = FileManager.getInstance();
		File appFile = fm.getIDEFile(RESOURCE);
		try {
			FileUtilities.writeStringToFile(APP_CONTENT, appFile);
			fm.refresh(RESOURCE);
			assertEquals(list(APP_CONTENT), contents(fm.getDataOverlays(RESOURCE)));

			assertTrue(appFile.delete());
			fm.refresh(RESOURCE);
			assertEquals(list(), contents(fm.getDataOverlays(RESOURCE)));
			assertNull(fm.getDataOrNull(RESOURCE));

			drainEvents(PathUpdate::getDeletions, appFile);
			assertEquals(list(), contents(fm.getDataOverlays(RESOURCE)));
		} finally {
			cleanup(RESOURCE, appFile);
		}
	}

	/**
	 * Tests that the refresh of a directory makes all files below it visible, and after the
	 * directory has been deleted as a whole, drops all of them.
	 */
	public void testRefreshDirectory() throws IOException, InterruptedException {
		FileManager fm = FileManager.getInstance();
		File dir = fm.getIDEFile(DIR_RESOURCE);
		File file1 = fm.getIDEFile(DIR_FILE_1);
		File file2 = fm.getIDEFile(DIR_FILE_2);
		try {
			file2.getParentFile().mkdirs();
			FileUtilities.writeStringToFile(APP_CONTENT, file1);
			FileUtilities.writeStringToFile(LIB_CONTENT, file2);
			fm.refresh(DIR_RESOURCE);
			assertEquals(list(APP_CONTENT), contents(fm.getDataOverlays(DIR_FILE_1)));
			assertEquals(list(LIB_CONTENT), contents(fm.getDataOverlays(DIR_FILE_2)));

			assertTrue(FileUtilities.deleteR(dir));
			fm.refresh(DIR_RESOURCE);
			assertEquals(list(), contents(fm.getDataOverlays(DIR_FILE_1)));
			assertEquals(list(), contents(fm.getDataOverlays(DIR_FILE_2)));
			assertNull(fm.getDataOrNull(DIR_FILE_1));
			assertNull(fm.getDataOrNull(DIR_FILE_2));

			drainEvents(PathUpdate::getDeletions, dir);
			assertEquals(list(), contents(fm.getDataOverlays(DIR_FILE_1)));
			assertEquals(list(), contents(fm.getDataOverlays(DIR_FILE_2)));
		} finally {
			cleanup(DIR_RESOURCE, dir);
		}
	}

	/**
	 * Tests that {@link FileManager#createData(String, BinaryContent)} and
	 * {@link FileManager#delete(String)} take effect on subsequent lookups immediately.
	 */
	public void testCreateAndDeleteData() throws IOException, InterruptedException {
		FileManager fm = FileManager.getInstance();
		File appFile = fm.getIDEFile(RESOURCE);
		try {
			fm.createData(RESOURCE, BinaryDataFactory.createBinaryData(APP_CONTENT.getBytes(StandardCharsets.UTF_8)));
			assertEquals(list(APP_CONTENT), contents(fm.getDataOverlays(RESOURCE)));
			assertEquals(APP_CONTENT, content(fm.getDataOrNull(RESOURCE)));

			drainEvents(PathUpdate::getCreations, appFile);
			assertEquals(list(APP_CONTENT), contents(fm.getDataOverlays(RESOURCE)));

			fm.delete(RESOURCE);
			assertFalse(appFile.exists());
			assertEquals(list(), contents(fm.getDataOverlays(RESOURCE)));
			assertNull(fm.getDataOrNull(RESOURCE));

			drainEvents(PathUpdate::getDeletions, appFile);
			assertEquals(list(), contents(fm.getDataOverlays(RESOURCE)));
		} finally {
			cleanup(RESOURCE, appFile);
		}
	}

	/**
	 * Deletes the given files left over by a test, refreshes the given resource and consumes the
	 * file system events of the deletion, so that they are not reported to the next test.
	 */
	private void cleanup(String resourceName, File... files) throws InterruptedException, IOException {
		List<File> existing = new ArrayList<>();
		for (File file : files) {
			if (file.exists()) {
				existing.add(file);
				FileUtilities.deleteR(file);
			}
		}
		FileManager.getInstance().refresh(resourceName);
		drainEvents(PathUpdate::getDeletions, existing.toArray(new File[existing.size()]));
	}

	/**
	 * Consumes file system events until events of the given kind have been reported for all given
	 * files, but at most {@value #MAX_EVENT_WAIT} milliseconds.
	 *
	 * <p>
	 * Does not fail, if the events are not reported in time, or not at all: A file created and
	 * deleted before the events are fetched is reported neither as created nor as deleted.
	 * </p>
	 */
	private void drainEvents(Function<PathUpdate, Collection<Path>> kind, File... files)
			throws InterruptedException, IOException {
		List<Path> expected = canonicalPaths(files);
		for (int i = 0; !expected.isEmpty() && i <= MAX_EVENT_WAIT / POLL_INTERVAL; i++) {
			FileSystemCache.getCache().fetchUpdates();
			while (_updates.hasNext()) {
				expected.removeAll(canonicalPaths(kind.apply(_updates.next())));
			}
			if (!expected.isEmpty()) {
				Thread.sleep(POLL_INTERVAL);
			}
		}
	}

	private static List<Path> canonicalPaths(File... files) throws IOException {
		List<Path> result = new ArrayList<>();
		for (File file : files) {
			result.add(file.getCanonicalFile().toPath());
		}
		return result;
	}

	private static List<Path> canonicalPaths(Collection<Path> paths) throws IOException {
		List<Path> result = new ArrayList<>();
		for (Path path : paths) {
			result.add(path.toFile().getCanonicalFile().toPath());
		}
		return result;
	}

	private static List<String> contents(List<BinaryData> overlays) throws IOException {
		List<String> result = new ArrayList<>();
		for (BinaryData data : overlays) {
			result.add(content(data));
		}
		return result;
	}

	private static String content(BinaryData data) throws IOException {
		assertNotNull(data);
		try (InputStream in = data.getStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static List<String> list(String... values) {
		return List.of(values);
	}

	private static String relative(String resourceName) {
		return resourceName.substring(1);
	}

	private static Path libRoot() {
		List<Path> paths = FileManager.getInstance().getPaths();
		return paths.get(paths.size() - 1);
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(new TestSuite(TestFileSystemCacheRefresh.class),
			FileSystemCache.Module.INSTANCE);
		innerTest = new TwoRootsSetup(innerTest);
		String customFile = CustomPropertiesDecorator.createFileName(TestFileSystemCacheRefresh.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return TLTestSetup.createTLTestSetup(innerTest);
	}

	/**
	 * {@link TestSetup} installing a {@link MultiFileManager} with two fresh root directories (an
	 * application root overlaying a library root) for the duration of the test.
	 */
	private static class TwoRootsSetup extends TestSetup {

		private FileManager _before;

		private Path _base;

		TwoRootsSetup(Test test) {
			super(test);
		}

		@Override
		protected void setUp() throws Exception {
			super.setUp();

			_base = Files.createTempDirectory(TestFileSystemCacheRefresh.class.getSimpleName()).toRealPath();
			Path app = createRoot("app");
			Path lib = createRoot("lib");
			_before = FileManager.setInstance(MultiFileManager.createMultiFileManager(app, lib));
		}

		private Path createRoot(String name) throws IOException {
			Path root = _base.resolve(name);
			Files.createDirectories(root.resolve(relative(LayoutConstants.LAYOUT_BASE_RESOURCE)));
			return root;
		}

		@Override
		protected void tearDown() throws Exception {
			FileManager.setInstance(_before);
			FileUtilities.deleteR(_base.toFile());

			super.tearDown();
		}
	}

}
