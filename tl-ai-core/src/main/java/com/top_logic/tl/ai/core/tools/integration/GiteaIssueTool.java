/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.integration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import com.top_logic.tl.ai.core.tools.integration.messages.CreateIssueRequest;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;
import com.top_logic.basic.Logger;

import com.top_logic.tl.ai.tool.api.P;
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
 * Connection configuration is read from environment variables (with system property fallback):
 * {@code tl_gitea_url}, {@code tl_gitea_token}, {@code tl_gitea_repo_owner},
 * {@code tl_gitea_repo_name}.
 * </p>
 */
public class GiteaIssueTool {

	private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(HTTP_TIMEOUT)
		.build();

	/**
	 * Creates a new {@link GiteaIssueTool}.
	 */
	public GiteaIssueTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
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
	@ToolMethod(name = "create_gitea_issue",
		description = "Create a Gitea issue to report a quality problem found during AI conversation review. "
			+ "Use this to file issues for incorrect answers, bad delegation, tool misuse, "
			+ "unhelpful responses, or inefficiencies.",
		mode = ToolMode.USER_SCOPED)
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
		String giteaToken = getConfig("tl_gitea_token");
		String repoOwner = getConfig("tl_gitea_repo_owner");
		String repoName = getConfig("tl_gitea_repo_name");

		if (giteaUrl == null || giteaUrl.isEmpty()) {
			return "Error: Gitea URL not configured. Set tl_gitea_url environment variable.";
		}
		if (giteaToken == null || giteaToken.isEmpty()) {
			return "Error: Gitea token not configured. Set tl_gitea_token environment variable.";
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
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(apiUrl))
				.header("Content-Type", "application/json")
				.header("Authorization", "token " + giteaToken)
				.timeout(HTTP_TIMEOUT)
				.POST(BodyPublishers.ofString(jsonPayload))
				.build();

			HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

			int statusCode = response.statusCode();
			if (statusCode == 201) {
				Logger.info("Successfully created Gitea issue: " + title, GiteaIssueTool.class);
				return "Issue created successfully: " + title;
			} else {
				String errorMsg = "Failed to create Gitea issue. HTTP " + statusCode + ": " + response.body();
				Logger.error(errorMsg, GiteaIssueTool.class);
				return "Error: " + errorMsg;
			}
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
