/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.factory.project.WorkspaceReconciliation;
import com.top_logic.factory.project.WorkspaceReconciliation.Outcome;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;

/**
 * Test of {@link WorkspaceReconciliation} against a {@link WorkspaceProvider} reporting a fixed set
 * of containers.
 */
public class TestWorkspaceReconciliation extends TestCase {

	private static final String URL_SUFFIX = "app/";

	private static final String IMAGE = "tl-factory-workspace:0123456789abcdef";

	/**
	 * A workspace that runs is stored as running with the address of its application, one that is
	 * stopped as stopped without address.
	 */
	public void testContainerStates() {
		FakeProvider provider = new FakeProvider()
			.add(running("a", 20001))
			.add(info("b", WorkspaceState.STOPPED))
			.add(info("c", WorkspaceState.CREATED))
			.add(info("d", WorkspaceState.UNKNOWN));

		Map<String, Outcome> result = reconcile(provider,
			"a", WorkspaceStatus.STOPPED,
			"b", WorkspaceStatus.RUNNING,
			"c", WorkspaceStatus.CREATED,
			"d", WorkspaceStatus.RUNNING);

		Outcome a = result.get("a");
		assertEquals(WorkspaceStatus.RUNNING, a.status());
		assertEquals(Integer.valueOf(20001), a.hostPort());
		assertEquals("http://localhost:20001/" + URL_SUFFIX, a.previewUrl());
		assertNotNull("A changed state is explained.", a.message());

		Outcome b = result.get("b");
		assertEquals(WorkspaceStatus.STOPPED, b.status());
		assertNull(b.hostPort());
		assertNull(b.previewUrl());

		Outcome c = result.get("c");
		assertEquals(WorkspaceStatus.CREATED, c.status());
		assertNull("An unchanged state needs no explanation.", c.message());

		assertEquals(WorkspaceStatus.UNKNOWN, result.get("d").status());
	}

	/**
	 * The image of an existing container is reported, so that a workspace whose image was not
	 * recorded learns it; a workspace without container and one whose platform cannot be asked
	 * report none.
	 */
	public void testImage() {
		FakeProvider provider = new FakeProvider()
			.add(running("a", 20006))
			.add(info("b", WorkspaceState.STOPPED))
			.add(WorkspaceInfo.missing("c", Collections.emptyList()));

		Map<String, Outcome> result = reconcile(provider,
			"a", WorkspaceStatus.RUNNING,
			"b", WorkspaceStatus.BUILDING,
			"c", WorkspaceStatus.STOPPED);

		assertEquals(IMAGE, result.get("a").image());
		assertEquals("Also after an interrupted operation.", IMAGE, result.get("b").image());
		assertNull(result.get("c").image());

		Map<String, Outcome> unknown = reconcile(new FakeProvider().setReachable(false),
			"a", WorkspaceStatus.RUNNING);
		assertNull(unknown.get("a").image());
	}

	/**
	 * A workspace whose container is gone, also one that still has volumes, is an error.
	 */
	public void testMissingContainer() {
		FakeProvider provider = new FakeProvider()
			.add(WorkspaceInfo.missing("b", Collections.singletonList("tl-factory-b-source")));

		Map<String, Outcome> result = reconcile(provider,
			"a", WorkspaceStatus.RUNNING,
			"b", WorkspaceStatus.STOPPED);

		assertEquals(WorkspaceStatus.ERROR, result.get("a").status());
		assertNotNull(result.get("a").message());
		assertEquals(WorkspaceStatus.ERROR, result.get("b").status());
	}

	/**
	 * An operation interrupted by the restart leaves a building or removed workspace in an error,
	 * while a starting or stopping one takes the state of its container.
	 */
	public void testInterruptedOperations() {
		FakeProvider provider = new FakeProvider()
			.add(running("building", 20002))
			.add(info("removing", WorkspaceState.STOPPED))
			.add(running("starting", 20003))
			.add(info("stopping", WorkspaceState.STOPPED));

		Map<String, Outcome> result = reconcile(provider,
			"building", WorkspaceStatus.BUILDING,
			"removing", WorkspaceStatus.REMOVING,
			"starting", WorkspaceStatus.STARTING,
			"stopping", WorkspaceStatus.STOPPING);

		assertEquals(WorkspaceStatus.ERROR, result.get("building").status());
		assertNull(result.get("building").previewUrl());
		assertEquals(WorkspaceStatus.ERROR, result.get("removing").status());
		assertEquals(WorkspaceStatus.RUNNING, result.get("starting").status());
		assertEquals(Integer.valueOf(20003), result.get("starting").hostPort());
		assertEquals(WorkspaceStatus.STOPPED, result.get("stopping").status());
	}

	/**
	 * An error is kept until the workspace is removed; its application is still reachable while
	 * the container runs.
	 */
	public void testErrorIsKept() {
		FakeProvider provider = new FakeProvider()
			.add(running("a", 20004))
			.add(info("b", WorkspaceState.STOPPED));

		Map<String, Outcome> result = reconcile(provider,
			"a", WorkspaceStatus.ERROR,
			"b", WorkspaceStatus.ERROR);

		assertEquals(WorkspaceStatus.ERROR, result.get("a").status());
		assertEquals(Integer.valueOf(20004), result.get("a").hostPort());
		assertNull(result.get("a").message());
		assertEquals(WorkspaceStatus.ERROR, result.get("b").status());
		assertNull(result.get("b").hostPort());
	}

