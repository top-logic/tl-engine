/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse;

/**
 * Tests where {@link ToolResults} puts a caller's framing, which decides what the marking on a
 * model-data result actually claims.
 */
public class TestToolResults extends TestCase {

	/** The success payload is what the caller framed, and it arrives framed. */
	public void testSuccessResponseIsFramed() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> ReadFilter.marked("pm:Project", payload),
			() -> listing());

		assertTrue("the framing is applied: " + json,
			json.contains("<untrusted_model_data source=\"pm:Project\">"));
		assertTrue("and the payload survives it", json.contains("\"module\":\"pm\""));
	}

	/**
	 * The <b>error</b> response is not framed. It is this application's own sentence about a call that
	 * failed - marking it as business data somebody else wrote would be untrue, and would spend the
	 * standing note on a result that discloses nothing.
	 */
	public void testErrorResponseIsNotFramed() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> ReadFilter.marked("pm:Project", payload),
			() -> {
				throw new IllegalArgumentException("Type 'Nope' not found in module 'pm'");
			});

		assertFalse("no envelope belongs around a refusal: " + json, json.contains("untrusted_"));
		assertTrue("and the reason is still reported", json.contains("Type 'Nope' not found"));
	}

	/** A framing that throws is a failed call, not an excuse to hand the model an unframed payload. */
	public void testAFailingFramingCannotLeakAnUnframedPayload() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ",
			payload -> {
				throw new IllegalStateException("framing broke");
			},
			() -> listing());

		assertFalse("the unframed payload must not reach the model: " + json,
			json.contains("\"module\""));
		assertTrue(json, json.contains("framing broke"));
	}

	/** The plain form frames nothing, which is what a tool disclosing no model data wants. */
	public void testUnframedInvokeIsUnchanged() {
		String json = ToolResults.invoke(TestToolResults.class, "listInstances()", Map.of(),
			"Error accessing instances: ", () -> listing());

		assertFalse(json, json.contains("untrusted_"));
		assertTrue(json, json.contains("\"module\":\"pm\""));
	}

	private static InstanceListResponse listing() {
		return InstanceListResponse.create().setModule("pm").setType("Project");
	}
}
