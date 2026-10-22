/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.ResearchFinding;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.tl.ai.core.security.ModelAccessRightsSecurity;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.TidResolver;
import com.top_logic.tl.ai.core.tools.research.FindingTools;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.Resources;
import com.top_logic.util.model.ModelService;
import com.top_logic.util.TLContextManager;

/**
 * A research run is kept with references to what it rests on - the archived pages, the objects of
 * the application, the runs that wrote and found it - and its document is rendered from the report
 * whenever it is read.
 *
 * <p>
 * The run is kept the way the research skill keeps it - {@code research.saveReport} in a lead run
 * whose reading list holds the sources and the findings a worker run recorded - and read back after
 * the test's interaction is moved to the newest commit, because the stores write on their own
 * threads. The access is asserted against the role rules and grants this module ships, nothing
 * stubbed.
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

	private static int runs;

	/** The lead run of the current test's research. */
	private String _lead;

	/** The worker run that recorded the current test's findings. */
	private String _worker;

	/** An object of the application the current test's research read. */
	private UserMemory _object;

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
		runs++;
		_lead = "research-lead-" + runs;
		_worker = "research-worker-" + runs;
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession chat = store.createSession(SessionKind.CHAT, owner, "Research " + runs);
		AgentExecution lead = store.beginExecution(chat, _lead, "TL_SingleAgent", "test-model", null, null);
		store.beginExecution(chat, _worker, "TL_ResearchWorker", "test-model", null, lead);
		_object = store.createMemory(owner, MemoryKind.FACT, "Prefers storage figures in GWh.", null);
		HistoryUtils.updateSessionRevision();
	}

	/** Runs each test as the owner of the research runs. */
	@Override
	protected void runTest() throws Throwable {
		TLContextManager.<Void, Throwable, RuntimeException> inPersonContext(owner, () -> {
			super.runTest();
			return null;
		});
	}

	/**
	 * The run references its lead run, the archived pages of its web sources, the object it read
	 * and, for each finding, the source and the worker run - and copies none of them.
	 */
	public void testTheRunIsKeptWithReferences() throws Exception {
		String answer = keep("How much storage was installed in 2025?",
			"Storage grew to 41 GWh [laut Bericht](" + CITED + " \"" + CITED_TEXT + "\"). A claim [nowhere](https://never.example/).");

		assertTrue(answer, answer.startsWith("Kept research run "));
		assertTrue(answer, answer.contains("1 cited sources, 3 sources read and not cited, 2 findings"));

		ResearchRun run = newest();
		assertEquals("How much storage was installed in 2025?", run.getQuestion());
		assertEquals(TlAiExecutionFactory.getCOMPLETEResearchStatusClassifier(), run.getStatus());
		assertEquals(TlAiExecutionFactory.getSTANDARDResearchEffortClassifier(), run.getEffort());
		assertEquals("1. How much?\n2. Where?", run.getPlan());
		assertEquals(_lead, run.getExecution().getRunId());
		assertEquals(owner, run.getOwner());
		assertTrue("the report is kept as the lead wrote it", run.getReport().contains("[laut Bericht](" + CITED));

		List<? extends ResearchSource> cited = run.getCitedSources();
		assertEquals(1, cited.size());
		assertEquals("a web source references its archived copy", CITED, cited.get(0).getPage().getUrl());
		assertNull("and copies nothing of it", cited.get(0).getTitle());
		assertNull("and copies nothing of it", cited.get(0).getUrl());
		assertEquals(Integer.valueOf(1), cited.get(0).getFootnote());
		assertEquals("Energy report 2026", cited.get(0).getDisplayName());

		List<String> alsoRead = new ArrayList<>();
		for (ResearchSource source : run.getAlsoRead()) {
			assertEquals(Boolean.FALSE, source.getCited());
			assertNull(source.getFootnote());
			alsoRead.add(source.getPage() != null ? source.getPage().getUrl() : ReadFilter.idOf(source.getObject()));
		}
		assertEquals(List.of(NOT_CITED, HOSTILE, ReadFilter.idOf(_object)), alsoRead);
		assertEquals("every source is one part of the run", 4, run.getSources().size());

		List<? extends ResearchFinding> findings = run.getFindings();
		assertEquals(2, findings.size());
		assertEquals("41 GWh at the end of 2025.", findings.get(0).getClaim());
		assertSame("a finding references the source it quotes", cited.get(0), findings.get(0).getSource());
		assertEquals("and the run that recorded it", _worker, findings.get(0).getRecordedBy().getRunId());
		assertEquals(NOT_CITED, findings.get(1).getSource().getPage().getUrl());
	}

	/**
	 * A lead run that keeps its report a second time - after an answer that came too late, or with a
	 * rewritten report - replaces what it kept: one run, with the second report and nothing left of
	 * the first.
	 */
	public void testKeepingTwiceInOneRunKeepsOneRun() throws Exception {
		keep("Twice?", "First [laut Bericht](" + CITED + ").");
		int partsBefore = parts();
		String answer = keep("Twice?", "Second [laut Bericht](" + CITED + ").", "partial");

		assertTrue(answer, answer.startsWith("Replaced the research run this run kept before, "));
		List<ResearchRun> kept = keptFor(_lead);
		assertEquals("one run per lead run", 1, kept.size());
		ResearchRun run = kept.get(0);
		assertTrue(run.getReport(), run.getReport().startsWith("Second"));
		assertEquals(TlAiExecutionFactory.getPARTIALResearchStatusClassifier(), run.getStatus());
		assertEquals("the parts of the first are deleted, not left behind", partsBefore, parts());
	}

	/**
	 * The document is rendered from the report: an archived copy is linked below the context path,
	 * never with a host; only the markers the citation check wrote are footnote links, a {@code [1]}
	 * in code stays code; the quoted passage is shown with its source.
	 */
	public void testTheDocumentLinksRelativelyAndOnlyItsFootnotes() throws Exception {
		keep("Relative?", "Storage grew [laut Bericht](" + CITED + " \"" + CITED_TEXT + "\").\n\n"
			+ "The value is in `items[1]`, and so is `[2]`.\n\n```\nlist[1] = 41\n```\n");

		ResearchRun run = newest();
		String handle = run.getCitedSources().get(0).getPage().getHandle();
		String document = ResearchDocument.render(run, Locale.ENGLISH, ZoneId.of("Europe/Berlin"), "/my-app");

		assertTrue("the copy is linked below the context path: " + document,
			document.contains("<a href=\"/my-app" + PageArchive.URL_PREFIX + handle + "\""));
		assertFalse("no address of this application carries a host: " + document,
			document.matches("(?s).*href=\"https?://[^\"]*" + PageArchive.URL_PREFIX + ".*"));
		assertEquals("one footnote link, for the one citation: " + document, 1,
			document.split("href=\"#source-", -1).length - 1);
		assertTrue(document, document.contains("<sup><a href=\"#source-1\">[1]</a></sup>"));
		assertTrue("code stays code: " + document, document.contains("<code>items[1]</code>"));
		assertTrue("code stays code: " + document, document.contains("list[1] = 41"));
		assertTrue("the quoted passage is shown with its source", document.contains("<blockquote>" + CITED_TEXT));
		assertTrue("a source opens in a new tab, never in place of the application: " + document,
			document.contains("<li id=\"source-1\"><a href=\"" + CITED
				+ "\" target=\"_blank\" rel=\"noopener noreferrer\">Energy report 2026</a>"));
		assertFalse("what was read and not cited is the run's, not the report's", document.contains(NOT_CITED));
		assertTrue("only tags the safety check allows: " + document,
			!document.contains("<main") && !document.contains("<details") && !document.contains("<article"));
		assertFalse("a link to nothing the run read is not a link", document.contains("never.example"));

		BinaryDataSource download = run.getDocument();
		assertEquals("the field the pane shows and downloads is the rendered document", ResearchDocument.CONTENT_TYPE,
			download.getContentType());
		String downloaded = text(download);
		assertTrue(downloaded, downloaded.startsWith("<!DOCTYPE html>"));
		assertTrue("rendered below the application's own context path: " + downloaded,
			downloaded.contains("href=\"" + PageArchive.URL_PREFIX + handle + "\""));
	}

	/**
	 * A source that is an object of the application survives the deletion of the object: it shows
	 * the label it was read with, marked as deleted, keeps its footnote number, and the run and its
	 * document still render.
	 */
	public void testAnObjectSourceSurvivesTheDeletionOfItsObject() throws Exception {
		String label = MetaLabelProvider.INSTANCE.getLabel(_object);
		keep("Objects?", "Storage grew [laut Bericht](" + CITED + "). The user prefers GWh [laut Notiz](tl:"
			+ ReadFilter.idOf(_object) + ").");
		ResearchRun run = newest();
		ResearchSource source = run.getCitedSources().get(1);
		assertEquals("an object source references the object", _object, source.getObject());
		assertEquals(label, source.getDisplayName());

		AgentExecutionStore.getInstance().deleteMemory(_object);
		HistoryUtils.updateSessionRevision();

		assertFalse("the object is gone", _object.tValid());
		run = newest();
		assertEquals("the source is still part of the run", 2, run.getCitedSources().size());
		source = run.getCitedSources().get(1);
		assertNull("its reference is cleared", source.getObject());
		String deleted = Resources.getInstance().getString(I18NConstants.DELETED__NAME.fill(label));
		assertEquals("it says which object it was, and that it was deleted", deleted, source.getDisplayName());
		assertEquals(Integer.valueOf(2), source.getFootnote());

		String document = render(run);
		assertTrue("the deleted object keeps its footnote number: " + document,
			document.contains("<sup><a href=\"#source-2\">[2]</a></sup>"));
		assertTrue(document, document.contains("<li id=\"source-2\">" + TagUtil.encodeXML(deleted)));
	}

	/** A report that opens with the question as its heading does not show the question twice. */
	public void testTheQuestionIsTheTitleOnce() throws Exception {
		keep("Wie wächst der Speicher?", "# Wie wächst der **Speicher?**\n\nEr wächst [laut Bericht](" + CITED + ").");

		String document = render(newest());
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

		String document = render(newest());
		assertFalse("one title, the question's: " + document, document.contains("<h1"));
		assertTrue(document, document.contains("<h2>Prices fell</h2>"));
		assertTrue(document, document.contains("<h3>Reasons</h3>"));
	}

	/** A page's title is the page's text: it is escaped, never markup. */
	public void testAPagesTitleIsEscaped() throws Exception {
		keep("Escaping?", "Cited [here](" + HOSTILE + ").");

		String document = render(newest());
		assertFalse(document, document.contains("<img src=x"));
		assertTrue(document, document.contains("&lt;img src=x onerror=alert(1)&gt;"));
	}

	/**
	 * The owner reads the run, its parts and the archived pages they reference, a colleague nothing
	 * of it, an administrator everything - the shipped rule, as for a screenshot.
	 */
	public void testOnlyTheOwnerAndAdministratorsReadIt() throws Exception {
		keep("Access?", "Storage grew [laut Bericht](" + CITED + ").");

		ResearchRun run = newest();
		List<TLObject> parts = new ArrayList<>();
		parts.add(run);
		for (ResearchSource source : run.getSources()) {
			parts.add(source);
			if (source.getPage() != null) {
				parts.add(source.getPage());
			}
		}
		parts.addAll(run.getFindings());
		boolean ownerReads = true;
		boolean strangerReads = false;
		boolean adminReads = true;
		for (TLObject part : parts) {
			ownerReads &= new ModelAccessRightsSecurity(owner).canRead(part);
			strangerReads |= new ModelAccessRightsSecurity(stranger).canRead(part);
			adminReads &= new ModelAccessRightsSecurity(administrator).canRead(part);
		}
		assertTrue("the run has a source, a page and a finding", parts.size() > 6);
		assertTrue("the owner reads the run and every part", ownerReads);
		assertFalse("a colleague reads none of it", strangerReads);
		assertTrue("an administrator reads all of it", adminReads);
	}

	/** Deleting a run deletes its parts and never a page another run may rest on. */
	public void testDeletingARunKeepsItsPages() throws Exception {
		keep("Delete?", "Storage grew [laut Bericht](" + CITED + ").");
		ResearchRun run = newest();
		List<TLObject> pages = new ArrayList<>();
		for (ResearchSource source : run.getSources()) {
			if (source.getPage() != null) {
				pages.add(source.getPage());
			}
		}
		List<TLObject> parts = new ArrayList<>(run.getSources());
		parts.addAll(run.getFindings());

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			run.tDelete();
			tx.commit();
		}

		assertFalse(pages.isEmpty());
		for (TLObject page : pages) {
			assertTrue("an archived page outlives the run", page.tValid());
		}
		for (TLObject part : parts) {
			assertFalse("a source or a finding is deleted with its run", part.tValid());
		}
	}

	/** Outside an agent run there is nothing to keep. */
	public void testOutsideARunNothingIsKept() {
		assertEquals("Error: a research run is kept only within an agent run.",
			new FindingTools().saveReport("q", "r", null, null, null, null));
	}

	/** An effort the run does not know is refused before anything is kept. */
	public void testAnUnknownEffortIsRefused() {
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(new ReadingList())) {
			assertEquals("Error: effort must be quick, standard or deep.",
				new FindingTools().saveReport("q", "r", "thorough", null, null, null));
		}
	}

	private String keep(String question, String report) {
		return keep(question, report, null);
	}

	/**
	 * Keeps a research run of a run tree that read three pages and an object, and whose worker
	 * recorded two findings.
	 */
	private String keep(String question, String report, String status) {
		ReadingList list = new ReadingList();
		Instant read = Instant.parse("2026-10-09T08:00:00Z");
		list.add(web(CITED, "Energy report 2026", "# Energy report 2026\n\n" + CITED_TEXT + " Costs fell.", read));
		list.add(web(NOT_CITED, "Grid outlook", "Grid operators plan 120 new substations until 2030.",
			read.plusSeconds(60)));
		list.add(web(HOSTILE, "<img src=x onerror=alert(1)>", "Nothing of use here at all.", read.plusSeconds(120)));
		list.add(ReadSource.of(CitationKinds.OBJECT, ReadFilter.idOf(_object),
			MetaLabelProvider.INSTANCE.getLabel(_object)));
		FindingTools tools = new FindingTools();
		try (ReadingListBinding.Binding bound = ReadingListBinding.bind(list)) {
			try (RunInfoBinding.Binding worker = RunInfoBinding.bind(run(_worker, "TL_ResearchWorker"))) {
				tools.recordFinding("41 GWh at the end of 2025.", CITED, CITED_TEXT);
				tools.recordFinding("120 substations until 2030.", NOT_CITED,
					"Grid operators plan 120 new substations until 2030.");
			}
			try (RunInfoBinding.Binding lead = RunInfoBinding.bind(run(_lead, "TL_SingleAgent"))) {
				return tools.saveReport(question, report, "standard", "1. How much?\n2. Where?", status, CONTEXT);
			}
		} finally {
			// The stores wrote on their own threads; this interaction reads at the revision it
			// started with until it is moved on.
			HistoryUtils.updateSessionRevision();
		}
	}

	/** A web page the run read, archived the way webFetch archives it. */
	private static ReadSource web(String url, String title, String text, Instant readAt) {
		PageArchive.Copy copy = PageArchive.getInstance().archive(url, title, text, owner);
		assertNotNull("the page was archived", copy);
		return new ReadSource(CitationKinds.WEB, url, title, url, text, copy.viewUrl(), readAt);
	}

	private static RunInfo run(String runId, String agent) {
		return new RunInfo(runId, null, agent, "test-model", null, null, 0, null);
	}

	/** The context of the lead run: a principal who may read the objects its sources name. */
	private static final ToolExecutionContext CONTEXT = new ToolExecutionContext() {
		@Override
		public ProgressReporter progressReporter() {
			return ProgressReporter.NOOP;
		}

		@Override
		public CancellationToken cancellationToken() {
			return CancellationToken.UNCANCELLABLE;
		}

		@Override
		public TLObject resolveObject(String id) {
			return TidResolver.resolve(id, ModelSecurity.UNCHECKED);
		}
	};

	private static String render(ResearchRun run) {
		return ResearchDocument.render(run, Locale.ENGLISH, ZoneId.of("UTC"), "");
	}

	private static String text(BinaryDataSource binary) throws IOException {
		try (InputStream in = binary.toData().getStream()) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/** How many sources and findings exist. */
	private static int parts() {
		return MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchSourceType(), TLObject.class).size()
			+ MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchFindingType(), TLObject.class).size();
	}

	/** The runs kept for the given lead run. */
	private static List<ResearchRun> keptFor(String lead) {
		List<ResearchRun> result = new ArrayList<>();
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getResearchRunType(),
			TLObject.class)) {
			ResearchRun run = (ResearchRun) object;
			if (run.getExecution() != null && lead.equals(run.getExecution().getRunId())) {
				result.add(run);
			}
		}
		return result;
	}

	/** The run the current test's lead run kept. */
	private ResearchRun newest() {
		List<ResearchRun> kept = keptFor(_lead);
		assertEquals("one run was kept", 1, kept.size());
		return kept.get(0);
	}

	/** The model, the access manager and the rights service the policy is made of, and the stores. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestResearchRuns.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				PageArchive.Module.INSTANCE,
				ResearchRuns.Module.INSTANCE));
	}
}
