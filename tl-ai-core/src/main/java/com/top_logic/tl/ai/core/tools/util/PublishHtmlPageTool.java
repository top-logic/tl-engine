/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.util;

import java.nio.charset.StandardCharsets;

import com.top_logic.basic.AliasManager;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.htmlpages.GeneratedHtmlPageService;
import com.top_logic.tl.ai.htmlpages.GeneratedHtmlPage;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContext;

/**
 * Tool that persists an HTML document and returns a URL the user can open in a browser.
 *
 * <p>
 * The returned URL is served by
 * {@link com.top_logic.tl.ai.core.htmlpages.GeneratedHtmlPageServlet} mapped to {@code /html/*}.
 * </p>
 *
 * <p>
 * The URL is absolute when {@link AliasManager#HOST %HOST%} is configured (typical for any real
 * deployment), and root-relative otherwise. Returning an absolute URL whenever possible matters
 * because some LLMs will otherwise invent a host themselves when handed a root-relative path.
 * </p>
 */
public class PublishHtmlPageTool {

	private static final String URL_PREFIX = "/html/";

	/**
	 * Required public no-arg constructor for {@code ReflectiveToolFactory}.
	 */
	public PublishHtmlPageTool() {
		// no state
	}

	/**
	 * Persists the given HTML and returns the URL of the freshly stored page.
	 *
	 * @param title
	 *        Short human-readable title.
	 * @param html
	 *        Full self-contained HTML document.
	 * @return The URL of the stored page. Absolute when {@link AliasManager#HOST %HOST%} is
	 *         configured (e.g. {@code https://apps.example.com/hype/html/a7f3b2...}), root-relative
	 *         otherwise (e.g. {@code /hype/html/a7f3b2...}).
	 */
	@ToolMethod(
		name = "publishPage",
		description = "Publishes an HTML page and returns a URL. After generating the full HTML "
			+ "document following the chosen design skill's spec, call this tool. Embed the "
			+ "returned URL in your reply as a markdown link.",
		mode = ToolMode.USER_SCOPED, group = ToolGroups.WORKSPACE)
	public String publishPage(
			@P("Short human-readable title for the page. Used in the admin list and as default link text. "
				+ "If the HTML omits <title>, browsers may show this in the tab.") String title,
			@P("Full self-contained HTML document. Must start with <!doctype html> and include "
				+ "<html>, <head>, <body>. Embed CSS inline via <style> tags.") String html) {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("title must be a non-blank string");
		}
		if (html == null || html.isEmpty()) {
			throw new IllegalArgumentException("html must be a non-empty HTML document");
		}
		if (!html.contains("<")) {
			throw new IllegalArgumentException("html does not look like an HTML document (no '<' found)");
		}

		byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
		GeneratedHtmlPageService service =
			GeneratedHtmlPageService.Module.INSTANCE.getImplementationInstance();
		GeneratedHtmlPage page = service.savePage(title, bytes, currentPerson());
		return hostPrefix() + contextPath() + URL_PREFIX + page.getId();
	}

	/**
	 * Resolves the application's public host (scheme + host + optional port) so the returned URL
	 * is absolute when the deployment declares one.
	 *
	 * <p>
	 * Reads the {@code %HOST%} alias. Returns the empty string when unset (e.g. unit tests, or a
	 * dev deployment that hasn't bothered to configure it) — yielding a root-relative URL, same
	 * as before the host prefix was introduced.
	 * </p>
	 *
	 * <p>
	 * Trailing slashes on the configured host are stripped so callers don't have to be careful
	 * about whether they ended {@code %HOST%} with one.
	 * </p>
	 */
	private static String hostPrefix() {
		String host = AliasManager.getInstance().getAlias(AliasManager.HOST);
		if (host == null) {
			return "";
		}
		host = host.trim();
		if (host.isEmpty()) {
			return "";
		}
		while (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		return host;
	}

	/**
	 * Resolves the application's deployment context path so the returned URL works regardless of
	 * where the WAR is mounted (e.g. {@code /tl-ai-demo/html/...} vs. {@code /hype/html/...}).
	 *
	 * <p>
	 * Reads the {@code %APP_CONTEXT%} alias maintained by {@link AliasManager}, which is
	 * populated at startup from {@code ServletContext.getContextPath()}. Returns the empty
	 * string when the alias is unresolved (e.g. unit tests) — yielding a root-relative URL.
	 * </p>
	 */
	private static String contextPath() {
		String path = AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT);
		return path == null ? "" : path;
	}

	private static Person currentPerson() {
		TLContext context = TLContext.getContext();
		if (context == null) {
			return null;
		}
		return context.getPerson();
	}
}
