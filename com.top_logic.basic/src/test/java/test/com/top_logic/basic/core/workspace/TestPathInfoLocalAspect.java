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

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.core.workspace.Workspace;

/**
 * Test for the selection of the project whose deploy aspect
 * {@link ModuleLayoutConstants#DEPLOY_LOCAL_FOLDER_NAME} is included by
 * {@link Workspace#getAppPaths(Iterable, String, String[])}.
 *
 * <p>
 * The selection among several independent projects by the working directory of the JVM is not
 * tested, since a test cannot change the working directory of its JVM.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPathInfoLocalAspect extends BasicTestCase {

	private static final String GROUP_ID = "g";

	private static final String APP = "app";

	private static final String LIB = "lib";

	private static final String OTHER = "other";

	private static final String[] DEPLOY_ASPECTS =
		{ ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME, ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME };

	private File _root;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_root = createdCleanTestDir(getClass().getSimpleName()).getCanonicalFile();
	}

	public void testLocalAspectOfApplicationOnly() throws IOException {
		File app = createProject(APP, LIB);
		File lib = createProject(LIB);

		List<Path> resourceDirs = resourceDirs(app, lib);

		assertLocalAspects(resourceDirs, app, lib);
	}

	public void testIndependentOfClasspathOrder() throws IOException {
		File app = createProject(APP, LIB);
		File lib = createProject(LIB);

		List<Path> resourceDirs = resourceDirs(lib, app);

		assertLocalAspects(resourceDirs, app, lib);
	}

	public void testNoLocalAspectForSeveralIndependentProjects() throws IOException {
		File app = createProject(APP);
		File other = createProject(OTHER);

		List<Path> resourceDirs = resourceDirs(app, other);

		assertFalse(resourceDirs.contains(aspectWebapp(app, ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME)));
		assertFalse(resourceDirs.contains(aspectWebapp(other, ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME)));
		assertTrue(resourceDirs.contains(aspectWebapp(app, ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME)));
		assertTrue(resourceDirs.contains(aspectWebapp(other, ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME)));
	}

	private static void assertLocalAspects(List<Path> resourceDirs, File app, File lib) {
		assertTrue(resourceDirs.contains(aspectWebapp(app, ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME)));
		assertFalse(resourceDirs.contains(aspectWebapp(lib, ModuleLayoutConstants.DEPLOY_LOCAL_FOLDER_NAME)));
		assertTrue(resourceDirs.contains(aspectWebapp(app, ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME)));
		assertTrue(resourceDirs.contains(aspectWebapp(lib, ModuleLayoutConstants.DEPLOY_PRIVATE_FOLDER_NAME)));
	}

	private static List<Path> resourceDirs(File... projects) throws IOException {
		List<Path> classpath = new ArrayList<>();
		for (File project : projects) {
			classpath.add(classesDir(project).toPath());
		}
		return Workspace.getAppPaths(classpath, ModuleLayoutConstants.DEPLOY_FOLDER_NAME, DEPLOY_ASPECTS)
			.getResourceDirs();
	}

	private File createProject(String artifactId, String... dependencies) throws IOException {
		File project = new File(_root, artifactId);
		classesDir(project).mkdirs();
		for (String aspect : DEPLOY_ASPECTS) {
			aspectWebapp(project, aspect).toFile().mkdirs();
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

	private static Path aspectWebapp(File project, String aspect) {
		File aspectRoot = new File(new File(project, ModuleLayoutConstants.DEPLOY_FOLDER_NAME), aspect);
		return new File(aspectRoot, ModuleLayoutConstants.WEBAPP_LOCAL_DIR_NAME).toPath();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPathInfoLocalAspect.class));
	}

}
