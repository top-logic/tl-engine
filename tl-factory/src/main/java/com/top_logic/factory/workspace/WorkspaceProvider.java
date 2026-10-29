/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import com.top_logic.util.error.TopLogicException;

/**
 * Lifecycle of workspaces: one container per managed application, with named volumes that
 * survive stopping and restarting the container.
 *
 * <p>
 * A workspace is addressed by the identifier its {@link WorkspaceSpec#getWorkspaceId() spec}
 * assigns. A provider marks every object it creates in the container platform as its own and
 * operates only on objects carrying this mark: {@link #list()} and {@link #cleanup(Collection)}
 * never touch containers, volumes or networks created by someone else, whatever their name.
 * </p>
 *
 * <p>
 * An operation that fails reports the failure as {@link TopLogicException}. An operation that
 * addresses a workspace that does not exist fails, except {@link #status(String)}, which reports
 * it as {@link WorkspaceState#MISSING}.
 * </p>
 *
 * @see WorkspaceService
 */
public interface WorkspaceProvider extends AutoCloseable {

	/**
	 * Whether the container platform answers requests.
	 */
	boolean isAvailable();

	/**
	 * Makes sure that the given image is available to create containers from, pulling it if
	 * necessary.
	 *
	 * @param image
	 *        The image reference, e.g. {@code busybox:1.37}. A reference without tag means the tag
	 *        {@code latest}.
	 */
	void ensureImage(String image) throws InterruptedException;

	/**
	 * Makes sure that an image built from the given context is available to create containers
	 * from, building it if necessary.
	 *
	 * <p>
	 * The image is tagged with the {@link ImageContext#contentHash() content hash} of the context.
	 * If an image of this tag exists, it is reused without building, so a changed context leads to
	 * a new image and an unchanged one does not.
	 * </p>
	 *
	 * @param repository
	 *        The name of the image without tag, e.g. {@code tl-factory-workspace}.
	 * @param context
	 *        The files to build the image from.
	 * @param output
	 *        Receiver of the output of the build while it runs.
	 * @return The reference of the image, the repository tagged with the content hash, see
	 *         {@link ImageContext#imageReference(String)}.
	 */
	String buildImage(String repository, ImageContext context, ExecOutput output) throws InterruptedException;

	/**
	 * Makes sure that a {@link VolumeMount#isShared() shared} volume exists, creating it empty if
	 * necessary.
	 *
	 * <p>
	 * A shared volume belongs to no workspace: neither {@link #remove(String)} nor
	 * {@link #cleanup(Collection)} removes it, only {@link #removeSharedVolume(String)} does.
	 * </p>
	 *
	 * @param name
	 *        The logical name of the volume, see {@link VolumeMount#getName()}.
	 */
	void ensureSharedVolume(String name);

	/**
	 * The logical names of all {@link VolumeMount#isShared() shared} volumes of this provider.
	 */
	List<String> listSharedVolumes();

	/**
	 * Removes a {@link VolumeMount#isShared() shared} volume.
	 *
	 * <p>
	 * Removing a volume that does not exist does nothing. A volume mounted by a workspace that
	 * still has a container cannot be removed.
	 * </p>
	 *
	 * @param name
	 *        The logical name of the volume, see {@link VolumeMount#getName()}.
	 */
	void removeSharedVolume(String name);

	/**
	 * Creates the container and the volumes of a workspace without starting it.
	 *
	 * <p>
	 * The image is pulled if it is not available. Volumes of the same workspace that remain from
	 * an earlier container are reused, so that a workspace can be created anew on its persisted
	 * state. {@link VolumeMount#isShared() Shared} volumes must exist, see
	 * {@link #ensureSharedVolume(String)}.
	 * </p>
	 *
	 * @return The workspace in state {@link WorkspaceState#CREATED}.
	 */
	WorkspaceInfo create(WorkspaceSpec spec) throws InterruptedException;

