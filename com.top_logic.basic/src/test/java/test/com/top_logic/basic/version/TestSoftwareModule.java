/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.version;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.version.SoftwareModule;

import de.haumacher.msgbuf.data.DataObject;

/**
 * Tests for {@link SoftwareModule}: the module a class is delivered with, derived from where it is
 * loaded from.
 */
public class TestSoftwareModule extends TestCase {

	/** Artifact ID of this module, see its {@code pom.xml}. */
	private static final String BASIC_ARTIFACT = "tl-basic";

	/** A class from a jar on the test class path is attributed to the artifact of that jar. */
	public void testClassFromJar() {
		SoftwareModule module = SoftwareModule.of(DataObject.class);

		assertEquals("msgbuf-api", module.artifactId());
		assertNotNull(module.label());
		assertFalse(module.label().isEmpty());
	}

	/**
	 * A class from the {@code target/classes} folder of a module is attributed to the artifact its
	 * {@code pom.xml} declares - not to the one of its parent project.
	 */
	public void testClassFromModuleFolder() {
		assertEquals(BASIC_ARTIFACT, SoftwareModule.of(SoftwareModule.class).artifactId());
		assertEquals(BASIC_ARTIFACT, SoftwareModule.of(TestSoftwareModule.class).artifactId());
	}

	/** A class of the Java runtime is loaded from no file and has no known module. */
	public void testRuntimeClass() {
		assertSame(SoftwareModule.UNKNOWN, SoftwareModule.of(String.class));
	}

	/** The module is computed once per class. */
	public void testCached() {
		assertSame(SoftwareModule.of(DataObject.class), SoftwareModule.of(DataObject.class));
	}

	/** Without a Maven descriptor, a jar is named by its file name without version. */
	public void testJarFileName() {
		assertEquals("my-lib", SoftwareModule.artifactOfFileName("my-lib-1.2.3-SNAPSHOT.jar"));
		assertEquals("my-lib", SoftwareModule.artifactOfFileName("my-lib.jar"));
	}

	/** Suite with the file manager the version information of the application is read with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestSoftwareModule.class);
	}
}
