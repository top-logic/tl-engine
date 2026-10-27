/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.factory.ci.CIProvider;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;

/**
 * {@link ProjectWorkspaces} of the tests, working on the {@link ApplicationWorkspaces} a test
 * sets with {@link #setApplications(ApplicationWorkspaces)}, the git hosting service it sets
 * with {@link #setGitHosting(GitHostingProvider)} and the CI server it sets with
 * {@link #setCI(CIProvider)}.
 */
public class TestingProjectWorkspaces extends ProjectWorkspaces {

	private static volatile ApplicationWorkspaces _applications;

	private static volatile GitHostingProvider _gitHosting;

	private static volatile CIProvider _ci;

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

	/**
	 * Sets the git hosting service the repositories of the projects are on, {@code null} for that
	 * of the {@link com.top_logic.factory.git.GitHostingService}.
	 */
	public static void setGitHosting(GitHostingProvider gitHosting) {
		_gitHosting = gitHosting;
	}

	/**
	 * Sets the CI server building the applications of the projects, {@code null} for that of the
	 * {@link com.top_logic.factory.ci.CIService}.
	 */
	public static void setCI(CIProvider ci) {
		_ci = ci;
	}

	@Override
	protected CIProvider getCI() {
		CIProvider ci = _ci;
		return ci != null ? ci : super.getCI();
	}

	@Override
	protected GitHostingProvider getGitHosting() {
		GitHostingProvider gitHosting = _gitHosting;
		return gitHosting != null ? gitHosting : super.getGitHosting();
	}

	@Override
	protected ApplicationWorkspaces getApplications() {
		ApplicationWorkspaces applications = _applications;
		return applications != null ? applications : super.getApplications();
	}

}
