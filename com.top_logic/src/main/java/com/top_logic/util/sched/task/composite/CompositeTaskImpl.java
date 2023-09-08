/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.composite.CompositeTaskImpl.Config;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Extension of {@link TaskImpl} that implements the {@link CompositeTask} interface.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
@InApp
public class CompositeTaskImpl<C extends Config<?>> extends StateHandlingTask<C> implements CompositeTask {

	/** The configuration for the {@link CompositeTaskImpl}. */
	@TagName("composite-task")
	public static interface Config<I extends CompositeTaskImpl<?>> extends StateHandlingTask.Config<I> {

		/** Property name of {@link #getTasks()}. */
		String TASKS = "tasks";

		/**
		 * The configurations for the children of this {@link CompositeTask}.
		 * <p>
		 * <b>Their scheduling-information are ignored.</b><br/>
		 * They are executed in this order when this {@link CompositeTaskImpl} is executed.
		 * </p>
		 */
		@Name(TASKS)
		@DefaultContainer
		@Options(fun = AllInAppImplementations.class)
		List<Task.Config<? extends Task>> getTasks();

		/**
		 * If these children fail, the {@link CompositeTask} will not fail but continue.
		 * <p>
		 * If any other child fails, the {@link CompositeTask} will fail, too. Without executing any
		 * other child task. <br/>
		 * "<i>fail</i>" means: The task returns with a {@link ResultType} that is not
		 * {@link ResultType#SUCCESS} or throws any {@link Throwable}.
		 * </p>
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getNonCriticalChildren();

		/**
		 * Should the maintenance mode be left as soon as the last child needing it is done?
		 * <p>
		 * Is implemented only for top-level tasks. {@link CompositeTask}s that are nested within
		 * other {@link CompositeTask}s cannot leave the maintenance mode early.
		 * </p>
		 */
		@BooleanDefault(true)
		boolean getLeaveMaintenanceModeEarly();

		/**
		 * Whether this {@link CompositeTaskImpl} can be stopped.
		 * 
		 * <p>
		 * If the value is <code>false</code>, the task simply ignores {@link Task#signalStop() stop
		 * requests}.
		 * </p>
		 */
		@BooleanDefault(true)
		boolean isStoppable();

	}

	static class ChildTaskException extends RuntimeException {

		private static final String MESSAGE_PREFIX = "One of the child tasks died: ";

		private final TaskResult _result;

		public ChildTaskException(TaskResult result, String message) {
			super(MESSAGE_PREFIX + message);
			_result = result;
		}

		public ChildTaskException(TaskResult result, Throwable cause) {
			super(MESSAGE_PREFIX + cause.getMessage(), cause);
			_result = result;
		}

		public ChildTaskException(TaskResult result, String message, Throwable cause) {
			super(MESSAGE_PREFIX + message, cause);
			_result = result;
		}

		public TaskResult getResult() {
			return _result;
		}

	}

	private final List<Task> _children;

	private volatile Task _activeChild;

	/** Used by the typed configuration to instantiate the {@link CompositeTaskImpl}. */
	@CalledByReflection
	public CompositeTaskImpl(InstantiationContext context, C config) {
		super(context, config);
		_children = createChildren(context, config);
	}

	private List<Task> createChildren(InstantiationContext context, Config<?> config) {
		return TypedConfiguration.getInstanceList(context, config.getTasks());
	}

	@Override
	public List<Task> getChildren() {
		return new ArrayList<>(_children);
	}

	@Override
	protected void onAttachToScheduler() {
		super.onAttachToScheduler();
		Scheduler scheduler = getScheduler().get();
		for (Task child : getChildren()) {
			child.attachTo(scheduler);
		}
	}

	@Override
	protected void onDetachFromScheduler() {
		for (Task child : getChildren()) {
			child.detachFromScheduler();
		}
		super.onDetachFromScheduler();
	}

	@Override
	protected final void runHook() {
		try {
			ResultType resultType = runChildren();
			if (getShouldStop()) {
				getLog().taskEnded(ResultType.CANCELED, TaskResult.ResultType.CANCELED.getCombinedMessageI18N());
				return;
			}
			getLog().taskEnded(resultType, resultType.getCombinedMessageI18N());
		} catch (ChildTaskException exception) {
			ResultType resultType = calcParentResultType(exception.getResult().getResultType());
			getLog().taskEnded(resultType, I18NConstants.CHILD_TASK_DIED, exception);
		}
	}

	private ResultType calcParentResultType(ResultType childResultType) {
		if (childResultType == ResultType.ERROR || childResultType == ResultType.FAILURE) {
			return childResultType;
		}
		return ResultType.ERROR;
	}

