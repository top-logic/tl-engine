/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.research;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.subagent.SubagentResultDelivery;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * A research run records findings whose quotes its sources really contain; the findings travel to
 * the lead with the sources, and the lead reads a source again without fetching it.
 */
public class TestFindingTools extends TestCase {

	private static final String REPORT = "https://energy.example/report-2026";

	private static final String PAGE = "# Energy report 2026\n\nInstalled battery storage reached 41 GWh at the"
		+ " end of 2025,\nup from 29 GWh a year before. Grid operators expect 60 GWh by 2027.\n\nCosts fell"
		+ " by 2\\.5 percent.";

	private final FindingTools _tools = new FindingTools();

	/** A quote the source contains is recorded, and the answer names the finding's id. */
	public void testAQuoteTheSourceContainsIsRecorded() {
		ReadingList list = listWith(REPORT, "Energy report 2026", PAGE);
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			String answer = _tools.recordFinding("Battery storage grew by about 40 percent in 2025.", REPORT,
				"Installed battery storage reached 41 GWh at the end of 2025");

			assertTrue(answer, answer.startsWith("Recorded finding F1-"));
			Finding finding = list.findings().get(0);
			assertEquals(REPORT, finding.source());
			assertEquals("Energy report 2026", finding.sourceTitle());
			assertTrue(answer, answer.contains(finding.id()));
		}
	}

	/**
	 * Word for word, not line for line: a line break in the stored text is a space in a quoted
	 * sentence, and the Markdown escapes of the stored text are not required.
	 */
	public void testWhiteSpaceAndEscapesMayDiffer() {
		ReadingList list = listWith(REPORT, "Energy report 2026", PAGE);
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			assertTrue(_tools.recordFinding("Storage grew.", REPORT,
				"at the end of 2025, up from 29 GWh a year before").startsWith("Recorded"));
			assertTrue(_tools.recordFinding("Costs fell.", REPORT, "Costs fell by 2.5 percent.").startsWith("Recorded"));
		}
	}

	/** A quote the source does not say is refused, and nothing is recorded. */
	public void testAnInventedQuoteIsRefused() {
		ReadingList list = listWith(REPORT, "Energy report 2026", PAGE);
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			String answer = _tools.recordFinding("Storage tripled.", REPORT,
				"Installed battery storage reached 87 GWh at the end of 2025");

			assertTrue(answer, answer.startsWith("Error: the quote does not occur"));
			assertTrue(list.findings().isEmpty());
		}
	}

	/**
	 * Of a long document, only what the run read is the source's text: a quote from a part it has
	 * not read is refused, however true it is.
	 */
	public void testAQuoteFromAnUnreadPartIsRefused() {
		String document = "Page one says the plant opened in 2019. " + "x ".repeat(5_000)
			+ "Page forty says the plant closed in 2031.";
		ReadingList list = new ReadingList();
		String read = list.readPart(REPORT, document, 0, 100);
		list.add(new ReadSource(CitationKinds.WEB, REPORT, "Plant history", REPORT, read, "", Instant.now()));
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			assertTrue(_tools.recordFinding("It opened in 2019.", REPORT, "the plant opened in 2019")
				.startsWith("Recorded"));
			assertTrue(_tools.recordFinding("It closes in 2031.", REPORT, "the plant closed in 2031")
				.startsWith("Error: the quote does not occur"));
		}
	}

	/** A source the run never read cannot carry a finding. */
	public void testAnUnreadSourceIsRefused() {
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(listWith(REPORT, "Report", PAGE))) {
			String answer = _tools.recordFinding("Something.", "https://other.example/",
				"Installed battery storage reached 41 GWh");

			assertTrue(answer, answer.startsWith("Error: the source is not one this run has read"));
		}
	}

	/** A quote of a few words checks nothing, and one of a page is no finding. */
	public void testTheQuoteMustBeASentence() {
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(listWith(REPORT, "Report", PAGE))) {
			assertTrue(_tools.recordFinding("Grew.", REPORT, "41 GWh").startsWith("Error: the quote must have"));
			assertTrue(_tools.recordFinding("Grew.", REPORT, "x".repeat(FindingTools.MAX_QUOTE + 1))
				.startsWith("Error: the quote must have"));
		}
	}

	/**
	 * A worker's findings join its lead's list with its sources, under the ids its report names, and
	 * the lead lists them with the quoted words.
	 */
	public void testAWorkersFindingsReachTheLead() {
		ReadingList worker = listWith(REPORT, "Energy report 2026", PAGE);
		ReadingList otherWorker = listWith("https://grid.example/outlook", "Grid outlook",
			"Grid operators plan 120 new substations until 2030.");
		ReadingList lead = new ReadingList();
		String first;
		String second;
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(worker)) {
			first = idIn(_tools.recordFinding("Storage reached 41 GWh.", REPORT,
				"Installed battery storage reached 41 GWh at the end of 2025"));
		}
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(otherWorker)) {
			second = idIn(_tools.recordFinding("120 substations are planned.", "https://grid.example/outlook",
				"Grid operators plan 120 new substations until 2030."));
		}
		assertFalse("ids are unique in the run tree", first.equals(second));

		lead.addAll(worker);
		lead.addAll(otherWorker);

		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(lead)) {
			String listed = _tools.listFindings(null);
			assertTrue(listed, listed.startsWith("2 findings:"));
			assertTrue(listed, listed.contains(first + ": Storage reached 41 GWh."));
			assertTrue(listed, listed.contains("quote: \"Grid operators plan 120 new substations until 2030.\""));
			assertTrue("written by research runs, so marked as their report", listed.contains("<untrusted_subagent_report"));

			String onlyGrid = _tools.listFindings("https://grid.example/outlook");
			assertTrue(onlyGrid, onlyGrid.startsWith("1 finding:"));
			assertFalse(onlyGrid, onlyGrid.contains(first));
		}
	}

	/** The report a lead receives names the worker's findings, one line each, inside the report's envelope. */
	public void testTheDeliveredReportListsTheFindings() {
		ReadingList worker = listWith(REPORT, "Energy report 2026", PAGE);
		String id;
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(worker)) {
			id = idIn(_tools.recordFinding("Storage reached 41 GWh.", REPORT,
				"Installed battery storage reached 41 GWh at the end of 2025"));
		}

		String report = SubagentResultDelivery.report("subagent sa-1", "Storage grew fast, see " + id + ".", worker);

		assertTrue(report, report.contains("Findings recorded (1):\n- " + id + ": Storage reached 41 GWh. [Energy report 2026]"));
		assertTrue("inside the envelope", report.indexOf(id + ": Storage") < report.lastIndexOf("</untrusted_subagent_report>"));
		assertEquals("without findings the report is unchanged",
			SubagentResultDelivery.report("s", "text"), SubagentResultDelivery.report("s", "text", new ReadingList()));
	}

	/** The lead reads a source's stored text in windows, as page content, without a fetch. */
	public void testTheLeadReadsTheStoredTextInWindows() {
		String text = ("A paragraph of the report about storage. ".repeat(20) + "\n\n").repeat(40);
		ReadingList list = listWith(REPORT, "Energy report 2026", text);
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			String first = _tools.readSource(REPORT, null);
			assertTrue(first, first.startsWith("<untrusted_page_content source=\"" + REPORT + "\">"));
			Matcher next = Pattern.compile("offset=(\\d+)\\.\\]$").matcher(first);
			assertTrue("a long text is read in parts: " + first.substring(first.length() - 200), next.find());

			String rest = _tools.readSource(REPORT, Integer.valueOf(next.group(1)));
			assertTrue(rest, rest.endsWith("This is the end of the stored text.]"));

			assertTrue(_tools.readSource("https://never.example/", null)
				.startsWith("Error: the source is not one this run or its subagents read"));
		}
	}

	/**
	 * As the tool registry offers them: three tools a background worker reaches without a grant
	 * (the implicit {@code interaction} group) and calls without an approval (READ), callable by the
	 * names an agent definition lists after the factory name.
	 */
	public void testTheToolsAsTheRegistryOffersThem() {
		Map<String, Tool> tools = new HashMap<>();
		for (Tool tool : AnnotatedTools.from(new FindingTools())) {
			tools.put(tool.name(), tool);
		}
		assertEquals(Set.of("recordFinding", "listFindings", "readSource"), tools.keySet());
		for (Tool tool : tools.values()) {
			assertEquals(tool.name(), ToolMode.READ, tool.mode());
			assertEquals(tool.name(), ToolGroups.INTERACTION, tool.group());
		}

		ReadingList list = listWith(REPORT, "Energy report 2026", PAGE);
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			String answer = tools.get("recordFinding").execute(Map.of(
				"claim", "Storage reached 41 GWh.",
				"source", REPORT,
				"quote", "Installed battery storage reached 41 GWh at the end of 2025"), null).text();
			assertTrue(answer, answer.startsWith("Recorded finding F1-"));
		}
	}

	/** Outside an agent run there is nowhere to keep a finding. */
	public void testOutsideARunNothingIsKept() {
		assertEquals("Error: findings are kept only within an agent run.",
			_tools.recordFinding("c", REPORT, "Installed battery storage reached 41 GWh"));
	}

	private static ReadingList listWith(String url, String title, String text) {
		ReadingList list = new ReadingList();
		list.add(new ReadSource(CitationKinds.WEB, url, title, url, text, "", Instant.now()));
		return list;
	}

	private static String idIn(String answer) {
		Matcher matcher = Pattern.compile("Recorded finding (\\S+) ").matcher(answer);
		assertTrue(answer, matcher.find());
		return matcher.group(1);
	}
}
