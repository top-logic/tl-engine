/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import java.util.function.Consumer;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
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

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.git.GitConnectionSettings;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.gitea.GiteaProviderType;
import com.top_logic.factory.gitea.client.ApiClient;
import com.top_logic.factory.gitea.client.Configuration;
import com.top_logic.factory.gitea.client.api.OrganizationApi;
import com.top_logic.factory.gitea.client.model.CreateOrgOption;

/**
 * A Gitea server in a Docker container for the integration tests, with an administrator
 * {@link #ADMIN}, an organization {@link #ORG} and two access tokens of the administrator.
 *
 * <p>
 * The image is given by the system property {@link #IMAGE_PROPERTY}, which the build sets to the
 * Gitea version the client is generated from. The daemon is the one at the environment variable
 * {@code DOCKER_HOST}, or the local socket. If no daemon answers there or the image is not given,
 * {@link #suite(Class, Consumer)} contains no tests.
 * </p>
 */
public final class GiteaTestServer implements AutoCloseable {

	/**
	 * System property naming the Gitea image to test against.
	 */
	public static final String IMAGE_PROPERTY = "tl.factory.gitea.image";

	/**
	 * The loopback address the server is reached at from the host.
	 */
	public static final String LOOPBACK = "127.0.0.1";

	/**
	 * The host name of the {@code ROOT_URL} of the server: another name of the {@link #LOOPBACK}
	 * address, so that the URLs Gitea reports differ from the URL the providers of the tests talk
	 * to, as a public URL of Gitea does from its address in the network of the control
	 * application.
	 */
	public static final String PUBLIC_HOST = "localhost";

	/**
	 * The {@code PUBLIC_URL_DETECTION} of the server: the URLs below the {@code ROOT_URL}, not
	 * below the host of the request.
	 */
	public static final String PUBLIC_URL_DETECTION = "legacy";

	/**
	 * Port of the web interface and the API inside the container.
	 */
	public static final int GITEA_PORT = 3000;

	/**
	 * The administrator of the server, owner of the tokens.
	 */
	public static final String ADMIN = "factory-admin";

	/**
	 * The organization the repositories of the tests are created in.
	 */
	public static final String ORG = "tl-factory-test";

	/**
	 * The default branch of the server.
	 */
	public static final String BRANCH = "main";

	private static final String ADMIN_PASSWORD = "factory-admin-password";

	/**
	 * The scopes of the access token of a git connection to Gitea.
	 */
	public static final String FULL_SCOPES = "write:organization,write:repository,read:user";

	private static final String READ_SCOPES = "read:organization,read:repository";

	private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

	private static final String IMAGE_REPOSITORY = "gitea/gitea";

	/**
	 * User the Gitea process runs as in the container, and its commands must.
	 */
	private static final String GITEA_USER = "git";

	/**
	 * Label marking the containers of the tests.
	 */
	private static final String CONTAINER_LABEL = "test.tl-factory-gitea";

	private static final Duration STARTUP = Duration.ofSeconds(90);

	private final DockerClient _docker;

	private final String _containerId;

	private final String _baseUrl;

	private String _token;

	private String _readToken;

	private GiteaTestServer(DockerClient docker, String containerId, String baseUrl) {
		_docker = docker;
		_containerId = containerId;
		_baseUrl = baseUrl;
	}

