/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.git;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GitHostingProvider} without hosting service: it keeps the repositories and their branch
 * protections in memory and records the calls.
 *
 * <p>
 * The repositories live at {@link #BASE_URL}; their default branch is {@link #DEFAULT_BRANCH}.
 * </p>
 */
public class RecordingGitHostingProvider implements GitHostingProvider {

	/**
	 * The URL of the simulated hosting service.
	 */
	public static final String BASE_URL = "https://git.test/";

	/**
	 * The {@link GitRepository#getDefaultBranch() default branch} of every repository.
	 */
	public static final String DEFAULT_BRANCH = "main";

	/**
	 * The {@link #getOwner() owner} of a provider created without one.
	 */
	public static final String DEFAULT_OWNER = "factory";

	/**
	 * Entry of {@link #calls()} for {@link #createRepository(String, String)}.
	 */
	public static final String CREATE = "create";

	/**
	 * Entry of {@link #calls()} for {@link #findRepository(String)}.
	 */
	public static final String FIND = "find";

	/**
	 * Entry of {@link #calls()} for {@link #protectBranch(GitRepository, String, List)}.
	 */
	public static final String PROTECT = "protect";

	private final String _owner;

	private final Map<RepositoryName, GitRepository> _repositories = new LinkedHashMap<>();

	private final Map<String, List<String>> _protections = new LinkedHashMap<>();

	private final List<String> _calls = new ArrayList<>();

	/**
	 * Creates a {@link RecordingGitHostingProvider} with the owner {@link #DEFAULT_OWNER}.
	 */
	public RecordingGitHostingProvider() {
		this(DEFAULT_OWNER);
	}

	/**
	 * Creates a {@link RecordingGitHostingProvider}.
	 *
	 * @param owner
	 *        See {@link #getOwner()}.
	 */
	public RecordingGitHostingProvider(String owner) {
		_owner = owner;
	}

	@Override
	public String getOwner() {
		return _owner;
	}

	/**
	 * Adds an existing repository of any owner, as if it had been created on the hosting service
	 * by someone else.
	 */
	public synchronized GitRepository addRepository(String owner, String name) {
		GitRepository repository = repository(new RepositoryName(owner, name));
		_repositories.put(repository.getName(), repository);
		return repository;
	}

	@Override
	public synchronized GitRepository createRepository(String name, String description) {
		_calls.add(CREATE + " " + name);
		RepositoryName fullName = new RepositoryName(_owner, name);
		if (_repositories.containsKey(fullName)) {
			throw new TopLogicException(I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY.fill(fullName.getFullName()));
		}
		GitRepository repository = repository(fullName);
		_repositories.put(fullName, repository);
		return repository;
	}

	@Override
	public synchronized GitRepository findRepository(String reference) {
		_calls.add(FIND + " " + reference);
		RepositoryName name = RepositoryName.parse(reference, BASE_URL);
		return name == null ? null : _repositories.get(name);
	}

	@Override
	public synchronized void protectBranch(GitRepository repository, String branch, List<String> protectedFiles) {
		_calls.add(PROTECT + " " + repository.getFullName() + " " + branch);
		if (!_repositories.containsKey(repository.getName())) {
			throw new TopLogicException(
				I18NConstants.ERROR_REPOSITORY_NOT_FOUND__REPOSITORY.fill(repository.getFullName()));
		}
		_protections.put(protectionKey(repository, branch), List.copyOf(protectedFiles));
	}

	/**
	 * The protected file patterns of the given branch, {@code null} if the branch is not
	 * protected.
	 */
	public synchronized List<String> protection(GitRepository repository, String branch) {
		return _protections.get(protectionKey(repository, branch));
	}

	/**
	 * The repositories in the order of their creation.
	 */
	public synchronized List<GitRepository> repositories() {
		return new ArrayList<>(_repositories.values());
	}

	/**
	 * The calls of the operations, each as {@code <operation> <argument>...}, in the order of the
	 * calls.
	 */
	public synchronized List<String> calls() {
		return new ArrayList<>(_calls);
	}

	private static String protectionKey(GitRepository repository, String branch) {
		return repository.getFullName() + ':' + branch;
	}

	private static GitRepository repository(RepositoryName name) {
		String webUrl = BASE_URL + name.getFullName();
		return new GitRepository(name, webUrl + RepositoryName.GIT_SUFFIX, webUrl, DEFAULT_BRANCH);
	}

}
