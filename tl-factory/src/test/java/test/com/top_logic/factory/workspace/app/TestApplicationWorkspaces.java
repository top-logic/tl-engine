/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;

/**
 * Integration test of {@link ApplicationWorkspaces}: an application is generated, built and run in
 * a Docker workspace with a given initial password of its account root, the tools of an agent are
 * checked in the workspace, the workspace is stopped, started again, updated to another image and
 * removed.
 *
 * <p>
 * The test downloads the workspace base image, Maven and the TopLogic release artifacts and takes
 * minutes, so it only runs when the system property {@value #ENABLE_PROPERTY} is {@code true},
 * e.g.:
 * </p>
 *
 * <pre>
 * mvn test -DskipTests=false -pl tl-factory -Dtest=test.com.top_logic.factory.workspace.app.TestApplicationWorkspaces -Dtl.factory.integration=true
 * </pre>
 *
 * <p>
 * Without the property, or if no Docker daemon answers at the environment variable
 * {@code DOCKER_HOST} or the local socket, the suite contains no tests.
 * </p>
 *
 * <p>
 * The test uses a label namespace, name prefix and network of its own. The release repository it
 * fills is kept on purpose, so that a later run reuses it; it is the shared volume
 * {@code tl-factory-it-shared-release-<version>}.
 * </p>
 */
public class TestApplicationWorkspaces extends TestCase {

	/**
	 * System property enabling this test.
	 */
	public static final String ENABLE_PROPERTY = "tl.factory.integration";

	private static final String NAME_PREFIX = "tl-factory-it";

	private static final String LABEL_NAMESPACE = "test.tl-factory.it";

	private static final int HOST_PORT_MIN = 22000;

	private static final int HOST_PORT_MAX = 22999;

	private static final String GROUP_ID = "com.example.factory";

	private static final String ARTIFACT_ID = "factory-it-app";

	private static final String DATA_FILE = ApplicationWorkspaces.SOURCE_PATH + "/it-data";

	private static final String DATA_CONTENT = "persisted";

	private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

	private static final String INITIAL_PASSWORD = "it-" + UUID.randomUUID();

	/**
	 * Log message of the generated application when it takes the initial password of root from
	 * {@link ApplicationWorkspaces#ENV_INITIAL_PASSWORD}, see
	 * <code>PersonManager.setupRootPassword()</code>.
	 */
	private static final String PASSWORD_FROM_ENV = "Initial password for 'root' set up from environment variable.";

	/**
	 * Start of the log message of the generated application when it chose a random initial
	 * password for root.
	 */
	private static final String RANDOM_PASSWORD = "Initial password for 'root': ";

	private static final Duration WAIT = Duration.ofSeconds(30);

	/**
	 * File added to the image context to get another workspace image for the update. The
	 * Dockerfile does not copy it, so the image is built from the cached layers.
	 */
	private static final String CHANGED_IMAGE_FILE = "it-changed";

	/**
	 * Declaration of the Claude Code version in the Dockerfile of the workspace image.
	 */
	private static final Pattern CLAUDE_CODE_VERSION =
		Pattern.compile("^ARG CLAUDE_CODE_VERSION=(\\S+)$", Pattern.MULTILINE);

	private static final String TL_PLUGIN = "tl-claude-tools@toplogic";

	private static final String PLAYWRIGHT_MCP_SERVER = "playwright";

	private static final String MCP_CONNECTED = "Connected";

	private static final String MCP_COMMAND_LINE = "Command:";

	private static final String MCP_ARGS_LINE = "Args:";

	private static final String BROWSER_CHECK_TITLE = "headless-ok";

	private static final String MCP_INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\","
		+ "\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},"
		+ "\"clientInfo\":{\"name\":\"tl-factory-it\",\"version\":\"1\"}}}";

	private static final String MCP_INITIALIZED = "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}";

	private static final String MCP_NAVIGATE = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/call\","
		+ "\"params\":{\"name\":\"browser_navigate\","
		+ "\"arguments\":{\"url\":\"data:text/html,<title>" + BROWSER_CHECK_TITLE + "</title>\"}}}";

	private DockerWorkspaceProvider _provider;

	private ApplicationWorkspaces _applications;

	private DockerClient _docker;

	private String _workspaceId;

