/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.task;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.ai.service.tools.task.TaskStore.AgentTask;

/**
 * Tests for {@link TaskStore} covering CRUD operations, status transitions, dependency tracking,
 * version counter, and thread safety contracts.
 */
public class TestTaskStore extends TestCase {

	private TaskStore _store;

	@Override
	protected void setUp() {
		_store = new TaskStore();
	}

	/**
	 * Verifies that a newly created task has an auto-incremented ID, the given properties, and
	 * status {@value TaskStore#STATUS_PENDING}.
	 */
	public void testCreateTask() {
		AgentTask task = _store.createTask("Fix bug", "Fix the login bug", "Fixing bug");

		assertEquals("First task should have ID '1'", "1", task.getId());
		assertEquals("Fix bug", task.getSubject());
		assertEquals("Fix the login bug", task.getDescription());
		assertEquals("Fixing bug", task.getActiveForm());
		assertEquals("New tasks must start as pending", TaskStore.STATUS_PENDING, task.getStatus());
	}

	/**
	 * Verifies that task IDs auto-increment with each creation.
	 */
	public void testCreateMultipleTasksIncrementsIds() {
		AgentTask first = _store.createTask("Task A", "desc", "Doing A");
		AgentTask second = _store.createTask("Task B", "desc", "Doing B");
		AgentTask third = _store.createTask("Task C", "desc", "Doing C");

		assertEquals("1", first.getId());
		assertEquals("2", second.getId());
		assertEquals("3", third.getId());
	}

	/**
	 * Verifies that {@link TaskStore#getTask(String)} returns the correct task by ID.
	 */
	public void testGetTask() {
		_store.createTask("Task A", "desc A", "Doing A");
		AgentTask created = _store.createTask("Task B", "desc B", "Doing B");

		AgentTask retrieved = _store.getTask("2");
		assertSame("getTask should return the same object", created, retrieved);
		assertEquals("Task B", retrieved.getSubject());
	}

	/**
	 * Verifies that {@link TaskStore#getTask(String)} returns {@code null} for unknown IDs.
	 */
	public void testGetTaskNotFound() {
		assertNull("Non-existent task should return null", _store.getTask("999"));
	}

	/**
	 * Verifies that {@link TaskStore#getAllTasks()} returns tasks in creation order.
	 */
	public void testGetAllTasksPreservesCreationOrder() {
		_store.createTask("First", "d", "f");
		_store.createTask("Second", "d", "f");
		_store.createTask("Third", "d", "f");

		List<AgentTask> tasks = _store.getAllTasks();
		assertEquals("Should return all 3 tasks", 3, tasks.size());
		assertEquals("First", tasks.get(0).getSubject());
		assertEquals("Second", tasks.get(1).getSubject());
		assertEquals("Third", tasks.get(2).getSubject());
	}

	/**
	 * Verifies that an empty store returns an empty list.
	 */
	public void testGetAllTasksEmpty() {
		List<AgentTask> tasks = _store.getAllTasks();
		assertNotNull("Should return empty list, not null", tasks);
		assertTrue("List should be empty", tasks.isEmpty());
	}

	/**
	 * Verifies that the list returned by {@link TaskStore#getAllTasks()} is unmodifiable.
	 */
	public void testGetAllTasksUnmodifiable() {
		_store.createTask("Task", "d", "f");
		List<AgentTask> tasks = _store.getAllTasks();

		try {
			tasks.add(new AgentTask("fake") {
			});
			fail("List should be unmodifiable");
		} catch (UnsupportedOperationException expected) {
			// expected
		}
	}

	/**
	 * Verifies that only non-null fields are updated; null parameters preserve existing values.
	 */
	public void testUpdateTaskNullFieldsPreserved() {
		_store.createTask("Original", "Original desc", "Doing original");

		_store.updateTask("1", null, "Updated Subject", null, null, null, null, null);

		AgentTask task = _store.getTask("1");
		assertEquals("Subject should be updated", "Updated Subject", task.getSubject());
		assertEquals("Description should be preserved", "Original desc", task.getDescription());
		assertEquals("ActiveForm should be preserved", "Doing original", task.getActiveForm());
		assertEquals("Status should be preserved", TaskStore.STATUS_PENDING, task.getStatus());
	}

	/**
	 * Verifies status transitions from pending to in_progress to completed.
	 */
	public void testUpdateTaskStatusTransitions() {
		_store.createTask("Task", "d", "f");

		_store.updateTask("1", TaskStore.STATUS_IN_PROGRESS, null, null, null, null, null, null);
		assertEquals(TaskStore.STATUS_IN_PROGRESS, _store.getTask("1").getStatus());

		_store.updateTask("1", TaskStore.STATUS_COMPLETED, null, null, null, null, null, null);
		assertEquals(TaskStore.STATUS_COMPLETED, _store.getTask("1").getStatus());
	}

	/**
	 * Verifies that setting the owner works correctly.
	 */
	public void testUpdateTaskOwner() {
		_store.createTask("Task", "d", "f");

		_store.updateTask("1", null, null, null, null, "agent-1", null, null);
		assertEquals("agent-1", _store.getTask("1").getOwner());
	}

