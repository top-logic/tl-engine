/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.tools.research.FindingTools;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * A research run is kept with its report, what the report cites and what else the run tree read,
 * as a document only its owner and administrators can read.
 *
 * <p>
 * The run is kept the way the research skill keeps it - {@code research.saveReport} in a run whose
 * reading list holds the sources and findings - and read back after the test's interaction is moved
 * to the newest commit, because the store writes it on its own thread. The access is asserted against the role rules and grants
 * this module ships, nothing stubbed.
 * </p>
 */
public class TestResearchRuns extends BasicTestCase {

	private static final String CITED = "https://energy.example/report-2026";

	private static final String NOT_CITED = "https://grid.example/outlook";

	private static final String HOSTILE = "https://spam.example/page";

	private static final String CITED_TEXT = "Installed battery storage reached 41 GWh at the end of 2025.";

	private static Person owner;

	private static Person stranger;

	private static Person administrator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("research-owner");
			stranger = TestPerson.createPerson("research-stranger");
			administrator = TestPerson.createPerson("research-admin");
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				administrator.setAdmin(true);
				tx.commit();
			}
		}
		TLContext.getContext().setPerson(owner);
	}

	/**
	 * The report's footnotes are the cited sources; what the run read and did not cite is "also
	 * read", and the findings are split the same way. The document carries all of it, the report
	 * text with linked footnotes.
	 */
	public void testTheRunIsKeptWithWhatItCitesAndWhatElseItRead() throws Exception {
		String answer = keep("How much storage was installed in 2025?",
			"Storage grew to 41 GWh [laut Bericht](" + CITED + " \"" + CITED_TEXT + "\"). A claim [nowhere](https://never.example/).");

		assertTrue(answer, answer.startsWith("Kept research run "));
		assertTrue(answer, answer.contains("1 cited sources, 2 sources read and not cited, 2 findings"));

		ResearchRunView run = latestRun();
		assertEquals("How much storage was installed in 2025?", run.question);
		assertEquals(ResearchReport.COMPLETE, run.status);
		assertEquals("standard", run.effort);
		assertEquals(List.of(CITED), run.cited);
		assertEquals(List.of(NOT_CITED, HOSTILE), run.alsoRead);
		assertEquals("the finding on the cited source", List.of("41 GWh at the end of 2025."), run.findings);
		assertEquals("the finding on a source not cited", List.of("120 substations until 2030."), run.otherFindings);

		assertTrue(run.document, run.document.startsWith("<!DOCTYPE html>"));
		assertTrue("the footnote links to its source: " + run.document,
			run.document.contains("<sup><a href=\"#source-1\">[1]</a></sup>"));
		assertTrue("a source opens in a new tab, never in place of the application: " + run.document,
			run.document.contains("<li id=\"source-1\"><a href=\"" + CITED
				+ "\" target=\"_blank\" rel=\"noopener noreferrer\">Energy report 2026</a>"));
		assertTrue("the quoted passage is shown with its source", run.document.contains("<blockquote>" + CITED_TEXT));
assertFalse("what was read and not cited is the run's, not the report's", run.document.contains(NOT_CITED));
		assertTrue("only tags the safety check allows: " + run.document,
			!run.document.contains("<main") && !run.document.contains("<details") && !run.document.contains("<article"));
		assertFalse("a link to nothing the run read is not a link", run.document.contains("https://never.example/"));
	}

	/** A report that opens with the question as its heading does not show the question twice. */
	public void testTheQuestionIsTheTitleOnce() throws Exception {
		keep("Wie wächst der Speicher?", "# Wie wächst der **Speicher?**\n\nEr wächst [laut Bericht](" + CITED + ").");

		String document = latestRun().document;
		assertEquals("one title: " + document, 1, document.split("Wie wächst der", -1).length - 2);
		assertTrue(document, document.contains("<p class=\"title download-only\"><strong>Wie wächst der Speicher?</strong></p>"));
	}

	/**
	 * The question is the document's title, so the report's own headings start one level below it: a
	 * report titled with a statement does not show a second, equally large title (#800).
	 */
	public void testTheReportsHeadingsAreBelowTheTitle() throws Exception {
		keep("How did prices develop?", "# Prices fell\n\nThey fell [laut Bericht](" + CITED + ").\n\n"
			+ "## Reasons\n\nCheaper cells.");

		String document = latestRun().document;
		assertFalse("one title, the question's: " + document, document.contains("<h1"));
		assertTrue(document, document.contains("<h2>Prices fell</h2>"));
		assertTrue(document, document.contains("<h3>Reasons</h3>"));
	}

	/** A page's title is the page's text: it is escaped, never markup. */
	public void testAPagesTitleIsEscaped() throws Exception {
		keep("Escaping?", "Cited [here](" + HOSTILE + ").");

		String document = latestRun().document;
		assertFalse(document, document.contains("<img src=x"));
		assertTrue(document, document.contains("&lt;img src=x onerror=alert(1)&gt;"));
	}

	/**
	 * The owner reads the run and its parts, a colleague nothing of it, an administrator everything
	 * - the shipped rule, as for a screenshot.
	 */
	public void testOnlyTheOwnerAndAdministratorsReadIt() throws Exception {
		keep("Access?", "Storage grew [laut Bericht](" + CITED + ").");

		HistoryUtils.updateSessionRevision();
		ResearchRun run = newest();
		List<TLObject> parts = new ArrayList<>();
		parts.add(run);
		parts.addAll(run.getCitedSources());
		parts.addAll(run.getAlsoRead());
		parts.addAll(run.getFindings());
		parts.addAll(run.getOtherFindings());
		boolean ownerReads = true;
		boolean strangerReads = false;
		boolean adminReads = true;
		for (TLObject part : parts) {
			ownerReads &= new ModelAccessRightsSecurity(owner).canRead(part);
			strangerReads |= new ModelAccessRightsSecurity(stranger).canRead(part);
			adminReads &= new ModelAccessRightsSecurity(administrator).canRead(part);
		}
		boolean[] answers = { ownerReads, strangerReads, adminReads, parts.size() > 4 };
		assertTrue("the owner reads the run and every part", answers[0]);
		assertFalse("a colleague reads none of it", answers[1]);
		assertTrue("an administrator reads all of it", answers[2]);
		assertTrue("the run has parts of every kind", answers[3]);
	}

	/** Outside an agent run there is nothing to keep. */
	public void testOutsideARunNothingIsKept() {
		assertEquals("Error: a research run is kept only within an agent run.",
			new FindingTools().saveReport("q", "r", null, null, null, null));
	}

	/** Keeps a research run of a run tree that read three pages and recorded two findings. */
	private static String keep(String question, String report) {
		ReadingList list = new ReadingList();
		Instant read = Instant.parse("2026-10-09T08:00:00Z");
		list.add(new ReadSource(CitationKinds.WEB, CITED, "Energy report 2026", CITED,
			"# Energy report 2026\n\n" + CITED_TEXT + " Costs fell.", "", read));
		list.add(new ReadSource(CitationKinds.WEB, NOT_CITED, "Grid outlook", NOT_CITED,
			"Grid operators plan 120 new substations until 2030.", "", read.plusSeconds(60)));
		list.add(new ReadSource(CitationKinds.WEB, HOSTILE, "<img src=x onerror=alert(1)>", HOSTILE,
			"Nothing of use here at all.", "", read.plusSeconds(120)));
		FindingTools tools = new FindingTools();
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			tools.recordFinding("41 GWh at the end of 2025.", CITED, CITED_TEXT);
			tools.recordFinding("120 substations until 2030.", NOT_CITED,
				"Grid operators plan 120 new substations until 2030.");
			return tools.saveReport(question, report, "standard", "1. How much?\n2. Where?", null, null);
		}
	}

	/** What a test reads of the newest kept run. */
	static final class ResearchRunView {
		String question;

		String status;

		String effort;

		List<String> cited = new ArrayList<>();

		List<String> alsoRead = new ArrayList<>();

		List<String> findings = new ArrayList<>();

		List<String> otherFindings = new ArrayList<>();

		String document;
	}

	private static ResearchRunView latestRun() throws IOException {
		// The store wrote the run on its own thread; this interaction reads at the revision it
		// started with until it is moved on.
		HistoryUtils.updateSessionRevision();
		ResearchRun run = newest();
		ResearchRunView view = new ResearchRunView();
		view.question = run.getQuestion();
		view.status = run.getStatus();
		view.effort = run.getEffort();
		run.getCitedSources().forEach(s -> view.cited.add(s.getUrl()));
		run.getAlsoRead().forEach(s -> view.alsoRead.add(s.getUrl()));
		run.getFindings().forEach(f -> view.findings.add(f.getClaim()));
		run.getOtherFindings().forEach(f -> view.otherFindings.add(f.getClaim()));
		try (InputStream in = run.getDocument().toData().getStream()) {
			view.document = new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		return view;
	}

	/** The run kept last. */
	private static ResearchRun newest() {
		ResearchRun newest = null;
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchRunType(),
			TLObject.class)) {
			ResearchRun run = (ResearchRun) object;
			if (newest == null || run.getFinished().after(newest.getFinished())
				|| (run.getFinished().equals(newest.getFinished()) && run.tIdLocal().compareTo(newest.tIdLocal()) > 0)) {
				newest = run;
			}
		}
		assertNotNull("a run was kept", newest);
		return newest;
	}

	/** The model, the access manager and the rights service the policy is made of, and the store. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestResearchRuns.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				ResearchRuns.Module.INSTANCE));
	}
}
