/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.ci.RecordingCIProvider;
import test.com.top_logic.factory.git.RecordingGitHostingProvider;
import test.com.top_logic.factory.tenant.TestTenants;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;
import test.com.top_logic.knowledge.KBSetup;

import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.git.RepositorySource;
import com.top_logic.factory.project.FactoryFunctions;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of the repository of a project when its workspace is created, through
 * {@link ProjectWorkspaces} against a knowledge base, a {@link RecordingGitHostingProvider} and a
 * {@link RecordingWorkspaceProvider} simulating git in the workspace: a new repository with the
 * generated application pushed before the branch is protected, an adopted repository cloned and
 * checked, the retries after a failed run, and no repository without git hosting; with a
 * {@link RecordingCIProvider CI server}, the pipeline definition in the first commit, the CI job
 * set up after the protection, a failing CI server only a warning, and the CI build a user starts.
 */
public class TestProjectRepository extends BasicTestCase {

	private static final String ENGINE_VERSION = "8.0.0";

	private static final String GROUP_ID = "com.example";

	/**
	 * The address the requests of the simulated git commands come from.
	 */
	private static final String SOURCE = "172.30.0.7";

	/**
	 * The URL of the git route: the proxy URL of the test configuration and the path of the route.
	 */
	private static final String ROUTE_URL = "http://workspace-proxy.test" + GitRoute.DEFAULT_PATH;

	private static final String OTHER_OWNER = "someone";

	private static final long OPERATION_TIMEOUT_MILLIS = 30_000;

	private static final long POLL_MILLIS = 100;

	private static final int HTTP_OK = 200;

	private static final int GIT_FAILURE = 128;

	private HttpServer _server;

	private RecordingWorkspaceProvider _provider;

	private RecordingGitHostingProvider _hosting;

	private WorkspaceProxyService _proxy;

	private final List<TLObject> _projects = new ArrayList<>();

	/**
	 * The remotes that have commits, with the pom of their root.
	 */
	private final Map<String, String> _remotes = new ConcurrentHashMap<>();

	/**
	 * The pom a clone of a remote gets, by remote; absent for a repository without pom.
	 */
	private final Map<String, String> _poms = new ConcurrentHashMap<>();

	/**
	 * The commands whose next run fails.
	 */
	private final Set<String> _failing = ConcurrentHashMap.newKeySet();

	/**
	 * The remote the current workspace pushes to or cloned from.
	 */
	private volatile String _remote;

	/**
	 * What happened in the workspace, in order: the git command, {@code generate}, {@code build}.
	 */
	private final List<String> _steps = new ArrayList<>();

	private volatile String _pushToken;

	private volatile boolean _admittedAtPush;

	private volatile boolean _protectedAtPush;

	private volatile boolean _admittedAtBuild;

	private volatile boolean _protectedAtBuild;

	private volatile boolean _commitIdentity;

	private RecordingCIProvider _ci;

	/**
	 * The content of the pipeline file written into the generated application, {@code null} if
	 * none was written.
	 */
	private volatile String _writtenPipeline;

