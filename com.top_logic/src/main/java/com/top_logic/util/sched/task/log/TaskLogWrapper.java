/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.log;

import static com.top_logic.util.sched.task.TaskCommon.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.GCQueue;
import com.top_logic.basic.col.iterator.IteratorUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.retry.Retry;
import com.top_logic.basic.util.retry.RetryResult;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.I18NConstants;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskCommon;
import com.top_logic.util.sched.task.TaskListenerNotification;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.result.TaskResult;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;
import com.top_logic.util.sched.task.result.TaskResultCommon;
import com.top_logic.util.sched.task.result.TaskResultWrapper;

/**
 * A persistent implementation of {@link TaskLog} based on {@link AbstractWrapper}.
 * <p>
 * If the {@link Task} is {@link Task#isNodeLocal()}, there is one {@link TaskLogWrapper} per
 * cluster node. But if the {@link Task} is not {@link Task#isNodeLocal() node local}, there is one
 * {@link TaskLogWrapper} for all cluster nodes. <br/>
 * Commits in this class have to be retried in most cases, if they fail. The reason is, that the
 * different schedulers of the cluster nodes are synchronized and communicating via the
 * KnowledgeBase. Therefore, concurrent write accesses can happen. Example: A task which is not node
 * local is told to stop from a user that is logged in on a different cluster node. But at the same
 * time the task is storing a warning. If they happen "concurrently" one of them will fail and has
 * to be retried.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TaskLogWrapper extends AbstractWrapper implements TaskLog {

	private static final String ASSOCIATION_TYPE = "hasTaskResult";

	/** The name of the knowledge object type. */
	public static final String TYPE = "TaskLog";

	private static final String PROPERTY_STATE = "state";

	/**
	 * The cluster node name to which this {@link TaskLogWrapper} currently belongs.
	 * <p>
	 * See: {@link TaskCommon#getCurrentClusterNodeName()} <br/>
	 * If the {@link Task} is {@link Task#isNodeLocal()}, this is the name of cluster node to which
	 * this {@link TaskLogWrapper} permanently belongs. But if the {@link Task} is not
	 * {@link Task#isNodeLocal() node local}, this is the cluster node which is currently executing
	 * this {@link Task}, and if it is not running, it is <code>null</code>.
	 * </p>
	 */
	private static final String PROPERTY_CLUSTER_NAME = "clusterNode";

	/**
	 * The cluster node id to which this {@link TaskLogWrapper} currently belongs.
	 * <p>
	 * See: {@link TaskCommon#getCurrentClusterNodeId()} <br/>
	 * If the {@link Task} is {@link Task#isNodeLocal()}, this is the id of cluster node to which
	 * this {@link TaskLogWrapper} permanently belongs. But if the {@link Task} is not
	 * {@link Task#isNodeLocal() node local}, this is the cluster node which is currently executing
	 * this {@link Task}, and if it is not running, it is <code>null</code>.
	 * </p>
	 */
	private static final String PROPERTY_CLUSTER_ID = "clusterId";

	private static final AssociationSetQuery<KnowledgeAssociation> RESULTS_QUERY =
		AssociationQuery.createOutgoingQuery("results", ASSOCIATION_TYPE);

	/**
	 * <p>
	 * Normally, fields in a {@link Wrapper} are a bug: The {@link WrapperFactory} only caches
	 * {@link Wrapper}s with weak references. If there is no other reference to them, they can be
	 * garbage collected. If they are requested again, they are rebuild, but the values of the
	 * fields are lost, of course. <br/>
	 * This is no problem here: {@link TaskLogWrapper}s always belong to a {@link Task}, which has a
	 * strong reference to them. And the {@link Task}s are held by the Scheduler. Therefore, a
	 * {@link TaskLogWrapper} can never be garbage collected, as long as the {@link Scheduler} still
	 * exists. And the {@link Scheduler} is the only one using this field.
	 * </p>
	 */
	private GCQueue<TaskListenerNotification> _events;

	@Override
	public synchronized void setEventQueue(GCQueue<TaskListenerNotification> events) throws IllegalStateException {
		if (_events != null && events != null) {
			throw new IllegalStateException("An event queue is already registered.");
		}
		_events = events;
	}

	/**
	 * If there is already a log for the given {@link Task}, return that. Otherwise, create a new
	 * one.
	 */
	public static TaskLogWrapper getLogForTask(Task task) {
		TaskLogWrapper taskLog = fetchPersistedTaskLog(task);
		if (taskLog == null) {
			taskLog = createInternal(task);
		}
		return taskLog;
	}

	private static TaskLogWrapper fetchPersistedTaskLog(Task task) {
		List<TaskLogWrapper> taskLogs;
		if (task.isNodeLocal()) {
			taskLogs = getLocalTaskLogs(task);
		} else {
			taskLogs = getAllTaskLogs(task);
		}

		if (taskLogs.isEmpty()) {
			return null;
		}
		if (taskLogs.size() > 1) {
			Logger.error("Found multiple task logs for task '" + task.getName() + "'. Using the last used one.",
				TaskLogWrapper.class);
			sortTaskLogs(taskLogs);
		}
		return taskLogs.get(0);
	}

	private static List<TaskLogWrapper> getLocalTaskLogs(Task task) {
		List<TaskLogWrapper> localTaskLogs = new ArrayList<>();
		for (TaskLogWrapper taskLog : getAllTaskLogs(task)) {
			if (StringServices.equals(taskLog.getClusterLockName(), getCurrentClusterNodeName())) {
				localTaskLogs.add(taskLog);
			}
		}
		return localTaskLogs;
	}

	/** Returns a {@link List} of all {@link TaskLogWrapper}s stored for the given {@link Task}. */
	public static List<TaskLogWrapper> getAllTaskLogs(Task task) {
		return getAllTaskLogs(task.getName());
	}

	/** Returns a {@link List} of all {@link TaskLogWrapper}s stored for the given {@link Task}. */
	public static List<TaskLogWrapper> getAllTaskLogs(String taskName) {
		try {
			Iterator<KnowledgeItem> taskLogsIter = PersistencyLayer.getKnowledgeBase().getObjectsByAttribute(
				TaskLogWrapper.TYPE, AbstractWrapper.NAME_ATTRIBUTE, taskName);
			List<KnowledgeObject> taskLogKOs = (List) IteratorUtil.toList(taskLogsIter);
			return WrapperFactory.getWrappersForKOs(TaskLogWrapper.class, taskLogKOs);
		} catch (UnknownTypeException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static void sortTaskLogs(List<TaskLogWrapper> taskLogs) {
		Collections.sort(taskLogs, Collections.reverseOrder((left, right) -> {
			if (left == null && right == null) {
				return 0;
			}
			if (left == null) {
				return -1;
			}
			if (right == null) {
				return 1;
			}
			TaskResult currentResultLeft = left.getCurrentResult();
			TaskResult currentResultRight = right.getCurrentResult();
			if (currentResultLeft == null && currentResultRight == null) {
				return 0;
			}
			if (currentResultLeft == null) {
				return -1;
			}
			if (currentResultRight == null) {
				return 1;
			}
			return currentResultLeft.getStartDate().compareTo(currentResultRight.getStartDate());
		}));
	}

	/**
	 * This creates a new {@link TaskLogWrapper} with the given {@link Task}.
	 */
	private static TaskLogWrapper createInternal(Task task) {
		RetryResult<TaskLogWrapper, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> createRetry(task));
		if (!result.isSuccess()) {
			String message = "Committing the creation of the log of task '" + task.getName() + "' failed after "
				+ COMMIT_RETRIES + " attempts.";
			throw ExceptionUtil.createException(message, result.getReason());
		}
		return result.getValue();
	}

	private static RetryResult<TaskLogWrapper, Throwable> createRetry(Task task) {
		KnowledgeBase knowledgeBase = PersistencyLayer.getKnowledgeBase();
		Transaction transaction = knowledgeBase.beginTransaction();
		try {
			KnowledgeObject taskLogKO = knowledgeBase.createKnowledgeObject(TYPE);
			taskLogKO.setAttributeValue(NAME_ATTRIBUTE, task.getName());
			taskLogKO.setAttributeValue(PROPERTY_STATE, TaskState.INACTIVE.name());
			if (task.isNodeLocal()) {
				taskLogKO.setAttributeValue(PROPERTY_CLUSTER_NAME, getCurrentClusterNodeName());
				taskLogKO.setAttributeValue(PROPERTY_CLUSTER_ID, getCurrentClusterNodeId());
			}
			TaskLogWrapper taskLogWrapper = (TaskLogWrapper) WrapperFactory.getWrapper(taskLogKO);
			transaction.commit();
			return RetryResult.createSuccess(taskLogWrapper);
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<TaskLogWrapper, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * <b>NEVER USE THIS CONSTRUCTOR!</b>
	 * <p>
	 * This constructor is only for the WrapperFactory!
	 * </p>
	 */
	public TaskLogWrapper(KnowledgeObject ko) {
		super(ko);
	}

	private Task getTaskElseNull() {
		Scheduler scheduler = getSchedulerElseNull();
		if (scheduler == null) {
			return null;
		}
		Task task = scheduler.getTaskByName(getNameInternal());
		if (task == null) {
			Logger.error("Could not find task '" + getNameInternal()
				+ "'. Make sure to always register tasks at the scheduler."
				+ " If the task is instantiated manually, make sure to use the name"
				+ " under which the task is registered at the scheduler.", TaskLogWrapper.class);
			return null;
		}
		return task;
	}

	/**
	 * If the {@link com.top_logic.util.sched.Scheduler.Module#isActive()}, return it. Otherwise,
	 * return null.
	 */
	private Scheduler getSchedulerElseNull() {
		if (!Scheduler.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			/* Race condition: The Scheduler might be shutting down right now. Therefore, prepare
			 * for an exception. */
			return Scheduler.getSchedulerInstance();
		} catch (IllegalStateException ex) {
			// Scheduler has been shut down right between the check and the get.
			return null;
		}
	}

	private String getNameInternal() {
		try {
			return (String) tHandle().getAttributeValue(NAME_ATTRIBUTE);
		} catch (NoSuchAttributeException ex) {
			// Unreachable, as there is a name attribute.
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Creates a new {@link TaskResultWrapper}.
	 * <p>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * <br/>
	 * <b>Internal method!</b>
	 * </p>
	 * 
	 * @param type
	 *        The result of the run, must not be <code>null</code>.
	 * @param message
	 *        The I18N message key, must not be <code>null</code>.
	 * @param start
	 *        The date the task has started, must not be <code>null</code>.
	 * @param end
	 *        The date the task has ended, is allowed to be <code>null</code>.
	 * @param exception
	 *        The exception, is allowed to be <code>null</code>.
	 */
	public TaskResultWrapper createTaskResult(ResultType type, ResKey message, Date start, Date end,
			Throwable exception) {
		TaskResultWrapper taskResult =
			TaskResultWrapper.createTaskResult(getNameInternal(), type, message, start, end, exception);
		createAssociation(taskResult);
		return taskResult;
	}

	private void createAssociation(TaskResultWrapper taskResult) {
		PersistencyLayer.getKnowledgeBase().createAssociation(
			this.tHandle(), taskResult.tHandle(), TaskLogWrapper.ASSOCIATION_TYPE);
	}

	/**
	 * If this is a cluster global task, this is the combined state on all cluster nodes, not just
	 * the local one.
	 */
	@Override
	public TaskState getState() {
		return TaskState.valueOf(getString(PROPERTY_STATE));
	}

	@Override
	public TaskResultWrapper getCurrentResult() {
		return findLatestResult();
	}

	private TaskResultWrapper findLatestResult() {
		TaskResultWrapper currentResult = null;
		for (TaskResultWrapper taskResult : getOwnResults()) {
			if (currentResult == null) {
				currentResult = taskResult;
				continue;
			}
			if (taskResult.getStartDate().after(currentResult.getStartDate())) {
				currentResult = taskResult;
			}
		}
		return currentResult;
	}

	@Override
	public TaskResultWrapper getLastResult() {
		if (getState() == TaskState.INACTIVE) {
			return findLatestResult();
		}
		TaskResultWrapper currentResult = null;
		TaskResultWrapper lastResult = null;
		for (TaskResultWrapper taskResult : getOwnResults()) {
			if (currentResult == null) {
				currentResult = taskResult;
				continue;
			}
			if (lastResult == null) {
				if (taskResult.getStartDate().before(currentResult.getStartDate())) {
					lastResult = taskResult;
				} else {
					lastResult = currentResult;
					currentResult = taskResult;
				}
				continue;
			}
			if (taskResult.getStartDate().after(currentResult.getStartDate())) {
				lastResult = currentResult;
				currentResult = taskResult;
				continue;
			}
			if (taskResult.getStartDate().after(lastResult.getStartDate())) {
				lastResult = taskResult;
			}
		}
		return lastResult;
	}

	/**
	 * Get the {@link TaskResult}s stored for this {@link TaskLogWrapper} and those for the
	 * {@link TaskLogWrapper}s of other nodes.
	 * 
	 * @see TaskLog#getResults
	 */
	@Override
	public Collection<? extends TaskResultWrapper> getResults() {
		List<TaskResultWrapper> allResults = new ArrayList<>();
		for (TaskLogWrapper taskLog : getAllTaskLogs(getNameInternal())) {
			allResults.addAll(taskLog.getOwnResults());
		}
		return allResults;
	}

	/**
	 * Get the {@link TaskResult}s stored for this {@link TaskLogWrapper}, but not those for the
	 * {@link TaskLogWrapper}s of other nodes.
	 * <p>
	 * If the {@link Task} is not {@link Task#isNodeLocal() node local}, there is one
	 * {@link TaskLogWrapper} for all cluster nodes. <br/>
	 * <b>Internal method!</b>
	 * </p>
	 */
	public Collection<? extends TaskResultWrapper> getOwnResults() {
		return resolveWrappersTyped(RESULTS_QUERY, TaskResultWrapper.class);
	}

	@Override
	public Collection<? extends TaskResultWrapper> getResults(ResultType resultType) {
		return TaskResultCommon.findAll(getResults(), resultType);
	}

	@Override
	public Map<ResultType, ? extends Collection<? extends TaskResultWrapper>> getResultsByType() {
		return TaskResultCommon.partitionByResultType(getResults());
	}

	@Override
	public Collection<? extends TaskResultWrapper> getFailures() {
		return FilterUtil.filterList(PROBLEM_FILTER, getResults());
	}

	@Override
	public Collection<? extends TaskResultWrapper> getSuccess() {
		return FilterUtil.filterList(NO_PROBLEM_FILTER, getResults());
	}

	@Override
	public void taskStarted() {
		TaskLogCommon.logTaskStarted(getNameInternal());
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			this::taskStartedInternal);
		if (!result.isSuccess()) {
			String message = "Committing the start of task '" + getNameInternal() + "' failed after "
				+ COMMIT_RETRIES + " attempts.";
			RuntimeException retryError = ExceptionUtil.createException(message, result.getReason());
			Logger.error(retryError.getMessage(), retryError, TaskLogWrapper.class);
		}
	}

	private RetryResult<Void, Throwable> taskStartedInternal() {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			setState(TaskState.RUNNING);
			createNotFinishedResult();
			transaction.commit();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	private TaskResultWrapper createNotFinishedResult() {
		return createTaskResult(
			ResultType.NOT_FINISHED, ResultType.NOT_FINISHED.getMessageI18N(), new Date(), null, null);
	}

	@Override
	public void taskEnded(ResultType type, ResKey message) {
		taskEnded(type, message, null);
	}

	@Override
	public void taskEnded(ResultType type, ResKey message, Throwable error) {
		TaskLogCommon.logTaskEnd(getNameInternal(), type, message, error);
		checkIsTaskEndResultType(type);
		commitTaskEnded(type, message, error);
		commitShrinkResultLists();
	}

	/**
	 * Tries to commit that the task died without writing an end result.
	 * <p>
	 * Tries to commit only once. If the commit fails, a {@link RuntimeException} is thrown.
	 * </p>
	 */
	@Override
	public void taskDied(ResKey messageKey) {
		TaskLogCommon.logTaskEnd(getNameInternal(), ResultType.ERROR, messageKey, null);
		RetryResult<Void, Throwable> result = taskEndedInternal(ResultType.ERROR, messageKey, null);
		if (!result.isSuccess()) {
			throw new RuntimeException(
				"Failed to commit that task '" + getNameInternal() + "' died.", result.getReason());
		}
	}

	private void commitShrinkResultLists() {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			this::tryCommitShrinkResultLists);
		if (!result.isSuccess()) {
			String message = "Shrinking the result lists for task '" + getNameInternal() + "' failed after "
				+ COMMIT_RETRIES + " attempts.";
			RuntimeException retryError = ExceptionUtil.createException(message, result.getReason());
			Logger.error(retryError.getMessage(), retryError, TaskLogWrapper.class);
		}
	}

	private RetryResult<Void, Throwable> tryCommitShrinkResultLists() {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			shrinkToMaxSize(FilterUtil.filterList(PROBLEM_FILTER, getOwnResults()), MAX_FAILURES);
			shrinkToMaxSize(FilterUtil.filterList(NO_PROBLEM_FILTER, getOwnResults()), MAX_SUCCESSES);
			transaction.commit();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	private void checkIsTaskEndResultType(ResultType type) {
		if (type.equals(ResultType.NOT_FINISHED) || type.equals(ResultType.UNKNOWN)) {
			throw new IllegalArgumentException("A " + Task.class.getSimpleName() + " cannot end in the state "
				+ ResultType.NOT_FINISHED + " or " + ResultType.UNKNOWN + ".");
		}
	}

	private void commitTaskEnded(
			final ResultType type, final ResKey message, final Throwable error) {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> taskEndedInternal(type, message, error));
		if (!result.isSuccess()) {
			String retryMessage =
				"Failed to commit end of task '" + getNameInternal() + "' after " + COMMIT_RETRIES + " attempts.";
			RuntimeException retryError = ExceptionUtil.createException(retryMessage, result.getReason());
			Logger.error(retryError.getMessage(), retryError, TaskLogWrapper.class);
		}
	}

	private RetryResult<Void, Throwable> taskEndedInternal(
			ResultType type, ResKey message, Throwable exception) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			Date end = new Date();
			TaskResultWrapper currentResult = findLatestResult();
			if (currentResult == null) {
				Logger.error("The task '" + getNameInternal() + "' is about to end, but there is no current result.",
					TaskLogWrapper.class);
				createTaskResult(type, message, end, end, exception);
			} else if (currentResult.getResultType() != ResultType.NOT_FINISHED) {
				Logger.error("The task '" + getNameInternal()
					+ "' is about to end, but the last result is not a 'currently running' result but: "
					+ currentResult.getResultType(), TaskLogWrapper.class);
				createTaskResult(type, message, end, end, exception);
			} else {
				currentResult.setResultType(type);
				currentResult.setMessage(message);
				currentResult.setEnd(end);
				currentResult.setException(exception);
			}
			setState(TaskState.INACTIVE);
			transaction.commit();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	private void shrinkToMaxSize(Collection<? extends TaskResultWrapper> taskResults, int maxSize) {
		List<TaskResultWrapper> excessResults = getExcessTaskResults(taskResults, maxSize);
		KBUtils.deleteAll(excessResults);
	}

	private List<TaskResultWrapper> getExcessTaskResults(
			Collection<? extends TaskResultWrapper> taskResults, int maxSize) {
		if (taskResults.size() <= maxSize) {
			return Collections.emptyList();
		}
		ArrayList<TaskResultWrapper> excess = new ArrayList<>(taskResults);
		Collections.sort(excess, Comparator.comparing(TaskResult::getStartDate).reversed());
		return excess.subList(maxSize, excess.size());
	}

	@Override
	public void taskCanceling() {
		TaskLogCommon.logTaskCanceling(getNameInternal());
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			this::taskCancelingInternal);
		if (!result.isSuccess()) {
			RuntimeException error = ExceptionUtil.createException("Failed to commit cancel request for task '"
				+ getNameInternal() + "' after " + COMMIT_RETRIES + " attempts.", result.getReason());
			Logger.error(error.getMessage(), error, TaskLogWrapper.class);
		}
	}

	private RetryResult<Void, Throwable> taskCancelingInternal() {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			touch();
			if (getState().equals(TaskState.RUNNING)) {
				setState(TaskState.CANCELING);
				transaction.commit();
			} else {
				transaction.rollback();
			}
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * This is the combined state on all cluster nodes, not just the local one.
	 * <p>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * <br/>
	 * <b>Internal method!</b>
	 * </p>
	 */
	public void setState(TaskState newState) {
		TaskState oldState = getState();
		setString(PROPERTY_STATE, newState.name());

		notifyListener(newState, oldState);
	}

	private synchronized void notifyListener(TaskState newState, TaskState oldState) {
		if (_events != null) {
			Task task = getTaskElseNull();
			if (task == null) {
				/* The Scheduler is not running. It is probably starting up or shutting down. In
				 * this state, events are not sent, as there are no 'Task' objects (yet), as their
				 * lifetime is bound to the Schedulers lifetime. */
				return;
			}
			TaskListenerNotification notification =
				new TaskListenerNotification(task, newState, oldState, getCurrentResult());
			_events.add(notification);
		}
	}

	/**
	 * Marks the task as running. Must only be called by schedulers for cluster synchronization and
	 * locking. Everyone else <b>must</b> use {@link #taskStarted()}.
	 * <p>
	 * This is the combined state on all cluster nodes, not just the local one. <br/>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * </p>
	 */
	public void markAsRunning() {
		setState(TaskState.RUNNING);
	}

	/**
	 * See: {@link #PROPERTY_CLUSTER_NAME}
	 * 
	 * @return Is <code>null</code> if the {@link Task} is not {@link Task#isNodeLocal() node local}
	 *         and not running.
	 */
	public String getClusterLockName() {
		return getString(PROPERTY_CLUSTER_NAME);
	}

	/**
	 * See: {@link #PROPERTY_CLUSTER_ID}
	 * 
	 * @return Is <code>null</code> if the {@link Task} is not {@link Task#isNodeLocal() node local}
	 *         and not running.
	 */
	public Long getClusterLockId() {
		return getLong(PROPERTY_CLUSTER_ID);
	}

	/**
	 * Sets {@link #getClusterLockName()} to {@link TaskCommon#getCurrentClusterNodeName()} and
	 * {@link #getClusterLockId()} to {@link TaskCommon#getCurrentClusterNodeId()}.
	 * <p>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * </p>
	 * 
	 * @see #getClusterLockName()
	 * @see #getClusterLockId()
	 */
	public void setClusterLock() {
		setString(PROPERTY_CLUSTER_NAME, getCurrentClusterNodeName());
		setLong(PROPERTY_CLUSTER_ID, getCurrentClusterNodeId());
	}

	/**
	 * Sets {@link #getClusterLockName()} and {@link #getClusterLockId()} to <code>null</code>.
	 * <p>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * </p>
	 * 
	 * @see #getClusterLockName()
	 * @see #getClusterLockId()
	 */
	public void clearClusterLock() {
		setString(PROPERTY_CLUSTER_NAME, null);
		setLong(PROPERTY_CLUSTER_ID, null);
	}

	/**
	 * Is the {@link #getClusterLockName()} and {@link #getClusterLockId()} set to the local cluster
	 * node?
	 */
	public boolean hasClusterLock() {
		String persistentLockName = getClusterLockName();
		String localLockName = getCurrentClusterNodeName();
		Long persistentLockId = getClusterLockId();
		Long localLockId = getCurrentClusterNodeId();
		boolean namesEqual = StringServices.equals(persistentLockName, localLockName);
		boolean idsEqual = Utils.equals(persistentLockId, localLockId);
		if (namesEqual != idsEqual) {
			String message = "Cluster lock values are out of sync:"
				+ " Name equality: " + namesEqual
				+ "; Id Equality: " + idsEqual + "; Persistent lock values: Name="
				+ StringServices.getObjectDescription(persistentLockName) + "; Id="
				+ StringServices.getObjectDescription(persistentLockId)
				+ "; Local lock values: Name="
				+ StringServices.getObjectDescription(localLockName) + "; Id="
				+ StringServices.getObjectDescription(localLockId);
			logError(message, new RuntimeException(message));
		}
		return namesEqual && idsEqual;
	}

	/**
	 * Is the {@link #getClusterLockName()} or {@link #getClusterLockId()} set? If not, no one has
	 * the lock.
	 */
	public boolean isClusterLockSet() {
		String persistentLockName = getClusterLockName();
		Long persistentLockId = getClusterLockId();
		boolean nameEmpty = StringServices.isEmpty(persistentLockName);
		boolean idEmpty = persistentLockId == null;
		if (nameEmpty != idEmpty) {
			String message = "Cluster lock values are out of sync:"
				+ " Name empty: " + nameEmpty
				+ "; Id empty: " + idEmpty + "; Lock values: Name="
				+ StringServices.getObjectDescription(persistentLockName) + "; Id="
				+ StringServices.getObjectDescription(persistentLockId);
			logError(message, new RuntimeException(message));
		}
		return !(nameEmpty && idEmpty);
	}

	/**
	 * Has to be called when this {@link TaskLog} is loaded on a cluster node startup, if the
	 * {@link Task} is {@link Task#isNodeLocal()} and {@link Task#isPersistent()}.
	 * <p>
	 * Searches for unfinished tasks of this node and marks them as failed. <br/>
	 * If the cleanup fails too often, this method gives up and informs the given {@link Scheduler}
	 * to retry that cleanup regularly. This handing over is necessary to prevent this method from
	 * blocking for too long.
	 * </p>
	 * 
	 * @param scheduler
	 *        If this startup cleanup fails, the given {@link Scheduler} will take care of it later.
	 *        Some tests do not start the {@link Scheduler} module, but use tasks. That would not be
	 *        possible, if this method would use {@link Scheduler#getSchedulerInstance()}.
	 *        Therefore, the {@link Scheduler} has to be passed as parameter, instead.
	 */
	public void startupNodeClean(Scheduler scheduler, final Task task) {
		RetryResult<Void, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			() -> tryStartupNodeClean(task));
		if (!result.isSuccess()) {
			RuntimeException error = ExceptionUtil.createException("Failed to commit startup node cleanup for task '"
				+ getNameInternal() + "' after " + COMMIT_RETRIES + " attempts.", result.getReason());
			Logger.error(error.getMessage(), error, TaskLogWrapper.class);
			scheduler.addTaskLogToFix(getNameInternal());
		}
	}

	/** Tries to do the {@link #startupNodeClean(Scheduler, Task)}, but tries only once. */
	public RetryResult<Void, Throwable> tryStartupNodeClean(Task task) {
		checkCorrectTask(task);
		if (task.isNodeLocal()) {
			return startupNodeCleanLocal(task);
		} else {
			return startupNodeCleanGlobal(task);
		}
	}

	private RetryResult<Void, Throwable> startupNodeCleanLocal(Task task) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			// Update the node id, which changes with every startup.
			setClusterLock();
			// Cleanup in case the task is still marked as active.
			forceUncheckedMarkTaskAsInactive(task, I18NConstants.TASK_ACTIVE_ON_SHUTDOWN);
			/* Committing the lock even though nothing else has been changed creates an unnecessary
			 * row in the database. This row could be avoided, by rolling back the transaction if
			 * nothing has been changed. But that is not possible here: For every configured or
			 * persisted task, this code is executed during application startup. But there is an
			 * outer transaction encapsulating the whole application startup. A rollback of the
			 * inner transaction would cause that outer transaction to fail, which would in turn
			 * cause the application startup to fail. Therefore, this code creates an unnecessary
			 * row in the database for every application startup. */
			transaction.commit();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	private RetryResult<Void, Throwable> startupNodeCleanGlobal(Task task) {
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			/* This transaction is nested in the system startup transaction. If an object is changed
			 * in this transaction and that is rolled back due to a concurrent commit from another
			 * cluster node, the whole system startup transaction fails, causing the system startup
			 * itself to fail. Therefore, don't lock (i.e. touch) any persistent objects, unless it
			 * is sure that nobody else is currently changing that object. */
			if (hasClusterLock()) {
				forceUncheckedMarkTaskAsInactive(task, I18NConstants.TASK_ACTIVE_ON_SHUTDOWN);
			}
			/* See startupNodeCleanLocal() for why this commit is always necessary, even if nothing
			 * has been changed. */
			transaction.commit();
			return RetryResult.createSuccess();
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Void, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * Mark the {@link Task} as inactive without any checks.
	 * <p>
	 * <b>Framework internal API.</b><br/>
	 * This method releases the cluster lock, sets the {@link #getState()} to
	 * {@link TaskState#INACTIVE} and marks all active {@link TaskResult}s as
	 * {@link ResultType#ERROR}, all without any checks. It is meant as explicit override for error
	 * situations. <br/>
	 * <b>Does not commit the changes!</b> The caller has to take care of the transaction handling!
	 * </p>
	 */
	public void forceUncheckedMarkTaskAsInactive(Task task, ResKey taskResultI18n) {
		checkCorrectTask(task);
		/* By checking before locking, the number of locks (and therefore changes that will be
		 * committed) is drastically reduced. By checking again after locking, race conditions are
		 * prevented. */
		if ((!task.isNodeLocal()) && isClusterLockSet()) {
			touch();
			if (isClusterLockSet()) {
				logErroneousLock();
				clearClusterLock();
			}
		}
		if (getState() != TaskState.INACTIVE) {
			touch();
			if (getState() != TaskState.INACTIVE) {
				logErroneousState();
				setState(TaskState.INACTIVE);
			}
		}
		for (TaskResultWrapper result : getOwnResults()) {
			if (isInWrongState(result)) {
				result.touch();
				if (isInWrongState(result)) {
					logErroneousResult(result);
					fixWrongState(result, taskResultI18n);
				}
			}
		}
	}

	private void logErroneousLock() {
		String message = "TaskLog.lock is erroneously set to name='" + getClusterLockName() + "', id="
			+ getClusterLockId() + " and is deleted.";
		logWarning(message, new IllegalStateException(message));
	}

	private void logErroneousState() {
		String message =
			"TaskLog.state is erroneously set to " + getState() + " and is reset to " + TaskState.INACTIVE + ".";
		logWarning(message, new IllegalStateException(message));
	}

	private void logErroneousResult(TaskResultWrapper result) {
		String message = "TaskResult is erroneously still active and is marked as ended. Result: " + result;
		logWarning(message, new IllegalStateException(message));
	}

	private boolean isInWrongState(TaskResultWrapper result) {
		return (result.getResultType() == ResultType.NOT_FINISHED)
			|| (result.getResultType() == ResultType.UNKNOWN);
	}

	private void fixWrongState(TaskResultWrapper result, ResKey taskResultI18n) {
		result.setResultType(ResultType.ERROR);
		result.setMessage(taskResultI18n);
		result.setEnd(new Date());
	}

	private void checkCorrectTask(Task task) {
		if (!StringServices.equals(task.getName(), getNameInternal())) {
			throw new IllegalArgumentException("Expected the Task named '" + getNameInternal()
				+ "' but got a Task named '" + task.getName() + "'. Task: " + task);
		}
	}

	/** Log a message on level 'debug'. */
	protected void logDebug(String message) {
		if (Logger.isDebugEnabled(TaskLogWrapper.class)) {
			Logger.debug(getLogMessagePrefix() + message, TaskLogWrapper.class);
		}
	}

	/** Log a message on level 'info'. */
	protected void logInfo(String message) {
		Logger.info(getLogMessagePrefix() + message, TaskLogWrapper.class);
	}

	/** Log a message on level 'warning'. */
	protected void logWarning(String message) {
		Logger.warn(getLogMessagePrefix() + message, TaskLogWrapper.class);
	}

	/** Log a message on level 'warning'. */
	protected void logWarning(String message, Throwable exception) {
		Logger.warn(getLogMessagePrefix() + message, exception, TaskLogWrapper.class);
	}

	/** Log a message on level 'error'. */
	protected void logError(String message, Throwable exception) {
		Logger.error(getLogMessagePrefix() + message, exception, TaskLogWrapper.class);
	}

	/**
	 * The common prefix for all messages logged by this class.
	 * <p>
	 * Includes the name of the task and the name of this cluster node.
	 * </p>
	 */
	protected String getLogMessagePrefix() {
		return "Message from: '" + this + "'; This Cluster Node Name: '" + getCurrentClusterNodeName()
			+ "'; This Cluster Node Id: '" + getCurrentClusterNodeId() + "'; Message: ";
	}

	@Override
	protected String toStringValues() {
		return new StringBuilder()
			.append(", ").append(NAME_ATTRIBUTE).append(": ").append(getNameInternal())
			.append(", ").append(PROPERTY_CLUSTER_NAME).append(": ").append(getClusterLockName())
			.append(", ").append(PROPERTY_CLUSTER_ID).append(": ").append(getClusterLockId())
			.append(", ").append(PROPERTY_STATE).append(": ").append(getState())
			.toString();
	}

}
