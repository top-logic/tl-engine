/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import com.top_logic.ai.service.agents.background.AgentResult.Status;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger.FailPolicy;

import junit.framework.TestCase;

/**
 * Tests for {@link ResultParser} covering both authoritative and advisory parsing modes.
 *
 * @see ResultParser#parseAuthoritative(String, FailPolicy)
 * @see ResultParser#parseAdvisory(String)
 */
public class TestResultParser extends TestCase {

	// --- Authoritative parsing tests ---

	/** "PASS" response yields a pass result. */
	public void testAuthoritativePass() {
		AgentResult result = ResultParser.parseAuthoritative("PASS", FailPolicy.OPEN);

		assertEquals(Status.PASS, result.status());
	}

	/** Parsing is case-insensitive: "pass" is recognized. */
	public void testAuthoritativePassCaseInsensitive() {
		AgentResult result = ResultParser.parseAuthoritative("pass", FailPolicy.OPEN);

		assertEquals(Status.PASS, result.status());
	}

	/** "BLOCK: contains PII" yields a block result with the reason. */
	public void testAuthoritativeBlock() {
		AgentResult result = ResultParser.parseAuthoritative("BLOCK: contains PII", FailPolicy.OPEN);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("contains PII", result.reason());
	}

	/** Leading/trailing whitespace in the reason is trimmed. */
	public void testAuthoritativeBlockTrimmed() {
		AgentResult result = ResultParser.parseAuthoritative("BLOCK:   spaces  ", FailPolicy.OPEN);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("spaces", result.reason());
	}

	/** Unrecognized response with OPEN fail policy yields PASS. */
	public void testAuthoritativeUnrecognizedFailOpen() {
		AgentResult result = ResultParser.parseAuthoritative("random text", FailPolicy.OPEN);

		assertEquals(Status.PASS, result.status());
	}

	/** Unrecognized response with CLOSED fail policy yields BLOCK. */
	public void testAuthoritativeUnrecognizedFailClosed() {
		AgentResult result = ResultParser.parseAuthoritative("random text", FailPolicy.CLOSED);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("Unrecognized response", result.reason());
	}

	/** Null response with OPEN fail policy yields PASS. */
	public void testAuthoritativeNullResponseFailOpen() {
		AgentResult result = ResultParser.parseAuthoritative(null, FailPolicy.OPEN);

		assertEquals(Status.PASS, result.status());
	}

	/** Null response with CLOSED fail policy yields BLOCK. */
	public void testAuthoritativeNullResponseFailClosed() {
		AgentResult result = ResultParser.parseAuthoritative(null, FailPolicy.CLOSED);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("Unrecognized response", result.reason());
	}

	// --- Advisory parsing tests ---

	/** "No issues found." yields PASS. */
	public void testAdvisoryNoIssues() {
		AgentResult result = ResultParser.parseAdvisory("No issues found.");

		assertEquals(Status.PASS, result.status());
	}

	/** Parsing is case-insensitive: "NO ISSUES FOUND" is recognized. */
	public void testAdvisoryNoIssuesCaseInsensitive() {
		AgentResult result = ResultParser.parseAdvisory("NO ISSUES FOUND");

		assertEquals(Status.PASS, result.status());
	}

	/** Empty string yields PASS. */
	public void testAdvisoryEmpty() {
		AgentResult result = ResultParser.parseAdvisory("");

		assertEquals(Status.PASS, result.status());
	}

	/** Null yields PASS. */
	public void testAdvisoryNull() {
		AgentResult result = ResultParser.parseAdvisory(null);

		assertEquals(Status.PASS, result.status());
	}

	/** A non-empty, non-"no issues" response yields ISSUE_FOUND with the response as message. */
	public void testAdvisoryIssueFound() {
		AgentResult result = ResultParser.parseAdvisory("Found 3 violations");

		assertEquals(Status.ISSUE_FOUND, result.status());
		assertEquals("Found 3 violations", result.message());
	}

	/** Advisory parsing never produces BLOCK status. */
	public void testAdvisoryCannotBlock() {
		// Even if the text mentions "BLOCK", advisory parsing only produces PASS or ISSUE_FOUND
		AgentResult result = ResultParser.parseAdvisory("BLOCK: something dangerous");

		assertFalse("Advisory parsing must never produce BLOCK status",
			Status.BLOCK == result.status());
		assertEquals(Status.ISSUE_FOUND, result.status());
	}
}
