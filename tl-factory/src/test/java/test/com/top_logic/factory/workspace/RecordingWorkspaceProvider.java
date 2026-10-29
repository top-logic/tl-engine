/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.workspace;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 * {@link WorkspaceProvider} without container platform: it records the specs of the created
 * containers, keeps the state of each workspace in memory and runs every command successfully.
 *
 * <p>
 * Images are "built" without content under the reference
 * {@link ImageContext#imageReference(String)}, shared volumes always exist. A workspace keeps its
 * volume - its identifier - when its container is removed, until the workspace is removed.
 * </p>
 */
public class RecordingWorkspaceProvider implements WorkspaceProvider {

	/**
	 * Entry of {@link #calls()} for {@link #start(String)}.
	 */
	public static final String START = "start";

	/**
	 * Entry of {@link #calls()} for {@link #stop(String)}.
	 */
	public static final String STOP = "stop";

	/**
	 * Entry of {@link #calls()} for {@link #exec(String, ExecCommand, ExecOutput)}.
	 */
	public static final String EXEC = "exec";

	private final List<WorkspaceSpec> _created = new ArrayList<>();

	private final Map<String, WorkspaceInfo> _workspaces = new LinkedHashMap<>();

	private List<String> _networkSubnets = Collections.emptyList();

	private final List<String> _calls = new ArrayList<>();

	private int _hostPort = WorkspaceInfo.NO_PORT;

	private ExecHandler _execHandler = (workspaceId, command, output) -> 0;

	/**
	 * Answers the commands run in a workspace of a {@link RecordingWorkspaceProvider}.
	 */
	public interface ExecHandler {

		/**
		 * Runs the given command, writing its output to the given output.
		 *
		 * @return The exit code.
		 */
		int exec(String workspaceId, ExecCommand command, ExecOutput output);

	}

	/**
	 * Sets the handler answering the commands, which succeed without output by default.
	 */
	public synchronized RecordingWorkspaceProvider setExecHandler(ExecHandler handler) {
		_execHandler = handler;
		return this;
	}

	/**
	 * Sets the host port the running workspaces are published on, with the preview URL
	 * {@code http://localhost:<port>/}.
	 */
	public synchronized RecordingWorkspaceProvider setHostPort(int hostPort) {
		_hostPort = hostPort;
		return this;
	}

	/**
	 * The calls of the lifecycle operations ({@code start}, {@code stop}) and the commands run,
	 * each as {@code <operation> <workspace>} or {@code exec <workspace> <command line>}, in the
	 * order of the calls.
	 */
	public synchronized List<String> calls() {
		return new ArrayList<>(_calls);
	}

	/**
	 * Sets the address ranges reported by {@link #networkSubnets()}.
	 */
	public synchronized RecordingWorkspaceProvider setNetworkSubnets(String... subnets) {
		_networkSubnets = List.of(subnets);
		return this;
	}

	/**
	 * The spec of the container last created for the given workspace.
	 */
	public WorkspaceSpec created(String workspaceId) {
		for (int n = _created.size() - 1; n >= 0; n--) {
			WorkspaceSpec spec = _created.get(n);
			if (spec.getWorkspaceId().equals(workspaceId)) {
				return spec;
			}
		}
		throw new AssertionError("Not created: " + workspaceId);
	}

	/**
	 * The specs of all containers created for the given workspace, in the order of creation.
	 */
	public List<WorkspaceSpec> allCreated(String workspaceId) {
		List<WorkspaceSpec> result = new ArrayList<>();
		for (WorkspaceSpec spec : _created) {
			if (spec.getWorkspaceId().equals(workspaceId)) {
				result.add(spec);
			}
		}
		return result;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void ensureImage(String image) {
		// Always there.
	}

	@Override
	public String buildImage(String repository, ImageContext context, ExecOutput output) {
		return context.imageReference(repository);
	}

	@Override
	public void ensureSharedVolume(String name) {
		// Always there.
	}

	@Override
	public List<String> listSharedVolumes() {
		return Collections.emptyList();
	}

	@Override
	public void removeSharedVolume(String name) {
		// Nothing to remove.
	}

	@Override
	public synchronized WorkspaceInfo create(WorkspaceSpec spec) {
		String id = spec.getWorkspaceId();
		WorkspaceInfo existing = _workspaces.get(id);
		if (existing != null && existing.getState() != WorkspaceState.MISSING) {
			throw new IllegalStateException("Workspace exists: " + id);
		}
		_created.add(spec);
		return put(spec, WorkspaceState.CREATED);
	}

	@Override
	public synchronized WorkspaceInfo start(String workspaceId) {
		_calls.add(START + " " + workspaceId);
		return put(created(workspaceId), WorkspaceState.RUNNING);
	}

	@Override
	public synchronized WorkspaceInfo stop(String workspaceId) {
		_calls.add(STOP + " " + workspaceId);
		return put(created(workspaceId), WorkspaceState.STOPPED);
	}

	@Override
	public synchronized void remove(String workspaceId) {
		_workspaces.remove(workspaceId);
	}

	@Override
	public synchronized void removeContainer(String workspaceId) {
		if (_workspaces.containsKey(workspaceId)) {
			_workspaces.put(workspaceId, WorkspaceInfo.missing(workspaceId, volumes(workspaceId)));
		}
	}

	@Override
	public synchronized WorkspaceInfo status(String workspaceId) {
		WorkspaceInfo result = _workspaces.get(workspaceId);
		return result != null ? result : WorkspaceInfo.missing(workspaceId, Collections.emptyList());
	}

	@Override
	public synchronized List<String> networkSubnets() {
		return _networkSubnets;
	}

	@Override
	public synchronized List<WorkspaceInfo> list() {
		return new ArrayList<>(_workspaces.values());
	}

	@Override
	public List<String> cleanup(Collection<String> keep) {
		return Collections.emptyList();
	}

	@Override
	public int exec(String workspaceId, ExecCommand command, ExecOutput output) {
		ExecHandler handler;
		synchronized (this) {
			_calls.add(EXEC + " " + workspaceId + " " + String.join(" ", command.getCommand()));
			handler = _execHandler;
		}
		return handler.exec(workspaceId, command, output);
	}

	@Override
	public ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output) {
		throw new UnsupportedOperationException();
	}

	@Override
	public LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		// Nothing to release.
	}

	private WorkspaceInfo put(WorkspaceSpec spec, WorkspaceState state) {
		String id = spec.getWorkspaceId();
		boolean published = state == WorkspaceState.RUNNING && _hostPort != WorkspaceInfo.NO_PORT;
		WorkspaceInfo info = new WorkspaceInfo(id, state, "container-" + id + "-" + _created.size(), spec.getImage(),
			published ? _hostPort : WorkspaceInfo.NO_PORT, volumes(id));
		_workspaces.put(id, info);
		return info;
	}

	private static List<String> volumes(String workspaceId) {
		return Collections.singletonList(workspaceId);
	}

}