	/**
	 * Runs the child tasks and returns the combined {@link ResultType}.
	 * <p>
	 * Is called in {@link #runHook()} which also takes care of storing the returned
	 * {@link ResultType} and catching the {@link ChildTaskException}, if one is thrown.
	 * </p>
	 * 
	 * @throws ChildTaskException
	 *         If at least one of the children dies.
	 */
	protected ResultType runChildren() throws ChildTaskException {
		ResultType combinedResultType = ResultType.SUCCESS;
		Task lastMaintenanceChild = getLastMaintenanceChild();
		for (Task child : _children) {
			if (getShouldStop()) {
				return combinedResultType;
			}
			if (Scheduler.getSchedulerInstance().isTaskBlocked(child)) {
				Logger.info("Skipping execution of subtask '" + child.getName() + "' as it is blocked.",
					CompositeTaskImpl.class);
				continue;
			}
			Maybe<ResultType> childResultType = runChild(child);

			if (!childResultType.hasValue()) {
				// There is no result only if, the task should stop.
				break;
			}
			if (childResultType.get() == ResultType.WARNING) {
				logGuiWarning(Resources.getInstance().getString(I18NConstants.CHILD_HAS_WARNINGS__NAME.fill(child.getName())));
			}
			combinedResultType = combine(combinedResultType, childResultType.get());
			if (isStoppingParent(childResultType.get()) && !isNonCriticalChild(child)) {
				break;
			}
			if ((child == lastMaintenanceChild) && getConfig().getLeaveMaintenanceModeEarly()) {
				Scheduler.getSchedulerInstance().waiveMaintenanceMode(this);
			}
		}
		return combinedResultType;
	}

	/** Find the last child task that needs the maintenance mode. */
	protected Task getLastMaintenanceChild() {
		List<Task> children = getChildren();
		Collections.reverse(children);
		for (Task child : children) {
			if (child.needsMaintenanceMode()) {
				return child;
			}
		}
		return null;
	}

	/** Combines two {@link ResultType}s with the rule: "The worst wins." */
	protected ResultType combine(ResultType left, ResultType right) {
		if (left == ResultType.ERROR || right == ResultType.ERROR) {
			return ResultType.ERROR;
		}
		if (left == ResultType.FAILURE || right == ResultType.FAILURE) {
			return ResultType.FAILURE;
		}
		if (left == ResultType.UNKNOWN || right == ResultType.UNKNOWN) {
			return ResultType.UNKNOWN;
		}
		if (left == ResultType.CANCELED || right == ResultType.CANCELED) {
			return ResultType.CANCELED;
		}
		if (left == ResultType.WARNING || right == ResultType.WARNING) {
			return ResultType.WARNING;
		}
		assert left == ResultType.SUCCESS && right == ResultType.SUCCESS :
			"Unexpected result type: '" + left + "' or '" + right + "'";

		return ResultType.SUCCESS;
	}

	private boolean isStoppingParent(ResultType resultType) {
		return resultType == ResultType.ERROR || resultType == ResultType.FAILURE || resultType == ResultType.UNKNOWN;
	}

	/**
	 * Runs the child {@link Task} and converts any {@link Throwable} thrown by the child into a
	 * {@link ChildTaskException}.
	 */
	protected Maybe<ResultType> runChild(Task child) throws ChildTaskException {
		try {
			if (getShouldStop()) {
				return Maybe.none();
			}
			synchronized (this) {
				_activeChild = child;
			}
			child.started(System.currentTimeMillis());
			child.run();
			return getChildResultType(child);
		} catch (Throwable exception) {
			if (isNonCriticalChild(child)) {
				return getChildResultType(child);
			}
			throw new ChildTaskException(child.getLog().getCurrentResult(), exception);
		} finally {
			_activeChild = null;
		}
	}

	/**
	 * Is the child {@link Task} non-critical?
	 * <p>
	 * If a child is critical, the {@link CompositeTask} stops execution if the child returns with a
	 * problem.
	 * </p>
	 */
	protected boolean isNonCriticalChild(Task child) {
		return getConfig().getNonCriticalChildren().contains(child.getName());
	}

	/** Convenience getter for the {@link ResultType} of the {@link Task}. */
	protected Maybe<ResultType> getChildResultType(Task child) {
		// getCurrentResult cannot be null here, as the child was just executed.
		return Maybe.some(child.getLog().getCurrentResult().getResultType());
	}

	@Override
	public synchronized boolean signalStopHook() {
		if (!getConfig().isStoppable()) {
			return false;
		}
		boolean result = true;
		Task activeChild = _activeChild;
		if (activeChild != null) {
			result &= activeChild.signalStop();
		}
		return result;
	}

	@Override
	public synchronized void signalShutdown() {
		super.signalShutdown();
		Task activeChild = _activeChild;
		if (activeChild != null) {
			activeChild.signalShutdown();
		}
	}

	@Override
	public boolean isPersistent() {
		if (super.isPersistent()) {
			return true;
		}
		for (Task child : _children) {
			if (child.isPersistent()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isNodeLocal() {
		if (!super.isNodeLocal()) {
			return false;
		}
		for (Task child : _children) {
			if (!child.isNodeLocal()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean needsMaintenanceMode() {
		if (super.needsMaintenanceMode()) {
			return true;
		}
		for (Task child : _children) {
			if (child.needsMaintenanceMode()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isMaintenanceModeSafe() {
		if (super.isMaintenanceModeSafe()) {
			return true;
		}
		for (Task child : _children) {
			if (!child.isMaintenanceModeSafe()) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected synchronized boolean getShouldStop() {
		if (!getConfig().isStoppable()) {
			return false;
		}
		return super.getShouldStop();
	}

}
