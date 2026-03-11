/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import java.util.Map;

import com.top_logic.ai.service.agents.background.AgentResult.Status;

import junit.framework.TestCase;

/**
 * Tests for {@link AgentResult} factory methods, template handling, and copy semantics.
 *
 * @see AgentResult
 */
public class TestAgentResult extends TestCase {

	// --- pass() factory ---

	/** {@link AgentResult#pass()} returns PASS with empty message, reason, and details. */
	public void testPassFactory() {
		AgentResult result = AgentResult.pass();

		assertEquals(Status.PASS, result.status());
		assertEquals("", result.message());
		assertEquals("", result.reason());
		assertTrue(result.details().isEmpty());
	}

	// --- issueFound() factory ---

	/** {@link AgentResult#issueFound(String)} returns ISSUE_FOUND with the given message. */
	public void testIssueFoundFactory() {
		AgentResult result = AgentResult.issueFound("Found 3 violations");

		assertEquals(Status.ISSUE_FOUND, result.status());
		assertEquals("Found 3 violations", result.message());
		assertEquals("", result.reason());
		assertTrue(result.details().isEmpty());
	}

	/** {@link AgentResult#issueFound(String)} with empty message. */
	public void testIssueFoundEmptyMessage() {
		AgentResult result = AgentResult.issueFound("");

		assertEquals(Status.ISSUE_FOUND, result.status());
		assertEquals("", result.message());
	}

	// --- block() factory ---

	/** {@link AgentResult#block(String, String)} with a template containing {reason}. */
	public void testBlockWithTemplate() {
		AgentResult result = AgentResult.block("contains PII", "Blocked because: {reason}");

		assertEquals(Status.BLOCK, result.status());
		assertEquals("Blocked because: contains PII", result.message());
		assertEquals("contains PII", result.reason());
		assertTrue(result.details().isEmpty());
	}

	/** {@link AgentResult#block(String, String)} with empty template uses reason as message. */
	public void testBlockWithEmptyTemplate() {
		AgentResult result = AgentResult.block("safety violation", "");

		assertEquals(Status.BLOCK, result.status());
		assertEquals("safety violation", result.message());
		assertEquals("safety violation", result.reason());
	}

	/** {@link AgentResult#block(String, String)} with null template uses reason as message. */
	public void testBlockWithNullTemplate() {
		AgentResult result = AgentResult.block("unsafe content", null);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("unsafe content", result.message());
		assertEquals("unsafe content", result.reason());
	}

	/** {@link AgentResult#block(String, String)} with null reason. */
	public void testBlockWithNullReason() {
		AgentResult result = AgentResult.block(null, "Blocked: {reason}");

		assertEquals(Status.BLOCK, result.status());
		assertEquals("Blocked: ", result.message());
		assertEquals("", result.reason());
	}

	/** {@link AgentResult#block(String, String)} with both null. */
	public void testBlockWithBothNull() {
		AgentResult result = AgentResult.block(null, null);

		assertEquals(Status.BLOCK, result.status());
		assertEquals("", result.message());
		assertEquals("", result.reason());
	}

	/** Template without {reason} placeholder returns template as-is. */
	public void testBlockTemplateWithoutPlaceholder() {
		AgentResult result = AgentResult.block("some reason", "Action was blocked.");

		assertEquals(Status.BLOCK, result.status());
		assertEquals("Action was blocked.", result.message());
		assertEquals("some reason", result.reason());
	}

	// --- withBlockMessage() ---

	/** {@link AgentResult#withBlockMessage(String)} copies result with a new message. */
	public void testWithBlockMessage() {
		AgentResult original = AgentResult.block("reason", "original message");
		AgentResult copy = original.withBlockMessage("new message");

		// New message applied
		assertEquals("new message", copy.message());
		// Other fields preserved
		assertEquals(original.status(), copy.status());
		assertEquals(original.reason(), copy.reason());
		assertEquals(original.details(), copy.details());
		// Original unchanged
		assertEquals("original message", original.message());
	}

	/** {@link AgentResult#withBlockMessage(String)} works on non-block results too. */
	public void testWithBlockMessageOnPass() {
		AgentResult pass = AgentResult.pass();
		AgentResult copy = pass.withBlockMessage("override");

		assertEquals(Status.PASS, copy.status());
		assertEquals("override", copy.message());
		assertEquals("", copy.reason());
	}

	// --- Record equality ---

	/** Two pass results are equal. */
	public void testPassEquality() {
		assertEquals(AgentResult.pass(), AgentResult.pass());
	}

	/** Results with different statuses are not equal. */
	public void testDifferentStatusNotEqual() {
		assertFalse(AgentResult.pass().equals(AgentResult.issueFound("")));
	}

	// --- details() immutability ---

	/** The details map from factory methods is immutable. */
	public void testDetailsImmutable() {
		AgentResult result = AgentResult.pass();
		Map<String, Object> details = result.details();

		try {
			details.put("key", "value");
			fail("Expected UnsupportedOperationException for immutable map");
		} catch (UnsupportedOperationException expected) {
			// OK — Map.of() is immutable
		}
	}
}
