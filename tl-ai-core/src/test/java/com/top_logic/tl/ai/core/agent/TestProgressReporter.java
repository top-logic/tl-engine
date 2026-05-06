/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.ProgressReporter;

/**
 * Tests for {@link ProgressReporter}.
 */
public class TestProgressReporter extends TestCase {

	/**
	 * {@link ProgressReporter#NOOP} does not throw on any input.
	 */
	public void testNoopDoesNotThrow() {
		ProgressReporter.NOOP.progress("step 1");
		ProgressReporter.NOOP.progress("");
		ProgressReporter.NOOP.progress(null);
	}

	/**
	 * A custom reporter receives all messages in order.
	 */
	public void testCustomReporter() {
		List<String> captured = new ArrayList<>();
		ProgressReporter reporter = captured::add;

		reporter.progress("step 1");
		reporter.progress("step 2");
		reporter.progress("done");

		assertEquals(List.of("step 1", "step 2", "done"), captured);
	}
}
