/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.proxy.ProcessToken;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.app.ApplicationSource;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ApplicationSource} keeping the sources of an application in a repository on the git
 * hosting service: the workspace has a clone of the repository, whose default branch is the
 * source of truth of the application.
 *
 * <p>
 * All git commands run in the workspace and reach the repository through the {@link GitRoute git
 * route} of the workspace proxy, with a token {@link ProxyTokens#issueProcessToken(String,
 * java.time.Duration) issued} for these commands only and revoked when they are done. The
 * sequence depends on the repository:
 * </p>
 * <ol>
 * <li>Without a {@link #getReference() reference}, the repository is
 * {@link GitHostingProvider#createRepository(String, String) created}, named by the artifact of
 * the application, and recorded at once (see the constructor), so that a run failing later
 * finds it again. With a reference, the repository is {@link GitHostingProvider#findRepository(String)
 * looked up} and recorded under its full name; a reference naming no repository fails.</li>
 * <li>An empty repository - created now, or created by a run that failed before its first push -
 * gets the application {@link ApplicationSource.Context#generate() generated} from the archetype
 * as its first commit, pushed to its default branch.</li>
 * <li>A repository with commits is cloned into the directory of the application. Its
 * {@link #POM} must describe the application of the project: the same artifact and, if the pom
 * names one, the same TopLogic version (see {@link Pom}).</li>
 * <li>Then the default branch is {@link GitHostingProvider#protectBranch(GitRepository, String,
 * List) protected} with the protected files - after the first push, which a hosting service may
 * reject for a protected branch. If the credentials of the git connection may not
 * {@link GitRepository#canAdminister() administer} the repository, the branch stays unprotected and
 * the output gets a {@link #WARNING warning} instead. Whether the branch is protected is recorded
 * for the project.</li>
 * </ol>
 *
 * <p>
 * With a {@link Pipeline CI pipeline}, the first commit of a new application also has the
 * {@link Pipeline#file() pipeline definition}; a cloned repository keeps its own, one without gets
 * a warning in the output. After the protection, the pipeline {@link Pipeline#setUp(GitRepository,
 * Consumer) sets up} the CI job of the repository. A failure of the CI is a warning in the output
 * only: the workspace is created without it, and the job can be set up later.
 * </p>
 *
 * <p>
 * The commits are made with the identity the git route gives every git process in a workspace,
 * see {@link GitRoute.Config#getUserName()}.
 * </p>
 */
public class RepositorySource implements ApplicationSource {

	/**
	 * The git command line in the workspace.
	 */
	public static final String GIT = "git";

	/**
	 * Name of the remote of the repository in the clone of the workspace.
	 */
	public static final String REMOTE = "origin";

	/**
	 * Name of the Maven project file in the root of the repository.
	 */
	public static final String POM = "pom.xml";

	/**
	 * Message of the first commit of a repository created for a project, followed by the TopLogic
	 * version.
	 */
	public static final String INITIAL_COMMIT_MESSAGE = "Generate the application from the TopLogic archetype ";

	private static final String LS_REMOTE = "ls-remote";

	private static final String CLONE = "clone";

	private static final String INIT = "init";

	private static final String INITIAL_BRANCH = "--initial-branch";

	private static final String ADD = "add";

	private static final String ALL = "--all";

	private static final String COMMIT = "commit";

	private static final String QUIET = "--quiet";

	private static final String MESSAGE = "--message";

	private static final String REMOTE_COMMAND = "remote";

	private static final String PUSH = "push";

	private static final String SET_UPSTREAM = "--set-upstream";

	private static final String TEST = "test";

	private static final String IS_FILE = "-f";

	private static final String CAT = "cat";

	private static final String PATH_SEPARATOR = "/";

	private static final String SHELL = "sh";

	private static final String SHELL_COMMAND = "-c";

	/**
	 * Environment variable holding the content of the file {@link #WRITE_FILE} writes.
	 */
	public static final String ENV_FILE_CONTENT = "TL_FACTORY_FILE_CONTENT";

	/**
	 * Shell script writing the content of {@link #ENV_FILE_CONTENT} to the file named by its first
	 * argument, creating its directory.
	 */
	public static final String WRITE_FILE =
		"mkdir -p \"$(dirname \"$1\")\" && printf '%s' \"$" + ENV_FILE_CONTENT + "\" > \"$1\"";

	/**
	 * Prefix of a line of the output of a step that failed without failing the workspace.
	 */
	public static final String WARNING = "Warning: ";

	private final GitHostingProvider _hosting;

	private final ProxyTokens _tokens;

	private final String _routeUrl;

	private final String _reference;

	private final String _description;

	private final List<String> _protectedFiles;

	private final Consumer<GitRepository> _record;

	private final Consumer<Boolean> _recordProtection;

	private final Pipeline _pipeline;

	/**
	 * Creates a {@link RepositorySource}.
	 *
	 * @param hosting
	 *        The hosting service of the repository.
	 * @param tokens
	 *        The issuer of the tokens of the git commands, {@code null} if the workspace proxy is
	 *        not running, which fails the source.
	 * @param routeUrl
	 *        The URL of the {@link GitRoute} as the workspace containers reach it, {@code null} if
	 *        the proxy is switched off or has no git route, which fails the source.
	 * @param reference
	 *        See {@link #getReference()}.
	 * @param description
	 *        The description of a created repository, {@code null} for none.
	 * @param protectedFiles
	 *        The glob patterns of the files no push to the default branch may change, see
	 *        {@link GitHostingService#getProtectedFiles()}.
	 * @param record
	 *        Stores the repository of the project, when it is created or looked up, in a
	 *        transaction of its own.
	 */
	public RepositorySource(GitHostingProvider hosting, ProxyTokens tokens, String routeUrl, String reference,
			String description, List<String> protectedFiles, Consumer<GitRepository> record) {
		this(hosting, tokens, routeUrl, reference, description, protectedFiles, record, null);
	}

	/**
	 * Creates a {@link RepositorySource} whose repository is built by a CI pipeline.
	 *
	 * @param pipeline
	 *        The CI pipeline of the repository, {@code null} for none.
	 *
	 * @see #RepositorySource(GitHostingProvider, ProxyTokens, String, String, String, List, Consumer)
	 */
	public RepositorySource(GitHostingProvider hosting, ProxyTokens tokens, String routeUrl, String reference,
			String description, List<String> protectedFiles, Consumer<GitRepository> record, Pipeline pipeline) {
		this(hosting, tokens, routeUrl, reference, description, protectedFiles, record, protection -> {
			// Not recorded.
		}, pipeline);
	}

	/**
	 * Creates a {@link RepositorySource} that records whether the default branch is protected.
	 *
	 * @param recordProtection
	 *        Stores whether the default branch of the repository is protected, in a transaction of
	 *        its own; called once the protection is set or skipped.
	 *
	 * @see #RepositorySource(GitHostingProvider, ProxyTokens, String, String, String, List, Consumer,
	 *      Pipeline)
	 */
	public RepositorySource(GitHostingProvider hosting, ProxyTokens tokens, String routeUrl, String reference,
			String description, List<String> protectedFiles, Consumer<GitRepository> record,
			Consumer<Boolean> recordProtection, Pipeline pipeline) {
		_hosting = Objects.requireNonNull(hosting, "hosting");
		_tokens = tokens;
		_routeUrl = routeUrl;
		_reference = reference == null || reference.isBlank() ? null : reference.strip();
		_description = description;
		_protectedFiles = List.copyOf(protectedFiles);
		_record = Objects.requireNonNull(record, "record");
		_recordProtection = Objects.requireNonNull(recordProtection, "recordProtection");
		_pipeline = pipeline;
	}

	/**
	 * The repository the sources are in, as recorded or entered for the project: its full name, a
	 * clone URL or the URL of one of its pages; {@code null} for a repository to create.
	 */
	public String getReference() {
		return _reference;
	}

	@Override
	public void provide(Context context) throws InterruptedException {
		if (_tokens == null || _routeUrl == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_GIT_ROUTE);
		}
		GitRepository repository = repository(context);
		String branch = repository.getDefaultBranch();
		String remote = GitRoute.remoteUrl(_routeUrl, repository.getName());

		boolean cloned;
		ProcessToken token = _tokens.issueProcessToken(context.spec().getWorkspaceId(), context.commandTimeout());
		if (token == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_GIT_ROUTE);
		}
		try (token) {
			Map<String, String> env = token.env();
			cloned = !isEmpty(context, remote, env);
			if (cloned) {
				context.step("Cloning the repository " + repository.getFullName());
				context.run(git(context, env, CLONE, remote, context.appDir())
					.setWorkingDir(ApplicationWorkspaces.SOURCE_PATH));
			} else {
				context.generate();
				if (_pipeline != null) {
					addPipeline(context);
				}
				initialPush(context, env, remote, branch);
			}
		}
		if (cloned) {
			checkPom(context, repository);
			if (_pipeline != null) {
				checkPipeline(context, repository);
			}
		}
		if (repository.canAdminister()) {
			context.step("Protecting the branch " + branch + " of " + repository.getFullName() + ", protected files "
				+ String.join(", ", _protectedFiles));
			_hosting.protectBranch(repository, branch, _protectedFiles);
			_recordProtection.accept(Boolean.TRUE);
		} else {
			context.step(WARNING + "The access token of the git connection may not administer the repository "
				+ repository.getFullName() + ", the branch " + branch
				+ " stays unprotected. Protect it in the git hosting service against force pushes and changes of "
				+ String.join(", ", _protectedFiles) + ".");
			_recordProtection.accept(Boolean.FALSE);
		}
		if (_pipeline != null) {
			setUpPipeline(context, repository);
		}
	}

	/**
	 * Writes the pipeline definition into the generated application.
	 */
	private void addPipeline(Context context) throws InterruptedException {
		String file = _pipeline.file();
		context.step("Adding the CI pipeline " + file);
		context.run(ExecCommand.of(SHELL, SHELL_COMMAND, WRITE_FILE, SHELL, context.appDir() + PATH_SEPARATOR + file)
			.putEnv(ENV_FILE_CONTENT, _pipeline.content()));
	}

	/**
	 * Warns if the cloned repository has no pipeline definition.
	 */
	private void checkPipeline(Context context, GitRepository repository) throws InterruptedException {
		String file = _pipeline.file();
		if (context.exec(ExecCommand.of(TEST, IS_FILE, context.appDir() + PATH_SEPARATOR + file),
			ExecOutput.NONE) != 0) {
			context.step(WARNING + "The repository " + repository.getFullName() + " has no CI pipeline " + file
				+ "; its CI builds fail until a reviewed pull request adds one.");
		}
	}

	/**
	 * Sets up the CI job of the repository; a failure is a warning only.
	 */
	private void setUpPipeline(Context context, GitRepository repository) {
		try {
			_pipeline.setUp(repository, context::step);
		} catch (RuntimeException ex) {
			Logger.warn("Setting up the CI job of " + repository.getFullName() + " failed.", ex,
				RepositorySource.class);
			String message = ex.getMessage();
			context.step(WARNING + "Setting up the CI job failed, the workspace is created without it: "
				+ (message == null || message.isEmpty() ? ex.getClass().getName() : message));
		}
	}

	/**
	 * The repository of the project: created and recorded without {@link #getReference()
	 * reference}, otherwise looked up and recorded under its full name.
	 */
	private GitRepository repository(Context context) {
		GitRepository result;
		if (_reference == null) {
			context.step("Creating the repository " + _hosting.getOwner() + RepositoryName.SEPARATOR
				+ context.spec().getArtifactId() + " on the git hosting service");
			result = _hosting.createRepository(context.spec().getArtifactId(), _description);
		} else {
			context.step("Looking up the repository " + _reference + " on the git hosting service");
			result = _hosting.findRepository(_reference);
			if (result == null) {
				throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_REPOSITORY__REFERENCE.fill(_reference));
			}
		}
		_record.accept(result);
		return result;
	}

	/**
	 * Checks a reference to an existing repository, as a user enters it.
	 *
	 * @param hosting
	 *        The hosting service of the repository.
	 * @param reference
	 *        The full name of the repository, its clone URL or the URL of one of its pages; empty
	 *        for none.
	 * @return The problem with the reference, {@code null} if it is empty or names a repository
	 *         the credentials of the connection can read.
	 */
	public static ResKey checkReference(GitHostingProvider hosting, String reference) {
		if (reference == null || reference.isBlank()) {
			return null;
		}
		String input = reference.strip();
		GitRepository repository;
		try {
			repository = hosting.findRepository(input);
		} catch (TopLogicException ex) {
			return ex.getErrorKey();
		}
		return repository == null ? I18NConstants.ERROR_UNKNOWN_REPOSITORY__REFERENCE.fill(input) : null;
	}

	/**
	 * Whether the repository has no refs yet, e.g. because a run failed before its first push.
	 */
	private static boolean isEmpty(Context context, String remote, Map<String, String> env)
			throws InterruptedException {
		return context.output(git(context, env, LS_REMOTE, remote)).isBlank();
	}

	/**
	 * Makes the generated application the first commit of the repository and pushes it.
	 */
	private static void initialPush(Context context, Map<String, String> env, String remote, String branch)
			throws InterruptedException {
		String dir = context.appDir();
		context.step("Committing the application to the branch " + branch);
		context.run(git(context, env, INIT, QUIET, INITIAL_BRANCH + "=" + branch).setWorkingDir(dir));
		context.run(git(context, env, ADD, ALL).setWorkingDir(dir));
		context.run(git(context, env, COMMIT, QUIET, MESSAGE, INITIAL_COMMIT_MESSAGE + context.engineVersion())
			.setWorkingDir(dir));
		context.run(git(context, env, REMOTE_COMMAND, ADD, REMOTE, remote).setWorkingDir(dir));
		context.step("Pushing the application to the repository");
		context.run(git(context, env, PUSH, SET_UPSTREAM, REMOTE, branch).setWorkingDir(dir));
	}

	/**
	 * Checks that the cloned repository holds the application of the project.
	 */
	private static void checkPom(Context context, GitRepository repository) throws InterruptedException {
		String path = context.appDir() + PATH_SEPARATOR + POM;
		if (context.exec(ExecCommand.of(TEST, IS_FILE, path), ExecOutput.NONE) != 0) {
			throw new TopLogicException(I18NConstants.ERROR_NO_POM__REPOSITORY.fill(repository.getFullName()));
		}
		Pom pom;
		try {
			pom = Pom.parse(context.output(ExecCommand.of(CAT, path)));
		} catch (IllegalArgumentException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_INVALID_POM__REPOSITORY_PROBLEM.fill(repository.getFullName(), ex.getMessage()), ex);
		}
		String artifactId = context.spec().getArtifactId();
		if (!artifactId.equals(pom.artifactId())) {
			throw new TopLogicException(I18NConstants.ERROR_ARTIFACT_MISMATCH__REPOSITORY_FOUND_EXPECTED
				.fill(repository.getFullName(), pom.artifactId(), artifactId));
		}
		String version = context.engineVersion();
		if (pom.engineVersion() != null && !pom.engineVersion().equals(version)) {
			throw new TopLogicException(I18NConstants.ERROR_ENGINE_VERSION_MISMATCH__REPOSITORY_FOUND_EXPECTED
				.fill(repository.getFullName(), pom.engineVersion(), version));
		}
	}

	private static ExecCommand git(Context context, Map<String, String> env, String... args) {
		List<String> command = new ArrayList<>(args.length + 1);
		command.add(GIT);
		command.addAll(List.of(args));
		return ExecCommand.of(command).putEnv(env).setTimeout(context.commandTimeout());
	}

	/**
	 * What the coding agent of a workspace whose application is a clone of the given repository
	 * is told about it, in addition to its instructions.
	 *
	 * @param repository
	 *        The full name of the repository.
	 * @param protectedFiles
	 *        The protected files of its default branch.
	 */
	public static String agentInstructions(String repository, List<String> protectedFiles) {
		StringBuilder result = new StringBuilder();
		result.append("The application in your working directory is a clone of the git repository ")
			.append(repository).append(" (remote ").append(REMOTE).append("), the source of truth of the application. ")
			.append("Your work reaches the application only when you commit it and push it to the default branch: ")
			.append("`git push ").append(REMOTE).append(" HEAD`. Never force push or rewrite pushed commits.");
		if (!protectedFiles.isEmpty()) {
			result.append(" A push changing one of the protected files (").append(String.join(", ", protectedFiles))
				.append(") is rejected; leave them unchanged.");
		}
		return result.toString();
	}

	/**
	 * The CI pipeline building the repository of a {@link RepositorySource}.
	 */
	public interface Pipeline {

		/**
		 * The path of the pipeline definition relative to the root of the repository, e.g.
		 * {@code Jenkinsfile}.
		 */
		String file();

		/**
		 * The pipeline definition the first commit of a new application gets.
		 */
		String content();

		/**
		 * Makes sure the CI job of the repository exists and builds it.
		 *
		 * <p>
		 * Called after the default branch of the repository has its sources and is protected.
		 * </p>
		 *
		 * @param repository
		 *        The repository.
		 * @param progress
		 *        Receiver of progress lines naming the steps.
		 * @throws RuntimeException
		 *         If the CI server fails; the {@link RepositorySource} reports it as a warning.
		 */
		void setUp(GitRepository repository, Consumer<String> progress);

	}

	/**
	 * What the {@link #POM} of an application tells about it.
	 *
	 * @param artifactId
	 *        The artifact of the project, {@code null} if it has none of its own.
	 * @param engineVersion
	 *        The TopLogic version the application is built on, {@code null} if the pom does not
	 *        name it literally: the property {@link #TL_VERSION} of an application generated from
	 *        the archetype, otherwise the version of the parent {@link #TL_PARENT_APP}.
	 */
	public record Pom(String artifactId, String engineVersion) {

		/**
		 * The property of the TopLogic version in the pom of an application generated from the
		 * archetype.
		 */
		public static final String TL_VERSION = "tl.version";

		/**
		 * The parent of the pom of an application not using the packaging of the TopLogic Maven
		 * plugin, whose version is the TopLogic version.
		 */
		public static final String TL_PARENT_APP = "tl-parent-app";

		private static final String PROJECT = "project";

		private static final String ARTIFACT_ID = "artifactId";

		private static final String VERSION = "version";

		private static final String PARENT = "parent";

		private static final String PROPERTIES = "properties";

		private static final String EXPRESSION_START = "${";

		private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";

		/**
		 * Reads the given pom.
		 *
		 * @throws IllegalArgumentException
		 *         If the text is no Maven project.
		 */
		public static Pom parse(String xml) {
			Element project;
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				factory.setFeature(DISALLOW_DOCTYPE, true);
				factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
				factory.setExpandEntityReferences(false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				builder.setErrorHandler(null);
				project = builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
			} catch (ParserConfigurationException | SAXException | IOException ex) {
				throw new IllegalArgumentException(ex.getMessage(), ex);
			}
			if (!PROJECT.equals(name(project))) {
				throw new IllegalArgumentException("The root element is not <" + PROJECT + ">.");
			}
			String artifactId = text(child(project, ARTIFACT_ID));
			String version = literal(text(child(child(project, PROPERTIES), TL_VERSION)));
			if (version == null) {
				Element parent = child(project, PARENT);
				if (TL_PARENT_APP.equals(text(child(parent, ARTIFACT_ID)))) {
					version = literal(text(child(parent, VERSION)));
				}
			}
			return new Pom(artifactId, version);
		}

		private static Element child(Element parent, String name) {
			if (parent == null) {
				return null;
			}
			for (Node node = parent.getFirstChild(); node != null; node = node.getNextSibling()) {
				if (node instanceof Element element && name.equals(name(element))) {
					return element;
				}
			}
			return null;
		}

		private static String name(Element element) {
			String localName = element.getLocalName();
			return localName == null ? element.getTagName() : localName;
		}

		private static String text(Element element) {
			if (element == null) {
				return null;
			}
			String text = element.getTextContent().strip();
			return text.isEmpty() ? null : text;
		}

		private static String literal(String value) {
			return value == null || value.contains(EXPRESSION_START) ? null : value;
		}
	}

}
