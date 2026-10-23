/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.project;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.Logger;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceState;

/**
 * Comparison of the stored states of workspaces with the containers that actually exist, as done
 * after a restart of the control application.
 *
 * <p>
 * An operation that was running when the control application stopped did not finish: a workspace
 * stored as {@link WorkspaceStatus#BUILDING}, {@link WorkspaceStatus#REMOVING} or
 * {@link WorkspaceStatus#UPDATING} is in an undefined state and becomes
 * {@link WorkspaceStatus#ERROR}; one stored as
 * {@link WorkspaceStatus#STARTING} or {@link WorkspaceStatus#STOPPING} takes the state of its
 * container. A workspace in state {@link WorkspaceStatus#ERROR} stays there, since the error is
 * only cleared by an operation on the workspace. Every other workspace takes the state of its container;
 * a workspace whose container is gone becomes {@link WorkspaceStatus#ERROR}. When the container
 * platform cannot be asked, every workspace becomes {@link WorkspaceStatus#UNKNOWN}.
 * </p>
 *
 * <p>
 * The image of an existing container is reported as well, so that a workspace knows the image it
 * runs on also when it was not recorded when the container was created.
 * </p>
 */
public class WorkspaceReconciliation {

	/**
	 * The corrected state of one workspace.
	 *
	 * @param status
	 *        The state to store.
	 * @param hostPort
	 *        The host port the application is published on, {@code null} if it is not.
	 * @param previewUrl
	 *        The URL of the application, {@code null} if it is not published.
	 * @param message
	 *        A line explaining a change to the user, {@code null} if there is nothing to explain.
	 * @param image
	 *        The image the container of the workspace was created from, {@code null} if there is no
	 *        container or the container platform could not be asked.
	 */
	public record Outcome(WorkspaceStatus status, Integer hostPort, String previewUrl, String message,
			String image) {

		/**
		 * Creates an {@link Outcome}.
		 */
		public Outcome {
			Objects.requireNonNull(status, "status");
		}
	}

	/**
	 * Function computing the URL of the application of a running workspace from what the provider
	 * reports about it.
	 */
	public interface ApplicationUrl {

		/**
		 * The URL of the application in the given running workspace.
		 */
		String of(WorkspaceInfo workspace);

	}

	private final WorkspaceProvider _provider;

	private final ApplicationUrl _applicationUrl;

	/**
	 * Creates a {@link WorkspaceReconciliation}.
	 *
	 * @param provider
	 *        The provider reporting the containers.
	 * @param applicationUrl
	 *        Computes the URL of the application of a running workspace.
	 */
	public WorkspaceReconciliation(WorkspaceProvider provider, ApplicationUrl applicationUrl) {
		_provider = provider;
		_applicationUrl = applicationUrl;
	}

	/**
	 * Computes the corrected states of the given workspaces.
	 *
	 * @param stored
	 *        The stored states by workspace identifier.
	 * @return The corrected states by workspace identifier, in the order of the given ones.
	 */
	public Map<String, Outcome> reconcile(Map<String, WorkspaceStatus> stored) {
		Map<String, WorkspaceInfo> actual;
		try {
			actual = new HashMap<>();
			for (WorkspaceInfo info : _provider.list()) {
				actual.put(info.getWorkspaceId(), info);
			}
		} catch (RuntimeException ex) {
			Logger.warn("Container platform not reachable, workspace states are unknown.", ex,
				WorkspaceReconciliation.class);
			actual = null;
		}

		Map<String, Outcome> result = new LinkedHashMap<>();
		for (Map.Entry<String, WorkspaceStatus> entry : stored.entrySet()) {
			String id = entry.getKey();
			WorkspaceInfo info;
			if (actual == null) {
				info = null;
			} else {
				info = actual.get(id);
				if (info == null) {
					info = WorkspaceInfo.missing(id, Collections.emptyList());
				}
			}
			result.put(id, reconcile(entry.getValue(), info));
		}
		return result;
	}

	/**
	 * Computes the corrected state of a single workspace.
	 *
	 * @param stored
	 *        The stored state.
	 * @param actual
	 *        What the provider reports, {@code null} if it could not be asked.
	 */
	public Outcome reconcile(WorkspaceStatus stored, WorkspaceInfo actual) {
		if (actual == null) {
			return new Outcome(WorkspaceStatus.UNKNOWN, null, null,
				"The container platform was not reachable when the control application started.", null);
		}
		switch (stored) {
			case BUILDING:
			case REMOVING:
			case UPDATING:
				return new Outcome(WorkspaceStatus.ERROR, null, null,
					"The operation was interrupted by a restart of the control application.", image(actual));
			case ERROR:
				return endpoint(WorkspaceStatus.ERROR, actual, null);
			default:
				break;
		}
		if (actual.getState() == WorkspaceState.MISSING) {
			return new Outcome(WorkspaceStatus.ERROR, null, null,
				"The container of the workspace does not exist any more.", null);
		}
		WorkspaceStatus status = WorkspaceStatus.of(actual.getState());
		return endpoint(status, actual, status == stored ? null : "The workspace is " + label(status) + ".");
	}

	private Outcome endpoint(WorkspaceStatus status, WorkspaceInfo actual, String message) {
		if (actual.getState() == WorkspaceState.RUNNING && actual.hasHostPort()) {
			return new Outcome(status, Integer.valueOf(actual.getHostPort()), _applicationUrl.of(actual), message,
				image(actual));
		}
		return new Outcome(status, null, null, message, image(actual));
	}

	private static String image(WorkspaceInfo actual) {
		return actual.getState() == WorkspaceState.MISSING ? null : actual.getImage();
	}

	private static String label(WorkspaceStatus status) {
		return status.name().toLowerCase(Locale.ROOT);
	}

}
