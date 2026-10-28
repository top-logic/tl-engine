/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.util.Objects;

/**
 * A repository on a git hosting service, as reported by its {@link GitHostingProvider}.
 */
public final class GitRepository {

	private final RepositoryName _name;

	private final String _cloneUrl;

	private final String _webUrl;

	private final String _defaultBranch;

	private final boolean _canAdminister;

	/**
	 * Creates a {@link GitRepository} whose settings the credentials may administer.
	 *
	 * @see #GitRepository(RepositoryName, String, String, String, boolean)
	 */
	public GitRepository(RepositoryName name, String cloneUrl, String webUrl, String defaultBranch) {
		this(name, cloneUrl, webUrl, defaultBranch, true);
	}

	/**
	 * Creates a {@link GitRepository}.
	 *
	 * @param name
	 *        See {@link #getName()}.
	 * @param cloneUrl
	 *        See {@link #getCloneUrl()}.
	 * @param webUrl
	 *        See {@link #getWebUrl()}.
	 * @param defaultBranch
	 *        See {@link #getDefaultBranch()}.
	 * @param canAdminister
	 *        See {@link #canAdminister()}.
	 */
	public GitRepository(RepositoryName name, String cloneUrl, String webUrl, String defaultBranch,
			boolean canAdminister) {
		_name = Objects.requireNonNull(name);
		_cloneUrl = Objects.requireNonNull(cloneUrl);
		_webUrl = Objects.requireNonNull(webUrl);
		_defaultBranch = Objects.requireNonNull(defaultBranch);
		_canAdminister = canAdminister;
	}

	/**
	 * The owner and name of the repository.
	 */
	public RepositoryName getName() {
		return _name;
	}

	/**
	 * The full name of the repository, its owner and name separated by a slash, e.g.
	 * {@code my-org/my-app}.
	 *
	 * @see RepositoryName#getFullName()
	 */
	public String getFullName() {
		return _name.getFullName();
	}

	/**
	 * The URL to clone the repository from and push to over HTTP(S), e.g.
	 * {@code https://git.example.com/my-org/my-app.git}.
	 */
	public String getCloneUrl() {
		return _cloneUrl;
	}

	/**
	 * The URL of the page of the repository in the web interface of the hosting service.
	 */
	public String getWebUrl() {
		return _webUrl;
	}

	/**
	 * The name of the default branch of the repository, e.g. {@code main}.
	 *
	 * <p>
	 * In an empty repository, the branch the first push to it should create.
	 * </p>
	 */
	public String getDefaultBranch() {
		return _defaultBranch;
	}

	/**
	 * Whether the credentials of the {@link GitHostingProvider} that reported the repository may
	 * administer it, e.g. {@link GitHostingProvider#protectBranch(GitRepository, String, java.util.List)
	 * protect its branches}.
	 */
	public boolean canAdminister() {
		return _canAdminister;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof GitRepository)) {
			return false;
		}
		GitRepository other = (GitRepository) obj;
		return _name.equals(other._name) && _cloneUrl.equals(other._cloneUrl) && _webUrl.equals(other._webUrl)
			&& _defaultBranch.equals(other._defaultBranch) && _canAdminister == other._canAdminister;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_name, _cloneUrl, _webUrl, _defaultBranch);
	}

	@Override
	public String toString() {
		return getFullName() + " (" + _cloneUrl + ", default branch " + _defaultBranch + ")";
	}

}
