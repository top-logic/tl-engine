/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tl.ai.core.screenshots.model.TlAiScreenshotsFactory;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link NameBasedInstanceResolver}.
 */
public class TestNameBasedInstanceResolver extends BasicTestCase {

	private static final String AGENT_KIND = "tl.ai.core:Agent";

	/**
	 * A type whose {@code name} carries no unique index, so duplicate names are actually reachable.
	 *
	 * @see #testResolveDeletesAllDuplicates()
	 */
	private static final String SCREENSHOT_KIND = "tl.ai.screenshots:Screenshot";

	/**
	 * Reproduces the re-import failure: the resolver is invoked once per imported object within a
	 * single transaction. After the first call deletes a matching instance, a later call must not
	 * trip over that already-deleted instance while scanning all instances of the type.
	 *
	 * <p>
	 * Before the fix, the second {@link NameBasedInstanceResolver#resolve} call threw
	 * {@code DeletedObjectAccess} when reading the {@code name} of the instance deleted by the
	 * first call.
	 * </p>
	 */
	public void testResolveSkipsInstancesDeletedByEarlierCall() throws Exception {
		TlAiCoreFactory factory = TlAiCoreFactory.getInstance();

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			createAgent(factory, "A");
			createAgent(factory, "B");
			tx.commit();
		}

		NameBasedInstanceResolver resolver = new NameBasedInstanceResolver();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			// Importing object "A": its pre-existing instance is deleted, null signals "create new".
			assertNull(resolver.resolve(null, null, AGENT_KIND, "A"));
			// Importing object "B": the scan must skip the now-deleted "A" instead of reading it.
			assertNull(resolver.resolve(null, null, AGENT_KIND, "B"));
			tx.commit();
		}

		assertNull("Existing agent 'A' must have been deleted by the resolver.", findAgent("A"));
		assertNull("Existing agent 'B' must have been deleted by the resolver.", findAgent("B"));
	}

	/**
	 * The resolver must delete <em>every</em> instance carrying the resolved name, not just the
	 * first one it finds: a re-import would otherwise leave duplicates behind and let them compound
	 * (issue #167, root cause E).
	 *
	 * <p>
	 * The resolver is generic - {@code ExportToDataFolderCommand.Config#getTypeName()} picks the
	 * type it is applied to - so this contract matters for every type without a persistence-level
	 * uniqueness guard. {@code Agent} can no longer serve as the vehicle: issue #172 added a unique
	 * index on the agent name, so two same-name agents cannot be committed at all (that guard is
	 * covered by {@link TestUniqueAgentSkillName}). {@code Screenshot} has no such index and is used
	 * here instead.
	 * </p>
	 */
	public void testResolveDeletesAllDuplicates() throws Exception {
		TlAiScreenshotsFactory factory = TlAiScreenshotsFactory.getInstance();

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			createScreenshot(factory, "Dup");
			createScreenshot(factory, "Dup");
			tx.commit();
		}
		assertEquals("Precondition: the store must actually hold two same-name instances.",
			2, countScreenshots("Dup"));

		NameBasedInstanceResolver resolver = new NameBasedInstanceResolver();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			assertNull(resolver.resolve(null, null, SCREENSHOT_KIND, "Dup"));
			tx.commit();
		}

		assertEquals("Regression target: the resolver must clean up ALL duplicates, not just the "
			+ "first.", 0, countScreenshots("Dup"));
	}

	private static Screenshot createScreenshot(TlAiScreenshotsFactory factory, String name) {
		Screenshot screenshot = factory.createScreenshot();
		screenshot.setName(name);
		screenshot.setImage(BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 }, "image/png", name));
		return screenshot;
	}

	private static int countScreenshots(String name) {
		int count = 0;
		for (TLObject instance : MetaElementUtil
			.getAllInstancesOf(TlAiScreenshotsFactory.getScreenshotType(), TLObject.class)) {
			if (instance.tValid() && name.equals(instance.tValueByName(Screenshot.NAME_ATTR))) {
				count++;
			}
		}
		return count;
	}

	private static Agent createAgent(TlAiCoreFactory factory, String name) {
		Agent agent = factory.createAgent();
		agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), name);
		return agent;
	}

	private static TLObject findAgent(String name) {
		TLClass type = TlAiCoreFactory.getAgentType();
		List<TLObject> instances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);
		for (TLObject instance : instances) {
			if (name.equals(instance.tValueByName(Agent.NAME_ATTR))) {
				return instance;
			}
		}
		return null;
	}

	/**
	 * Creates the test suite with the required service dependencies (KB + {@link ModelService}).
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestNameBasedInstanceResolver.class),
				ModelService.Module.INSTANCE));
	}
}