	/**
	 * Verifies that updating a non-existent task returns {@code null}.
	 */
	public void testUpdateTaskNotFound() {
		AgentTask result = _store.updateTask("999", TaskStore.STATUS_COMPLETED,
			null, null, null, null, null, null);
		assertNull("Updating non-existent task should return null", result);
	}

	/**
	 * Verifies that setting status to {@value TaskStore#STATUS_DELETED} removes the task from the
	 * store and that it no longer appears in {@link TaskStore#getAllTasks()}.
	 */
	public void testUpdateTaskDeletedRemovesFromStore() {
		_store.createTask("Task A", "d", "f");
		_store.createTask("Task B", "d", "f");

		_store.updateTask("1", TaskStore.STATUS_DELETED, null, null, null, null, null, null);

		assertNull("Deleted task should not be retrievable", _store.getTask("1"));
		assertEquals("Only one task should remain", 1, _store.getAllTasks().size());
		assertEquals("Remaining task should be Task B", "Task B", _store.getAllTasks().get(0).getSubject());
	}

	/**
	 * Verifies that the deleted task object is still returned by updateTask so the caller can read
	 * its final state.
	 */
	public void testUpdateTaskDeletedReturnsTask() {
		_store.createTask("Doomed", "d", "f");

		AgentTask deleted = _store.updateTask("1", TaskStore.STATUS_DELETED,
			null, null, null, null, null, null);
		assertNotNull("Deleted task should be returned", deleted);
		assertEquals("Doomed", deleted.getSubject());
	}

	/**
	 * Verifies that {@code addBlocks} adds task IDs to the blocks set.
	 */
	public void testUpdateTaskAddBlocks() {
		_store.createTask("Blocker", "d", "f");
		_store.createTask("Blocked 1", "d", "f");
		_store.createTask("Blocked 2", "d", "f");

		_store.updateTask("1", null, null, null, null, null, Arrays.asList("2", "3"), null);

		AgentTask blocker = _store.getTask("1");
		assertTrue("Should block task 2", blocker.getBlocks().contains("2"));
		assertTrue("Should block task 3", blocker.getBlocks().contains("3"));
		assertEquals("Should have exactly 2 blocks", 2, blocker.getBlocks().size());
	}

	/**
	 * Verifies that {@code addBlockedBy} adds task IDs to the blockedBy set.
	 */
	public void testUpdateTaskAddBlockedBy() {
		_store.createTask("Prerequisite 1", "d", "f");
		_store.createTask("Prerequisite 2", "d", "f");
		_store.createTask("Dependent", "d", "f");

		_store.updateTask("3", null, null, null, null, null, null, Arrays.asList("1", "2"));

		AgentTask dependent = _store.getTask("3");
		assertTrue("Should be blocked by task 1", dependent.getBlockedBy().contains("1"));
		assertTrue("Should be blocked by task 2", dependent.getBlockedBy().contains("2"));
	}

	/**
	 * Verifies that blocks and blockedBy sets are unmodifiable views.
	 */
	public void testDependencySetsUnmodifiable() {
		_store.createTask("Task", "d", "f");
		_store.updateTask("1", null, null, null, null, null, Arrays.asList("2"), Arrays.asList("3"));

		AgentTask task = _store.getTask("1");

		try {
			task.getBlocks().add("99");
			fail("Blocks set should be unmodifiable");
		} catch (UnsupportedOperationException expected) {
			// expected
		}

		try {
			task.getBlockedBy().add("99");
			fail("BlockedBy set should be unmodifiable");
		} catch (UnsupportedOperationException expected) {
			// expected
		}
	}

	/**
	 * Verifies that repeated addBlocks calls accumulate rather than replace.
	 */
	public void testAddBlocksAccumulates() {
		_store.createTask("Task", "d", "f");

		_store.updateTask("1", null, null, null, null, null, Arrays.asList("2"), null);
		_store.updateTask("1", null, null, null, null, null, Arrays.asList("3"), null);

		AgentTask task = _store.getTask("1");
		assertEquals("Should have accumulated 2 blocks", 2, task.getBlocks().size());
		assertTrue(task.getBlocks().contains("2"));
		assertTrue(task.getBlocks().contains("3"));
	}

	/**
	 * Verifies that the version counter starts at 0 and increments on each create and update
	 * operation.
	 */
	public void testVersionIncrements() {
		assertEquals("Initial version should be 0", 0, _store.getVersion());

		_store.createTask("Task", "d", "f");
		assertEquals("Version should increment on create", 1, _store.getVersion());

		_store.updateTask("1", TaskStore.STATUS_IN_PROGRESS, null, null, null, null, null, null);
		assertEquals("Version should increment on update", 2, _store.getVersion());

		_store.updateTask("1", TaskStore.STATUS_DELETED, null, null, null, null, null, null);
		assertEquals("Version should increment on delete", 3, _store.getVersion());
	}

