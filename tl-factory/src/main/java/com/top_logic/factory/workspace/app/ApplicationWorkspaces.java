/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.app;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.VolumeMount;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.util.error.TopLogicException;

/**
 * Workspaces each holding a TopLogic application: generated from the application archetype,
 * built, and run as the main process of the workspace container.
 *
 * <p>
 * All workspaces run the image built from the {@link #imageContext(String) workspace image
 * context}. The entry script of the image keeps the container alive until the application is built
 * (the file {@link #BUILT_MARKER} exists in the source volume), and from then on supervises the
 * application on the {@link WorkspaceSpec#getContainerPort() container port}, under the
 * {@link #contextPath(ApplicationSpec) context path} of the workspace. Starting and stopping
 * the workspace therefore starts and stops the application; in between, the
 * {@link #APP_CONTROL_COMMAND} stops and restarts it without ending the container. Its source,
 * database and Maven repository survive in the volumes of the workspace:
 * </p>
 *
 * <ul>
 * <li>{@link #SOURCE_VOLUME} at {@link #SOURCE_PATH}: the application, one directory named by its
 * artifact, including the database the application creates on its first start.</li>
 * <li>{@link #REPOSITORY_VOLUME} at {@link #REPOSITORY_PATH}: the local Maven repository of the
 * workspace.</li>
 * <li>The release repository of the engine version, a {@link VolumeMount#isShared() shared} volume
 * mounted read-only at {@link #RELEASE_PATH} and used as tail of the local Maven repository.</li>
 * <li>{@link #AGENT_SESSIONS_VOLUME} at {@link #AGENT_SESSIONS_PATH}: the conversations of the
 * coding agent, so that a conversation continues after the container was created anew.</li>
 * </ul>
 *
 * <p>
 * The release repository of an engine version is filled once by a short-lived resolver workspace
 * that builds a throwaway application of the same archetype into it, so that building an
 * application downloads (almost) nothing. The file {@link #RELEASE_COMPLETE_MARKER} records that
 * the repository is complete; a repository without it is filled again.
 * </p>
 *
 * <p>
 * The entry script and all commands run as the {@link #WORKSPACE_USER}, which is not root. The
 * image creates the mount points of all volumes owned by this user, so that a fresh volume belongs
 * to it as well. No command runs as root: the containers have no capabilities, so root could do
 * nothing the {@link #WORKSPACE_USER} cannot. A release repository the {@link #WORKSPACE_USER}
 * cannot write - filled by an image whose commands ran as root - is replaced by an empty one when
 * it has to be filled.
 * </p>
 */
public class ApplicationWorkspaces {

	/**
	 * Configuration of {@link ApplicationWorkspaces}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Configuration property name of {@link #getImageRepository()}.
		 *
		 * @see #getImageRepository()
		 */
		String IMAGE_REPOSITORY = "image-repository";

		/**
		 * Configuration property name of {@link #getRepositoryUrl()}.
		 *
		 * @see #getRepositoryUrl()
		 */
		String REPOSITORY_URL = "repository-url";

		/**
		 * Configuration property name of {@link #getEngineVersion()}.
		 *
		 * @see #getEngineVersion()
		 */
		String ENGINE_VERSION = "engine-version";

		/**
		 * Configuration property name of {@link #getStartTimeout()}.
		 *
		 * @see #getStartTimeout()
		 */
		String START_TIMEOUT = "start-timeout";

		/**
		 * Configuration property name of {@link #getBuildTimeout()}.
		 *
		 * @see #getBuildTimeout()
		 */
		String BUILD_TIMEOUT = "build-timeout";

		/**
		 * Configuration property name of {@link #getContextPath()}.
		 *
		 * @see #getContextPath()
		 */
		String CONTEXT_PATH = "context-path";

		/**
		 * Configuration property name of {@link #getApplicationOrigin()}.
		 *
		 * @see #getApplicationOrigin()
		 */
		String APPLICATION_ORIGIN = "application-origin";

		/**
		 * Name of the workspace image without tag.
		 *
		 * <p>
		 * The tag is the content hash of the files the image is built from.
		 * </p>
		 *
		 * @see #IMAGE_REPOSITORY
		 */
		@Name(IMAGE_REPOSITORY)
		@StringDefault("tl-factory-workspace")
		String getImageRepository();

		/**
		 * URL of the Maven repository the TopLogic releases are taken from.
		 *
		 * @see #REPOSITORY_URL
		 */
		@Name(REPOSITORY_URL)
		@StringDefault("https://dev.top-logic.com/nexus/repository/toplogic/")
		String getRepositoryUrl();

		/**
		 * The TopLogic version an application is built on that does not choose one.
		 *
		 * @see #ENGINE_VERSION
		 * @see ApplicationSpec#getEngineVersion()
		 */
		@Name(ENGINE_VERSION)
		@StringDefault("8.0.0-alpha11")
		String getEngineVersion();

		/**
		 * Seconds to wait for a started application to answer.
		 *
		 * @see #START_TIMEOUT
		 * @see ApplicationWorkspaces#awaitApplication(String, String)
		 */
		@Name(START_TIMEOUT)
		@IntDefault(600)
		int getStartTimeout();

		/**
		 * Seconds a Maven run may take: generating an application, building it, filling a release
		 * repository.
		 *
		 * <p>
		 * A run that takes longer is terminated and fails the operation.
		 * </p>
		 *
		 * @see #BUILD_TIMEOUT
		 * @see ExecCommand#getTimeout()
		 */
		@Name(BUILD_TIMEOUT)
		@IntDefault(1800)
		int getBuildTimeout();

		/**
		 * Pattern of the context path the application of a workspace is served under.
		 *
		 * <p>
		 * The placeholder <code>{context}</code> stands for the context path of the control
		 * application (empty if it is served at the root), <code>{workspace}</code> for the
		 * identifier of the workspace and <code>{artifact}</code> for the artifact of the
		 * application. The result must start with a slash and may have several segments, each of
		 * letters, digits, <code>.</code>, <code>_</code>, <code>~</code> and <code>-</code>; no
		 * segment may be <code>view</code> or <code>servlet</code>, and the first one not
		 * <code>admin</code>. A workspace keeps the context path its container was created with
		 * until it is updated.
		 * </p>
		 *
		 * <p>
		 * The default is the path of the workspace below the preview route
		 * <code>/preview</code> of the control application, which forwards the path of a request
		 * unchanged, so that the application serves the path the browser shows. A workspace whose
		 * context path is not below the preview route of its own identifier is not reached
		 * through the route. The same path also answers on the published port of the workspace.
		 * </p>
		 *
		 * @see #CONTEXT_PATH
		 * @see ApplicationWorkspaces#contextPath(String, String, String, String)
		 * @see ApplicationWorkspaces#update(ApplicationSpec, ExecOutput)
		 * @see com.top_logic.factory.preview.PreviewServlet
		 */
		@Name(CONTEXT_PATH)
		@StringDefault(DEFAULT_CONTEXT_PATH)
		String getContextPath();

