/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.jenkins;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.gitea.GiteaTestServer;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIConsole;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIProvider;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.ci.ProjectCI;
import com.top_logic.factory.git.GitConnectionSettings;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.git.GitProviderType;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.git.RepositorySource;
import com.top_logic.factory.gitea.GiteaProviderType;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyListener;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;

/**
 * End-to-end test of the repository and the CI job of a new project against an external Gitea and
 * Jenkins, e.g. the local development setup of {@code tl-factory-app} ({@code tl-factory-app/devel}).
 *
 * <p>
 * {@link GitHostingService} and {@link CIService} are started from the configuration of the
 * modules, i.e. from the same environment variables and secrets as the control application; the
 * repository lives on a connection of the Gitea type to the Gitea at {@value #ENV_GITEA_URL}, in
 * the organization {@value #ENV_GITEA_OWNER} (default {@value #DEFAULT_GITEA_OWNER}), authenticated
 * with the token in the secret {@link GiteaProviderType#TOKEN_SECRET}, as the built-in connection
 * of {@code tl-factory-app} does. A Docker
 * workspace generates a new application and pushes it to a new repository through the
 * {@link GitRoute git route} of a workspace proxy on the host, which protects the branch without
 * CI. Then the CI of the project is set up ({@link ProjectCI#setUp(CIProvider, GitHostingProvider,
 * GitRepository, String, List, String, java.util.function.Consumer, java.util.function.Consumer)}):
 * the default pipeline definition is committed through the API of Gitea, the protection extended
 * by it, the CI job created and its first build started; the test waits for that build and expects
 * it to succeed. The end of its console output is printed.
 * </p>
 *
 * <p>
 * The test runs only with the system property {@value #ENABLE_PROPERTY}{@code =true}, a Docker
 * daemon and the environment variables {@value #ENV_GITEA_URL} and {@value #ENV_JENKINS_URL}; with
 * the local development setup running:
 * </p>
 *
 * <pre>
 * export tl_factory_gitea_url=http://localhost:3300/ tl_factory_jenkins_url=http://localhost:8380/
 * export tl_factory_jenkins_scm_credentials=tl-factory-gitea
 * export tl_factory_gitea_token="$(cat ~/.top-logic/secrets/tl_factory_gitea_token)"
 * export tl_factory_jenkins_token="$(cat ~/.top-logic/secrets/tl_factory_jenkins_token)"
 * mvn test -DskipTests=false -pl tl-factory-jenkins -Dtest=test.com.top_logic.factory.jenkins.TestProjectCI -Dtl.factory.integration=true
 * </pre>
 *
 * <p>
 * Every run leaves a repository and a job of a new name behind. The workspace containers share the
 * label namespace, name prefix and network {@value #NAME_PREFIX} with the integration tests of
 * tl-factory, whose release repository they reuse.
 * </p>
 */
public class TestProjectCI extends TestCase {

	/**
	 * System property enabling this test.
	 */
	public static final String ENABLE_PROPERTY = "tl.factory.integration";

	/**
	 * Environment variable with the URL of the Gitea, read by the configuration of the built-in
	 * git connection of {@code tl-factory-app}.
	 */
	public static final String ENV_GITEA_URL = "tl_factory_gitea_url";

	/**
	 * Environment variable with the organization the repository is created in.
	 */
	public static final String ENV_GITEA_OWNER = "tl_factory_gitea_owner";

	/**
	 * Default of {@link #ENV_GITEA_OWNER}, the organization of the local development setup.
	 */
	public static final String DEFAULT_GITEA_OWNER = "tl-factory";

	/**
	 * Environment variable with the URL of the Jenkins, read by the configuration of
	 * {@link CIService} of {@code tl-factory-jenkins}.
	 */
	public static final String ENV_JENKINS_URL = "tl_factory_jenkins_url";

	private static final String NAME_PREFIX = "tl-factory-it";

	private static final String LABEL_NAMESPACE = "test.tl-factory.it";

	private static final String GATEWAY_HOST = "host.docker.internal";

	private static final String ANY_ADDRESS = "0.0.0.0";

	private static final int HOST_PORT_MIN = 23000;

	private static final int HOST_PORT_MAX = 23999;

	private static final String GROUP_ID = "com.example.factory";

	private static final String USER_NAME = "Factory CI";

	private static final String USER_EMAIL = "factory-ci@example.com";

	/**
	 * How long the first build may take: it fills the Maven repository of the CI server.
	 */
	private static final Duration BUILD_TIMEOUT = Duration.ofMinutes(45);