	/**
	 * When the container platform cannot be asked, every workspace is unknown, and the order of
	 * the stored workspaces is kept.
	 */
	public void testPlatformNotReachable() {
		FakeProvider provider = new FakeProvider().setReachable(false);

		Map<String, Outcome> result = reconcile(provider,
			"a", WorkspaceStatus.RUNNING,
			"b", WorkspaceStatus.BUILDING,
			"c", WorkspaceStatus.ERROR);

		assertEquals(List.of("a", "b", "c"), new ArrayList<>(result.keySet()));
		for (Outcome outcome : result.values()) {
			assertEquals(WorkspaceStatus.UNKNOWN, outcome.status());
			assertNull(outcome.hostPort());
			assertNotNull(outcome.message());
		}
	}

	/**
	 * Containers of workspaces that are not stored are not reported.
	 */
	public void testForeignWorkspacesIgnored() {
		FakeProvider provider = new FakeProvider()
			.add(running("a", 20005))
			.add(running("resolve-release-8.0.0", 20006));

		Map<String, Outcome> result = reconcile(provider, "a", WorkspaceStatus.RUNNING);

		assertEquals(List.of("a"), new ArrayList<>(result.keySet()));
	}

	private static Map<String, Outcome> reconcile(WorkspaceProvider provider, Object... idsAndStates) {
		Map<String, WorkspaceStatus> stored = new LinkedHashMap<>();
		for (int n = 0; n < idsAndStates.length; n += 2) {
			stored.put((String) idsAndStates[n], (WorkspaceStatus) idsAndStates[n + 1]);
		}
		return new WorkspaceReconciliation(provider, info -> info.getPreviewUrl() + URL_SUFFIX).reconcile(stored);
	}

	private static WorkspaceInfo running(String id, int port) {
		return new WorkspaceInfo(id, WorkspaceState.RUNNING, "container-" + id, IMAGE, port,
			"http://localhost:" + port + "/", Collections.emptyList());
	}

	private static WorkspaceInfo info(String id, WorkspaceState state) {
		return new WorkspaceInfo(id, state, "container-" + id, IMAGE, WorkspaceInfo.NO_PORT, null,
			Collections.emptyList());
	}

	/**
	 * {@link WorkspaceProvider} reporting a fixed set of workspaces; it supports no operation.
	 */
	private static final class FakeProvider implements WorkspaceProvider {

		private final List<WorkspaceInfo> _workspaces = new ArrayList<>();

		private boolean _reachable = true;

		FakeProvider add(WorkspaceInfo workspace) {
			_workspaces.add(workspace);
			return this;
		}

		FakeProvider setReachable(boolean reachable) {
			_reachable = reachable;
			return this;
		}

		@Override
		public boolean isAvailable() {
			return _reachable;
		}

		@Override
		public List<String> networkSubnets() {
			return Collections.emptyList();
		}

		@Override
		public List<WorkspaceInfo> list() {
			if (!_reachable) {
				throw new IllegalStateException("Container platform not reachable.");
			}
			return new ArrayList<>(_workspaces);
		}

		@Override
		public WorkspaceInfo status(String workspaceId) {
			for (WorkspaceInfo workspace : list()) {
				if (workspace.getWorkspaceId().equals(workspaceId)) {
					return workspace;
				}
			}
			return WorkspaceInfo.missing(workspaceId, Collections.emptyList());
		}

		@Override
		public void ensureImage(String image) {
			throw unsupported();
		}

		@Override
		public String buildImage(String repository, ImageContext context, ExecOutput output) {
			throw unsupported();
		}

		@Override
		public void ensureSharedVolume(String name) {
			throw unsupported();
		}

		@Override
		public List<String> listSharedVolumes() {
			throw unsupported();
		}

		@Override
		public void removeSharedVolume(String name) {
			throw unsupported();
		}

		@Override
		public WorkspaceInfo create(WorkspaceSpec spec) {
			throw unsupported();
		}

		@Override
		public WorkspaceInfo start(String workspaceId) {
			throw unsupported();
		}

		@Override
		public WorkspaceInfo stop(String workspaceId) {
			throw unsupported();
		}

		@Override
		public void remove(String workspaceId) {
			throw unsupported();
		}

		@Override
		public void removeContainer(String workspaceId) {
			throw unsupported();
		}

		@Override
		public List<String> cleanup(Collection<String> keep) {
			throw unsupported();
		}

		@Override
		public ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output) {
			throw unsupported();
		}

		@Override
		public LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output) {
			throw unsupported();
		}

		@Override
		public void close() {
			// Nothing to release.
		}

		private static UnsupportedOperationException unsupported() {
			return new UnsupportedOperationException("Not supported by the fake provider.");
		}
	}

}
