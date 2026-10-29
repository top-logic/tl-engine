/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * {@link ProjectWorkspaces} of the tests, working on the {@link ApplicationWorkspaces} a test
 * sets with {@link #setApplications(ApplicationWorkspaces)}.
 */
public class TestingProjectWorkspaces extends ProjectWorkspaces {

	private static volatile ApplicationWorkspaces _applications;

	/**
	 * Creates a {@link TestingProjectWorkspaces} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public TestingProjectWorkspaces(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Sets the workspaces the operations work on, {@code null} for those of the
	 * {@link com.top_logic.factory.workspace.WorkspaceService}.
	 */
	public static void setApplications(ApplicationWorkspaces applications) {
		_applications = applications;
	}

	@Override
	protected ApplicationWorkspaces getApplications() {
		ApplicationWorkspaces applications = _applications;
		return applications != null ? applications : super.getApplications();
	}

}