	/**
	 * Images the test built in addition to the workspace image, removed after the test.
	 */
	private final List<String> _builtImages = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_workspaceId = "app-" + UUID.randomUUID().toString().substring(0, 8);
		_provider = createProvider();
		_applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), _provider);
		_docker = createClient();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_provider.cleanup(Collections.emptyList());
			assertTrue("Workspaces left after cleanup.", _provider.list().isEmpty());
			for (String image : _builtImages) {
				try {
					_docker.removeImageCmd(image).exec();
				} catch (NotFoundException ex) {
					// Not built.
				}
			}
		} finally {
			_provider.close();
			_docker.close();
			super.tearDown();
		}
	}

	/**
	 * Creates an application, checks that it serves its login page, stops and starts it again on
	 * the same volumes, updates it to another image on the same volumes with the same initial
	 * password and removes it, keeping the release repository.
	 */
	public void testApplicationLifecycle() throws Exception {
		String version = _applications.getConfig().getEngineVersion();
		ExecOutput progress = new ExecOutput() {
			@Override
			public void stdout(String text) {
				System.out.print(text);
			}

			@Override
			public void stderr(String text) {
				System.err.print(text);
			}
		};

		long start = System.currentTimeMillis();
		WorkspaceInfo created =
			_applications.create(
				new ApplicationSpec(_workspaceId, GROUP_ID, ARTIFACT_ID).setInitialPassword(INITIAL_PASSWORD),
				progress);
		long built = System.currentTimeMillis();
		assertEquals(WorkspaceState.RUNNING, created.getState());
		assertEquals(2, created.getVolumes().size());

		String url = _applications.awaitApplication(_workspaceId, ARTIFACT_ID);
		long reachable = System.currentTimeMillis();
		assertLoginPage(url);
		assertInitialPassword(created.getContainerId());
		assertAgentTools();
		assertEquals(0, _provider.exec(_workspaceId,
			ExecCommand.of("sh", "-c", "echo " + DATA_CONTENT + " > " + DATA_FILE), ExecOutput.NONE));

		WorkspaceInfo stopped = _applications.stop(_workspaceId);
		assertEquals(WorkspaceState.STOPPED, stopped.getState());
		assertEquals(created.getVolumes(), stopped.getVolumes());

		long restart = System.currentTimeMillis();
		WorkspaceInfo restarted = _applications.start(_workspaceId);
		assertEquals(WorkspaceState.RUNNING, restarted.getState());
		String restartedUrl = _applications.awaitApplication(_workspaceId, ARTIFACT_ID);
		long reachableAgain = System.currentTimeMillis();
		assertLoginPage(restartedUrl);
		assertInitialPassword(created.getContainerId());
		StringBuilder data = new StringBuilder();
		assertEquals(0, _provider.exec(_workspaceId, ExecCommand.of("cat", DATA_FILE), new ExecOutput() {
			@Override
			public void stdout(String text) {
				data.append(text);
			}
		}));
		assertEquals("The source volume did not survive the restart.", DATA_CONTENT + "\n", data.toString());

		long update = System.currentTimeMillis();
		ApplicationWorkspaces changed = changedImage();
		String updatedImage = changed.currentImage();
		assertFalse("The changed image context has the same tag.", updatedImage.equals(_applications.currentImage()));
		_builtImages.add(updatedImage);
		WorkspaceInfo updated = changed.update(
			new ApplicationSpec(_workspaceId, GROUP_ID, ARTIFACT_ID).setInitialPassword(INITIAL_PASSWORD), progress);
		assertEquals(WorkspaceState.CREATED, updated.getState());
		assertEquals(updatedImage, updated.getImage());
		assertEquals(created.getVolumes(), updated.getVolumes());
		assertFalse(created.getContainerId().equals(updated.getContainerId()));
		changed.start(_workspaceId);
		assertLoginPage(changed.awaitApplication(_workspaceId, ARTIFACT_ID));
		long reachableUpdated = System.currentTimeMillis();
		assertEquals("The initial password is not kept.", INITIAL_PASSWORD + "\n",
			output(ExecCommand.of("printenv", ApplicationWorkspaces.ENV_INITIAL_PASSWORD)));
		assertEquals("The source volume did not survive the update.", DATA_CONTENT + "\n",
			output(ExecCommand.of("cat", DATA_FILE)));

		_applications.remove(_workspaceId);
		WorkspaceInfo removed = _provider.status(_workspaceId);
		assertEquals(WorkspaceState.MISSING, removed.getState());
		assertTrue(removed.getVolumes().isEmpty());
		for (String volume : created.getVolumes()) {
			try {
				_docker.inspectVolumeCmd(volume).exec();
				fail("Volume not removed: " + volume);
			} catch (NotFoundException ex) {
				// Expected.
			}
		}
		try {
			_docker.inspectContainerCmd(created.getContainerId()).exec();
			fail("Container not removed: " + created.getContainerId());
		} catch (NotFoundException ex) {
			// Expected.
		}
		assertTrue("Release repository removed with the workspace.",
			_provider.listSharedVolumes().contains(ApplicationWorkspaces.releaseVolume(version)));

		System.out.println("Create (image, release repository, generate, build): " + seconds(start, built) + " s");
		System.out.println("First start until reachable: " + seconds(built, reachable) + " s");
		System.out.println("Restart until reachable: " + seconds(restart, reachableAgain) + " s");
		System.out.println("Update until reachable: " + seconds(update, reachableUpdated) + " s");
	}

	/**
	 * The application set up the account root with the given initial password: the container passes
	 * it in {@link ApplicationWorkspaces#ENV_INITIAL_PASSWORD}, and the application reports in its
	 * log that it took the password from there instead of choosing a random one.
	 */
	private void assertInitialPassword(String containerId) throws Exception {
		StringBuilder env = new StringBuilder();
		assertEquals(0, _provider.exec(_workspaceId,
			ExecCommand.of("printenv", ApplicationWorkspaces.ENV_INITIAL_PASSWORD), new ExecOutput() {
				@Override
				public void stdout(String text) {
					env.append(text);
				}
			}));
		assertEquals(INITIAL_PASSWORD + "\n", env.toString());

		StringBuilder log = new StringBuilder();
		_docker.logContainerCmd(containerId).withStdOut(true).withStdErr(true)
			.exec(new ResultCallback.Adapter<Frame>() {
				@Override
				public void onNext(Frame frame) {
					log.append(new String(frame.getPayload(), StandardCharsets.UTF_8));
				}
			}).awaitCompletion();
		assertTrue("Initial password not taken from the environment.", log.indexOf(PASSWORD_FROM_ENV) >= 0);
		assertTrue("Random initial password chosen.", log.indexOf(RANDOM_PASSWORD) < 0);
	}

	/**
	 * The workspace runs its commands as the non-root {@link ApplicationWorkspaces#WORKSPACE_USER},
	 * has Claude Code in the version of the image with the TopLogic plugin and the Playwright MCP
	 * server, whose headless Chromium opens a page, and has no API key in its environment; a command of the
	 * workspace user reads its input and is terminated.
	 */
	private void assertAgentTools() throws Exception {
		assertEquals(ApplicationWorkspaces.WORKSPACE_USER + "\n", output(ExecCommand.of("id", "-un")));
		assertFalse("Commands run as root.", "0\n".equals(output(ExecCommand.of("id", "-u"))));
		assertEquals("0\n", output(ExecCommand.of("id", "-u").setUser(ApplicationWorkspaces.ROOT_USER)));

		assertTrue("Unexpected Claude Code version.",
			output(ExecCommand.of("claude", "--version")).startsWith(claudeCodeVersion() + " "));
		String plugins = output(ExecCommand.of("claude", "plugin", "list"));
		assertTrue("TopLogic plugin not installed: " + plugins, plugins.contains(TL_PLUGIN));
		assertBrowserThroughMcp();

		String env = output(ExecCommand.of("env"));
		for (String line : env.split("\n")) {
			String name = line.split("=", 2)[0];
			assertFalse("Credential in the environment: " + name,
				name.startsWith("ANTHROPIC_") || name.contains("API_KEY"));
		}

		CollectingOutput echo = new CollectingOutput();
		try (ExecProcess process = _provider.startExec(_workspaceId, ExecCommand.of("cat"), echo)) {
			process.write("ping\n");
			process.closeStdin();
			assertTrue("Command did not end after its input was closed.", process.waitFor(WAIT));
			assertEquals(0, process.exitCode());
			assertEquals("ping\n", echo.stdout());
		}

		CollectingOutput sleeper = new CollectingOutput();
		try (ExecProcess process = _provider.startExec(_workspaceId,
			ExecCommand.of("sh", "-c", "id -un; sleep 600"), sleeper)) {
			sleeper.awaitStdout(ApplicationWorkspaces.WORKSPACE_USER + "\n");
			process.terminate();
			assertFalse(process.isAlive());
			assertEquals(128 + 15, process.exitCode());
		}
	}

	/**
	 * The Playwright MCP server registered for Claude Code answers requests on its standard input,
	 * opening a page in its headless Chromium.
	 */
	private void assertBrowserThroughMcp() throws Exception {
		String registration = output(ExecCommand.of("claude", "mcp", "get", PLAYWRIGHT_MCP_SERVER));
		assertTrue("Playwright MCP server not connected: " + registration,
			registration.contains(MCP_CONNECTED));
		List<String> command = new ArrayList<>();
		for (String line : registration.split("\n")) {
			String entry = line.trim();
			if (entry.startsWith(MCP_COMMAND_LINE)) {
				command.add(entry.substring(MCP_COMMAND_LINE.length()).trim());
			} else if (entry.startsWith(MCP_ARGS_LINE)) {
				command.addAll(Arrays.asList(entry.substring(MCP_ARGS_LINE.length()).trim().split(" +")));
			}
		}
		assertFalse("No command of the Playwright MCP server: " + registration, command.isEmpty());

		CollectingOutput responses = new CollectingOutput();
		try (ExecProcess server = _provider.startExec(_workspaceId, ExecCommand.of(command), responses)) {
			server.write(MCP_INITIALIZE + "\n");
			server.flush();
			responses.awaitStdoutContaining("\"id\":1");
			server.write(MCP_INITIALIZED + "\n");
			server.write(MCP_NAVIGATE + "\n");
			server.flush();
			responses.awaitStdoutContaining("Page Title: " + BROWSER_CHECK_TITLE);
			server.terminate();
			assertFalse(server.isAlive());
		}
	}

	/**
	 * {@link ApplicationWorkspaces} with the configuration of the tested ones whose image context
	 * has an additional file, so that its current image is another one.
	 */
	private ApplicationWorkspaces changedImage() {
		return new ApplicationWorkspaces(_applications.getConfig(), _provider) {
			@Override
			protected ImageContext workspaceImageContext() {
				return super.workspaceImageContext().add(CHANGED_IMAGE_FILE, _workspaceId);
			}
		};
	}

	/**
	 * The Claude Code version the workspace image installs.
	 */
	private String claudeCodeVersion() {
		String dockerfile = new String(
			ApplicationWorkspaces.imageContext(_applications.getConfig().getRepositoryUrl()).getFiles()
				.get(ImageContext.DOCKERFILE),
			StandardCharsets.UTF_8);
		Matcher matcher = CLAUDE_CODE_VERSION.matcher(dockerfile);
		assertTrue("No Claude Code version in the Dockerfile.", matcher.find());
		return matcher.group(1);
	}

	private String output(ExecCommand command) throws InterruptedException {
		CollectingOutput output = new CollectingOutput();
		int exitCode = _provider.exec(_workspaceId, command, output);
		assertEquals("Failed: " + command + "\n" + output.stdout() + output.stderr(), 0, exitCode);
		return output.stdout();
	}

	/**
	 * {@link ExecOutput} collecting the output of a command.
	 */
	private static final class CollectingOutput implements ExecOutput {

		private final StringBuilder _stdout = new StringBuilder();

		private final StringBuilder _stderr = new StringBuilder();

		@Override
		public synchronized void stdout(String text) {
			_stdout.append(text);
			notifyAll();
		}

		@Override
		public synchronized void stderr(String text) {
			_stderr.append(text);
		}

		synchronized String stdout() {
			return _stdout.toString();
		}

		synchronized String stderr() {
			return _stderr.toString();
		}

		synchronized void awaitStdout(String expected) throws InterruptedException {
			await(stdout -> stdout.equals(expected), expected);
		}

		synchronized void awaitStdoutContaining(String expected) throws InterruptedException {
			await(stdout -> stdout.contains(expected), expected);
		}

		private void await(Predicate<String> test, String expected) throws InterruptedException {
			long deadline = System.currentTimeMillis() + WAIT.toMillis();
			while (!test.test(_stdout.toString())) {
				long left = deadline - System.currentTimeMillis();
				assertTrue("Expected output not received: " + expected + ", got: " + _stdout + _stderr, left > 0);
				wait(left);
			}
		}
	}

	private static long seconds(long from, long to) {
		return (to - from) / 1000;
	}

	private static void assertLoginPage(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		HttpResponse<String> response = client.send(
			HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).build(),
			HttpResponse.BodyHandlers.ofString());
		assertEquals("Status of " + response.uri(), 200, response.statusCode());
		String page = response.body().toLowerCase(Locale.ROOT);
		assertTrue("Not an HTML page: " + response.body(), page.contains("<html"));
		assertTrue("Not a login page: " + response.body(), page.contains("login"));
	}

	private static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? DEFAULT_DOCKER_HOST : host;
	}

	private static DockerWorkspaceProvider createProvider() {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, dockerHost());
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NETWORK, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NAME_PREFIX, NAME_PREFIX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.LABEL_NAMESPACE, LABEL_NAMESPACE);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MIN, HOST_PORT_MIN);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MAX, HOST_PORT_MAX);
		return (DockerWorkspaceProvider) TypedConfigUtil.createInstance(config);
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
	 * The tests of this class, none unless enabled by {@link #ENABLE_PROPERTY} and a Docker daemon
	 * is reachable.
	 */
	public static Test suite() {
		String name = TestApplicationWorkspaces.class.getName();
		if (!Boolean.getBoolean(ENABLE_PROPERTY)) {
			System.err.println("Skipping " + name + ": enable it with -D" + ENABLE_PROPERTY + "=true.");
			return new TestSuite(name);
		}
		if (!dockerAvailable()) {
			System.err.println("Skipping " + name + ": no Docker daemon reachable at " + dockerHost() + ".");
			return new TestSuite(name);
		}
		return ServiceTestSetup.createSetup(TestApplicationWorkspaces.class, TypeIndex.Module.INSTANCE);
	}

}
