/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace.docker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.VolumeMount;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link WorkspaceProvider} running each workspace in a container of a Docker daemon, accessed
 * through the Docker Engine API.
 *
 * <p>
 * The daemon is addressed by {@link Config#getDockerHost()}, so the application may run on the
 * daemon host itself, in a container of that daemon (with access to its socket or to a socket
 * proxy) or anywhere else (with a remote daemon). Because of that, the provider only ever creates
 * named volumes and never bind-mounts a path: a host path would be resolved on the daemon host,
 * not where the application runs.
 * </p>
 *
 * <p>
 * Everything the provider creates carries labels in the {@link Config#getLabelNamespace() label
 * namespace}: containers and volumes the label {@link #LABEL_WORKSPACE} with the workspace
 * identifier, shared volumes the label {@link #LABEL_SHARED} with their logical name, built images
 * the label {@link #LABEL_IMAGE} with their repository, the network the label
 * {@link #LABEL_NETWORK}. The provider finds its objects by these labels only, never by name.
 * </p>
 *
 * <p>
 * All workspace containers join the network {@link Config#getNetwork()}, which is created if it
 * does not exist. The {@link WorkspaceSpec#getContainerPort() container port} of a workspace is
 * published on a port of the range from {@link Config#getHostPortMin()} to
 * {@link Config#getHostPortMax()}, which the daemon chooses anew on every start among the ports of
 * the range that are free at that time.
 * </p>
 *
 * <p>
 * The {@link Config#getExtraHosts() extra hosts} are added to the host names a workspace container
 * resolves, e.g. to reach the application on the daemon host through the gateway of the daemon.
 * </p>
 *
 * <p>
 * A {@link #startExec(String, ExecCommand, ExecOutput) command run in a workspace} needs a
 * {@code sh} with {@code kill} in the image: the command is run under a {@link #PID_SCRIPT shell
 * script} reporting its process ID, and terminating it sends signals to its process group by
 * {@code kill}.
 * </p>
 */
public class DockerWorkspaceProvider extends AbstractConfiguredInstance<DockerWorkspaceProvider.Config<?>>
		implements WorkspaceProvider {

	/**
	 * Configuration of {@link DockerWorkspaceProvider}.
	 */
	public interface Config<I extends DockerWorkspaceProvider> extends PolymorphicConfiguration<I> {

		/**
		 * Configuration property name of {@link #getDockerHost()}.
		 *
		 * @see #getDockerHost()
		 */
		String DOCKER_HOST = "docker-host";

		/**
		 * Configuration property name of {@link #getTlsCertPath()}.
		 *
		 * @see #getTlsCertPath()
		 */
		String TLS_CERT_PATH = "tls-cert-path";

		/**
		 * Configuration property name of {@link #getConnectTimeout()}.
		 *
		 * @see #getConnectTimeout()
		 */
		String CONNECT_TIMEOUT = "connect-timeout";

		/**
		 * Configuration property name of {@link #getNetwork()}.
		 *
		 * @see #getNetwork()
		 */
		String NETWORK = "network";

		/**
		 * Configuration property name of {@link #getPublicHost()}.
		 *
		 * @see #getPublicHost()
		 */
		String PUBLIC_HOST = "public-host";

		/**
		 * Configuration property name of {@link #getHostPortMin()}.
		 *
		 * @see #getHostPortMin()
		 */
		String HOST_PORT_MIN = "host-port-min";

		/**
		 * Configuration property name of {@link #getHostPortMax()}.
		 *
		 * @see #getHostPortMax()
		 */
		String HOST_PORT_MAX = "host-port-max";

		/**
		 * Configuration property name of {@link #getLabelNamespace()}.
		 *
		 * @see #getLabelNamespace()
		 */
		String LABEL_NAMESPACE = "label-namespace";

		/**
		 * Configuration property name of {@link #getNamePrefix()}.
		 *
		 * @see #getNamePrefix()
		 */
		String NAME_PREFIX = "name-prefix";

		/**
		 * Configuration property name of {@link #getStopTimeout()}.
		 *
		 * @see #getStopTimeout()
		 */
		String STOP_TIMEOUT = "stop-timeout";

		/**
		 * Configuration property name of {@link #getTerminateTimeout()}.
		 *
		 * @see #getTerminateTimeout()
		 */
		String TERMINATE_TIMEOUT = "terminate-timeout";

		/**
		 * Configuration property name of {@link #getExtraHosts()}.
		 *
		 * @see #getExtraHosts()
		 */
		String EXTRA_HOSTS = "extra-hosts";

		@Override
		@ClassDefault(DockerWorkspaceProvider.class)
		Class<? extends I> getImplementationClass();

		/**
		 * The URI of the Docker daemon.
		 *
		 * <p>
		 * Either the socket of a local daemon, e.g. {@code unix:///var/run/docker.sock}, or the
		 * address of a remote daemon or a socket proxy, e.g. {@code tcp://docker-proxy:2375}.
		 * </p>
		 *
		 * @see #DOCKER_HOST
		 */
		@Name(DOCKER_HOST)
		@StringDefault("unix:///var/run/docker.sock")
		String getDockerHost();

		/**
		 * Directory with the client certificate ({@code cert.pem}, {@code key.pem}) and the CA
		 * certificate ({@code ca.pem}) for a TLS connection to a {@code tcp://} daemon.
		 *
		 * <p>
		 * If not set, the connection is not encrypted.
		 * </p>
		 *
		 * @see #TLS_CERT_PATH
		 */
		@Name(TLS_CERT_PATH)
		@Label("TLS certificate directory")
		@Nullable
		String getTlsCertPath();

		/**
		 * Timeout in seconds for establishing a connection to the daemon.
		 *
		 * @see #CONNECT_TIMEOUT
		 */
		@Name(CONNECT_TIMEOUT)
		@IntDefault(30)
		int getConnectTimeout();

		/**
		 * Name of the Docker network the workspace containers join.
		 *
		 * <p>
		 * If the network does not exist, it is created. An application that itself runs in a
		 * container of the same daemon must be connected to this network to reach the workspaces by
		 * container name.
		 * </p>
		 *
		 * @see #NETWORK
		 */
		@Name(NETWORK)
		@StringDefault("tl-factory")
		String getNetwork();

		/**
		 * Host name of the daemon host under which a browser reaches the published ports.
		 *
		 * <p>
		 * The {@link WorkspaceInfo#getPreviewUrl() preview URL} of a workspace is built from this
		 * name and the host port.
		 * </p>
		 *
		 * @see #PUBLIC_HOST
		 */
		@Name(PUBLIC_HOST)
		@StringDefault("localhost")
		String getPublicHost();

		/**
		 * The lowest port of the daemon host a workspace port is published on.
		 *
		 * @see #HOST_PORT_MIN
		 */
		@Name(HOST_PORT_MIN)
		@IntDefault(20000)
		int getHostPortMin();

		/**
		 * The highest port of the daemon host a workspace port is published on.
		 *
		 * @see #HOST_PORT_MAX
		 */
		@Name(HOST_PORT_MAX)
		@IntDefault(20999)
		int getHostPortMax();

		/**
		 * Prefix of the keys of the labels by which the provider recognizes its objects.
		 *
		 * <p>
		 * Two applications that use the same daemon must use different namespaces, otherwise each
		 * one regards the workspaces of the other as its own.
		 * </p>
		 *
		 * @see #LABEL_NAMESPACE
		 */
		@Name(LABEL_NAMESPACE)
		@StringDefault("com.top-logic.factory")
		String getLabelNamespace();

		/**
		 * Prefix of the names of the containers and volumes the provider creates.
		 *
		 * <p>
		 * The name of a container is the prefix followed by the workspace identifier, the name of a
		 * volume additionally followed by the volume name. Names only have to be unique, the
		 * provider never recognizes an object by its name.
		 * </p>
		 *
		 * @see #NAME_PREFIX
		 */
		@Name(NAME_PREFIX)
		@StringDefault("tl-factory")
		String getNamePrefix();

		/**
		 * Seconds a container gets to shut down when it is stopped, before it is killed.
		 *
		 * @see #STOP_TIMEOUT
		 */
		@Name(STOP_TIMEOUT)
		@IntDefault(30)
		int getStopTimeout();

		/**
		 * Seconds a command run in a workspace gets to finish when it is terminated, before it is
		 * killed.
		 *
		 * @see #TERMINATE_TIMEOUT
		 * @see ExecProcess#terminate()
		 */
		@Name(TERMINATE_TIMEOUT)
		@IntDefault(10)
		int getTerminateTimeout();

		/**
		 * Entries added to the host names a workspace container resolves, each a host name and an
		 * address separated by a colon.
		 *
		 * <p>
		 * The address {@code host-gateway} stands for the address of the daemon host as the
		 * containers see it: {@code host.docker.internal:host-gateway} lets a workspace reach a
		 * service on the daemon host, e.g. an application that runs there directly, under the name
		 * {@code host.docker.internal}. The entries apply to containers created afterwards.
		 * </p>
		 *
		 * @see #EXTRA_HOSTS
		 */
		@Name(EXTRA_HOSTS)
		@Format(CommaSeparatedStrings.class)
		List<String> getExtraHosts();

	}

	/**
	 * Key suffix of the label carrying the workspace identifier, set on containers and volumes.
	 */
	public static final String LABEL_WORKSPACE = "workspace";

	/**
	 * Key suffix of the label carrying the logical volume name, set on volumes.
	 *
	 * @see VolumeMount#getName()
	 */
	public static final String LABEL_VOLUME = "volume";

	/**
	 * Key suffix of the label carrying the {@link WorkspaceSpec#getContainerPort() container
	 * port}, set on containers.
	 */
	public static final String LABEL_PORT = "port";

	/**
	 * Key suffix of the label carrying the network name, set on the network the provider created.
	 */
	public static final String LABEL_NETWORK = "network";

	/**
	 * Key suffix of the label carrying the logical name of a shared volume, set on shared volumes.
	 *
	 * @see VolumeMount#isShared()
	 */
	public static final String LABEL_SHARED = "shared";

	/**
	 * Key suffix of the label carrying the repository of an image the provider built, set on
	 * built images.
	 */
	public static final String LABEL_IMAGE = "image";

	/**
	 * Part of the name of a shared volume between the name prefix and the logical name.
	 */
	private static final String SHARED_NAME_PART = "shared";

	/**
	 * Separator between the repository and the tag of an image reference.
	 */
	private static final String TAG_SEPARATOR = ":";

	/**
	 * Prefix of the temporary directory an image context is written to for building.
	 */
	private static final String IMAGE_CONTEXT_DIR_PREFIX = "tl-factory-image";

	/**
	 * Separator between the label namespace and a key suffix.
	 */
	private static final String LABEL_SEPARATOR = ".";

	/**
	 * Separator between the parts of a container or volume name.
	 */
	private static final String NAME_SEPARATOR = "-";

	/**
	 * Syntax of a workspace identifier and a volume name, a subset of the syntax of Docker object
	 * names.
	 */
	private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_.-]*");

	private static final String STATE_CREATED = "created";

	private static final String STATE_RUNNING = "running";

	private static final String STATE_RESTARTING = "restarting";

	private static final String STATE_EXITED = "exited";

	private static final String LATEST_TAG = "latest";

	private static final String BRIDGE_DRIVER = "bridge";

	private static final String LABEL_FILTER = "label";

	/**
	 * Shell script a command run in a workspace is run under: it writes its process ID as first
	 * line of its standard output and replaces itself by the command, given as its arguments.
	 *
	 * @see DockerExecProcess
	 */
	static final String PID_SCRIPT = "echo $$; exec \"$@\"";

	/**
	 * Signal asking a command to terminate.
	 */
	static final String SIGNAL_TERM = "TERM";

	/**
	 * Signal killing a command.
	 */
	static final String SIGNAL_KILL = "KILL";

	private static final String SHELL = "sh";

	private static final String SHELL_COMMAND_OPTION = "-c";

	/**
	 * The user sending signals to commands, the root user by its ID, so that it needs no entry in
	 * the user database of the image.
	 */
	private static final String SIGNAL_USER = "0";

	/**
	 * Seconds to wait for a {@code kill} run in a container.
	 */
	private static final long SIGNAL_TIMEOUT_SECONDS = 30;

	private static final long EXEC_EXIT_POLL_MILLIS = 50;

	private static final int EXEC_EXIT_POLL_ATTEMPTS = 100;

	private final DockerClientConfig _clientConfig;

	private final DockerClient _client;

	/**
	 * Creates a {@link DockerWorkspaceProvider} from configuration.
	 *
	 * <p>
	 * Creating the provider does not contact the daemon.
	 * </p>
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DockerWorkspaceProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
		if (config.getHostPortMin() <= 0 || config.getHostPortMin() > config.getHostPortMax()) {
			throw new IllegalArgumentException("Invalid host port range " + config.getHostPortMin() + "-"
				+ config.getHostPortMax() + " of the Docker workspace provider.");
		}
		_clientConfig = createClientConfig(config);
		_client = createClient(config, _clientConfig);
	}

	private static DockerClientConfig createClientConfig(Config<?> config) {
		DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
			.withDockerHost(config.getDockerHost());
		String certPath = config.getTlsCertPath();
		if (certPath != null && !certPath.isEmpty()) {
			builder.withDockerTlsVerify(true).withDockerCertPath(certPath);
		} else {
			builder.withDockerTlsVerify(false);
		}
		return builder.build();
	}

	private static DockerClient createClient(Config<?> config, DockerClientConfig clientConfig) {
		ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
			.dockerHost(clientConfig.getDockerHost())
			.sslConfig(clientConfig.getSSLConfig())
			.connectionTimeout(Duration.ofSeconds(config.getConnectTimeout()))
			.build();
		return DockerClientImpl.getInstance(clientConfig, httpClient);
	}

	@Override
	public boolean isAvailable() {
		try {
			_client.pingCmd().exec();
			return true;
		} catch (RuntimeException ex) {
			Logger.info("Docker daemon at " + getConfig().getDockerHost() + " is not available: " + message(ex),
				DockerWorkspaceProvider.class);
			return false;
		}
	}

	@Override
	public void ensureImage(String image) throws InterruptedException {
		try {
			_client.inspectImageCmd(image).exec();
			return;
		} catch (NotFoundException ex) {
			// Pull below.
		} catch (RuntimeException ex) {
			throw requestFailed(ex);
		}

		String repository;
		String tag;
		int slash = image.lastIndexOf('/');
		int colon = image.lastIndexOf(':');
		if (image.indexOf('@') >= 0) {
			// A digest reference is pulled as it stands.
			repository = image;
			tag = null;
		} else if (colon > slash) {
			repository = image.substring(0, colon);
			tag = image.substring(colon + 1);
		} else {
			repository = image;
			tag = LATEST_TAG;
		}

		Logger.info("Pulling image " + image + ".", DockerWorkspaceProvider.class);
		try {
			_client.pullImageCmd(repository).withTag(tag).exec(new PullImageResultCallback()).awaitCompletion();
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_IMAGE_PULL_FAILED__IMAGE_MESSAGE.fill(image, message(ex)),
				ex);
		}
	}

	@Override
	public String buildImage(String repository, ImageContext context, ExecOutput output)
			throws InterruptedException {
		if (!context.getFiles().containsKey(ImageContext.DOCKERFILE)) {
			throw new IllegalArgumentException("The image context has no " + ImageContext.DOCKERFILE + ".");
		}
		String image = repository + TAG_SEPARATOR + context.contentHash();
		try {
			_client.inspectImageCmd(image).exec();
			return image;
		} catch (NotFoundException ex) {
			// Build below.
		} catch (RuntimeException ex) {
			throw requestFailed(ex);
		}

		Logger.info("Building image " + image + ".", DockerWorkspaceProvider.class);
		Path dir = null;
		try {
			dir = Files.createTempDirectory(IMAGE_CONTEXT_DIR_PREFIX);
			for (Map.Entry<String, byte[]> file : context.getFiles().entrySet()) {
				Path target = dir.resolve(file.getKey());
				Files.createDirectories(target.getParent());
				Files.write(target, file.getValue());
			}
			BuildCallback callback = new BuildCallback(output);
			_client.buildImageCmd()
				.withBaseDirectory(dir.toFile())
				.withDockerfile(dir.resolve(ImageContext.DOCKERFILE).toFile())
				.withTags(Collections.singleton(image))
				.withLabels(Collections.singletonMap(labelKey(LABEL_IMAGE), repository))
				.withPull(true)
				.withRemove(true)
				.exec(callback)
				.awaitCompletion();
			if (callback.getError() != null) {
				throw new TopLogicException(
					I18NConstants.ERROR_IMAGE_BUILD_FAILED__IMAGE_MESSAGE.fill(image, callback.getError()));
			}
			_client.inspectImageCmd(image).exec();
		} catch (IOException | DockerClientException | NotFoundException ex) {
			throw new TopLogicException(I18NConstants.ERROR_IMAGE_BUILD_FAILED__IMAGE_MESSAGE.fill(image, message(ex)),
				ex);
		} catch (RuntimeException ex) {
			throw requestFailed(ex);
		} finally {
			if (dir != null) {
				FileUtilities.deleteR(dir.toFile());
			}
		}
		return image;
	}

	@Override
	public void ensureSharedVolume(String name) {
		checkName(name);
		request(() -> {
			String volumeName = sharedVolumeName(name);
			try {
				InspectVolumeResponse existing = _client.inspectVolumeCmd(volumeName).exec();
				Map<String, String> labels = existing.getLabels();
				if (labels == null || !name.equals(labels.get(labelKey(LABEL_SHARED)))) {
					throw new TopLogicException(I18NConstants.ERROR_FOREIGN_VOLUME__NAME.fill(volumeName));
				}
				return null;
			} catch (NotFoundException ex) {
				// Create below.
			}
			_client.createVolumeCmd()
				.withName(volumeName)
				.withLabels(Collections.singletonMap(labelKey(LABEL_SHARED), name))
				.exec();
			Logger.info("Created shared volume " + volumeName + ".", DockerWorkspaceProvider.class);
			return null;
		});
	}

	@Override
	public List<String> listSharedVolumes() {
		return request(() -> {
			String sharedLabel = labelKey(LABEL_SHARED);
			List<String> result = new ArrayList<>();
			for (InspectVolumeResponse volume : listVolumes(sharedLabel)) {
				result.add(volume.getLabels().get(sharedLabel));
			}
			Collections.sort(result);
			return result;
		});
	}

	@Override
	public void removeSharedVolume(String name) {
		request(() -> {
			for (InspectVolumeResponse volume : findSharedVolumes(name)) {
				try {
					_client.removeVolumeCmd(volume.getName()).exec();
				} catch (NotFoundException ex) {
					// Removed concurrently.
				} catch (ConflictException ex) {
					throw new TopLogicException(I18NConstants.ERROR_SHARED_VOLUME_IN_USE__NAME.fill(name), ex);
				}
				Logger.info("Removed shared volume " + volume.getName() + ".", DockerWorkspaceProvider.class);
			}
			return null;
		});
	}

	@Override
	public WorkspaceInfo create(WorkspaceSpec spec) throws InterruptedException {
		String workspaceId = checkName(spec.getWorkspaceId());
		for (VolumeMount volume : spec.getVolumes()) {
			checkName(volume.getName());
		}

		if (request(() -> findContainer(workspaceId)) != null) {
			throw new TopLogicException(
				com.top_logic.factory.workspace.I18NConstants.ERROR_WORKSPACE_EXISTS__ID.fill(workspaceId));
		}

		ensureImage(spec.getImage());

		return request(() -> {
			ensureNetwork();

			List<Mount> mounts = new ArrayList<>();
			for (VolumeMount volume : spec.getVolumes()) {
				String volumeName = volume.isShared() ? requireSharedVolume(volume.getName()) : ensureVolume(spec, volume);
				mounts.add(new Mount()
					.withType(MountType.VOLUME)
					.withSource(volumeName)
					.withTarget(volume.getMountPath())
					.withReadOnly(volume.isReadOnly()));
			}

			Map<String, String> labels = new LinkedHashMap<>(spec.getLabels());
			labels.put(labelKey(LABEL_WORKSPACE), workspaceId);
			labels.put(labelKey(LABEL_PORT), Integer.toString(spec.getContainerPort()));

			List<String> env = new ArrayList<>();
			for (Map.Entry<String, String> entry : spec.getEnv().entrySet()) {
				env.add(entry.getKey() + "=" + entry.getValue());
			}

			ExposedPort port = ExposedPort.tcp(spec.getContainerPort());
			String containerName = containerName(workspaceId);
			CreateContainerCmd cmd = _client.createContainerCmd(spec.getImage())
				.withName(containerName)
				.withLabels(labels)
				.withEnv(env)
				.withExposedPorts(port)
				.withHostConfig(HostConfig.newHostConfig()
					.withNetworkMode(getConfig().getNetwork())
					.withMounts(mounts)
					.withExtraHosts(getConfig().getExtraHosts().toArray(new String[0]))
					.withPortBindings(new PortBinding(
						Ports.Binding.bindPortRange(getConfig().getHostPortMin(), getConfig().getHostPortMax()),
						port)));
			if (!spec.getCommand().isEmpty()) {
				cmd.withCmd(spec.getCommand());
			}

			String containerId;
			try {
				containerId = cmd.exec().getId();
			} catch (ConflictException ex) {
				throw new TopLogicException(I18NConstants.ERROR_FOREIGN_CONTAINER__NAME.fill(containerName), ex);
			}
			Logger.info("Created workspace " + workspaceId + " in container " + containerName + ".",
				DockerWorkspaceProvider.class);
			return info(workspaceId, containerId);
		});
	}

	@Override
	public WorkspaceInfo start(String workspaceId) {
		return request(() -> {
			String containerId = requireContainer(workspaceId).getId();
			try {
				_client.startContainerCmd(containerId).exec();
			} catch (NotModifiedException ex) {
				// Already running.
			}
			return info(workspaceId, containerId);
		});
	}

	@Override
	public WorkspaceInfo stop(String workspaceId) {
		return request(() -> {
			String containerId = requireContainer(workspaceId).getId();
			try {
				_client.stopContainerCmd(containerId).withTimeout(getConfig().getStopTimeout()).exec();
			} catch (NotModifiedException ex) {
				// Not running.
			}
			return info(workspaceId, containerId);
		});
	}

	@Override
	public void remove(String workspaceId) {
		request(() -> {
			Container container = findContainer(workspaceId);
			List<InspectVolumeResponse> volumes = findVolumes(workspaceId);
			if (container == null && volumes.isEmpty()) {
				throw unknownWorkspace(workspaceId);
			}
			if (container != null) {
				if (STATE_RUNNING.equals(container.getState())) {
					try {
						_client.stopContainerCmd(container.getId()).withTimeout(getConfig().getStopTimeout()).exec();
					} catch (NotModifiedException | NotFoundException ex) {
						// Stopped or removed concurrently.
					}
				}
				try {
					_client.removeContainerCmd(container.getId()).withForce(true).withRemoveVolumes(false).exec();
				} catch (NotFoundException ex) {
					// Removed concurrently.
				}
			}
			for (InspectVolumeResponse volume : volumes) {
				try {
					_client.removeVolumeCmd(volume.getName()).exec();
				} catch (NotFoundException ex) {
					// Removed concurrently.
				}
			}
			Logger.info("Removed workspace " + workspaceId + ".", DockerWorkspaceProvider.class);
			return null;
		});
	}

	@Override
	public WorkspaceInfo status(String workspaceId) {
		return request(() -> {
			Container container = findContainer(workspaceId);
			if (container == null) {
				return WorkspaceInfo.missing(workspaceId, volumeNames(findVolumes(workspaceId)));
			}
			return info(workspaceId, container.getId());
		});
	}

	@Override
	public List<WorkspaceInfo> list() {
		return request(() -> {
			String workspaceLabel = labelKey(LABEL_WORKSPACE);
			Set<String> ids = new LinkedHashSet<>();
			for (Container container : _client.listContainersCmd()
				.withShowAll(true)
				.withLabelFilter(Collections.singleton(workspaceLabel))
				.exec()) {
				ids.add(container.getLabels().get(workspaceLabel));
			}
			for (InspectVolumeResponse volume : listVolumes(workspaceLabel)) {
				ids.add(volume.getLabels().get(workspaceLabel));
			}

			List<WorkspaceInfo> result = new ArrayList<>();
			for (String id : ids) {
				result.add(status(id));
			}
			return result;
		});
	}

	@Override
	public List<String> cleanup(Collection<String> keep) {
		return request(() -> {
			List<String> removed = new ArrayList<>();
			for (WorkspaceInfo workspace : list()) {
				String id = workspace.getWorkspaceId();
				if (!keep.contains(id)) {
					remove(id);
					removed.add(id);
				}
			}
			if (list().isEmpty()) {
				removeNetworks();
			}
			return removed;
		});
	}

	@Override
	public ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output) {
		String[] execIds = request(() -> {
			Container container = requireContainer(workspaceId);
			if (!STATE_RUNNING.equals(container.getState())) {
				throw new TopLogicException(
					com.top_logic.factory.workspace.I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__ID.fill(workspaceId));
			}
			List<String> env = new ArrayList<>();
			for (Map.Entry<String, String> entry : command.getEnv().entrySet()) {
				env.add(entry.getKey() + "=" + entry.getValue());
			}
			List<String> cmd = new ArrayList<>();
			cmd.add(SHELL);
			cmd.add(SHELL_COMMAND_OPTION);
			cmd.add(PID_SCRIPT);
			// The name of the script, $0.
			cmd.add(SHELL);
			cmd.addAll(command.getCommand());
			String execId = _client.execCreateCmd(container.getId())
				.withCmd(cmd.toArray(new String[0]))
				.withAttachStdin(true)
				.withAttachStdout(true)
				.withAttachStderr(true)
				.withWorkingDir(command.getWorkingDir())
				.withUser(command.getUser())
				.withEnv(env)
				.exec()
				.getId();
			return new String[] { container.getId(), execId };
		});

		HijackedConnection connection;
		try {
			connection = HijackedConnection.startExec(_clientConfig,
				Duration.ofSeconds(getConfig().getConnectTimeout()), execIds[1]);
		} catch (IOException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_DOCKER_REQUEST_FAILED__HOST_MESSAGE.fill(getConfig().getDockerHost(), message(ex)),
				ex);
		}
		DockerExecProcess process =
			new DockerExecProcess(this, workspaceId, execIds[0], execIds[1], command, connection, output);
		process.startReading();
		return process;
	}

	/**
	 * Waits for the exit code of an exec instance whose output has ended.
	 */
	int awaitExitCode(String execId, ExecCommand command, String workspaceId) throws InterruptedException {
		for (int attempt = 0; attempt < EXEC_EXIT_POLL_ATTEMPTS; attempt++) {
			InspectExecResponse exec = request(() -> _client.inspectExecCmd(execId).exec());
			if (!Boolean.TRUE.equals(exec.isRunning()) && exec.getExitCodeLong() != null) {
				return exec.getExitCodeLong().intValue();
			}
			// The output stream may end shortly before the daemon records the exit code.
			Thread.sleep(EXEC_EXIT_POLL_MILLIS);
		}
		throw new IllegalStateException("No exit code reported for command '" + command + "' in workspace '"
			+ workspaceId + "'.");
	}

	/**
	 * Sends a signal to a process group in a container.
	 *
	 * <p>
	 * Failures are logged only: the group may have ended in the meantime.
	 * </p>
	 *
	 * @param containerId
	 *        The container the processes run in.
	 * @param processGroup
	 *        The ID of the process group, the process ID of its leader.
	 * @param signal
	 *        The name of the signal, {@link #SIGNAL_TERM} or {@link #SIGNAL_KILL}.
	 */
	void signal(String containerId, int processGroup, String signal) {
		try {
			String execId = _client.execCreateCmd(containerId)
				.withCmd(SHELL, SHELL_COMMAND_OPTION, "kill -" + signal + " -" + processGroup)
				.withUser(SIGNAL_USER)
				.withAttachStdout(true)
				.withAttachStderr(true)
				.exec()
				.getId();
			StringBuilder messages = new StringBuilder();
			FrameCallback callback = new FrameCallback(new ExecOutput() {
				@Override
				public void stdout(String text) {
					messages.append(text);
				}

				@Override
				public void stderr(String text) {
					messages.append(text);
				}
			});
			try {
				if (!_client.execStartCmd(execId).exec(callback).awaitCompletion(SIGNAL_TIMEOUT_SECONDS,
					TimeUnit.SECONDS)) {
					Logger.warn("Sending " + signal + " to process group " + processGroup + " in container "
						+ containerId + " did not finish.", DockerWorkspaceProvider.class);
				}
			} finally {
				callback.close();
			}
			if (messages.length() > 0) {
				Logger.info("Sending " + signal + " to process group " + processGroup + " in container " + containerId
					+ ": " + messages.toString().trim(), DockerWorkspaceProvider.class);
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		} catch (IOException | RuntimeException ex) {
			Logger.warn("Sending " + signal + " to process group " + processGroup + " in container " + containerId
				+ " failed: " + message(ex), DockerWorkspaceProvider.class);
		}
	}

	/**
	 * The time a terminated command gets before it is killed.
	 *
	 * @see Config#getTerminateTimeout()
	 */
	Duration getTerminateTimeout() {
		return Duration.ofSeconds(getConfig().getTerminateTimeout());
	}

	@Override
	public LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output) {
		return request(() -> {
			LogContainerCmd cmd = _client.logContainerCmd(requireContainer(workspaceId).getId())
				.withStdOut(true)
				.withStdErr(true)
				.withFollowStream(true);
			if (tailLines == ALL_LINES) {
				cmd.withTailAll();
			} else {
				cmd.withTail(tailLines);
			}
			if (since != null) {
				cmd.withSince((int) since.getEpochSecond());
			}
			FrameCallback callback = cmd.exec(new FrameCallback(output));
			return new LogFollower() {
				@Override
				public boolean awaitEnd(Duration timeout) throws InterruptedException {
					return callback.awaitEnd(timeout);
				}

				@Override
				public void close() {
					try {
						callback.close();
					} catch (IOException ex) {
						// Nothing left to release.
					}
				}
			};
		});
	}

	@Override
	public void close() {
		try {
			_client.close();
		} catch (IOException ex) {
			Logger.warn("Closing the Docker client failed.", ex, DockerWorkspaceProvider.class);
		}
	}

	private WorkspaceInfo info(String workspaceId, String containerId) {
		InspectContainerResponse container;
		try {
			container = _client.inspectContainerCmd(containerId).exec();
		} catch (NotFoundException ex) {
			// Removed concurrently.
			return WorkspaceInfo.missing(workspaceId, volumeNames(findVolumes(workspaceId)));
		}

		WorkspaceState state = state(container.getState().getStatus());
		int hostPort = WorkspaceInfo.NO_PORT;
		if (state == WorkspaceState.RUNNING) {
			hostPort = hostPort(container);
		}
		String previewUrl = hostPort == WorkspaceInfo.NO_PORT ? null
			: "http://" + getConfig().getPublicHost() + ":" + hostPort + "/";
		return new WorkspaceInfo(workspaceId, state, container.getId(), container.getConfig().getImage(), hostPort,
			previewUrl, volumeNames(findVolumes(workspaceId)));
	}

	private int hostPort(InspectContainerResponse container) {
		String portLabel = container.getConfig().getLabels().get(labelKey(LABEL_PORT));
		if (portLabel == null || container.getNetworkSettings() == null) {
			return WorkspaceInfo.NO_PORT;
		}
		Ports.Binding[] bindings =
			container.getNetworkSettings().getPorts().getBindings().get(ExposedPort.tcp(Integer.parseInt(portLabel)));
		if (bindings == null) {
			return WorkspaceInfo.NO_PORT;
		}
		for (Ports.Binding binding : bindings) {
			String spec = binding.getHostPortSpec();
			if (spec != null && !spec.isEmpty()) {
				return Integer.parseInt(spec);
			}
		}
		return WorkspaceInfo.NO_PORT;
	}

	private static WorkspaceState state(String status) {
		if (status == null) {
			return WorkspaceState.UNKNOWN;
		}
		switch (status) {
			case STATE_CREATED:
				return WorkspaceState.CREATED;
			case STATE_RUNNING:
			case STATE_RESTARTING:
				return WorkspaceState.RUNNING;
			case STATE_EXITED:
				return WorkspaceState.STOPPED;
			default:
				return WorkspaceState.UNKNOWN;
		}
	}

	private Container findContainer(String workspaceId) {
		List<Container> containers = _client.listContainersCmd()
			.withShowAll(true)
			.withLabelFilter(Collections.singletonMap(labelKey(LABEL_WORKSPACE), workspaceId))
			.exec();
		return containers.isEmpty() ? null : containers.get(0);
	}

	private Container requireContainer(String workspaceId) {
		Container container = findContainer(workspaceId);
		if (container == null) {
			throw unknownWorkspace(workspaceId);
		}
		return container;
	}

	private List<InspectVolumeResponse> findVolumes(String workspaceId) {
		return listVolumes(labelKey(LABEL_WORKSPACE) + "=" + workspaceId);
	}

	private List<InspectVolumeResponse> listVolumes(String labelFilter) {
		List<InspectVolumeResponse> volumes =
			_client.listVolumesCmd().withFilter(LABEL_FILTER, Collections.singletonList(labelFilter)).exec()
				.getVolumes();
		return volumes == null ? Collections.emptyList() : volumes;
	}

	private static List<String> volumeNames(List<InspectVolumeResponse> volumes) {
		List<String> result = new ArrayList<>();
		for (InspectVolumeResponse volume : volumes) {
			result.add(volume.getName());
		}
		Collections.sort(result);
		return result;
	}

	private List<InspectVolumeResponse> findSharedVolumes(String name) {
		return listVolumes(labelKey(LABEL_SHARED) + "=" + name);
	}

	private String requireSharedVolume(String name) {
		List<InspectVolumeResponse> volumes = findSharedVolumes(name);
		if (volumes.isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_SHARED_VOLUME__NAME.fill(name));
		}
		return volumes.get(0).getName();
	}

	private String ensureVolume(WorkspaceSpec spec, VolumeMount volume) {
		String workspaceId = spec.getWorkspaceId();
		String volumeName = volumeName(workspaceId, volume.getName());
		try {
			InspectVolumeResponse existing = _client.inspectVolumeCmd(volumeName).exec();
			Map<String, String> labels = existing.getLabels();
			if (labels == null || !workspaceId.equals(labels.get(labelKey(LABEL_WORKSPACE)))) {
				throw new TopLogicException(I18NConstants.ERROR_FOREIGN_VOLUME__NAME.fill(volumeName));
			}
			return volumeName;
		} catch (NotFoundException ex) {
			// Create below.
		}

		Map<String, String> labels = new LinkedHashMap<>(spec.getLabels());
		labels.put(labelKey(LABEL_WORKSPACE), workspaceId);
		labels.put(labelKey(LABEL_VOLUME), volume.getName());
		_client.createVolumeCmd().withName(volumeName).withLabels(labels).exec();
		return volumeName;
	}

	private void ensureNetwork() {
		String network = getConfig().getNetwork();
		for (Network existing : _client.listNetworksCmd().withNameFilter(network).exec()) {
			// The name filter matches substrings.
			if (network.equals(existing.getName())) {
				return;
			}
		}
		_client.createNetworkCmd()
			.withName(network)
			.withDriver(BRIDGE_DRIVER)
			.withLabels(Collections.singletonMap(labelKey(LABEL_NETWORK), network))
			.exec();
	}

	private void removeNetworks() {
		for (Network network : _client.listNetworksCmd()
			.withFilter(LABEL_FILTER, Collections.singletonList(labelKey(LABEL_NETWORK)))
			.exec()) {
			try {
				_client.removeNetworkCmd(network.getId()).exec();
			} catch (NotFoundException ex) {
				// Removed concurrently.
			} catch (RuntimeException ex) {
				// Still in use by containers not managed by this provider.
				Logger.info("Network " + network.getName() + " is not removed: " + message(ex),
					DockerWorkspaceProvider.class);
			}
		}
	}

	private String labelKey(String suffix) {
		return getConfig().getLabelNamespace() + LABEL_SEPARATOR + suffix;
	}

	private String containerName(String workspaceId) {
		return getConfig().getNamePrefix() + NAME_SEPARATOR + workspaceId;
	}

	private String volumeName(String workspaceId, String volume) {
		return containerName(workspaceId) + NAME_SEPARATOR + volume;
	}

	private String sharedVolumeName(String name) {
		return getConfig().getNamePrefix() + NAME_SEPARATOR + SHARED_NAME_PART + NAME_SEPARATOR + name;
	}

	private static String checkName(String name) {
		if (!NAME_PATTERN.matcher(name).matches()) {
			throw new IllegalArgumentException(
				"Invalid name '" + name + "', expected letters, digits, '_', '.' and '-'.");
		}
		return name;
	}

	private static TopLogicException unknownWorkspace(String workspaceId) {
		return new TopLogicException(
			com.top_logic.factory.workspace.I18NConstants.ERROR_UNKNOWN_WORKSPACE__ID.fill(workspaceId));
	}

	private <T> T request(Supplier<T> request) {
		try {
			return request.get();
		} catch (TopLogicException | IllegalArgumentException ex) {
			throw ex;
		} catch (RuntimeException ex) {
			throw requestFailed(ex);
		}
	}

	private TopLogicException requestFailed(RuntimeException ex) {
		if (ex instanceof TopLogicException) {
			return (TopLogicException) ex;
		}
		return new TopLogicException(
			I18NConstants.ERROR_DOCKER_REQUEST_FAILED__HOST_MESSAGE.fill(getConfig().getDockerHost(), message(ex)), ex);
	}

	private static String message(Throwable ex) {
		Throwable cause = ex;
		while (cause.getMessage() == null && cause.getCause() != null) {
			cause = cause.getCause();
		}
		return cause.getMessage() != null ? cause.getMessage() : cause.getClass().getName();
	}

	/**
	 * Forwards the output of an image build to an {@link ExecOutput} and records the error the
	 * build reports.
	 */
	private static final class BuildCallback extends ResultCallback.Adapter<BuildResponseItem> {

		private final ExecOutput _output;

		private String _error;

		BuildCallback(ExecOutput output) {
			_output = output;
		}

		@Override
		public void onNext(BuildResponseItem item) {
			String stream = item.getStream();
			if (stream != null && !stream.isEmpty()) {
				_output.stdout(stream);
			}
			if (item.isErrorIndicated()) {
				_error = item.getErrorDetail() != null && item.getErrorDetail().getMessage() != null
					? item.getErrorDetail().getMessage()
					: item.getError();
				_output.stderr(_error + "\n");
			}
		}

		/**
		 * The error the build reported, {@code null} if it succeeded.
		 */
		String getError() {
			return _error;
		}
	}

	/**
	 * Forwards frames of output to an {@link ExecOutput}.
	 *
	 * <p>
	 * After {@link #close()}, arriving frames are dropped. Unlike
	 * {@link #awaitCompletion(long, TimeUnit)}, {@link #awaitEnd(Duration)} does not close the
	 * callback when the time is up.
	 * </p>
	 */
	private static final class FrameCallback extends ResultCallback.Adapter<Frame> {

		private final ExecOutput _output;

		private final TextChunkDecoder _stdout = new TextChunkDecoder();

		private final TextChunkDecoder _stderr = new TextChunkDecoder();

		private final CountDownLatch _ended = new CountDownLatch(1);

		private volatile boolean _closed;

		FrameCallback(ExecOutput output) {
			_output = output;
		}

		@Override
		public void onNext(Frame frame) {
			if (_closed) {
				return;
			}
			switch (frame.getStreamType()) {
				case STDERR:
					emitStderr(_stderr.decode(frame.getPayload()));
					break;
				case STDOUT:
				case RAW:
					emitStdout(_stdout.decode(frame.getPayload()));
					break;
				default:
					break;
			}
		}

		@Override
		public void onComplete() {
			try {
				flush();
				super.onComplete();
			} finally {
				_ended.countDown();
			}
		}

		@Override
		public void onError(Throwable throwable) {
			try {
				flush();
				super.onError(throwable);
			} finally {
				_ended.countDown();
			}
		}

		@Override
		public void close() throws IOException {
			_closed = true;
			try {
				super.close();
			} finally {
				_ended.countDown();
			}
		}

		/**
		 * Waits until the stream has ended or the callback is closed, at most for the given time.
		 */
		boolean awaitEnd(Duration timeout) throws InterruptedException {
			return _ended.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}

		private void flush() {
			if (_closed) {
				return;
			}
			emitStdout(_stdout.finish());
			emitStderr(_stderr.finish());
		}

		private void emitStdout(String text) {
			if (!text.isEmpty()) {
				_output.stdout(text);
			}
		}

		private void emitStderr(String text) {
			if (!text.isEmpty()) {
				_output.stderr(text);
			}
		}
	}

}
