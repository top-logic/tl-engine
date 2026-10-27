/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.LinkedHashMap;
import java.util.List;
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

import javax.lang.model.SourceVersion;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.DevelopmentCategory;
import com.top_logic.basic.sched.DaemonThreadFactory;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.WorkspaceAgentService;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIPipeline;
import com.top_logic.factory.ci.CIProvider;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.ci.ProjectCI;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.RepositorySource;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationFailedException;
import com.top_logic.factory.workspace.app.ApplicationSource;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces.ApplicationState;
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
	SecureRandomService.Module.class,
	WorkspaceProxyService.Module.class,
	GitHostingService.Module.class,
	CIService.Module.class,
})
@ServiceCategory.Of(DevelopmentCategory.class)
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
		 * Configuration property name of {@link #getApplicationCheckInterval()}.
		 */
		String APPLICATION_CHECK_INTERVAL = "application-check-interval";

		/**
		 * Milliseconds between two checks of the applications of the workspaces whose container
		 * runs, {@code 0} for no checks.
		 *
		 * <p>
		 * The application of a running container may stop or start without an operation of the
		 * control application, e.g. when the agent of the workspace restarts it. The check
		 * stores {@link WorkspaceStatus#APP_STOPPED} for a {@link WorkspaceStatus#RUNNING}
		 * workspace whose application stopped or failed, and {@link WorkspaceStatus#RUNNING} for
		 * an {@link WorkspaceStatus#APP_STOPPED} one whose application answers again.
		 * </p>
		 *
		 * @see #APPLICATION_CHECK_INTERVAL
		 */
		@Name(APPLICATION_CHECK_INTERVAL)
		@IntDefault(10000)
		int getApplicationCheckInterval();

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

	/**
	 * Separator of the names of a Maven group, which are the names of the Java package of an
	 * application.
	 */
	private static final String GROUP_ID_SEPARATOR = ".";

	/**
	 * Separator of the parts of a Maven artifact, each of which becomes a name of the Java package
	 * of an application.
	 */
	private static final String ARTIFACT_ID_SEPARATOR = "-";

	private static final String WORKSPACE_ID_SEPARATOR = "-";

	private static final int WORKSPACE_ID_SUFFIX_LENGTH = 8;

	private final Set<String> _busy = ConcurrentHashMap.newKeySet();

	private ExecutorService _workers;

	private ScheduledFuture<?> _applicationCheck;

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
		long interval = getConfig().getApplicationCheckInterval();
		if (interval > 0) {
			_applicationCheck = SchedulerService.getInstance().scheduleWithFixedDelay(
				() -> ThreadContext.inSystemContext(ProjectWorkspaces.class, this::checkApplications), interval, interval,
				TimeUnit.MILLISECONDS);
		}
	}

	@Override
	protected void shutDown() {
		if (_applicationCheck != null) {
			_applicationCheck.cancel(false);
			_applicationCheck = null;
		}
		_workers.shutdownNow();
		_workers = null;
		super.shutDown();
	}

	private static ApplicationWorkspaces applications() {
		return WorkspaceService.getInstance().getApplications();
	}

	/**
	 * The workspaces the operations of this service work on.
	 *
	 * @implSpec The applications of the {@link WorkspaceService}.
	 */
	protected ApplicationWorkspaces getApplications() {
		return applications();
	}

	/**
	 * Creates the workspace of a project and builds and starts its application in the background.
	 *
	 * <p>
	 * Must be called outside of a transaction: the workspace is committed in state
	 * {@link WorkspaceStatus#BUILDING} before the operation starts.
	 * </p>
	 *
	 * <p>
	 * The workspace gets a random {@link FactoryModel#WORKSPACE_INITIAL_PASSWORD initial password},
	 * which the application in it sets for its account {@code root}. If the
	 * {@link WorkspaceProxyService workspace proxy} is switched on, the container gets what its
	 * routes need to find it, e.g. their URLs, in its environment.
	 * </p>
	 *
	 * <p>
	 * With {@link GitHostingService git hosting}, the sources live in the
	 * {@link FactoryModel#PROJECT_REPOSITORY repository of the project} (see
	 * {@link RepositorySource}): a project without repository gets one, created with the generated
	 * application as its first commit; the repository of a project that has one - entered when the
	 * project was created, or created for an earlier workspace - is cloned. Without git hosting,
	 * the application is generated in the workspace only.
	 * </p>
	 *
	 * <p>
	 * With {@link CIService CI} in addition, a new application gets the pipeline definition in its
	 * first commit, and the CI job of the repository is set up and builds the application once the
	 * branch is protected (see {@link CIPipeline}); a failure of the CI server is only a warning in
	 * the output of the operation.
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
		NewWorkspace created;
		try {
			created = commitNewWorkspace(project, workspaceId);
		} catch (RuntimeException ex) {
			_busy.remove(workspaceId);
			throw ex;
		}
		TLObject workspace = created.workspace();
		ApplicationSpec spec = created.spec();
		ApplicationSource source = applicationSource(project);
		if (source != null) {
			spec.setSource(source);
		}
		run(workspace, "Creating the workspace of application " + groupId + ":" + artifactId + " " + version + ".", log -> {
			WorkspaceInfo info = getApplications().create(spec, log);
			log.update(w -> {
				w.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, info.getImage());
				FactoryModel.setStatus(w, WorkspaceStatus.STARTING);
				FactoryModel.setEndpoint(w, port(info), null);
			});
			awaitApplication(log, workspaceId, artifactId);
		});
		return workspace;
	}

	/**
	 * Where the sources of the application of the given project come from.
	 *
	 * @return The {@link RepositorySource} of the repository of the project if git hosting is
	 *         {@link #getGitHosting() switched on}, {@code null} for the default, the archetype.
	 */
	protected ApplicationSource applicationSource(TLObject project) {
		GitHostingProvider hosting = getGitHosting();
		if (hosting == null) {
			return null;
		}
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		GitRoute route = proxy == null ? null : proxy.getRoute(GitRoute.class);
		String routeUrl = route == null ? null : proxy.getRouteUrl(route);
		CIProvider ci = getCI();
		String name = FactoryModel.getString(project, FactoryModel.PROJECT_NAME);
		CIPipeline pipeline = ci == null ? null
			: new CIPipeline(ci, pipelineTemplate(), name, job -> commit(() -> FactoryModel.setCIJob(project, job)));
		return new RepositorySource(hosting, proxy, routeUrl,
			FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY), name,
			ProjectCI.withPipelineFile(getConfiguredProtectedFiles(), ci),
			repository -> commit(() -> FactoryModel.setRepository(project, repository)), pipeline);
	}

	/**
	 * The CI server building the applications of the projects.
	 *
	 * @implSpec The provider of the {@link CIService}.
	 *
	 * @return The provider, {@code null} if CI is switched off.
	 */
	protected CIProvider getCI() {
		return CIService.configuredProvider();
	}

	/**
	 * Whether the projects have CI jobs.
	 */
	public boolean isCIEnabled() {
		return getCI() != null;
	}

	/**
	 * The pipeline definition of a new application.
	 *
	 * @see CIService#pipelineTemplate()
	 */
	private static String pipelineTemplate() {
		if (!CIService.Module.INSTANCE.isActive()) {
			return CIService.readResource(CIService.DEFAULT_PIPELINE_TEMPLATE);
		}
		return CIService.getInstance().pipelineTemplate();
	}

	/**
	 * Why no CI build of the given project can be started now.
	 *
	 * @return The reason, {@code null} if a build can be started.
	 * @see ProjectCI#unavailable(CIProvider, GitHostingProvider, TLObject)
	 */
	public ResKey ciUnavailable(TLObject project) {
		return ProjectCI.unavailable(getCI(), getGitHosting(), project);
	}

	/**
	 * Makes sure the CI job of the given project exists and builds its repository, records it on
	 * the project in a transaction of its own, and starts a build.
	 *
	 * <p>
	 * Must be called outside of a transaction.
	 * </p>
	 *
	 * @param project
	 *        A project with repository.
	 * @return The started build.
	 * @see ProjectCI#startBuild(CIProvider, GitHostingProvider, TLObject, java.util.function.Consumer)
	 */
	public CIBuildStart startBuild(TLObject project) {
		return ProjectCI.startBuild(getCI(), getGitHosting(), project, job -> commitInRequest(() -> {
			FactoryModel.setCIJob(project, job);
			return null;
		}));
	}

	/**
	 * The last build of the CI job of the given project.
	 *
	 * @return The build, {@code null} if CI is switched off, the project has no CI job or the job
	 *         has no build.
	 * @see ProjectCI#lastBuild(CIProvider, TLObject)
	 */
	public CIBuild lastBuild(TLObject project) {
		return ProjectCI.lastBuild(getCI(), project);
	}

	/**
	 * The git hosting service the repositories of the projects are on.
	 *
	 * @implSpec The provider of the {@link GitHostingService}.
	 *
	 * @return The provider, {@code null} if git hosting is switched off.
	 */
	protected GitHostingProvider getGitHosting() {
		if (!GitHostingService.Module.INSTANCE.isActive()) {
			return null;
		}
		GitHostingService service = GitHostingService.getInstance();
		return service.isEnabled() ? service.getProvider() : null;
	}

	/**
	 * Whether the projects have repositories on a git hosting service.
	 */
	public boolean isGitHostingEnabled() {
		return getGitHosting() != null;
	}

	/**
	 * The protected files of the default branch of a project repository: the configured ones and,
	 * with {@link CIService CI}, the {@link CIProvider#pipelineFile() pipeline file}.
	 *
	 * @see GitHostingService#getProtectedFiles()
	 */
	public static List<String> getProtectedFiles() {
		return ProjectCI.withPipelineFile(getConfiguredProtectedFiles(), CIService.configuredProvider());
	}

	/**
	 * The protected files configured for the default branch of a project repository.
	 *
	 * @see GitHostingService#getProtectedFiles()
	 */
	private static List<String> getConfiguredProtectedFiles() {
		if (!GitHostingService.Module.INSTANCE.isActive()) {
			return TypedConfiguration.newConfigItem(GitHostingService.Config.class).getProtectedFiles();
		}
		return GitHostingService.getInstance().getProtectedFiles();
	}

	/**
	 * Checks the existing repository entered for a new project.
	 *
	 * <p>
	 * A repository the project has recorded - with the {@link FactoryModel#PROJECT_REPOSITORY_URL
	 * page} the hosting service reported for it - is not checked again: the hosting service is not
	 * asked when the recorded repository is stored.
	 * </p>
	 *
	 * @param project
	 *        The project, {@code null} for none.
	 * @param reference
	 *        The repository as the user enters it: its full name {@code owner/name}, its clone
	 *        URL or the URL of one of its pages; empty for a repository to create.
	 * @return The problem with the reference, {@code null} if it is empty, names an existing
	 *         repository, is recorded, or git hosting is switched off.
	 */
	public ResKey checkRepository(TLObject project, String reference) {
		if (project != null && FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY_URL) != null) {
			return null;
		}
		GitHostingProvider hosting = getGitHosting();
		return hosting == null ? null : RepositorySource.checkReference(hosting, reference);
	}

	/**
	 * A workspace that is stored and the description of the application to create in it.
	 *
	 * @param workspace
	 *        The stored workspace.
	 * @param spec
	 *        The application to create in the container of the workspace, including the
	 *        environment the container gets.
	 */
	public record NewWorkspace(TLObject workspace, ApplicationSpec spec) {
		@Override
		public String toString() {
			// The environment of the spec holds secrets.
			return "NewWorkspace[" + workspace + "]";
		}
	}

	/**
	 * Stores a new workspace of a project in state {@link WorkspaceStatus#BUILDING}, in a
	 * transaction of its own, without creating its container.
	 *
	 * <p>
	 * The workspace gets a random {@link FactoryModel#WORKSPACE_INITIAL_PASSWORD initial password}.
	 * The password and, if the {@link WorkspaceProxyService workspace proxy} is switched on, the
	 * URLs of its routes are in the environment of the returned {@link NewWorkspace#spec() spec}.
	 * </p>
	 *
	 * @param project
	 *        A project without workspace whose group, artifact and engine version are valid.
	 * @param workspaceId
	 *        The identifier of the container of the workspace.
	 */
	public static NewWorkspace commitNewWorkspace(TLObject project, String workspaceId) {
		String initialPassword = SecureRandomService.getInstance().getRandomString();
		TLObject workspace = commitInRequest(
			() -> FactoryModel.createWorkspace(project, workspaceId, initialPassword, WorkspaceStatus.BUILDING));
		return new NewWorkspace(workspace, applicationSpec(project, workspaceId, initialPassword));
	}

	/**
	 * The description of the application of a workspace, with the environment its container gets.
	 */
	private static ApplicationSpec applicationSpec(TLObject project, String workspaceId, String initialPassword) {
		ApplicationSpec spec = new ApplicationSpec(workspaceId,
			FactoryModel.getString(project, FactoryModel.PROJECT_GROUP_ID),
			FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID))
			.setEngineVersion(FactoryModel.getString(project, FactoryModel.PROJECT_ENGINE_VERSION))
			.setInitialPassword(initialPassword);
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		if (proxy != null) {
			spec.putEnv(proxy.containerEnv());
		}
		return spec;
	}

	/**
	 * Creates the container of the workspace of a project anew on the current workspace image in
	 * the background, keeping its sources, its Maven repository and its database.
	 *
	 * <p>
	 * The container gets the {@link FactoryModel#WORKSPACE_INITIAL_PASSWORD initial password} of
	 * the workspace and, if the {@link WorkspaceProxyService workspace proxy} is switched on, the
	 * current URLs of its routes.
	 * A workspace that was running is started again and waited for like by
	 * {@link #start(TLObject)}.
	 * </p>
	 *
	 * @param project
	 *        A project with a workspace on which no operation runs.
	 * @see #isImageOutdated(TLObject)
	 */
	public void update(TLObject project) {
		TLObject existing = FactoryModel.getWorkspace(project);
		// Prepared before the state is stored, so that a failure leaves the workspace as it is.
		WorkspaceUpdate update = existing == null ? null : prepareUpdate(existing);
		TLObject workspace = begin(project, WorkspaceStatus.UPDATING, WorkspaceStatus.values());
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		String artifactId = FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID);
		run(workspace, "Updating the workspace to the current image.", log -> {
			boolean wasRunning =
				getApplications().getProvider().status(workspaceId).getState() == WorkspaceState.RUNNING;
			WorkspaceInfo info = getApplications().update(update.spec(), log);
			log.line("Container created on image " + info.getImage() + ".");
			log.update(w -> {
				recordUpdate(w, info);
				FactoryModel.setStatus(w, wasRunning ? WorkspaceStatus.STARTING : WorkspaceStatus.of(info.getState()));
				FactoryModel.setEndpoint(w, null, null);
			});
			if (wasRunning) {
				WorkspaceInfo started = getApplications().start(workspaceId);
				log.update(w -> FactoryModel.setEndpoint(w, port(started), null));
				awaitApplication(log, workspaceId, artifactId);
			}
		});
	}

	/**
	 * What the container of an updated workspace gets.
	 *
	 * @param spec
	 *        The application of the workspace, including the environment the container gets.
	 */
	public record WorkspaceUpdate(ApplicationSpec spec) {
		@Override
		public String toString() {
			// The environment of the spec holds secrets.
			return "WorkspaceUpdate[" + spec.getWorkspaceId() + "]";
		}
	}

	/**
	 * Describes the container of an updated workspace without storing anything.
	 *
	 * <p>
	 * The workspace keeps its {@link FactoryModel#WORKSPACE_INITIAL_PASSWORD initial password}.
	 * </p>
	 *
	 * @param workspace
	 *        The stored workspace.
	 */
	public static WorkspaceUpdate prepareUpdate(TLObject workspace) {
		ApplicationSpec spec = applicationSpec(FactoryModel.getProject(workspace),
			FactoryModel.getWorkspaceId(workspace),
			FactoryModel.getString(workspace, FactoryModel.WORKSPACE_INITIAL_PASSWORD));
		return new WorkspaceUpdate(spec);
	}

	/**
	 * Stores in the current transaction that the container of a workspace was created anew: the
	 * {@link FactoryModel#WORKSPACE_IMAGE image} of the container.
	 *
	 * @param workspace
	 *        The stored workspace.
	 * @param info
	 *        The workspace in its new container.
	 */
	public static void recordUpdate(TLObject workspace, WorkspaceInfo info) {
		workspace.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, info.getImage());
	}

	/**
	 * Whether the container of a workspace was created from another image than the
	 * {@link ApplicationWorkspaces#currentImage() current workspace image}.
	 *
	 * <p>
	 * A workspace without recorded image has no container yet - it is being built, or its build
	 * failed before the container was created - and is not outdated. The image of a container
	 * created before images were recorded is taken over when the states are compared with the
	 * containers after a restart (see {@link WorkspaceReconciliation}).
	 * </p>
	 *
	 * @see #update(TLObject)
	 */
	public static boolean isImageOutdated(TLObject workspace) {
		String image = FactoryModel.getString(workspace, FactoryModel.WORKSPACE_IMAGE);
		return image != null && !image.equals(applications().currentImage());
	}

	/**
	 * Checks the Maven group of an application.
	 *
	 * <p>
	 * The group is the start of the Java package of the application, so its names must be Java
	 * identifiers, none a reserved word.
	 * </p>
	 *
	 * @return The problem with the given value, {@code null} if it is valid.
	 */
	public static ResKey checkGroupId(String groupId) {
		if (groupId == null || !GROUP_ID.matcher(groupId).matches()) {
			return I18NConstants.ERROR_INVALID_GROUP_ID__VALUE.fill(groupId);
		}
		return checkKeywords(groupId, GROUP_ID_SEPARATOR);
	}

	/**
	 * Checks the Maven artifact of an application.
	 *
	 * <p>
	 * The artifact is the context path of the application, the name of its repository and CI job
	 * and the start of the workspace identifier. The archetype derives the end of the Java package
	 * and the module name of the application from it, a name for each part between dashes: each
	 * part must start with a letter and must not be a reserved word of Java.
	 * </p>
	 *
	 * @return The problem with the given value, {@code null} if it is valid.
	 */
	public static ResKey checkArtifactId(String artifactId) {
		if (artifactId == null || !ARTIFACT_ID.matcher(artifactId).matches()) {
			return I18NConstants.ERROR_INVALID_ARTIFACT_ID__VALUE.fill(artifactId);
		}
		for (String part : artifactId.split(ARTIFACT_ID_SEPARATOR, -1)) {
			if (part.isEmpty()) {
				return I18NConstants.ERROR_ARTIFACT_ID_EMPTY_PART__VALUE.fill(artifactId);
			}
			if (!Character.isLetter(part.charAt(0))) {
				return I18NConstants.ERROR_ARTIFACT_ID_PART_STARTS_WITH_DIGIT__VALUE_PART.fill(artifactId, part);
			}
		}
		return checkKeywords(artifactId, ARTIFACT_ID_SEPARATOR);
	}

	private static ResKey checkKeywords(String value, String separator) {
		for (String part : value.split(Pattern.quote(separator))) {
			if (SourceVersion.isKeyword(part, SourceVersion.latest())) {
				return I18NConstants.ERROR_JAVA_KEYWORD__VALUE_PART.fill(value, part);
			}
		}
		return null;
	}

	/**
	 * Starts the workspace of a project in the background and waits until its application
	 * answers.
	 *
	 * <p>
	 * A workspace whose container runs but whose application does not
	 * ({@link WorkspaceStatus#APP_STOPPED}) gets only its application started, through the
	 * supervisor in the container.
	 * </p>
	 *
	 * @param project
	 *        A project whose workspace is {@link WorkspaceStatus#STOPPED},
	 *        {@link WorkspaceStatus#CREATED}, {@link WorkspaceStatus#APP_STOPPED} or
	 *        {@link WorkspaceStatus#UNKNOWN}.
	 */
	public void start(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.STARTING, WorkspaceStatus.STOPPED, WorkspaceStatus.CREATED,
			WorkspaceStatus.APP_STOPPED, WorkspaceStatus.UNKNOWN);
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		String artifactId = FactoryModel.getString(project, FactoryModel.PROJECT_ARTIFACT_ID);
		run(workspace, "Starting the workspace.", log -> {
			WorkspaceInfo current = getApplications().getProvider().status(workspaceId);
			if (current.getState() == WorkspaceState.RUNNING) {
				log.line("The container runs, starting the application.");
				log.flush();
				try {
					getApplications().startApplication(workspaceId, log);
				} catch (ApplicationFailedException ex) {
					applicationStopped(log, workspaceId, ex);
					return;
				}
			} else {
				WorkspaceInfo info = getApplications().start(workspaceId);
				log.update(w -> FactoryModel.setEndpoint(w, port(info), null));
			}
			awaitApplication(log, workspaceId, artifactId);
		});
	}

	/**
	 * Stops the workspace of a project in the background.
	 *
	 * @param project
	 *        A project whose workspace is {@link WorkspaceStatus#RUNNING} or
	 *        {@link WorkspaceStatus#APP_STOPPED}, or whose container may run:
	 *        {@link WorkspaceStatus#UNKNOWN} or {@link WorkspaceStatus#ERROR}.
	 */
	public void stop(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.STOPPING, WorkspaceStatus.RUNNING,
			WorkspaceStatus.APP_STOPPED, WorkspaceStatus.UNKNOWN, WorkspaceStatus.ERROR);
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		run(workspace, "Stopping the workspace.", log -> {
			getApplications().stop(workspaceId);
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
	 * <p>
	 * The agents working in the workspace are stopped and the tokens the
	 * {@link WorkspaceProxyService workspace proxy} issued for the workspace are revoked first; the
	 * agent conversations are removed with the workspace.
	 * </p>
	 *
	 * @param project
	 *        A project with a workspace on which no operation runs.
	 */
	public void cleanup(TLObject project) {
		TLObject workspace = begin(project, WorkspaceStatus.REMOVING, WorkspaceStatus.values());
		String workspaceId = FactoryModel.getWorkspaceId(workspace);
		run(workspace, "Removing the workspace.", log -> {
			WorkspaceAgentService agents = WorkspaceAgentService.getInstance();
			if (agents != null) {
				agents.stopAll(workspaceId);
			}
			WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
			if (proxy != null) {
				proxy.revokeAll(workspaceId);
			}
			WorkspaceInfo info = getApplications().getProvider().status(workspaceId);
			if (info.getState() != WorkspaceState.MISSING || !info.getVolumes().isEmpty()) {
				getApplications().remove(workspaceId);
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
		String url;
		try {
			url = getApplications().awaitApplication(workspaceId, artifactId, log);
		} catch (ApplicationFailedException ex) {
			applicationStopped(log, workspaceId, ex);
			return;
		}
		WorkspaceInfo info = getApplications().getProvider().status(workspaceId);
		log.line("Application running at " + url);
		log.update(w -> {
			FactoryModel.setStatus(w, WorkspaceStatus.RUNNING);
			FactoryModel.setEndpoint(w, port(info), url);
		});
	}

	/**
	 * Ends an operation whose application failed to start in a running container in state
	 * {@link WorkspaceStatus#APP_STOPPED}.
	 */
	private void applicationStopped(WorkspaceLog log, String workspaceId, ApplicationFailedException ex) {
		log.line("Failed: " + message(ex));
		WorkspaceInfo info = getApplications().getProvider().status(workspaceId);
		log.update(w -> {
			FactoryModel.setStatus(w, WorkspaceStatus.APP_STOPPED);
			FactoryModel.setEndpoint(w, port(info), null);
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
				new WorkspaceReconciliation(getApplications().getProvider(), ProjectWorkspaces::applicationUrl,
					this::applicationState).reconcile(stored);
			commit(() -> {
				for (Map.Entry<String, WorkspaceReconciliation.Outcome> entry : outcomes.entrySet()) {
					TLObject workspace = workspaces.get(entry.getKey());
					WorkspaceReconciliation.Outcome outcome = entry.getValue();
					FactoryModel.setStatus(workspace, outcome.status());
					FactoryModel.setEndpoint(workspace, outcome.hostPort(), outcome.previewUrl());
					if (outcome.image() != null) {
						workspace.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, outcome.image());
					}
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

	/**
	 * Updates the stored states of the workspaces whose container runs to the states of their
	 * applications, see {@link Config#getApplicationCheckInterval()}.
	 */
	public void checkApplications() {
		for (TLObject workspace : FactoryModel.allWorkspaces()) {
			WorkspaceStatus stored = FactoryModel.getStatus(workspace);
			if (!stored.isContainerRunning()) {
				continue;
			}
			String id = FactoryModel.getWorkspaceId(workspace);
			if (!_busy.add(id)) {
				continue;
			}
			try {
				ApplicationState state = applicationState(id);
				if (stored == WorkspaceStatus.RUNNING && WorkspaceReconciliation.applicationDown(state)) {
					commit(() -> {
						FactoryModel.setStatus(workspace, WorkspaceStatus.APP_STOPPED);
						workspace.tUpdateByName(FactoryModel.WORKSPACE_PREVIEW_URL, null);
					});
					Logger.info("The application of workspace " + id + " is " + state + ".", ProjectWorkspaces.class);
				} else if (stored == WorkspaceStatus.APP_STOPPED && state == ApplicationState.RUNNING) {
					WorkspaceInfo info = getApplications().getProvider().status(id);
					String url = info.hasHostPort() ? applicationUrl(info) : null;
					commit(() -> {
						FactoryModel.setStatus(workspace, WorkspaceStatus.RUNNING);
						FactoryModel.setEndpoint(workspace, port(info), url);
					});
					Logger.info("The application of workspace " + id + " runs again.", ProjectWorkspaces.class);
				}
			} catch (RuntimeException ex) {
				Logger.error("Failed to check the application of workspace " + id + ".", ex, ProjectWorkspaces.class);
			} finally {
				_busy.remove(id);
			}
		}
	}

	/**
	 * The state of the application of a running workspace, {@link ApplicationState#UNKNOWN} if it
	 * cannot be asked.
	 */
	private ApplicationState applicationState(String workspaceId) {
		try {
			return getApplications().applicationState(workspaceId);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return ApplicationState.UNKNOWN;
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
