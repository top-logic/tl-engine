/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.factory.gitea.GiteaTestServer.ExecResult;

import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxy;
import com.top_logic.factory.proxy.WorkspaceProxyListener;
import com.top_logic.factory.proxy.WorkspaceProxyService;

/**
 * End-to-end test of the {@link GitRoute git route} of the workspace proxy against a Gitea in a
 * Docker container ({@link GiteaTestServer}): the git command line of the host, with nothing but
 * the environment of a workspace process ({@link WorkspaceProxyService#processEnv(List, String,
 * String)}), clones, pushes and fetches through the proxy on a port of its own, which adds the
 * token of the control application.
 *
 * <p>
 * Without Docker or Gitea image, or without git on the path, the suite contains no tests.
 * </p>
 */
public class TestGitRouteGitea extends TestCase {

	private static final String WS_ALPHA = "ws-alpha";

	private static final String WS_BETA = "ws-beta";

	private static final String BRANCH = GiteaTestServer.BRANCH;

	private static final Duration GIT_TIMEOUT = Duration.ofSeconds(60);

	/**
	 * Prefix of the variables of git's environment, which the commands of the test do not inherit.
	 */
	private static final String GIT_VARIABLE_PREFIX = "GIT_";

	private static GiteaTestServer _server;

	private GiteaHostingProvider _provider;

	private final Map<String, RepositoryName> _repositories = new ConcurrentHashMap<>();

	private final TokenGrants _grants = new TokenGrants(Clock.systemUTC(), workspace -> true, 0);

	private GitRoute _route;

	private WorkspaceProxyListener _listener;

	private String _proxyRoot;

	private String _routeUrl;

