/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.ci;

import com.top_logic.factory.git.GitRepository;
import com.top_logic.util.error.TopLogicException;

/**
 * Access to a CI server building the applications of the projects.
 *
 * <p>
 * Every project with a repository on the git hosting service has one job on the CI server, which
 * builds the default branch of the repository with the {@link #pipelineFile() pipeline definition}
 * in its root. The CI server clones the repository itself, with a credential of its own; the
 * control application never passes a git credential to it. The provider authenticates its requests
 * with the credentials of the control application.
 * </p>
 *
 * <p>
 * An operation that fails reports the failure as {@link TopLogicException} with one of the keys of
 * {@link I18NConstants}, e.g. {@link I18NConstants#ERROR_CI_UNREACHABLE__SERVICE_PROBLEM} for a CI
 * server that does not answer, {@link I18NConstants#ERROR_CI_AUTHENTICATION_FAILED__SERVICE} for
 * credentials it rejects, {@link I18NConstants#ERROR_CI_PERMISSION_DENIED__JOB} for an operation
 * the credentials do not permit, {@link I18NConstants#ERROR_CI_JOB_NOT_FOUND__JOB} for a job that
 * does not exist.
 * </p>
 *
 * @see CIService
 */
public interface CIProvider {

	/**
	 * The path of the pipeline definition in the repository of a project, relative to its root, e.g.
	 * {@code Jenkinsfile}: what a job runs.
	 */
	String pipelineFile();

	/**
	 * Makes sure the job of the given repository exists and builds its
	 * {@link GitRepository#getDefaultBranch() default branch} from its
	 * {@link GitRepository#getCloneUrl() clone URL} with the {@link #pipelineFile() pipeline
	 * definition}.
	 *
	 * <p>
	 * The job is named after the {@link GitRepository#getName() owner and name} of the repository.
	 * A job that does not exist is created, an existing one gets its configuration replaced, so the
	 * call can be repeated and corrects a job changed in the meantime.
	 * </p>
	 *
	 * @param repository
	 *        The repository of the project.
	 * @param description
	 *        The description of the job shown by the CI server, e.g. the name of the project;
	 *        {@code null} for none.
	 * @return The job.
	 */
	CIJob ensureJob(GitRepository repository, String description);

	/**
	 * Starts a build of the given job.
	 *
	 * <p>
	 * The CI server may keep the build in its queue until an executor is free. The provider waits a
	 * short time for the build to get its number; a build still waiting then is returned without
	 * number.
	 * </p>
	 *
	 * @param job
	 *        The {@link CIJob#name() name} of the job.
	 * @return The started build.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_CI_JOB_NOT_FOUND__JOB} if the job does not exist.
	 */
	CIBuildStart startBuild(String job);

	/**
	 * The last build of the given job, running or ended.
	 *
	 * @param job
	 *        The {@link CIJob#name() name} of the job.
	 * @return The build, {@code null} if the job has none.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_CI_JOB_NOT_FOUND__JOB} if the job does not exist.
	 */
	CIBuild lastBuild(String job);

	/**
	 * The end of the console output of a build.
	 *
	 * @param job
	 *        The {@link CIJob#name() name} of the job.
	 * @param number
	 *        The {@link CIBuild#number() number} of the build.
	 * @param maxBytes
	 *        The largest size of the returned text in bytes of its UTF-8 encoding: of a longer
	 *        output, only the last lines within this size are returned.
	 * @return The output.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD} if the job has no such
	 *         build, with {@link I18NConstants#ERROR_CI_JOB_NOT_FOUND__JOB} if the job does not
	 *         exist.
	 */
	CIConsole console(String job, int number, int maxBytes);

}
