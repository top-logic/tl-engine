/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import junit.framework.TestCase;

import com.top_logic.factory.project.I18NConstants;
import com.top_logic.factory.project.ProjectWorkspaces;

/**
 * Test of {@link ProjectWorkspaces#checkGroupId(String)} and
 * {@link ProjectWorkspaces#checkArtifactId(String)}: the coordinates of a project must map to a
 * valid Java package, which the archetype derives from them.
 */
public class TestCoordinateCheck extends TestCase {

	/**
	 * Coordinates whose parts are Java identifiers are accepted.
	 */
	public void testValid() {
		assertNull(ProjectWorkspaces.checkGroupId("com.example"));
		assertNull(ProjectWorkspaces.checkGroupId("com.example_2.factory"));
		assertNull(ProjectWorkspaces.checkArtifactId("my-app"));
		assertNull(ProjectWorkspaces.checkArtifactId("ci-app-a9389df6"));
		assertNull(ProjectWorkspaces.checkArtifactId("app2"));
	}

	/**
	 * A part of the artifact ID starting with a digit would start a package name with a digit.
	 */
	public void testDigitLeadingPart() {
		assertEquals(I18NConstants.ERROR_ARTIFACT_ID_PART_STARTS_WITH_DIGIT__VALUE_PART.fill("ci-app-9389df65", "9389df65"),
			ProjectWorkspaces.checkArtifactId("ci-app-9389df65"));
		assertEquals(I18NConstants.ERROR_INVALID_ARTIFACT_ID__VALUE.fill("2app"), ProjectWorkspaces.checkArtifactId("2app"));
	}

	/**
	 * Dashes without a part between them or at the end would give an empty package name.
	 */
	public void testEmptyPart() {
		assertEquals(I18NConstants.ERROR_ARTIFACT_ID_EMPTY_PART__VALUE.fill("my--app"),
			ProjectWorkspaces.checkArtifactId("my--app"));
		assertEquals(I18NConstants.ERROR_ARTIFACT_ID_EMPTY_PART__VALUE.fill("my-app-"),
			ProjectWorkspaces.checkArtifactId("my-app-"));
	}

	/**
	 * A reserved word of Java cannot be a package name, neither in the group nor in the artifact.
	 */
	public void testKeyword() {
		assertEquals(I18NConstants.ERROR_JAVA_KEYWORD__VALUE_PART.fill("my-new-app", "new"),
			ProjectWorkspaces.checkArtifactId("my-new-app"));
		assertEquals(I18NConstants.ERROR_JAVA_KEYWORD__VALUE_PART.fill("int", "int"),
			ProjectWorkspaces.checkArtifactId("int"));
		assertEquals(I18NConstants.ERROR_JAVA_KEYWORD__VALUE_PART.fill("com.int.example", "int"),
			ProjectWorkspaces.checkGroupId("com.int.example"));
		assertEquals(I18NConstants.ERROR_JAVA_KEYWORD__VALUE_PART.fill("com._", "_"),
			ProjectWorkspaces.checkGroupId("com._"));
	}

	/**
	 * Values outside the allowed characters are rejected as before.
	 */
	public void testInvalidCharacters() {
		assertEquals(I18NConstants.ERROR_INVALID_ARTIFACT_ID__VALUE.fill("My-App"),
			ProjectWorkspaces.checkArtifactId("My-App"));
		assertEquals(I18NConstants.ERROR_INVALID_GROUP_ID__VALUE.fill("com.1example"),
			ProjectWorkspaces.checkGroupId("com.1example"));
	}

}
