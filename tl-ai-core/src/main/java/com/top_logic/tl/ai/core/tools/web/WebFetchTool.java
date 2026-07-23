/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool that fetches a single web page and returns its main textual content.
 *
 * <p>
 * Includes SSRF protection (no internal/private hosts, http(s) only, no credentials in URL),
 * manual redirect handling with per-hop re-validation, and byte/character caps.
 * </p>
 */
public class WebFetchTool {

	private static final int MAX_REDIRECTS = 5;

	private static final String USER_AGENT = "TopLogic-AI-Agent/1.0 (+https://www.top-logic.com)";

	private static final String JUNK_SELECTOR =
		"script,style,noscript,iframe,svg,head,nav,footer,header,form,button,input,select,template";

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(15))
		.followRedirects(HttpClient.Redirect.NEVER)
		.build();

	/**
	 * Creates a {@link WebFetchTool}.
	 */
	public WebFetchTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
	}

	/**
	 * Fetches a single page and returns its extracted text.
	 *
	 * @param url
	 *        The absolute http(s) URL to fetch.
	 * @return The extracted text, or an error string.
	 */
	@ToolMethod(
		description = "Fetch a single web page by its URL and return the main content as Markdown, "
			+ "preserving headings, links, lists and tables. "
			+ "Use after webSearch to read a specific result, or to read a URL the user provided. "
			+ "Only public http(s) pages are supported.",
		mode = ToolMode.READ)
	public String webFetch(@P("The absolute http or https URL to fetch.") String url) {
		if (url == null || url.isBlank()) {
			return "Error: url is required.";
		}

		WebToolsService service = WebToolsService.getInstance();
		// Clamp to safe minimums so a misconfigured (zero/negative) limit cannot break the fetch.
		int timeoutSeconds = Math.max(1, service != null ? service.getFetchTimeoutSeconds() : 30);
		int maxBytes = Math.max(1024, service != null ? service.getFetchMaxBytes() : 5_000_000);
		int maxChars = Math.max(256, service != null ? service.getFetchMaxChars() : 50_000);

		String currentUrl = url.trim();
		try {
			for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
				URI uri;
				try {
					uri = URI.create(currentUrl);
				} catch (IllegalArgumentException ex) {
					return "Error: invalid URL: " + ex.getMessage();
				}
				String validationError = validateUrl(uri);
				if (validationError != null) {
					return "Error: " + validationError;
				}

				HttpRequest request = HttpRequest.newBuilder(uri)
					.header("User-Agent", USER_AGENT)
					.header("Accept", "text/html,application/xhtml+xml,text/plain;q=0.9,*/*;q=0.8")
					.timeout(Duration.ofSeconds(timeoutSeconds))
					.GET()
					.build();

				HttpResponse<InputStream> response = HTTP_CLIENT.send(request, BodyHandlers.ofInputStream());
				int status = response.statusCode();

				if (status >= 300 && status < 400) {
					try (InputStream body = response.body()) {
						body.readAllBytes();
					}
					Optional<String> location = response.headers().firstValue("location");
					if (location.isEmpty()) {
						return "Error: redirect (HTTP " + status + ") without a Location header.";
					}
					currentUrl = uri.resolve(location.get()).toString();
					continue;
				}
				if (status / 100 != 2) {
					try (InputStream body = response.body()) {
						body.readAllBytes();
					}
					return "Error: fetch failed (HTTP " + status + ") for " + currentUrl;
				}

				String contentType =
					response.headers().firstValue("content-type").orElse("").toLowerCase();
				byte[] bytes = readLimited(response.body(), maxBytes);
				String body = new String(bytes, charsetOf(contentType));

				String text;
				if (contentType.contains("html") || contentType.contains("xml")
						|| (contentType.isEmpty() && looksLikeHtml(body))) {
					text = htmlToMarkdown(body, currentUrl);
				} else if (contentType.startsWith("text/") || contentType.contains("json")
						|| contentType.isEmpty()) {
					text = body;
				} else {
					return "Fetched " + currentUrl + " (" + contentType
						+ "), but it is not a text or HTML document; cannot extract text.";
				}

				text = text.strip();
				boolean truncated = false;
				if (text.length() > maxChars) {
					text = text.substring(0, maxChars);
					truncated = true;
				}
				StringBuilder out = new StringBuilder();
				out.append("Content of ").append(currentUrl).append(":\n\n").append(text);
				if (truncated) {
					out.append("\n\n[Content truncated at ").append(maxChars).append(" characters.]");
				}
				return out.toString();
			}
			return "Error: too many redirects (more than " + MAX_REDIRECTS + ") starting from " + url;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "Error: web fetch was interrupted.";
		} catch (Exception ex) {
			Logger.error("Web fetch failed: " + ex.getMessage(), ex, WebFetchTool.class);
			return "Error: web fetch failed: " + ex.getMessage();
		}
	}

	/**
	 * Validates a URL for fetching. Returns {@code null} if acceptable, otherwise a human-readable
	 * reason for rejection. Package-visible for testing.
	 *
	 * @see UrlSafety#validateUrl(URI)
	 */
	static String validateUrl(URI uri) {
		return UrlSafety.validateUrl(uri);
	}

	private static boolean looksLikeHtml(String body) {
		String head = (body.length() > 512 ? body.substring(0, 512) : body).toLowerCase();
		return head.contains("<html") || head.contains("<!doctype html") || head.contains("<body")
			|| head.contains("<div") || head.contains("<p>");
	}

	/**
	 * Converts an HTML document to Markdown after stripping non-content chrome (scripts, styles,
	 * navigation, etc.). Relative links and image sources are resolved against {@code baseUrl} so
	 * the resulting Markdown contains usable absolute URLs. Package-visible for testing.
	 */
	static String htmlToMarkdown(String html, String baseUrl) {
		Document document = Jsoup.parse(html, baseUrl);
		document.select(JUNK_SELECTOR).remove();
		Element root = document.body() != null ? document.body() : document;

		// Inline the resolved absolute URLs so links survive the conversion to Markdown.
		for (Element link : root.select("a[href]")) {
			String absolute = link.absUrl("href");
			if (!absolute.isEmpty()) {
				link.attr("href", absolute);
			}
		}
		for (Element image : root.select("img[src]")) {
			String absolute = image.absUrl("src");
			if (!absolute.isEmpty()) {
				image.attr("src", absolute);
			}
		}

		String markdown = Converters.htmlToMarkdown(root.html());
		return markdown.replaceAll("\\n{3,}", "\n\n").strip();
	}

	private static byte[] readLimited(InputStream in, int maxBytes) throws IOException {
		try (in) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int total = 0;
			int read;
			while ((read = in.read(chunk)) != -1) {
				if (total + read > maxBytes) {
					buffer.write(chunk, 0, maxBytes - total);
					break;
				}
				buffer.write(chunk, 0, read);
				total += read;
			}
			return buffer.toByteArray();
		}
	}

	private static Charset charsetOf(String contentType) {
		int idx = contentType.indexOf("charset=");
		if (idx >= 0) {
			String name = contentType.substring(idx + "charset=".length()).trim();
			int semi = name.indexOf(';');
			if (semi >= 0) {
				name = name.substring(0, semi).trim();
			}
			name = name.replace("\"", "");
			try {
				if (!name.isEmpty() && Charset.isSupported(name)) {
					return Charset.forName(name);
				}
			} catch (Exception ex) {
				// Fall through to default.
			}
		}
		return StandardCharsets.UTF_8;
	}
}
