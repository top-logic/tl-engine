/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.jenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.BasicCredentials;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.ci.CIBuild;
import com.top_logic.factory.ci.CIBuildStart;
import com.top_logic.factory.ci.CIBuildState;
import com.top_logic.factory.ci.CIConsole;
import com.top_logic.factory.ci.CIJob;
import com.top_logic.factory.ci.CIProvider;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.ci.I18NConstants;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.server.ServerProviderConfig;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CIProvider} of a <a href="https://www.jenkins.io/">Jenkins</a> server.
 *
 * <p>
 * The job of a project repository is a pipeline job ("Pipeline script from SCM") in the folder
 * {@link Config#getFolder()}, below a folder named after the owner of the repository and named
 * after the repository: {@code folder/owner/name}. Missing folders are created. The job is
 * created from the {@link Config#getJobTemplate() job template}: it checks out the default branch
 * of the repository from its clone URL with the Jenkins credential {@link Config#getScmCredentialsId()}
 * and runs the pipeline definition {@link Config#getScriptPath()}. The control application never
 * sends a git credential to Jenkins; the credential with read access to the git hosting is set up
 * in Jenkins.
 * </p>
 *
 * <p>
 * The pages of jobs and builds are the URLs Jenkins reports in the {@link #FIELD_URL} of its
 * answers, below the Jenkins URL configured in Jenkins (its root URL), which the browsers of the
 * users reach; the {@link Config#getBaseUrl() base URL} is the address the control application
 * sends its requests to and may differ from it.
 * </p>
 *
 * <p>
 * The requests are authenticated with the configured {@link Config#getCredentials() credentials};
 * without them, with basic authentication of the user {@link Config#getUser()} and the API token in
 * the secret {@link #TOKEN_SECRET}. Jenkins does not ask a request authenticated with an API token
 * for a CSRF crumb, so the provider fetches none; credentials with a password instead of an API
 * token are rejected by Jenkins for the changing requests. A request rejected with 401 is repeated
 * once with fresh credentials.
 * </p>
 *
 * <h2>Permissions</h2>
 *
 * <p>
 * The user needs the permissions Overall/Read, Job/Create, Job/Configure, Job/Read and Job/Build in
 * the folder of the jobs (and Job/Create in its parent to create it), and the right to use the SCM
 * credential in the jobs.
 * </p>
 */
public class JenkinsCIProvider extends AbstractConfiguredInstance<JenkinsCIProvider.Config<?>>
		implements CIProvider {

	/**
	 * Configuration of {@link JenkinsCIProvider}.
	 */
	public interface Config<I extends JenkinsCIProvider> extends ServerProviderConfig<I> {

		/**
		 * Configuration property name of {@link #getUser()}.
		 */
		String USER = "user";

		/**
		 * Configuration property name of {@link #getCredentials()}.
		 */
		String CREDENTIALS = "credentials";

		/**
		 * Configuration property name of {@link #getFolder()}.
		 */
		String FOLDER = "folder";

		/**
		 * Configuration property name of {@link #getScmCredentialsId()}.
		 */
		String SCM_CREDENTIALS_ID = "scm-credentials-id";

		/**
		 * Configuration property name of {@link #getScriptPath()}.
		 */
		String SCRIPT_PATH = "script-path";

		/**
		 * Configuration property name of {@link #getJobTemplate()}.
		 */
		String JOB_TEMPLATE = "job-template";

		/**
		 * Configuration property name of {@link #getTimeout()}.
		 */
		String TIMEOUT = "timeout";

		/**
		 * Configuration property name of {@link #getQueueWait()}.
		 */
		String QUEUE_WAIT = "queue-wait";

		/**
		 * The URL of the Jenkins server, e.g. {@code https://jenkins.example.com/}.
		 *
		 * <p>
		 * The URL under which its web interface is served, its "Jenkins URL". The pages of the jobs
		 * and builds the provider reports are below it.
		 * </p>
		 *
		 * <p>
		 * Empty switches the provider off, see {@link ServerProviderConfig}.
		 * </p>
		 *
		 * @see #BASE_URL
		 */
		@Override
		@Name(BASE_URL)
		@Nullable
		String getBaseUrl();

		/**
		 * The Jenkins user the requests are made as, if no {@link #getCredentials() credentials}
		 * are configured.
		 *
		 * <p>
		 * The requests then authenticate with this user and the API token in the secret
		 * {@link JenkinsCIProvider#TOKEN_SECRET}.
		 * </p>
		 *
		 * @see #USER
		 */
		@Name(USER)
		@Nullable
		String getUser();

		/**
		 * How the requests to Jenkins are authenticated.
		 *
		 * <p>
		 * If not set, with basic authentication of the {@link #getUser() user} and the API token in
		 * the secret {@link JenkinsCIProvider#TOKEN_SECRET}.
		 * </p>
		 *
		 * @see #CREDENTIALS
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/**
		 * The folder of the jobs, nested folders separated by slashes; empty for the top level of
		 * Jenkins.
		 *
		 * <p>
		 * The folder is created if it does not exist.
		 * </p>
		 *
		 * @see #FOLDER
		 */
		@Name(FOLDER)
		@StringDefault(DEFAULT_FOLDER)
		String getFolder();

		/**
		 * The ID of the Jenkins credential the jobs check out the repositories with; empty for
		 * none, e.g. for public repositories.
		 *
		 * <p>
		 * A credential of Jenkins with read access to the repositories of the git hosting service,
		 * e.g. a user name with an access token. It is set up in Jenkins; the control application
		 * names it only.
		 * </p>
		 *
		 * @see #SCM_CREDENTIALS_ID
		 */
		@Name(SCM_CREDENTIALS_ID)
		@Nullable
		String getScmCredentialsId();

		/**
		 * The path of the pipeline definition in a repository the jobs run.
		 *
		 * @see #SCRIPT_PATH
		 */
		@Name(SCRIPT_PATH)
		@StringDefault(DEFAULT_SCRIPT_PATH)
		String getScriptPath();

		/**
		 * The web application resource holding the configuration of a job with the placeholders
		 * {@link JenkinsCIProvider#DESCRIPTION}, {@link JenkinsCIProvider#REPOSITORY_URL},
		 * {@link JenkinsCIProvider#CREDENTIALS_ID}, {@link JenkinsCIProvider#BRANCH} and
		 * {@link JenkinsCIProvider#SCRIPT_PATH}.
		 *
		 * <p>
		 * The values are inserted escaped for XML text and attribute values.
		 * </p>
		 *
		 * @see #JOB_TEMPLATE
		 */
		@Name(JOB_TEMPLATE)
		@StringDefault(DEFAULT_JOB_TEMPLATE)
		String getJobTemplate();

		/**
		 * How long a connection to Jenkins and each request may take.
		 *
		 * @see #TIMEOUT
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(30 * 1000)
		long getTimeout();

		/**
		 * How long starting a build waits for the build to leave the queue of Jenkins and get its
		 * number.
		 *
		 * @see #QUEUE_WAIT
		 */
		@Name(QUEUE_WAIT)
		@Format(MillisFormat.class)
		@LongDefault(10 * 1000)
		long getQueueWait();

	}

	/**
	 * Name of the secret holding the API token of the {@link Config#getUser() user}, if no
	 * {@link Config#getCredentials() credentials} are configured.
	 */
	public static final String TOKEN_SECRET = "tl_factory_jenkins_token";

	/**
	 * Default of {@link Config#getFolder()}.
	 */
	public static final String DEFAULT_FOLDER = "tl-factory";

	/**
	 * Default of {@link Config#getScriptPath()}.
	 */
	public static final String DEFAULT_SCRIPT_PATH = "Jenkinsfile";

	/**
	 * Default of {@link Config#getJobTemplate()}.
	 */
	public static final String DEFAULT_JOB_TEMPLATE = "/WEB-INF/factory/jenkins/pipeline-job.xml";

	/**
	 * Placeholder of the job template replaced by the description of the job.
	 */
	public static final String DESCRIPTION = "@DESCRIPTION@";

	/**
	 * Placeholder of the job template replaced by the clone URL of the repository.
	 */
	public static final String REPOSITORY_URL = "@REPOSITORY_URL@";

	/**
	 * Placeholder of the job template replaced by the {@link Config#getScmCredentialsId() SCM
	 * credential}.
	 */
	public static final String CREDENTIALS_ID = "@CREDENTIALS_ID@";

	/**
	 * Placeholder of the job template replaced by the default branch of the repository.
	 */
	public static final String BRANCH = "@BRANCH@";

	/**
	 * Placeholder of the job template replaced by the {@link Config#getScriptPath() pipeline
	 * definition}.
	 */
	public static final String SCRIPT_PATH = "@SCRIPT_PATH@";

	/**
	 * The configuration of a folder Jenkins creates.
	 */
	public static final String FOLDER_CONFIG = "<com.cloudbees.hudson.plugins.folder.Folder/>";

	/**
	 * Path segment introducing an item below Jenkins or a folder.
	 */
	public static final String JOB = "job";

	/**
	 * Path of the JSON API below an item.
	 */
	public static final String API_JSON = "api/json";

	/**
	 * Query parameter of the JSON API selecting the returned fields.
	 */
	public static final String TREE = "tree";

	/**
	 * Request creating an item in a folder.
	 */
	public static final String CREATE_ITEM = "createItem";

	/**
	 * Query parameter of {@link #CREATE_ITEM} naming the item.
	 */
	public static final String NAME = "name";

	/**
	 * The configuration of an item.
	 */
	public static final String CONFIG_XML = "config.xml";

	/**
	 * Request starting a build of a job.
	 */
	public static final String BUILD = "build";

	/**
	 * Query parameter of {@link #BUILD} with the quiet period.
	 */
	public static final String DELAY = "delay";

	/**
	 * Value of {@link #DELAY}: no quiet period.
	 */
	public static final String NO_DELAY = "0sec";

	/**
	 * The last build of a job.
	 */
	public static final String LAST_BUILD = "lastBuild";

	/**
	 * The console output of a build, read from an offset.
	 */
	public static final String PROGRESSIVE_TEXT = "logText/progressiveText";

	/**
	 * Query parameter of {@link #PROGRESSIVE_TEXT}: the offset to read from.
	 */
	public static final String START = "start";

	/**
	 * Header of an answer of {@link #PROGRESSIVE_TEXT}: the size of the whole output.
	 */
	public static final String TEXT_SIZE = "X-Text-Size";

	/**
	 * Queue of Jenkins.
	 */
	public static final String QUEUE_ITEM = "queue/item";

	/**
	 * Header naming the queue item of a started build.
	 */
	public static final String LOCATION = "Location";

	/**
	 * Field of the JSON API: the number of a build.
	 */
	public static final String FIELD_NUMBER = "number";

	/**
	 * Field of the JSON API: the result of a build, {@code null} while it runs.
	 */
	public static final String FIELD_RESULT = "result";

	/**
	 * Field of the JSON API: whether a build runs.
	 */
	public static final String FIELD_BUILDING = "building";

	/**
	 * Field of the JSON API: the start of a build in milliseconds since the epoch.
	 */
	public static final String FIELD_TIMESTAMP = "timestamp";

	/**
	 * Field of the JSON API: the duration of an ended build in milliseconds.
	 */
	public static final String FIELD_DURATION = "duration";

	/**
	 * Field of the JSON API: the build a queue item started.
	 */
	public static final String FIELD_EXECUTABLE = "executable";

	/**
	 * Field of the JSON API: whether a queue item was cancelled.
	 */
	public static final String FIELD_CANCELLED = "cancelled";

	/**
	 * Field of the JSON API: the page of an item or build, below the root URL of Jenkins.
	 */
	public static final String FIELD_URL = "url";

	private static final String BUILD_FIELDS =
		String.join(",", FIELD_NUMBER, FIELD_RESULT, FIELD_BUILDING, FIELD_TIMESTAMP, FIELD_DURATION, FIELD_URL);

	private static final String QUEUE_FIELDS =
		FIELD_CANCELLED + "," + FIELD_EXECUTABLE + "[" + FIELD_NUMBER + "," + FIELD_URL + "]";

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String XML_CONTENT_TYPE = "application/xml; charset=utf-8";

	private static final String GET = "GET";

	private static final String HEAD = "HEAD";

	private static final String POST = "POST";

	private static final String SLASH = "/";

	/**
	 * The ID of a queue item in its URL.
	 */
	private static final Pattern QUEUE_ID = Pattern.compile("/" + QUEUE_ITEM + "/([0-9]+)/?$");

	/**
	 * A console note of Jenkins in the raw console output: an encoded annotation, invisible in the
	 * plain text.
	 */
	private static final Pattern CONSOLE_NOTE = Pattern.compile("\u001B\\[8mha:[^\u001B]*\u001B\\[0m");

	private static final long QUEUE_POLL_MILLIS = 250;

	private static final int HTTP_CREATED = 201;

	private static final int HTTP_BAD_REQUEST = 400;

	/**
	 * Status of an answer to a request that got no answer, see {@link #failure(int, String, String, byte[])}.
	 */
	private static final int NO_RESPONSE = 0;

	private final String _baseUrl;

	private final List<String> _folder;

	private final CredentialProvider _credentials;

	private final Duration _timeout;

	private final HttpClient _client;

	private final ObjectMapper _json = new ObjectMapper();

	/**
	 * Creates a {@link JenkinsCIProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public JenkinsCIProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
		_baseUrl = withSlash(Objects.toString(config.getBaseUrl(), "").strip());
		URI base;
		try {
			base = URI.create(_baseUrl);
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationError(I18NConstants.ERROR_INVALID_CI_URL__URL.fill(config.getBaseUrl()), ex);
		}
		if (base.getScheme() == null || base.getHost() == null) {
			throw new ConfigurationError(I18NConstants.ERROR_INVALID_CI_URL__URL.fill(config.getBaseUrl()));
		}
		_folder = segments(config.getFolder());

		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials == null) {
			String user = config.getUser();
			if (user == null || user.isBlank()) {
				throw new ConfigurationError("The Jenkins provider needs '" + Config.USER + "' or '"
					+ Config.CREDENTIALS + "'.");
			}
			credentials = defaultCredentials(user.strip());
		}
		_credentials = context.getInstance(credentials);
		_timeout = Duration.ofMillis(config.getTimeout());
		_client = HttpClient.newBuilder()
			.connectTimeout(_timeout)
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
	}

	/**
	 * The credentials sending the given user and the API token in the secret
	 * {@link #TOKEN_SECRET} with basic authentication.
	 */
	private static PolymorphicConfiguration<? extends CredentialProvider> defaultCredentials(String user) {
		NamedSecretSource.Config token = TypedConfiguration.newConfigItem(NamedSecretSource.Config.class);
		token.setName(TOKEN_SECRET);
		BasicCredentials.Config basic = TypedConfiguration.newConfigItem(BasicCredentials.Config.class);
		basic.setUser(user);
		basic.setPassword(token);
		return basic;
	}

	@Override
	public String pipelineFile() {
		return getConfig().getScriptPath();
	}

	@Override
	public CIJob ensureJob(GitRepository repository, String description) {
		List<String> parent = new ArrayList<>(_folder);
		parent.add(repository.getName().getOwner());
		ensureFolders(parent);

		List<String> job = new ArrayList<>(parent);
		job.add(repository.getName().getName());
		String fullName = String.join(SLASH, job);
		byte[] config = jobConfig(repository, description).getBytes(StandardCharsets.UTF_8);
		String page = page(job, fullName);
		if (page != null) {
			updateConfig(job, fullName, config);
		} else {
			Response created = request(POST, createItemPath(parent, repository.getName().getName()), config,
				fullName, CREATE_ITEM);
			if (created.status() == HTTP_BAD_REQUEST && exists(job, fullName)) {
				// Created in the meantime.
				updateConfig(job, fullName, config);
			} else {
				expectOk(created, fullName, CREATE_ITEM);
			}
			page = existingPage(job, fullName);
		}
		return new CIJob(fullName, page);
	}

	@Override
	public CIJob findJob(String job) {
		List<String> path = segments(job);
		String page = page(path, job);
		return page == null ? null : new CIJob(String.join(SLASH, path), page);
	}

	private void updateConfig(List<String> job, String fullName, byte[] config) {
		expectOk(request(POST, itemPath(job) + CONFIG_XML, config, fullName, CONFIG_XML), fullName, CONFIG_XML);
	}

	/**
	 * Creates the folders of the given path that do not exist.
	 */
	private void ensureFolders(List<String> path) {
		for (int n = 1; n <= path.size(); n++) {
			List<String> folder = path.subList(0, n);
			String fullName = String.join(SLASH, folder);
			if (exists(folder, fullName)) {
				continue;
			}
			Response created = request(POST, createItemPath(folder.subList(0, n - 1), folder.get(n - 1)),
				FOLDER_CONFIG.getBytes(StandardCharsets.UTF_8), fullName, CREATE_ITEM);
			if (created.status() == HTTP_BAD_REQUEST && exists(folder, fullName)) {
				// Created in the meantime.
				continue;
			}
			expectOk(created, fullName, CREATE_ITEM);
		}
	}

	/**
	 * The configuration of the job of the given repository, from the job template.
	 */
	String jobConfig(GitRepository repository, String description) {
		String credentialsId = getConfig().getScmCredentialsId();
		return fill(CIService.readResource(getConfig().getJobTemplate()), Map.of(
			DESCRIPTION, description == null ? "" : description,
			REPOSITORY_URL, repository.getCloneUrl(),
			CREDENTIALS_ID, credentialsId == null ? "" : credentialsId.strip(),
			BRANCH, repository.getDefaultBranch(),
			SCRIPT_PATH, getConfig().getScriptPath()));
	}

	/**
	 * Replaces the placeholders in the given template by the given values, escaped for XML.
	 */
	public static String fill(String template, Map<String, String> values) {
		String result = template;
		for (Map.Entry<String, String> entry : values.entrySet()) {
			result = result.replace(entry.getKey(), xml(entry.getValue()));
		}
		return result;
	}

	/**
	 * The given text escaped for XML text and attribute values.
	 */
	public static String xml(String value) {
		StringBuilder result = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '&':
					result.append("&amp;");
					break;
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '"':
					result.append("&quot;");
					break;
				case '\'':
					result.append("&apos;");
					break;
				default:
					result.append(c);
			}
		}
		return result.toString();
	}

	@Override
	public CIBuildStart startBuild(String job) {
		List<String> path = segments(job);
		Response started = request(POST, itemPath(path) + BUILD + "?" + DELAY + "=" + NO_DELAY, null, job, BUILD);
		if (started.status() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(job));
		}
		if (started.status() != HTTP_CREATED && started.status() != HttpURLConnection.HTTP_OK) {
			throw failure(started, job, BUILD);
		}
		String queueId = queueId(started.header(LOCATION));
		if (queueId == null) {
			return new CIBuildStart(null, null, existingPage(path, job));
		}
		JsonNode build = awaitBuild(job, queueId);
		if (build == null) {
			return new CIBuildStart(null, queueId, existingPage(path, job));
		}
		int number = build.path(FIELD_NUMBER).intValue();
		return new CIBuildStart(Integer.valueOf(number), queueId, webUrl(build, () -> buildUrl(path, number)));
	}

	/**
	 * Waits for the queue item with the given ID to start its build.
	 *
	 * @return The {@link #FIELD_EXECUTABLE} of the queue item, the started build with its
	 *         {@link #FIELD_NUMBER} and {@link #FIELD_URL}; {@code null} if it waits longer than the
	 *         {@link Config#getQueueWait()} or was cancelled.
	 */
	private JsonNode awaitBuild(String job, String queueId) {
		long deadline = System.nanoTime() + Duration.ofMillis(getConfig().getQueueWait()).toNanos();
		String path = QUEUE_ITEM + SLASH + queueId + SLASH + API_JSON + "?" + TREE + "=" + encode(QUEUE_FIELDS);
		while (true) {
			Response item = request(GET, path, null, job, QUEUE_ITEM);
			if (item.status() == HttpURLConnection.HTTP_OK) {
				JsonNode json = parse(item, job, QUEUE_ITEM);
				JsonNode executable = json.path(FIELD_EXECUTABLE);
				if (executable.path(FIELD_NUMBER).isInt()) {
					return executable;
				}
				if (json.path(FIELD_CANCELLED).asBoolean()) {
					return null;
				}
			} else if (item.status() != HttpURLConnection.HTTP_NOT_FOUND) {
				// Jenkins forgets a queue item some minutes after its build started.
				throw failure(item, job, QUEUE_ITEM);
			}
			if (System.nanoTime() > deadline) {
				return null;
			}
			try {
				Thread.sleep(QUEUE_POLL_MILLIS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}

	/**
	 * The ID of the queue item in the given {@link #LOCATION}, {@code null} if it names none.
	 */
	static String queueId(String location) {
		if (location == null) {
			return null;
		}
		Matcher matcher = QUEUE_ID.matcher(location);
		return matcher.find() ? matcher.group(1) : null;
	}

	@Override
	public CIBuild lastBuild(String job) {
		List<String> path = segments(job);
		Response response = request(GET,
			itemPath(path) + LAST_BUILD + SLASH + API_JSON + "?" + TREE + "=" + encode(BUILD_FIELDS), null, job,
			LAST_BUILD);
		if (response.status() == HttpURLConnection.HTTP_NOT_FOUND) {
			if (!exists(path, job)) {
				throw new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(job));
			}
			return null;
		}
		expectOk(response, job, LAST_BUILD);
		JsonNode json = parse(response, job, LAST_BUILD);
		int number = json.path(FIELD_NUMBER).asInt();
		Instant start = Instant.ofEpochMilli(json.path(FIELD_TIMESTAMP).asLong());
		CIBuildState state = state(json);
		Duration duration = state == CIBuildState.RUNNING ? Duration.between(start, Instant.now())
			: Duration.ofMillis(json.path(FIELD_DURATION).asLong());
		return new CIBuild(number, state, webUrl(json, () -> buildUrl(path, number)), start,
			duration.isNegative() ? Duration.ZERO : duration);
	}

	/**
	 * The state of the build described by the given answer of the JSON API.
	 */
	static CIBuildState state(JsonNode build) {
		JsonNode result = build.path(FIELD_RESULT);
		if (build.path(FIELD_BUILDING).asBoolean() || result.isNull() || result.isMissingNode()) {
			return CIBuildState.RUNNING;
		}
		try {
			return CIBuildState.valueOf(result.asText());
		} catch (IllegalArgumentException ex) {
			// A result of a plugin; it did not succeed.
			return CIBuildState.FAILURE;
		}
	}

	@Override
	public CIConsole console(String job, int number, int maxBytes) {
		List<String> path = segments(job);
		String log = itemPath(path) + number + SLASH + PROGRESSIVE_TEXT + "?" + START + "=";
		String operation = PROGRESSIVE_TEXT;
		Response head = request(HEAD, log + 0, null, job, operation);
		if (head.status() == HttpURLConnection.HTTP_NOT_FOUND) {
			if (!exists(path, job)) {
				throw new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(job));
			}
			throw new TopLogicException(I18NConstants.ERROR_CI_BUILD_NOT_FOUND__JOB_BUILD.fill(job, number));
		}
		expectOk(head, job, operation);
		long start = 0;
		String size = head.header(TEXT_SIZE);
		if (size != null) {
			try {
				start = Math.max(0, Long.parseLong(size.strip()) - maxBytes);
			} catch (NumberFormatException ex) {
				start = 0;
			}
		}
		long from = start;
		HttpResponse<InputStream> text =
			send(() -> newRequest(log + from).GET(), HttpResponse.BodyHandlers.ofInputStream(), job, operation);
		byte[] tail;
		boolean cut;
		try (InputStream in = text.body()) {
			if (text.statusCode() != HttpURLConnection.HTTP_OK) {
				throw failure(text.statusCode(), job, operation, in.readAllBytes());
			}
			Tail read = Tail.read(in, maxBytes);
			tail = read.bytes();
			cut = from > 0 || read.cut();
		} catch (IOException ex) {
			throw unreachable(ex);
		}
		return console(tail, cut);
	}

	/**
	 * The plain text of the given end of a raw console output.
	 *
	 * @param raw
	 *        The bytes of the output.
	 * @param cut
	 *        Whether the bytes are not the start of the output: the first line is then incomplete
	 *        and dropped.
	 */
	static CIConsole console(byte[] raw, boolean cut) {
		String text = new String(raw, StandardCharsets.UTF_8);
		if (cut) {
			int lineEnd = text.indexOf('\n');
			text = lineEnd < 0 ? "" : text.substring(lineEnd + 1);
		}
		return new CIConsole(CONSOLE_NOTE.matcher(text).replaceAll(""), cut);
	}

	/**
	 * Whether the item of the given path exists.
	 */
	private boolean exists(List<String> path, String fullName) {
		return page(path, fullName) != null;
	}

	/**
	 * The page of the item of the given path, as Jenkins reports it.
	 *
	 * @return The URL, {@code null} if the item does not exist.
	 */
	private String page(List<String> path, String fullName) {
		Response response =
			request(GET, itemPath(path) + API_JSON + "?" + TREE + "=" + FIELD_URL, null, fullName, API_JSON);
		if (response.status() == HttpURLConnection.HTTP_NOT_FOUND) {
			return null;
		}
		expectOk(response, fullName, API_JSON);
		return webUrl(parse(response, fullName, API_JSON), () -> itemUrl(path));
	}

	/**
	 * The page of the item of the given path, which exists.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_CI_JOB_NOT_FOUND__JOB} if it does not exist.
	 */
	private String existingPage(List<String> path, String fullName) {
		String page = page(path, fullName);
		if (page == null) {
			throw new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(fullName));
		}
		return page;
	}

	/**
	 * The {@link #FIELD_URL} of the given answer of the JSON API.
	 *
	 * @param fallback
	 *        The URL below the {@link Config#getBaseUrl() base URL}, for an answer without
	 *        {@link #FIELD_URL}.
	 */
	private static String webUrl(JsonNode answer, Supplier<String> fallback) {
		JsonNode url = answer.path(FIELD_URL);
		return url.isTextual() && !url.asText().isBlank() ? withSlash(url.asText()) : fallback.get();
	}

	private void expectOk(Response response, String job, String operation) {
		if (response.status() != HttpURLConnection.HTTP_OK) {
			throw failure(response, job, operation);
		}
	}

	private JsonNode parse(Response response, String job, String operation) {
		try {
			return _json.readTree(response.body());
		} catch (IOException ex) {
			throw new TopLogicException(I18NConstants.ERROR_CI_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE
				.fill(_baseUrl, operation, response.status(), String.valueOf(ex.getMessage())), ex);
		}
	}

	/**
	 * Sends a request to Jenkins and reads its answer.
	 *
	 * @param method
	 *        The method.
	 * @param path
	 *        The path below the {@link Config#getBaseUrl() base URL}, with query.
	 * @param body
	 *        The XML body, {@code null} for none.
	 * @param job
	 *        The full name of the item the request is about, for error messages.
	 * @param operation
	 *        The name of the request, for error messages.
	 * @return The answer; also one with an error status. An answer with 401, 403 and 5xx is
	 *         reported as {@link TopLogicException} instead.
	 */
	private Response request(String method, String path, byte[] body, String job, String operation) {
		Supplier<HttpRequest.Builder> request = () -> {
			HttpRequest.Builder builder = newRequest(path);
			if (body != null) {
				builder.header(CONTENT_TYPE, XML_CONTENT_TYPE);
				return builder.method(method, HttpRequest.BodyPublishers.ofByteArray(body));
			}
			return builder.method(method, HttpRequest.BodyPublishers.noBody());
		};
		HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray(), job, operation);
		int status = response.statusCode();
		if (status == HttpURLConnection.HTTP_FORBIDDEN || status >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
			throw failure(status, job, operation, response.body());
		}
		return new Response(status, response.headers().map(), response.body());
	}

	private HttpRequest.Builder newRequest(String path) {
		return HttpRequest.newBuilder(URI.create(_baseUrl + path)).timeout(_timeout);
	}

	/**
	 * Sends a request with fresh credentials; if Jenkins rejects them, once more after invalidating
	 * them.
	 *
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_CI_AUTHENTICATION_FAILED__SERVICE} if Jenkins rejects
	 *         the credentials also the second time, with
	 *         {@link I18NConstants#ERROR_CI_UNREACHABLE__SERVICE_PROBLEM} if it does not answer.
	 */
	private <T> HttpResponse<T> send(Supplier<HttpRequest.Builder> request, HttpResponse.BodyHandler<T> body,
			String job, String operation) {
		HttpResponse<T> response = sendOnce(request, body);
		if (response.statusCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
			return response;
		}
		discard(response);
		// Cached credentials may have expired or been rotated.
		_credentials.invalidate();
		response = sendOnce(request, body);
		if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			discard(response);
			_credentials.invalidate();
			throw new TopLogicException(I18NConstants.ERROR_CI_AUTHENTICATION_FAILED__SERVICE.fill(_baseUrl));
		}
		return response;
	}

	private <T> HttpResponse<T> sendOnce(Supplier<HttpRequest.Builder> request, HttpResponse.BodyHandler<T> body) {
		HttpRequest.Builder builder = request.get();
		_credentials.authenticate(RequestAuthentication.of(builder));
		try {
			return _client.send(builder.build(), body);
		} catch (IOException ex) {
			throw unreachable(ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw unreachable(ex);
		}
	}

	private static void discard(HttpResponse<?> response) {
		if (response.body() instanceof InputStream in) {
			try {
				in.close();
			} catch (IOException ex) {
				// Nothing to read anyway.
			}
		}
	}

	private TopLogicException unreachable(Exception ex) {
		String problem = ex instanceof HttpTimeoutException ? "timeout" : ex.getClass().getName() + ": " + ex.getMessage();
		return new TopLogicException(I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM.fill(_baseUrl, problem), ex);
	}

	private TopLogicException failure(Response response, String job, String operation) {
		return failure(response.status(), job, operation, response.body());
	}

	private TopLogicException failure(int status, String job, String operation, byte[] body) {
		switch (status) {
			case NO_RESPONSE:
				return new TopLogicException(I18NConstants.ERROR_CI_UNREACHABLE__SERVICE_PROBLEM.fill(_baseUrl, operation));
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				return new TopLogicException(I18NConstants.ERROR_CI_AUTHENTICATION_FAILED__SERVICE.fill(_baseUrl));
			case HttpURLConnection.HTTP_FORBIDDEN: {
				TopLogicException denied = new TopLogicException(I18NConstants.ERROR_CI_PERMISSION_DENIED__JOB.fill(job));
				denied.initDetails(ResKey.text(operation + ": " + status));
				return denied;
			}
			case HttpURLConnection.HTTP_NOT_FOUND:
				return new TopLogicException(I18NConstants.ERROR_CI_JOB_NOT_FOUND__JOB.fill(job));
			default:
				return new TopLogicException(I18NConstants.ERROR_CI_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE
					.fill(_baseUrl, operation, status, message(body)));
		}
	}

	/**
	 * The start of an error answer of Jenkins, for a message.
	 */
	private static String message(byte[] body) {
		if (body == null || body.length == 0) {
			return "";
		}
		String text = new String(body, StandardCharsets.UTF_8).strip();
		int max = 500;
		return text.length() > max ? text.substring(0, max) + "..." : text;
	}

	/**
	 * The path of the given item below the base URL, with a slash at its end.
	 */
	private static String itemPath(List<String> path) {
		StringBuilder result = new StringBuilder();
		for (String segment : path) {
			result.append(JOB).append(SLASH).append(encodeSegment(segment)).append(SLASH);
		}
		return result.toString();
	}

	private static String createItemPath(List<String> parent, String name) {
		return itemPath(parent) + CREATE_ITEM + "?" + NAME + "=" + encode(name);
	}

	/**
	 * The page of the given item below the {@link Config#getBaseUrl() base URL}.
	 */
	private String itemUrl(List<String> path) {
		return _baseUrl + itemPath(path);
	}

	/**
	 * The page of a build of the given job below the {@link Config#getBaseUrl() base URL}.
	 */
	private String buildUrl(List<String> path, int number) {
		return itemUrl(path) + number + SLASH;
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private static String encodeSegment(String segment) {
		return encode(segment).replace("+", "%20");
	}

	private static List<String> segments(String path) {
		if (path == null) {
			return List.of();
		}
		return Arrays.stream(path.split(SLASH)).map(String::strip).filter(s -> !s.isEmpty()).toList();
	}

	private static String withSlash(String url) {
		return url.endsWith(SLASH) ? url : url + SLASH;
	}

	/**
	 * An answer of Jenkins.
	 *
	 * @param status
	 *        The HTTP status.
	 * @param headers
	 *        The headers.
	 * @param body
	 *        The body.
	 */
	private record Response(int status, Map<String, List<String>> headers, byte[] body) {

		/**
		 * The first value of the header of the given name, compared ignoring case.
		 */
		String header(String name) {
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(name) && !entry.getValue().isEmpty()) {
					return entry.getValue().get(0);
				}
			}
			return null;
		}
	}

	/**
	 * The end of a stream.
	 *
	 * @param bytes
	 *        At most the requested number of the last bytes of the stream.
	 * @param cut
	 *        Whether bytes before them were dropped.
	 */
	record Tail(byte[] bytes, boolean cut) {

		/**
		 * Reads the given stream, keeping at most its last given number of bytes.
		 */
		static Tail read(InputStream in, int max) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8 * 1024];
			boolean cut = false;
			int read;
			while ((read = in.read(chunk)) >= 0) {
				buffer.write(chunk, 0, read);
				if (buffer.size() > 2 * max) {
					byte[] all = buffer.toByteArray();
					buffer.reset();
					buffer.write(all, all.length - max, max);
					cut = true;
				}
			}
			byte[] all = buffer.toByteArray();
			if (all.length > max) {
				return new Tail(Arrays.copyOfRange(all, all.length - max, all.length), true);
			}
			return new Tail(all, cut);
		}
	}

}
