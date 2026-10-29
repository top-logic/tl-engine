/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

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
import com.top_logic.factory.preview.PreviewLookup;
import com.top_logic.factory.preview.PreviewRefusal;
import com.top_logic.factory.preview.PreviewServlet;
import com.top_logic.factory.preview.PreviewTarget;
import com.top_logic.factory.preview.PreviewWorkspaces;
import com.top_logic.factory.preview.StoredPreviewWorkspaces;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationFailedException;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces.ApplicationState;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;

/**
 * Integration test of {@link ApplicationWorkspaces}: an application is generated, built and run in
 * a Docker workspace with a given initial password of its account root, reached through the
 * {@link PreviewServlet preview route}, the tools of an agent are checked in the workspace, the
 * workspace is stopped, started again, updated to another image and removed.
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

	/** A file in the store of the agent's conversations, which survives an update. */
	private static final String SESSION_FILE = ApplicationWorkspaces.AGENT_SESSIONS_PATH + "/it-session";

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
	 * Log message of the generated application once it has started.
	 */
	private static final String UP_AND_RUNNING = "up and running";

	/**
	 * Start of the line of the supervisor in the container log when the application is up.
	 */
	private static final String SUPERVISOR_UP = "[tl-workspace-app] The application is up at ";

	/**
	 * Name of the init process Docker runs as PID 1 of a container created with init.
	 */
	private static final String INIT_PROCESS = "docker-init";

	/**
	 * Process state of a zombie in the output of {@code ps}.
	 */
	private static final String ZOMBIE_STATE = "Z";

	/**
	 * Part of the command line of the application process, the Maven JVM launching it.
	 */
	private static final String APPLICATION_PROCESS = "-Dtl.port=";

	/**
	 * Exit code of {@link ApplicationWorkspaces#APP_CONTROL_COMMAND} {@code status} for an
	 * application that does not answer.
	 */
	private static final int STATUS_NOT_RUNNING = 3;

	/**
	 * Finds the script of the {@code tl-app} skill of the TopLogic plugin in the workspace image.
	 */
	/**
	 * Lists the application configuration of the generated application, which
	 * {@link #BREAK_COMMAND} makes malformed. Its name is the project name the archetype derives
	 * from the artifact.
	 */
	private static final String FIND_BROKEN_FILE = "ls " + ApplicationWorkspaces.appDir(ARTIFACT_ID)
		+ "/src/main/webapp/WEB-INF/conf/*.conf.config.xml";

	/**
	 * Command making the file given as its argument malformed XML.
	 */
	private static final String BREAK_COMMAND = "sed -i 's/<application /<application broken= /' ";

	/**
	 * Text in the log of an application whose start failed.
	 */
	private static final Pattern ERROR_IN_LOG = Pattern.compile("ERROR|Exception");

	private static final String BACKUP_SUFFIX = ".it-backup";

	/**
	 * A script or stylesheet a page refers to by a URL relative to the page or its host.
	 */
	private static final Pattern PAGE_RESOURCE =
		Pattern.compile("(?:src|href)=\"((?!https?:|//|data:|#)[^\"]+\\.(?:js|css)(?:\\?[^\"]*)?)\"");

	/**
	 * The route of the layouts of the application, relative to its URL: the generated application
	 * of the engine version the test uses has the classic UI.
	 */
	private static final String LAYOUT_ROUTE = "servlet/LayoutServlet";

	/**
	 * Time within which a failed start of the application is recognized: its start up to the failure
	 * and the grace period of the supervisor.
	 */
	private static final Duration FAILURE_LIMIT = Duration.ofSeconds(120);

	private static final String FIND_TL_APP_SCRIPT = "find / -path '*tl-app/tl-app.sh' 2>/dev/null | head -1";

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

	/**
	 * Context path of the control application the preview route of the test runs in, and in which
	 * the context path of the application is therefore placed.
	 */
	private static final String CONTROL_CONTEXT = "/factory-it-control";

	/**
	 * Path of the control application logging the client in, answering with the session.
	 */
	private static final String CONTROL_LOGIN = "/login";

	/**
	 * Session attribute of a client logged into the control application.
	 */
	private static final String LOGGED_IN = "loggedIn";

	/**
	 * Path of the stream of {@link #SSE_SERVER} below the context path of the application.
	 */
	private static final String SSE_PATH = "/events";

	/**
	 * Output of {@link #SSE_SERVER} once it listens.
	 */
	private static final String SSE_LISTENING = "sse-listening";

	/**
	 * A server of Server-Sent Events on the container port, run with node in the workspace in place
	 * of the application: it answers its argument, a path, with a stream that never ends, a comment
	 * and then an event every half second.
	 */
	private static final String SSE_SERVER = "const path = process.argv[1];"
		+ "require('http').createServer((req, res) => {"
		+ " if (req.url !== path) { res.writeHead(404); res.end(); return; }"
		+ " res.writeHead(200, {'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache'});"
		+ " res.write(': connected\\n\\n');"
		+ " let n = 0;"
		+ " const timer = setInterval(() => res.write('data: ' + (n++) + '\\n\\n'), 500);"
		+ " req.on('close', () => clearInterval(timer));"
		+ "}).listen(" + WorkspaceSpec.DEFAULT_CONTAINER_PORT + ", () => console.log('" + SSE_LISTENING + "'));";

	/**
	 * Maximum number of redirects followed to the login page.
	 */
	private static final int MAX_REDIRECTS = 5;

	private DockerWorkspaceProvider _provider;

	private ApplicationWorkspaces _applications;

	private DockerClient _docker;

	private String _workspaceId;

	/**
	 * The context path of the application, the one of the default pattern.
	 */
	private String _contextPath;

	/**
	 * Images the test built in addition to the workspace image, removed after the test.
	 */
	private final List<String> _builtImages = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_workspaceId = "app-" + UUID.randomUUID().toString().substring(0, 8);
		_provider = createProvider();
		_applications = new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class),
			_provider, CONTROL_CONTEXT);
		_contextPath = _applications.contextPath(_workspaceId, ARTIFACT_ID);
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
		assertEquals(3, created.getVolumes().size());

		String url = _applications.awaitApplication(_workspaceId, _contextPath);
		long reachable = System.currentTimeMillis();
		assertEquals("Not the multi-segment context path of the default pattern.",
			CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + _workspaceId + "/" + ARTIFACT_ID,
			_contextPath);
		assertEquals(ApplicationWorkspaces.applicationUrl(_provider.status(_workspaceId), _contextPath), url);
		assertLoginPage(url);
		assertServedBelow(url);
		assertPreviewRoute();
		assertInitialPassword(created.getContainerId());
		assertAgentTools();
		assertApplicationControl(url);
		assertEquals(0, _provider.exec(_workspaceId,
			ExecCommand.of("sh", "-c", "echo " + DATA_CONTENT + " > " + DATA_FILE), ExecOutput.NONE));
		assertEquals("The workspace user cannot write the agent's conversations.", 0, _provider.exec(_workspaceId,
			ExecCommand.of("sh", "-c", "echo " + DATA_CONTENT + " > " + SESSION_FILE), ExecOutput.NONE));

		assertStreamThroughPreview();

		WorkspaceInfo stopped = _applications.stop(_workspaceId);
		assertEquals(WorkspaceState.STOPPED, stopped.getState());
		assertEquals(created.getVolumes(), stopped.getVolumes());

		long restart = System.currentTimeMillis();
		WorkspaceInfo restarted = _applications.start(_workspaceId);
		assertEquals(WorkspaceState.RUNNING, restarted.getState());
		String restartedUrl = _applications.awaitApplication(_workspaceId, _contextPath);
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
		assertLoginPage(changed.awaitApplication(_workspaceId, _contextPath));
		long reachableUpdated = System.currentTimeMillis();
		assertEquals("The initial password is not kept.", INITIAL_PASSWORD + "\n",
			output(ExecCommand.of("printenv", ApplicationWorkspaces.ENV_INITIAL_PASSWORD)));
		assertEquals("The source volume did not survive the update.", DATA_CONTENT + "\n",
			output(ExecCommand.of("cat", DATA_FILE)));
		assertEquals("The agent's conversations did not survive the update.", DATA_CONTENT + "\n",
			output(ExecCommand.of("cat", SESSION_FILE)));
		assertAgentTools();

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
	 * The workspace runs its commands as the non-root {@link ApplicationWorkspaces#WORKSPACE_USER}
	 * without capabilities, has Claude Code in the version of the image with the TopLogic plugin and the Playwright MCP
	 * server, whose headless Chromium opens a page, and has no API key in its environment; a command of the
	 * workspace user reads its input and is terminated.
	 */
	private void assertAgentTools() throws Exception {
		assertEquals(ApplicationWorkspaces.WORKSPACE_USER + "\n", output(ExecCommand.of("id", "-un")));
		assertFalse("Commands run as root.", "0\n".equals(output(ExecCommand.of("id", "-u"))));
		// No capabilities, no gain of privileges (setuid programs): nothing in the container acts as root.
		String status = output(ExecCommand.of("cat", "/proc/self/status"));
		assertTrue(status, status.matches("(?s).*\\nCapEff:\\s+0+\\n.*"));
		assertTrue(status, status.matches("(?s).*\\nNoNewPrivs:\\s+1\\n.*"));

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
	 * The init process of the container reaps orphans; the application runs under the supervisor of
	 * the entry script, the {@link ApplicationWorkspaces#APP_CONTROL_COMMAND} stops, starts and
	 * restarts it on the same database without ending the container, its output still reaches the
	 * container log, and the {@code tl-app} skill hands its commands to it.
	 */
	private void assertApplicationControl(String url) throws Exception {
		assertEquals(INIT_PROCESS + "\n", output(ExecCommand.of("cat", "/proc/1/comm")));
		for (int n = 0; n < 3; n++) {
			output(ExecCommand.of("sh", "-c", "sleep 1 & sleep 1 & exit 0"));
		}
		// The orphans end after a second and are reparented to PID 1.
		Thread.sleep(3000);
		String ps = output(ExecCommand.of("ps", "-eo", "stat,args"));
		for (String line : ps.split("\n")) {
			assertFalse("Zombie left: " + ps, line.trim().startsWith(ZOMBIE_STATE));
		}

		assertEquals(ApplicationWorkspaces.APP_CONTROL_COMMAND + "\n",
			output(ExecCommand.of("printenv", ApplicationWorkspaces.ENV_APP_CONTROL)));
		String control = ApplicationWorkspaces.APP_CONTROL_COMMAND;
		String localUrl = "http://localhost:" + WorkspaceSpec.DEFAULT_CONTAINER_PORT + _contextPath + "/";
		String status = output(ExecCommand.of(control, "status"));
		assertTrue(status, status.contains("status: running\n"));
		assertTrue(status, status.contains("url: " + localUrl + "\n"));
		assertTrue(status, status.contains("login: root / " + INITIAL_PASSWORD + "\n"));
		assertEquals(1, applicationProcesses());

		CollectingOutput containerLog = new CollectingOutput();
		try (LogFollower follower = _provider.followLog(_workspaceId, 0, null, containerLog)) {
			String restarted = output(ExecCommand.of(control, "restart", ApplicationWorkspaces.appDir(ARTIFACT_ID)));
			assertStarted(restarted, localUrl);
			containerLog.awaitStdoutContaining(UP_AND_RUNNING);
			containerLog.awaitStdoutContaining(SUPERVISOR_UP + localUrl + ".");
		}
		assertLoginPage(url);
		assertEquals(1, applicationProcesses());
		String runLog = output(ExecCommand.of("sh", "-c",
			"TL_WORKSPACE_APP_LOG_LINES=100000 " + control + " log"));
		assertTrue("Log of the restarted run incomplete.", runLog.contains(UP_AND_RUNNING));
		assertFalse("Database set up anew on restart.", runLog.contains(PASSWORD_FROM_ENV));
		assertFalse("Database set up anew on restart.", runLog.contains(RANDOM_PASSWORD));

		CollectingOutput wrongModule = new CollectingOutput();
		assertEquals(1, _provider.exec(_workspaceId, ExecCommand.of(control, "restart", "/tmp"), wrongModule));
		assertEquals(1, applicationProcesses());

		assertTrue(output(ExecCommand.of(control, "stop")).contains("Stopped."));
		assertEquals(WorkspaceState.RUNNING, _provider.status(_workspaceId).getState());
		assertEquals(0, applicationProcesses());
		CollectingOutput stopped = new CollectingOutput();
		assertEquals(STATUS_NOT_RUNNING, _provider.exec(_workspaceId, ExecCommand.of(control, "status"), stopped));
		assertTrue(stopped.stdout(), stopped.stdout().contains("status: stopped\n"));
		assertStarted(output(ExecCommand.of(control, "start")), localUrl);
		assertEquals(1, applicationProcesses());
		assertLoginPage(url);

		String tlApp = output(ExecCommand.of("sh", "-c", FIND_TL_APP_SCRIPT)).trim();
		assertFalse("No tl-app skill in the image.", tlApp.isEmpty());
		assertStarted(output(ExecCommand.of(tlApp, "restart", ApplicationWorkspaces.appDir(ARTIFACT_ID))), localUrl);
		assertEquals(1, applicationProcesses());
		assertLoginPage(url);

		assertFailedStart(url);
	}

	/**
	 * An application whose initialization fails - its server runs, but it does not answer - is
	 * reported as failed and stopped soon, the waits for it fail at once with the end of its log,
	 * and after the repair it starts again.
	 */
	private void assertFailedStart(String url) throws Exception {
		String layout = output(ExecCommand.of("sh", "-c", FIND_BROKEN_FILE)).trim();
		output(ExecCommand.of("sh", "-c", "cp " + layout + " " + layout + BACKUP_SUFFIX
			+ " && " + BREAK_COMMAND + layout + " && grep -q 'broken=' " + layout));

		long start = System.currentTimeMillis();
		CollectingOutput restart = new CollectingOutput();
		assertEquals(1, _provider.exec(_workspaceId,
			ExecCommand.of(ApplicationWorkspaces.APP_CONTROL_COMMAND, "restart"), restart));
		assertTrue(restart.stderr(), restart.stderr().contains("Error: "));
		assertTrue("Failure not recognized at once.", System.currentTimeMillis() - start < FAILURE_LIMIT.toMillis());
		assertEquals(ApplicationState.FAILED, _applications.applicationState(_workspaceId));
		assertEquals(0, applicationProcesses());

		CollectingOutput waitLog = new CollectingOutput();
		start = System.currentTimeMillis();
		try {
			_applications.awaitApplication(_workspaceId, _contextPath, waitLog);
			fail("A failed application is waited for.");
		} catch (ApplicationFailedException ex) {
			// Expected.
		}
		assertTrue("Wait did not fail at once.", System.currentTimeMillis() - start < WAIT.toMillis());
		assertTrue("No log of the failure: " + waitLog.stdout(), ERROR_IN_LOG.matcher(waitLog.stdout()).find());

		CollectingOutput startLog = new CollectingOutput();
		try {
			_applications.startApplication(_workspaceId, startLog);
			fail("A broken application started.");
		} catch (ApplicationFailedException ex) {
			// Expected.
		}

		output(ExecCommand.of("mv", layout + BACKUP_SUFFIX, layout));
		CollectingOutput repaired = new CollectingOutput();
		_applications.startApplication(_workspaceId, repaired);
		assertTrue(repaired.stdout(), repaired.stdout().contains("url: "));
		assertEquals(ApplicationState.RUNNING, _applications.applicationState(_workspaceId));
		assertLoginPage(_applications.awaitApplication(_workspaceId, _contextPath));
		assertLoginPage(url);
	}

	/**
	 * The output of a successful start: the URL, the log and the login of the application.
	 */
	private static void assertStarted(String output, String localUrl) {
		assertTrue(output, output.contains("url: " + localUrl + "\n"));
		assertTrue(output, output.contains("\nlog: ") || output.startsWith("log: "));
		assertTrue(output, output.contains("login: root / " + INITIAL_PASSWORD + "\n"));
	}

	/**
	 * The number of processes of the application in the workspace.
	 */
	private int applicationProcesses() throws InterruptedException {
		int result = 0;
		for (String line : output(ExecCommand.of("ps", "-eo", "args")).split("\n")) {
			if (line.contains(APPLICATION_PROCESS)) {
				result++;
			}
		}
		return result;
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
		return new ApplicationWorkspaces(_applications.getConfig(), _provider, _applications.getControlContextPath()) {
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

	/**
	 * The resources the login page refers to and the route of the layouts are served below the
	 * context path of the application.
	 */
	private void assertServedBelow(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();
		HttpResponse<String> login = client.send(
			HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30)).build(),
			HttpResponse.BodyHandlers.ofString());
		assertTrue("Login page not below the context path: " + login.uri(),
			login.uri().getPath().startsWith(_contextPath + "/"));
		Matcher resource = PAGE_RESOURCE.matcher(login.body());
		int resources = 0;
		while (resource.find()) {
			URI resourceUri = login.uri().resolve(resource.group(1).replace(" ", "%20"));
			assertTrue("Resource not below the context path: " + resourceUri,
				resourceUri.getPath().startsWith(_contextPath + "/"));
			assertEquals("Status of " + resourceUri, 200, status(client, resourceUri));
			resources++;
		}
		assertTrue("The login page refers to no script or stylesheet: " + login.body(), resources > 0);
		URI layouts = URI.create(url + LAYOUT_ROUTE);
		assertEquals("Status of " + layouts, 200, status(client, layouts));
	}

	/**
	 * The application is reached through the preview route of a control application in front of
	 * the workspace: only by a client logged into the control application, its login page with
	 * its scripts and stylesheets and its layouts below the context path, after redirects that
	 * stay below the origin of the control application; paths of the workspace outside the
	 * application are not forwarded. The control application and the application each keep their
	 * session, although both session cookies have the same name.
	 */
	private void assertPreviewRoute() throws Exception {
		try (Control control = startControl()) {
			CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
			HttpClient browser = HttpClient.newBuilder()
				.cookieHandler(cookies)
				.connectTimeout(Duration.ofSeconds(10))
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
			URI app = URI.create(control.origin() + _contextPath + "/");
			assertEquals("Not logged into the control application.", 403, status(browser, app));

			String controlSession = control.login(browser);
			HttpResponse<String> login = followBelow(browser, app, control.origin());
			String page = login.body().toLowerCase(Locale.ROOT);
			assertTrue("Not a login page: " + login.body(), page.contains("<html") && page.contains("login"));
			Matcher resource = PAGE_RESOURCE.matcher(login.body());
			int resources = 0;
			while (resource.find()) {
				URI resourceUri = login.uri().resolve(resource.group(1).replace(" ", "%20"));
				assertTrue("Resource not below the context path: " + resourceUri,
					resourceUri.getPath().startsWith(_contextPath + "/"));
				assertEquals("Status of " + resourceUri, 200, status(browser, resourceUri));
				resources++;
			}
			assertTrue("The login page refers to no script or stylesheet: " + login.body(), resources > 0);
			assertEquals(200, followBelow(browser, URI.create(app + LAYOUT_ROUTE), control.origin()).statusCode());
			assertEquals("A path of the workspace outside the application.", 404, status(browser, URI.create(
				control.origin() + CONTROL_CONTEXT + ApplicationWorkspaces.PREVIEW_PATH + "/" + _workspaceId + "/other/")));

			List<HttpCookie> sessions = cookies.getCookieStore().getCookies().stream()
				.filter(cookie -> cookie.getName().equals(PreviewServlet.DEFAULT_SESSION_COOKIE)).toList();
			List<String> paths = sessions.stream().map(HttpCookie::getPath).sorted().toList();
			assertEquals("One session cookie of each application: " + sessions, List.of(CONTROL_CONTEXT, _contextPath),
				paths);
			assertEquals("The session of the control application was replaced.", controlSession,
				control.login(browser));
			HttpResponse<String> again = followBelow(browser, URI.create(app + LAYOUT_ROUTE), control.origin());
			assertEquals(200, again.statusCode());
			assertTrue("The application started another session: " + again.headers().map(),
				again.headers().allValues(ProxyForwarding.SET_COOKIE).stream()
					.noneMatch(value -> value.startsWith(PreviewServlet.DEFAULT_SESSION_COOKIE + "=")));
		}
	}

	/**
	 * Requests the given URL through the preview route, following redirects that must stay below
	 * the given origin and the context path of the application.
	 *
	 * @return The first answer that is no redirect.
	 */
	private HttpResponse<String> followBelow(HttpClient browser, URI url, String origin) throws Exception {
		URI current = url;
		for (int n = 0; n <= MAX_REDIRECTS; n++) {
			HttpResponse<String> response = browser.send(
				HttpRequest.newBuilder(current).timeout(Duration.ofSeconds(30)).build(),
				HttpResponse.BodyHandlers.ofString());
			int status = response.statusCode();
			if (status < 300 || status >= 400) {
				return response;
			}
			String location = response.headers().firstValue("Location").orElse(null);
			assertNotNull("Redirect without location from " + current, location);
			URI next = current.resolve(location);
			assertTrue("Redirect leaves the origin of the control application: " + location,
				(next.getScheme() + "://" + next.getAuthority()).equals(origin));
			assertTrue("Redirect leaves the application: " + location,
				next.getPath().startsWith(_contextPath + "/") || next.getPath().equals(_contextPath));
			current = next;
		}
		fail("More than " + MAX_REDIRECTS + " redirects from " + url);
		return null;
	}

	/**
	 * A stream of Server-Sent Events from the published port of the workspace reaches the client
	 * of the preview route while it is produced. The application is stopped for it, and a server
	 * of such a stream listens on the container port in its place.
	 */
	private void assertStreamThroughPreview() throws Exception {
		assertTrue(output(ExecCommand.of(ApplicationWorkspaces.APP_CONTROL_COMMAND, "stop")).contains("Stopped."));
		CollectingOutput serverOutput = new CollectingOutput();
		try (ExecProcess server = _provider.startExec(_workspaceId,
			ExecCommand.of("node", "-e", SSE_SERVER, _contextPath + SSE_PATH), serverOutput);
				Control control = startControl()) {
			serverOutput.awaitStdoutContaining(SSE_LISTENING);
			HttpClient browser = HttpClient.newBuilder()
				.cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
				.connectTimeout(Duration.ofSeconds(10))
				.build();
			control.login(browser);
			HttpResponse<InputStream> response = browser.send(
				HttpRequest.newBuilder(URI.create(control.origin() + _contextPath + SSE_PATH)).build(),
				HttpResponse.BodyHandlers.ofInputStream());
			assertEquals(200, response.statusCode());
			assertEquals("text/event-stream",
				response.headers().firstValue(ProxyForwarding.CONTENT_TYPE).orElse(null));
			try (BufferedReader reader =
				new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
				List<String> lines = CompletableFuture.supplyAsync(() -> {
					List<String> result = new ArrayList<>();
					try {
						for (int n = 0; n < 6; n++) {
							result.add(reader.readLine());
						}
					} catch (IOException ex) {
						result.add(ex.toString());
					}
					return result;
				}).get(WAIT.toSeconds(), TimeUnit.SECONDS);
				assertEquals(List.of(": connected", "", "data: 0", "", "data: 1", ""), lines);
			}
			server.terminate();
		}
	}

	/**
	 * Starts a control application with the preview route in front of the workspace of the test,
	 * which lets a client logged in at {@link #CONTROL_LOGIN} pass.
	 */
	private Control startControl() throws Exception {
		PreviewWorkspaces workspaces = (request, workspaceId) -> {
			HttpSession session = request.getSession(false);
			if (session == null || session.getAttribute(LOGGED_IN) == null) {
				return PreviewLookup.refused(PreviewRefusal.NOT_LOGGED_IN);
			}
			if (!workspaceId.equals(_workspaceId)) {
				return PreviewLookup.refused(PreviewRefusal.NOT_FOUND);
			}
			return PreviewLookup.found(new PreviewTarget(_contextPath, StoredPreviewWorkspaces
				.origin(_provider.publishedAddress(), _provider.status(_workspaceId).getHostPort())));
		};
		Server jetty = new Server();
		ServerConnector connector = new ServerConnector(jetty);
		connector.setHost(WorkspaceProvider.LOOPBACK_ADDRESS);
		connector.setPort(0);
		jetty.addConnector(connector);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(CONTROL_CONTEXT);
		context.addServlet(new ServletHolder(new HttpServlet() {
			@Override
			protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
				HttpSession session = req.getSession(true);
				session.setAttribute(LOGGED_IN, Boolean.TRUE);
				resp.getWriter().write(session.getId());
			}
		}), CONTROL_LOGIN);
		ServletHolder proxyHolder = new ServletHolder(new PreviewServlet(workspaces));
		// The proxy answers asynchronously.
		proxyHolder.setAsyncSupported(true);
		context.addServlet(proxyHolder, ApplicationWorkspaces.PREVIEW_PATH + "/*");
		jetty.setHandler(context);
		jetty.start();
		return new Control(jetty, "http://" + WorkspaceProvider.LOOPBACK_ADDRESS + ":" + connector.getLocalPort());
	}

	/**
	 * A control application started by {@link #startControl()}.
	 */
	private record Control(Server jetty, String origin) implements AutoCloseable {

		/**
		 * Logs the given client into the control application.
		 *
		 * @return The session of the client in the control application.
		 */
		String login(HttpClient browser) throws IOException, InterruptedException {
			HttpResponse<String> response = browser.send(
				HttpRequest.newBuilder(URI.create(origin + CONTROL_CONTEXT + CONTROL_LOGIN)).build(),
				HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
			return response.body();
		}

		@Override
		public void close() throws Exception {
			jetty.stop();
		}
	}

	private static int status(HttpClient client, URI uri) throws IOException, InterruptedException {
		return client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).build(),
			HttpResponse.BodyHandlers.discarding()).statusCode();
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
