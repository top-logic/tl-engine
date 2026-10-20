/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;

/**
 * Tests for {@link TaskTool} verifying JSON responses from all four tool methods and error handling
 * when tasks are not found or no store is bound.
 */
public class TestTaskTool extends TestCase {

	private TaskTool _tool;

	private TaskStore _store;

	private TaskStoreBinding.Binding _binding;

	@Override
	protected void setUp() {
		_tool = new TaskTool();
		_store = new TaskStore();
		_binding = TaskStoreBinding.bind(_store);
	}

	@Override
	protected void tearDown() {
		_binding.close();
	}

	// -- createTask --

	/**
	 * Verifies that {@link TaskTool#createTask} returns JSON containing the task ID, pending
	 * status, and subject.
	 */
	public void testCreateTaskReturnsJson() {
		String json = _tool.createTask("Build feature", "Implement the feature", "Building feature");

		assertContains(json, "\"taskId\":\"1\"");
		assertContains(json, "\"status\":\"pending\"");
		assertContains(json, "\"subject\":\"Build feature\"");
	}

	/**
	 * Verifies that consecutive createTask calls produce incrementing IDs in the JSON response.
	 */
	public void testCreateTaskIncrementsId() {
		String first = _tool.createTask("Task 1", "d", "f");
		String second = _tool.createTask("Task 2", "d", "f");

		assertContains(first, "\"taskId\":\"1\"");
		assertContains(second, "\"taskId\":\"2\"");
	}

	/**
	 * Verifies that createTask returns an error JSON when no store is bound.
	 */
	public void testCreateTaskNoStoreReturnsError() {
		_binding.close();

		String json = _tool.createTask("Task", "d", "f");
		assertContains(json, "\"error\":");
	}

	// -- updateTask --

	/**
	 * Verifies that {@link TaskTool#updateTask} returns JSON with the updated status.
	 */
	public void testUpdateTaskReturnsJson() {
		_tool.createTask("Task", "d", "f");

		String json = _tool.updateTask("1", "in_progress", null, null, null, null, null, null);

		assertContains(json, "\"taskId\":\"1\"");
		assertContains(json, "\"status\":\"in_progress\"");
		assertContains(json, "\"subject\":\"Task\"");
	}

	/**
	 * Verifies that updating a non-existent task returns an error JSON.
	 */
	public void testUpdateTaskNotFoundReturnsError() {
		String json = _tool.updateTask("999", "completed", null, null, null, null, null, null);

		assertContains(json, "\"error\":");
		assertContains(json, "999");
	}

	/**
	 * Verifies that updating subject changes the returned subject in JSON.
	 */
	public void testUpdateTaskSubjectReflectedInResponse() {
		_tool.createTask("Old Subject", "d", "f");

		String json = _tool.updateTask("1", null, "New Subject", null, null, null, null, null);

		assertContains(json, "\"subject\":\"New Subject\"");
	}

	/**
	 * Verifies that updateTask returns error JSON when no store is bound.
	 */
	public void testUpdateTaskNoStoreReturnsError() {
		_binding.close();

		String json = _tool.updateTask("1", "completed", null, null, null, null, null, null);
		assertContains(json, "\"error\":");
	}

	// -- listTasks --

	/**
	 * Verifies that {@link TaskTool#listTasks()} returns JSON with an empty tasks array when no
	 * tasks exist.
	 */
	public void testListTasksEmpty() {
		String json = _tool.listTasks();

		assertContains(json, "\"tasks\":");
	}

	/**
	 * Verifies that listTasks returns summary information for all created tasks.
	 */
	public void testListTasksReturnsSummaries() {
		_tool.createTask("Task A", "desc A", "Doing A");
		_tool.createTask("Task B", "desc B", "Doing B");

		String json = _tool.listTasks();

		assertContains(json, "\"id\":\"1\"");
		assertContains(json, "\"id\":\"2\"");
		assertContains(json, "\"subject\":\"Task A\"");
		assertContains(json, "\"subject\":\"Task B\"");
		assertContains(json, "\"status\":\"pending\"");
	}

	/**
	 * Verifies that listTasks includes blockedBy information in task summaries.
	 */
	public void testListTasksIncludesBlockedBy() {
		_tool.createTask("Blocker", "d", "f");
		_tool.createTask("Dependent", "d", "f");
		_store.updateTask("2", null, null, null, null, null, null, Arrays.asList("1"));

		String json = _tool.listTasks();

		// The dependent task should show blockedBy
		assertContains(json, "\"blockedBy\":");
	}

	/**
	 * Verifies that listTasks includes owner information.
	 */
	public void testListTasksIncludesOwner() {
		_tool.createTask("Task", "d", "f");
		_store.updateTask("1", null, null, null, null, "agent-1", null, null);

		String json = _tool.listTasks();

		assertContains(json, "\"owner\":\"agent-1\"");
	}

	/**
	 * Verifies that listTasks returns error JSON when no store is bound.
	 */
	public void testListTasksNoStoreReturnsError() {
		_binding.close();

		String json = _tool.listTasks();
		assertContains(json, "\"error\":");
	}

	// -- Integration scenario --

	/**
	 * Verifies a full lifecycle: create task, update to in_progress, complete it, verify via
	 * listTasks.
	 */
	public void testFullLifecycle() {
		// Create
		String createJson = _tool.createTask("Deploy app", "Deploy to staging", "Deploying app");
		assertContains(createJson, "\"taskId\":\"1\"");

		// Start
		String startJson = _tool.updateTask("1", "in_progress", null, null, null, "bot", null, null);
		assertContains(startJson, "\"status\":\"in_progress\"");

		// Complete
		String completeJson = _tool.updateTask("1", "completed", null, null, null, null, null, null);
		assertContains(completeJson, "\"status\":\"completed\"");

		// Verify via listTasks
		String listJson = _tool.listTasks();
		assertContains(listJson, "\"status\":\"completed\"");
		assertContains(listJson, "\"owner\":\"bot\"");
	}

	/**
	 * Verifies that a deleted task no longer appears in listTasks.
	 */
	public void testDeletedTaskDisappearsFromToolResponses() {
		_tool.createTask("Temporary", "d", "f");
		_tool.createTask("Permanent", "d", "f");

		_tool.updateTask("1", "deleted", null, null, null, null, null, null);

		// listTasks should only show task 2
		String listJson = _tool.listTasks();
		assertFalse("Deleted task should not appear in list", listJson.contains("\"id\":\"1\""));
		assertContains(listJson, "\"id\":\"2\"");
	}

	/**
	 * Asserts that the given string contains the expected substring.
	 */
	private static void assertContains(String actual, String expectedSubstring) {
		assertNotNull("Response should not be null", actual);
		assertTrue(
			"Expected JSON to contain '" + expectedSubstring + "' but was: " + actual,
			actual.contains(expectedSubstring));
	}
}