	private static final Duration POLL_INTERVAL = Duration.ofSeconds(10);

	/**
	 * Bytes of the end of the console output that are read.
	 */
	private static final int CONSOLE_BYTES = 16 * 1024;

	/**
	 * Lines of the end of the console output that are printed.
	 */
	private static final int CONSOLE_LINES = 20;

	private final TokenGrants _grants = new TokenGrants(Clock.systemUTC(), workspace -> true, 0);

	private final Map<String, RepositoryName> _repositories = new ConcurrentHashMap<>();

	private GitHostingProvider _hosting;

	private CIProvider _ci;

	private GitRoute _route;

	private WorkspaceProxyListener _listener;

	private String _proxyRoot;

	private ProxyTokens _tokens;

	private DockerWorkspaceProvider _provider;

	private ApplicationWorkspaces _applications;

	private CIJob _job;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		GitProviderType gitea = GitHostingService.getInstance().getProviderType(GiteaProviderType.NAME);
		assertNotNull("The Gitea type is registered by tl-factory-gitea.", gitea);
		assertTrue("CI is switched on by " + ENV_JENKINS_URL + ".", CIService.getInstance().isEnabled());
		String owner = System.getenv(ENV_GITEA_OWNER);
		_hosting = gitea.createProvider(new GitConnectionSettings(System.getenv(ENV_GITEA_URL),
			owner == null || owner.isBlank() ? DEFAULT_GITEA_OWNER : owner,
			gitea.tokenCredentials(GiteaProviderType.TOKEN_SECRET), false));
		_ci = CIService.getInstance().getProvider();
		_route = new GitRoute(GitRoute.DEFAULT_PATH,
			workspace -> GitRoute.Target.of(_hosting, _repositories.get(workspace)), Duration.ofSeconds(10),
			Duration.ofMinutes(10), USER_NAME, USER_EMAIL);
		WorkspaceProxy proxy = new WorkspaceProxy() {
			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public List<GitRoute> routes() {
				return List.of(_route);
			}

			@Override
			public boolean isAllowedSource(String remoteAddress) {
				return true;
			}

			@Override
			public ProxyAdmission admit(String token, String remoteAddress) {
				return _grants.admit(token, remoteAddress);
			}
		};
		// The containers reach the host through the gateway of Docker.
		_listener = new WorkspaceProxyListener(new InetSocketAddress(ANY_ADDRESS, 0), () -> proxy);
		_proxyRoot = "http://" + GATEWAY_HOST + ":" + _listener.getAddress().getPort();
		_tokens = new ProxyTokens() {
			@Override
			public String issue(String workspaceId, Duration lifetime) {
				return _grants.issue(workspaceId, lifetime);
			}

			@Override
			public Instant expiresAt(String token) {
				return _grants.expiresAt(token);
			}

			@Override
			public void revoke(String token) {
				_grants.revoke(token);
			}

			@Override
			public Map<String, String> processEnv(String token) {
				return WorkspaceProxyService.processEnv(List.of(_route), _proxyRoot, token);
			}
		};
		_provider = createProvider();
		_applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), _provider);
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_provider.cleanup(Collections.emptyList());
		} finally {
			_provider.close();
			_listener.close();
			_route.close();
			super.tearDown();
		}
	}

	/**
	 * A new project gets a repository with the generated application; setting up its CI commits
	 * the default pipeline definition, creates a CI job, and its first build of the default pipeline
	 * succeeds.
	 */
	public void testNewProjectBuilds() throws Exception {
		// A letter first: each part of the artifact ID becomes a name of the Java package.
		String suffix = "r" + UUID.randomUUID().toString().substring(0, 8);
		String workspaceId = "ci-" + suffix;
		String artifactId = "ci-app-" + suffix;
		assertNull(ProjectWorkspaces.checkArtifactId(artifactId));
		GitHostingService hostingService = GitHostingService.getInstance();
		CIService ciService = CIService.getInstance();
		List<String> protectedFiles = ProjectCI.withoutPipelineFile(hostingService.getProtectedFiles(), _ci);
		RepositorySource source = new RepositorySource(_hosting, _tokens, _proxyRoot + GitRoute.DEFAULT_PATH, null,
			"CI integration test", protectedFiles,
			repository -> _repositories.put(workspaceId, repository.getName()));

		create(workspaceId, artifactId, source);
		GitRepository repository = _hosting.findRepository(_repositories.get(workspaceId).getFullName());
		assertNotNull("Repository created.", repository);
		System.out.println("Repository: " + repository.getWebUrl());
		assertNull("No CI job before the set up.", _job);

		ProjectCI.Setup setup = ProjectCI.setUp(_ci, _hosting, repository, ciService.pipelineTemplate(),
			protectedFiles, "CI integration test " + artifactId, job -> _job = job, protection -> {
				// Not recorded.
			});
		assertTrue("The new repository gets the default pipeline.", setup.pipelineAdded());
		assertTrue("The token administers the repository.", setup.pipelineProtected());
		assertEquals(setup.job(), _job);
		System.out.println("CI job: " + _job.webUrl());
		assertFalse("The pipeline is committed once.", _hosting.commitFile(repository,
			repository.getDefaultBranch(), _ci.pipelineFile(), ciService.pipelineTemplate(), "Again"));

		CIBuild build = awaitBuild(_job.name());
		CIConsole console = _ci.console(_job.name(), build.number(), CONSOLE_BYTES);
		List<String> lines = console.text().lines().toList();
		System.out.println("Build " + build.number() + " " + build.state() + " after " + build.duration() + ": "
			+ build.webUrl());
		for (String line : lines.subList(Math.max(0, lines.size() - CONSOLE_LINES), lines.size())) {
			System.out.println("  " + line);
		}
		assertEquals(console.text(), CIBuildState.SUCCESS, build.state());
	}

	private CIBuild awaitBuild(String job) throws InterruptedException {
		long deadline = System.nanoTime() + BUILD_TIMEOUT.toNanos();
		while (true) {
			CIBuild build = _ci.lastBuild(job);
			if (build != null && build.state() != CIBuildState.RUNNING) {
				return build;
			}
			if (System.nanoTime() > deadline) {
				fail("The first build of " + job + " did not end within " + BUILD_TIMEOUT + ".");
			}
			Thread.sleep(POLL_INTERVAL.toMillis());
		}
	}

	private String create(String workspaceId, String artifactId, RepositorySource source)
			throws InterruptedException {
		ApplicationSpec spec = new ApplicationSpec(workspaceId, GROUP_ID, artifactId).setSource(source);
		StringBuilder log = new StringBuilder();
		try {
			_applications.create(spec, new ExecOutput() {
				@Override
				public void stdout(String text) {
					append(text);
				}

				@Override
				public void stderr(String text) {
					append(text);
				}

				private void append(String text) {
					synchronized (log) {
						log.append(text);
						if (text.startsWith("[tl-factory]")) {
							System.out.print(text);
						}
					}
				}
			});
		} catch (RuntimeException ex) {
			synchronized (log) {
				int start = Math.max(0, log.length() - 8000);
				throw new AssertionError("Creating " + workspaceId + " failed: " + ex.getMessage() + "\n"
					+ log.substring(start), ex);
			}
		}
		synchronized (log) {
			return log.toString();
		}
	}

	private static DockerWorkspaceProvider createProvider() {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, GiteaTestServer.dockerHost());
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NETWORK, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NAME_PREFIX, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.LABEL_NAMESPACE, LABEL_NAMESPACE);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MIN, HOST_PORT_MIN);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MAX, HOST_PORT_MAX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.EXTRA_HOSTS,
			List.of(GATEWAY_HOST + ":host-gateway"));
		return (DockerWorkspaceProvider) TypedConfigUtil.createInstance(config);
	}

	/**
	 * The test, none unless enabled by {@link #ENABLE_PROPERTY}, the Gitea and the Jenkins are given
	 * by {@link #ENV_GITEA_URL} and {@link #ENV_JENKINS_URL}, and a Docker daemon is reachable.
	 */
	public static Test suite() {
		String name = TestProjectCI.class.getName();
		if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
			System.err.println("Skipping " + name + ": enable it with -D" + ENABLE_PROPERTY + "=true.");
			return new TestSuite(name);
		}
		for (String variable : List.of(ENV_GITEA_URL, ENV_JENKINS_URL)) {
			String value = System.getenv(variable);
			if (value == null || value.isBlank()) {
				System.err.println("Skipping " + name + ": no URL in the environment variable " + variable + ".");
				return new TestSuite(name);
			}
		}
		if (!GiteaTestServer.dockerAvailable()) {
			System.err.println("Skipping " + name + ": no Docker daemon reachable at " + GiteaTestServer.dockerHost() + ".");
			return new TestSuite(name);
		}
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(new TestSuite(TestProjectCI.class),
			TypeIndex.Module.INSTANCE, SecretStoreService.Module.INSTANCE, GitHostingService.Module.INSTANCE,
			CIService.Module.INSTANCE));
	}

}
