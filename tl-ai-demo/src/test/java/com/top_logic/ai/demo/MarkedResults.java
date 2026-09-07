/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Assert;

/**
 * Reads the disclosure out of a tool result that is marked as business data.
 *
 * <p>
 * A tool result that renders model data carries the untrusted-content envelope (#340), so the raw
 * result is the disclosure <em>plus</em> that framing. A test about what a tool disclosed reads the
 * disclosure out rather than also asserting the wording of the note - that the note is there at all
 * is asserted where the marking lives, in {@code TestReadFilter}.
 * </p>
 */
public final class MarkedResults {

	private MarkedResults() {
		// Static utility.
	}

	/**
	 * What the given tool result disclosed, without the envelope around it.
	 *
	 * @param toolResult
	 *        A tool result that is expected to be marked; the expectation is asserted.
	 */
	public static String disclosed(String toolResult) {
		// The note names the tag too, so the opening tag is found by its source attribute.
		int start = toolResult.indexOf('>', toolResult.indexOf("<untrusted_model_data source=\""));
		int end = toolResult.lastIndexOf("</untrusted_model_data>");
		Assert.assertTrue("Expected model data to be marked as untrusted, but found no envelope in: "
			+ toolResult, start >= 0 && end > start);
		return toolResult.substring(start + 1, end).trim();
	}

}
