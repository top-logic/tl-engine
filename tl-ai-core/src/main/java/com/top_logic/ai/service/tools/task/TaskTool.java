/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.ai.service.tools.task.TaskStore.AgentTask;
import com.top_logic.ai.service.tools.task.messages.TaskListResponse;
import com.top_logic.ai.service.tools.task.messages.TaskResponse;
import com.top_logic.ai.service.tools.task.messages.TaskSummary;
import com.top_logic.ai.service.tools.util.DataClassSerializer;

import com.top_logic.tl.ai.core.tool.P;
import com.top_logic.tl.ai.core.tool.ToolMethod;

/**
 * Tool for managing agent tasks within a processing session.
 *
 * <p>
 * Provides CRUD operations on an in-memory {@link TaskStore} that is bound to the current thread
 * via {@link TaskStoreBinding}. Tasks are used to track progress and organize multi-step agent work.
 * </p>
 */
public class TaskTool {

	/**
	 * Configuration for {@link TaskTool}.
	 */
	public interface Config extends PolymorphicConfiguration<TaskTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link TaskTool}.
	 */
	public TaskTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link TaskTool} from configuration.
	 */
	@CalledByReflection
	public TaskTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Creates a new task with status "pending".
	 *
	 * @param subject
	 *        Brief title for the task.
	 * @param description
	 *        Detailed description of what needs to be done.
	 * @param activeForm
	 *        Present continuous form shown while the task is in progress (e.g. "Running tests").
	 * @return JSON with {@code taskId}, status, and subject.
	 */
	@ToolMethod(description = "Create a new task to track a unit of work. Use for complex multi-step operations (3+ steps)."
		+ " Do NOT use for single, trivial tasks. Tasks start as 'pending'. Always provide activeForm."
		+ " Use imperative form for subject (e.g. 'Create user model'),"
		+ " present continuous for activeForm (e.g. 'Creating user model')."
		+ " Returns the task ID for later updates via updateTask.")
	public String createTask(
			@P("Brief title for the task") String subject,
			@P("Detailed description of what needs to be done") String description,
			@P("Present continuous form shown in spinner when in progress (e.g. 'Running tests')") String activeForm) {

		Map<String, Object> args = new HashMap<>();
		args.put("subject", subject);
		args.put("description", description);
		args.put("activeForm", activeForm);

		try {
			TaskStore store = requireStore();
			AgentTask task = store.createTask(subject, description, activeForm);

			TaskResponse response = TaskResponse.create()
				.setTaskId(task.getId())
				.setStatus(task.getStatus())
				.setSubject(task.getSubject());

			return DataClassSerializer.toJson(response, TaskTool.class, "createTask()", args);
		} catch (Exception ex) {
			Logger.error("createTask() error: " + ex.getMessage(), ex, TaskTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to create task: " + ex.getMessage(), TaskTool.class, "createTask()", args);
		}
	}

	/**
	 * Updates an existing task. Only non-null parameters are applied.
	 *
	 * @param taskId
	 *        The ID of the task to update.
	 * @param status
	 *        New status (pending, in_progress, completed, deleted), or null to keep existing.
	 * @param subject
	 *        New subject, or null to keep existing.
	 * @param description
	 *        New description, or null to keep existing.
	 * @param activeForm
	 *        New active form, or null to keep existing.
	 * @param owner
	 *        New owner, or null to keep existing.
	 * @param addBlocks
	 *        Task IDs to add to the blocks set, or null to skip.
	 * @param addBlockedBy
	 *        Task IDs to add to the {@code blockedBy} set, or null to skip.
	 * @return JSON with updated {@code taskId}, status, and subject, or error if task not found.
	 */
	@ToolMethod(description = "Update a task's status, subject, description, owner, or dependencies."
		+ " Set status to 'in_progress' BEFORE starting work on a task."
		+ " Set status to 'completed' ONLY when the task is FULLY done."
		+ " If blocked or encountering errors, keep status as 'in_progress'."
		+ " Use 'deleted' to remove a task that is no longer needed."
		+ " After completing a task, call listTasks to find the next one.")
	public String updateTask(
			@P("The ID of the task to update") String taskId,
			@P("New status (pending, in_progress, completed, deleted)") String status,
			@P("New subject") String subject,
			@P("New description") String description,
			@P("New active form") String activeForm,
			@P("New owner") String owner,
			@P("Task IDs to add to the blocks set") List<String> addBlocks,
			@P("Task IDs to add to the blockedBy set") List<String> addBlockedBy) {

		Map<String, Object> args = new HashMap<>();
		args.put("taskId", taskId);
		args.put("status", status);
		args.put("subject", subject);
		args.put("description", description);
		args.put("activeForm", activeForm);
		args.put("owner", owner);
		args.put("addBlocks", addBlocks);
		args.put("addBlockedBy", addBlockedBy);

		try {
			TaskStore store = requireStore();
			AgentTask task = store.updateTask(taskId, status, subject, description, activeForm, owner,
				addBlocks, addBlockedBy);

			if (task == null) {
				return DataClassSerializer.errorToJson(
					"Task not found: " + taskId, TaskTool.class, "updateTask()", args);
			}

			TaskResponse response = TaskResponse.create()
				.setTaskId(task.getId())
				.setStatus(task.getStatus())
				.setSubject(task.getSubject());

			return DataClassSerializer.toJson(response, TaskTool.class, "updateTask()", args);
		} catch (Exception ex) {
			Logger.error("updateTask() error: " + ex.getMessage(), ex, TaskTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to update task: " + ex.getMessage(), TaskTool.class, "updateTask()", args);
		}
	}

	/**
	 * Lists all tasks with summary information.
	 *
	 * @return JSON with array of task summaries containing id, subject, status, owner, and
	 *         {@code blockedBy}.
	 */
	@ToolMethod(description = "List all tasks with their ID, subject, status, owner, and blockedBy dependencies."
		+ " Use to see available work, check overall progress, or find the next task after completing one."
		+ " Prefer working on tasks in ID order (lowest first)."
		+ " Tasks with non-empty blockedBy cannot be started until their blockers are completed.")
	public String listTasks() {

		Map<String, Object> args = new HashMap<>();

		try {
			TaskStore store = requireStore();
			List<AgentTask> tasks = store.getAllTasks();

			TaskListResponse response = TaskListResponse.create();
			for (AgentTask task : tasks) {
				TaskSummary summary = TaskSummary.create()
					.setId(task.getId())
					.setSubject(task.getSubject())
					.setStatus(task.getStatus())
					.setOwner(task.getOwner() != null ? task.getOwner() : "");

				for (String blockedById : task.getBlockedBy()) {
					summary.addBlockedBy(blockedById);
				}

				response.addTask(summary);
			}

			return DataClassSerializer.toJson(response, TaskTool.class, "listTasks()", args);
		} catch (Exception ex) {
			Logger.error("listTasks() error: " + ex.getMessage(), ex, TaskTool.class);
			return DataClassSerializer.errorToJson(
				"Failed to list tasks: " + ex.getMessage(), TaskTool.class, "listTasks()", args);
		}
	}

	/**
	 * Returns the {@link TaskStore} bound to the current thread.
	 *
	 * @return The current task store, never null.
	 * @throws IllegalStateException
	 *         if no store is bound to the current thread.
	 */
	private TaskStore requireStore() {
		TaskStore store = TaskStoreBinding.getStore();
		if (store == null) {
			throw new IllegalStateException("No TaskStore bound to the current thread.");
		}
		return store;
	}
}
