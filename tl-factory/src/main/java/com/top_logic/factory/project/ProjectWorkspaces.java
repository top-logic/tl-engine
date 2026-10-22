/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Service running the operations on the workspaces of projects in the background and keeping the
 * {@link FactoryModel#WORKSPACE workspace} objects up to date with them.
 *
 * <p>
 * Creating, starting, stopping and removing a workspace takes from seconds to minutes. An
 * operation is therefore started from a request, which only stores the state the operation is in
 * (e.g. {@link WorkspaceStatus#BUILDING}), and runs on a worker thread of this service in a system
 * context. The output of the operation is collected in a {@link WorkspaceLog}, which stores it
 * every {@link Config#getLogFlushInterval() flush interval} in a transaction of its own; the final
 * state is stored when the operation ends. A view following these commits displays the progress
 * while the operation runs.
 * </p>
 *
 * <p>
 * Only one operation runs on a workspace at a time. After a restart, the service compares the
 * stored states with the containers that exist, see {@link WorkspaceReconciliation}. It does that
 * on a worker thread as well, so that the application starts also when the container platform is
 * not reachable.
 * </p>
 */
@ServiceDependencies({
	WorkspaceService.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
	SchedulerService.Module.class,
})
public class ProjectWorkspaces extends ConfiguredManagedClass<ProjectWorkspaces.Config<?>> {

	/**
	 * Configuration of {@link ProjectWorkspaces}.
	 */
	public interface Config<I extends ProjectWorkspaces> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getLogFlushInterval()}.
		 */
		String LOG_FLUSH_INTERVAL = "log-flush-interval";

		/**
		 * Configuration property name of {@link #getMaxLogSize()}.
		 */
		String MAX_LOG_SIZE = "max-log-size";

		/**
		 * Milliseconds between two commits of the output of a running operation.
		 *
		 * @see #LOG_FLUSH_INTERVAL
		 */
		@Name(LOG_FLUSH_INTERVAL)
		@IntDefault(1000)
		int getLogFlushInterval();

		/**
		 * Number of characters of the output of an operation that are stored; older output is
		 * dropped.
		 *
		 * @see #MAX_LOG_SIZE
		 */
		@Name(MAX_LOG_SIZE)
		@IntDefault(64 * 1024)
		int getMaxLogSize();

	}

	/**
	 * Allowed Maven group of an application.
	 */
	private static final Pattern GROUP_ID = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*");

	/**
	 * Allowed Maven artifact of an application, which is also the context path of the
	 * application and the start of the workspace identifier.
	 */
	private static final Pattern ARTIFACT_ID = Pattern.compile("[a-z][a-z0-9-]*");

	private static final String WORKSPACE_ID_SEPARATOR = "-";

	private static final int WORKSPACE_ID_SUFFIX_LENGTH = 8;

	private final Set<String> _busy = ConcurrentHashMap.newKeySet();

	private ExecutorService _workers;

	/**
	 * Creates a {@link ProjectWorkspaces} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ProjectWorkspaces(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_workers = Executors.newCachedThreadPool(DaemonThreadFactory.daemonThreadFactory());
		_workers.execute(() -> ThreadContext.inSystemContext(ProjectWorkspaces.class, this::reconcile));
	}

	@Override
	protected void shutDown() {
		_workers.shutdownNow();
		_workers = null;
		super.shutDown();
	}

	private static ApplicationWorkspaces applications() {
		return WorkspaceService.getInstance().getApplications();
	}

	/**
	 * Creates the workspace of a project and builds and starts its application in the background.
	 *
	 * <p>
	 * Must be called outside of a transaction: the workspace is committed in state
	 * {@link WorkspaceStatus#BUILDING} before the operation starts.
	 * </p>
	 *
	 * @param project
	 *        A project without workspace.
	 * @return The workspace.
	 */
	public TLObject create(TLObject project) {
		if (FactoryModel.getWorkspace(project) != null) {
			throw new TopLogicException(I18NConstants.ERROR_HAS_WORKSPACE__PROJECT.fill(project));
		}
		String groupId = FactoryModel.getString(project, FactoryModel.PROJECT_GROUP_ID);
		String artifactId = FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID);
		String version = FactoryModel.getString(project, FactoryModel.PROJECT_ENGINE_VERSION);
		ResKey groupIdError = checkGroupId(groupId);
		if (groupIdError != null) {
			throw new TopLogicException(groupIdError);
		}
		ResKey artifactIdError = checkArtifactId(artifactId);
		if (artifactIdError != null) {
			throw new TopLogicException(artifactIdError);
		}

		String workspaceId = artifactId + WORKSPACE_ID_SEPARATOR
			+ UUID.randomUUID().toString().substring(0, WORKSPACE_ID_SUFFIX_LENGTH);
		if (!_busy.add(workspaceId)) {
			throw new TopLogicException(I18NConstants.ERROR_WORKSPACE_BUSY__PROJECT.fill(project));
		}
		TLObject workspace;
		try {
			workspace = commitInRequest(
				() -> FactoryModel.createWorkspace(project, workspaceId, WorkspaceStatus.BUILDING));
		} catch (RuntimeException ex) {
			_busy.remove(workspaceId);
			throw ex;
		}
		ApplicationSpec spec = new ApplicationSpec(workspaceId, groupId, artifactId).setEngineVersion(version);
		run(workspace, "Creating application " + groupId + ":" + artifactId + " " + version + ".", log -> {
			WorkspaceInfo info = applications().create(spec, log);
			log.update(w -> {
				FactoryModel.setStatus(w, WorkspaceStatus.STARTING);
				FactoryModel.setEndpoint(w, port(info), null);
			});
			awaitApplication(log, workspaceId, artifactId);
		});
		return workspace;
	}

	/**
	 * Checks the Maven group of an application.
	 *
	 * @return The problem with the given value, {@code null} if it is valid.
	 */
	public static ResKey checkGroupId(String groupId) {
		if (groupId == null || !GROUP_ID.matcher(groupId).matches()) {
			return I18NConstants.ERROR_INVALID_GROUP_ID__VALUE.fill(groupId);
		}
		return null;
	}

	/**
	 * Checks the Maven artifact of an application.
	 *
	 * @return The problem with the given value, {@code null} if it is valid.
	 */
	public static ResKey checkArtifactId(String artifactId) {
		if (artifactId == null || !ARTIFACT_ID.matcher(artifactId).matches()) {
			return I18NConstants.ERROR_INVALID_ARTIFACT_ID__VALUE.fill(artifactId);
		}
		return null;
	}

	/**
	 * Starts the workspace of a project in the background and waits until its application
	 * answers.
	 *
	 * @param project
	 *        A project whose workspace is {@link WorkspaceStatus#STOPPED},
	 *        {@link WorkspaceStatus#CREATED} or {@link WorkspaceStatus#UNKNOWN}.
	 */
	public void start(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.STARTING, WorkspaceStatus.STOPPED, WorkspaceStatus.CREATED,
			WorkspaceStatus.UNKNOWN);
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		String artifactId = FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID);
		run(workspace, "Starting the workspace.", log -> {
			WorkspaceInfo info = applications().start(workspaceId);
			log.update(w -> FactoryModel.setEndpoint(w, port(info), null));
			awaitApplication(log, workspaceId, artifactId);
		});
	}

	/**
	 * Stops the workspace of a project in the background.
	 *
	 * @param project
	 *        A project whose workspace is {@link WorkspaceStatus#RUNNING}, or whose container may
	 *        run: {@link WorkspaceStatus#UNKNOWN} or {@link WorkspaceStatus#ERROR}.
	 */
	public void stop(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.STOPPING, WorkspaceStatus.RUNNING, WorkspaceStatus.UNKNOWN,
			WorkspaceStatus.ERROR);
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		run(workspace, "Stopping the workspace.", log -> {
			applications().stop(workspaceId);
			log.line("Workspace stopped.");
			log.update(w -> {
				FactoryModel.setStatus(w, WorkspaceStatus.STOPPED);
				FactoryModel.setEndpoint(w, null, null);
			});
		});
	}

	/**
	 * Removes the container and the volumes of the workspace of a project in the background, and
	 * then the workspace itself; the project is kept.
	 *
	 * @param project
	 *        A project with a workspace on which no operation runs.
	 */
	public void cleanup(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.REMOVING, WorkspaceStatus.values());
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		run(workspace, "Removing the workspace.", log -> {
			WorkspaceInfo info = applications().getProvider().status(workspaceId);
			if (info.getState() != WorkspaceState.MISSING || !info.getVolumes().isEmpty()) {
				applications().remove(workspaceId);
			}
			log.line("Workspace removed.");
			log.update(TLObject::tDelete);
		});
	}

	/**
	 * Commits the state an operation is in, if the workspace of the given project is in one of
	 * the given states and no other operation runs on it.
	 */
	private TLObject begin(TLObject project, WorkspaceStatus during, WorkspaceStatus... allowed) {
		TLObject workspace = FactoryModel.getWorkspace(project);
		if (workspace == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_WORKSPACE__PROJECT.fill(project));
		}
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		if (!_busy.add(workspaceId)) {
			throw new TopLogicException(I18NConstants.ERROR_WORKSPACE_BUSY__PROJECT.fill(project));
		}
		try {
			WorkspaceStatus status = FactoryModel.getStatus(workspace);
			boolean ok = false;
			for (WorkspaceStatus candidate : allowed) {
				ok |= candidate == status && !candidate.isBusy();
			}
			if (!ok) {
				throw new TopLogicException(
					I18NConstants.ERROR_INVALID_STATE__PROJECT_STATE.fill(project, FactoryModel.classifier(status)));
			}
			commitInRequest(() -> {
				FactoryModel.setStatus(workspace, during);
				return null;
			});
			return workspace;
		} catch (RuntimeException ex) {
			_busy.remove(workspaceId);
			throw ex;
		}
	}

	/**
	 * Runs an operation on a worker thread, storing its output, and ends it with state
	 * {@link WorkspaceStatus#ERROR} when it fails.
	 */
	private void run(TLObject workspace, String title, Operation operation) {
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		WorkspaceLog log = new WorkspaceLog(workspace, getConfig().getMaxLogSize(), ProjectWorkspaces::commit);
		try {
			// No system context around the whole operation: it would pin the operation to the revision
			// it started in, and its commits would conflict with those of the periodic flush. Every
			// commit opens a system context of its own, see commit(Runnable).
			_workers.execute(() -> {
				long interval = getConfig().getLogFlushInterval();
				ScheduledFuture<?> flush = SchedulerService.getInstance()
					.scheduleWithFixedDelay(() -> flushQuietly(log, workspaceId), interval, interval, TimeUnit.MILLISECONDS);
				try {
					log.line(title);
					log.flush();
					operation.run(log);
				} catch (InterruptedException ex) {
					fail(log, workspaceId, "Interrupted.");
				} catch (RuntimeException | Error ex) {
					Logger.error("Operation on workspace " + workspaceId + " failed.", ex, ProjectWorkspaces.class);
					fail(log, workspaceId, message(ex));
				} finally {
					flush.cancel(false);
					_busy.remove(workspaceId);
				}
			});
		} catch (RuntimeException ex) {
			_busy.remove(workspaceId);
			throw ex;
		}
	}

	private void awaitApplication(WorkspaceLog log, String workspaceId, String artifactId)
			throws InterruptedException {
		log.line("Waiting for the application to answer.");
		log.flush();
		String url = applications().awaitApplication(workspaceId, artifactId);
		WorkspaceInfo info = applications().getProvider().status(workspaceId);
		log.line("Application running at " + url);
		log.update(w -> {
			FactoryModel.setStatus(w, WorkspaceStatus.RUNNING);
			FactoryModel.setEndpoint(w, port(info), url);
		});
	}

	private static void fail(WorkspaceLog log, String workspaceId, String message) {
		log.line("Failed: " + message);
		try {
			log.update(w -> {
				FactoryModel.setStatus(w, WorkspaceStatus.ERROR);
				FactoryModel.setEndpoint(w, null, null);
			});
		} catch (RuntimeException ex) {
			Logger.error("Failed to store the error of workspace " + workspaceId + ".", ex, ProjectWorkspaces.class);
		}
	}

	private static void flushQuietly(WorkspaceLog log, String workspaceId) {
		try {
			log.flush();
		} catch (RuntimeException ex) {
			Logger.error("Failed to store the output of workspace " + workspaceId + ".", ex, ProjectWorkspaces.class);
		}
	}

	private static String message(Throwable ex) {
		String message = ex.getMessage();
		return message == null || message.isEmpty() ? ex.getClass().getName() : message;
	}

	private static Integer port(WorkspaceInfo info) {
		return info.hasHostPort() ? Integer.valueOf(info.getHostPort()) : null;
	}

	/**
	 * Compares the stored states of all workspaces with the containers that exist and stores the
	 * corrected states.
	 */
	void reconcile() {
		Map<String, TLObject> workspaces = new LinkedHashMap<>();
		Map<String, WorkspaceStatus> stored = new LinkedHashMap<>();
		for (TLObject workspace : FactoryModel.allWorkspaces()) {
			String id = FactoryModel.getWorkspaceId(workspace);
			if (_busy.add(id)) {
				workspaces.put(id, workspace);
				stored.put(id, FactoryModel.getStatus(workspace));
			}
		}
		if (workspaces.isEmpty()) {
			return;
		}
		try {
			Map<String, WorkspaceReconciliation.Outcome> outcomes =
				new WorkspaceReconciliation(applications().getProvider(), ProjectWorkspaces::applicationUrl)
					.reconcile(stored);
			commit(() -> {
				for (Map.Entry<String, WorkspaceReconciliation.Outcome> entry : outcomes.entrySet()) {
					TLObject workspace = workspaces.get(entry.getKey());
					WorkspaceReconciliation.Outcome outcome = entry.getValue();
					FactoryModel.setStatus(workspace, outcome.status());
					FactoryModel.setEndpoint(workspace, outcome.hostPort(), outcome.previewUrl());
					if (outcome.message() != null) {
						String log = FactoryModel.getString(workspace, FactoryModel.WORKSPACE_LOG);
						workspace.tUpdateByName(FactoryModel.WORKSPACE_LOG,
							(log == null || log.isEmpty() || log.endsWith("\n") ? nonNull(log) : log + "\n")
								+ outcome.message() + "\n");
					}
				}
			});
			Logger.info("Reconciled the states of " + outcomes.size() + " workspaces.", ProjectWorkspaces.class);
		} catch (RuntimeException ex) {
			Logger.error("Failed to reconcile the states of the workspaces.", ex, ProjectWorkspaces.class);
		} finally {
			_busy.removeAll(workspaces.keySet());
		}
	}

	private static String nonNull(String text) {
		return text == null ? "" : text;
	}

	private static String applicationUrl(WorkspaceInfo info) {
		String id = info.getWorkspaceId();
		for (TLObject workspace : FactoryModel.allWorkspaces()) {
			if (id.equals(FactoryModel.getWorkspaceId(workspace))) {
				String artifactId =
					FactoryModel.getString(FactoryModel.getProject(workspace), FactoryModel.PROJECT_ARTIFACT_ID);
				return ApplicationWorkspaces.applicationUrl(info, artifactId);
			}
		}
		return info.getPreviewUrl();
	}

	/**
	 * Runs the given change in a transaction of its own, in the context of the current request.
	 */
	private static <T> T commitInRequest(Supplier<T> change) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(I18NConstants.COMMIT_WORKSPACE)) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * Runs the given change in a transaction of its own, in a system context.
	 */
	static void commit(Runnable change) {
		ThreadContext.inSystemContext(ProjectWorkspaces.class, () -> {
			commitInRequest(() -> {
				change.run();
				return null;
			});
		});
	}

	/**
	 * An operation on a workspace, run on a worker thread.
	 */
	private interface Operation {

		/**
		 * Runs the operation, writing its output and its changes of the workspace to the given
		 * log.
		 */
		void run(WorkspaceLog log) throws InterruptedException;

	}

	/**
	 * The {@link ProjectWorkspaces} of the running application.
	 */
	public static ProjectWorkspaces getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module of the {@link ProjectWorkspaces}.
	 */
	public static final class Module extends TypedRuntimeModule<ProjectWorkspaces> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<ProjectWorkspaces> getImplementation() {
			return ProjectWorkspaces.class;
		}
	}

}
