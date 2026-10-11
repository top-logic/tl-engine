/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.server;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.ai.a2a.protocol.A2AJson;
import com.top_logic.ai.a2a.protocol.Json;
import com.top_logic.ai.a2a.protocol.TaskState;

/**
 * Test case for {@link A2ATask#restored}: a task read back after a restart.
 */
public class TestA2ATaskRestore extends TestCase {

	/** The task as it comes back from its persisted JSON. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> reread(A2ATask task) {
		return (Map<String, Object>) Json.parse(Json.write(task.toJson(null)));
	}

	public void testAFinishedTaskComesBackAsItWas() {
		A2ATask task = new A2ATask("t1", "c1", "alpha", "root");
		task.addHistory(A2AJson.textMessage(A2AJson.ROLE_USER, null, null, "Bau es"));
		task.moveTo(TaskState.WORKING, null);
		task.addArtifact(A2AJson.artifact("answer", null, List.of(A2AJson.textPart("Gebaut"))));
		task.moveTo(TaskState.COMPLETED, A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Gebaut"));
		task.putMetadata("runId", "r1");

		A2ATask restored = A2ATask.restored(reread(task), "alpha", "root");

		assertEquals(TaskState.COMPLETED, restored.state());
		assertEquals(task.toJson(null), restored.toJson(null));
		assertEquals("Gebaut", A2AJson.plainText(restored.artifact("answer")));
	}

	public void testATaskThatWasRunningReadsAsFailedAndSaysWhatToDo() {
		A2ATask task = new A2ATask("t2", "c1", "alpha", "root");
		task.moveTo(TaskState.WORKING, null);
		task.requestInput(A2AJson.textMessage(A2AJson.ROLE_AGENT, null, null, "Welcher Standort?"), answer -> {
			// The run is gone after the restart.
		});

		A2ATask restored = A2ATask.restored(reread(task), "alpha", "root");

		assertEquals(TaskState.FAILED, restored.state());
		assertTrue(A2AJson.plainText(Json.obj(Json.obj(restored.toJson(null), "status"), "message")).contains("restarted"));
		assertFalse("Nothing waits for an answer any more.",
			restored.answer(A2AJson.textMessage(A2AJson.ROLE_USER, null, null, "Werk Nord")));
		List<?> history = Json.list(restored.toJson(null), "history");
		assertEquals("The question stays in the history.", 1, history.size());
	}
}
