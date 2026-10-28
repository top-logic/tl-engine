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
 * The CI operations on a stored project: setting up its CI, the build a user starts and the last
 * build the views show.
 *
 * <p>
 * A project gets no CI with its repository: developing the application needs none. Its CI is set
 * up on request (see {@link #setUp(CIProvider, GitHostingProvider, GitRepository, String, List,
 * String, Consumer, Consumer)}): the pipeline definition is committed to the default branch of the
 * repository unless it has one, the protection of the branch is extended by the pipeline file, the
 * CI job is created and builds the application. From then on, the project has a
 * {@link FactoryModel#PROJECT_CI_JOB CI job}, and builds are started for it.
 * </p>
 */
public final class ProjectCI {

	private ProjectCI() {
		// Static utility.
	}

	/**
	 * Message of the commit adding the pipeline definition to a repository, followed by the path
	 * of the {@link CIProvider#pipelineFile() pipeline file}.
	 */
	public static final String PIPELINE_COMMIT_MESSAGE = "Add the CI pipeline ";

	/**
	 * Why no CI build of the given project can be started.
	 *
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 * @param hosting
	 *        The git hosting service, {@code null} if it is switched off.
	 * @param project
	 *        The project.
	 * @return The reason, {@code null} if a build can be started: the project has a repository and
	 *         a CI job.
	 */
	public static ResKey unavailable(CIProvider ci, GitHostingProvider hosting, TLObject project) {
		ResKey problem = repositoryUnavailable(ci, hosting, project);
		if (problem != null) {
			return problem;
		}
		if (FactoryModel.getCIJob(project) == null) {
			return I18NConstants.ERROR_NO_CI_JOB__PROJECT.fill(project);
		}
		return null;
	}

	/**
	 * Why the CI of the given project cannot be set up.
	 *
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 * @param hosting
	 *        The git hosting service, {@code null} if it is switched off.
	 * @param project
	 *        The project.
	 * @return The reason, {@code null} if the CI can be set up: the project has a repository with
	 *         the application in it - a workspace of the project pushed or cloned it (see
	 *         {@link FactoryModel#PROJECT_BRANCH_PROTECTED}) - and no CI job yet.
	 */
	public static ResKey setUpUnavailable(CIProvider ci, GitHostingProvider hosting, TLObject project) {
		ResKey problem = repositoryUnavailable(ci, hosting, project);
		if (problem != null) {
			return problem;
		}
		if (project.tValueByName(FactoryModel.PROJECT_BRANCH_PROTECTED) == null) {
			return I18NConstants.ERROR_NO_SOURCES__PROJECT.fill(project);
		}
		if (FactoryModel.getCIJob(project) != null) {
			return I18NConstants.ERROR_HAS_CI_JOB__PROJECT.fill(project);
		}
		return null;
	}

	private static ResKey repositoryUnavailable(CIProvider ci, GitHostingProvider hosting, TLObject project) {
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
	 * Sets up the CI of a repository.
	 *
	 * <ol>
	 * <li>The {@link CIProvider#pipelineFile() pipeline file} with the given definition is
	 * {@link GitHostingProvider#commitFile(GitRepository, String, String, String, String) committed}
	 * to the default branch, unless the branch has one, e.g. a repository adopted with its own.</li>
	 * <li>If the credentials of the hosting service may {@link GitRepository#canAdminister()
	 * administer} the repository, the default branch is
	 * {@link GitHostingProvider#protectBranch(GitRepository, String, List) protected} with the given
	 * protected files {@link #withPipelineFile(List, CIProvider) and the pipeline file}; otherwise
	 * the protection stays as it is.</li>
	 * <li>The {@link CIProvider#ensureJob(GitRepository, String) job} of the repository is set up
	 * and recorded, and its first build started.</li>
	 * </ol>
	 *
	 * <p>
	 * A step that fails ends the set up with its exception. Each step can be repeated: an existing
	 * pipeline file is kept, the protection is replaced, an existing job is reused. So a set up that
	 * failed is completed by calling it again.
	 * </p>
	 *
	 * @param ci
	 *        The CI server.
	 * @param hosting
	 *        The git hosting service of the repository.
	 * @param repository
	 *        The repository, whose default branch has the application.
	 * @param pipeline
	 *        The pipeline definition the repository gets if it has none, see
	 *        {@link CIService#pipelineTemplate()}.
	 * @param protectedFiles
	 *        The protected files of the default branch without CI, see
	 *        {@link com.top_logic.factory.git.GitHostingService#getProtectedFiles()}.
	 * @param description
	 *        The description of the job, e.g. the name of the project; {@code null} for none.
	 * @param recordJob
	 *        Stores the job of the project, called once it is set up.
	 * @param recordProtection
	 *        Stores that the default branch of the repository is protected, called once its
	 *        protection includes the pipeline file.
	 * @return What was done.
	 * @throws TopLogicException
	 *         If the hosting service or the CI server fails.
	 */
	public static Setup setUp(CIProvider ci, GitHostingProvider hosting, GitRepository repository, String pipeline,
			List<String> protectedFiles, String description, Consumer<CIJob> recordJob,
			Consumer<Boolean> recordProtection) {
		String branch = repository.getDefaultBranch();
		String file = ci.pipelineFile();
		boolean added = hosting.commitFile(repository, branch, file, pipeline, PIPELINE_COMMIT_MESSAGE + file);
		boolean protectedFile = repository.canAdminister();
		if (protectedFile) {
			hosting.protectBranch(repository, branch, withPipelineFile(protectedFiles, ci));
			recordProtection.accept(Boolean.TRUE);
		}
		CIJob job = ci.ensureJob(repository, description);
		recordJob.accept(job);
		CIBuildStart build = ci.startBuild(job.name());
		return new Setup(file, added, protectedFile, job, build);
	}

	/**
	 * What {@link ProjectCI#setUp(CIProvider, GitHostingProvider, GitRepository, String, List,
	 * String, Consumer, Consumer) setting up the CI} of a repository did.
	 *
	 * @param pipelineFile
	 *        The path of the pipeline file in the repository.
	 * @param pipelineAdded
	 *        Whether the pipeline file was committed; {@code false} if the repository had one.
	 * @param pipelineProtected
	 *        Whether the default branch is protected with the pipeline file; {@code false} if the
	 *        credentials of the hosting service may not administer the repository.
	 * @param job
	 *        The CI job of the repository.
	 * @param build
	 *        The first build of the job.
	 */
	public record Setup(String pipelineFile, boolean pipelineAdded, boolean pipelineProtected, CIJob job,
			CIBuildStart build) {

		/**
		 * The message telling a user about the set up CI.
		 */
		public ResKey message() {
			ResKey started = startedMessage(build, job.name());
			if (pipelineProtected) {
				return I18NConstants.CI_SET_UP__JOB_BUILD.fill(job.name(), started);
			}
			return I18NConstants.CI_SET_UP_UNPROTECTED__JOB_BUILD_FILE.fill(job.name(), started, pipelineFile);
		}

	}

	/**
	 * Makes sure the CI job of the given project, which has one, exists and points at its
	 * repository, and starts a build of it.
	 *
	 * @param ci
	 *        The CI server.
	 * @param hosting
	 *        The git hosting service of the repository.
	 * @param project
	 *        The project, which has a repository and a CI job.
	 * @param record
	 *        Stores the job of the project if it differs from the stored one.
	 * @return The started build.
	 * @throws TopLogicException
	 *         If the project has no repository or no CI job, the repository no longer exists on
	 *         the hosting service, or the CI server fails.
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
	 * The given protected files without the {@link CIProvider#pipelineFile() pipeline file} of the
	 * given CI server: the protected files of a repository without CI, whose pipeline file can be
	 * committed when its CI is set up.
	 *
	 * @param protectedFiles
	 *        The glob patterns of the protected files.
	 * @param ci
	 *        The CI server, {@code null} if CI is switched off.
	 */
	public static List<String> withoutPipelineFile(List<String> protectedFiles, CIProvider ci) {
		if (ci == null || !protectedFiles.contains(ci.pipelineFile())) {
			return protectedFiles;
		}
		List<String> result = new ArrayList<>(protectedFiles);
		result.remove(ci.pipelineFile());
		return List.copyOf(result);
	}

	/**
	 * The given protected files with the {@link CIProvider#pipelineFile() pipeline file} of the
	 * given CI server added, unless it is listed already: the protected files of a repository with
	 * CI.
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