	private Path _dir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_provider = _server.provider(_server.token());
		_route = new GitRoute(GitRoute.DEFAULT_PATH,
			workspace -> GitRoute.Target.of(_provider, _repositories.get(workspace)), Duration.ofSeconds(10),
			Duration.ofMinutes(5));
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
		_listener = new WorkspaceProxyListener(new InetSocketAddress(GiteaTestServer.LOOPBACK, 0), () -> proxy);
		_proxyRoot = "http://" + GiteaTestServer.LOOPBACK + ":" + _listener.getAddress().getPort();
		_routeUrl = _proxyRoot + GitRoute.DEFAULT_PATH;
		_dir = Files.createTempDirectory("git-route-gitea");
		Files.writeString(_dir.resolve(".gitconfig"), "");
	}

	@Override
	protected void tearDown() throws Exception {
		_grants.revokeAll(WS_ALPHA);
		_grants.revokeAll(WS_BETA);
		_listener.close();
		_route.close();
		try (Stream<Path> paths = Files.walk(_dir)) {
			for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(path);
			}
		}
		super.tearDown();
	}

	/**
	 * Clone of an empty repository, the first and a second push, a fetch with protocol version 2;
	 * the repository of another project is not found, a revoked token is refused.
	 */
	public void testCloneAndPushThroughProxy() throws Exception {
		GitRepository repository = _provider.createRepository("through-proxy", null);
		_repositories.put(WS_ALPHA, repository.getName());
		Map<String, String> env = processEnv(WS_ALPHA);
		String remote = GitRoute.remoteUrl(_routeUrl, repository.getName());

		assertSucceeded("Clone of the empty repository.", git(env, _dir, "clone", remote, "work"));
		Path work = _dir.resolve("work");
		assertSucceeded("Branch.", git(env, work, "symbolic-ref", "HEAD", "refs/heads/" + BRANCH));
		commit(env, work, "README.md", "one");
		assertSucceeded("First push.", git(env, work, "push", "origin", BRANCH));
		assertEquals(head(env, work), remoteHead(env, work));

		commit(env, work, "README.md", "two");
		assertSucceeded("Second push.", git(env, work, "push", "origin", BRANCH));
		assertEquals(head(env, work), remoteHead(env, work));

		assertSucceeded("Second clone.", git(env, _dir, "clone", remote, "second"));
		Path second = _dir.resolve("second");
		commit(env, work, "README.md", "three");
		assertSucceeded("Third push.", git(env, work, "push", "origin", BRANCH));

		Map<String, String> traced = new LinkedHashMap<>(env);
		traced.put("GIT_TRACE_PACKET", "1");
		ExecResult fetch = git(traced, second, "-c", "protocol.version=2", "fetch", "origin");
		assertSucceeded("Fetch with protocol version 2.", fetch);
		assertTrue("Protocol version 2 spoken: " + fetch.err(), fetch.err().contains("git< version 2"));
		assertEquals(head(env, work), git(env, second, "rev-parse", "origin/" + BRANCH).out().strip());

		GitRepository other = _provider.createRepository("other-project", null);
		_repositories.put(WS_BETA, other.getName());
		ExecResult foreign = git(env, work, "push", GitRoute.remoteUrl(_routeUrl, other.getName()), BRANCH);
		assertTrue("Push to the repository of another project accepted.", foreign.exitCode() != 0);
		assertTrue(foreign.err(), foreign.err().contains("not found"));

		_grants.revokeAll(WS_ALPHA);
		commit(env, work, "README.md", "four");
		ExecResult revoked = git(env, work, "push", "origin", BRANCH);
		assertTrue("Push with a revoked token accepted.", revoked.exitCode() != 0);
		assertTrue(revoked.err(), revoked.err().contains("Authentication failed"));
	}

	/**
	 * Without the credential helper of the route, git is challenged by the proxy and has no
	 * credentials to answer with.
	 */
	public void testChallengeWithoutCredentials() throws Exception {
		GitRepository repository = _provider.createRepository("challenge", null);
		_repositories.put(WS_ALPHA, repository.getName());
		ExecResult clone = git(Map.of(), _dir, "clone", GitRoute.remoteUrl(_routeUrl, repository.getName()), "work");
		assertTrue("Clone without credentials succeeded.", clone.exitCode() != 0);
		assertTrue(clone.err(), clone.err().contains("terminal prompts disabled"));
	}

	/**
	 * The branch protection of the hosting service applies to pushes through the proxy, and its
	 * rejection reaches the git client.
	 */
	public void testProtectionEnforcedThroughProxy() throws Exception {
		GitRepository repository = _provider.createRepository("protected-through-proxy", null);
		_repositories.put(WS_ALPHA, repository.getName());
		Map<String, String> env = processEnv(WS_ALPHA);

		assertSucceeded("Clone.", git(env, _dir, "clone", GitRoute.remoteUrl(_routeUrl, repository.getName()), "work"));
		Path work = _dir.resolve("work");
		assertSucceeded("Branch.", git(env, work, "symbolic-ref", "HEAD", "refs/heads/" + BRANCH));
		commit(env, work, "README.md", "one");
		assertSucceeded("First push.", git(env, work, "push", "origin", BRANCH));
		_provider.protectBranch(repository, BRANCH, List.of("Jenkinsfile"));

		commit(env, work, "Jenkinsfile", "pipeline");
		ExecResult jenkins = git(env, work, "push", "origin", BRANCH);
		assertTrue("Push changing Jenkinsfile accepted.", jenkins.exitCode() != 0);
		assertTrue(jenkins.err(), jenkins.err().contains("protected from changing file"));

		assertSucceeded("Reset.", git(env, work, "reset", "-q", "--hard", "HEAD~1"));
		commit(env, work, "README.md", "two");
		assertSucceeded("Push of a commit.", git(env, work, "push", "origin", BRANCH));

		assertSucceeded("Reset.", git(env, work, "reset", "-q", "--hard", "HEAD~1"));
		commit(env, work, "README.md", "rewritten");
		ExecResult force = git(env, work, "push", "--force", "origin", BRANCH);
		assertTrue("Force push accepted.", force.exitCode() != 0);
		assertTrue(force.err(), force.err().contains("force push"));
	}

	/**
	 * The environment of a process in the given workspace with a token of its own.
	 */
	private Map<String, String> processEnv(String workspaceId) {
		String token = _grants.issue(workspaceId, Duration.ofMinutes(10));
		return WorkspaceProxyService.processEnv(List.of(_route), _proxyRoot, token);
	}

	private void commit(Map<String, String> env, Path work, String file, String content) throws Exception {
		Files.writeString(work.resolve(file), content + "\n");
		assertSucceeded("Add.", git(env, work, "add", file));
		assertSucceeded("Commit.", git(env, work, "commit", "-q", "-m", content));
	}

	private String head(Map<String, String> env, Path work) throws Exception {
		return git(env, work, "rev-parse", "HEAD").out().strip();
	}

	private String remoteHead(Map<String, String> env, Path work) throws Exception {
		ExecResult result = git(env, work, "ls-remote", "origin", "refs/heads/" + BRANCH);
		assertSucceeded("ls-remote.", result);
		return result.out().split("\\s+")[0];
	}

	/**
	 * Runs git of the host in the given directory, with the given environment and the
	 * configuration of an empty home only.
	 */
	private ExecResult git(Map<String, String> env, Path dir, String... args) throws IOException, InterruptedException {
		List<String> command = new ArrayList<>();
		command.add("git");
		command.addAll(List.of(args));
		ProcessBuilder builder = new ProcessBuilder(command).directory(dir.toFile());
		Map<String, String> environment = builder.environment();
		environment.keySet().removeIf(name -> name.startsWith(GIT_VARIABLE_PREFIX));
		environment.put("HOME", _dir.toString());
		environment.put("GIT_CONFIG_NOSYSTEM", "1");
		environment.put("GIT_CONFIG_GLOBAL", _dir.resolve(".gitconfig").toString());
		environment.put("GIT_TERMINAL_PROMPT", "0");
		environment.put("GIT_AUTHOR_NAME", "Test");
		environment.put("GIT_AUTHOR_EMAIL", "test@example.com");
		environment.put("GIT_COMMITTER_NAME", "Test");
		environment.put("GIT_COMMITTER_EMAIL", "test@example.com");
		environment.putAll(env);
		Process process = builder.start();
		process.getOutputStream().close();
		CompletableFuture<String> err = CompletableFuture.supplyAsync(() -> read(process.getErrorStream()));
		String out = read(process.getInputStream());
		if (!process.waitFor(GIT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
			process.destroyForcibly();
			fail("git " + String.join(" ", args) + " did not end within " + GIT_TIMEOUT + ".");
		}
		return new ExecResult(process.exitValue(), out, err.join());
	}

	private static String read(InputStream in) {
		try {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static void assertSucceeded(String message, ExecResult result) {
		assertEquals(message + " " + result.err() + result.out(), 0, result.exitCode());
	}

	private static boolean gitAvailable() {
		try {
			Process process = new ProcessBuilder("git", "--version").redirectErrorStream(true).start();
			process.getInputStream().readAllBytes();
			return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
		} catch (IOException | InterruptedException ex) {
			return false;
		}
	}

	/**
	 * The tests of this class around a Gitea server, none if no Docker daemon is reachable, no
	 * image is given or there is no git.
	 */
	public static Test suite() {
		if (!gitAvailable()) {
			System.err.println("Skipping " + TestGitRouteGitea.class.getName() + ": no git on the path.");
			return new TestSuite(TestGitRouteGitea.class.getName());
		}
		return GiteaTestServer.suite(TestGitRouteGitea.class, server -> _server = server);
	}

}
