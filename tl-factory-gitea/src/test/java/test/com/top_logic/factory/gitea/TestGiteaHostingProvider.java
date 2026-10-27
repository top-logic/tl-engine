/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.gitea.client.ApiClient;
import com.top_logic.factory.gitea.client.api.OrganizationApi;
import com.top_logic.factory.gitea.client.api.RepositoryApi;
import com.top_logic.factory.gitea.client.model.BranchProtection;
import com.top_logic.factory.gitea.client.model.CreateOrgOption;
import com.top_logic.util.error.TopLogicException;

/**
 * Integration test of {@link GiteaHostingProvider} against a Gitea started in a Docker container.
 *
 * <p>
 * The image is given by the system property {@link #IMAGE_PROPERTY}, which the build sets to the
 * Gitea version the client is generated from. The daemon is the one at the environment variable
 * {@code DOCKER_HOST}, or the local socket. If no daemon answers there or the image is not given,
 * the suite contains no tests.
 * </p>
 */
public class TestGiteaHostingProvider extends TestCase {

	/**
	 * System property naming the Gitea image to test against.
	 */
	public static final String IMAGE_PROPERTY = "tl.factory.gitea.image";

	private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

	private static final String LOOPBACK = "127.0.0.1";

	/**
	 * Port of the web interface and the API inside the container.
	 */
	private static final int GITEA_PORT = 3000;

	/**
	 * User the Gitea process runs as in the container, and its commands must.
	 */
	private static final String GITEA_USER = "git";

	/**
	 * Label marking the containers of this test.
	 */
	private static final String CONTAINER_LABEL = "test.tl-factory-gitea";

	private static final String ADMIN = "factory-admin";

	private static final String ADMIN_PASSWORD = "factory-admin-password";

	private static final String ORG = "tl-factory-test";

	private static final String FULL_SCOPES = "write:organization,write:repository";

	private static final String READ_SCOPES = "read:organization,read:repository";

	private static final String BRANCH = "main";

	private static final List<String> PROTECTED = List.of("Jenkinsfile", "deploy/**");

	private static final Duration STARTUP = Duration.ofSeconds(90);

	private static GiteaServer _server;

	/**
	 * A created repository has the clone URL, web URL and default branch of the server, and is
	 * found by both.
	 */
	public void testCreate() {
		GiteaHostingProvider provider = provider(_server.token());
		assertEquals(ORG, provider.getOwner());

		GitRepository created = provider.createRepository("create", "A test repository");
		assertEquals(ORG + "/create", created.getFullName());
		assertEquals(_server.baseUrl() + ORG + "/create.git", created.getCloneUrl());
		assertEquals(_server.baseUrl() + ORG + "/create", created.getWebUrl());
		assertEquals(BRANCH, created.getDefaultBranch());

		assertEquals(created, provider.findRepository(created.getCloneUrl()));
		assertEquals(created, provider.findRepository(created.getWebUrl()));
		assertEquals(created, provider.findRepository(ORG + "/create"));
	}

