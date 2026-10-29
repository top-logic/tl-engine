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

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.factory.git.GitCapabilities;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GitHostingProvider} without hosting service: it keeps the repositories, their branch
 * protections and the files committed through it in memory and records the calls.
 *
 * <p>
 * The repositories live at {@link #BASE_URL}; their default branch is {@link #DEFAULT_BRANCH}.
 * Their pages and clone URLs are below {@link #BASE_URL} as well, until a test
 * {@link #setRootUrl(String) moves} them, as a hosting service does when its public URL is changed.
 * The {@link #gitUrl(RepositoryName) git URLs} are below a {@link #getGitBaseUrl() base URL of its
 * own}, by default {@link #BASE_URL}, so that a test can serve git's protocol itself.
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

	/**
	 * Entry of {@link #calls()} for {@link #commitFile(GitRepository, String, String, String, String)}.
	 */
	public static final String COMMIT_FILE = "commit-file";

	private final String _owner;

	private final String _gitBaseUrl;

	private String _rootUrl = BASE_URL;

	private final CredentialProvider _gitCredentials;

	private final Map<RepositoryName, GitRepository> _repositories = new LinkedHashMap<>();

	private final Map<String, List<String>> _protections = new LinkedHashMap<>();

	private final Map<String, Map<String, String>> _files = new LinkedHashMap<>();

	private final List<String> _calls = new ArrayList<>();

	private GitCapabilities _capabilities = GitCapabilities.ALL;

	private boolean _canAdminister = true;

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
		this(owner, BASE_URL, request -> {
			// No credentials.
		});
	}

	/**
	 * Creates a {@link RecordingGitHostingProvider} whose git protocol is served elsewhere.
	 *
	 * @param owner
	 *        See {@link #getOwner()}.
	 * @param gitBaseUrl
	 *        See {@link #getGitBaseUrl()}.
	 * @param gitCredentials
	 *        See {@link #gitCredentials()}.
	 */
	public RecordingGitHostingProvider(String owner, String gitBaseUrl, CredentialProvider gitCredentials) {
		_owner = owner;
		_gitBaseUrl = gitBaseUrl;
		_gitCredentials = gitCredentials;
	}

	/**
	 * The URL the {@link #gitUrl(RepositoryName) git URLs} are below, with a slash at its end.
	 */
	public String getGitBaseUrl() {
		return _gitBaseUrl;
	}

	@Override
	public String gitUrl(RepositoryName repository) {
		return _gitBaseUrl + repository.getFullName() + RepositoryName.GIT_SUFFIX;
	}

	@Override
	public CredentialProvider gitCredentials() {
		return _gitCredentials;
	}

	@Override
	public String getOwner() {
		return _owner;
	}

	@Override
	public synchronized GitCapabilities capabilities() {
		return _capabilities;
	}

	/**
	 * Sets what {@link #capabilities()} reports.
	 */
	public synchronized void setCapabilities(GitCapabilities capabilities) {
		_capabilities = capabilities;
	}

	/**
	 * Sets whether the credentials may administer the repositories created or added from now on,
	 * see {@link GitRepository#canAdminister()}; a protection of a repository they may not
	 * administer fails.
	 */
	public synchronized void setCanAdminister(boolean canAdminister) {
		_canAdminister = canAdminister;
	}

	/**
	 * Adds an existing repository of any owner, as if it had been created on the hosting service
	 * by someone else.
	 */
	public synchronized GitRepository addRepository(String owner, String name) {
		GitRepository repository = repository(new RepositoryName(owner, name), _canAdminister);
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
		GitRepository repository = repository(fullName, _canAdminister);
		_repositories.put(fullName, repository);
		return repository;
	}

	@Override
	public synchronized GitRepository findRepository(String reference) {
		_calls.add(FIND + " " + reference);
		RepositoryName name = RepositoryName.parse(reference, BASE_URL);
		if (name == null) {
			name = RepositoryName.parse(reference, _rootUrl);
		}
		return name == null ? null : _repositories.get(name);
	}

	/**
	 * Sets the URL the pages and clone URLs of the repositories are below, with a slash at its
	 * end, and gives the existing repositories their URLs below it.
	 */
	public synchronized void setRootUrl(String rootUrl) {
		_rootUrl = rootUrl;
		for (Map.Entry<RepositoryName, GitRepository> entry : _repositories.entrySet()) {
			entry.setValue(repository(entry.getKey(), entry.getValue().canAdminister()));
		}
	}

	@Override
	public synchronized void protectBranch(GitRepository repository, String branch, List<String> protectedFiles) {
		_calls.add(PROTECT + " " + repository.getFullName() + " " + branch);
		GitRepository stored = _repositories.get(repository.getName());
		if (stored == null) {
			throw new TopLogicException(
				I18NConstants.ERROR_REPOSITORY_NOT_FOUND__REPOSITORY.fill(repository.getFullName()));
		}
		if (!stored.canAdminister()) {
			throw new TopLogicException(
				I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(repository.getFullName()));
		}
		_protections.put(protectionKey(repository, branch), List.copyOf(protectedFiles));
	}

	/**
	 * Adds the file to the {@link #files(GitRepository, String) files} of the branch; a file of a
	 * protected path fails like the hosting service rejects it.
	 */
	@Override
	public synchronized boolean commitFile(GitRepository repository, String branch, String path, String content,
			String message) {
		_calls.add(COMMIT_FILE + " " + repository.getFullName() + " " + branch + " " + path);
		if (!_repositories.containsKey(repository.getName())) {
			throw new TopLogicException(
				I18NConstants.ERROR_REPOSITORY_NOT_FOUND__REPOSITORY.fill(repository.getFullName()));
		}
		Map<String, String> files = _files.computeIfAbsent(protectionKey(repository, branch), key -> new LinkedHashMap<>());
		if (files.containsKey(path)) {
			return false;
		}
		List<String> protection = protection(repository, branch);
		if (protection != null && protection.contains(path)) {
			throw new TopLogicException(
				I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(repository.getFullName()));
		}
		files.put(path, content);
		return true;
	}

	/**
	 * Adds a file to a branch, as if it had been pushed by someone else.
	 */
	public synchronized void addFile(GitRepository repository, String branch, String path, String content) {
		_files.computeIfAbsent(protectionKey(repository, branch), key -> new LinkedHashMap<>()).put(path, content);
	}

	/**
	 * The files of the given branch {@link #commitFile(GitRepository, String, String, String, String)
	 * committed} through this provider or {@link #addFile(GitRepository, String, String, String)
	 * added}, by path, with their content.
	 */
	public synchronized Map<String, String> files(GitRepository repository, String branch) {
		return new LinkedHashMap<>(_files.getOrDefault(protectionKey(repository, branch), Map.of()));
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

	private GitRepository repository(RepositoryName name, boolean canAdminister) {
		String webUrl = _rootUrl + name.getFullName();
		return new GitRepository(name, webUrl + RepositoryName.GIT_SUFFIX, webUrl, DEFAULT_BRANCH, canAdminister);
	}

}
