/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.docker;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.UnixDomainSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.AccessMode;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import com.top_logic.basic.col.filter.CIDRFilter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.I18NConstants;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.VolumeMount;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.docker.DockerWorkspaceProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * Integration test of {@link DockerWorkspaceProvider} against a real Docker daemon.
 *
 * <p>
 * The daemon is the one at the environment variable {@code DOCKER_HOST}, or the local socket. If
 * no daemon answers there, the suite contains no tests.
 * </p>
 *
 * <p>
 * Each run uses its own label namespace, name prefix and network, so it neither sees nor touches
 * workspaces of an application using the same daemon.
 * </p>
 */
public class TestDockerWorkspaceProvider extends TestCase {

	private static final String IMAGE = "busybox:1.37";

	private static final int HOST_PORT_MIN = 21000;

	private static final int HOST_PORT_MAX = 21999;

	private static final String DATA_VOLUME = "data";

	private static final String DATA_PATH = "/data";

	private static final String PAGE_CONTENT = "workspace-ok";

	private static final String DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";

	private static final String UNIX_SCHEME = "unix";

	private static final String LOOPBACK = "127.0.0.1";

	/**
	 * System property switching on the tests that pull images from a registry.
	 */
	private static final String INTEGRATION_PROPERTY = "tl.factory.integration";

	/**
	 * The Docker socket proxy of {@link #testExecThroughSocketProxy()}, pinned.
	 */
	private static final String SOCKET_PROXY_IMAGE = "tecnativa/docker-socket-proxy:v0.5.0";

	private static final int SOCKET_PROXY_PORT = 2375;

	/**
	 * Where the socket proxy expects the socket of the daemon.
	 */
	private static final String SOCKET_PROXY_SOCKET = "/var/run/docker.sock";

	/**
	 * The sections of the API the socket proxy lets through: those the provider uses (see the
	 * article factory/docker-server), with the requests that change something ({@code POST}).
	 */
	private static final List<String> SOCKET_PROXY_ENV = List.of("CONTAINERS=1", "EXEC=1", "IMAGES=1",
		"BUILD=1", "VOLUMES=1", "NETWORKS=1", "POST=1");

	/**
	 * Host name the containers resolve to {@link #EXTRA_HOST_ADDRESS}.
	 */
	private static final String EXTRA_HOST_NAME = "tl-factory-test-host";

	/**
	 * An address of the documentation range, never assigned.
	 */
	private static final String EXTRA_HOST_ADDRESS = "192.0.2.17";

	private static final String GATEWAY_HOST_NAME = "host.docker.internal";

	private static final String HOST_GATEWAY = "host-gateway";

	private static final Duration WAIT = Duration.ofSeconds(20);

	/**
	 * File of the images of {@link #testRecreate()} holding their variant.
	 */
	private static final String VARIANT_FILE = "/variant";

	/**
	 * Environment variable a recreated container of {@link #testRecreate()} gets.
	 */
	private static final String VARIANT_ENV = "VARIANT";

	/**
	 * Label given by the spec in {@link #testRecreate()}, beside the labels of the provider.
	 */
	private static final String EXTRA_LABEL = "test.extra";

	private static final String EXTRA_LABEL_VALUE = "kept";

	/**
	 * File whose content the main process of {@link #chattySpec(String)} prints and deletes.
	 */
	private static final String SAY_FILE = "/tmp/say";

	/**
	 * Command line matching the long-running commands of the tests, written so that it does not
	 * match the command looking for them.
	 */
	private static final String SLEEPER_PATTERN = "sleep 60[0-9]";

	/**
	 * Name of the init process Docker runs as PID 1 of a container created with init.
	 */
	private static final String INIT_PROCESS = "docker-init";

	/**
	 * Process state of a zombie in the output of {@code ps}.
	 */
	private static final String ZOMBIE_STATE = "Z";

	private String _suffix;

	private String _namePrefix;

	private String _network;

	private DockerWorkspaceProvider _provider;

