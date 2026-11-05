/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.execution.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.bpe.BPEUtil;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.TlBpeBpmlFactory;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.InitialProcessSetup;
import com.top_logic.bpe.execution.engine.InitialProcessSetup.Resource;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Test for {@link InitialProcessSetup#importWorkflows(List)}: a workflow file that cannot be
 * imported leaves no trace, while the other files are imported.
 *
 * <p>
 * The workflow of a file is named after the file, since the fixture names no collaboration. The
 * broken version of the fixture refers to a model type that does not exist, which the importer
 * reports after it has created the collaboration of the workflow; the number of all workflows shows
 * that the failed import keeps none of it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestInitialProcessSetup extends BasicTestCase {

	private static final String FIXTURE = "fixtures/task-decision.bpmn.xml";

	private static final String FILE_SUFFIX = ".bpmn.xml";

	private static final String REVIEW = "Review";

	private static final String CHECK = "Check";

	private static int _fileCount;

	public void testInitialImport() throws IOException, SQLException {
		String good = newName("good");
		String broken = newName("broken");
		int before = allWorkflows().size();

		InitialProcessSetup.importWorkflows(List.of(resource(good, fixture()), resource(broken, broken())));

		assertEquals("Only the good workflow is created.", before + 1, allWorkflows().size());
		assertEquals(1, workflows(good).size());
		assertEquals(Set.of(), nodeNames(broken));
		assertTrue(workflows(broken).isEmpty());
		assertNotNull(InitialProcessSetup.importState(resource(good, fixture())));
		assertNull(InitialProcessSetup.importState(resource(broken, broken())));

		// The broken file is tried again; once repaired, it is imported.
		InitialProcessSetup.importWorkflows(List.of(resource(good, fixture()), resource(broken, fixture())));
		assertEquals(1, workflows(good).size());
		assertEquals(1, workflows(broken).size());
	}

	public void testUpdate() throws IOException, SQLException {
		String good = newName("good");
		String broken = newName("broken");
		InitialProcessSetup.importWorkflows(List.of(resource(good, fixture()), resource(broken, fixture())));
		String goodState = InitialProcessSetup.importState(resource(good, fixture()));
		String brokenState = InitialProcessSetup.importState(resource(broken, fixture()));
		assertNotNull(goodState);
		assertNotNull(brokenState);

		int before = allWorkflows().size();
		String renamed = fixture().replace("name=\"" + REVIEW + "\"", "name=\"" + CHECK + "\"");
		InitialProcessSetup.importWorkflows(List.of(resource(good, renamed), resource(broken, broken())));

		assertEquals(1, workflows(good).size());
		assertTrue(nodeNames(good).contains(CHECK));
		assertFalse(nodeNames(good).contains(REVIEW));
		assertFalse(goodState.equals(InitialProcessSetup.importState(resource(good, renamed))));

		assertEquals(1, workflows(broken).size());
		assertTrue(nodeNames(broken).contains(REVIEW));
		assertEquals(brokenState, InitialProcessSetup.importState(resource(broken, broken())));
		assertEquals("No workflow is left from the failed update.", before, allWorkflows().size());
	}

	private static String newName(String prefix) {
		return prefix + "-" + (++_fileCount);
	}

	private static Resource resource(String name, String content) {
		BinaryData data = BinaryDataFactory.createBinaryData(content.getBytes(StandardCharsets.UTF_8),
			BinaryData.CONTENT_TYPE_OCTET_STREAM, name + FILE_SUFFIX);
		return Resource.create(name + FILE_SUFFIX, data);
	}

	private static String fixture() throws IOException {
		BinaryData data = BinaryDataFactory.createBinaryData(TestInitialProcessSetup.class, FIXTURE);
		return StreamUtilities.readAllFromStream(data.getStream(), StandardCharsets.UTF_8);
	}

	private static String broken() throws IOException {
		String fixture = fixture();
		String brokenDefinitions = fixture.replace("<bpmn:definitions ",
			"<bpmn:definitions xmlns:tl=\"http://www.top-logic.com/ns/BPMN/extensions/7.0\" ");
		String result = brokenDefinitions.replace("processRef=\"Process_TaskDecision\" />",
			"processRef=\"Process_TaskDecision\"><bpmn:extensionElements>"
				+ "<tl:modelType value=\"no.such.module:NoSuchType\"/>"
				+ "</bpmn:extensionElements></bpmn:participant>");
		assertFalse("Fixture not modified.", result.equals(fixture));
		return result;
	}

	private static List<Collaboration> allWorkflows() {
		return MetaElementUtil.getAllInstancesOf(TlBpeBpmlFactory.getCollaborationType(), Collaboration.class);
	}

	private static List<Collaboration> workflows(String name) {
		return BPEUtil.collaborationsByName().getOrDefault(name, List.of());
	}

	private static Set<String> nodeNames(String workflow) {
		return workflows(workflow).stream()
			.flatMap(c -> c.getProcesses().stream())
			.flatMap(p -> p.getNodes().stream())
			.map(Node::getName)
			.collect(Collectors.toSet());
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInitialProcessSetup.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				ExecutionEngine.Module.INSTANCE));
	}

}