	/**
	 * The remotes whose default branch has a pipeline file.
	 */
	private final Set<String> _pipelines = ConcurrentHashMap.newKeySet();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_server.createContext("/", exchange -> {
			exchange.sendResponseHeaders(HTTP_OK, -1);
			exchange.close();
		});
		_server.start();
		_provider = new RecordingWorkspaceProvider()
			.setHostPort(_server.getAddress().getPort())
			.setExecHandler(this::exec);
		TestingProjectWorkspaces.setApplications(new ApplicationWorkspaces(
			WorkspaceService.getInstance().getApplications().getConfig(), _provider));
		_hosting = new RecordingGitHostingProvider();
		TestingProjectWorkspaces.setGitHosting(_hosting);
		_ci = new RecordingCIProvider();
		_proxy = WorkspaceProxyService.getInstance();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			commit(() -> {
				for (TLObject project : _projects) {
					TLObject workspace = FactoryModel.getWorkspace(project);
					if (workspace != null) {
						_proxy.revokeAll(FactoryModel.getWorkspaceId(workspace));
						workspace.tDelete();
					}
					project.tDelete();
				}
				return null;
			});
		} finally {
			TestingProjectWorkspaces.setApplications(null);
			TestingProjectWorkspaces.setGitHosting(null);
			TestingProjectWorkspaces.setCI(null);
			_server.stop(0);
			super.tearDown();
		}
	}

	/**
	 * A project without repository gets one named by its artifact, recorded on the project; the
	 * generated application is committed with the identity of the git route and pushed with a token
	 * that is revoked afterwards; the branch is protected after the push and before the build.
	 */
	public void testNewRepository() throws Exception {
		TLObject project = project("fresh-repo", null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));

		String fullName = RecordingGitHostingProvider.DEFAULT_OWNER + "/fresh-repo";
		assertEquals(List.of(RecordingGitHostingProvider.CREATE + " fresh-repo",
			RecordingGitHostingProvider.PROTECT + " " + fullName + " " + RecordingGitHostingProvider.DEFAULT_BRANCH),
			_hosting.calls());
		assertEquals(fullName, FactoryModel.getRepository(project).getFullName());
		assertEquals(RecordingGitHostingProvider.BASE_URL + fullName,
			FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY_URL));
		assertEquals(ProjectWorkspaces.getProtectedFiles(), _hosting.protection(repository(fullName),
			RecordingGitHostingProvider.DEFAULT_BRANCH));
		assertEquals(List.of("Jenkinsfile", "deploy/**"), ProjectWorkspaces.getProtectedFiles());

		assertEquals(List.of("ls-remote", "generate", "init", "add", "commit", "remote", "push", "build"), steps());
		assertEquals(ROUTE_URL + "/" + fullName + RepositoryName.GIT_SUFFIX, _remote);
		assertTrue("The commit has the identity of the git route.", _commitIdentity);
		assertTrue("The push is admitted.", _admittedAtPush);
		assertFalse("The branch is protected after the push.", _protectedAtPush);
		assertTrue("The branch is protected before the build.", _protectedAtBuild);
		assertFalse("The token of the push is revoked after it.", _admittedAtBuild);
		assertEquals(Boolean.TRUE, project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		assertFalse("No token in the container.",
			_provider.created(FactoryModel.getWorkspaceId(workspace)).getEnv().containsKey(ProxyTokens.ENV_PROXY_TOKEN));
	}

	/**
	 * If the credentials of the git connection may not administer the repository, the branch is not
	 * protected: the output has a warning, the project records the unprotected branch (empty, not
	 * false, before the attempt), the
	 * application is built anyway, and the agent is told no protected files.
	 */
	public void testBranchNotProtected() throws Exception {
		_hosting.setCanAdminister(false);
		TLObject project = project("unprotected", null);
		assertNull("Undecided before the workspace tried to protect the branch.",
			project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertEquals(List.of(RecordingGitHostingProvider.CREATE + " unprotected"), _hosting.calls());
		assertEquals(Boolean.FALSE, project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		assertTrue(log(workspace), log(workspace).contains(RepositorySource.WARNING
			+ "The access token of the git connection may not administer the repository "
			+ RecordingGitHostingProvider.DEFAULT_OWNER + "/unprotected"));
		assertTrue("Built anyway.", steps().contains("build"));
		String instructions = new com.top_logic.factory.agent.StoredConversation(
			commit(() -> FactoryModel.createConversation(workspace, "A task."))).instructions();
		assertFalse(instructions, instructions.contains("protected files"));
	}

	/**
	 * After the workspace is cleaned up, the project keeps its repository; the next workspace
	 * clones it instead of creating one and protects the branch again.
	 */
	public void testCleanupKeepsRepository() throws Exception {
		TLObject project = project("kept-repo", null);
		TLObject first = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(first), WorkspaceStatus.RUNNING, awaitOperation(first));
		cleanup(project);
		assertNotNull("Cleanup keeps the repository.", FactoryModel.getRepository(project));
		_steps.clear();

		TLObject second = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(second), WorkspaceStatus.RUNNING, awaitOperation(second));
		assertEquals(List.of("ls-remote", "clone", "build"), steps());
		assertEquals("Created once.", 1, count(RecordingGitHostingProvider.CREATE));
		assertEquals("Protected again.", 2, count(RecordingGitHostingProvider.PROTECT));
	}

	/**
	 * A failed push leaves the created repository recorded but empty; the retry pushes the
	 * generated application into it instead of creating another one.
	 */
	public void testRetryAfterFailedPush() throws Exception {
		TLObject project = project("failed-push", null);
		_failing.add("push");
		TLObject first = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(first));
		assertTrue(log(first), log(first).contains("remote rejected"));
		assertNotNull("The created repository is recorded.", FactoryModel.getRepository(project));
		assertEquals("Not protected.", 0, count(RecordingGitHostingProvider.PROTECT));
		assertFalse("Not built.", steps().contains("build"));

		cleanup(project);
		_steps.clear();
		TLObject second = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(second), WorkspaceStatus.RUNNING, awaitOperation(second));
		assertEquals(List.of("ls-remote", "generate", "init", "add", "commit", "remote", "push", "build"), steps());
		assertEquals("Created once.", 1, count(RecordingGitHostingProvider.CREATE));
		assertEquals(1, count(RecordingGitHostingProvider.PROTECT));
	}

	/**
	 * A build failing after the push leaves the repository with the application; the retry clones
	 * it.
	 */
	public void testRetryAfterPushedRepository() throws Exception {
		TLObject project = project("failed-build", null);
		_failing.add("build");
		TLObject first = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(first));

		cleanup(project);
		_steps.clear();
		TLObject second = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(second), WorkspaceStatus.RUNNING, awaitOperation(second));
		assertEquals(List.of("ls-remote", "clone", "build"), steps());
		assertEquals(1, count(RecordingGitHostingProvider.CREATE));
	}

	/**
	 * An existing repository entered as clone URL is looked up, recorded under its full name,
	 * cloned, checked and protected.
	 */
	public void testAdoptRepository() throws Exception {
		GitRepository existing = existing("existing-app", pom("adopted", ENGINE_VERSION));
		TLObject project = project("adopted", existing.getCloneUrl(), null);
		assertNull(ProjectWorkspaces.getInstance().checkRepository(null, existing.getCloneUrl()));

		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertEquals(existing.getFullName(), FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY));
		assertEquals(existing.getWebUrl(), FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY_URL));
		assertEquals(List.of("ls-remote", "clone", "build"), steps());
		assertEquals(0, count(RecordingGitHostingProvider.CREATE));
		assertNotNull(_hosting.protection(existing, RecordingGitHostingProvider.DEFAULT_BRANCH));
	}

	/**
	 * A reference to a repository that does not exist is rejected by the check of the form and
	 * fails the creation of the workspace.
	 */
	public void testAdoptUnknownRepository() throws Exception {
		String reference = OTHER_OWNER + "/missing";
		ResKey problem = ProjectWorkspaces.getInstance().checkRepository(null, reference);
		assertNotNull(problem);
		assertEquals(I18NConstants.ERROR_UNKNOWN_REPOSITORY__REFERENCE.fill(reference), problem);
		assertNull("Empty: a new repository.", ProjectWorkspaces.getInstance().checkRepository(null, ""));

		try {
			project("unknown-repo", reference, null);
			fail("An unknown repository is stored.");
		} catch (TopLogicException expected) {
			// The constraint of the attribute.
		}

		// A recorded repository that is gone from the hosting service since.
		TLObject project = project("unknown-repo", reference);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(workspace));
		assertTrue(log(workspace), log(workspace).contains(reference));
		assertEquals("Nothing done in the workspace.", List.of(), steps());
		assertEquals(0, count(RecordingGitHostingProvider.CREATE));
	}

	/**
	 * An adopted repository with the application of another artifact fails before the branch is
	 * protected.
	 */
	public void testAdoptArtifactMismatch() throws Exception {
		GitRepository existing = existing("other-app", pom("other-app", ENGINE_VERSION));
		TLObject project = project("mismatch", existing.getFullName(), null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(workspace));
		assertTrue(log(workspace), log(workspace).contains("other-app"));
		assertEquals(List.of("ls-remote", "clone"), steps());
		assertNull("Not protected.", _hosting.protection(existing, RecordingGitHostingProvider.DEFAULT_BRANCH));
	}

	/**
	 * An adopted repository with an application on another TopLogic version fails.
	 */
	public void testAdoptEngineVersionMismatch() throws Exception {
		GitRepository existing = existing("old-app", pom("old-app", "7.11.0"));
		TLObject project = project("old-app", existing.getFullName(), null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(workspace));
		assertTrue(log(workspace), log(workspace).contains("7.11.0"));
		assertFalse(steps().contains("build"));
	}

	/**
	 * An adopted repository without pom fails.
	 */
	public void testAdoptWithoutPom() throws Exception {
		GitRepository existing = existing("no-pom", null);
		TLObject project = project("no-pom", existing.getFullName(), null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(WorkspaceStatus.ERROR, awaitOperation(workspace));
		assertFalse(steps().contains("build"));
	}

	/**
	 * Without git connection, the application is generated in the workspace only.
	 */
	public void testNoGitHosting() throws Exception {
		TestingProjectWorkspaces.setGitHosting(null);
		TLObject project = project("no-hosting", null);
		commit(() -> {
			project.tUpdateByName(FactoryModel.PROJECT_GIT_CONNECTION, null);
			return null;
		});
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertEquals(List.of("generate", "build"), steps());
		assertNull(FactoryModel.getRepository(project));
		assertEquals(List.of(), _hosting.calls());
	}

	/**
	 * With CI, the first commit of a new application has the pipeline definition of the template;
	 * after the protection, the CI job of the repository is set up, recorded on the project and
	 * builds the application once.
	 */
	public void testNewRepositoryWithCI() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		TLObject project = project("ci-fresh", null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));

		assertEquals(List.of("ls-remote", "generate", "write " + RecordingCIProvider.PIPELINE_FILE, "init", "add",
			"commit", "remote", "push", "build"), steps());
		assertEquals(CIService.readResource(CIService.DEFAULT_PIPELINE_TEMPLATE), _writtenPipeline);
		assertTrue(_writtenPipeline, _writtenPipeline.contains("mvn -B verify"));

		String fullName = RecordingGitHostingProvider.DEFAULT_OWNER + "/ci-fresh";
		String job = RecordingCIProvider.FOLDER + "/" + fullName;
		assertEquals(List.of(RecordingCIProvider.ENSURE + " " + fullName, RecordingCIProvider.START + " " + job),
			_ci.calls());
		assertTrue("The job is set up after the protection.",
			_hosting.calls().contains(RecordingGitHostingProvider.PROTECT + " " + fullName + " "
				+ RecordingGitHostingProvider.DEFAULT_BRANCH));
		assertEquals(job, FactoryModel.getCIJob(project));
		assertEquals(RecordingCIProvider.jobUrl(job), FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL));
		assertTrue(log(workspace), log(workspace).contains("First CI build 1 started"));
		assertTrue("The pipeline file is protected.",
			_hosting.protection(repository(fullName), RecordingGitHostingProvider.DEFAULT_BRANCH)
				.contains(RecordingCIProvider.PIPELINE_FILE));
	}

	/**
	 * An adopted repository keeps its own pipeline definition; one without gets a warning, and its
	 * job is set up anyway.
	 */
	public void testAdoptRepositoryWithCI() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		GitRepository withPipeline = existing("with-pipeline", pom("with-pipeline", ENGINE_VERSION));
		_pipelines.add(GitRoute.remoteUrl(ROUTE_URL, withPipeline.getName()));
		TLObject first = ProjectWorkspaces.getInstance().create(project("with-pipeline", withPipeline.getFullName(), null));
		assertEquals(log(first), WorkspaceStatus.RUNNING, awaitOperation(first));
		assertEquals(List.of("ls-remote", "clone", "build"), steps());
		assertNull("Nothing written.", _writtenPipeline);
		assertFalse(log(first), log(first).contains(RepositorySource.WARNING));

		_steps.clear();
		GitRepository without = existing("without-pipeline", pom("without-pipeline", ENGINE_VERSION));
		TLObject project = project("without-pipeline", without.getFullName(), null);
		TLObject second = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(second), WorkspaceStatus.RUNNING, awaitOperation(second));
		assertEquals(List.of("ls-remote", "clone", "build"), steps());
		assertTrue(log(second), log(second).contains(RepositorySource.WARNING + "The repository "
			+ without.getFullName() + " has no CI pipeline " + RecordingCIProvider.PIPELINE_FILE));
		assertEquals(RecordingCIProvider.jobName(without), FactoryModel.getCIJob(project));
		assertEquals(2, _ci.calls().stream().filter(call -> call.startsWith(RecordingCIProvider.START + " ")).count());
	}

	/**
	 * A failing CI server is a warning in the output only: the workspace runs, the project has no
	 * CI job.
	 */
	public void testCIFailureIsWarning() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		_ci.setFailure(new TopLogicException(com.top_logic.factory.ci.I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM
			.fill(RecordingCIProvider.BASE_URL, "connection refused")));
		TLObject project = project("ci-down", null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertTrue(log(workspace), log(workspace).contains(RepositorySource.WARNING + "Setting up the CI job failed"));
		assertTrue(log(workspace), log(workspace).contains("connection refused"));
		assertNull(FactoryModel.getCIJob(project));
		assertTrue("Built anyway.", steps().contains("build"));
		assertTrue("The pipeline is in the first commit anyway.", _pipelines.contains(_remote));
	}

	/**
	 * The command of a user sets up the job if it is missing and starts a build; it is unavailable
	 * without CI or without repository. The last build is read for the views.
	 */
	public void testStartBuild() throws Exception {
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		TLObject project = project("ci-command", null);
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, service.ciUnavailable(project));
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, FactoryFunctions.ciUnavailable(project));

		TestingProjectWorkspaces.setCI(_ci);
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_REPOSITORY__PROJECT.fill(project),
			service.ciUnavailable(project));
		try {
			service.startBuild(project);
			fail("A build without repository.");
		} catch (TopLogicException expected) {
			// Expected.
		}
		assertNull(FactoryFunctions.lastBuild(project));

		// A repository adopted while CI was down: no job yet.
		GitRepository existing = existing("ci-command", pom("ci-command", ENGINE_VERSION));
		TLObject adopted = project("ci-command", existing.getFullName(), RecordingGitHostingProvider.BASE_URL
			+ existing.getFullName());
		assertNull(service.ciUnavailable(adopted));
		assertNull(FactoryModel.getCIJob(adopted));

		CIBuildStart started = service.startBuild(adopted);
		HistoryUtils.updateSessionRevision();
		String job = RecordingCIProvider.jobName(existing);
		assertEquals("The missing job is created.", job, FactoryModel.getCIJob(adopted));
		assertEquals(Integer.valueOf(1), started.number());
		assertEquals(com.top_logic.factory.ci.I18NConstants.BUILD_STARTED__NUMBER_JOB.fill(2, job),
			FactoryFunctions.startBuild(adopted));

		_ci.finish(job, CIBuildState.SUCCESS);
		CIBuild last = service.lastBuild(adopted);
		assertEquals(2, last.number());
		assertEquals(CIBuildState.SUCCESS, last.state());
		Map<String, Object> shown = FactoryFunctions.lastBuild(adopted);
		assertEquals(Integer.valueOf(2), shown.get(FactoryFunctions.BUILD_NUMBER));
		assertEquals(CIBuildState.SUCCESS.name(), shown.get(FactoryFunctions.BUILD_STATE));
		assertEquals(CIBuildState.SUCCESS.label(), shown.get(FactoryFunctions.BUILD_STATE_LABEL));
		assertEquals(last.webUrl(), shown.get(FactoryFunctions.BUILD_URL));

		_ci.setQueued(true);
		assertEquals(com.top_logic.factory.ci.I18NConstants.BUILD_QUEUED__JOB.fill(job),
			FactoryFunctions.startBuild(adopted));

		_ci.setFailure(new TopLogicException(com.top_logic.factory.ci.I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM
			.fill(RecordingCIProvider.BASE_URL, "down")));
		assertNotNull("The view shows why.", FactoryFunctions.lastBuild(adopted).get(FactoryFunctions.BUILD_ERROR));
	}

	/**
	 * With CI, the pipeline file is a protected file, added unless it is listed.
	 */
	public void testProtectedFilesWithCI() {
		assertEquals(List.of("Jenkinsfile", "deploy/**", "ci/pipeline.groovy"),
			com.top_logic.factory.ci.ProjectCI.withPipelineFile(List.of("Jenkinsfile", "deploy/**"),
				new RecordingCIProvider() {
					@Override
					public String pipelineFile() {
						return "ci/pipeline.groovy";
					}
				}));
		assertEquals(List.of("Jenkinsfile"),
			com.top_logic.factory.ci.ProjectCI.withPipelineFile(List.of("Jenkinsfile"), _ci));
	}

	/**
	 * The pom of the archetype names the TopLogic version in a property, that of an older
	 * application in its parent; an expression is not read.
	 */
	public void testPom() {
		assertEquals(new RepositorySource.Pom("app", "8.0.0"), RepositorySource.Pom.parse(pom("app", "8.0.0")));
		assertEquals(new RepositorySource.Pom("app", "7.11.2"), RepositorySource.Pom.parse(
			"<project><parent><groupId>com.top-logic</groupId><artifactId>tl-parent-app</artifactId>"
				+ "<version>7.11.2</version></parent><artifactId>app</artifactId></project>"));
		assertEquals(new RepositorySource.Pom("app", null), RepositorySource.Pom.parse(
			"<project><artifactId>app</artifactId><properties><tl.version>${revision}</tl.version></properties></project>"));
		try {
			RepositorySource.Pom.parse("<html/>");
			fail("Not a pom.");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
		try {
			RepositorySource.Pom.parse("<!DOCTYPE project [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><project>&x;</project>");
			fail("A document type is refused.");
		} catch (IllegalArgumentException expected) {
			// Expected.
		}
	}

	/**
	 * The agent of a project with repository is told to commit and push.
	 */
	public void testAgentInstructions() {
		String text = RepositorySource.agentInstructions("factory/app", List.of("Jenkinsfile"));
		assertTrue(text, text.contains("factory/app"));
		assertTrue(text, text.contains("git push"));
		assertTrue(text, text.contains("Jenkinsfile"));
	}

	/**
	 * Simulates the commands in the workspace: git against {@link #_remotes}, the archetype, the
	 * build and the supervisor of a running application.
	 */
	private int exec(String workspaceId, ExecCommand command, ExecOutput output) {
		List<String> line = command.getCommand();
		String program = line.get(0);
		if (program.equals(RepositorySource.GIT)) {
			return git(command, output);
		}
		if (program.equals("sh") && line.size() == 5 && line.get(2).equals(RepositorySource.WRITE_FILE)) {
			String path = line.get(4);
			step("write " + path.substring(path.lastIndexOf('/') + 1));
			_writtenPipeline = command.getEnv().get(RepositorySource.ENV_FILE_CONTENT);
			return 0;
		}
		if (program.equals("test") && line.get(line.size() - 1).endsWith("/" + RecordingCIProvider.PIPELINE_FILE)) {
			return _pipelines.contains(_remote) ? 0 : 1;
		}
		if (program.equals("mvn") && line.stream().anyMatch(arg -> arg.startsWith("-DarchetypeArtifactId="))) {
			step("generate");
			return 0;
		}
		if (program.equals("mvn") && line.contains("install")) {
			step("build");
			_admittedAtBuild = _pushToken != null && admitted(_pushToken);
			_protectedAtBuild = protectedNow();
			return fails("build") ? 1 : 0;
		}
		if ((program.equals("test") || program.equals("cat")) && line.get(line.size() - 1).endsWith(RepositorySource.POM)) {
			String pom = _poms.get(_remote);
			if (pom == null) {
				return 1;
			}
			if (program.equals("cat")) {
				output.stdout(pom);
			}
			return 0;
		}
		// The supervisor reports a running application.
		return 0;
	}

	private int git(ExecCommand command, ExecOutput output) {
		List<String> line = command.getCommand();
		String subCommand = line.get(1);
		step(subCommand);
		switch (subCommand) {
			case "ls-remote": {
				_remote = line.get(2);
				if (_remotes.containsKey(_remote)) {
					output.stdout("0123456789abcdef\tHEAD\n0123456789abcdef\trefs/heads/main\n");
				}
				return 0;
			}
			case "clone":
				return _remotes.containsKey(line.get(2)) ? 0 : GIT_FAILURE;
			case "commit":
				_commitIdentity = command.getEnv().containsValue(GitRoute.GIT_USER_NAME)
					&& command.getEnv().containsValue(GitRoute.DEFAULT_USER_NAME);
				return 0;
			case "push": {
				String token = command.getEnv().get(ProxyTokens.ENV_PROXY_TOKEN);
				_pushToken = token;
				_admittedAtPush = token != null && admitted(token);
				_protectedAtPush = protectedNow();
				if (fails("push")) {
					output.stderr(" ! [remote rejected] main -> main (simulated)\n");
					return 1;
				}
				_remotes.put(_remote, "pushed");
				if (_writtenPipeline != null) {
					_pipelines.add(_remote);
				}
				_poms.put(_remote, pom(artifactOf(_remote), ENGINE_VERSION));
				return 0;
			}
			default:
				return 0;
		}
	}

	private boolean fails(String step) {
		return _failing.remove(step);
	}

	private synchronized void step(String step) {
		_steps.add(step);
	}

	private synchronized List<String> steps() {
		return new ArrayList<>(_steps);
	}

	/**
	 * Whether the repository of the current remote has a protected branch.
	 */
	private boolean protectedNow() {
		return _hosting.calls().stream().anyMatch(call -> call.startsWith(RecordingGitHostingProvider.PROTECT + " "
			+ fullNameOf(_remote) + " "));
	}

	private static String fullNameOf(String remote) {
		String path = remote.substring(ROUTE_URL.length() + 1);
		return path.substring(0, path.length() - RepositoryName.GIT_SUFFIX.length());
	}

	private static String artifactOf(String remote) {
		String fullName = fullNameOf(remote);
		return fullName.substring(fullName.indexOf('/') + 1);
	}

	private boolean admitted(String token) {
		return _proxy.admit(token, SOURCE).status() == ProxyAdmission.Status.ADMITTED;
	}

	private int count(String operation) {
		return (int) _hosting.calls().stream().filter(call -> call.startsWith(operation + " ")).count();
	}

	private GitRepository repository(String fullName) {
		for (GitRepository repository : _hosting.repositories()) {
			if (repository.getFullName().equals(fullName)) {
				return repository;
			}
		}
		throw new AssertionError("No repository " + fullName);
	}

	/**
	 * A repository of another owner with commits, whose clone has the given pom.
	 */
	private GitRepository existing(String name, String pom) {
		GitRepository repository = _hosting.addRepository(OTHER_OWNER, name);
		String remote = GitRoute.remoteUrl(ROUTE_URL, repository.getName());
		_remotes.put(remote, "existing");
		if (pom != null) {
			_poms.put(remote, pom);
		}
		return repository;
	}

	private static String pom(String artifactId, String version) {
		return "<?xml version=\"1.0\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\">"
			+ "<modelVersion>4.0.0</modelVersion><groupId>" + GROUP_ID + "</groupId><artifactId>" + artifactId
			+ "</artifactId><version>1.0-SNAPSHOT</version><packaging>tl-app</packaging>"
			+ "<properties><tl.version>" + version + "</tl.version></properties></project>";
	}

	private TLObject project(String artifactId, String repository) {
		return project(artifactId, repository,
			repository == null ? null : RecordingGitHostingProvider.BASE_URL + repository);
	}

	/**
	 * A stored project.
	 *
	 * @param repository
	 *        The repository entered or recorded, {@code null} for none.
	 * @param repositoryUrl
	 *        The page of a recorded repository, {@code null} for an entered one.
	 */
	private TLObject project(String artifactId, String repository, String repositoryUrl) {
		TLObject project = commit(() -> {
			TLObject result = TestTenants.newProject();
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "Repository test " + artifactId);
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, GROUP_ID);
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, artifactId);
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, ENGINE_VERSION);
			result.tUpdateByName(FactoryModel.PROJECT_REPOSITORY, repository);
			result.tUpdateByName(FactoryModel.PROJECT_REPOSITORY_URL, repositoryUrl);
			return result;
		});
		_projects.add(project);
		return project;
	}

	/**
	 * Removes the workspace of the project and waits until it is gone.
	 */
	private static void cleanup(TLObject project) throws InterruptedException {
		ProjectWorkspaces.getInstance().cleanup(project);
		long deadline = System.currentTimeMillis() + OPERATION_TIMEOUT_MILLIS;
		while (true) {
			HistoryUtils.updateSessionRevision();
			if (FactoryModel.getWorkspace(project) == null) {
				return;
			}
			assertTrue("Cleanup did not end.", System.currentTimeMillis() < deadline);
			Thread.sleep(POLL_MILLIS);
		}
	}

	/**
	 * Waits until the operation running on the workspace has ended.
	 *
	 * @return The state it ended in.
	 */
	private static WorkspaceStatus awaitOperation(TLObject workspace) throws InterruptedException {
		long deadline = System.currentTimeMillis() + OPERATION_TIMEOUT_MILLIS;
		while (true) {
			HistoryUtils.updateSessionRevision();
			WorkspaceStatus status = FactoryModel.getStatus(workspace);
			if (!status.isBusy()) {
				return status;
			}
			assertTrue("Operation did not end, state " + status + ": " + log(workspace),
				System.currentTimeMillis() < deadline);
			Thread.sleep(POLL_MILLIS);
		}
	}

	private static String log(TLObject workspace) {
		HistoryUtils.updateSessionRevision();
		return String.valueOf(FactoryModel.getString(workspace, FactoryModel.WORKSPACE_LOG));
	}

	private static <T> T commit(Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Test"))) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * The tests of this class, with a knowledge base holding the model of the control application,
	 * the workspace proxy of the test configuration and the {@link TestingProjectWorkspaces}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestProjectRepository.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				// The configuration of the workspace image; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE,
				WorkspaceProxyService.Module.INSTANCE,
				GitHostingService.Module.INSTANCE,
				CIService.Module.INSTANCE,
				ProjectWorkspaces.Module.INSTANCE));
	}

}
