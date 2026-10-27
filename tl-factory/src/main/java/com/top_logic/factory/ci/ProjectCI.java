/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.model.TLObject;
import com.top_logic.util.error.TopLogicException;

/**
 * The CI operations on a stored project: the build a user starts and the last build the views
 * show.
 */
public final class ProjectCI {

	private ProjectCI() {
		// Static utility.
	}

	/**
	 * Why no CI build of the given project can be started.
	 *
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 * @param hosting
	 *        The git hosting service, {@code null} if it is switched off.
	 * @param project
	 *        The project.
	 * @return The reason, {@code null} if a build can be started.
	 */
	public static ResKey unavailable(CIProvider ci, GitHostingProvider hosting, TLObject project) {
		if (ci == null) {
			return I18NConstants.ERROR_NO_CI;
		}
		if (hosting == null) {
			return com.top_logic.factory.git.I18NConstants.ERROR_NO_GIT_HOSTING;
		}
		if (project == null || FactoryModel.getRepository(project) == null) {
			return I18NConstants.ERROR_NO_REPOSITORY__PROJECT.fill(project);
		}
		return null;
	}

	/**
	 * Makes sure the CI job of the given project exists and points at its repository, and starts
	 * a build of it.
	 *
	 * @param ci
	 *        The CI server.
	 * @param hosting
	 *        The git hosting service of the repository.
	 * @param project
	 *        The project, which has a repository.
	 * @param record
	 *        Stores the job of the project if it differs from the stored one.
	 * @return The started build.
	 * @throws TopLogicException
	 *         If the project has no repository, the repository no longer exists on the hosting
	 *         service, or the CI server fails.
	 */
	public static CIBuildStart startBuild(CIProvider ci, GitHostingProvider hosting, TLObject project,
			Consumer<CIJob> record) {
		ResKey problem = unavailable(ci, hosting, project);
		if (problem != null) {
			throw new TopLogicException(problem);
		}
		RepositoryName name = FactoryModel.getRepository(project);
		GitRepository repository = hosting.findRepository(name.getFullName());
		if (repository == null) {
			throw new TopLogicException(
				com.top_logic.factory.git.I18NConstants.ERROR_UNKNOWN_REPOSITORY__REFERENCE.fill(name.getFullName()));
		}
		CIJob job = ci.ensureJob(repository, FactoryModel.getString(project, FactoryModel.PROJECT_NAME));
		if (!job.name().equals(FactoryModel.getCIJob(project))
			|| !job.webUrl().equals(FactoryModel.getString(project, FactoryModel.PROJECT_CI_JOB_URL))) {
			record.accept(job);
		}
		return ci.startBuild(job.name());
	}

	/**
	 * The message telling a user about a started build.
	 */
	public static ResKey startedMessage(CIBuildStart build, String job) {
		if (build.isQueued()) {
			return I18NConstants.BUILD_QUEUED__JOB.fill(job);
		}
		return I18NConstants.BUILD_STARTED__NUMBER_JOB.fill(build.number(), job);
	}

	/**
	 * The last build of the CI job of the given project.
	 *
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 * @param project
	 *        The project.
	 * @return The build, {@code null} if CI is switched off, the project has no CI job, or the job
	 *         has no build.
	 * @throws TopLogicException
	 *         If the CI server fails.
	 */
	public static CIBuild lastBuild(CIProvider ci, TLObject project) {
		if (ci == null || project == null) {
			return null;
		}
		String job = FactoryModel.getCIJob(project);
		if (job == null) {
			return null;
		}
		return ci.lastBuild(job);
	}

	/**
	 * The given protected files with the {@link CIProvider#pipelineFile() pipeline file} of the
	 * given CI server added, unless it is listed already.
	 *
	 * @param protectedFiles
	 *        The glob patterns of the protected files.
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 */
	public static List<String> withPipelineFile(List<String> protectedFiles, CIProvider ci) {
		if (ci == null || protectedFiles.contains(ci.pipelineFile())) {
			return protectedFiles;
		}
		List<String> result = new ArrayList<>(protectedFiles);
		result.add(ci.pipelineFile());
		return List.copyOf(result);
	}

}
