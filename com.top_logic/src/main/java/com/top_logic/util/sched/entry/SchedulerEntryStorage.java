/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.entry;

import static com.top_logic.util.sched.entry.SchedulerEntryUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.entry.filter.TopLevelSchedulerEntryFilter;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTask;
import com.top_logic.util.sched.task.composite.CompositeTaskUtil;

/**
 * Stores the {@link SchedulerEntry}s of the {@link Scheduler}.
 * <p>
 * Manages additions, removals, queries and other related functionality.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SchedulerEntryStorage {

	private final Map<String, SchedulerEntry> _entries = new HashMap<>();

	/**
	 * The {@link SchedulerEntry} for the {@link Task}.
	 * <p>
	 * Works for children of a {@link CompositeTask}, too, recursively.
	 * </p>
	 */
	public synchronized Maybe<SchedulerEntry> get(Task task) {
		return get(task.getName());
	}

	/**
	 * The {@link SchedulerEntry} for the {@link Task} with the given name.
	 * <p>
	 * Works for children of a {@link CompositeTask}, too, recursively.
	 * </p>
	 */
	public synchronized Maybe<SchedulerEntry> get(String taskName) {
		return Maybe.toMaybe(_entries.get(taskName));
	}

	/**
	 * Returns a new {@link Collection} containing the {@link SchedulerEntry}s for all tasks and
	 * subtasks (recursively) known to the {@link SchedulerEntryStorage}.
	 * 
	 * @see #getAllEntries(Filter)
	 * @see #getTopLevelEntries()
	 * @see #getTopLevelEntries(Filter)
	 * 
	 * @return Never null. Is modifiable and resizeable. Has no guaranteed order.
	 */
	public Collection<SchedulerEntry> getAllEntries() {
		return copyEntries();
	}

	/**
	 * Returns a new {@link Collection} containing the {@link SchedulerEntry}s for all tasks and
	 * subtasks (recursively) accepted by the given {@link Filter}.
	 * 
	 * @see #getAllEntries(Filter)
	 * @see #getTopLevelEntries()
	 * @see #getTopLevelEntries(Filter)
	 * 
	 * @param filter
	 *        Is not allowed to be null.
	 * @return Never null. Is modifiable and resizeable. Has no guaranteed order.
	 */
	public Collection<SchedulerEntry> getAllEntries(Filter<? super SchedulerEntry> filter) {
		List<SchedulerEntry> result = copyEntries();
		Iterator<SchedulerEntry> iterator = result.iterator();
		while (iterator.hasNext()) {
			SchedulerEntry entry = iterator.next();
			if (!filter.accept(entry)) {
				iterator.remove();
			}
		}
		return result;
	}

	/**
	 * Returns a new {@link ArrayList} containing the {@link SchedulerEntry}s.
	 */
	protected synchronized ArrayList<SchedulerEntry> copyEntries() {
		return new ArrayList<>(_entries.values());
	}

	/**
	 * Returns a new {@link Collection} containing the {@link SchedulerEntry}s for all top-level
	 * tasks known to the {@link SchedulerEntryStorage}.
	 * 
	 * @see #getAllEntries()
	 * @see #getAllEntries(Filter)
	 * @see #getTopLevelEntries(Filter)
	 * 
	 * @return Never null. Is modifiable and resizeable. Has no guaranteed order.
	 */
	public Collection<SchedulerEntry> getTopLevelEntries() {
		return getAllEntries(TopLevelSchedulerEntryFilter.INSTANCE);
	}

	/**
	 * Returns a new {@link Collection} containing the {@link SchedulerEntry}s for all top-level
	 * tasks accepted by the given {@link Filter}.
	 * 
	 * @see #getAllEntries()
	 * @see #getAllEntries(Filter)
	 * @see #getTopLevelEntries()
	 * 
	 * @param filter
	 *        Is not allowed to be null.
	 * @return Never null. Is modifiable and resizeable. Has no guaranteed order.
	 */
	public Collection<SchedulerEntry> getTopLevelEntries(Filter<? super SchedulerEntry> filter) {
		return getAllEntries(FilterFactory.and(TopLevelSchedulerEntryFilter.INSTANCE, filter));
	}

	/**
	 * Add the {@link Task} to the {@link SchedulerEntryStorage}.
	 * <p>
	 * The children of a {@link CompositeTask} are added as {@link SchedulerEntry#isChild()
	 * children}.
	 * </p>
	 * 
	 * @param task
	 *        Is not allowed to be null. Has to have a case-insensitive unique {@link Task#getName()
	 *        name} among {@link #getAllEntries()}. The same rules apply for the children of
	 *        {@link CompositeTask} s, too, recursively.
	 */
	public synchronized SchedulerEntry add(Task task) {
		checkPreAdd(task);

		SchedulerEntry taskEntry = new SchedulerEntry(task, false);
		_entries.put(task.getName(), taskEntry);
		for (Task descendant : CompositeTaskUtil.getChildrenRecursive(task)) {
			_entries.put(descendant.getName(), new SchedulerEntry(descendant, true));
		}
		return taskEntry;
	}

	/** Has to be called by {@link #add(Task)} before a {@link Task} is added. */
	protected void checkPreAdd(Task task) {
		for (Task descendant : CompositeTaskUtil.getDescendents(task)) {
			checkPreAddNonRecursive(descendant);
		}
		checkUniqueDescendants(task);
	}

	/** Checks a {@link Task}, but not its children. Is called by {@link #checkPreAdd(Task)}. */
	private void checkPreAddNonRecursive(Task task) {
		checkIsNonNull(task);
		checkName(task);
		assert !_entries.containsKey(task.getName());
	}

	/**
	 * If the name is null, the empty string, or a case-insensitive duplicate of another known task,
	 * an {@link IllegalArgumentException} is thrown.
	 */
	protected final void checkName(Task task) throws IllegalArgumentException {
		checkNameNotNull(task);
		checkNameNoExactConflict(task);
		checkNameNoCaseInsensitiveConflict(task);
	}

	/**
	 * Checks that the task's name is not <code>null</code>.
	 * 
	 * @throws IllegalArgumentException
	 *         If the {@link Task} is null.
	 */
	protected void checkNameNotNull(Task task) throws IllegalArgumentException {
		if (StringServices.isEmpty(task.getName())) {
			throw new IllegalArgumentException("Task names are not allowed to be null or the empty string. Task: "
				+ StringServices.getObjectDescription(task));
		}
	}

	/**
	 * Checks that there is no taks with the same name.
	 * 
	 * @throws IllegalArgumentException
	 *         If there is another {@link Task} in {@link #getAllEntries()} that has the same name
	 *         (case sensitive).
	 */
	protected void checkNameNoExactConflict(Task newTask) throws IllegalArgumentException {
		Maybe<SchedulerEntry> conflictingTask = get(newTask.getName());
		if (conflictingTask.hasValue()) {
			throw new IllegalArgumentException("Task names have to be unique. But there is already a task named '"
				+ newTask.getName() + "' and another task with that name should be added to the Scheduler. New Task: "
				+ StringServices.getObjectDescription(newTask) + "; Existing Task: "
				+ StringServices.getObjectDescription(conflictingTask.get().getTask()));
		}
	}

	/**
	 * Checks that there is no task with the same name ignoring case.
	 * 
	 * @throws IllegalArgumentException
	 *         If there is another {@link Task} in {@link #getAllEntries()} that has an
	 *         case-insensitive equal name.
	 */
	protected void checkNameNoCaseInsensitiveConflict(Task newTask) throws IllegalArgumentException {
		for (Task conflictingTask : toTasks(getAllEntries())) {
			if (newTask.getName().equalsIgnoreCase(conflictingTask.getName())) {
				throw new IllegalArgumentException("Task names have to be _case insensitive_ unique."
					+ " But there is already a task named '"
					+ conflictingTask.getName() + "' and another task with name '"
					+ newTask.getName() + "' should be added to the Scheduler. New Task: "
					+ StringServices.getObjectDescription(newTask) + "; Existing Task: "
					+ StringServices.getObjectDescription(conflictingTask));
			}
		}
	}

	/**
	 * Checks that the root {@link Task} has no two descendants (including itself) with the same
	 * names, case-insensitive.
	 */
	protected void checkUniqueDescendants(Task task) {
		List<Task> descendents = CompositeTaskUtil.getDescendents(task);
		for (int outer = 0; outer < descendents.size(); outer++) {
			Task outerTask = descendents.get(outer);
			for (int inner = outer + 1; inner < descendents.size(); inner++) {
				Task innerTask = descendents.get(inner);
				checkUniqueDescendantNames(task, outerTask, innerTask);
			}
		}
	}

	private void checkUniqueDescendantNames(Task root, Task left, Task right) {
		String leftName = left.getName();
		String rightName = right.getName();
		if (leftName.equalsIgnoreCase(rightName)) {
			throw new IllegalArgumentException("Task names have to be _case insensitive_ unique."
				+ " But task '" + root.getName() + "' has multiple descendents with the same name '" + leftName
				+ "'. First task: " + Utils.debug(left) + "; Other task: " + Utils.debug(right));
		}
	}

	/**
	 * Removes the {@link Task} from the {@link SchedulerEntryStorage}.
	 * <p>
	 * The children of a {@link CompositeTask} are removed, too, recursively.
	 * </p>
	 * <p>
	 * If the {@link Task} is not known to the {@link SchedulerEntryStorage}, nothing happens.
	 * </p>
	 */
	public synchronized void remove(Task task) {
		Maybe<SchedulerEntry> entry = get(task);
		if (entry.hasValue()) {
			remove(entry.get());
		}
	}

	/**
	 * Removes the {@link Task} from the {@link SchedulerEntryStorage}.
	 * <p>
	 * The children of a {@link CompositeTask} are removed, too, recursively.
	 * </p>
	 * <p>
	 * If the {@link Task} is not known to the {@link SchedulerEntryStorage}, nothing happens.
	 * </p>
	 */
	public synchronized void remove(SchedulerEntry task) {
		for (Task descendant : CompositeTaskUtil.getDescendents(task.getTask())) {
			_entries.remove(descendant.getName());
		}
	}

	/**
	 * Util: Throws a {@link NullPointerException}, if the {@link Task} is null.
	 */
	protected void checkIsNonNull(Task task) {
		if (task == null) {
			throw new NullPointerException("Task is not allowed to be null.");
		}
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("entries", getAllEntries())
			.build();
	}

}
