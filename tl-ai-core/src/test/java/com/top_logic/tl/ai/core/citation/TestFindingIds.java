/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * The findings of different runs never share an id, so a lead that receives the findings of many
 * workers keeps each of them under the id the worker's report names.
 */
public class TestFindingIds extends TestCase {

	private static final String URL = "https://energy.example/report";

	/**
	 * However many runs record a finding, the first findings of all of them have different ids. With a
	 * random tag of four letters, five thousand runs share an id about forty times.
	 */
	public void testTheFindingsOfDifferentRunsNeverShareAnId() {
		Set<String> ids = new HashSet<>();
		for (int run = 0; run < 5_000; run++) {
			String id = firstFindingOf(new ReadingList());
			assertTrue("the id " + id + " of run " + run + " is new", ids.add(id));
		}
	}

	/** A lead keeps the findings of all its workers, each under its own id. */
	public void testALeadKeepsEveryWorkersFindings() {
		ReadingList lead = new ReadingList();
		for (int worker = 0; worker < 50; worker++) {
			ReadingList list = new ReadingList();
			firstFindingOf(list);
			lead.addAll(list);
		}

		assertEquals(50, lead.findings().size());
	}

	private static String firstFindingOf(ReadingList list) {
		ReadSource source = new ReadSource(CitationKinds.WEB, URL, "Report", URL, "Storage reached 41 GWh.", "",
			Instant.now());
		list.add(source);
		return list.recordFinding("Storage reached 41 GWh.", list.find(URL), "Storage reached 41 GWh.", "worker",
			null).id();
	}
}