	/**
	 * A second repository of the same name is rejected.
	 */
	public void testDuplicate() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		provider.createRepository("duplicate", null);
		assertErrorKey(I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY.fill(ORG + "/duplicate"),
			() -> provider.createRepository("duplicate", null));
	}

	/**
	 * A name Gitea does not accept is reported as such.
	 */
	public void testInvalidName() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		assertErrorKey(I18NConstants.ERROR_INVALID_REPOSITORY_NAME__NAME.fill("in valid"),
			() -> provider.createRepository("in valid", null));
	}

	/**
	 * References to repositories that do not exist or are not on the server find nothing.
	 */
	public void testNotFound() {
		GiteaHostingProvider provider = provider(_server.token());
		assertNull(provider.findRepository(ORG + "/missing"));
		assertNull(provider.findRepository(_server.baseUrl() + "nobody/missing.git"));
		assertNull(provider.findRepository("https://elsewhere.example.com/" + ORG + "/missing.git"));
		assertNull(provider.findRepository("missing"));
	}

	/**
	 * The branch protection read back from Gitea, also after it is replaced.
	 */
	public void testProtectBranch() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		GitRepository repository = provider.createRepository("protect", null);

		provider.protectBranch(repository, BRANCH, List.of("Jenkinsfile"));
		provider.protectBranch(repository, BRANCH, PROTECTED);

		BranchProtection protection =
			new RepositoryApi(_server.client(_server.token())).repoGetBranchProtection(ORG, "protect", BRANCH);
		assertEquals(BRANCH, protection.getRuleName());
		assertEquals(Boolean.TRUE, protection.getEnablePush());
		assertEquals(Boolean.FALSE, protection.getEnablePushWhitelist());
		assertEquals(Boolean.FALSE, protection.getEnableForcePush());
		assertEquals(Boolean.FALSE, protection.getBlockAdminMergeOverride());
		assertEquals("Jenkinsfile;deploy/**", protection.getProtectedFilePatterns());
		assertEquals(Long.valueOf(1), protection.getRequiredApprovals());
		assertEquals(Boolean.TRUE, protection.getBlockOnRejectedReviews());
	}

	/**
	 * What the protection of an existing branch enforces on pushes: a push adding commits is
	 * accepted, a push changing a protected file or rewriting the history is rejected - here for the
	 * token of an organization owner and site administrator.
	 */
	public void testProtectionEnforced() throws Exception {
		GiteaHostingProvider provider = provider(_server.token());
		GitRepository repository = provider.createRepository("enforced", null);

		String remote = "http://" + ADMIN + ":" + _server.token() + "@localhost:" + GITEA_PORT + "/" + ORG
			+ "/enforced.git";
		assertSucceeded("Clone.", _server.git("rm -rf /tmp/enforced && git clone -q " + remote + " /tmp/enforced && cd /tmp/enforced"
			+ " && git config user.email test@example.com && git config user.name Test"
			+ " && git symbolic-ref HEAD refs/heads/" + BRANCH
			+ " && echo one > README.md && git add README.md && git commit -qm one"));
		assertSucceeded("First push.", _server.git(push(false)));
		// Gitea rejects the push creating a branch whose rule has protected file patterns.
		provider.protectBranch(repository, BRANCH, PROTECTED);

		assertSucceeded("Commit.", _server.git(inClone("echo two > README.md && git commit -qam two")));
		assertSucceeded("Push of a commit.", _server.git(push(false)));

		_server.git(inClone("echo pipeline > Jenkinsfile && git add Jenkinsfile && git commit -qm jenkins"));
		ExecResult jenkins = _server.git(push(false));
		assertTrue("Push changing Jenkinsfile accepted.", jenkins.exitCode() != 0);
		assertTrue(jenkins.err(), jenkins.err().contains("protected from changing file"));

		_server.git(inClone("git reset -q --hard HEAD~1 && mkdir -p deploy/k8s && echo x > deploy/k8s/app.yaml"
			+ " && git add deploy && git commit -qm deploy"));
		ExecResult deploy = _server.git(push(false));
		assertTrue("Push changing deploy/ accepted.", deploy.exitCode() != 0);

		_server.git(inClone("git reset -q --hard HEAD~2 && echo three > README.md && git commit -qam three"));
		ExecResult force = _server.git(push(true));
		assertTrue("Force push accepted.", force.exitCode() != 0);
		assertTrue(force.err(), force.err().contains("force push"));
	}

	/**
	 * The default credentials send the token from the secret
	 * {@link GiteaHostingProvider#TOKEN_SECRET}.
	 */
	public void testDefaultCredentials() {
		String before = System.getProperty(GiteaHostingProvider.TOKEN_SECRET);
		System.setProperty(GiteaHostingProvider.TOKEN_SECRET, _server.token());
		try {
			GiteaHostingProvider provider = provider(null);
			GitRepository created = provider.createRepository("default-credentials", null);
			assertEquals(created, provider.findRepository(created.getFullName()));
		} finally {
			if (before == null) {
				System.clearProperty(GiteaHostingProvider.TOKEN_SECRET);
			} else {
				System.setProperty(GiteaHostingProvider.TOKEN_SECRET, before);
			}
		}
	}

	/**
	 * A token Gitea does not know is reported as rejected credentials.
	 */
	public void testWrongToken() throws Exception {
		GiteaHostingProvider provider = provider("not-a-token");
		ResKey rejected = I18NConstants.ERROR_AUTHENTICATION_FAILED__SERVICE.fill(_server.baseUrl());
		assertErrorKey(rejected, () -> provider.createRepository("wrong-token", null));
		assertErrorKey(rejected, () -> provider.findRepository(ORG + "/wrong-token"));
	}

	/**
	 * A token without the scopes for creating repositories is reported as missing permission.
	 */
	public void testMissingPermission() throws Exception {
		GiteaHostingProvider provider = provider(_server.readToken());
		assertErrorKey(I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(ORG + "/read-only"),
			() -> provider.createRepository("read-only", null));
	}

	/**
	 * A server that does not answer is reported as unreachable.
	 */
	public void testUnreachable() throws Exception {
		int port = freePort();
		GiteaHostingProvider provider = provider("http://" + LOOPBACK + ":" + port + "/", _server.token());
		try {
			provider.createRepository("unreachable", null);
			fail("Server without listener reached.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM.getKey(),
				ex.getErrorKey().plain().getKey());
		}
	}

	private static void assertSucceeded(String message, ExecResult result) {
		assertEquals(message + " " + result.err() + result.out(), 0, result.exitCode());
	}

	private static String push(boolean force) {
		return inClone("git push" + (force ? " --force" : "") + " origin " + BRANCH);
	}

	private static String inClone(String command) {
		return "cd /tmp/enforced && " + command;
	}

	private static GiteaHostingProvider provider(String token) {
		return provider(_server.baseUrl(), token);
	}

	/**
	 * A provider of the organization {@link #ORG}, authenticated with the given token, or with the
	 * default credentials if {@code null}.
	 */
	private static GiteaHostingProvider provider(String baseUrl, String token) {
		GiteaHostingProvider.Config<?> config = TypedConfiguration.newConfigItem(GiteaHostingProvider.Config.class);
		TypedConfigUtil.setProperty(config, GiteaHostingProvider.Config.BASE_URL, baseUrl);
		TypedConfigUtil.setProperty(config, GiteaHostingProvider.Config.OWNER, ORG);
		TypedConfigUtil.setProperty(config, GiteaHostingProvider.Config.TIMEOUT, 10000L);
		if (token != null) {
			TypedConfigUtil.setProperty(config, GiteaHostingProvider.Config.CREDENTIALS, credentials(token));
		}
		return (GiteaHostingProvider) TypedConfigUtil.createInstance(config);
	}

	private static PolymorphicConfiguration<? extends CredentialProvider> credentials(String token) {
		LiteralSecretSource.Config secret = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		secret.setValue(token);
		return HeaderCredentials.config(CredentialProvider.AUTHORIZATION, GiteaHostingProvider.TOKEN_SCHEME, secret);
	}

	private static void assertErrorKey(ResKey expected, Operation operation) throws Exception {
		try {
			operation.run();
			fail("Expected failure " + expected + ".");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? DEFAULT_DOCKER_HOST : host;
	}

	private static DockerClient createClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
			.withDockerHost(dockerHost())
			.build();
		return DockerClientImpl.getInstance(config,
			new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).build());
	}

	private static boolean dockerAvailable() {
		try (DockerClient client = createClient()) {
			client.pingCmd().exec();
			return true;
		} catch (IOException | RuntimeException ex) {
			return false;
		}
	}

	/**
	 * The tests of this class around a Gitea server, none if no Docker daemon is reachable or no
	 * image is given.
	 */
	public static Test suite() {
		String image = System.getProperty(IMAGE_PROPERTY);
		if (image == null || image.isEmpty()) {
			System.err.println("Skipping " + TestGiteaHostingProvider.class.getName() + ": no Gitea image given in "
				+ IMAGE_PROPERTY + ".");
			return new TestSuite(TestGiteaHostingProvider.class.getName());
		}
		if (!dockerAvailable()) {
			System.err.println("Skipping " + TestGiteaHostingProvider.class.getName()
				+ ": no Docker daemon reachable at " + dockerHost() + ".");
			return new TestSuite(TestGiteaHostingProvider.class.getName());
		}
		Test server = new GiteaSetup(new TestSuite(TestGiteaHostingProvider.class), image);
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(server, TypeIndex.Module.INSTANCE, SecretStoreService.Module.INSTANCE));
	}

	/**
	 * An operation expected to fail.
	 */
	private interface Operation {

		void run() throws Exception;

	}

	/**
	 * The result of a command run in the Gitea container.
	 */
	private record ExecResult(long exitCode, String out, String err) {
		// Pure data.
	}

	/**
	 * Starts the Gitea container before the tests and removes it after them.
	 */
	private static final class GiteaSetup extends TestSetup {

		private final String _image;

		GiteaSetup(Test test, String image) {
			super(test);
			_image = image;
		}

		@Override
		protected void setUp() throws Exception {
			_server = GiteaServer.start(_image);
		}

		@Override
		protected void tearDown() throws Exception {
			if (_server != null) {
				_server.close();
				_server = null;
			}
		}
	}

	/**
	 * A Gitea server in a container, with an administrator, an organization {@link #ORG} and two
	 * access tokens of the administrator.
	 */
	private static final class GiteaServer implements AutoCloseable {

		private final DockerClient _docker;

		private final String _containerId;

		private final String _baseUrl;

		private String _token;

		private String _readToken;

		private GiteaServer(DockerClient docker, String containerId, String baseUrl) {
			_docker = docker;
			_containerId = containerId;
			_baseUrl = baseUrl;
		}

		static GiteaServer start(String image) throws Exception {
			DockerClient docker = createClient();
			try {
				docker.inspectImageCmd(image).exec();
			} catch (NotFoundException ex) {
				docker.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion();
			}

			int port = freePort();
			String baseUrl = "http://" + LOOPBACK + ":" + port + "/";
			ExposedPort http = ExposedPort.tcp(GITEA_PORT);
			String containerId = docker.createContainerCmd(image)
				.withName("tl-factory-gitea-test-" + UUID.randomUUID().toString().substring(0, 8))
				.withLabels(Map.of(CONTAINER_LABEL, "true"))
				.withEnv(
					"GITEA__security__INSTALL_LOCK=true",
					"GITEA__database__DB_TYPE=sqlite3",
					"GITEA__server__ROOT_URL=" + baseUrl,
					"GITEA__repository__DEFAULT_BRANCH=" + BRANCH,
					"GITEA__service__DISABLE_REGISTRATION=true")
				.withExposedPorts(http)
				.withHostConfig(HostConfig.newHostConfig()
					.withPortBindings(new PortBinding(Ports.Binding.bindIpAndPort(LOOPBACK, port), http)))
				.exec()
				.getId();
			GiteaServer server = new GiteaServer(docker, containerId, baseUrl);
			try {
				docker.startContainerCmd(containerId).exec();
				server.awaitStarted();
				server.setUp();
				return server;
			} catch (Exception | Error ex) {
				server.close();
				throw ex;
			}
		}

		private void awaitStarted() throws Exception {
			HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
			HttpRequest version = HttpRequest.newBuilder(URI.create(_baseUrl + "api/v1/version"))
				.timeout(Duration.ofSeconds(2)).build();
			long deadline = System.nanoTime() + STARTUP.toNanos();
			while (true) {
				try {
					if (http.send(version, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
						return;
					}
				} catch (IOException ex) {
					// Not yet listening.
				}
				if (System.nanoTime() > deadline) {
					fail("Gitea did not start within " + STARTUP + ".");
				}
				Thread.sleep(500);
			}
		}

		private void setUp() throws Exception {
			gitea("admin", "user", "create", "--username", ADMIN, "--password", ADMIN_PASSWORD,
				"--email", ADMIN + "@example.com", "--admin", "--must-change-password=false");
			_token = gitea("admin", "user", "generate-access-token", "--username", ADMIN, "--token-name",
				"full", "--scopes", FULL_SCOPES, "--raw").trim();
			_readToken = gitea("admin", "user", "generate-access-token", "--username", ADMIN, "--token-name",
				"read", "--scopes", READ_SCOPES, "--raw").trim();
			new OrganizationApi(client(_token)).orgCreate(new CreateOrgOption().username(ORG));
		}

		String baseUrl() {
			return _baseUrl;
		}

		String token() {
			return _token;
		}

		String readToken() {
			return _readToken;
		}

		/**
		 * A client of the Gitea API sending the given token.
		 */
		ApiClient client(String token) {
			ApiClient client = new ApiClient(HttpClient.newBuilder(), ApiClient.createDefaultObjectMapper(),
				_baseUrl + GiteaHostingProvider.API_PATH);
			client.setRequestInterceptor(request -> request.setHeader(CredentialProvider.AUTHORIZATION,
				GiteaHostingProvider.TOKEN_SCHEME + " " + token));
			return client;
		}

		/**
		 * Runs the Gitea command line with the given arguments, failing if it fails.
		 *
		 * @return The standard output.
		 */
		private String gitea(String... args) throws InterruptedException {
			List<String> command = new ArrayList<>();
			command.add("gitea");
			command.addAll(List.of(args));
			ExecResult result = exec(command.toArray(new String[0]));
			assertEquals("Failed: " + command + ": " + result.err() + result.out(), 0, result.exitCode());
			return result.out();
		}

		/**
		 * Runs a shell command line, e.g. a git command, in the container.
		 */
		ExecResult git(String commandLine) throws InterruptedException {
			return exec("sh", "-c", commandLine);
		}

		private ExecResult exec(String... command) throws InterruptedException {
			String execId = _docker.execCreateCmd(_containerId)
				.withUser(GITEA_USER)
				.withEnv(List.of("HOME=/tmp"))
				.withAttachStdout(true)
				.withAttachStderr(true)
				.withCmd(command)
				.exec()
				.getId();
			StringBuilder out = new StringBuilder();
			StringBuilder err = new StringBuilder();
			_docker.execStartCmd(execId).exec(new ResultCallback.Adapter<Frame>() {
				@Override
				public void onNext(Frame frame) {
					String text = new String(frame.getPayload(), StandardCharsets.UTF_8);
					synchronized (out) {
						(frame.getStreamType() == StreamType.STDERR ? err : out).append(text);
					}
				}
			}).awaitCompletion();
			Long exitCode = _docker.inspectExecCmd(execId).exec().getExitCodeLong();
			synchronized (out) {
				return new ExecResult(exitCode == null ? -1 : exitCode.longValue(), out.toString(), err.toString());
			}
		}

		@Override
		public void close() throws IOException {
			try {
				_docker.removeContainerCmd(_containerId).withForce(true).withRemoveVolumes(true).exec();
			} catch (NotFoundException ex) {
				// Already gone.
			} finally {
				_docker.close();
			}
		}
	}

}