		/**
		 * The origin (scheme, host and port, e.g. {@code https://factory.example.com}) under which
		 * a browser reaches the applications of the workspaces, empty if it is not known.
		 *
		 * <p>
		 * The applications get it as their external URL, the variable
		 * {@value ApplicationWorkspaces#ENV_HOST}; without it, an application takes
		 * {@code http://localhost:} followed by its port in the container.
		 * </p>
		 *
		 * <p>
		 * <b>Note:</b> An application started in development mode requests the path
		 * {@code /admin/stop} of the origin on every start to stop an instance running before. The
		 * origin must answer this request with a status other than 200, e.g. 404: a 200 with the
		 * body {@code OK} stops the server answering there, a 200 with another body keeps the
		 * application from starting ("Port is occupied").
		 * </p>
		 *
		 * @see #APPLICATION_ORIGIN
		 */
		@Name(APPLICATION_ORIGIN)
		String getApplicationOrigin();

	}

	/**
	 * Logical name of the volume holding the application sources.
	 */
	public static final String SOURCE_VOLUME = "source";

	/**
	 * Mount path of the {@link #SOURCE_VOLUME}.
	 */
	public static final String SOURCE_PATH = "/workspace";

	/**
	 * Logical name of the volume holding the local Maven repository of a workspace.
	 */
	public static final String REPOSITORY_VOLUME = "m2";

	/**
	 * Mount path of the {@link #REPOSITORY_VOLUME}.
	 */
	public static final String REPOSITORY_PATH = "/m2";

	/**
	 * Mount path of the release repository.
	 */
	public static final String RELEASE_PATH = "/release";

	/**
	 * Logical name of the volume holding the conversations of the coding agent of a workspace.
	 */
	public static final String AGENT_SESSIONS_VOLUME = "agent-sessions";

	/**
	 * Prefix of the logical name of the shared volume holding the release repository of an engine
	 * version, followed by the version.
	 */
	public static final String RELEASE_VOLUME_PREFIX = "release-";

	/**
	 * Name of the file in {@link #SOURCE_PATH} whose existence tells the entry script that the
	 * application is built and is to be run.
	 */
	public static final String BUILT_MARKER = ".tl-factory-built";

	/**
	 * Name of the file in {@link #RELEASE_PATH} whose existence records that the release
	 * repository is complete.
	 */
	public static final String RELEASE_COMPLETE_MARKER = ".tl-factory-complete";

	/**
	 * Environment variable of the entry script naming the {@link #BUILT_MARKER} file.
	 */
	public static final String ENV_BUILT_MARKER = "TL_FACTORY_BUILT_MARKER";

	/**
	 * Environment variable of the entry script naming the directory of the application.
	 */
	public static final String ENV_APP_DIR = "TL_FACTORY_APP_DIR";

	/**
	 * Environment variable of the entry script giving the port the application listens on.
	 */
	public static final String ENV_PORT = "TL_FACTORY_PORT";

	/**
	 * Environment variable of the entry script giving the context path the application is served
	 * under.
	 *
	 * @see #contextPath(ApplicationSpec)
	 */
	public static final String ENV_CONTEXT_PATH = "TL_FACTORY_CONTEXT_PATH";

	/**
	 * Environment variable giving a TopLogic application its external URL.
	 *
	 * @see Config#getApplicationOrigin()
	 */
	public static final String ENV_HOST = "tl_host";

	/**
	 * Placeholder in the {@link Config#getContextPath() pattern of the context path} replaced by
	 * the context path of the control application, empty if it is served at the root.
	 *
	 * @see com.top_logic.basic.AliasManager#APP_CONTEXT
	 */
	public static final String CONTEXT_PLACEHOLDER = "{context}";

	/**
	 * Placeholder in the {@link Config#getContextPath() pattern of the context path} replaced by
	 * the identifier of the workspace.
	 */
	public static final String WORKSPACE_PLACEHOLDER = "{workspace}";

	/**
	 * Placeholder in the {@link Config#getContextPath() pattern of the context path} replaced by
	 * the artifact of the application.
	 */
	public static final String ARTIFACT_PLACEHOLDER = "{artifact}";

	/**
	 * Path of the preview route below the context of the control application, see
	 * {@link com.top_logic.factory.preview.PreviewServlet}.
	 */
	public static final String PREVIEW_PATH = "/preview";

	/**
	 * Default of {@link Config#getContextPath()}: the path of the workspace below the
	 * {@link #PREVIEW_PATH preview route} of the control application.
	 */
	public static final String DEFAULT_CONTEXT_PATH =
		CONTEXT_PLACEHOLDER + PREVIEW_PATH + "/" + WORKSPACE_PLACEHOLDER + "/" + ARTIFACT_PLACEHOLDER;

	/**
	 * Segments no context path may have: the client of a TopLogic application finds its routes at
	 * the first segment {@code view} or {@code servlet} of a URL.
	 */
	public static final Set<String> RESERVED_SEGMENTS = Set.of("view", "servlet");

	/**
	 * First segment no context path may have: the development server of an application serves its
	 * administration (the stop of a running instance) there.
	 */
	public static final String RESERVED_FIRST_SEGMENT = "admin";

	/**
	 * Allowed segment of a context path.
	 */
	private static final Pattern CONTEXT_PATH_SEGMENT = Pattern.compile("[A-Za-z0-9_~-][A-Za-z0-9._~-]*");

	/**
	 * Environment variable with arguments Maven adds to every invocation.
	 */
	public static final String ENV_MAVEN_ARGS = "MAVEN_ARGS";

	/**
	 * Environment variable choosing the browser a started TopLogic application opens.
	 */
	public static final String ENV_BROWSER = "tl_browser";

	/**
	 * Environment variable giving a TopLogic application the password of the account {@code root}
	 * it creates with its database.
	 *
	 * @see ApplicationSpec#getInitialPassword()
	 */
	public static final String ENV_INITIAL_PASSWORD = "tl_initial_password";

	/**
	 * Value of {@link #ENV_BROWSER} opening no browser.
	 */
	public static final String BROWSER_NONE = "none";

	/**
	 * The user in the workspace image that runs the entry script and all commands.
	 */
	public static final String WORKSPACE_USER = "tl";

	/**
	 * Mount path of the {@link #AGENT_SESSIONS_VOLUME}: the directory in which Claude Code keeps the
	 * conversations it resumes, one directory per working directory.
	 *
	 * <p>
	 * Only this directory is a volume, not the configuration directory of Claude Code around it:
	 * the plugin, the MCP servers and the user instructions installed there come from the image, so
	 * that a workspace created anew on a current image has the current ones.
	 * </p>
	 */
	public static final String AGENT_SESSIONS_PATH = "/home/" + WORKSPACE_USER + "/.claude/projects";

	/**
	 * Placeholder in the {@link ImageContext#DOCKERFILE} of the image replaced by the
	 * {@link #WORKSPACE_USER}.
	 */
	public static final String WORKSPACE_USER_PLACEHOLDER = "@WORKSPACE_USER@";

	/**
	 * Placeholder in the {@link ImageContext#DOCKERFILE} of the image replaced by the mount paths
	 * of all volumes, separated by spaces.
	 *
	 * @see #VOLUME_PATHS
	 */
	public static final String VOLUME_PATHS_PLACEHOLDER = "@VOLUME_PATHS@";

	/**
	 * The mount paths of all volumes a workspace of this class has, the paths the image creates
	 * owned by the {@link #WORKSPACE_USER}.
	 */
	public static final List<String> VOLUME_PATHS =
		Collections.unmodifiableList(Arrays.asList(SOURCE_PATH, REPOSITORY_PATH, RELEASE_PATH, AGENT_SESSIONS_PATH));

	/**
	 * Placeholder in the Maven settings of the image replaced by the
	 * {@link Config#getRepositoryUrl() repository URL}.
	 */
	public static final String REPOSITORY_URL_PLACEHOLDER = "@REPOSITORY_URL@";

	/**
	 * Path of the Maven settings in the {@link #imageContext(String) image context}.
	 */
	public static final String SETTINGS_FILE = "settings.xml";

	/**
	 * Path of the entry script in the {@link #imageContext(String) image context}.
	 */
	public static final String ENTRY_SCRIPT = "entrypoint.sh";

	/**
	 * Path of the script of the {@link #APP_CONTROL_COMMAND} in the {@link #imageContext(String)
	 * image context}.
	 */
	public static final String APP_CONTROL_SCRIPT = "tl-workspace-app.sh";

	/**
	 * Path of the user instructions of Claude Code in the {@link #imageContext(String) image
	 * context}, installed for the {@link #WORKSPACE_USER}.
	 */
	public static final String USER_INSTRUCTIONS = "CLAUDE.md";

	/**
	 * Command in the workspace image controlling the application the entry script supervises:
	 * {@code status}, {@code start}, {@code stop}, {@code restart}, {@code log}.
	 *
	 * @see #ENV_APP_CONTROL
	 */
	public static final String APP_CONTROL_COMMAND = "tl-workspace-app";

	/**
	 * Environment variable of the workspace image naming the {@link #APP_CONTROL_COMMAND}, to which
	 * the {@code tl-app} skill of the TopLogic plugin hands starting and stopping the application.
	 */
	public static final String ENV_APP_CONTROL = "TL_APP_CONTROL";

	/**
	 * Argument of the {@link #APP_CONTROL_COMMAND} printing the state of the application.
	 */
	public static final String APP_CONTROL_STATUS = "status";

	/**
	 * Argument of the {@link #APP_CONTROL_COMMAND} starting the application and waiting until it
	 * answers.
	 */
	public static final String APP_CONTROL_START = "start";

	/**
	 * Argument of the {@link #APP_CONTROL_COMMAND} printing the end of the log of the application.
	 */
	public static final String APP_CONTROL_LOG = "log";

	/**
	 * Environment variable of the {@link #APP_CONTROL_COMMAND} giving the seconds {@code start}
	 * waits for the application.
	 */
	public static final String ENV_APP_TIMEOUT = "TL_WORKSPACE_APP_TIMEOUT";

	/**
	 * Environment variable of the {@link #APP_CONTROL_COMMAND} giving the number of lines
	 * {@code log} prints.
	 */
	public static final String ENV_APP_LOG_LINES = "TL_WORKSPACE_APP_LOG_LINES";

	/**
	 * Exit code of {@code status} of the {@link #APP_CONTROL_COMMAND}: the application answers.
	 */
	public static final int APP_STATUS_RUNNING = 0;

	/**
	 * Exit code of {@code status} of the {@link #APP_CONTROL_COMMAND}: the application does not
	 * answer (yet).
	 */
	public static final int APP_STATUS_NOT_RUNNING = 3;

	/**
	 * Exit code of {@code status} of the {@link #APP_CONTROL_COMMAND}: the application failed.
	 */
	public static final int APP_STATUS_FAILED = 4;

	/**
	 * Start of the line of {@code status} of the {@link #APP_CONTROL_COMMAND} naming the state of
	 * the supervisor.
	 */
	public static final String APP_STATUS_PREFIX = "status: ";

	/**
	 * States of the supervisor in which the application does not run and is not about to.
	 */
	private static final Set<String> SUPERVISOR_STOPPED = Set.of("stopped", "unbuilt");

	private static final Duration APP_STATUS_TIMEOUT = Duration.ofSeconds(30);

	private static final Duration APP_CONTROL_GRACE = Duration.ofSeconds(60);

	private static final int FAILURE_LOG_LINES = 60;

	/**
	 * Prefix of the identifier of the workspace filling a release repository, followed by the
	 * logical name of the release volume.
	 */
	public static final String RESOLVER_WORKSPACE_PREFIX = "resolve-";

	/**
	 * Group of the archetype applications are generated from.
	 */
	public static final String ARCHETYPE_GROUP_ID = "com.top-logic";

	/**
	 * Artifact of the archetype applications are generated from.
	 */
	public static final String ARCHETYPE_ARTIFACT_ID = "tl-archetype-app";

	/**
	 * Version of a generated application.
	 */
	public static final String APPLICATION_VERSION = "1.0-SNAPSHOT";

	private static final String IMAGE_RESOURCE_DIR = "image/";

	private static final String PROGRESS_PREFIX = "[tl-factory] ";

	private static final String LINE_END = "\n";

	private static final String ARCHETYPE_GOAL = "org.apache.maven.plugins:maven-archetype-plugin:3.4.1:generate";

	private static final String TL_PLUGIN = "com.top-logic:tl-maven-plugin";

	private static final String RESOLVE_GOAL = "resolve";

	private static final String RESOLVER_DIR = "/tmp";

	private static final String RESOLVER_GROUP_ID = "com.top-logic.factory";

	private static final String RESOLVER_ARTIFACT_ID = "resolve";

	private static final String PATH_SEPARATOR = "/";

	private static final String HTTP_SCHEME = "http://";

	private static final char PORT_SEPARATOR = ':';

	private static final char IPV6_START = '[';

	private static final char IPV6_END = ']';

	private static final long POLL_MILLIS = 2000;

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

	private static final int MAX_OUTPUT_TAIL = 16 * 1024;

	private static final int HTTP_OK = 200;

	private static final int HTTP_REDIRECT_MIN = 300;

	private static final int HTTP_REDIRECT_MAX = 399;

	private final Config _config;

	private final WorkspaceProvider _provider;

	private final String _controlContextPath;

	private final Object _imageLock = new Object();

	private final Map<String, Object> _releaseLocks = new ConcurrentHashMap<>();

	private volatile String _currentImage;

	/**
	 * Creates {@link ApplicationWorkspaces}.
	 *
	 * @param config
	 *        The configuration.
	 * @param provider
	 *        The provider the workspaces are created by.
	 */
	public ApplicationWorkspaces(Config config, WorkspaceProvider provider) {
		this(config, provider, "");
	}

	/**
	 * Creates {@link ApplicationWorkspaces}.
	 *
	 * @param config
	 *        The configuration.
	 * @param provider
	 *        The provider the workspaces are created by.
	 * @param controlContextPath
	 *        The context path of the control application, empty if it is served at the root;
	 *        replaces the {@link #CONTEXT_PLACEHOLDER} of the {@link Config#getContextPath()
	 *        pattern of the context path}.
	 */
	public ApplicationWorkspaces(Config config, WorkspaceProvider provider, String controlContextPath) {
		_config = config;
		_provider = provider;
		_controlContextPath = controlContextPath;
	}

	/**
	 * The context path of the control application, empty if it is served at the root.
	 *
	 * @see #ApplicationWorkspaces(Config, WorkspaceProvider, String)
	 */
	public String getControlContextPath() {
		return _controlContextPath;
	}

	/**
	 * The configuration.
	 */
	public Config getConfig() {
		return _config;
	}

	/**
	 * The provider the workspaces are created by.
	 */
	public WorkspaceProvider getProvider() {
		return _provider;
	}

	/**
	 * Creates a workspace with a TopLogic application, builds the application and lets the
	 * workspace run it.
	 *
	 * <p>
	 * The sources of the application come from the {@link ApplicationSpec#getSource() source} of
	 * the spec, by default they are generated from the archetype. The method returns as soon as the
	 * application is built; the application is starting then. Use
	 * {@link #awaitApplication(String, String)} to wait until it answers. If a step fails, the
	 * workspace is left as it is for inspection.
	 * </p>
	 *
	 * @param spec
	 *        The application to create.
	 * @param progress
	 *        Receiver of the output of all steps while they run.
	 * @return The running workspace.
	 */
	public WorkspaceInfo create(ApplicationSpec spec, ExecOutput progress) throws InterruptedException {
		String version = engineVersion(spec);
		String image = ensureImage(progress);
		String releaseVolume = ensureReleaseRepository(image, version, progress);

		String workspaceId = spec.getWorkspaceId();
		String appDir = appDir(spec);
		_provider.create(workspaceSpec(spec, image, releaseVolume));
		_provider.start(workspaceId);

		spec.getSource().provide(new SourceContext(spec, version, progress));

		step(progress, "Building application");
		run(workspaceId, ExecCommand.of("mvn", "-B", "install").setWorkingDir(appDir).setTimeout(buildTimeout()),
			progress);

		run(workspaceId, ExecCommand.of("touch", builtMarker()), progress);
		step(progress, "Application built, starting it");
		return _provider.status(workspaceId);
	}

	/**
	 * Creates the container of an existing workspace anew on the current workspace image, keeping
	 * its application, its database and its Maven repository.
	 *
	 * <p>
	 * The image is built if necessary, the release repository of the engine version is filled if
	 * it is not complete. The container gets the environment of the given spec, which must
	 * describe the application the workspace was created with. A running workspace is stopped; the
	 * new container is not started.
	 * </p>
	 *
	 * @param spec
	 *        The application of the workspace.
	 * @param progress
	 *        Receiver of the output of all steps while they run.
	 * @return The workspace in its new container, see {@link WorkspaceInfo#getImage()}.
	 * @see WorkspaceProvider#recreate(WorkspaceSpec)
	 */
	public WorkspaceInfo update(ApplicationSpec spec, ExecOutput progress) throws InterruptedException {
		String image = ensureImage(progress);
		String releaseVolume = ensureReleaseRepository(image, engineVersion(spec), progress);
		step(progress, "Creating the container anew on image " + image);
		return _provider.recreate(workspaceSpec(spec, image, releaseVolume));
	}

	/**
	 * The reference of the image new and updated workspaces run, whether or not it is built yet.
	 *
	 * <p>
	 * A workspace whose container runs another image is outdated, see
	 * {@link #update(ApplicationSpec, ExecOutput)}.
	 * </p>
	 *
	 * @see #ensureImage(ExecOutput)
	 */
	public String currentImage() {
		String result = _currentImage;
		if (result == null) {
			result = workspaceImageContext().imageReference(_config.getImageRepository());
			_currentImage = result;
		}
		return result;
	}

	/**
	 * Starts a workspace and with it its application.
	 *
	 * @see WorkspaceProvider#start(String)
	 */
	public WorkspaceInfo start(String workspaceId) {
		return _provider.start(workspaceId);
	}

	/**
	 * Starts the stopped or failed application of a running workspace through the
	 * {@link #APP_CONTROL_COMMAND} and waits until it answers, at most
	 * {@link Config#getStartTimeout()}.
	 *
	 * @param workspaceId
	 *        The running workspace.
	 * @param progress
	 *        Receiver of the output of the command, the end of the log of the application if it
	 *        fails.
	 * @throws ApplicationFailedException
	 *         If the application failed to start.
	 */
	public void startApplication(String workspaceId, ExecOutput progress) throws InterruptedException {
		int timeout = _config.getStartTimeout();
		ExecCommand command = ExecCommand.of(APP_CONTROL_COMMAND, APP_CONTROL_START)
			.putEnv(ENV_APP_TIMEOUT, Integer.toString(timeout))
			// The command ends by itself after the start timeout.
			.setTimeout(Duration.ofSeconds(timeout).plus(APP_CONTROL_GRACE));
		OutputTail tail = new OutputTail(progress);
		int exitCode = _provider.exec(workspaceId, command, tail);
		if (exitCode != 0) {
			if (applicationState(workspaceId) == ApplicationState.FAILED) {
				throw new ApplicationFailedException(workspaceId);
			}
			throw new TopLogicException(I18NConstants.ERROR_COMMAND_FAILED__WORKSPACE_COMMAND_CODE_OUTPUT
				.fill(workspaceId, command.toString(), exitCode, tail.toString()));
		}
	}

	/**
	 * The state of the application in a running workspace, as its supervisor reports it.
	 *
	 * @param workspaceId
	 *        The running workspace.
	 * @return The state, {@link ApplicationState#UNKNOWN} if the workspace does not tell, e.g.
	 *         because its image has no {@link #APP_CONTROL_COMMAND}.
	 */
	public ApplicationState applicationState(String workspaceId) throws InterruptedException {
		StringBuilder output = new StringBuilder();
		int exitCode;
		try {
			exitCode = _provider.exec(workspaceId,
				ExecCommand.of(APP_CONTROL_COMMAND, APP_CONTROL_STATUS).setTimeout(APP_STATUS_TIMEOUT),
				new ExecOutput() {
					@Override
					public void stdout(String text) {
						output.append(text);
					}
				});
		} catch (TopLogicException ex) {
			Logger.info("No application state of workspace " + workspaceId + ": " + ex.getMessage(),
				ApplicationWorkspaces.class);
			return ApplicationState.UNKNOWN;
		}
		return ApplicationState.of(exitCode, output.toString());
	}

	/**
	 * Writes the end of the log of a failed application to the given output and fails.
	 */
	private void failure(String workspaceId, ExecOutput progress) throws InterruptedException {
		_provider.exec(workspaceId, ExecCommand.of(APP_CONTROL_COMMAND, APP_CONTROL_LOG)
			.putEnv(ENV_APP_LOG_LINES, Integer.toString(FAILURE_LOG_LINES))
			.setTimeout(APP_STATUS_TIMEOUT), progress);
		throw new ApplicationFailedException(workspaceId);
	}

	/**
	 * The state of the application in a running workspace, as the {@link #APP_CONTROL_COMMAND}
	 * reports it.
	 */
	public enum ApplicationState {

		/**
		 * The application answers.
		 */
		RUNNING,

		/**
		 * The application is starting or stopping.
		 */
		CHANGING,

		/**
		 * The application is stopped on request, or not built yet.
		 */
		STOPPED,

		/**
		 * The application ended by itself or its initialization failed.
		 */
		FAILED,

		/**
		 * The workspace does not tell.
		 */
		UNKNOWN;

		/**
		 * The state reported by {@code status} of the {@link #APP_CONTROL_COMMAND}.
		 *
		 * @param exitCode
		 *        The exit code of the command.
		 * @param output
		 *        Its output, with a line {@value ApplicationWorkspaces#APP_STATUS_PREFIX} followed by
		 *        the state of the supervisor.
		 */
		public static ApplicationState of(int exitCode, String output) {
			switch (exitCode) {
				case APP_STATUS_RUNNING:
					return RUNNING;
				case APP_STATUS_FAILED:
					return FAILED;
				case APP_STATUS_NOT_RUNNING:
					for (String line : output.split(LINE_END)) {
						if (line.startsWith(APP_STATUS_PREFIX)) {
							String state = line.substring(APP_STATUS_PREFIX.length()).trim();
							return SUPERVISOR_STOPPED.contains(state) ? STOPPED : CHANGING;
						}
					}
					return UNKNOWN;
				default:
					return UNKNOWN;
			}
		}
	}

	/**
	 * Stops a workspace and with it its application.
	 *
	 * @see WorkspaceProvider#stop(String)
	 */
	public WorkspaceInfo stop(String workspaceId) {
		return _provider.stop(workspaceId);
	}

	/**
	 * Removes a workspace with its application, its database and its Maven repository.
	 *
	 * <p>
	 * The release repository the workspace used is kept.
	 * </p>
	 *
	 * @see WorkspaceProvider#remove(String)
	 */
	public void remove(String workspaceId) {
		_provider.remove(workspaceId);
	}

	/**
	 * Waits until the application of a running workspace answers HTTP requests.
	 *
	 * @see #awaitApplication(String, String, ExecOutput)
	 */
	public String awaitApplication(String workspaceId, String contextPath) throws InterruptedException {
		return awaitApplication(workspaceId, contextPath, ExecOutput.NONE);
	}

	/**
	 * Waits until the application of a running workspace answers HTTP requests.
	 *
	 * <p>
	 * The application answers when a request of its context path is answered with status 200 or a
	 * redirect, e.g. to the login page. The wait fails if the workspace stops or the application
	 * does not answer within {@link Config#getStartTimeout()}. It fails at once if the supervisor in
	 * the container reports the application as {@link ApplicationState#FAILED failed}; the end of
	 * the log of the application is written to the given output then.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace whose application is waited for.
	 * @param contextPath
	 *        The context path of the application, see {@link #contextPath(ApplicationSpec)}.
	 * @param progress
	 *        Receiver of the end of the log of an application that failed.
	 * @return The URL of the application.
	 * @throws ApplicationFailedException
	 *         If the application failed to start.
	 */
	public String awaitApplication(String workspaceId, String contextPath, ExecOutput progress)
			throws InterruptedException {
		long timeoutMillis = _config.getStartTimeout() * 1000L;
		long deadline = System.currentTimeMillis() + timeoutMillis;
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(REQUEST_TIMEOUT)
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
		String url = null;
		while (true) {
			WorkspaceInfo info = _provider.status(workspaceId);
			if (info.getState() != WorkspaceState.RUNNING || !info.hasHostPort()) {
				throw new TopLogicException(I18NConstants.ERROR_WORKSPACE_STOPPED__WORKSPACE.fill(workspaceId));
			}
			url = applicationUrl(info, contextPath);
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).build();
				int status = client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
				if (status == HTTP_OK || (status >= HTTP_REDIRECT_MIN && status <= HTTP_REDIRECT_MAX)) {
					return url;
				}
			} catch (IOException ex) {
				// Not yet listening.
			}
			if (applicationState(workspaceId) == ApplicationState.FAILED) {
				failure(workspaceId, progress);
			}
			if (System.currentTimeMillis() >= deadline) {
				throw new TopLogicException(I18NConstants.ERROR_APPLICATION_NOT_REACHABLE__WORKSPACE_URL_SECONDS
					.fill(workspaceId, url, _config.getStartTimeout()));
			}
			Thread.sleep(POLL_MILLIS);
		}
	}

	/**
	 * The URL under which this application reaches the application with the given context path in
	 * a running workspace: at the {@link WorkspaceProvider#publishedAddress() address the ports are
	 * published on} and the {@link WorkspaceInfo#getHostPort() host port} of the workspace.
	 *
	 * <p>
	 * This URL is for the control application talking to the container, e.g. waiting for the
	 * application. A browser opens the application through the preview route, see
	 * {@link #previewUrl(String, String)}.
	 * </p>
	 *
	 * @param workspace
	 *        The running workspace.
	 * @param contextPath
	 *        The context path of the application, see {@link #contextPath(ApplicationSpec)}.
	 */
	public String applicationUrl(WorkspaceInfo workspace, String contextPath) {
		if (!workspace.hasHostPort()) {
			throw new IllegalArgumentException("Workspace has no published port: " + workspace);
		}
		return applicationUrl(_provider.publishedAddress(), workspace.getHostPort(), contextPath);
	}

	/**
	 * The URL of the application with the given context path listening on the given address and
	 * port.
	 *
	 * @param address
	 *        The address the port is published on, see {@link WorkspaceProvider#publishedAddress()}.
	 * @param port
	 *        The host port of the workspace.
	 * @param contextPath
	 *        The context path of the application.
	 * @return The {@link #origin(String, int) origin} followed by the context path and a slash.
	 */
	public static String applicationUrl(String address, int port, String contextPath) {
		String path = contextPath.startsWith(PATH_SEPARATOR) ? contextPath : PATH_SEPARATOR + contextPath;
		return origin(address, port) + path + PATH_SEPARATOR;
	}

	/**
	 * The origin of an application listening on the given address and port.
	 */
	public static String origin(String address, int port) {
		boolean ipv6 = address.indexOf(PORT_SEPARATOR) >= 0 && address.charAt(0) != IPV6_START;
		String host = ipv6 ? IPV6_START + address + IPV6_END : address;
		return HTTP_SCHEME + host + PORT_SEPARATOR + port;
	}

	/**
	 * The URL under which a browser opens the application of a workspace: the path of the
	 * application below the {@link #PREVIEW_PATH preview route} of the control application.
	 *
	 * @param workspaceId
	 *        The identifier of the workspace.
	 * @param contextPath
	 *        The context path the application of the workspace is served under.
	 * @return The path-absolute URL, {@code null} if the application is not reached through the
	 *         preview route.
	 * @see #previewUrl(String, String, String)
	 */
	public String previewUrl(String workspaceId, String contextPath) {
		return previewUrl(_controlContextPath, workspaceId, contextPath);
	}

	/**
	 * The URL under which a browser opens the application of a workspace through the
	 * {@link #PREVIEW_PATH preview route} of the control application.
	 *
	 * <p>
	 * The route forwards the requests below {@code <control context>/preview/<workspace>} that are
	 * below the context path of the application of the workspace, unchanged. So the application is
	 * reached through it only if its context path is that path of the route or below it; it is not
	 * for a context path created with another pattern or before the context path of a workspace
	 * started with the one of the control application, until the workspace is updated.
	 * </p>
	 *
	 * @param controlContextPath
	 *        The context path of the control application, empty if it is served at the root.
	 * @param workspaceId
	 *        The identifier of the workspace.
	 * @param contextPath
	 *        The context path the application of the workspace is served under.
	 * @return The context path followed by a slash, a path-absolute URL that needs no origin;
	 *         {@code null} if the application is not reached through the preview route.
	 */
	public static String previewUrl(String controlContextPath, String workspaceId, String contextPath) {
		if (contextPath == null) {
			return null;
		}
		String route = controlContextPath + PREVIEW_PATH + PATH_SEPARATOR + workspaceId;
		if (!contextPath.equals(route) && !contextPath.startsWith(route + PATH_SEPARATOR)) {
			return null;
		}
		return contextPath + PATH_SEPARATOR;
	}

	/**
	 * The context path the application of the given spec is served under: the
	 * {@link ApplicationSpec#getContextPath() one of the spec}, or the one of the
	 * {@link Config#getContextPath() configured pattern}.
	 */
	public String contextPath(ApplicationSpec spec) {
		String given = spec.getContextPath();
		return given != null ? given : contextPath(spec.getWorkspaceId(), spec.getArtifactId());
	}

	/**
	 * The context path of the {@link Config#getContextPath() configured pattern} for the
	 * application with the given artifact in the given workspace, below the
	 * {@link #getControlContextPath() context path of the control application}.
	 *
	 * @see #contextPath(String, String, String, String)
	 */
	public String contextPath(String workspaceId, String artifactId) {
		return contextPath(_config.getContextPath(), _controlContextPath, workspaceId, artifactId);
	}

	/**
	 * The context path of the given pattern for the application with the given artifact in the
	 * given workspace.
	 *
	 * @param pattern
	 *        The pattern, see {@link Config#getContextPath()}.
	 * @param controlContextPath
	 *        The context path of the control application, empty if it is served at the root,
	 *        replacing {@link #CONTEXT_PLACEHOLDER}.
	 * @param workspaceId
	 *        The identifier of the workspace, replacing {@link #WORKSPACE_PLACEHOLDER}.
	 * @param artifactId
	 *        The artifact of the application, replacing {@link #ARTIFACT_PLACEHOLDER}.
	 * @return The context path: a slash followed by segments separated by slashes, each of
	 *         letters, digits, {@code .}, {@code _}, {@code ~} and {@code -}, not starting with
	 *         {@code .}, none of the {@link #RESERVED_SEGMENTS} and not starting with the
	 *         {@link #RESERVED_FIRST_SEGMENT}.
	 * @throws TopLogicException
	 *         If the result is no such context path.
	 */
	public static String contextPath(String pattern, String controlContextPath, String workspaceId,
			String artifactId) {
		String result = pattern
			.replace(CONTEXT_PLACEHOLDER, controlContextPath)
			.replace(WORKSPACE_PLACEHOLDER, workspaceId)
			.replace(ARTIFACT_PLACEHOLDER, artifactId);
		if (!isValidContextPath(result)) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_CONTEXT_PATH__PATH_PATTERN.fill(result, pattern));
		}
		return result;
	}

	/**
	 * The context path of an application that is served under its artifact, the default of a
	 * TopLogic application in development mode.
	 */
	public static String defaultContextPath(String artifactId) {
		return PATH_SEPARATOR + artifactId;
	}

	private static boolean isValidContextPath(String contextPath) {
		if (!contextPath.startsWith(PATH_SEPARATOR)) {
			return false;
		}
		String[] segments = contextPath.substring(PATH_SEPARATOR.length()).split(PATH_SEPARATOR, -1);
		if (segments[0].equals(RESERVED_FIRST_SEGMENT)) {
			return false;
		}
		for (String segment : segments) {
			if (!CONTEXT_PATH_SEGMENT.matcher(segment).matches() || RESERVED_SEGMENTS.contains(segment)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Makes sure that the workspace image is available, building it if necessary.
	 *
	 * @param progress
	 *        Receiver of the output of the image build.
	 * @return The reference of the image.
	 */
	public String ensureImage(ExecOutput progress) throws InterruptedException {
		synchronized (_imageLock) {
			return _provider.buildImage(_config.getImageRepository(), workspaceImageContext(), progress);
		}
	}

	/**
	 * The files the image of the workspaces is built from.
	 *
	 * @implSpec The {@link #imageContext(String) workspace image context} for the configured
	 *           {@link Config#getRepositoryUrl() repository URL}.
	 */
	protected ImageContext workspaceImageContext() {
		return imageContext(_config.getRepositoryUrl());
	}

	/**
	 * Creates and starts the workspace filling a release repository.
	 */
	private void startResolver(String resolverId, String image, String volume) throws InterruptedException {
		_provider.create(new WorkspaceSpec(resolverId, image)
			.addVolume(VolumeMount.shared(volume, RELEASE_PATH, false))
			.putEnv(ENV_MAVEN_ARGS, "-Dmaven.repo.local=" + RELEASE_PATH)
			// There is no application in the resolver, the entry script only keeps it alive.
			.putEnv(ENV_BUILT_MARKER, builtMarker())
			.putEnv(ENV_APP_DIR, SOURCE_PATH)
			.putEnv(ENV_PORT, Integer.toString(WorkspaceSpec.DEFAULT_CONTAINER_PORT)));
		_provider.start(resolverId);
	}

	/**
	 * Makes sure that the release repository of an engine version is complete, filling it if
	 * necessary.
	 *
	 * @param image
	 *        The workspace image, see {@link #ensureImage(ExecOutput)}.
	 * @param version
	 *        The engine version.
	 * @param progress
	 *        Receiver of the output of the commands filling the repository.
	 * @return The logical name of the shared volume holding the repository.
	 */
	public String ensureReleaseRepository(String image, String version, ExecOutput progress)
			throws InterruptedException {
		String volume = releaseVolume(version);
		synchronized (_releaseLocks.computeIfAbsent(volume, x -> new Object())) {
			_provider.ensureSharedVolume(volume);

			String resolverId = RESOLVER_WORKSPACE_PREFIX + volume;
			WorkspaceInfo leftOver = _provider.status(resolverId);
			if (leftOver.getState() != WorkspaceState.MISSING || !leftOver.getVolumes().isEmpty()) {
				_provider.remove(resolverId);
			}
			startResolver(resolverId, image, volume);
			try {
				String marker = RELEASE_PATH + PATH_SEPARATOR + RELEASE_COMPLETE_MARKER;
				if (_provider.exec(resolverId, ExecCommand.of("test", "-f", marker), ExecOutput.NONE) == 0) {
					return volume;
				}
				if (_provider.exec(resolverId, ExecCommand.of("test", "-w", RELEASE_PATH), ExecOutput.NONE) != 0) {
					// An incomplete repository filled by an image whose commands ran as root.
					step(progress, "Replacing the incomplete release repository of TopLogic " + version
						+ ", which the workspace user cannot write");
					_provider.remove(resolverId);
					_provider.removeSharedVolume(volume);
					_provider.ensureSharedVolume(volume);
					startResolver(resolverId, image, volume);
				}

				step(progress, "Filling the release repository of TopLogic " + version);
				String appDir = RESOLVER_DIR + PATH_SEPARATOR + RESOLVER_ARTIFACT_ID;
				run(resolverId, ExecCommand.of("rm", "-rf", appDir), progress);
				run(resolverId,
					generateCommand(version, RESOLVER_GROUP_ID, RESOLVER_ARTIFACT_ID).setWorkingDir(RESOLVER_DIR)
						.setTimeout(buildTimeout()),
					progress);
				run(resolverId,
					ExecCommand.of(resolveCommand(version)).setWorkingDir(appDir).setTimeout(buildTimeout()),
					progress);
				run(resolverId, ExecCommand.of("touch", marker), progress);
				return volume;
			} finally {
				_provider.remove(resolverId);
			}
		}
	}

	/**
	 * The logical name of the shared volume holding the release repository of the given engine
	 * version.
	 */
	public static String releaseVolume(String version) {
		return RELEASE_VOLUME_PREFIX + version;
	}

	/**
	 * The files the workspace image is built from.
	 *
	 * @param repositoryUrl
	 *        The URL of the Maven repository the TopLogic releases are taken from.
	 */
	public static ImageContext imageContext(String repositoryUrl) {
		String settings = resource(SETTINGS_FILE).replace(REPOSITORY_URL_PLACEHOLDER, xmlText(repositoryUrl));
		String dockerfile = resource(ImageContext.DOCKERFILE)
			.replace(WORKSPACE_USER_PLACEHOLDER, WORKSPACE_USER)
			.replace(VOLUME_PATHS_PLACEHOLDER, String.join(" ", VOLUME_PATHS));
		return new ImageContext()
			.add(ImageContext.DOCKERFILE, dockerfile)
			.add(ENTRY_SCRIPT, resource(ENTRY_SCRIPT))
			.add(APP_CONTROL_SCRIPT, resource(APP_CONTROL_SCRIPT))
			.add(USER_INSTRUCTIONS, resource(USER_INSTRUCTIONS))
			.add(SETTINGS_FILE, settings);
	}

	/**
	 * The Maven arguments of a workspace: the workspace repository as local repository, the
	 * release repository as its read-only tail.
	 */
	static List<String> workspaceMavenArgs() {
		return Arrays.asList(
			"-Dmaven.repo.local=" + REPOSITORY_PATH,
			"-Dmaven.repo.local.tail=" + RELEASE_PATH,
			"-Daether.chainedLocalRepository.ignoreTailAvailability=true");
	}

	/**
	 * The command generating an application from the archetype in the working directory.
	 */
	static ExecCommand generateCommand(String version, String groupId, String artifactId) {
		return ExecCommand.of("mvn", "-B", ARCHETYPE_GOAL,
			"-DarchetypeGroupId=" + ARCHETYPE_GROUP_ID,
			"-DarchetypeArtifactId=" + ARCHETYPE_ARTIFACT_ID,
			"-DarchetypeVersion=" + version,
			// The archetype is given completely, no catalog has to be downloaded to find it.
			"-DarchetypeCatalog=internal",
			"-DgroupId=" + groupId,
			"-DartifactId=" + artifactId,
			"-Dversion=" + APPLICATION_VERSION,
			"-DinteractiveMode=false");
	}

	/**
	 * The command resolving everything building and running an application needs: the engine
	 * artifacts, the build and the launch of the application (whose execution is skipped).
	 */
	static List<String> resolveCommand(String version) {
		List<String> result = new ArrayList<>();
		result.add("mvn");
		result.add("-B");
		result.add("-Dexec.skip=true");
		result.add(TL_PLUGIN + ":" + version + ":" + RESOLVE_GOAL);
		result.add("install");
		result.add("exec:java");
		return result;
	}

	/**
	 * The description of the container of the application of a workspace.
	 */
	private WorkspaceSpec workspaceSpec(ApplicationSpec spec, String image, String releaseVolume) {
		WorkspaceSpec workspace = new WorkspaceSpec(spec.getWorkspaceId(), image)
			.addVolume(new VolumeMount(SOURCE_VOLUME, SOURCE_PATH))
			.addVolume(new VolumeMount(REPOSITORY_VOLUME, REPOSITORY_PATH))
			.addVolume(VolumeMount.shared(releaseVolume, RELEASE_PATH, true))
			.addVolume(new VolumeMount(AGENT_SESSIONS_VOLUME, AGENT_SESSIONS_PATH))
			.putEnv(ENV_MAVEN_ARGS, String.join(" ", workspaceMavenArgs()))
			.putEnv(ENV_APP_DIR, appDir(spec))
			.putEnv(ENV_CONTEXT_PATH, contextPath(spec))
			.putEnv(ENV_BUILT_MARKER, builtMarker())
			.putEnv(ENV_PORT, Integer.toString(WorkspaceSpec.DEFAULT_CONTAINER_PORT))
			.putEnv(ENV_BROWSER, BROWSER_NONE);
		String origin = _config.getApplicationOrigin();
		if (origin != null && !origin.isEmpty()) {
			workspace.putEnv(ENV_HOST, origin);
		}
		String initialPassword = spec.getInitialPassword();
		if (initialPassword != null) {
			workspace.putEnv(ENV_INITIAL_PASSWORD, initialPassword);
		}
		spec.getEnv().forEach(workspace::putEnv);
		return workspace;
	}

	private static String appDir(ApplicationSpec spec) {
		return appDir(spec.getArtifactId());
	}

	/**
	 * The directory in a workspace holding the sources of the application with the given artifact
	 * ID.
	 */
	public static String appDir(String artifactId) {
		return SOURCE_PATH + PATH_SEPARATOR + artifactId;
	}

	private String engineVersion(ApplicationSpec spec) {
		String version = spec.getEngineVersion();
		return version == null || version.isEmpty() ? _config.getEngineVersion() : version;
	}

	private Duration buildTimeout() {
		return Duration.ofSeconds(_config.getBuildTimeout());
	}

	private static String builtMarker() {
		return SOURCE_PATH + PATH_SEPARATOR + BUILT_MARKER;
	}

	private void run(String workspaceId, ExecCommand command, ExecOutput progress) throws InterruptedException {
		OutputTail tail = new OutputTail(progress);
		int exitCode = _provider.exec(workspaceId, command, tail);
		if (exitCode != 0) {
			throw new TopLogicException(I18NConstants.ERROR_COMMAND_FAILED__WORKSPACE_COMMAND_CODE_OUTPUT
				.fill(workspaceId, command.toString(), exitCode, tail.toString()));
		}
	}

	private static void step(ExecOutput progress, String message) {
		Logger.info(message + ".", ApplicationWorkspaces.class);
		progress.stdout(PROGRESS_PREFIX + message + LINE_END);
	}

	private static String resource(String name) {
		try (InputStream in = ApplicationWorkspaces.class.getResourceAsStream(IMAGE_RESOURCE_DIR + name)) {
			if (in == null) {
				throw new TopLogicException(I18NConstants.ERROR_MISSING_IMAGE_RESOURCE__NAME.fill(name));
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_MISSING_IMAGE_RESOURCE__NAME.fill(name), ex);
		}
	}

	private static String xmlText(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/**
	 * The {@link ApplicationSource.Context} of an application being created.
	 */
	private final class SourceContext implements ApplicationSource.Context {

		private final ApplicationSpec _spec;

		private final String _version;

		private final ExecOutput _progress;

		SourceContext(ApplicationSpec spec, String version, ExecOutput progress) {
			_spec = spec;
			_version = version;
			_progress = progress;
		}

		@Override
		public ApplicationSpec spec() {
			return _spec;
		}

		@Override
		public String engineVersion() {
			return _version;
		}

		@Override
		public String appDir() {
			return ApplicationWorkspaces.appDir(_spec);
		}

		@Override
		public Duration commandTimeout() {
			return buildTimeout();
		}

		@Override
		public void step(String message) {
			ApplicationWorkspaces.step(_progress, message);
		}

		@Override
		public void run(ExecCommand command) throws InterruptedException {
			ApplicationWorkspaces.this.run(_spec.getWorkspaceId(), command, _progress);
		}

		@Override
		public String output(ExecCommand command) throws InterruptedException {
			StringBuilder stdout = new StringBuilder();
			OutputTail tail = new OutputTail(new ExecOutput() {
				@Override
				public void stdout(String text) {
					stdout.append(text);
				}

				@Override
				public void stderr(String text) {
					_progress.stderr(text);
				}
			});
			int exitCode = _provider.exec(_spec.getWorkspaceId(), command, tail);
			if (exitCode != 0) {
				throw new TopLogicException(I18NConstants.ERROR_COMMAND_FAILED__WORKSPACE_COMMAND_CODE_OUTPUT
					.fill(_spec.getWorkspaceId(), command.toString(), exitCode, tail.toString()));
			}
			return stdout.toString();
		}

		@Override
		public int exec(ExecCommand command, ExecOutput output) throws InterruptedException {
			return _provider.exec(_spec.getWorkspaceId(), command, output);
		}

		@Override
		public void generate() throws InterruptedException {
			step("Generating application " + _spec.getGroupId() + ":" + _spec.getArtifactId() + " " + _version);
			run(generateCommand(_version, _spec.getGroupId(), _spec.getArtifactId()).setWorkingDir(SOURCE_PATH)
				.setTimeout(buildTimeout()));
		}

	}

	/**
	 * {@link ExecOutput} forwarding the output to another one and keeping its end for an error
	 * message.
	 */
	private static final class OutputTail implements ExecOutput {

		private final ExecOutput _target;

		private final StringBuilder _tail = new StringBuilder();

		OutputTail(ExecOutput target) {
			_target = target;
		}

		@Override
		public void stdout(String text) {
			append(text);
			_target.stdout(text);
		}

		@Override
		public void stderr(String text) {
			append(text);
			_target.stderr(text);
		}

		private void append(String text) {
			_tail.append(text);
			if (_tail.length() > MAX_OUTPUT_TAIL) {
				_tail.delete(0, _tail.length() - MAX_OUTPUT_TAIL);
			}
		}

		@Override
		public String toString() {
			return _tail.toString();
		}
	}

}