	/**
	 * Creates the container of an existing workspace anew from the given spec, keeping its
	 * volumes.
	 *
	 * <p>
	 * The container the workspace has is stopped and removed, its volumes are kept and mounted by
	 * the new container, which may use another {@link WorkspaceSpec#getImage() image} and another
	 * environment. The image is made available before the container is removed, so that a
	 * workspace whose image cannot be pulled keeps its container. A workspace that has lost its
	 * container but still has volumes gets a container again. The new container is not started.
	 * </p>
	 *
	 * @param spec
	 *        The workspace to create anew, addressed by its {@link WorkspaceSpec#getWorkspaceId()
	 *        identifier}.
	 * @return The workspace in state {@link WorkspaceState#CREATED}.
	 */
	default WorkspaceInfo recreate(WorkspaceSpec spec) throws InterruptedException {
		String workspaceId = spec.getWorkspaceId();
		WorkspaceInfo existing = status(workspaceId);
		if (existing.getState() == WorkspaceState.MISSING && existing.getVolumes().isEmpty()) {
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_WORKSPACE__ID.fill(workspaceId));
		}
		ensureImage(spec.getImage());
		removeContainer(workspaceId);
		return create(spec);
	}

	/**
	 * Starts the container of a workspace and publishes its
	 * {@link WorkspaceSpec#getContainerPort() container port} on a free port of the host.
	 *
	 * <p>
	 * Starting a running workspace does nothing. The host port may differ from the one of an
	 * earlier start.
	 * </p>
	 *
	 * @return The workspace with its {@link WorkspaceInfo#getHostPort() host port} and
	 *         {@link WorkspaceInfo#getPreviewUrl() preview URL}.
	 */
	WorkspaceInfo start(String workspaceId);

	/**
	 * Stops the container of a workspace. Its volumes are kept.
	 *
	 * <p>
	 * Stopping a workspace that is not running does nothing.
	 * </p>
	 *
	 * @return The stopped workspace.
	 */
	WorkspaceInfo stop(String workspaceId);

	/**
	 * Removes the container and all volumes of a workspace.
	 *
	 * <p>
	 * A running container is stopped first. A workspace that has lost its container but still has
	 * volumes is removed as well.
	 * </p>
	 */
	void remove(String workspaceId);

	/**
	 * Removes the container of a workspace and keeps its volumes.
	 *
	 * <p>
	 * A running container is stopped first. Removing the container of a workspace that has none
	 * does nothing.
	 * </p>
	 *
	 * @see #recreate(WorkspaceSpec)
	 */
	void removeContainer(String workspaceId);

	/**
	 * The current state of a workspace.
	 *
	 * @return The workspace, in state {@link WorkspaceState#MISSING} if it has no container.
	 */
	WorkspaceInfo status(String workspaceId);

	/**
	 * The address ranges of the network the workspace containers are attached to, as CIDR
	 * strings, e.g. {@code 172.18.0.0/16} or {@code fd00:1::/64}.
	 *
	 * <p>
	 * A request from one of these addresses comes from a workspace container (or from another
	 * container attached to that network).
	 * </p>
	 *
	 * @return The ranges, empty if the network does not exist yet or the platform does not report
	 *         them.
	 */
	List<String> networkSubnets();

	/**
	 * All workspaces of this provider, including those that have only volumes left.
	 *
	 * <p>
	 * The result describes what actually exists in the container platform. A control application
	 * reconciles the state it has stored against it, e.g. after a restart.
	 * </p>
	 */
	List<WorkspaceInfo> list();

	/**
	 * Removes all workspaces of this provider except the given ones.
	 *
	 * <p>
	 * When no workspace is left, the provider also removes the infrastructure it created for its
	 * workspaces, e.g. a network.
	 * </p>
	 *
	 * @param keep
	 *        Identifiers of the workspaces to keep.
	 * @return The identifiers of the removed workspaces.
	 */
	List<String> cleanup(Collection<String> keep);

	/**
	 * Value of the number of lines of {@link #followLog(String, int, Instant, ExecOutput)} that
	 * delivers the whole log.
	 */
	int ALL_LINES = -1;

	/**
	 * Runs a command inside the container of a running workspace and waits for it to finish.
	 *
	 * <p>
	 * The command reads an empty standard input. If it does not finish within its
	 * {@link ExecCommand#getTimeout() timeout}, it is {@link ExecProcess#terminate() terminated}
	 * and the call fails. If the waiting thread is interrupted, the command is killed.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace to run the command in.
	 * @param command
	 *        The command to run.
	 * @param output
	 *        Receiver of the output of the command while it runs.
	 * @return The exit code of the command.
	 */
	default int exec(String workspaceId, ExecCommand command, ExecOutput output) throws InterruptedException {
		try (ExecProcess process = startExec(workspaceId, command, output)) {
			try {
				process.closeStdin();
			} catch (IOException ex) {
				// The command has ended before its input was closed, its exit code tells how.
			}
			if (!process.waitFor(command.getTimeout())) {
				process.terminate();
				throw new TopLogicException(I18NConstants.ERROR_COMMAND_TIMEOUT__WORKSPACE_COMMAND_SECONDS
					.fill(workspaceId, command.toString(), command.getTimeout().toSeconds()));
			}
			return process.exitCode();
		}
	}

	/**
	 * Starts a command inside the container of a running workspace and returns without waiting
	 * for it.
	 *
	 * <p>
	 * The command reads its standard input from the returned handle, which also waits for and
	 * terminates the command. The {@link ExecCommand#getTimeout() timeout} of the command does
	 * not apply, the command runs until it ends, is terminated, or the workspace stops.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace to run the command in.
	 * @param command
	 *        The command to run.
	 * @param output
	 *        Receiver of the output of the command while it runs.
	 * @return The handle of the running command, to be closed by the caller.
	 */
	ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output);

	/**
	 * Delivers the output of the main process of a workspace: what it has written so far and,
	 * while it runs, what it writes from now on.
	 *
	 * <p>
	 * The output of a stopped workspace is the one of its last run; its delivery ends after the
	 * existing output.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace whose output is delivered.
	 * @param tailLines
	 *        The number of the last lines of the existing output to deliver, {@link #ALL_LINES} for
	 *        all of them.
	 * @param since
	 *        The time from which on existing output is delivered, {@code null} for all of it. The
	 *        precision is a second.
	 * @param output
	 *        Receiver of the output, called from a thread of the provider.
	 * @return The handle of the delivery, to be closed by the caller.
	 */
	LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output);

	/**
	 * Releases the connection to the container platform. The workspaces are not affected.
	 */
	@Override
	void close();

}
