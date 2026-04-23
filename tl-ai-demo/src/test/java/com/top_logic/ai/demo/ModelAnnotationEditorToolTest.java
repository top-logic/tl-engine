/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;

import com.top_logic.tl.ai.core.tools.model.ModelAnnotationEditorTool;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Test cases for {@link ModelAnnotationEditorTool}.
 */
public class ModelAnnotationEditorToolTest extends ModelAnnotationBrowserToolTest {

	/**
	 * Tests setAnnotation with null target.
	 */
	public void testSetAnnotationNullTarget() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.addAnnotation(null, "com.example.Annotation", "{}");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests setAnnotation with empty configInterfaceName.
	 */
	public void testSetAnnotationEmptyConfigInterface() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.addAnnotation("tl.core", "", "{}");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests setAnnotation with nonexistent class.
	 */
	public void testSetAnnotationInvalidClass() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.addAnnotation("tl.core", "com.nonexistent.Class", "{}");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
		assertTrue("Error should mention class not found",
			result.contains("not found") || result.contains("Class"));
	}

	/**
	 * Tests setAnnotation with null configJson.
	 */
	public void testSetAnnotationNullJson() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.addAnnotation("tl.core", "com.top_logic.model.annotate.TLAnnotation", null);

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests removeAnnotation with null target.
	 */
	public void testRemoveAnnotationNullTarget() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.removeAnnotation(null, "com.example.Annotation");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests removeAnnotation with empty configInterfaceName.
	 */
	public void testRemoveAnnotationEmptyConfigInterface() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.removeAnnotation("tl.core", "");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Tests removeAnnotation with nonexistent module.
	 */
	public void testRemoveAnnotationInvalidModule() {
		ModelAnnotationEditorTool tool = new ModelAnnotationEditorTool();
		String result = tool.removeAnnotation("nonexistent.module",
			"com.top_logic.model.annotate.TLAnnotation");

		assertNotNull("Result should not be null", result);
		assertTrue("Result should contain error", result.contains("\"error\""));
	}

	/**
	 * Creates the test suite with required service dependencies.
	 */
	public static Test suite() {
		return suite(ModelAnnotationEditorToolTest.class, PersistencyLayer.Module.INSTANCE);
	}
}
