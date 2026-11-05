/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.bpml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.bpe.bpml.BPMLDefinitions;
import com.top_logic.bpe.bpml.BPMLFunctions;
import com.top_logic.bpe.bpml.model.ApprovalState;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.ManualTask;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.ProcessExecutions;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Test for {@link BPMLDefinitions} and the TL-Script functions of {@link BPMLFunctions}.
 */
@SuppressWarnings("javadoc")
public class TestBPMLDefinitions extends BasicTestCase {

	private static final String FIXTURE = "fixtures/diagram-gateways.bpmn.xml";

	private static final String TASK_NAME = "Create";

	public void testCreate() {
		Collaboration collaboration = inTx(() -> BPMLDefinitions.createCollaboration("New workflow"));

		assertEquals("New workflow", collaboration.getName());
		assertEquals(ApprovalState.developmentClassifier(), collaboration.getState());
		assertFalse(collaboration.getParticipants().isEmpty());
		for (Participant participant : collaboration.getParticipants()) {
			assertEquals("New workflow", participant.getName());
		}
		assertNotNull(findNode(collaboration, StartEvent.class, null));
		assertNotNull(findNode(collaboration, ManualTask.class, TASK_NAME));
	}

	public void testImportExportRoundTrip() throws IOException {
		BinaryData fixture = BinaryDataFactory.createBinaryData(TestBPMLDefinitions.class, FIXTURE);
		Collaboration imported = inTx(() -> BPMLDefinitions.importCollaboration(fixture));
		assertFalse(nodesById(imported).isEmpty());

		BinaryData exported = BPMLDefinitions.export(imported, true);
		assertEquals(BPMLDefinitions.exportFileName(imported), exported.getName());
		assertEquals(BPMLDefinitions.EXPORT_CONTENT_TYPE, exported.getContentType());

		Collaboration reimported = inTx(() -> BPMLDefinitions.importCollaboration(exported));
		assertNotSame(imported, reimported);
		assertEquals(imported.getName(), reimported.getName());
		assertEquals(nodesById(imported), nodesById(reimported));

		assertEquals(xml(imported), xml(reimported));
	}

