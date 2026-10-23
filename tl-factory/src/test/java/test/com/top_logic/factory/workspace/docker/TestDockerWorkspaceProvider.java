/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace.docker;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.I18NConstants;
import com.top_logic.factory.workspace.VolumeMount;
import com.top_logic.factory.workspace.WorkspaceInfo;
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

	private WorkspaceSpec httpdSpec(String id) {
		return new WorkspaceSpec(id, IMAGE)
			.setCommand(Arrays.asList("sh", "-c",
				"echo " + PAGE_CONTENT + " > " + DATA_PATH + "/index.html && exec httpd -f -p 8080 -h " + DATA_PATH))
			.addVolume(new VolumeMount(DATA_VOLUME, DATA_PATH));
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

	private static DockerWorkspaceProvider createProvider(String suffix) {
		DockerWorkspaceProvider.Config<?> config = TypedConfiguration.newConfigItem(DockerWorkspaceProvider.Config.class);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.DOCKER_HOST, dockerHost());
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NETWORK, "tl-factory-test-" + suffix);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.NAME_PREFIX, "tl-factory-test-" + suffix);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.LABEL_NAMESPACE,
			"test.tl-factory." + suffix);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MIN, HOST_PORT_MIN);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.HOST_PORT_MAX, HOST_PORT_MAX);
		TypedConfigUtil.setProperty(config, DockerWorkspaceProvider.Config.STOP_TIMEOUT, 2);
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
	 * {@link ExecOutput} collecting the output of a command.
	 */
	private static final class CollectingOutput implements ExecOutput {

		private final StringBuilder _stdout = new StringBuilder();

		private final StringBuilder _stderr = new StringBuilder();

		@Override
		public void stdout(String text) {
			_stdout.append(text);
		}

		@Override
		public void stderr(String text) {
			_stderr.append(text);
		}

		String stdout() {
			return _stdout.toString();
		}

		String stderr() {
			return _stderr.toString();
		}

	}

}
