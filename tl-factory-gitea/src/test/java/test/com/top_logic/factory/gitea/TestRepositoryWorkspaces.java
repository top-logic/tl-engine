/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

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

import test.com.top_logic.factory.gitea.GiteaTestServer.ExecResult;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.git.RepositorySource;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.gitea.client.api.RepositoryApi;
import com.top_logic.factory.gitea.client.model.BranchProtection;
import com.top_logic.factory.proxy.ProcessToken;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyListener;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;

/**
 * Integration test of the repository of a project ({@link RepositorySource}): Docker workspaces
 * with TopLogic applications whose sources live in a repository of a Gitea in a Docker container
 * ({@link GiteaTestServer}), reached through the {@link GitRoute git route} of a workspace proxy
 * on the host.
 *
 * <p>
 * The first workspace creates the repository, pushes the generated application to its default
 * branch and protects it; a workspace created anew for the same project clones it; a second
 * project adopting the repository by its clone URL clones it as well. Every workspace builds its
 * application.
 * </p>
 *
 * <p>
 * The test builds the workspace image, fills the release repository of the engine version and
 * builds three applications, which takes minutes. It only runs with the system property
 * {@value #ENABLE_PROPERTY}{@code =true}, a Docker daemon and the Gitea image of the build:
 * </p>
 *
 * <pre>
 * mvn test -DskipTests=false -pl tl-factory-gitea -Dtest=test.com.top_logic.factory.gitea.TestRepositoryWorkspaces -Dtl.factory.integration=true
 * </pre>
 *
 * <p>
 * The workspaces reach the proxy on the host as {@value #GATEWAY_HOST}; the proxy reaches Gitea at
 * its published loopback port. Like the integration test of the workspaces of tl-factory, the test
 * shares the label namespace, name prefix and network {@value #NAME_PREFIX}, so that it reuses the
 * release repository that test keeps.
 * </p>
 */
public class TestRepositoryWorkspaces extends TestCase {

	/**
	 * System property enabling this test.
	 */
	public static final String ENABLE_PROPERTY = "tl.factory.integration";

	private static final String NAME_PREFIX = "tl-factory-it";

	private static final String LABEL_NAMESPACE = "test.tl-factory.it";

	private static final String GATEWAY_HOST = "host.docker.internal";

	private static final String ANY_ADDRESS = "0.0.0.0";

	private static final int HOST_PORT_MIN = 23000;

	private static final int HOST_PORT_MAX = 23999;

	private static final String GROUP_ID = "com.example.factory";

	private static final String ARTIFACT_ID = "repo-it-app";

	private static final String BRANCH = GiteaTestServer.BRANCH;

	private static final String USER_NAME = "Factory IT";

	private static final String USER_EMAIL = "factory-it@example.com";

	private static final List<String> PROTECTED_FILES = List.of("Jenkinsfile", "deploy/**");

	/**
	 * Directory of the bare repositories in the Gitea container.
	 */
	private static final String GITEA_REPOSITORIES = "/data/git/repositories/";

	private static final Duration GIT_TIMEOUT = Duration.ofMinutes(5);

	private static GiteaTestServer _server;

	private GiteaHostingProvider _hosting;

	private final TokenGrants _grants = new TokenGrants(Clock.systemUTC(), workspace -> true, 0);

	/**
	 * The repository of the project of each workspace, as the projects record it.
	 */
	private final Map<String, RepositoryName> _repositories = new ConcurrentHashMap<>();

	private GitRoute _route;

	private WorkspaceProxyListener _listener;

	private String _proxyRoot;

	private ProxyTokens _tokens;

	private DockerWorkspaceProvider _provider;