	private DockerClient _docker;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_suffix = UUID.randomUUID().toString().substring(0, 8);
		_namePrefix = "tl-factory-test-" + _suffix;
		_network = "tl-factory-test-" + _suffix;
		_provider = createProvider(_suffix);
		_docker = createClient();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_provider.cleanup(Collections.emptyList());
			assertTrue("Workspaces left after cleanup.", _provider.list().isEmpty());
			assertTrue("Network left after cleanup.",
				_docker.listNetworksCmd().withNameFilter(_network).exec().isEmpty());
		} finally {
			_provider.close();
			_docker.close();
			super.tearDown();
		}
	}

	/**
	 * Runs a workspace through its lifecycle: create, start, exec, stop, start again, remove.
	 */
	public void testLifecycle() throws Exception {
		String id = "lifecycle";
		WorkspaceInfo created = _provider.create(httpdSpec(id));
		assertEquals(WorkspaceState.CREATED, created.getState());
		assertNotNull(created.getContainerId());
		assertFalse(created.hasHostPort());
		assertNull(created.getPreviewUrl());
		assertEquals(1, created.getVolumes().size());
		String volume = created.getVolumes().get(0);

		WorkspaceInfo started = _provider.start(id);
		assertEquals(WorkspaceState.RUNNING, started.getState());
		assertTrue("Host port not in range: " + started.getHostPort(),
			started.getHostPort() >= HOST_PORT_MIN && started.getHostPort() <= HOST_PORT_MAX);
		assertEquals("http://localhost:" + started.getHostPort() + "/", started.getPreviewUrl());
		assertTrue(httpGet(started.getPreviewUrl()).contains(PAGE_CONTENT));

		CollectingOutput output = new CollectingOutput();
		int exitCode = _provider.exec(id,
			ExecCommand.of("sh", "-c", "echo out; echo err >&2; cat index.html; echo persisted > marker; exit 3")
				.setWorkingDir(DATA_PATH),
			output);
		assertEquals(3, exitCode);
		assertEquals("out\n" + PAGE_CONTENT + "\n", output.stdout());
		assertEquals("err\n", output.stderr());

		CollectingOutput envOutput = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("sh", "-c", "echo $GREETING").putEnv("GREETING", "hi"),
			envOutput));
		assertEquals("hi\n", envOutput.stdout());

		WorkspaceInfo stopped = _provider.stop(id);
		assertEquals(WorkspaceState.STOPPED, stopped.getState());
		assertFalse(stopped.hasHostPort());
		assertEquals(Arrays.asList(volume), stopped.getVolumes());
		assertNotNull(_docker.inspectVolumeCmd(volume).exec());
		assertErrorKey(I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__ID.fill(id),
			() -> _provider.exec(id, ExecCommand.of("true"), ExecOutput.NONE));

		WorkspaceInfo restarted = _provider.start(id);
		assertEquals(WorkspaceState.RUNNING, restarted.getState());
		assertTrue(restarted.hasHostPort());
		assertTrue(httpGet(restarted.getPreviewUrl()).contains(PAGE_CONTENT));
		CollectingOutput markerOutput = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("cat", DATA_PATH + "/marker"), markerOutput));
		assertEquals("The volume did not survive the restart.", "persisted\n", markerOutput.stdout());

		assertEquals(Arrays.asList(id), ids(_provider.list()));

		_provider.remove(id);
		WorkspaceInfo removed = _provider.status(id);
		assertEquals(WorkspaceState.MISSING, removed.getState());
		assertTrue(removed.getVolumes().isEmpty());
		assertTrue(_provider.list().isEmpty());
		try {
			_docker.inspectVolumeCmd(volume).exec();
			fail("Volume not removed: " + volume);
		} catch (NotFoundException ex) {
			// Expected.
		}
		try {
			_docker.inspectContainerCmd(created.getContainerId()).exec();
			fail("Container not removed: " + created.getContainerId());
		} catch (NotFoundException ex) {
			// Expected.
		}
	}

	/**
	 * Creating a workspace anew on another image replaces its container and keeps its volume with
	 * its content, its labels and its ownership.
	 */
	public void testRecreate() throws Exception {
		String repository = _namePrefix + "-image";
		String imageA = _provider.buildImage(repository, variantContext("a"), ExecOutput.NONE);
		String imageB = _provider.buildImage(repository, variantContext("b"), ExecOutput.NONE);
		assertFalse(imageA.equals(imageB));
		try {
			String id = "recreate";
			WorkspaceInfo created = _provider.create(httpdSpec(id, imageA).putLabel(EXTRA_LABEL, EXTRA_LABEL_VALUE));
			assertEquals(imageA, created.getImage());
			_provider.start(id);
			assertEquals(0, _provider.exec(id, ExecCommand.of("sh", "-c", "echo persisted > " + DATA_PATH + "/marker"),
				ExecOutput.NONE));

			WorkspaceInfo recreated = _provider.recreate(
				httpdSpec(id, imageB).putLabel(EXTRA_LABEL, EXTRA_LABEL_VALUE).putEnv(VARIANT_ENV, "b"));
			assertEquals(WorkspaceState.CREATED, recreated.getState());
			assertEquals(imageB, recreated.getImage());
			assertFalse("The container is not replaced.", created.getContainerId().equals(recreated.getContainerId()));
			assertEquals(created.getVolumes(), recreated.getVolumes());
			try {
				_docker.inspectContainerCmd(created.getContainerId()).exec();
				fail("Old container not removed: " + created.getContainerId());
			} catch (NotFoundException ex) {
				// Expected.
			}
			String workspaceLabel = labelNamespace(_suffix) + "." + DockerWorkspaceProvider.LABEL_WORKSPACE;
			List<Container> containers = _docker.listContainersCmd().withShowAll(true)
				.withNameFilter(Collections.singletonList(_namePrefix)).exec();
			assertEquals("Containers: " + containers, 1, containers.size());
			Container container = containers.get(0);
			assertEquals(recreated.getContainerId(), container.getId());
			assertEquals(id, container.getLabels().get(workspaceLabel));
			assertEquals(EXTRA_LABEL_VALUE, container.getLabels().get(EXTRA_LABEL));
			for (String volume : recreated.getVolumes()) {
				assertEquals("Volume lost its owner: " + volume, id,
					_docker.inspectVolumeCmd(volume).exec().getLabels().get(workspaceLabel));
			}

			WorkspaceInfo started = _provider.start(id);
			assertEquals(WorkspaceState.RUNNING, started.getState());
			assertTrue(httpGet(started.getPreviewUrl()).contains(PAGE_CONTENT));
			CollectingOutput marker = new CollectingOutput();
			assertEquals(0, _provider.exec(id, ExecCommand.of("cat", DATA_PATH + "/marker"), marker));
			assertEquals("The volume did not survive the new container.", "persisted\n", marker.stdout());
			CollectingOutput variant = new CollectingOutput();
			assertEquals(0, _provider.exec(id, ExecCommand.of("cat", VARIANT_FILE), variant));
			assertEquals("b\n", variant.stdout());
			CollectingOutput env = new CollectingOutput();
			assertEquals(0, _provider.exec(id, ExecCommand.of("sh", "-c", "echo $" + VARIANT_ENV), env));
			assertEquals("The new environment is not applied.", "b\n", env.stdout());

			_provider.removeContainer(id);
			WorkspaceInfo containerRemoved = _provider.status(id);
			assertEquals(WorkspaceState.MISSING, containerRemoved.getState());
			assertEquals("Volumes removed with the container.", created.getVolumes(), containerRemoved.getVolumes());
			WorkspaceInfo again = _provider.recreate(httpdSpec(id, imageA));
			assertEquals("A workspace with volumes only gets a container again.", WorkspaceState.CREATED,
				again.getState());
			assertEquals(imageA, again.getImage());
		} finally {
			_provider.cleanup(Collections.emptyList());
			for (String image : Arrays.asList(imageA, imageB)) {
				try {
					_docker.removeImageCmd(image).withForce(true).exec();
				} catch (NotFoundException ex) {
					// Already removed.
				}
			}
		}
	}

	/**
	 * Operations on a workspace that does not exist report it as missing or fail with a clear
	 * error.
	 */
	public void testUnknownWorkspace() throws Exception {
		String id = "unknown";
		WorkspaceInfo status = _provider.status(id);
		assertEquals(WorkspaceState.MISSING, status.getState());
		assertNull(status.getContainerId());

		ResKey unknown = I18NConstants.ERROR_UNKNOWN_WORKSPACE__ID.fill(id);
		assertErrorKey(unknown, () -> _provider.start(id));
		assertErrorKey(unknown, () -> _provider.stop(id));
		assertErrorKey(unknown, () -> _provider.remove(id));
		assertErrorKey(unknown, () -> _provider.exec(id, ExecCommand.of("true"), ExecOutput.NONE));
		assertErrorKey(unknown, () -> _provider.recreate(httpdSpec(id)));
		// Nothing to remove.
		_provider.removeContainer(id);
	}

	/**
	 * A workspace cannot be created twice.
	 */
	public void testDuplicateWorkspace() throws Exception {
		String id = "duplicate";
		_provider.create(httpdSpec(id));
		assertErrorKey(I18NConstants.ERROR_WORKSPACE_EXISTS__ID.fill(id), () -> _provider.create(httpdSpec(id)));
	}

	/**
	 * Containers and volumes without the labels of the provider are neither listed nor cleaned up,
	 * even if their names look like the ones of the provider.
	 */
	public void testForeignObjectsIgnored() throws Exception {
		String foreignContainerName = _namePrefix + "-foreign";
		String foreignVolumeName = _namePrefix + "-foreign-" + DATA_VOLUME;
		_provider.ensureImage(IMAGE);
		String foreignContainer = _docker.createContainerCmd(IMAGE)
			.withName(foreignContainerName)
			.withCmd("sleep", "600")
			.exec()
			.getId();
		_docker.createVolumeCmd().withName(foreignVolumeName).exec();
		try {
			String id = "managed";
			_provider.create(httpdSpec(id));

			assertEquals(Arrays.asList(id), ids(_provider.list()));
			assertEquals(WorkspaceState.MISSING, _provider.status("foreign").getState());

			List<String> removed = _provider.cleanup(Collections.emptyList());
			assertEquals(Arrays.asList(id), removed);
			assertTrue(_provider.list().isEmpty());

			assertNotNull(_docker.inspectContainerCmd(foreignContainer).exec());
			assertNotNull(_docker.inspectVolumeCmd(foreignVolumeName).exec());
		} finally {
			_docker.removeContainerCmd(foreignContainer).withForce(true).exec();
			_docker.removeVolumeCmd(foreignVolumeName).exec();
		}
		List<Container> left = _docker.listContainersCmd().withShowAll(true)
			.withNameFilter(Collections.singletonList(_namePrefix)).exec();
		assertTrue("Containers left: " + left, left.isEmpty());
	}

	/**
	 * Cleanup keeps the workspaces it is told to keep.
	 */
	public void testCleanupKeeps() throws Exception {
		_provider.create(httpdSpec("keep"));
		_provider.create(httpdSpec("drop"));

		assertEquals(Arrays.asList("drop"), _provider.cleanup(Arrays.asList("keep")));
		assertEquals(Arrays.asList("keep"), ids(_provider.list()));
	}

	/**
	 * A started command reads what is written to it and ends when its input is closed.
	 */
	public void testStdinRoundTrip() throws Exception {
		String id = "stdin";
		_provider.create(httpdSpec(id));
		_provider.start(id);
		assertStdinRoundTrip(_provider, id);
	}

	/**
	 * Standard input, its end and the termination of a command work over a TCP connection to the
	 * daemon, here through a forwarder to its Unix socket.
	 */
	public void testExecOverTcp() throws Exception {
		SocketAddress daemon = unixDaemon();
		if (daemon == null) {
			return;
		}
		String id = "tcp";
		_provider.create(httpdSpec(id));
		_provider.start(id);
		try (SocketForwarder forwarder = SocketForwarder.start(daemon, null);
				DockerWorkspaceProvider provider = createProvider(_suffix, forwarder.dockerHost(), null)) {
			assertStdinRoundTrip(provider, id);
			assertTerminate(provider, id);
		}
	}

	/**
	 * Standard input, its end and the termination of a command work over a TLS connection to the
	 * daemon with client certificates, here through a TLS forwarder to its Unix socket.
	 */
	public void testExecOverTls() throws Exception {
		SocketAddress daemon = unixDaemon();
		if (daemon == null) {
			return;
		}
		String id = "tls";
		_provider.create(httpdSpec(id));
		_provider.start(id);
		Path certs = Files.createTempDirectory("tl-factory-test-certs");
		try {
			DaemonCertificates certificates = DaemonCertificates.create(certs);
			try (SocketForwarder forwarder = SocketForwarder.start(daemon, certificates.server());
					DockerWorkspaceProvider provider =
						createProvider(_suffix, forwarder.dockerHost(), certificates.clientDir().toString())) {
				assertStdinRoundTrip(provider, id);
				assertTerminate(provider, id);
			}
		} finally {
			deleteTree(certs);
		}
	}

	/**
	 * Standard input, its end and the termination of a command work through a Docker socket proxy
	 * that lets through the API calls of the provider (see the article factory/docker-server).
	 *
	 * <p>
	 * Pulls the image {@value #SOCKET_PROXY_IMAGE}, so it runs only with
	 * {@code -Dtl.factory.integration=true}.
	 * </p>
	 */
	public void testExecThroughSocketProxy() throws Exception {
		if (!Boolean.getBoolean(INTEGRATION_PROPERTY)) {
			return;
		}
		SocketAddress daemon = unixDaemon();
		if (daemon == null) {
			return;
		}
		String id = "proxy";
		_provider.create(httpdSpec(id));
		_provider.start(id);

		ensureImage(SOCKET_PROXY_IMAGE);
		String socket = ((UnixDomainSocketAddress) daemon).getPath().toString();
		ExposedPort proxyPort = ExposedPort.tcp(SOCKET_PROXY_PORT);
		String proxy = _docker.createContainerCmd(SOCKET_PROXY_IMAGE)
			.withName(_namePrefix + "-socket-proxy")
			.withEnv(SOCKET_PROXY_ENV)
			.withExposedPorts(proxyPort)
			.withHostConfig(HostConfig.newHostConfig()
				.withBinds(new Bind(socket, new Volume(SOCKET_PROXY_SOCKET), AccessMode.ro))
				.withPortBindings(new PortBinding(Ports.Binding.bindIp(LOOPBACK), proxyPort)))
			.exec().getId();
		try {
			_docker.startContainerCmd(proxy).exec();
			Ports.Binding[] bindings = _docker.inspectContainerCmd(proxy).exec()
				.getNetworkSettings().getPorts().getBindings().get(proxyPort);
			String host = "tcp://" + LOOPBACK + ":" + bindings[0].getHostPortSpec();
			try (DockerWorkspaceProvider provider = createProvider(_suffix, host, null)) {
				awaitPing(provider);
				assertStdinRoundTrip(provider, id);
				assertTerminate(provider, id);
			}
		} finally {
			_docker.removeContainerCmd(proxy).withForce(true).exec();
		}
	}

	/**
	 * Checks that a command started through the given provider reads its input and ends when its
	 * input is closed.
	 */
	private static void assertStdinRoundTrip(WorkspaceProvider provider, String id) throws Exception {
		CollectingOutput output = new CollectingOutput();
		try (ExecProcess process = provider.startExec(id, ExecCommand.of("cat"), output)) {
			process.write("hello\n");
			process.flush();
			output.awaitStdout("hello\n", WAIT);
			assertTrue(process.isAlive());

			process.write("w\u00F6rld \u2713\n".getBytes(StandardCharsets.UTF_8));
			process.closeStdin();
			assertTrue("Command did not end after its input was closed.", process.waitFor(WAIT));
			assertEquals(0, process.exitCode());
			assertEquals("hello\nw\u00F6rld \u2713\n", output.stdout());
			assertEquals("", output.stderr());

			try {
				process.write("late");
				process.flush();
				fail("Writing to a closed input must fail.");
			} catch (IOException ex) {
				// Expected.
			}
		}
	}

	/**
	 * Checks that a command started through the given provider, which waits for its input, ends
	 * when it is terminated.
	 */
	private static void assertTerminate(WorkspaceProvider provider, String id) throws Exception {
		CollectingOutput output = new CollectingOutput();
		try (ExecProcess process = provider.startExec(id, ExecCommand.of("sh", "-c", "echo started; cat"), output)) {
			output.awaitStdout("started\n", WAIT);
			assertFalse(process.waitFor(Duration.ofMillis(200)));
			process.terminate();
			assertFalse(process.isAlive());
			assertEquals(128 + 15, process.exitCode());
		}
	}

	/**
	 * The Unix socket of the daemon of the test, {@code null} if it is reached otherwise.
	 */
	private static SocketAddress unixDaemon() {
		URI host = URI.create(dockerHost());
		if (!UNIX_SCHEME.equals(host.getScheme())) {
			System.err.println("Skipping a test forwarding to the Unix socket of the daemon: " + host);
			return null;
		}
		return UnixDomainSocketAddress.of(host.getPath());
	}

	private void ensureImage(String image) throws InterruptedException {
		try {
			_docker.inspectImageCmd(image).exec();
		} catch (NotFoundException ex) {
			_docker.pullImageCmd(image).start().awaitCompletion();
		}
	}

	/**
	 * Waits until the daemon answers through the given provider, e.g. a proxy that has just
	 * started.
	 */
	private static void awaitPing(DockerWorkspaceProvider provider) throws InterruptedException {
		RuntimeException problem = null;
		for (int attempt = 0; attempt < 50; attempt++) {
			try {
				provider.list();
				return;
			} catch (RuntimeException ex) {
				problem = ex;
			}
			Thread.sleep(200);
		}
		throw problem;
	}

	private static void deleteTree(Path dir) throws IOException {
		try (Stream<Path> files = Files.walk(dir)) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(file);
			}
		}
	}

	/**
	 * Terminating a command ends it and the processes it started; a command ignoring the request
	 * is killed after the grace period.
	 */
	public void testTerminate() throws Exception {
		String id = "terminate";
		_provider.create(httpdSpec(id));
		_provider.start(id);

		CollectingOutput output = new CollectingOutput();
		try (ExecProcess process = _provider.startExec(id,
			ExecCommand.of("sh", "-c", "sleep 601 & echo started; sleep 600"), output)) {
			output.awaitStdout("started\n", WAIT);
			assertEquals(2, sleepers(id));
			assertFalse(process.waitFor(Duration.ofMillis(200)));
			assertTrue(process.isAlive());

			process.terminate();
			assertFalse(process.isAlive());
			assertEquals(128 + 15, process.exitCode());
			assertEquals("Processes left after terminate.", 0, sleepers(id));
		}

		CollectingOutput stubborn = new CollectingOutput();
		try (ExecProcess process = _provider.startExec(id,
			ExecCommand.of("sh", "-c", "trap '' TERM; sleep 602 & echo started; wait"), stubborn)) {
			stubborn.awaitStdout("started\n", WAIT);
			long start = System.nanoTime();
			process.terminate();
			Duration took = Duration.ofNanos(System.nanoTime() - start);
			assertTrue("Killed before the grace period: " + took, took.compareTo(Duration.ofSeconds(2)) >= 0);
			assertEquals(128 + 9, process.exitCode());
			assertEquals("Processes left after kill.", 0, sleepers(id));
		}
	}

	/**
	 * A blocking command that exceeds its timeout is terminated and fails.
	 */
	public void testExecTimeout() throws Exception {
		String id = "timeout";
		_provider.create(httpdSpec(id));
		_provider.start(id);

		ExecCommand command = ExecCommand.of("sleep", "603").setTimeout(Duration.ofSeconds(2));
		long start = System.nanoTime();
		assertErrorKey(I18NConstants.ERROR_COMMAND_TIMEOUT__WORKSPACE_COMMAND_SECONDS.fill(id, "sleep 603", 2L),
			() -> _provider.exec(id, command, ExecOutput.NONE));
		Duration took = Duration.ofNanos(System.nanoTime() - start);
		assertTrue("Timeout not respected: " + took, took.compareTo(Duration.ofSeconds(2)) >= 0
			&& took.compareTo(Duration.ofSeconds(15)) < 0);
		assertEquals("Command left after timeout.", 0, sleepers(id));
	}

	/**
	 * The output of the main process of a workspace is delivered, the existing and the following.
	 */
	public void testFollowLog() throws Exception {
		String id = "log";
		_provider.create(chattySpec(id));
		_provider.start(id);

		CollectingOutput all = new CollectingOutput();
		try (LogFollower follower = _provider.followLog(id, WorkspaceProvider.ALL_LINES, null, all)) {
			all.awaitStdout("started\n", WAIT);
			all.awaitStderr("complaint\n", WAIT);

			say(id, "first");
			all.awaitStdout("started\nfirst\n", WAIT);
			assertFalse(follower.awaitEnd(Duration.ZERO));

			CollectingOutput recent = new CollectingOutput();
			try (LogFollower tail = _provider.followLog(id, 1, Instant.now().minusSeconds(3600), recent)) {
				recent.awaitStdout("first\n", WAIT);
				say(id, "second");
				recent.awaitStdout("first\nsecond\n", WAIT);
			}
			all.awaitStdout("started\nfirst\nsecond\n", WAIT);

			CollectingOutput none = new CollectingOutput();
			try (LogFollower future = _provider.followLog(id, 0, null, none)) {
				say(id, "third");
				none.awaitStdout("third\n", WAIT);
			}

			_provider.stop(id);
			assertTrue("Log delivery did not end with the main process.", follower.awaitEnd(WAIT));
		}

		CollectingOutput stopped = new CollectingOutput();
		try (LogFollower follower = _provider.followLog(id, WorkspaceProvider.ALL_LINES, null, stopped)) {
			assertTrue("Log of a stopped workspace did not end.", follower.awaitEnd(WAIT));
			assertTrue(stopped.stdout(), stopped.stdout().startsWith("started\nfirst\nsecond\nthird\n"));
		}
	}

	/**
	 * The extra hosts of the provider configuration are resolved inside the workspace.
	 */
	public void testExtraHosts() throws Exception {
		String id = "hosts";
		_provider.create(httpdSpec(id));
		_provider.start(id);

		CollectingOutput hosts = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("cat", "/etc/hosts"), hosts));
		assertTrue(hosts.stdout(),
			hosts.stdout().matches("(?s).*\\n" + Pattern.quote(EXTRA_HOST_ADDRESS) + "\\s+"
				+ Pattern.quote(EXTRA_HOST_NAME) + "\\n.*"));
		assertTrue(hosts.stdout(),
			hosts.stdout().matches("(?s).*\\n[0-9a-f.:]+\\s+" + Pattern.quote(GATEWAY_HOST_NAME) + "\\n.*"));

		// The resolver of the container uses the entry: ping names the address before it tries it.
		CollectingOutput ping = new CollectingOutput();
		_provider.exec(id, ExecCommand.of("ping", "-c", "1", "-W", "1", EXTRA_HOST_NAME), ping);
		assertTrue(ping.stdout() + ping.stderr(),
			ping.stdout().contains(EXTRA_HOST_NAME + " (" + EXTRA_HOST_ADDRESS + ")"));
	}

	/**
	 * The ranges of the workspace network: none before the network exists, afterwards ranges that
	 * contain the address of a workspace container.
	 */
	public void testNetworkSubnets() throws Exception {
		assertEquals("No network yet.", Collections.emptyList(), _provider.networkSubnets());

		String id = "subnets";
		WorkspaceInfo created = _provider.create(httpdSpec(id));
		_provider.start(id);

		List<String> subnets = _provider.networkSubnets();
		assertFalse("The network has ranges.", subnets.isEmpty());
		String address = _docker.inspectContainerCmd(created.getContainerId()).exec()
			.getNetworkSettings().getNetworks().get(_network).getIpAddress();
		boolean contained = false;
		for (String subnet : subnets) {
			contained |= CIDRFilter.fromString(subnet).accept(address);
		}
		assertTrue("Address " + address + " in " + subnets, contained);
	}

	/**
	 * A workspace container runs an init process as PID 1, which reaps the orphaned processes of
	 * the commands even if the main process never waits for children.
	 */
	public void testInit() throws Exception {
		String id = "init";
		_provider.create(new WorkspaceSpec(id, IMAGE).setCommand(Arrays.asList("sleep", "3600")));
		_provider.start(id);

		CollectingOutput init = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("cat", "/proc/1/comm"), init));
		assertEquals(INIT_PROCESS + "\n", init.stdout());

		for (int n = 0; n < 3; n++) {
			assertEquals(0, _provider.exec(id, ExecCommand.of("sh", "-c", "sleep 1 & sleep 1 & exit 0"),
				ExecOutput.NONE));
		}
		// The orphans end after a second and are reparented to PID 1.
		Thread.sleep(3000);
		CollectingOutput ps = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("ps", "-o", "stat,args"), ps));
		for (String line : ps.stdout().split("\n")) {
			assertFalse("Zombie left: " + ps.stdout(), line.trim().startsWith(ZOMBIE_STATE));
		}
	}

	/**
	 * The number of processes of the tests' long-running commands in a workspace.
	 */
	private int sleepers(String id) throws InterruptedException {
		CollectingOutput ps = new CollectingOutput();
		assertEquals(0, _provider.exec(id, ExecCommand.of("ps", "-o", "args"), ps));
		int result = 0;
		for (String line : ps.stdout().split("\n")) {
			if (line.trim().matches(SLEEPER_PATTERN)) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Lets the main process of a {@link #chattySpec(String)} workspace print a line.
	 */
	private void say(String id, String line) throws InterruptedException {
		assertEquals(0, _provider.exec(id,
			ExecCommand.of("sh", "-c", "echo \"$0\" > " + SAY_FILE + ".new && mv " + SAY_FILE + ".new " + SAY_FILE,
				line),
			ExecOutput.NONE));
	}

	/**
	 * A workspace whose main process prints a line and an error line when started, and afterwards
	 * the lines given by {@link #say(String, String)}.
	 */
	private WorkspaceSpec chattySpec(String id) {
		return new WorkspaceSpec(id, IMAGE)
			.setCommand(Arrays.asList("sh", "-c",
				"echo started; echo complaint >&2; while true; do if [ -f " + SAY_FILE + " ]; then cat " + SAY_FILE
					+ "; rm " + SAY_FILE + "; fi; sleep 0.1; done"));
	}

	private WorkspaceSpec httpdSpec(String id) {
		return httpdSpec(id, IMAGE);
	}

	private WorkspaceSpec httpdSpec(String id, String image) {
		return new WorkspaceSpec(id, image)
			.setCommand(Arrays.asList("sh", "-c",
				"echo " + PAGE_CONTENT + " > " + DATA_PATH + "/index.html && exec httpd -f -p 8080 -h " + DATA_PATH))
			.addVolume(new VolumeMount(DATA_VOLUME, DATA_PATH));
	}

	/**
	 * Context of an image based on {@link #IMAGE} holding the given variant in
	 * {@link #VARIANT_FILE}.
	 */
	private static ImageContext variantContext(String variant) {
		return new ImageContext().add(ImageContext.DOCKERFILE,
			"FROM " + IMAGE + "\nRUN echo " + variant + " > " + VARIANT_FILE + "\n");
	}

	private static List<String> ids(List<WorkspaceInfo> workspaces) {
		List<String> result = new ArrayList<>();
		for (WorkspaceInfo workspace : workspaces) {
			result.add(workspace.getWorkspaceId());
		}
		Collections.sort(result);
		return result;
	}

	private static String httpGet(String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).build();
		IOException problem = null;
		// The server in the container needs a moment after the container has started.
		for (int attempt = 0; attempt < 50; attempt++) {
			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					return response.body();
				}
				problem = new IOException("Status " + response.statusCode() + " from " + url);
			} catch (IOException ex) {
				problem = ex;
			}
			Thread.sleep(200);
		}
		throw problem;
	}

	private static void assertErrorKey(ResKey expected, Operation operation) throws Exception {
		try {
			operation.run();
			fail("Expected failure with " + expected);
		} catch (TopLogicException ex) {
			assertEquals(expected.plain().getKey(), ex.getErrorKey().plain().getKey());
			assertEquals(Arrays.asList(expected.arguments()), Arrays.asList(ex.getErrorKey().arguments()));
		}
	}

	private static String dockerHost() {
		String host = System.getenv("DOCKER_HOST");
		return host == null || host.isEmpty() ? DEFAULT_DOCKER_HOST : host;
	}

	private static String labelNamespace(String suffix) {
		return "test.tl-factory." + suffix;
	}

	private static DockerWorkspaceProvider createProvider(String suffix) {
		return createProvider(suffix, dockerHost(), null);
	}

	/**
	 * A provider of the workspaces of the test with the given suffix, reaching the daemon under
	 * the given URI.
	 *
	 * @param certPath
	 *        The directory with the client certificates for TLS, {@code null} without TLS.
	 */
	private static DockerWorkspaceProvider createProvider(String suffix, String dockerHost, String certPath) {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, dockerHost);
		if (certPath != null) {
			TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.TLS_CERT_PATH, certPath);
		}
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NETWORK, "tl-factory-test-" + suffix);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NAME_PREFIX, "tl-factory-test-" + suffix);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.LABEL_NAMESPACE, labelNamespace(suffix));
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MIN, HOST_PORT_MIN);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MAX, HOST_PORT_MAX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.STOP_TIMEOUT, 2);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.TERMINATE_TIMEOUT, 2);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.EXTRA_HOSTS,
			Arrays.asList(EXTRA_HOST_NAME + ":" + EXTRA_HOST_ADDRESS, GATEWAY_HOST_NAME + ":" + HOST_GATEWAY));
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
	 * The tests of this class, none if no Docker daemon is reachable.
	 */
	public static Test suite() {
		if (!dockerAvailable()) {
			System.err.println("Skipping " + TestDockerWorkspaceProvider.class.getName()
				+ ": no Docker daemon reachable at " + dockerHost() + ".");
			return new TestSuite(TestDockerWorkspaceProvider.class.getName());
		}
		return ServiceTestSetup.createSetup(TestDockerWorkspaceProvider.class, TypeIndex.Module.INSTANCE);
	}

	/**
	 * An operation expected to fail.
	 */
	private interface Operation {

		void run() throws Exception;

	}

	/**
	 * {@link ExecOutput} collecting the output of a command, which another thread may wait for.
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
			notifyAll();
		}

		synchronized String stdout() {
			return _stdout.toString();
		}

		synchronized String stderr() {
			return _stderr.toString();
		}

		/**
		 * Waits until the standard output is the given text.
		 */
		void awaitStdout(String expected, Duration timeout) throws InterruptedException {
			await(_stdout, expected, timeout);
		}

		/**
		 * Waits until the error output is the given text.
		 */
		void awaitStderr(String expected, Duration timeout) throws InterruptedException {
			await(_stderr, expected, timeout);
		}

		private synchronized void await(StringBuilder buffer, String expected, Duration timeout)
				throws InterruptedException {
			long deadline = System.nanoTime() + timeout.toNanos();
			while (!buffer.toString().equals(expected)) {
				long left = deadline - System.nanoTime();
				if (left <= 0) {
					assertEquals("Output not received in time.", expected, buffer.toString());
				}
				wait(Math.max(1, left / 1_000_000));
			}
		}

	}

}
