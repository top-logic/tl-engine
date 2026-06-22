/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.model.TLModel;
import com.top_logic.tl.ai.core.tools.model.ModelBrowserTool;
import com.top_logic.tl.ai.core.tools.model.ModelDigestGenerator;
import com.top_logic.util.model.ModelService;

/**
 * Test cases for {@link ModelDigestGenerator}.
 */
public class ModelDigestGeneratorTest extends BasicTestCase {

	/**
	 * Tests that the digest contains the pizza shop module and its types.
	 */
	public void testDigestContainsPizzashopModule() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertNotNull("Digest should not be null", digest);
		assertFalse("Digest should not be empty", digest.isEmpty());
		assertTrue("Digest should contain module header",
			digest.contains("## luigis.pizzashop"));
	}

	/**
	 * Tests that the digest contains type definitions with correct keywords.
	 */
	public void testDigestContainsTypeDefinitions() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertTrue("Digest should contain Product class",
			digest.contains("class Product"));
		assertTrue("Digest should contain Ingredient class",
			digest.contains("class Ingredient"));
		assertTrue("Digest should contain Component interface",
			digest.contains("interface Component") || digest.contains("class Component"));
		assertTrue("Digest should contain NamedEntities",
			digest.contains("NamedEntities"));
	}

	/**
	 * Tests that the digest contains enum definitions as one-liners.
	 */
	public void testDigestContainsEnums() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertTrue("Digest should contain UnitOfMeasure enum",
			digest.contains("enum UnitOfMeasure"));
		assertTrue("Digest should contain enum classifiers",
			digest.contains("G") && digest.contains("KG") && digest.contains("ML"));
	}

	/**
	 * Tests that the digest contains part definitions with type info.
	 */
	public void testDigestContainsParts() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertTrue("Digest should contain budget property",
			digest.contains("budget"));
		assertTrue("Digest should contain components reference",
			digest.contains("components"));
		assertTrue("Digest should contain mandatory marker",
			digest.contains("mandatory"));
		assertTrue("Digest should contain derived marker",
			digest.contains("derived"));
	}

	/**
	 * Tests that the digest contains inheritance information.
	 */
	public void testDigestContainsInheritance() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertTrue("Digest should show Product extends NamedEntities",
			digest.contains("Product extends NamedEntities"));
		assertTrue("Digest should show Ingredient extends Component",
			digest.contains("Ingredient extends Component"));
	}

	/**
	 * Tests that framework modules are excluded by default.
	 */
	public void testDigestExcludesFrameworkModules() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertFalse("Digest should NOT contain tl.model module",
			digest.contains("## tl.model"));
		assertFalse("Digest should NOT contain tl.core module",
			digest.contains("## tl.core"));
	}

	/**
	 * Tests that framework modules are included when requested.
	 */
	public void testDigestIncludesFrameworkModules() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, true);

		assertTrue("Digest should contain framework module tl.model",
			digest.contains("## tl.model"));
	}

	/**
	 * Tests that the digest uses reference arrow notation.
	 */
	public void testDigestUsesArrowNotation() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, false);

		assertTrue("Digest should use -> arrow for references",
			digest.contains("-> "));
	}

	/**
	 * Tests that the outlineModel tool returns the application model digest as plain text.
	 */
	public void testOutlineModelToolReturnsDigest() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String overview = tool.outlineModel(Boolean.FALSE);

		assertNotNull("Overview should not be null", overview);
		assertTrue("Overview should contain pizzashop module",
			overview.contains("## luigis.pizzashop"));
		assertTrue("Overview should contain Product class",
			overview.contains("class Product"));
		assertFalse("Overview should exclude framework modules by default",
			overview.contains("## tl.core"));
	}

	/** */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(ModelDigestGeneratorTest.class),
				ModelService.Module.INSTANCE));
	}
}
