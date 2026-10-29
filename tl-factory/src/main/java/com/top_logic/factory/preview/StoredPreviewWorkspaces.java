/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.preview;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.TLContextManager;

/**
 * {@link PreviewWorkspaces} of the stored workspaces of the control application.
 *
 * <p>
 * The client of a request is the user the {@link SessionService} registered for the HTTP session
 * of the request at the login to the control application. The user may see the application of a
 * workspace if the user may read the project of the workspace, checked with the rights of the user
 * by the access model of the control application: the members of the tenant of the project and
 * the operators.
 * </p>
 *
 * <p>
 * The application of a workspace is forwarded to while the workspace is
 * {@link #FORWARDED_STATES running or starting} and has a {@link FactoryModel#WORKSPACE_HOST_PORT
 * published port}, at the {@link com.top_logic.factory.workspace.WorkspaceProvider#publishedAddress()
 * address the ports are published on}, under its stored {@link FactoryModel#getContextPath(TLObject)
 * context path}.
 * </p>
 */
public class StoredPreviewWorkspaces implements PreviewWorkspaces {

	/**
	 * States of a workspace in which requests are forwarded to its application.
	 */
	public static final Set<WorkspaceStatus> FORWARDED_STATES = Set.of(WorkspaceStatus.RUNNING, WorkspaceStatus.STARTING);

	@Override
	public PreviewLookup lookup(HttpServletRequest request, String workspaceId) {
		return lookup(user(request), workspaceId);
	}

	/**
	 * The user of the control application the session of the given request is logged in for.
	 *
	 * @return The user, {@code null} if the request belongs to no logged in session.
	 */
	public static Person user(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session == null ? null : SessionService.getInstance().getUser(session.getId());
	}

	/**
	 * The application of the given workspace, if the given user may see it.
	 *
	 * @param user
	 *        The user of the control application, {@code null} if the request belongs to no
	 *        logged in session.
	 * @param workspaceId
	 *        The identifier of the workspace.
	 * @return The application to forward to, or why the request is refused.
	 */
	public PreviewLookup lookup(Person user, String workspaceId) {
		if (user == null) {
			return PreviewLookup.refused(PreviewRefusal.NOT_LOGGED_IN);
		}
		return TLContextManager.<PreviewLookup, RuntimeException, RuntimeException> inPersonContext(user, () -> {
			TLObject workspace = FactoryModel.findWorkspace(workspaceId);
			if (workspace == null) {
				return PreviewLookup.refused(PreviewRefusal.NOT_FOUND);
			}
			if (!ModelAccessRights.getInstance().isAllowed(user, FactoryModel.getProject(workspace),
				SimpleBoundCommandGroup.READ)) {
				return PreviewLookup.refused(PreviewRefusal.FORBIDDEN);
			}
			Number hostPort = (Number) workspace.tValueByName(FactoryModel.WORKSPACE_HOST_PORT);
			if (hostPort == null || !FORWARDED_STATES.contains(FactoryModel.getStatus(workspace))) {
				return PreviewLookup.refused(PreviewRefusal.NOT_RUNNING);
			}
			return PreviewLookup.found(new PreviewTarget(FactoryModel.getContextPath(workspace),
				ApplicationWorkspaces.origin(publishedAddress(), hostPort.intValue())));
		});
	}

	private static String publishedAddress() {
		return WorkspaceService.getInstance().getProvider().publishedAddress();
	}

}
