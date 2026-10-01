/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.model.TLModel;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ModelSecurity;
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
		String digest = digest(model, false);

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
		String digest = digest(model, false);

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
		String digest = digest(model, false);

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
		String digest = digest(model, false);

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
		String digest = digest(model, false);

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
		String digest = digest(model, false);

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
		String digest = digest(model, true);

		assertTrue("Digest should contain framework module tl.model",
			digest.contains("## tl.model"));
	}

	/**
	 * Tests that the digest uses reference arrow notation.
	 */
	public void testDigestUsesArrowNotation() {
		TLModel model = ModelService.getApplicationModel();
		String digest = digest(model, false);

		assertTrue("Digest should use -> arrow for references",
			digest.contains("-> "));
	}

	/**
	 * A documented module, type and part carry their description, on one line and in the requested
	 * language; an undocumented type and part carry none.
	 */
	public void testDigestCarriesDescriptionsInTheGivenLanguage() {
		TLModel model = ModelService.getApplicationModel();
		String english = ModelDigestGenerator.generate(model, false, ModelSecurity.UNCHECKED, Locale.ENGLISH);
		String german = ModelDigestGenerator.generate(model, false, ModelSecurity.UNCHECKED, Locale.GERMAN);

		assertTrue(english, english.contains("## luigis.pizzashop (6 types, 1 enum)\n"
			+ "// Product costing of a pizza shop: what a product is made of and what it costs to make.\n"));
		assertTrue(english, english.contains("\n// A dish the shop sells, with the components one unit of it needs.\n"
			+ "class Product extends NamedEntities {\n"));
		assertTrue("The line break of the tooltip is collapsed.", english.contains(
			"  budget: tl.core:Double // The most one unit of the product may cost to make, in euros."
				+ " Compared with the cost per unit.\n"));

		assertTrue(english, english.contains("\n\nclass Ingredient extends Component { }\n"));
		assertTrue(english, english.contains("  components: -> ProductComponent[] (mandatory, composite)\n"));

		assertTrue(german, german.contains("\n// Ein Gericht, das die Pizzeria verkauft, mit den Komponenten,"
			+ " die eine Einheit davon braucht.\nclass Product extends NamedEntities {\n"));
		assertTrue(german, german.contains("  budget: tl.core:Double // Was die Herstellung einer Einheit des"
			+ " Produkts höchstens kosten darf, in Euro. Wird mit den Kosten pro Einheit verglichen.\n"));
	}

	/**
	 * Tests that the outlineModel tool returns the application model digest as plain text.
	 */
	public void testOutlineModelToolReturnsDigest() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String overview = tool.outlineModel(Boolean.FALSE, ToolContext.unrestricted());

		assertNotNull("Overview should not be null", overview);
		assertTrue("Overview should contain pizzashop module",
			overview.contains("## luigis.pizzashop"));
		assertTrue("Overview should contain Product class",
			overview.contains("class Product"));
		assertFalse("Overview should exclude framework modules by default",
			overview.contains("## tl.core"));
	}

	/**
	 * The digest of everything, for the cases that are about the notation rather than about who may
	 * read what.
	 *
	 * <p>
	 * {@link ModelSecurity#UNCHECKED} is the test seam for a setup with no booted model security; what
	 * a real account sees is asserted against the fixture's personas by {@code SecDemoModelDigestTest}.
	 * There is no form of {@code generate} without a principal to call instead.
	 * </p>
	 */
	private static String digest(TLModel model, boolean includeFramework) {
		return ModelDigestGenerator.generate(model, includeFramework, ModelSecurity.UNCHECKED, Locale.ENGLISH);
	}

	/** */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(ModelDigestGeneratorTest.class),
				ModelService.Module.INSTANCE));
	}
}