	private ApplicationWorkspaces _applications;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_hosting = _server.provider(_server.token());
		_route = new GitRoute(GitRoute.DEFAULT_PATH, () -> _hosting, _repositories::get, Duration.ofSeconds(10),
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
	 * A new project gets a repository with the generated application on its protected default
	 * branch; the workspace created anew clones it; a second project adopts it by its clone URL.
	 */
	public void testCreateCleanupAdopt() throws Exception {
		// A project without repository.
		String first = workspaceId();
		RepositorySource created = source(first, null);
		create(first, created);
		RepositoryName name = _repositories.get(first);
		assertEquals(new RepositoryName(GiteaTestServer.ORG, ARTIFACT_ID), name);
		GitRepository repository = _hosting.findRepository(name.getFullName());
		assertNotNull("Repository created.", repository);
		assertEquals(BRANCH, repository.getDefaultBranch());

		String head = head(first);
		assertEquals("The generated application is pushed to the default branch.", head, giteaHead(name));
		assertEquals(USER_NAME + " <" + USER_EMAIL + ">",
			output(first, "git", "log", "-1", "--format=%an <%ae>").strip());
		assertTrue(output(first, "git", "log", "-1", "--format=%s").startsWith(RepositorySource.INITIAL_COMMIT_MESSAGE));
		assertEquals("Nothing left uncommitted.", "", output(first, "git", "status", "--porcelain").strip());

		BranchProtection protection =
			new RepositoryApi(_server.client(_server.token())).repoGetBranchProtection(name.getOwner(), name.getName(),
				BRANCH);
		assertEquals(String.join(";", PROTECTED_FILES), protection.getProtectedFilePatterns());
		assertEquals(Boolean.FALSE, protection.getEnableForcePush());

		// The workspace is removed and created anew: the repository is cloned.
		_applications.remove(first);
		String second = workspaceId();
		create(second, source(second, name.getFullName()));
		assertEquals("The clone has the pushed application.", head, head(second));

		// The agent pushes through the proxy; the protected files are refused.
		ExecResult jenkins = agentGit(second, "echo pipeline > Jenkinsfile && git add Jenkinsfile"
			+ " && git commit -q -m pipeline && git push origin HEAD");
		assertTrue("Push changing the Jenkinsfile accepted.", jenkins.exitCode() != 0);
		assertTrue(jenkins.err() + jenkins.out(), (jenkins.err() + jenkins.out()).contains("protected"));
		ExecResult change = agentGit(second, "git reset -q --hard HEAD~1 && echo change >> README.md"
			+ " && git add README.md && git commit -q -m change && git push origin HEAD");
		assertEquals(change.err() + change.out(), 0, change.exitCode());
		String changed = head(second);
		assertEquals(changed, giteaHead(name));
		_applications.remove(second);

		// A second project adopts the repository by its clone URL.
		String third = workspaceId();
		RepositorySource adopted = source(third, repository.getCloneUrl());
		create(third, adopted);
		assertEquals("Recorded under its full name.", name, _repositories.get(third));
		assertEquals(changed, head(third));
	}

	private RepositorySource source(String workspaceId, String reference) {
		return new RepositorySource(_hosting, _tokens, _proxyRoot + GitRoute.DEFAULT_PATH, reference,
			"Repository integration test", PROTECTED_FILES,
			repository -> _repositories.put(workspaceId, repository.getName()));
	}

	private void create(String workspaceId, RepositorySource source) throws InterruptedException {
		ApplicationSpec spec = new ApplicationSpec(workspaceId, GROUP_ID, ARTIFACT_ID).setSource(source);
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
	}

	/**
	 * Runs a shell command line in the application directory of a workspace with the environment
	 * of an agent process, whose token is revoked afterwards.
	 */
	private ExecResult agentGit(String workspaceId, String commandLine) throws InterruptedException {
		try (ProcessToken token = _tokens.issueProcessToken(workspaceId, GIT_TIMEOUT)) {
			StringBuilder out = new StringBuilder();
			StringBuilder err = new StringBuilder();
			int exitCode = _provider.exec(workspaceId, ExecCommand.of("sh", "-c", commandLine)
				.setWorkingDir(ApplicationWorkspaces.appDir(ARTIFACT_ID))
				.putEnv(token.env())
				.setTimeout(GIT_TIMEOUT), new ExecOutput() {
					@Override
					public void stdout(String text) {
						out.append(text);
					}

					@Override
					public void stderr(String text) {
						err.append(text);
					}
				});
			return new ExecResult(exitCode, out.toString(), err.toString());
		}
	}

	private String head(String workspaceId) throws InterruptedException {
		return output(workspaceId, "git", "rev-parse", "HEAD").strip();
	}

	private String output(String workspaceId, String... command) throws InterruptedException {
		StringBuilder out = new StringBuilder();
		int exitCode = _provider.exec(workspaceId,
			ExecCommand.of(command).setWorkingDir(ApplicationWorkspaces.appDir(ARTIFACT_ID)), new ExecOutput() {
				@Override
				public void stdout(String text) {
					out.append(text);
				}
			});
		assertEquals(String.join(" ", command) + ": " + out, 0, exitCode);
		return out.toString();
	}

	/**
	 * The commit the default branch of the repository is at in Gitea.
	 */
	private static String giteaHead(RepositoryName name) throws InterruptedException {
		ExecResult result = _server.git("git -C " + GITEA_REPOSITORIES + name.getFullName().toLowerCase()
			+ RepositoryName.GIT_SUFFIX + " rev-parse refs/heads/" + BRANCH);
		assertEquals(result.err(), 0, result.exitCode());
		return result.out().strip();
	}

	private static String workspaceId() {
		return "repo-" + UUID.randomUUID().toString().substring(0, 8);
	}

	private static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? "unix:///var/run/docker.sock" : host;
	}

	private static DockerWorkspaceProvider createProvider() {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, dockerHost());
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
	 * The tests of this class around a Gitea server, none unless enabled by
	 * {@link #ENABLE_PROPERTY}, a Docker daemon is reachable and the Gitea image is given.
	 */
	public static Test suite() {
		String name = TestRepositoryWorkspaces.class.getName();
		if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
			System.err.println("Skipping " + name + ": enable it with -D" + ENABLE_PROPERTY + "=true.");
			return new TestSuite(name);
		}
		return GiteaTestServer.suite(TestRepositoryWorkspaces.class, server -> _server = server);
	}

}
