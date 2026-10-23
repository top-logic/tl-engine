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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * (the file {@link #BUILT_MARKER} exists in the source volume), and from then on runs the
 * application in the foreground on the {@link WorkspaceSpec#getContainerPort() container port}.
 * Starting and stopping the workspace therefore starts and stops the application, and its source,
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
 * </ul>
 *
 * <p>
 * The release repository of an engine version is filled once by a short-lived resolver workspace
 * that builds a throwaway application of the same archetype into it, so that building an
 * application downloads (almost) nothing. The file {@link #RELEASE_COMPLETE_MARKER} records that
 * the repository is complete; a repository without it is filled again.
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

	private static final long POLL_MILLIS = 2000;

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

	private static final int MAX_OUTPUT_TAIL = 16 * 1024;

	private static final int HTTP_OK = 200;

	private static final int HTTP_REDIRECT_MIN = 300;

	private static final int HTTP_REDIRECT_MAX = 399;

	private final Config _config;

	private final WorkspaceProvider _provider;

	private final Object _imageLock = new Object();

	private final Map<String, Object> _releaseLocks = new ConcurrentHashMap<>();

	/**
	 * Creates {@link ApplicationWorkspaces}.
	 *
	 * @param config
	 *        The configuration.
	 * @param provider
	 *        The provider the workspaces are created by.
	 */
	public ApplicationWorkspaces(Config config, WorkspaceProvider provider) {
		_config = config;
		_provider = provider;
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
	 * Creates a workspace with a TopLogic application generated from the archetype, builds the
	 * application and lets the workspace run it.
	 *
	 * <p>
	 * The method returns as soon as the application is built; the application is starting then.
	 * Use {@link #awaitApplication(String, String)} to wait until it answers. If a step fails, the
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
		String appDir = SOURCE_PATH + PATH_SEPARATOR + spec.getArtifactId();
		WorkspaceSpec workspace = new WorkspaceSpec(workspaceId, image)
			.addVolume(new VolumeMount(SOURCE_VOLUME, SOURCE_PATH))
			.addVolume(new VolumeMount(REPOSITORY_VOLUME, REPOSITORY_PATH))
			.addVolume(VolumeMount.shared(releaseVolume, RELEASE_PATH, true))
			.putEnv(ENV_MAVEN_ARGS, String.join(" ", workspaceMavenArgs()))
			.putEnv(ENV_APP_DIR, appDir)
			.putEnv(ENV_BUILT_MARKER, builtMarker())
			.putEnv(ENV_PORT, Integer.toString(WorkspaceSpec.DEFAULT_CONTAINER_PORT))
			.putEnv(ENV_BROWSER, BROWSER_NONE);
		String initialPassword = spec.getInitialPassword();
		if (initialPassword != null) {
			workspace.putEnv(ENV_INITIAL_PASSWORD, initialPassword);
		}
		_provider.create(workspace);
		_provider.start(workspaceId);

		step(progress, "Generating application " + spec.getGroupId() + ":" + spec.getArtifactId() + " " + version);
		run(workspaceId, generateCommand(version, spec.getGroupId(), spec.getArtifactId()).setWorkingDir(SOURCE_PATH)
			.setTimeout(buildTimeout()), progress);

		step(progress, "Building application");
		run(workspaceId, ExecCommand.of("mvn", "-B", "install").setWorkingDir(appDir).setTimeout(buildTimeout()),
			progress);

		run(workspaceId, ExecCommand.of("touch", builtMarker()), progress);
		step(progress, "Application built, starting it");
		return _provider.status(workspaceId);
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
	 * <p>
	 * The application answers when a request of its context path is answered with status 200 or a
	 * redirect, e.g. to the login page. The wait fails if the workspace stops or the application
	 * does not answer within {@link Config#getStartTimeout()}.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace whose application is waited for.
	 * @param contextPath
	 *        The context path of the application, its {@link ApplicationSpec#getArtifactId()
	 *        artifact}.
	 * @return The URL of the application.
	 */
	public String awaitApplication(String workspaceId, String contextPath) throws InterruptedException {
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
			if (System.currentTimeMillis() >= deadline) {
				throw new TopLogicException(I18NConstants.ERROR_APPLICATION_NOT_REACHABLE__WORKSPACE_URL_SECONDS
					.fill(workspaceId, url, _config.getStartTimeout()));
			}
			Thread.sleep(POLL_MILLIS);
		}
	}

	/**
	 * The URL of the application with the given context path in a running workspace.
	 */
	public static String applicationUrl(WorkspaceInfo workspace, String contextPath) {
		String previewUrl = workspace.getPreviewUrl();
		if (previewUrl == null) {
			throw new IllegalArgumentException("Workspace has no published port: " + workspace);
		}
		return (previewUrl.endsWith(PATH_SEPARATOR) ? previewUrl : previewUrl + PATH_SEPARATOR) + contextPath
			+ PATH_SEPARATOR;
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
			return _provider.buildImage(_config.getImageRepository(), imageContext(_config.getRepositoryUrl()),
				progress);
		}
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
			_provider.create(new WorkspaceSpec(resolverId, image)
				.addVolume(VolumeMount.shared(volume, RELEASE_PATH, false))
				.putEnv(ENV_MAVEN_ARGS, "-Dmaven.repo.local=" + RELEASE_PATH)
				// There is no application in the resolver, the entry script only keeps it alive.
				.putEnv(ENV_BUILT_MARKER, builtMarker())
				.putEnv(ENV_APP_DIR, SOURCE_PATH)
				.putEnv(ENV_PORT, Integer.toString(WorkspaceSpec.DEFAULT_CONTAINER_PORT)));
			try {
				_provider.start(resolverId);
				String marker = RELEASE_PATH + PATH_SEPARATOR + RELEASE_COMPLETE_MARKER;
				if (_provider.exec(resolverId, ExecCommand.of("test", "-f", marker), ExecOutput.NONE) == 0) {
					return volume;
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
		return new ImageContext()
			.add(ImageContext.DOCKERFILE, resource(ImageContext.DOCKERFILE))
			.add(ENTRY_SCRIPT, resource(ENTRY_SCRIPT))
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
