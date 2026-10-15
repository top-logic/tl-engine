/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.maven.war;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.top_logic.build.maven.war.AppModuleStack;
import com.top_logic.build.maven.war.AppModuleStack.Module;

/**
 * Test case for {@link AppModuleStack}.
 */
public class TestAppModuleStack {

	private static final String GROUP = "test.group";

	private static final String MARKER = "module.txt";

	/**
	 * Directory for the web fragment archives created by the test.
	 */
	@Rule
	public TemporaryFolder _tmp = new TemporaryFolder();

	/**
	 * Tests that the resource stack lists the application first and the most basic module last,
	 * independent of the order of the dependency artifacts.
	 */
	@Test
	public void testDependencyOrder() throws IOException {
		Model app = model("app", "a", "b", "c");
		Model a = model("a", "b");
		Model b = model("b", "c");
		Model c = model("c");

		List<Module> modules = List.of(
			new Module(c, fragment("c")),
			new Module(a, fragment("a")),
			new Module(b, fragment("b")));

		Path appDir = _tmp.newFolder("app").toPath();
		try (AppModuleStack stack = AppModuleStack.create(app, modules)) {
			assertEquals(GROUP + ":app", stack.getAppId());
			assertEquals(List.of(GROUP + ":c", GROUP + ":b", GROUP + ":a", GROUP + ":app"), stack.getBuildOrder());

			List<Path> resourceStack = stack.resourceStack(List.of(appDir));
			assertEquals(appDir, resourceStack.get(0));
			assertEquals(List.of("a", "b", "c"), markers(resourceStack.subList(1, resourceStack.size())));
		}
	}

	/**
	 * Tests that an application layer with several entries is put in front of the stack in the
	 * given order.
	 */
	@Test
	public void testMultiEntryAppLayer() throws IOException {
		Model app = model("app", "a");
		Model a = model("a");

		Path appTop = _tmp.newFolder("app-top").toPath();
		Path appBottom = _tmp.newFolder("app-bottom").toPath();
		try (AppModuleStack stack = AppModuleStack.create(app, List.of(new Module(a, fragment("a"))))) {
			List<Path> resourceStack = stack.resourceStack(List.of(appTop, appBottom));
			assertEquals(3, resourceStack.size());
			assertEquals(appTop, resourceStack.get(0));
			assertEquals(appBottom, resourceStack.get(1));
			assertEquals(List.of("a"), markers(resourceStack.subList(2, 3)));
		}
	}

	/**
	 * Tests that a module without web fragment is left out of the resource stack, but still orders
	 * the modules it depends on.
	 */
	@Test
	public void testModuleWithoutFragment() throws IOException {
		Model app = model("app", "a", "b");
		Model a = model("a", "b");
		Model b = model("b");

		List<Module> modules = List.of(
			new Module(b, fragment("b")),
			new Module(a, null));

		try (AppModuleStack stack = AppModuleStack.create(app, modules)) {
			assertNull(stack.getFragment(GROUP + ":a"));
			assertNull(stack.getFragment(stack.getAppId()));
			assertNotNull(stack.getFragment(GROUP + ":b"));

			assertEquals(List.of("b"), markers(stack.resourceStack(List.of())));
		}
	}

	/**
	 * Tests that closing the stack closes the opened archives.
	 */
	@Test
	public void testClose() throws IOException {
		Model app = model("app", "a");
		Model a = model("a");

		AppModuleStack stack = AppModuleStack.create(app, List.of(new Module(a, fragment("a"))));
		List<Path> resourceStack = stack.resourceStack(List.of());
		assertEquals(1, resourceStack.size());
		assertTrue(resourceStack.get(0).getFileSystem().isOpen());

		stack.close();
		assertFalse(resourceStack.get(0).getFileSystem().isOpen());
	}

	private static List<String> markers(List<Path> roots) throws IOException {
		List<String> result = new ArrayList<>();
		for (Path root : roots) {
			result.add(Files.readString(root.resolve(MARKER), StandardCharsets.UTF_8));
		}
		return result;
	}

	private static Model model(String artifactId, String... dependencies) {
		Model result = new Model();
		result.setModelVersion("4.0.0");
		result.setGroupId(GROUP);
		result.setArtifactId(artifactId);
		for (String dependencyId : dependencies) {
			Dependency dependency = new Dependency();
			dependency.setGroupId(GROUP);
			dependency.setArtifactId(dependencyId);
			result.addDependency(dependency);
		}
		return result;
	}

	private Artifact fragment(String artifactId) throws IOException {
		File file = _tmp.newFile(artifactId + "-web-fragment.war");
		try (OutputStream out = Files.newOutputStream(file.toPath());
				ZipOutputStream zip = new ZipOutputStream(out)) {
			zip.putNextEntry(new ZipEntry(MARKER));
			zip.write(artifactId.getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}

		DefaultArtifact result = new DefaultArtifact(GROUP, artifactId, "1.0", "compile", "war", "web-fragment",
			new DefaultArtifactHandler("war"));
		result.setFile(file);
		return result;
	}

}