	public void testImportInvalid() {
		BinaryData garbage =
			BinaryDataFactory.createBinaryData("<no-bpmn/>".getBytes(), "text/xml", "garbage.bpmn");
		try {
			inTx(() -> BPMLDefinitions.importCollaboration(garbage));
			fail("Invalid BPMN must not be imported.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	public void testUpdateFromDiagramKeepsIdentity() throws IOException {
		Collaboration collaboration = inTx(() -> BPMLDefinitions.createCollaboration("Update"));
		ManualTask task = findNode(collaboration, ManualTask.class, TASK_NAME);
		assertNotNull(task);

		String xml = xml(collaboration);
		String renamed = xml.replace("name=\"" + TASK_NAME + "\"", "name=\"Renamed\"");
		assertFalse(xml.equals(renamed));

		inTx(() -> {
			BPMLDefinitions.updateFromDiagram(collaboration, renamed);
			return null;
		});

		assertTrue(task.tValid());
		assertEquals("Renamed", task.getName());
		assertSame(task, findNode(collaboration, ManualTask.class, "Renamed"));
		assertNull(findNode(collaboration, ManualTask.class, TASK_NAME));
	}

	public void testUpdateFromFile() throws IOException {
		Collaboration collaboration = inTx(() -> BPMLDefinitions.createCollaboration("Update file"));
		ManualTask task = findNode(collaboration, ManualTask.class, TASK_NAME);

		String renamed = xml(collaboration).replace("name=\"" + TASK_NAME + "\"", "name=\"From file\"");
		BinaryData data =
			BinaryDataFactory.createBinaryData(renamed.getBytes(StandardCharsets.UTF_8), "text/xml", "x.bpmn");
		inTx(() -> {
			BPMLDefinitions.updateCollaboration(collaboration, data, false);
			return null;
		});

		assertSame(task, findNode(collaboration, ManualTask.class, "From file"));
	}

	public void testFixDiagramNamespaces() {
		String declaration = "xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"";
		String broken = "<bpmn:definitions " + declaration + "><bpmndi:BPMNDiagram/></bpmn:definitions>";
		String fixed = BPMLDefinitions.fixDiagramNamespaces(broken);
		assertTrue(fixed.contains("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\""));
		assertTrue(fixed.contains("xmlns:dc="));
		assertTrue(fixed.contains("xmlns:di="));

		assertEquals(fixed, BPMLDefinitions.fixDiagramNamespaces(fixed));
	}

	public void testCopy() {
		Collaboration orig = inTx(() -> {
			Collaboration result = BPMLDefinitions.createCollaboration("Original");
			BPMLDefinitions.release(result);
			return result;
		});

		Collaboration copy = inTx(() -> BPMLDefinitions.copyCollaboration(orig, "Copy"));
		assertNotSame(orig, copy);
		assertEquals("Copy", copy.getName());
		assertEquals("Original", orig.getName());
		assertEquals(ApprovalState.developmentClassifier(), copy.getState());
		assertEquals(ApprovalState.releasedClassifier(), orig.getState());

		ManualTask origTask = findNode(orig, ManualTask.class, TASK_NAME);
		ManualTask copyTask = findNode(copy, ManualTask.class, TASK_NAME);
		assertNotNull(copyTask);
		assertNotSame(origTask, copyTask);
		assertEquals(origTask.getExtId(), copyTask.getExtId());

		inTx(() -> {
			copyTask.setName("Changed in copy");
			return null;
		});
		assertEquals(TASK_NAME, origTask.getName());
	}

	public void testReleaseRevoke() {
		Collaboration collaboration = inTx(() -> BPMLDefinitions.createCollaboration("States"));
		assertTrue(BPMLDefinitions.canRelease(collaboration));
		assertFalse(BPMLDefinitions.canRevoke(collaboration));
		assertRefused(() -> BPMLDefinitions.revoke(collaboration));

		inTx(() -> {
			BPMLDefinitions.release(collaboration);
			return null;
		});
		assertEquals(ApprovalState.releasedClassifier(), collaboration.getState());
		assertFalse(BPMLDefinitions.canRelease(collaboration));
		assertRefused(() -> BPMLDefinitions.release(collaboration));

		inTx(() -> {
			BPMLDefinitions.revoke(collaboration);
			return null;
		});
		assertEquals(ApprovalState.retiredClassifier(), collaboration.getState());
		assertTrue(BPMLDefinitions.canRelease(collaboration));
		assertRefused(() -> BPMLDefinitions.revoke(collaboration));

		inTx(() -> {
			BPMLDefinitions.release(collaboration);
			return null;
		});
		assertEquals(ApprovalState.releasedClassifier(), collaboration.getState());
	}

	public void testCanDelete() throws ConfigurationException {
		Collaboration collaboration = inTx(() -> {
			Collaboration result = BPMLDefinitions.createCollaboration("Delete");
			BPMLDefinitions.release(result);
			return result;
		});
		QueryExecutor canDelete = compile("w -> bpmlCanDelete($w)");
		assertTrue("A workflow without processes may be deleted.", BPMLDefinitions.canDelete(collaboration));
		assertEquals(Boolean.TRUE, canDelete.execute(collaboration));

		StartEvent start = findNode(collaboration, StartEvent.class, null);
		ProcessExecution execution = inTx(() -> {
			ProcessExecution result = TlBpeExecutionFactory.getInstance().createProcessExecution();
			ProcessExecutions.initialize(result, start);
			return result;
		});
		assertTrue(BPMLDefinitions.isInUse(collaboration));
		assertFalse("A workflow with a started process is revoked, not deleted.",
			BPMLDefinitions.canDelete(collaboration));
		assertEquals(Boolean.FALSE, canDelete.execute(collaboration));

		inTx(() -> {
			execution.setCollaboration(null);
			return null;
		});
		assertFalse("The process of the execution refers to the workflow as well.",
			BPMLDefinitions.canDelete(collaboration));

		inTx(() -> {
			execution.tDelete();
			return null;
		});
		assertTrue(BPMLDefinitions.canDelete(collaboration));
	}

	public void testScriptFunctions() throws ConfigurationException {
		QueryExecutor create = compile("bpmlCreate('Scripted')");
		QueryExecutor canRelease = compile("w -> bpmlCanRelease($w)");
		QueryExecutor canRevoke = compile("w -> bpmlCanRevoke($w)");
		QueryExecutor release = compile("w -> bpmlRelease($w)");
		QueryExecutor revoke = compile("w -> bpmlRevoke($w)");
		QueryExecutor export = compile("w -> bpmlExport($w)");
		QueryExecutor copyFn = compile("w -> bpmlCopy($w, 'Scripted copy')");
		QueryExecutor importFn = compile("d -> bpmlImport($d)");

		Collaboration created = (Collaboration) inTx(() -> create.execute());
		assertEquals("Scripted", created.getName());

		assertEquals(Boolean.TRUE, canRelease.execute(created));
		assertEquals(Boolean.FALSE, canRevoke.execute(created));
		inTx(() -> release.execute(created));
		assertEquals(ApprovalState.releasedClassifier(), created.getState());

		BinaryData exported = (BinaryData) export.execute(created);
		assertEquals("Scripted" + BPMLDefinitions.EXPORT_FILE_SUFFIX, exported.getName());

		Collaboration copy = (Collaboration) inTx(() -> copyFn.execute(created));
		assertEquals("Scripted copy", copy.getName());
		assertEquals(ApprovalState.developmentClassifier(), copy.getState());

		Collaboration imported = (Collaboration) inTx(() -> importFn.execute(exported));
		assertEquals("Scripted", imported.getName());
		assertNotSame(created, imported);

		inTx(() -> revoke.execute(created));
		assertEquals(ApprovalState.retiredClassifier(), created.getState());
	}

	private static QueryExecutor compile(String script) throws ConfigurationException {
		return QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
	}

	private static void assertRefused(Runnable transition) {
		try {
			inTx(() -> {
				transition.run();
				return null;
			});
			fail("Transition must be refused.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static String xml(Collaboration collaboration) throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			BPMLDefinitions.exportXml(collaboration, buffer, false);
			return buffer.toString(StringServices.UTF8);
		}
	}

	private static Map<String, String> nodesById(Collaboration collaboration) {
		Map<String, String> result = new HashMap<>();
		for (Process process : collaboration.getProcesses()) {
			for (Node node : process.getNodes()) {
				result.put(node.getExtId(), node.tType().getName() + ":" + node.getName());
			}
		}
		return result;
	}

	private static <T extends Node> T findNode(Collaboration collaboration, Class<T> type, String name) {
		for (Process process : collaboration.getProcesses()) {
			for (Node node : process.getNodes()) {
				if (type.isInstance(node) && (name == null || name.equals(node.getName()))) {
					return type.cast(node);
				}
			}
		}
		return null;
	}

	private static <T> T inTx(Supplier<T> operation) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			T result = operation.get();
			tx.commit();
			return result;
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestBPMLDefinitions.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				ExecutionEngine.Module.INSTANCE));
	}

}
