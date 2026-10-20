/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.tl.ai.core.tools.web.BrowserToolsService.BlockSignal;

/**
 * Small, conservative matcher that recognises a bot-protection / access-denied "block page" from the
 * main navigation's HTTP status, response headers and rendered body, so the agent gets a distinct
 * "blocked" signal instead of an unexplained empty element list.
 *
 * <p>
 * <b>Why so few signatures.</b> A false positive is worse than a false negative here: mislabelling a
 * real page as "blocked" would mislead the model about content it can actually read. Every body
 * marker is therefore a multi-word phrase that does not occur on ordinary pages (never a bare word
 * such as {@code denied}), and the matcher is purely advisory - the page content is still rendered
 * alongside the signal, so an over-eager match degrades gracefully.
 * </p>
 *
 * <p>
 * The recognised signatures are:
 * </p>
 * <ul>
 * <li><b>HTTP 403</b> - the primary, status-based signal (Akamai/Cloudflare/WAF access-denied).</li>
 * <li><b>{@code cf-mitigated} response header</b> - Cloudflare's explicit "this response was a
 * challenge/block" marker.</li>
 * <li>Akamai/Apache <b>"you don't have permission to access"</b> and <b>"access denied"</b> bodies,
 * and the Akamai <b>"Reference #&lt;id&gt;"</b> error stamp.</li>
 * <li>Cloudflare <b>"Attention Required"</b> and <b>"checking your browser before accessing"</b>
 * interstitials.</li>
 * <li>Google-style <b>"unusual traffic"</b> bot-detection pages.</li>
 * </ul>
 *
 * <p>
 * This is detection and reporting only: nothing here retries, evades or works around a block.
 * Package-visible and browser-free so the signatures can be unit-tested without launching Chromium.
 * </p>
 */
final class BotWallDetector {

	/** A blocked-page detection with no HTTP status available (e.g. a re-snapshot of the same page). */
	static final int NO_STATUS = -1;

	/**
	 * Distinctive body phrases (already lower-cased) whose presence marks a bot-wall / access-denied
	 * page. Deliberately multi-word so they never fire on the bare word {@code denied}.
	 */
	private static final String[] BODY_MARKERS = {
		// Apache/Akamai 403 body ("You don't have permission to access ... on this server").
		"you don't have permission to access",
		// Generic WAF / Akamai access-denied title/heading.
		"access denied",
		// Cloudflare block/challenge page title ("Attention Required! | Cloudflare").
		"attention required",
		// Cloudflare "I'm Under Attack" interstitial.
		"checking your browser before accessing",
		// Google and others: "Our systems have detected unusual traffic from your computer network".
		"unusual traffic",
	};

	/** Human-readable reason paired with each {@link #BODY_MARKERS} entry (same index). */
	private static final String[] BODY_REASONS = {
		"Akamai/Apache access-denied page (\"You don't have permission to access\")",
		"\"Access Denied\" bot-wall page",
		"Cloudflare \"Attention Required\" block page",
		"Cloudflare \"Checking your browser\" challenge",
		"\"unusual traffic\" bot-detection page",
	};

	/** Akamai error-reference stamp, e.g. {@code Reference #18.abcd1234.1700000000.deadbeef}. */
	private static final Pattern AKAMAI_REFERENCE = Pattern.compile("reference\\s*#\\s*\\d");

	private BotWallDetector() {
		// Static helper.
	}

	/**
	 * Classifies a captured page as a bot-protection block or not.
	 *
	 * @param status
	 *        The main-document HTTP status, or {@link #NO_STATUS} if unknown (re-snapshot / in-page
	 *        interaction, where only the body can be inspected).
	 * @param headers
	 *        The main-document response headers (lower-cased keys), or {@code null} if unavailable.
	 * @param html
	 *        The fully rendered document HTML, or {@code null}.
	 * @return A {@link BlockSignal}; {@link BlockSignal#detected()} is {@code false} when nothing matched.
	 */
	static BlockSignal detect(int status, Map<String, String> headers, String html) {
		// Cloudflare sets `cf-mitigated: challenge` (or similar) on a challenged/blocked response -
		// an explicit, body-independent block marker.
		if (headers != null) {
			String cfMitigated = headers.get("cf-mitigated");
			if (cfMitigated != null && !cfMitigated.isBlank()) {
				return new BlockSignal(true, status,
					"Cloudflare challenge (cf-mitigated: " + cfMitigated.trim() + ")");
			}
		}

		String bodyReason = matchBody(html);
		if (bodyReason != null) {
			return new BlockSignal(true, status, bodyReason);
		}

		// Status-only fallback: a 403 with no recognisable body is still an access-denied response.
		if (status == 403) {
			return new BlockSignal(true, status, "the server returned an access-denied / forbidden response");
		}

		return new BlockSignal(false, status, null);
	}

	/** The reason for the first matching body signature, or {@code null} if the body looks normal. */
	private static String matchBody(String html) {
		if (html == null || html.isEmpty()) {
			return null;
		}
		String lower = html.toLowerCase(Locale.ROOT);
		for (int i = 0; i < BODY_MARKERS.length; i++) {
			if (lower.contains(BODY_MARKERS[i])) {
				return BODY_REASONS[i];
			}
		}
		if (AKAMAI_REFERENCE.matcher(lower).find()) {
			return "Akamai reference error page (\"Reference #...\")";
		}
		return null;
	}
}
