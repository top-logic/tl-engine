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
	 * Tests that the digest contains the event-management module and its types.
	 */
	public void testDigestContainsEventsModule() {
		TLModel model = ModelService.getApplicationModel();
		String digest = digest(model, false);

		assertNotNull("Digest should not be null", digest);
		assertFalse("Digest should not be empty", digest.isEmpty());
		assertTrue("Digest should contain module header",
			digest.contains("## demo.events"));
	}

	/**
	 * Tests that the digest contains type definitions with correct keywords.
	 */
	public void testDigestContainsTypeDefinitions() {
		TLModel model = ModelService.getApplicationModel();
		String digest = digest(model, false);

		assertTrue("Digest should contain Event class",
			digest.contains("class Event"));
		assertTrue("Digest should contain Room class",
			digest.contains("class Room"));
		assertTrue("Digest should contain Documented interface",
			digest.contains("interface Documented"));
		assertTrue("Digest should contain Named",
			digest.contains("interface Named"));
	}

	/**
	 * Tests that the digest contains enum definitions as one-liners.
	 */
	public void testDigestContainsEnums() {
		TLModel model = ModelService.getApplicationModel();
		String digest = digest(model, false);

		assertTrue("Digest should contain EventStatus enum",
			digest.contains("enum EventStatus"));
		assertTrue("Digest should contain enum classifiers",
			digest.contains("DRAFT") && digest.contains("PLANNED") && digest.contains("SOLD_OUT"));
	}

	/**
	 * Tests that the digest contains part definitions with type info.
	 */
	public void testDigestContainsParts() {
		TLModel model = ModelService.getApplicationModel();
		String digest = digest(model, false);

		assertTrue("Digest should contain ticketPrice property",
			digest.contains("ticketPrice"));
		assertTrue("Digest should contain sessions reference",
			digest.contains("sessions"));
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

		assertTrue("Digest should show Event extends Named, Documented",
			digest.contains("Event extends Named, Documented"));
		assertTrue("Digest should show Room extends Named",
			digest.contains("Room extends Named"));
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

		assertTrue(english, english.contains("## demo.events (13 types, 8 enums)\n"
			+ "// Demo model for events, venues, sessions, registrations, sponsorships and invoices.\n"));
		assertTrue(english, english.contains("\n// An event such as a conference, with sessions, registrations,"
			+ " sponsorships and invoices.\nclass Event extends Named, Documented {\n"));
		assertTrue("The line break of the tooltip is collapsed.", english.contains(
			"  balance: tl.core:Double (derived) // Revenue minus total expenses."
				+ " Negative when the event costs more than it earns.\n"));

		// The security test bed's "pm" model documents nothing: no comment above or beside it.
		assertTrue(english, english.contains("\n\nclass Project extends tl.model:TLObject {\n"
			+ "  name: tl.core:String (mandatory)\n"));
		assertTrue(english, english.contains("  sessions: -> Session[] (composite) // Program of the event.\n"));

		assertTrue(german, german.contains("\n// Eine Veranstaltung, z. B. eine Konferenz, mit Sessions, Anmeldungen,"
			+ " Sponsoring und Rechnungen.\nclass Event extends Named, Documented {\n"));
		assertTrue(german, german.contains("  balance: tl.core:Double (derived) // Einnahmen abzüglich Ausgaben."
			+ " Negativ, wenn die Veranstaltung mehr kostet, als sie einbringt.\n"));
	}

	/**
	 * An enumeration with described values gets one value per line; a framework module stays structure
	 * only although its elements are documented.
	 */
	public void testDigestDescribesEnumValuesOfApplicationModulesOnly() {
		TLModel model = ModelService.getApplicationModel();
		String digest = ModelDigestGenerator.generate(model, true, ModelSecurity.UNCHECKED, Locale.ENGLISH);

		assertTrue(digest, digest.contains("// Lifecycle status of an event.\nenum EventStatus {\n"
			+ "  DRAFT // Being prepared, not announced yet.\n"
			+ "  PLANNED // Announced, registration not open yet.\n"));

		// tl.ai.execution documents the type and every value, and still gets the one-line form.
		assertTrue(digest, digest.contains("\n\nenum ExecutionStatus { RUNNING, COMPLETE, ERROR, CANCELLED, REJECTED }\n"));
		assertFalse(digest, digest.contains("Lifecycle state of an agent execution."));
	}

	/**
	 * Tests that the outlineModel tool returns the application model digest as plain text.
	 */
	public void testOutlineModelToolReturnsDigest() {
		ModelBrowserTool tool = new ModelBrowserTool();
		String overview = tool.outlineModel(Boolean.FALSE, ToolContext.unrestricted());

		assertNotNull("Overview should not be null", overview);
		assertTrue("Overview should contain the events module",
			overview.contains("## demo.events"));
		assertTrue("Overview should contain Event class",
			overview.contains("class Event"));
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
