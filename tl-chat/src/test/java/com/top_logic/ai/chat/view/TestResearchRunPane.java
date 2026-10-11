/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.element.GroupElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.tl.ai.core.execution.model.ResearchFinding;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * What the chat's side pane shows of a kept research run: the report in place, then its cited
 * sources and its findings, and what it read and did not cite in a closed group - each a table of
 * the engine's, though two of them are computed selections of the run's one composition of sources.
 */
public class TestResearchRunPane extends BasicTestCase {

	private static Person owner;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("pane-research-owner");
		}
		TLContext.getContext().setPerson(owner);
	}

	/** Cited sources and findings are tables, what was also read a closed group; all sources are not shown. */
	public void testThePaneShowsTheThreeGroups() {
		List<String> shown = new ArrayList<>();
		for (TLStructuredTypePart part : PaneLayouts.ordered(TlAiExecutionFactory.getResearchRunType())) {
			if (!PaneLayouts.isTable(part)) {
				continue;
			}
			PolymorphicConfiguration<? extends UIElement> table = PresentationPane.table(part);
			if (table instanceof GroupElement.Config group) {
				assertTrue(part.getName(), group.getCollapsed());
				CompositionTableElement.Config inner = (CompositionTableElement.Config) group.getChildren().get(0);
				shown.add("closed group: " + inner.getAttribute());
			} else {
				shown.add("table: " + ((CompositionTableElement.Config) table).getAttribute());
			}
		}
		assertEquals(List.of("table: citedSources", "table: findings", "closed group: alsoRead"), shown);
	}

	/** The pane builds the form of a run, with its report read in place. */
	public void testThePaneShowsARun() throws Exception {
		ResearchRun run = keptRun();
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		String window = "paneWindow";
		DefaultReactContext react = new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry);
		ViewChannel input = new DefaultViewChannel("paneInput");
		PresentationPane pane = new PresentationPane(new DefaultViewContext(react), input,
			new ReactControl(react, null, "TLTest"), () -> {
				// Nothing to tell a browser.
			});
		try {
			input.set(run);

			List<Map<String, Object>> steps = pane.state();
			assertEquals("the pane built the run's form rather than swallowing a failure", 1, steps.size());
			assertEquals("form", steps.get(0).get("kind"));
		} finally {
			pane.dispose();
		}

		// The lists are computed with the reader's rights; the run is the test account's own.
		assertEquals(List.of("Energy report 2026"), names(run.getCitedSources()));
		assertEquals(List.of("Grid outlook"), names(run.getAlsoRead()));
		String report = PresentationPane.htmlDocument(run, TlAiExecutionFactory.getDocumentResearchRunAttr());
		assertNotNull("the report is a document the pane shows in place", report);
		assertTrue(report, report.contains("Storage grew"));
		assertTrue(report, report.contains("<sup><a href=\"#source-1\">[1]</a></sup>"));
		assertFalse("the question is a field, not repeated in the report: " + report, report.contains("download-only"));
	}

	private static List<String> names(List<? extends ResearchSource> sources) {
		List<String> result = new ArrayList<>();
		for (ResearchSource source : sources) {
			result.add(source.getDisplayName());
		}
		return result;
	}

	private static ResearchRun keptRun() {
		TlAiExecutionFactory factory = TlAiExecutionFactory.getInstance();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ResearchRun run = factory.createResearchRun();
			run.setOwner(owner);
			run.setQuestion("How much storage was installed in 2025?");
			run.setStatus(TlAiExecutionFactory.getCOMPLETEResearchStatusClassifier());
			run.setEffort(TlAiExecutionFactory.getQUICKResearchEffortClassifier());
			run.setReport("Storage grew to 41 GWh [laut Bericht](https://energy.example/report).");
			run.setFinished(new Date());
			ResearchSource cited = source(factory, "Energy report 2026", "https://energy.example/report", 1);
			run.addSource(cited);
			run.addSource(source(factory, "Grid outlook", "https://grid.example/outlook", 0));
			ResearchFinding finding = factory.createResearchFinding();
			finding.setFindingId("F1-test");
			finding.setClaim("Storage reached 41 GWh.");
			finding.setQuote("Installed battery storage reached 41 GWh.");
			finding.setSource(cited);
			run.addFinding(finding);
			tx.commit();
			return run;
		}
	}

	private static ResearchSource source(TlAiExecutionFactory factory, String title, String url, int footnote) {
		ResearchSource source = factory.createResearchSource();
		source.setKind("web");
		source.setTitle(title);
		source.setUrl(url);
		source.setCited(Boolean.valueOf(footnote > 0));
		source.setFootnote(footnote > 0 ? Integer.valueOf(footnote) : null);
		source.setReadAt(new Date());
		return source;
	}

	/** The model, and what the pane's form and tables are built with. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestResearchRunPane.class),
				ModelService.Module.INSTANCE,
				AccessManager.Module.INSTANCE,
				SecurityConfigurationService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE));
	}
}
