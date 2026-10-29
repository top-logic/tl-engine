/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.maven.packaging;

import static org.junit.Assert.*;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import com.top_logic.build.maven.packaging.TLPackaging;
import com.top_logic.build.maven.packaging.TLParentInjector;

/**
 * Test case for {@link TLParentInjector#checkBomVersion(MavenProject, String)}.
 */
public class TestBomVersionCheck {

	private static final String PLUGIN_VERSION = "8.0.0";

	private static final String OTHER_VERSION = "8.1.0";

	/**
	 * Tests that a project managing the plugin in the version of the plugin passes.
	 */
	@Test
	public void testMatchingVersion() throws MavenExecutionException {
		TLParentInjector.checkBomVersion(project(PLUGIN_VERSION), PLUGIN_VERSION);
	}

	/**
	 * Tests that a project managing the plugin in another version fails with a message naming both
	 * versions.
	 */
	@Test
	public void testDifferingVersion() {
		try {
			TLParentInjector.checkBomVersion(project(OTHER_VERSION), PLUGIN_VERSION);
			fail("Version mismatch not detected.");
		} catch (MavenExecutionException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(OTHER_VERSION));
			assertTrue(ex.getMessage(), ex.getMessage().contains(PLUGIN_VERSION));
		}
	}

	/**
	 * Tests that a project not managing the plugin passes.
	 */
	@Test
	public void testNoManagedPlugin() throws MavenExecutionException {
		TLParentInjector.checkBomVersion(project(null), PLUGIN_VERSION);
	}

	/**
	 * Tests that a project without dependency management passes.
	 */
	@Test
	public void testNoDependencyManagement() throws MavenExecutionException {
		Model model = model();
		TLParentInjector.checkBomVersion(new MavenProject(model), PLUGIN_VERSION);
	}

	/**
	 * A project whose dependency management manages the plugin in the given version, or does not
	 * manage the plugin if the version is <code>null</code>.
	 */
	private static MavenProject project(String managedPluginVersion) {
		DependencyManagement management = new DependencyManagement();
		management.addDependency(dependency("other.group", "other-artifact", OTHER_VERSION));
		if (managedPluginVersion != null) {
			management.addDependency(
				dependency(TLPackaging.TL_GROUP_ID, TLParentInjector.PLUGIN_ARTIFACT_ID, managedPluginVersion));
		}

		Model model = model();
		model.setDependencyManagement(management);
		return new MavenProject(model);
	}

	private static Model model() {
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId("test.group");
		model.setArtifactId("app");
		model.setVersion("1.0");
		model.setPackaging(TLPackaging.APP.getId());
		return model;
	}

	private static Dependency dependency(String groupId, String artifactId, String version) {
		Dependency result = new Dependency();
		result.setGroupId(groupId);
		result.setArtifactId(artifactId);
		result.setVersion(version);
		return result;
	}

}
