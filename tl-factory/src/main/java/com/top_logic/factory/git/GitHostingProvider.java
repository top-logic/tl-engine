/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.List;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.util.error.TopLogicException;

/**
 * Access to a git hosting service holding the repositories of the projects.
 *
 * <p>
 * A provider creates repositories in one configured {@link #getOwner() owner} (an organization or
 * user of the hosting service) and authenticates its requests with the credentials of the control
 * application.
 * </p>
 *
 * <p>
 * An operation that fails reports the failure as {@link TopLogicException} with one of the keys of
 * {@link I18NConstants}, e.g. {@link I18NConstants#ERROR_REPOSITORY_EXISTS__REPOSITORY} for a
 * repository that already exists, {@link I18NConstants#ERROR_AUTHENTICATION_FAILED__SERVICE} for
 * credentials the service rejects, {@link I18NConstants#ERROR_PERMISSION_DENIED__REPOSITORY} for an
 * operation the credentials do not permit.
 * </p>
 *
 * @see GitHostingService
 */
public interface GitHostingProvider {

	/**
	 * The organization or user the {@link #createRepository(String, String) created} repositories
	 * belong to.
	 */
	String getOwner();

	/**
	 * Creates an empty, private repository of the {@link #getOwner() owner}.
	 *
	 * <p>
	 * The repository has no commit; its {@link GitRepository#getDefaultBranch() default branch} is
	 * created by the first push to it.
	 * </p>
	 *
	 * @param name
	 *        The name of the repository within the owner.
	 * @param description
	 *        The description shown by the hosting service, {@code null} for none.
	 * @return The created repository.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_REPOSITORY_EXISTS__REPOSITORY} if the owner has a
	 *         repository of this name, with {@link I18NConstants#ERROR_INVALID_REPOSITORY_NAME__NAME}
	 *         if the service does not accept the name, with
	 *         {@link I18NConstants#ERROR_OWNER_NOT_FOUND__OWNER} if the owner does not exist.
	 */
	GitRepository createRepository(String name, String description);

	/**
	 * Looks up an existing repository of the hosting service, of any owner the credentials can
	 * read.
	 *
	 * @param reference
	 *        The repository as the user enters it: its full name {@code owner/name}, its clone URL
	 *        or the URL of one of its pages, see {@link RepositoryName#parse(String, String)}.
	 * @return The repository, {@code null} if the reference names no repository of this hosting
	 *         service, the repository does not exist or the credentials cannot read it.
	 * @throws TopLogicException
	 *         If the hosting service cannot be asked, or rejects the credentials.
	 */
	GitRepository findRepository(String reference);

	/**
	 * Protects a branch of a repository.
	 *
	 * <p>
	 * After the call, the hosting service enforces on the branch:
	 * </p>
	 * <ul>
	 * <li>The branch cannot be deleted, and no push may rewrite its history (no force push), for
	 * every user.</li>
	 * <li>A push that changes a file matching one of the protected file patterns is rejected, for
	 * every user. Such a change reaches the branch only through a pull request that is reviewed and
	 * merged in the hosting service by a user allowed to do so; how the hosting service decides who
	 * that is, describes the provider.</li>
	 * <li>Any other push that adds commits on top of the branch is accepted from every user with
	 * write access to the repository.</li>
	 * </ul>
	 *
	 * <p>
	 * A protection the branch already has is replaced, so the call can be repeated.
	 * </p>
	 *
	 * @param repository
	 *        The repository.
	 * @param branch
	 *        The name of the branch. It should exist: a hosting service may reject the push
	 *        creating a protected branch, see the provider.
	 * @param protectedFiles
	 *        Glob patterns of the paths of the protected files, relative to the root of the
	 *        repository: {@code *} matches within one path segment, {@code **} across segments,
	 *        e.g. {@code Jenkinsfile} or {@code deploy/**}. Matched without regard to case.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_REPOSITORY_NOT_FOUND__REPOSITORY} if the repository
	 *         does not exist, with {@link I18NConstants#ERROR_PERMISSION_DENIED__REPOSITORY} if the
	 *         credentials may not administer it.
	 */
	void protectBranch(GitRepository repository, String branch, List<String> protectedFiles);

	/**
	 * The URL git's smart HTTP protocol reaches the given repository at, without a slash at its
	 * end, e.g. {@code https://git.example.com/my-org/my-app.git}.
	 *
	 * <p>
	 * The requests of the protocol go to this URL followed by {@code /info/refs},
	 * {@code /git-upload-pack} or {@code /git-receive-pack}, see {@link GitRoute}. The repository
	 * is not looked up.
	 * </p>
	 */
	String gitUrl(RepositoryName repository);

	/**
	 * The credentials of the control application for git's smart HTTP protocol at
	 * {@link #gitUrl(RepositoryName)}, the ones a push or fetch of the {@link GitRoute} is
	 * authenticated with.
	 *
	 * <p>
	 * The {@link GitRoute} {@link CredentialProvider#invalidate() invalidates} them when the
	 * hosting service rejects them.
	 * </p>
	 */
	CredentialProvider gitCredentials();

}