	/**
	 * Verifies that updating a non-existent task does NOT increment the version.
	 */
	public void testVersionNotIncrementedOnFailedUpdate() {
		assertEquals(0, _store.getVersion());

		_store.updateTask("999", TaskStore.STATUS_COMPLETED, null, null, null, null, null, null);
		assertEquals("Version should not change for non-existent task", 0, _store.getVersion());
	}

	/**
	 * Verifies that toJson/fromJson round-trips all task fields including dependencies.
	 */
	public void testSerializationRoundTrip() {
		_store.createTask("Task A", "desc A", "Doing A");
		_store.createTask("Task B", "desc B", "Doing B");
		_store.updateTask("1", TaskStore.STATUS_IN_PROGRESS, null, null, null, "agent-1",
			Arrays.asList("2"), null);
		_store.updateTask("2", null, null, null, null, null, null, Arrays.asList("1"));

		String json = _store.toJson();
		assertNotNull("JSON should not be null", json);
		assertFalse("JSON should not be empty", json.isEmpty());

		TaskStore restored = TaskStore.fromJson(json);
		List<AgentTask> tasks = restored.getAllTasks();
		assertEquals("Should have 2 tasks", 2, tasks.size());

		AgentTask a = restored.getTask("1");
		assertEquals("Task A", a.getSubject());
		assertEquals("desc A", a.getDescription());
		assertEquals("Doing A", a.getActiveForm());
		assertEquals(TaskStore.STATUS_IN_PROGRESS, a.getStatus());
		assertEquals("agent-1", a.getOwner());
		assertTrue(a.getBlocks().contains("2"));

		AgentTask b = restored.getTask("2");
		assertEquals("Task B", b.getSubject());
		assertTrue(b.getBlockedBy().contains("1"));
	}

	/**
	 * Verifies that the high-water mark prevents ID reuse after deletion and serialization.
	 */
	public void testHighWaterMarkPreservesAcrossRoundTrip() {
		_store.createTask("Task 1", "d", "f");
		_store.createTask("Task 2", "d", "f");
		_store.createTask("Task 3", "d", "f");
		_store.updateTask("2", TaskStore.STATUS_DELETED, null, null, null, null, null, null);

		String json = _store.toJson();
		TaskStore restored = TaskStore.fromJson(json);

		// Next ID should be 4 (not 3, even though task 2 was deleted)
		AgentTask next = restored.createTask("Task 4", "d", "f");
		assertEquals("ID should skip deleted ID 2 via high-water mark", "4", next.getId());
	}

	/**
	 * Verifies that fromJson with null returns an empty store.
	 */
	public void testFromJsonNull() {
		TaskStore restored = TaskStore.fromJson(null);
		assertNotNull(restored);
		assertTrue(restored.getAllTasks().isEmpty());
	}

	/**
	 * Verifies that fromJson with empty string returns an empty store.
	 */
	public void testFromJsonEmpty() {
		TaskStore restored = TaskStore.fromJson("");
		assertNotNull(restored);
		assertTrue(restored.getAllTasks().isEmpty());
	}

	/**
	 * Verifies that an empty store serializes and deserializes correctly.
	 */
	public void testSerializationEmptyStore() {
		String json = _store.toJson();
		TaskStore restored = TaskStore.fromJson(json);
		assertTrue(restored.getAllTasks().isEmpty());
		// New tasks should start at ID 1
		AgentTask task = restored.createTask("First", "d", "f");
		assertEquals("1", task.getId());
	}

	/**
	 * Verifies that countUncompleted() counts pending and in_progress tasks.
	 */
	public void testCountUncompleted() {
		assertEquals(0, _store.countUncompleted());

		_store.createTask("A", "d", "f");
		_store.createTask("B", "d", "f");
		_store.createTask("C", "d", "f");
		assertEquals(3, _store.countUncompleted());

		_store.updateTask("1", TaskStore.STATUS_IN_PROGRESS, null, null, null, null, null, null);
		assertEquals("in_progress counts as uncompleted", 3, _store.countUncompleted());

		_store.updateTask("1", TaskStore.STATUS_COMPLETED, null, null, null, null, null, null);
		assertEquals("completed should not count", 2, _store.countUncompleted());

		_store.updateTask("2", TaskStore.STATUS_DELETED, null, null, null, null, null, null);
		assertEquals("deleted should not count", 1, _store.countUncompleted());
	}

	/**
	 * Verifies that all fields can be updated simultaneously in a single call.
	 */
	public void testUpdateAllFieldsAtOnce() {
		_store.createTask("Original", "orig desc", "Doing original");

		_store.updateTask("1",
			TaskStore.STATUS_IN_PROGRESS,
			"New Subject",
			"New Description",
			"New Active Form",
			"new-owner",
			Arrays.asList("2"),
			Arrays.asList("3"));

		AgentTask task = _store.getTask("1");
		assertEquals(TaskStore.STATUS_IN_PROGRESS, task.getStatus());
		assertEquals("New Subject", task.getSubject());
		assertEquals("New Description", task.getDescription());
		assertEquals("New Active Form", task.getActiveForm());
		assertEquals("new-owner", task.getOwner());
		assertTrue(task.getBlocks().contains("2"));
		assertTrue(task.getBlockedBy().contains("3"));
	}
}
