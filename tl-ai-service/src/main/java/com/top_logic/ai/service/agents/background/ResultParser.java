/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.Locale;

import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger.FailPolicy;

/**
 * Parses raw LLM responses from background agents into structured {@link AgentResult} values.
 *
 * <p>
 * Two parsing modes are supported:
 * </p>
 * <ul>
 * <li><b>Authoritative</b> ({@link #parseAuthoritative(String, FailPolicy)}): For pre-turn agents
 * that can block a turn. Recognizes "PASS" and "BLOCK:" prefixes.</li>
 * <li><b>Advisory</b> ({@link #parseAdvisory(String)}): For post-turn agents that report issues but
 * cannot block. Recognizes "No issues found" as a pass signal.</li>
 * </ul>
 *
 * @see AgentResult
 */
public class ResultParser {

	private static final String PASS_PREFIX = "PASS";

	private static final String BLOCK_PREFIX = "BLOCK:";

	private static final String NO_ISSUES_PREFIX = "NO ISSUES FOUND";

	/**
	 * Parses an authoritative response from a pre-turn background agent.
	 *
	 * <p>
	 * Recognition rules (case-insensitive):
	 * </p>
	 * <ul>
	 * <li>Starts with "PASS" &rarr; {@link AgentResult#pass()}</li>
	 * <li>Starts with "BLOCK:" &rarr; {@link AgentResult#block(String, String)} with the text
	 * after the colon as reason</li>
	 * <li>Null, empty, or unrecognized &rarr; determined by the {@link FailPolicy}</li>
	 * </ul>
	 *
	 * @param response
	 *        The raw LLM response, may be {@code null}.
	 * @param failPolicy
	 *        The policy to apply when the response is unrecognized.
	 * @return A structured {@link AgentResult}, never {@code null}.
	 */
	public static AgentResult parseAuthoritative(String response, FailPolicy failPolicy) {
		if (response == null || response.isEmpty()) {
			return applyFailPolicy(failPolicy);
		}

		String upper = response.toUpperCase(Locale.ROOT);

		if (upper.startsWith(PASS_PREFIX)) {
			return AgentResult.pass();
		}

		if (upper.startsWith(BLOCK_PREFIX)) {
			String reason = response.substring(BLOCK_PREFIX.length()).trim();
			return AgentResult.block(reason, "");
		}

		return applyFailPolicy(failPolicy);
	}

	/**
	 * Parses an advisory response from a post-turn background agent.
	 *
	 * <p>
	 * Advisory agents can only produce {@link AgentResult.Status#PASS} or
	 * {@link AgentResult.Status#ISSUE_FOUND}; they never produce
	 * {@link AgentResult.Status#BLOCK}.
	 * </p>
	 *
	 * @param response
	 *        The raw LLM response, may be {@code null}.
	 * @return A structured {@link AgentResult}, never {@code null}.
	 */
	public static AgentResult parseAdvisory(String response) {
		if (response == null || response.isEmpty()) {
			return AgentResult.pass();
		}

		String upper = response.toUpperCase(Locale.ROOT);
		if (upper.startsWith(NO_ISSUES_PREFIX)) {
			return AgentResult.pass();
		}

		return AgentResult.issueFound(response);
	}

	private static AgentResult applyFailPolicy(FailPolicy failPolicy) {
		if (failPolicy == FailPolicy.CLOSED) {
			return AgentResult.block("Unrecognized response", "");
		}
		return AgentResult.pass();
	}
}
