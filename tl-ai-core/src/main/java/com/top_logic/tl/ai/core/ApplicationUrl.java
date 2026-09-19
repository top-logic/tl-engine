/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core;

import com.top_logic.basic.AliasManager;

/**
 * Builds a URL into this application for content a tool hands to the model.
 *
 * <p>
 * A URL that reaches the model ends up in the answer, usually as a Markdown link or image, so it
 * has to work in the reader's browser. Two aliases decide what that means:
 * </p>
 *
 * <ul>
 * <li>{@link AliasManager#HOST %HOST%} - the deployment's public origin. Configured in any real
 * deployment, unset in a unit test. An absolute URL is preferred whenever one can be built, because
 * some models otherwise invent a host for a root-relative path.</li>
 * <li>{@link AliasManager#APP_CONTEXT %APP_CONTEXT%} - where the WAR is mounted, so the URL works
 * under {@code /tl-ai-demo/...} as well as under {@code /hype/...}.</li>
 * </ul>
 *
 * <p>
 * With neither configured the result is root-relative ({@code /screenshot/a7f3...}). That form is
 * deliberate and survives the Markdown sanitizer: {@code HtmlSanitizer} resolves a relative URL
 * against a fixed {@code https} base for its scheme check and then writes it back unchanged.
 * </p>
 */
public class ApplicationUrl {

	/**
	 * The absolute or root-relative URL of the given application-local path.
	 *
	 * @param path
	 *        The path below the application's context path, starting with {@code "/"}, e.g.
	 *        {@code "/screenshot/a7f3b2..."}.
	 * @return The same path prefixed with the configured host and context path, as far as either is
	 *         known.
	 */
	public static String of(String path) {
		return hostPrefix() + contextPath() + path;
	}

	/**
	 * The application's public origin (scheme, host, optional port), or the empty string when the
	 * deployment declares none.
	 *
	 * <p>
	 * Trailing slashes are stripped so callers need not care whether {@code %HOST%} ended with one.
	 * </p>
	 */
	public static String hostPrefix() {
		String host = AliasManager.getInstance().getAlias(AliasManager.HOST);
		if (host == null) {
			return "";
		}
		host = host.trim();
		while (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		return host;
	}

	/**
	 * Where the WAR is mounted, or the empty string when the alias is unresolved (a unit test).
	 *
	 * @see AliasManager#APP_CONTEXT
	 */
	public static String contextPath() {
		String path = AliasManager.getInstance().getAlias(AliasManager.APP_CONTEXT);
		return path == null ? "" : path;
	}

	private ApplicationUrl() {
		// Static utility.
	}

}
