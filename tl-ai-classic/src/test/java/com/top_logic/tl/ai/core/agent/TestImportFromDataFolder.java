/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.util.model.ModelService;

/**
 * Reproduction and regression tests for the "Import from development environment" flow of the
 * agent editor (issue #167).
 *
 * <p>
 * These tests drive the {@link XMLInstanceExporter} -&gt; {@link XMLInstanceImporter} round-trip
 * used by {@link ExportToDataFolderCommand} / {@link ImportFromDataFolderCommand} without a running
 * UI, matching the behaviour a user triggers via the editor buttons.
 * </p>
 *
 * <p>
 * Root cause E ("duplicates compound") is no longer reproducible on {@code Agent}: issue #172 added
 * a persistence-level unique index on the agent name, so the two same-name agents this suite used
 * to create can no longer be committed at all. That guard is covered by the test of unique agent
 * and skill names in tl-ai-core; the resolver contract it used to test - delete <em>every</em>
 * match, not just the first - is covered there by the test of the name-based instance resolver on
 * a type that still permits duplicate names.
 * </p>
 */
public class TestImportFromDataFolder extends BasicTestCase {

	private static final String AGENT_KIND = "tl.ai.core:Agent";

	/**
	 * A normal export/import round-trip restores the committed values and discards runtime edits.
	 */
	public void testRoundTripRestoresCommittedValues() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();
		try (Transaction tx = kb().beginTransaction()) {
			createAgent(factory, "Alpha", "committed description");
			tx.commit();
		}

		BinaryData exported = exportAgents();

		try (Transaction tx = kb().beginTransaction()) {
			findAgent("Alpha").tUpdate(descriptionAttr(), "runtime edit");
			tx.commit();
		}

		try (Transaction tx = kb().beginTransaction()) {
			importAgents(exported);
			tx.commit();
		}

		TLObject reimported = findAgent("Alpha");
		assertNotNull("Agent must exist after re-import.", reimported);
		assertEquals("Runtime edit must be discarded, committed value restored.",
			"committed description", reimported.tValueByName(Agent.DESCRIPTION_ATTR));
	}

	/**
	 * Reproduces the in-dev flake (root cause C): the importer deletes and recreates every matched
	 * instance, so an object held by the editor (the current selection) becomes a deleted object
	 * after import. Rendering such a selection is what makes the import look broken.
	 */
	public void testSelectedInstanceIsDeletedByReimport() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();
		TLObject held;
		try (Transaction tx = kb().beginTransaction()) {
			held = createAgent(factory, "Beta", "d");
			tx.commit();
		}

		BinaryData exported = exportAgents();

		try (Transaction tx = kb().beginTransaction()) {
			importAgents(exported);
			tx.commit();
		}

		assertFalse("Re-import deletes and recreates the instance; the previously selected "
			+ "object is now invalid - the editor must not keep it selected.", held.tValid());
		assertNotNull("A fresh instance with the same name must exist.", findAgent("Beta"));
	}

	/**
	 * Regression for root cause F: a per-object failure during import must be observable, so the
	 * command can warn instead of reporting an unconditional success.
	 */
	public void testPartialImportIsDetected() throws Exception {
		ObjectsConf objects = TypedConfiguration.newConfigItem(ObjectsConf.class);

		// No globalId is set, so the importer creates the objects directly without consulting a
		// resolver: the valid object imports cleanly, the second one trips over an unknown attribute.
		ObjectConf good = TypedConfiguration.newConfigItem(ObjectConf.class);
		good.setId("good");
		good.setType(AGENT_KIND);
		addAttribute(good, "name", "Good");

		ObjectConf broken = TypedConfiguration.newConfigItem(ObjectConf.class);
		broken.setId("broken");
		broken.setType(AGENT_KIND);
		addAttribute(broken, "name", "Broken");
		addAttribute(broken, "noSuchAttribute", "boom");

		objects.getObjects().add(good);
		objects.getObjects().add(broken);

		ModelService ms = ModelService.getInstance();
		ImportFromDataFolderCommand.ImportOutcome outcome;
		try (Transaction tx = kb().beginTransaction()) {
			outcome = ImportFromDataFolderCommand.runImport(objects, ms.getModel(), ms.getFactory());
			tx.commit();
		}

		assertTrue("A failing per-object attribute must be reported as an import problem.",
			outcome.hasProblems());
		assertEquals("Both top-level objects are processed.", 2, outcome.totalCount());
		assertNotNull("The valid object must still be imported despite the sibling's problem.",
			findAgent("Good"));
	}

	private static void addAttribute(ObjectConf obj, String name, String value) {
		AttributeValueConf attr = TypedConfiguration.newConfigItem(AttributeValueConf.class);
		attr.setName(name);
		attr.setValue(value);
		obj.getAttributes().add(attr);
	}

	private static BinaryData exportAgents() throws Exception {
		TLClass type = TlAiCoreFactory.getAgentType();
		List<TLObject> instances = new ArrayList<>(MetaElementUtil.getAllInstancesOf(type, TLObject.class));
		XMLInstanceExporter exporter = new XMLInstanceExporter();
		exporter.addResolver(type, new NameBasedInstanceResolver());
		BinaryDataSource data = exporter.exportInstances("agents.objects.xml", instances);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		data.deliverTo(buffer);
		return BinaryDataFactory.createBinaryData(buffer.toByteArray());
	}

	private static void importAgents(BinaryData data) throws Exception {
		ObjectsConf objects = XMLInstanceImporter.loadConfig(data);
		ModelService ms = ModelService.getInstance();
		XMLInstanceImporter importer = new XMLInstanceImporter(ms.getModel(), ms.getFactory());
		importer.importInstances(objects);
	}

	private static Agent createAgent(TlAiCoreFactory factory, String name, String description) {
		Agent agent = factory.createAgent();
		agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
		agent.tUpdate(descriptionAttr(), description);
		return agent;
	}

	private static com.top_logic.model.TLStructuredTypePart descriptionAttr() {
		return TlAiCoreFactory.getDescriptionAgentAttr();
	}

	private static TLObject findAgent(String name) {
		for (TLObject instance : allAgents()) {
			if (name.equals(instance.tValueByName(Agent.NAME_ATTR))) {
				return instance;
			}
		}
		return null;
	}

	private static List<TLObject> allAgents() {
		return MetaElementUtil.getAllInstancesOf(TlAiCoreFactory.getAgentType(), TLObject.class);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Creates the test suite with the required service dependencies (KB + {@link ModelService}).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestImportFromDataFolder.class),
				ModelService.Module.INSTANCE));
	}
}
