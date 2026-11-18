/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.browser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.core.tools.web.UrlProvenance;
import com.top_logic.tl.ai.tool.api.RunInfo;

/**
 * Tests that {@link BrowserTool} follows the {@link UrlProvenance} rule before it starts a browser:
 * a run the rule applies to opens no address it was not given.
 *
 * <p>
 * Needs no Chromium: the refusal comes before a session is opened. What a running browser does with
 * the links and forms of a page is {@link TestBrowserUrlProvenance}.
 * </p>
 *
 * @see com.top_logic.tl.ai.core.tools.web.TestUrlProvenance
 */
public class TestBrowserToolUrlProvenance extends TestCase {

	/** The tool resolves the acting sub-session, so these runs need the module system up. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestBrowserToolUrlProvenance.class)));
	}

	/** The browser opens no address the run was not given, and needs no browser to say so. */
	public void testTheBrowserRefusesAnUnseenAddress() {
		try (RunInfoBinding.Binding run = RunInfoBinding.bind(workerRun());
				ReadingListBinding.Binding reading = ReadingListBinding.bind(new ReadingList())) {
			String answer = new BrowserTool().browserNavigate("https://collector.example/log?d=1", null);

			assertTrue(answer, answer.startsWith("Error: this run may open only"));
		}
	}

	/** A subagent's run: known, one level below a top-level run. */
	private static RunInfo workerRun() {
		RunInfo lead = new RunInfo("lead", null, "lead", null, null, null, 0, null);
		return new RunInfo("worker", null, "worker", null, null, null, 1, lead);
	}
}
