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
import test.com.top_logic.factory.git.RecordingGitProviderType;
import test.com.top_logic.factory.tenant.TestTenants;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;
import test.com.top_logic.knowledge.KBSetup;

import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.StoredConversation;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIRoute;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.ci.ProjectCI;
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
 * {@link RecordingCIProvider CI server}, no CI set up by the creation, setting up the CI of a
 * project on request (the pipeline definition committed unless the repository has one, the
 * protection extended by the pipeline file, the job created, its retry after a failure, its refusal
 * for a project on a git connection of its tenant), the CI build a user starts and what the agent
 * is told about the CI.
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
	 * The git connections of the test tenant the tests create.
	 */
	private final List<TLObject> _connections = new ArrayList<>();

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
				for (TLObject connection : _connections) {
					connection.tDelete();
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
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES), _hosting.protection(repository(fullName),
			RecordingGitHostingProvider.DEFAULT_BRANCH));

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
	 * With CI switched on, creating the workspace sets up no CI: the first commit has no pipeline
	 * definition, the CI server is not contacted, the branch is protected without the pipeline
	 * file; the CI of the project can be set up afterwards, no build started before.
	 */
	public void testNewRepositoryWithoutCI() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		TLObject project = project("ci-none", null);
		TLObject workspace = service.create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));

		assertEquals(List.of("ls-remote", "generate", "init", "add", "commit", "remote", "push", "build"), steps());
		assertEquals("The CI server is not contacted.", List.of(), _ci.calls());
		assertNull(FactoryModel.getCIJob(project));
		String fullName = RecordingGitHostingProvider.DEFAULT_OWNER + "/ci-none";
		assertEquals(List.of(RecordingGitHostingProvider.CREATE + " ci-none",
			RecordingGitHostingProvider.PROTECT + " " + fullName + " " + RecordingGitHostingProvider.DEFAULT_BRANCH),
			_hosting.calls());
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES),
			_hosting.protection(repository(fullName), RecordingGitHostingProvider.DEFAULT_BRANCH));
		assertNull("The CI can be set up.", service.ciSetUpUnavailable(project));
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI_JOB__PROJECT.fill(project),
			service.ciUnavailable(project));
	}

	/**
	 * Setting up the CI of a project commits the pipeline definition of the template to the default
	 * branch, extends the protection by the pipeline file, records the job and starts its first
	 * build; from then on, the agent is told about the CI, the CI is not offered again, and a new
	 * workspace protects the pipeline file as well.
	 */
	public void testSetUpCI() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		TLObject project = project("ci-setup", null);
		TLObject first = service.create(project);
		assertEquals(log(first), WorkspaceStatus.RUNNING, awaitOperation(first));
		String fullName = RecordingGitHostingProvider.DEFAULT_OWNER + "/ci-setup";
		GitRepository repository = repository(fullName);
		String branch = RecordingGitHostingProvider.DEFAULT_BRANCH;
		String before = StoredConversation.projectInstructions(project, _ci, true);
		assertFalse(before, before.contains(CIRoute.agentInstructions()));
		assertFalse(before, before.contains(RecordingCIProvider.PIPELINE_FILE));

		ProjectCI.Setup setup = service.setUpCI(project);
		HistoryUtils.updateSessionRevision();
		assertTrue(setup.pipelineAdded());
		assertTrue(setup.pipelineProtected());
		assertEquals(CIService.readResource(CIService.DEFAULT_PIPELINE_TEMPLATE),
			_hosting.files(repository, branch).get(RecordingCIProvider.PIPELINE_FILE));
		List<String> calls = _hosting.calls();
		assertEquals("The pipeline file is committed before it is protected.",
			List.of(RecordingGitHostingProvider.FIND + " " + fullName,
				RecordingGitHostingProvider.COMMIT_FILE + " " + fullName + " " + branch + " "
					+ RecordingCIProvider.PIPELINE_FILE,
				RecordingGitHostingProvider.PROTECT + " " + fullName + " " + branch),
			calls.subList(2, calls.size()));
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES, RecordingCIProvider.PIPELINE_FILE),
			_hosting.protection(repository, branch));
		String job = RecordingCIProvider.jobName(repository);
		assertEquals(List.of(RecordingCIProvider.ENSURE + " " + fullName, RecordingCIProvider.START + " " + job),
			_ci.calls());
		assertEquals(job, FactoryModel.getCIJob(project));
		assertEquals(RecordingCIProvider.jobUrl(job), FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL));
		assertEquals(Boolean.TRUE, project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		assertEquals(com.top_logic.factory.ci.I18NConstants.CI_SET_UP__JOB_BUILD.fill(job,
			com.top_logic.factory.ci.I18NConstants.BUILD_STARTED__NUMBER_JOB.fill(1, job)), setup.message());

		assertEquals("Not offered again.", com.top_logic.factory.ci.I18NConstants.ERROR_HAS_CI_JOB__PROJECT.fill(project),
			service.ciSetUpUnavailable(project));
		assertNull("Builds can be started.", service.ciUnavailable(project));
		String after = StoredConversation.projectInstructions(project, _ci, true);
		assertTrue(after, after.contains(CIRoute.agentInstructions()));
		assertTrue(after, after.contains(RecordingCIProvider.PIPELINE_FILE));
		String withoutRoute = StoredConversation.projectInstructions(project, _ci, false);
		assertFalse(withoutRoute, withoutRoute.contains(CIRoute.agentInstructions()));

		cleanup(project);
		TLObject second = service.create(project);
		assertEquals(log(second), WorkspaceStatus.RUNNING, awaitOperation(second));
		assertEquals("A new workspace keeps the pipeline file protected.",
			List.of(GitHostingService.DEFAULT_PROTECTED_FILES, RecordingCIProvider.PIPELINE_FILE),
			_hosting.protection(repository, branch));
		assertEquals("No further CI call.", 2, _ci.calls().size());
	}

	/**
	 * A repository with a pipeline definition of its own keeps it when the CI is set up.
	 */
	public void testSetUpCIKeepsPipeline() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		GitRepository existing = existing("own-pipeline", pom("own-pipeline", ENGINE_VERSION));
		String branch = RecordingGitHostingProvider.DEFAULT_BRANCH;
		_hosting.addFile(existing, branch, RecordingCIProvider.PIPELINE_FILE, "own pipeline");
		TLObject project = withSources(project("own-pipeline", existing.getFullName()));

		ProjectCI.Setup setup = ProjectWorkspaces.getInstance().setUpCI(project);
		HistoryUtils.updateSessionRevision();
		assertFalse("Kept.", setup.pipelineAdded());
		assertEquals("own pipeline", _hosting.files(existing, branch).get(RecordingCIProvider.PIPELINE_FILE));
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES, RecordingCIProvider.PIPELINE_FILE),
			_hosting.protection(existing, branch));
		assertEquals(RecordingCIProvider.jobName(existing), FactoryModel.getCIJob(project));
	}

	/**
	 * A set up failing at the CI server leaves the pipeline file committed and protected, but no
	 * job; it is offered again, and the retry keeps the file and creates the job.
	 */
	public void testSetUpCIRetry() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		GitRepository existing = existing("ci-retry", pom("ci-retry", ENGINE_VERSION));
		String branch = RecordingGitHostingProvider.DEFAULT_BRANCH;
		TLObject project = withSources(project("ci-retry", existing.getFullName()));
		ResKey down = com.top_logic.factory.ci.I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM
			.fill(RecordingCIProvider.BASE_URL, "connection refused");
		_ci.setFailure(new TopLogicException(down));
		try {
			service.setUpCI(project);
			fail("The CI server is down.");
		} catch (TopLogicException ex) {
			assertEquals(down, ex.getErrorKey());
		}
		HistoryUtils.updateSessionRevision();
		assertNull(FactoryModel.getCIJob(project));
		assertTrue(_hosting.files(existing, branch).containsKey(RecordingCIProvider.PIPELINE_FILE));
		assertTrue(_hosting.protection(existing, branch).contains(RecordingCIProvider.PIPELINE_FILE));
		assertNull("Offered again.", service.ciSetUpUnavailable(project));

		_ci.setFailure(null);
		ProjectCI.Setup setup = service.setUpCI(project);
		HistoryUtils.updateSessionRevision();
		assertFalse("The committed file is kept.", setup.pipelineAdded());
		assertEquals(1, _hosting.files(existing, branch).size());
		assertEquals(RecordingCIProvider.jobName(existing), FactoryModel.getCIJob(project));
	}

	/**
	 * If the credentials of the git connection may not administer the repository, the pipeline
	 * file is committed but not protected; the project keeps its unprotected branch, and the user
	 * is told.
	 */
	public void testSetUpCIUnprotected() throws Exception {
		TestingProjectWorkspaces.setCI(_ci);
		_hosting.setCanAdminister(false);
		TLObject project = project("ci-unprotected", null);
		TLObject workspace = ProjectWorkspaces.getInstance().create(project);
		assertEquals(log(workspace), WorkspaceStatus.RUNNING, awaitOperation(workspace));
		assertEquals(Boolean.FALSE, project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));

		ProjectCI.Setup setup = ProjectWorkspaces.getInstance().setUpCI(project);
		HistoryUtils.updateSessionRevision();
		assertTrue(setup.pipelineAdded());
		assertFalse(setup.pipelineProtected());
		assertEquals("Never protected.", 0, count(RecordingGitHostingProvider.PROTECT));
		assertEquals(Boolean.FALSE, project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED));
		String job = FactoryModel.getCIJob(project);
		assertNotNull(job);
		assertEquals(com.top_logic.factory.ci.I18NConstants.CI_SET_UP_UNPROTECTED__JOB_BUILD_FILE.fill(job,
			com.top_logic.factory.ci.I18NConstants.BUILD_STARTED__NUMBER_JOB.fill(1, job),
			RecordingCIProvider.PIPELINE_FILE), setup.message());
	}

	/**
	 * The CI cannot be set up without CI server, without repository, before a workspace brought the
	 * application into the repository, and for a project on a git connection of its tenant.
	 */
	public void testSetUpCIUnavailable() throws Exception {
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		GitRepository existing = existing("ci-refused", pom("ci-refused", ENGINE_VERSION));
		TLObject project = project("ci-refused", existing.getFullName());
		assertFalse(FactoryFunctions.ciEnabled());
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, service.ciSetUpUnavailable(project));
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, FactoryFunctions.ciSetUpUnavailable(project));

		TestingProjectWorkspaces.setCI(_ci);
		assertTrue(FactoryFunctions.ciEnabled());
		TLObject withoutRepository = project("ci-no-repository", null);
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_REPOSITORY__PROJECT.fill(withoutRepository),
			service.ciSetUpUnavailable(withoutRepository));
		assertSetUpRefused(com.top_logic.factory.ci.I18NConstants.ERROR_NO_SOURCES__PROJECT.fill(project), project);

		withSources(project);
		assertNull(service.ciSetUpUnavailable(project));
		TLObject own = ownConnection();
		commit(() -> {
			project.tUpdateByName(FactoryModel.PROJECT_GIT_CONNECTION, own);
			return null;
		});
		ResKey ownConnection =
			com.top_logic.factory.ci.I18NConstants.ERROR_CI_NEEDS_BUILT_IN_CONNECTION__PROJECT.fill(project);
		assertSetUpRefused(ownConnection, project);
		assertEquals(ownConnection, service.ciUnavailable(project));
		assertEquals("Nothing done.", List.of(), _ci.calls());
		assertEquals(0, count(RecordingGitHostingProvider.COMMIT_FILE));
	}

	private void assertSetUpRefused(ResKey expected, TLObject project) {
		assertEquals(expected, ProjectWorkspaces.getInstance().ciSetUpUnavailable(project));
		try {
			ProjectWorkspaces.getInstance().setUpCI(project);
			fail("Set up: " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	/**
	 * A build is started only for a project with CI job; the job is created again if the CI
	 * server lost it. The last build is read for the views.
	 */
	public void testStartBuild() throws Exception {
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		TLObject project = project("ci-command", null);
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, service.ciUnavailable(project));
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI, FactoryFunctions.ciUnavailable(project));

		TestingProjectWorkspaces.setCI(_ci);
		assertEquals(com.top_logic.factory.ci.I18NConstants.ERROR_NO_REPOSITORY__PROJECT.fill(project),
			service.ciUnavailable(project));
		assertNull(FactoryFunctions.lastBuild(project));

		GitRepository existing = existing("ci-command", pom("ci-command", ENGINE_VERSION));
		TLObject adopted = withSources(project("ci-command", existing.getFullName()));
		ResKey noJob = com.top_logic.factory.ci.I18NConstants.ERROR_NO_CI_JOB__PROJECT.fill(adopted);
		assertEquals(noJob, service.ciUnavailable(adopted));
		try {
			service.startBuild(adopted);
			fail("A build without CI job.");
		} catch (TopLogicException ex) {
			assertEquals(noJob, ex.getErrorKey());
		}
		assertEquals("No job created by a build.", List.of(), _ci.calls());

		service.setUpCI(adopted);
		HistoryUtils.updateSessionRevision();
		String job = RecordingCIProvider.jobName(existing);
		CIBuildStart started = service.startBuild(adopted);
		assertEquals(Integer.valueOf(2), started.number());
		assertEquals(com.top_logic.factory.ci.I18NConstants.BUILD_STARTED__NUMBER_JOB.fill(3, job),
			FactoryFunctions.startBuild(adopted));

		_ci.finish(job, CIBuildState.SUCCESS);
		CIBuild last = service.lastBuild(adopted);
		assertEquals(3, last.number());
		assertEquals(CIBuildState.SUCCESS, last.state());
		Map<String, Object> shown = FactoryFunctions.lastBuild(adopted);
		assertEquals(Integer.valueOf(3), shown.get(FactoryFunctions.BUILD_NUMBER));
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
	 * The pages of repository and CI job recorded on a project follow the URLs the services report
	 * after their public URLs changed; a project whose service fails keeps its page.
	 */
	public void testRefreshLinks() throws Exception {
		ProjectWorkspaces service = ProjectWorkspaces.getInstance();
		TestingProjectWorkspaces.setCI(_ci);
		GitRepository existing = existing("moved-links", pom("moved-links", ENGINE_VERSION));
		TLObject project = withSources(project("moved-links", existing.getFullName()));
		service.setUpCI(project);
		HistoryUtils.updateSessionRevision();
		String job = RecordingCIProvider.jobName(existing);

		service.refreshLinks();
		HistoryUtils.updateSessionRevision();
		assertEquals(existing.getWebUrl(), FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY_URL));
		assertEquals(RecordingCIProvider.jobUrl(job), FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL));

		String gitRoot = "https://git.public.test/";
		String ciRoot = "https://ci.public.test/";
		_hosting.setRootUrl(gitRoot);
		_ci.setRootUrl(ciRoot);
		_ci.setFailure(new TopLogicException(com.top_logic.factory.ci.I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM
			.fill(RecordingCIProvider.BASE_URL, "down")));
		service.refreshLinks();
		HistoryUtils.updateSessionRevision();
		assertEquals(gitRoot + existing.getFullName(),
			FactoryModel.getString(project, FactoryModel.PROJECT_REPOSITORY_URL));
		assertEquals("The CI server is down.", RecordingCIProvider.jobUrl(job),
			FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL));

		_ci.setFailure(null);
		service.refreshLinks();
		HistoryUtils.updateSessionRevision();
		assertEquals(RecordingCIProvider.jobUrl(ciRoot, job),
			FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL));
		assertEquals("Found by its public URL.", gitRoot + existing.getFullName(),
			_hosting.findRepository(gitRoot + existing.getFullName() + RepositoryName.GIT_SUFFIX).getWebUrl());
	}

	/**
	 * The pipeline file is protected only with CI: it is removed from the protected files of a
	 * project without CI job, and added for one with a CI job, unless it is listed.
	 */
	public void testProtectedFilesWithCI() {
		RecordingCIProvider other = new RecordingCIProvider() {
			@Override
			public String pipelineFile() {
				return "ci/pipeline.groovy";
			}
		};
		assertEquals(List.of("Jenkinsfile", "deploy/**", "ci/pipeline.groovy"),
			ProjectCI.withPipelineFile(List.of("Jenkinsfile", "deploy/**"), other));
		assertEquals(List.of("Jenkinsfile"), ProjectCI.withPipelineFile(List.of("Jenkinsfile"), _ci));
		assertEquals(List.of("deploy/**"), ProjectCI.withoutPipelineFile(List.of("Jenkinsfile", "deploy/**"), _ci));
		assertEquals(List.of("Jenkinsfile", "deploy/**"),
			ProjectCI.withoutPipelineFile(List.of("Jenkinsfile", "deploy/**"), null));

		TLObject project = project("files-with-ci", null);
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES), ProjectWorkspaces.protectedFiles(project, _ci));
		commit(() -> {
			FactoryModel.setCIJob(project, new CIJob("job", RecordingCIProvider.jobUrl("job")));
			return null;
		});
		assertEquals(List.of(GitHostingService.DEFAULT_PROTECTED_FILES, RecordingCIProvider.PIPELINE_FILE),
			ProjectWorkspaces.protectedFiles(project, _ci));
		assertEquals("Without CI server.", List.of(GitHostingService.DEFAULT_PROTECTED_FILES),
			ProjectWorkspaces.protectedFiles(project, null));
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
	 * Records for the given project that a workspace brought its application into its repository,
	 * as the creation of the workspace does when it protects the branch.
	 */
	private static TLObject withSources(TLObject project) {
		return commit(() -> {
			project.tUpdateByName(FactoryModel.PROJECT_BRANCH_PROTECTED, Boolean.TRUE);
			return project;
		});
	}

	/**
	 * A git connection of the test tenant to the {@link RecordingGitHostingProvider}.
	 */
	private TLObject ownConnection() {
		TLObject connection = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.gitConnectionType());
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_NAME, "Own connection of the repository test");
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_PROVIDER_TYPE, RecordingGitProviderType.NAME);
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_URL, RecordingGitHostingProvider.BASE_URL);
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_OWNER, OTHER_OWNER);
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_TENANT, TestTenants.testTenant());
			result.tUpdateByName(FactoryModel.GIT_CONNECTION_SECRET_NAME, "tl_factory_git_repository_test");
			return result;
		});
		_connections.add(connection);
		return connection;
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