	/**
	 * The tests of the given class around a Gitea server, none if no Docker daemon is reachable or
	 * no image is given.
	 *
	 * @param tests
	 *        The test class.
	 * @param server
	 *        Gets the started server before the tests, {@code null} after them.
	 */
	public static Test suite(Class<? extends TestCase> tests, Consumer<GiteaTestServer> server) {
		String image = System.getProperty(IMAGE_PROPERTY);
		if (image == null || image.isEmpty()) {
			System.err.println("Skipping " + tests.getName() + ": no Gitea image given in " + IMAGE_PROPERTY + ".");
			return new TestSuite(tests.getName());
		}
		if (!dockerAvailable()) {
			System.err.println("Skipping " + tests.getName() + ": no Docker daemon reachable at " + dockerHost() + ".");
			return new TestSuite(tests.getName());
		}
		Test setup = new TestSetup(new TestSuite(tests)) {
			private GiteaTestServer _started;

			@Override
			protected void setUp() throws Exception {
				_started = start(image);
				server.accept(_started);
			}

			@Override
			protected void tearDown() throws Exception {
				server.accept(null);
				if (_started != null) {
					_started.close();
					_started = null;
				}
			}
		};
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(setup, TypeIndex.Module.INSTANCE, SecretStoreService.Module.INSTANCE));
	}

	/**
	 * The Gitea image of the version the client is generated from, the version of its OpenAPI
	 * specification.
	 */
	public static String pinnedImage() {
		return IMAGE_REPOSITORY + ":" + Configuration.VERSION;
	}

	/**
	 * Starts a server of the given image.
	 */
	public static GiteaTestServer start(String image) throws Exception {
		return start(image, null, null);
	}

	/**
	 * Starts a server of the given image, also reachable from the containers of a Docker network.
	 *
	 * @param image
	 *        The Gitea image.
	 * @param network
	 *        The Docker network the server joins, {@code null} for none.
	 * @param alias
	 *        The host name of the server in the network, e.g. {@code gitea}: the containers of the
	 *        network reach it at {@code http://<alias>:}{@value #GITEA_PORT}{@code /}. Ignored
	 *        without network.
	 */
	public static GiteaTestServer start(String image, String network, String alias) throws Exception {
		DockerClient docker = createClient();
		try {
			docker.inspectImageCmd(image).exec();
		} catch (NotFoundException ex) {
			docker.pullImageCmd(image).exec(new PullImageResultCallback()).awaitCompletion();
		}

		int port = freePort();
		String baseUrl = "http://" + LOOPBACK + ":" + port + "/";
		ExposedPort http = ExposedPort.tcp(GITEA_PORT);
		HostConfig hostConfig = HostConfig.newHostConfig()
			.withPortBindings(new PortBinding(Ports.Binding.bindIpAndPort(LOOPBACK, port), http));
		if (network != null) {
			hostConfig.withNetworkMode(network);
		}
		CreateContainerCmd create = docker.createContainerCmd(image)
			.withName("tl-factory-gitea-test-" + UUID.randomUUID().toString().substring(0, 8))
			.withLabels(Map.of(CONTAINER_LABEL, "true"))
			.withEnv(
				"GITEA__security__INSTALL_LOCK=true",
				"GITEA__database__DB_TYPE=sqlite3",
				"GITEA__server__ROOT_URL=" + rootUrl(port),
				"GITEA__server__PUBLIC_URL_DETECTION=" + PUBLIC_URL_DETECTION,
				"GITEA__repository__DEFAULT_BRANCH=" + BRANCH,
				"GITEA__service__DISABLE_REGISTRATION=true")
			.withExposedPorts(http)
			.withHostConfig(hostConfig);
		if (network != null && alias != null) {
			create.withAliases(alias);
		}
		String containerId = create.exec().getId();
		GiteaTestServer server = new GiteaTestServer(docker, containerId, baseUrl);
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

	/**
	 * The URL of the server as reached from the host, with a slash at its end.
	 */
	public String baseUrl() {
		return _baseUrl;
	}

	/**
	 * The {@code ROOT_URL} of the server, below which it reports the pages and clone URLs of the
	 * repositories, with a slash at its end.
	 */
	public String rootUrl() {
		return rootUrl(URI.create(_baseUrl).getPort());
	}

	private static String rootUrl(int port) {
		return "http://" + PUBLIC_HOST + ":" + port + "/";
	}

	/**
	 * A token of {@link #ADMIN} with the scopes for creating and protecting repositories.
	 */
	public String token() {
		return _token;
	}

	/**
	 * A token of {@link #ADMIN} that may only read.
	 */
	public String readToken() {
		return _readToken;
	}

	/**
	 * A client of the Gitea API sending the given token.
	 */
	public ApiClient client(String token) {
		ApiClient client = new ApiClient(HttpClient.newBuilder(), ApiClient.createDefaultObjectMapper(),
			_baseUrl + GiteaHostingProvider.API_PATH);
		client.setRequestInterceptor(request -> request.setHeader(CredentialProvider.AUTHORIZATION,
			GiteaHostingProvider.TOKEN_SCHEME + " " + token));
		return client;
	}

	/**
	 * Creates a user of this server who is no administrator, with an access token of the
	 * {@link #FULL_SCOPES} and {@code write:user}, which creating a repository in the namespace of
	 * the user needs.
	 *
	 * @param name
	 *        The name of the user.
	 * @return The access token.
	 */
	public String createUser(String name) throws InterruptedException {
		return createUser(name, FULL_SCOPES + ",write:user");
	}

	/**
	 * Creates a user of this server who is no administrator, with an access token of the given
	 * scopes.
	 *
	 * @param name
	 *        The name of the user.
	 * @param scopes
	 *        The scopes of the token, separated by commas, e.g. {@code write:repository,read:user}.
	 * @return The access token.
	 */
	public String createUser(String name, String scopes) throws InterruptedException {
		gitea("admin", "user", "create", "--username", name, "--password", name + "-password-1",
			"--email", name + "@example.com", "--must-change-password=false");
		return accessToken(name, "full", scopes);
	}

	/**
	 * Creates another access token of a user of this server.
	 *
	 * @param user
	 *        The name of the user, e.g. {@link #ADMIN}.
	 * @param tokenName
	 *        The name of the token, unique among the tokens of the user.
	 * @param scopes
	 *        The scopes of the token, separated by commas.
	 * @return The access token.
	 */
	public String accessToken(String user, String tokenName, String scopes) throws InterruptedException {
		return gitea("admin", "user", "generate-access-token", "--username", user, "--token-name", tokenName,
			"--scopes", scopes, "--raw").trim();
	}

	/**
	 * A provider of the organization {@link #ORG} of this server, authenticated with the given
	 * token, or with the token in the secret {@link GiteaProviderType#TOKEN_SECRET} if
	 * {@code null}.
	 */
	public GiteaHostingProvider provider(String token) {
		return provider(_baseUrl, token);
	}

	/**
	 * A provider of the given owner on this server, authenticated with the given token.
	 *
	 * @param createOwner
	 *        See {@link GitConnectionSettings#createOwner()}.
	 */
	public GiteaHostingProvider provider(String owner, String token, boolean createOwner) {
		return provider(_baseUrl, owner, token, createOwner);
	}

	/**
	 * A provider of the organization {@link #ORG} of the server at the given URL, authenticated
	 * with the given token, or with the token in the secret {@link GiteaProviderType#TOKEN_SECRET}
	 * if {@code null}.
	 */
	public static GiteaHostingProvider provider(String baseUrl, String token) {
		return provider(baseUrl, ORG, token, false);
	}

	private static GiteaHostingProvider provider(String baseUrl, String owner, String token, boolean createOwner) {
		CredentialProvider credentials =
			token == null ? GiteaProviderType.credentials(GiteaProviderType.TOKEN_SECRET) : credentials(token);
		return new GiteaHostingProvider(new GitConnectionSettings(baseUrl, owner, credentials, createOwner),
			Duration.ofSeconds(10), 1);
	}

	private static CredentialProvider credentials(String token) {
		LiteralSecretSource.Config secret = TypedConfiguration.newConfigItem(LiteralSecretSource.Config.class);
		secret.setValue(token);
		return TypedConfigUtil.createInstance(
			HeaderCredentials.config(CredentialProvider.AUTHORIZATION, GiteaHostingProvider.TOKEN_SCHEME, secret));
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
	public ExecResult git(String commandLine) throws InterruptedException {
		return exec("sh", "-c", commandLine);
	}

	/**
	 * Runs a shell command line in the container with the given arguments, {@code $0} the first.
	 */
	public ExecResult git(String commandLine, String... args) throws InterruptedException {
		List<String> command = new ArrayList<>();
		command.add("sh");
		command.add("-c");
		command.add(commandLine);
		command.addAll(List.of(args));
		return exec(command.toArray(new String[0]));
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

	/**
	 * A free port of the loopback address.
	 */
	public static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	/**
	 * The Docker daemon of the tests: the one at the environment variable {@code DOCKER_HOST}, or
	 * the local socket.
	 */
	public static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? DEFAULT_DOCKER_HOST : host;
	}

	/**
	 * A client of the {@link #dockerHost() Docker daemon of the tests}.
	 */
	public static DockerClient createClient() {
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
			.withDockerHost(dockerHost())
			.build();
		return DockerClientImpl.getInstance(config,
			new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).build());
	}

	/**
	 * Whether the {@link #dockerHost() Docker daemon of the tests} answers.
	 */
	public static boolean dockerAvailable() {
		try (DockerClient client = createClient()) {
			client.pingCmd().exec();
			return true;
		} catch (IOException | RuntimeException ex) {
			return false;
		}
	}

	/**
	 * The result of a command.
	 *
	 * @param exitCode
	 *        The exit code of the command.
	 * @param out
	 *        Its standard output.
	 * @param err
	 *        Its error output.
	 */
	public record ExecResult(long exitCode, String out, String err) {
		// Pure data.
	}

}
