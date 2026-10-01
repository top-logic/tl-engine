/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.core.workspace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.core.workspace.PathInfo;
import com.top_logic.basic.core.workspace.Workspace;

/**
 * Test for the lookup of the web fragment of a jar on the classpath by
 * {@link Workspace#getAppPaths(Iterable, String, String[])}, in particular when jar and fragment
 * are resolved from different local Maven repositories of a repository chain.
 */
@SuppressWarnings("javadoc")
public class TestPathInfoFragmentLookup extends BasicTestCase {

	private static final String MAVEN_REPO_LOCAL = "maven.repo.local";

	private static final String MAVEN_REPO_LOCAL_TAIL = "maven.repo.local.tail";

	private static final String COORDINATE_DIR = "g/r/p/art/1.0";

	private static final String JAR_NAME = "art-1.0.jar";

	private static final String FRAGMENT_NAME = "art-1.0-web-fragment.war";

	private static final String MARKER_ENTRY = "META-INF/tl-module-with-resources";

	private static final String POM_ENTRY = "META-INF/maven/g.r.p/art/pom.xml";

	private static final String POM =
		"<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
			+ "<modelVersion>4.0.0</modelVersion>"
			+ "<groupId>g.r.p</groupId>"
			+ "<artifactId>art</artifactId>"
			+ "<version>1.0</version>"
			+ "</project>";

	private File _repoA;

	private File _repoB;

	private String _oldRepoLocal;

	private String _oldRepoLocalTail;

	private Logger _logger;

	private List<LogRecord> _warnings;

	private Handler _handler;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File root = createdCleanTestDir(getClass().getSimpleName()).getCanonicalFile();
		_repoA = new File(root, "repoA");
		_repoB = new File(root, "repoB");

		_oldRepoLocal = System.getProperty(MAVEN_REPO_LOCAL);
		_oldRepoLocalTail = System.getProperty(MAVEN_REPO_LOCAL_TAIL);
		System.clearProperty(MAVEN_REPO_LOCAL);
		System.clearProperty(MAVEN_REPO_LOCAL_TAIL);

		_warnings = new ArrayList<>();
		_handler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
					_warnings.add(record);
				}
			}

			@Override
			public void flush() {
				// Nothing to flush.
			}

			@Override
			public void close() {
				// Nothing to close.
			}
		};
		_logger = Logger.getLogger(PathInfo.class.getName());
		_logger.addHandler(_handler);
	}

	@Override
	protected void tearDown() throws Exception {
		_logger.removeHandler(_handler);
		restoreProperty(MAVEN_REPO_LOCAL, _oldRepoLocal);
		restoreProperty(MAVEN_REPO_LOCAL_TAIL, _oldRepoLocalTail);
		super.tearDown();
	}

	public void testFragmentNextToJar() throws IOException {
		File jar = createJar(_repoA, true);
		File fragment = createFragment(_repoA);

		List<URL> resourcePath = resourcePath(jar);

		assertTrue(resourcePath.contains(url(fragment)));
		assertTrue(_warnings.isEmpty());
	}

	public void testFragmentInOtherRepositoryOfChain() throws IOException {
		File jar = createJar(_repoA, true);
		File fragment = createFragment(_repoB);
		System.setProperty(MAVEN_REPO_LOCAL, _repoB.getPath());
		System.setProperty(MAVEN_REPO_LOCAL_TAIL, " , " + _repoA.getPath() + " ");

		List<URL> resourcePath = resourcePath(jar);

		assertTrue(resourcePath.contains(url(fragment)));
		assertTrue(_warnings.isEmpty());
	}

	public void testFragmentInTailRepository() throws IOException {
		File jar = createJar(_repoB, true);
		File fragment = createFragment(_repoA);
		System.setProperty(MAVEN_REPO_LOCAL, _repoB.getPath());
		System.setProperty(MAVEN_REPO_LOCAL_TAIL, _repoA.getPath());

		List<URL> resourcePath = resourcePath(jar);

		assertTrue(resourcePath.contains(url(fragment)));
		assertTrue(_warnings.isEmpty());
	}

	public void testMissingFragmentOfModuleWarns() throws IOException {
		File jar = createJar(_repoA, true);
		System.setProperty(MAVEN_REPO_LOCAL, _repoB.getPath());
		System.setProperty(MAVEN_REPO_LOCAL_TAIL, _repoA.getPath());

		List<URL> resourcePath = resourcePath(jar);

		assertFalse(resourcePath.contains(url(new File(new File(_repoA, COORDINATE_DIR), FRAGMENT_NAME))));
		assertFalse(resourcePath.contains(url(new File(new File(_repoB, COORDINATE_DIR), FRAGMENT_NAME))));
		assertEquals(1, _warnings.size());
		assertTrue(_warnings.get(0).getMessage().contains(jar.getPath()));
	}

	public void testMissingFragmentOfPlainJarIsSilent() throws IOException {
		File jar = createJar(_repoA, false);
		System.setProperty(MAVEN_REPO_LOCAL, _repoB.getPath());
		System.setProperty(MAVEN_REPO_LOCAL_TAIL, _repoA.getPath());

		resourcePath(jar);

		assertTrue(_warnings.isEmpty());
	}

	private static List<URL> resourcePath(File jar) throws IOException {
		return Workspace.getAppPaths(List.of(jar.toPath()), "deploy", new String[0]).getResourcePath();
	}

	private static File createJar(File repository, boolean withMarker) throws IOException {
		File jar = new File(coordinateDir(repository), JAR_NAME);
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(jar))) {
			addEntry(out, POM_ENTRY, POM);
			if (withMarker) {
				addEntry(out, MARKER_ENTRY, "");
			}
		}
		return jar;
	}

	private static File createFragment(File repository) throws IOException {
		File fragment = new File(coordinateDir(repository), FRAGMENT_NAME);
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fragment))) {
			addEntry(out, "WEB-INF/some.txt", "content");
		}
		return fragment;
	}

	private static File coordinateDir(File repository) {
		File dir = new File(repository, COORDINATE_DIR);
		dir.mkdirs();
		return dir;
	}

	private static void addEntry(ZipOutputStream out, String name, String content) throws IOException {
		out.putNextEntry(new ZipEntry(name));
		out.write(content.getBytes(StandardCharsets.UTF_8));
		out.closeEntry();
	}

	private static URL url(File file) throws IOException {
		return file.toURI().toURL();
	}

	private static void restoreProperty(String name, String value) {
		if (value == null) {
			System.clearProperty(name);
		} else {
			System.setProperty(name, value);
		}
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPathInfoFragmentLookup.class));
	}

}
