/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static java.nio.charset.StandardCharsets.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.DefaultFileManager;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.FileManagerOverlay;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.io.FileUtilities;

/**
 * Test case for {@link FileManagerOverlay} stacked over a {@link MultiFileManager} whose resources
 * partly live inside a zip file system (as web-fragment WARs are mounted).
 */
@SuppressWarnings("javadoc")
public class TestFileManagerOverlay extends TestCase {

	private static final String IN_ZIP = "/WEB-INF/conf/in-zip.xml";

	private static final String IN_OVERLAY = "/WEB-INF/conf/in-overlay.xml";

	private static final String SHADOWED = "/WEB-INF/conf/shadowed.xml";

	private static final String MISSING = "/WEB-INF/conf/missing.xml";

	private static final String IN_ZIP_CONTENT = "<in-zip/>";

	private static final String IN_OVERLAY_CONTENT = "<in-overlay/>";

	private static final String SHADOWED_ZIP_CONTENT = "<shadowed-in-zip/>";

	private static final String SHADOWED_OVERLAY_CONTENT = "<shadowed-in-overlay/>";

	private Path _tmpDir;

	private FileSystem _zipFs;

	private FileManager _fileManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tmpDir = Files.createTempDirectory("TestFileManagerOverlay");

		Path zip = _tmpDir.resolve("fragment.war");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
			addEntry(out, IN_ZIP, IN_ZIP_CONTENT);
			addEntry(out, SHADOWED, SHADOWED_ZIP_CONTENT);
		}
		_zipFs = FileSystems.newFileSystem(zip);

		Path overlayDir = _tmpDir.resolve("overlay");
		writeFile(overlayDir, IN_OVERLAY, IN_OVERLAY_CONTENT);
		writeFile(overlayDir, SHADOWED, SHADOWED_OVERLAY_CONTENT);

		Path emptyWebapp = Files.createDirectories(_tmpDir.resolve("webapp"));

		FileManager multi =
			MultiFileManager.createMultiFileManager(emptyWebapp, _zipFs.getPath("/"));
		FileManager overlay = new DefaultFileManager(overlayDir.toFile());
		_fileManager = new FileManagerOverlay(overlay, multi);
	}

	@Override
	protected void tearDown() throws Exception {
		_fileManager = null;
		if (_zipFs != null) {
			_zipFs.close();
			_zipFs = null;
		}
		if (_tmpDir != null) {
			FileUtilities.deleteR(_tmpDir.toFile());
			_tmpDir = null;
		}
		super.tearDown();
	}

	public void testStreamFromZip() throws IOException {
		try (InputStream in = _fileManager.getStreamOrNull(IN_ZIP)) {
			assertNotNull(in);
			assertEquals(IN_ZIP_CONTENT, read(in));
		}
		try (InputStream in = _fileManager.getStream(IN_ZIP)) {
			assertEquals(IN_ZIP_CONTENT, read(in));
		}
	}

	public void testResourceUrlFromZip() throws IOException {
		URL url = _fileManager.getResourceUrl(IN_ZIP);
		assertNotNull(url);
		try (InputStream in = url.openStream()) {
			assertEquals(IN_ZIP_CONTENT, read(in));
		}
	}

	@SuppressWarnings("deprecation")
	public void testDataFromZip() throws IOException {
		try (InputStream in = _fileManager.getData(IN_ZIP).getStream()) {
			assertEquals(IN_ZIP_CONTENT, read(in));
		}
		try (InputStream in = _fileManager.getBinaryContent(IN_ZIP).getStream()) {
			assertEquals(IN_ZIP_CONTENT, read(in));
		}
	}

	public void testOverlayOnly() throws IOException {
		try (InputStream in = _fileManager.getStream(IN_OVERLAY)) {
			assertEquals(IN_OVERLAY_CONTENT, read(in));
		}
		URL url = _fileManager.getResourceUrl(IN_OVERLAY);
		assertNotNull(url);
		try (InputStream in = url.openStream()) {
			assertEquals(IN_OVERLAY_CONTENT, read(in));
		}
	}

	public void testOverlayPrecedence() throws IOException {
		try (InputStream in = _fileManager.getStream(SHADOWED)) {
			assertEquals(SHADOWED_OVERLAY_CONTENT, read(in));
		}
		try (InputStream in = _fileManager.getResourceUrl(SHADOWED).openStream()) {
			assertEquals(SHADOWED_OVERLAY_CONTENT, read(in));
		}
		try (InputStream in = _fileManager.getData(SHADOWED).getStream()) {
			assertEquals(SHADOWED_OVERLAY_CONTENT, read(in));
		}
	}

	public void testMissing() throws IOException {
		assertNull(_fileManager.getStreamOrNull(MISSING));
		assertNull(_fileManager.getResourceUrl(MISSING));
	}

	private static void addEntry(ZipOutputStream out, String name, String content) throws IOException {
		out.putNextEntry(new ZipEntry(name.substring(1)));
		out.write(content.getBytes(UTF_8));
		out.closeEntry();
	}

	private static void writeFile(Path root, String name, String content) throws IOException {
		Path file = root.resolve(name.substring(1));
		Files.createDirectories(file.getParent());
		try (OutputStream out = Files.newOutputStream(file)) {
			out.write(content.getBytes(UTF_8));
		}
	}

	private static String read(InputStream in) throws IOException {
		return new String(in.readAllBytes(), UTF_8);
	}

	public static Test suite() {
		return new FileManagerTestSetup(new TestSuite(TestFileManagerOverlay.class));
	}

}
