/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.integration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.CancellationException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.tools.integration.messages.CreateIssueRequest;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for creating issues in a Gitea repository via REST API.
 *
 * <p>
 * This tool is used by the Review Agent to report quality issues found during conversation
 * analysis. Issues are created with structured Markdown bodies containing category, severity,
 * description, and optional suggested fix information.
 * </p>
 *
 * <p>
 * The repository is named by environment variables (with system property fallback):
 * {@code tl_gitea_url}, {@code tl_gitea_repo_owner}, {@code tl_gitea_repo_name}. The requests are
 * authenticated by the configured {@link Config#getCredentials() credentials}; without them, with
 * the access token in the variable {@link #TOKEN_VARIABLE}, sent with the scheme
 * {@link #TOKEN_SCHEME}. Credentials from another source are configured with the
 * {@link com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory}:
 * </p>
 *
 * <pre>
 * &lt;implementation class="com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory"&gt;
 *   &lt;tool class="com.top_logic.tl.ai.core.tools.integration.GiteaIssueTool"&gt;
 *     &lt;credentials class="com.top_logic.basic.credential.HeaderCredentials" scheme="token"&gt;
 *       &lt;secret class="com.top_logic.basic.credential.FileSecretSource" path="/run/secrets/gitea-token"/&gt;
 *     &lt;/credentials&gt;
 *   &lt;/tool&gt;
 * &lt;/implementation&gt;
 * </pre>
 *
 * <p>
 * The tool is {@link ToolMode#EXTERNAL}: it creates a shared, publicly visible issue in another
 * system, and nothing here can take it back. It declared {@link ToolMode#USER_SCOPED} before there
 * was a mode for that - "changes scoped to the current user only" was untrue of it, and was said in
 * order to obtain the approval prompt that mode happens to carry. Now the true classification and
 * the prompt come from the same statement.
 * </p>
 */
public class GiteaIssueTool {

	/**
	 * Configuration options for {@link GiteaIssueTool}.
	 */
	public interface Config extends PolymorphicConfiguration<GiteaIssueTool> {

		/** Configuration property name for {@link #getCredentials()}. */
		String CREDENTIALS = "credentials";

		/**
		 * How the requests to Gitea are authenticated.
		 *
		 * <p>
		 * If not set, with the access token in the variable {@link GiteaIssueTool#TOKEN_VARIABLE},
		 * sent with the scheme {@link GiteaIssueTool#TOKEN_SCHEME}.
		 * </p>
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

	}

	/**
	 * Name of the system property or environment variable holding the Gitea access token, if no
	 * {@link Config#getCredentials() credentials} are configured.
	 */
	public static final String TOKEN_VARIABLE = "tl_gitea_token";

	/**
	 * Authentication scheme of a Gitea access token in the {@link CredentialProvider#AUTHORIZATION}
	 * header.
	 */
	public static final String TOKEN_SCHEME = "token";

	private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(HTTP_TIMEOUT)
		.build();

	private final CredentialProvider _credentials;

	/**
	 * Creates a {@link GiteaIssueTool} authenticated with the access token in
	 * {@link #TOKEN_VARIABLE}.
	 */
	public GiteaIssueTool() {
		this(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			TypedConfiguration.newConfigItem(Config.class));
	}

	/**
	 * Creates a {@link GiteaIssueTool} from configuration.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GiteaIssueTool(InstantiationContext context, Config config) {
		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials == null) {
			credentials = defaultCredentials();
		}
		_credentials = context.getInstance(credentials);
	}

	/**
	 * The credentials sending the access token in {@link #TOKEN_VARIABLE}.
	 */
	private static PolymorphicConfiguration<? extends CredentialProvider> defaultCredentials() {
		EnvironmentSecretSource.Config token = TypedConfiguration.newConfigItem(EnvironmentSecretSource.Config.class);
		token.setName(TOKEN_VARIABLE);
		return HeaderCredentials.config(CredentialProvider.AUTHORIZATION, TOKEN_SCHEME, token);
	}

	/**
	 * Creates an issue in the configured Gitea repository.
	 *
	 * <p>
	 * The issue body is formatted as a Markdown AI Review Report containing the category, severity,
	 * description, and optional suggested fix.
	 * </p>
	 *
	 * @param title
	 *        The issue title.
	 * @param category
	 *        The category of the quality issue. One of: incorrect-answer, bad-delegation,
	 *        tool-misuse, unhelpful-response, inefficiency.
	 * @param severity
	 *        The severity level. One of: low, medium, high.
	 * @param description
	 *        A detailed description of the quality issue.
	 * @param suggestedFix
	 *        An optional suggestion for how to fix the issue. May be {@code null}.
	 * @return A confirmation message on success, or an error description on failure.
	 */
	@ToolMethod(
		description = "Create a Gitea issue to report a quality problem found during AI conversation review. "
			+ "Use this to file issues for incorrect answers, bad delegation, tool misuse, "
			+ "unhelpful responses, or inefficiencies.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.EGRESS)
	public String createGiteaIssue(
			@P("The issue title") String title,
			@P("Category of the issue: incorrect-answer, bad-delegation, tool-misuse, "
				+ "unhelpful-response, or inefficiency") String category,
			@P("Severity level: low, medium, or high") String severity,
			@P("Detailed description of the quality issue") String description,
			@P("Optional suggestion for how to fix the issue") String suggestedFix) {

		// Validate required parameters.
		if (title == null || title.trim().isEmpty()) {
			return "Error: title is required.";
		}
		if (category == null || category.trim().isEmpty()) {
			return "Error: category is required.";
		}
		if (severity == null || severity.trim().isEmpty()) {
			return "Error: severity is required.";
		}
		if (description == null || description.trim().isEmpty()) {
			return "Error: description is required.";
		}

		// Read connection configuration.
		String giteaUrl = getConfig("tl_gitea_url");
		String repoOwner = getConfig("tl_gitea_repo_owner");
		String repoName = getConfig("tl_gitea_repo_name");

		if (giteaUrl == null || giteaUrl.isEmpty()) {
			return "Error: Gitea URL not configured. Set tl_gitea_url environment variable.";
		}
		if (repoOwner == null || repoOwner.isEmpty()) {
			return "Error: Gitea repo owner not configured. Set tl_gitea_repo_owner environment variable.";
		}
		if (repoName == null || repoName.isEmpty()) {
			return "Error: Gitea repo name not configured. Set tl_gitea_repo_name environment variable.";
		}

		// Build issue body in Markdown format.
		String issueBody = buildIssueBody(category, severity, description, suggestedFix);

		// Build JSON payload using msgbuf.
		CreateIssueRequest issueRequest = CreateIssueRequest.create();
		issueRequest.setTitle(title);
		issueRequest.setBody(issueBody);
		String jsonPayload = DataClassSerializer.toJson(issueRequest, GiteaIssueTool.class, "createGiteaIssue()");

		// Build API URL.
		String apiUrl = giteaUrl;
		if (apiUrl.endsWith("/")) {
			apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
		}
		apiUrl = apiUrl + "/api/v1/repos/"
			+ java.net.URLEncoder.encode(repoOwner, java.nio.charset.StandardCharsets.UTF_8) + "/"
			+ java.net.URLEncoder.encode(repoName, java.nio.charset.StandardCharsets.UTF_8) + "/issues";

		try {
			HttpRequest.Builder request = HttpRequest.newBuilder()
				.uri(URI.create(apiUrl))
				.header("Content-Type", "application/json")
				.timeout(HTTP_TIMEOUT)
				.POST(BodyPublishers.ofString(jsonPayload));

			HttpResponse<String> response = send(request);
			if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// Cached credentials (an access token) may have expired or been revoked: one more
				// attempt with fresh ones.
				_credentials.invalidate();
				response = send(request);
				if (response.statusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					_credentials.invalidate();
				}
			}

			int statusCode = response.statusCode();
			if (statusCode == 201) {
				Logger.info("Successfully created Gitea issue: " + title, GiteaIssueTool.class);
				return "Issue created successfully: " + title;
			} else {
				String errorMsg = "Failed to create Gitea issue. HTTP " + statusCode + ": " + response.body();
				Logger.error(errorMsg, GiteaIssueTool.class);
				return "Error: " + errorMsg;
			}
		} catch (I18NRuntimeException ex) {
			String errorMsg = "No Gitea credentials available: " + ex.getMessage();
			Logger.error(errorMsg, GiteaIssueTool.class);
			return "Error: " + errorMsg;
		} catch (CancellationException ex) {
			return "Error: creating the Gitea issue was cancelled; it may have been created anyway.";
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			String errorMsg = "Interrupted while creating Gitea issue: " + ex.getMessage();
			Logger.error(errorMsg, ex, GiteaIssueTool.class);
			return "Error: " + errorMsg;
		} catch (Exception ex) {
			String errorMsg = "Failed to create Gitea issue: " + ex.getMessage();
			Logger.error(errorMsg, ex, GiteaIssueTool.class);
			return "Error: " + errorMsg;
		}
	}

	/**
	 * Sends a copy of the given request with the current credentials.
	 */
	private HttpResponse<String> send(HttpRequest.Builder request) throws IOException, InterruptedException {
		HttpRequest.Builder authenticated = request.copy();
		_credentials.authenticate(RequestAuthentication.of(authenticated));
		HttpRequest httpRequest = authenticated.build();
		// Stopping the run ends the wait for the server.
		return RunCancellationBinding.whileBlocked(() -> HTTP_CLIENT.send(httpRequest, BodyHandlers.ofString()));
	}

	/**
	 * Builds the Markdown body for the Gitea issue.
	 */
	private String buildIssueBody(String category, String severity, String description, String suggestedFix) {
		StringBuilder body = new StringBuilder();
		body.append("## AI Review Report\n\n");
		body.append("**Category:** ").append(category).append("\n\n");
		body.append("**Severity:** ").append(severity).append("\n\n");
		body.append("### Description\n\n");
		body.append(description).append("\n");

		if (suggestedFix != null && !suggestedFix.trim().isEmpty()) {
			body.append("\n### Suggested Fix\n\n");
			body.append(suggestedFix).append("\n");
		}

		return body.toString();
	}

	/**
	 * Reads a configuration value from environment variables, falling back to system properties.
	 *
	 * @param key
	 *        The configuration key to look up.
	 * @return The configuration value, or {@code null} if not found.
	 */
	private String getConfig(String key) {
		String value = System.getenv(key);
		if (value != null && !value.isEmpty()) {
			return value;
		}
		return System.getProperty(key);
	}
}
