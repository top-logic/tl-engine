/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.core.workspace;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.core.workspace.ApplicationResources;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.core.workspace.PathInfo;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.io.binary.BinaryDataFactory;

/**
 * Test for the web application folder of the application module, see
 * {@link PathInfo#getToplevelWebapp()}, and its use as location of new files in
 * {@link MultiFileManager#getIDEFile(String)}.
 */
@SuppressWarnings("javadoc")
public class TestPathInfoToplevelWebapp extends BasicTestCase {

	private static final String GROUP_ID = "g";

	private static final String APP = "app";

	private static final String FRAGMENT = "fragment";

	private static final String NEW_RESOURCE = "/WEB-INF/autoconf/new.xml";

	private static final String FRAGMENT_RESOURCE = "/WEB-INF/conf/fragment.xml";

	private static final String NEW_RESOURCE_FOLDER = "/WEB-INF/autoconf/";

	private static final String CREATED_RESOURCE = NEW_RESOURCE_FOLDER + "created.xml";

	private static final String CONTENT = "<x/>";

	private static final String[] NO_DEPLOY_ASPECTS = {};

	private static final String[] LOCAL_ASPECT = { ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME };

	private File _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir(getClass().getSimpleName()).getCanonicalFile();
	}

	/**
	 * An application module without own web application folder is not part of the resource path,
	 * the fragment comes first. Its web application folder is nevertheless the location for new
	 * files.
	 */
	public void testApplicationWithoutWebapp() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, false, FRAGMENT);

		PathInfo pathInfo = pathInfo(app, fragment);

		assertEquals(webapp(app), pathInfo.getToplevelWebapp());
		assertFalse(pathInfo.getResourceDirs().contains(webapp(app)));
		assertEquals(webapp(fragment), pathInfo.getResourceDirs().get(0));

		// The estimate from the first path of the resource path selects the fragment.
		FileManager estimated = MultiFileManager.createMultiFileManager(pathInfo.getResourceDirs());
		assertEquals(webapp(fragment), estimated.getTopLevelWebapp());

		FileManager fileManager = fileManager(pathInfo);
		assertEquals(webapp(app), fileManager.getTopLevelWebapp());
		assertEquals(newFile(app), fileManager.getIDEFile(NEW_RESOURCE));
	}

	/**
	 * A file created in the not yet existing web application folder of the application module is
	 * found afterwards, the resolution order of the other paths is unchanged.
	 */
	public void testFileCreatedInMissingWebapp() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, false, FRAGMENT);
		PathInfo pathInfo = pathInfo(app, fragment);

		FileManager fileManager = fileManager(pathInfo);
		List<Path> paths = fileManager.getPaths();
		assertEquals(webapp(app), paths.get(0));
		assertEquals(pathInfo.getResourceDirs(), paths.subList(1, paths.size()));
		assertFalse(fileManager.exists(NEW_RESOURCE));

		// Written directly to the file system.
		File file = fileManager.getIDEFile(NEW_RESOURCE);
		file.getParentFile().mkdirs();
		Files.write(file.toPath(), CONTENT.getBytes(StandardCharsets.UTF_8));
		fileManager.refresh(NEW_RESOURCE);

		assertEquals(newFile(app), file);
		assertTrue(fileManager.exists(NEW_RESOURCE));
		assertNotNull(fileManager.getDataOrNull(NEW_RESOURCE));
		assertTrue(fileManager.getResourcePaths(NEW_RESOURCE_FOLDER).contains(NEW_RESOURCE));

		// Written through the file manager.
		fileManager.createData(CREATED_RESOURCE,
			BinaryDataFactory.createBinaryData(CONTENT.getBytes(StandardCharsets.UTF_8)));

		assertEquals(webapp(app).resolve(CREATED_RESOURCE.substring(1)).toFile(),
			fileManager.getIDEFile(CREATED_RESOURCE));
		assertTrue(fileManager.exists(CREATED_RESOURCE));
		assertTrue(fileManager.getResourcePaths(NEW_RESOURCE_FOLDER).contains(CREATED_RESOURCE));
	}

	/**
	 * The not yet existing web application folder of the application module is searched after the
	 * deploy folders of the application module, as an existing one would be.
	 */
	public void testMissingWebappAfterDeployFolders() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, false, FRAGMENT);
		Path localWebapp = localWebapp(app);
		localWebapp.toFile().mkdirs();

		PathInfo pathInfo = pathInfo(LOCAL_ASPECT, app, fragment);
		assertEquals(List.of(localWebapp, webapp(fragment)), pathInfo.getResourceDirs());

		FileManager fileManager = fileManager(pathInfo);
		assertEquals(List.of(localWebapp, webapp(app), webapp(fragment)), fileManager.getPaths());
	}

	/**
	 * The application module is determined by dependencies, not by the order of the classpath.
	 */
	public void testFragmentFirstOnClasspath() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, true, FRAGMENT);

		PathInfo pathInfo = pathInfo(fragment, app);

		assertEquals(webapp(app), pathInfo.getToplevelWebapp());

		FileManager fileManager = fileManager(pathInfo);
		assertEquals(newFile(app), fileManager.getIDEFile(NEW_RESOURCE));
		assertEquals(pathInfo.getResourceDirs(), fileManager.getPaths());
	}

	/**
	 * A file existing in the fragment is still resolved in the fragment.
	 */
	public void testExistingFragmentFile() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, false, FRAGMENT);
		File existing = webapp(fragment).resolve(FRAGMENT_RESOURCE.substring(1)).toFile();
		existing.getParentFile().mkdirs();
		Files.write(existing.toPath(), CONTENT.getBytes(StandardCharsets.UTF_8));

		FileManager fileManager = fileManager(pathInfo(app, fragment));

		assertEquals(existing, fileManager.getIDEFile(FRAGMENT_RESOURCE));
	}

	/**
	 * With an explicit resource path, the application module is unknown and the first path of the
	 * resource path receives new files.
	 */
	public void testExplicitResourcePath() throws IOException {
		File fragment = createProject(FRAGMENT, true);
		File app = createProject(APP, true, FRAGMENT);
		String resourcePath = webapp(fragment) + File.pathSeparator + webapp(app);

		String before = System.setProperty(Workspace.RESOURCE_PATH_PROPERTY, resourcePath);
		ApplicationResources resources;
		try {
			resources = Workspace.applicationResources();
		} finally {
			if (before == null) {
				System.clearProperty(Workspace.RESOURCE_PATH_PROPERTY);
			} else {
				System.setProperty(Workspace.RESOURCE_PATH_PROPERTY, before);
			}
		}

		assertNull(resources.topLevelWebapp());
		assertEquals(List.of(webapp(fragment), webapp(app)), resources.resourceDirs());

		FileManager fileManager =
			MultiFileManager.createMultiFileManager(resources.resourceDirs(), resources.topLevelWebapp());
		assertEquals(newFile(fragment), fileManager.getIDEFile(NEW_RESOURCE));
	}

	private static File newFile(File project) {
		return webapp(project).resolve(NEW_RESOURCE.substring(1)).toFile();
	}

	private static FileManager fileManager(PathInfo pathInfo) {
		return MultiFileManager.createMultiFileManager(pathInfo.getResourceDirs(), pathInfo.getToplevelWebapp());
	}

	private static PathInfo pathInfo(File... projects) throws IOException {
		return pathInfo(NO_DEPLOY_ASPECTS, projects);
	}

	private static PathInfo pathInfo(String[] deployAspects, File... projects) throws IOException {
		List<Path> classpath = new ArrayList<>();
		for (File project : projects) {
			classpath.add(classesDir(project).toPath());
		}
		return Workspace.getAppPaths(classpath, ModuleLayoutConstants.DEPLOY_FOLDER_NAME, deployAspects);
	}

	private File createProject(String artifactId, boolean withWebapp, String... dependencies) throws IOException {
		File project = new File(_root, artifactId);
		classesDir(project).mkdirs();
		if (withWebapp) {
			webapp(project).toFile().mkdirs();
		}
		Files.write(new File(project, "pom.xml").toPath(),
			pom(artifactId, dependencies).getBytes(StandardCharsets.UTF_8));
		return project;
	}

	private static String pom(String artifactId, String... dependencies) {
		StringBuilder result = new StringBuilder();
		result.append("<project xmlns=\"http://maven.apache.org/POM/4.0.0\">");
		result.append("<modelVersion>4.0.0</modelVersion>");
		result.append("<groupId>").append(GROUP_ID).append("</groupId>");
		result.append("<artifactId>").append(artifactId).append("</artifactId>");
		result.append("<version>1.0</version>");
		result.append("<dependencies>");
		for (String dependency : dependencies) {
			result.append("<dependency>");
			result.append("<groupId>").append(GROUP_ID).append("</groupId>");
			result.append("<artifactId>").append(dependency).append("</artifactId>");
			result.append("<version>1.0</version>");
			result.append("</dependency>");
		}
		result.append("</dependencies>");
		result.append("</project>");
		return result.toString();
	}

	private static File classesDir(File project) {
		return new File(new File(project, ModuleLayoutConstants.TARGET_DIR), ModuleLayoutConstants.CLASSES_DIR);
	}

	private static Path webapp(File project) {
		return new File(project, ModuleLayoutConstants.WEBAPP_DIR).toPath();
	}

	private static Path localWebapp(File project) {
		File aspectRoot = new File(new File(project, ModuleLayoutConstants.DEPLOY_FOLDER_NAME),
			ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME);
		return new File(aspectRoot, ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME).toPath();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPathInfoToplevelWebapp.class));
	}

}
