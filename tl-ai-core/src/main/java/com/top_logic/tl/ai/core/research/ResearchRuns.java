/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.core.execution.model.ResearchFinding;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.util.model.ModelService;

/**
 * Keeps research runs as {@link ResearchRun} objects.
 *
 * <p>
 * <b>Its own transaction, on its own thread</b>, for the reason {@code PageArchive} gives: the run
 * that keeps its report may hold an open transaction for the writes of an earlier tool call, and a
 * report written into it would be rolled back with that turn. The caller waits for the write.
 * </p>
 *
 * <p>
 * <b>Private to its owner.</b> The run is written with the account the research ran for as its
 * owner; the role rule in {@code tl-ai-core.binarySecurity.config.xml} lets that account, and only
 * that account, read the run and its parts. Administrators read everything anyway.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,
	PersistencyLayer.Module.class,
})
public class ResearchRuns extends ConfiguredManagedClass<ResearchRuns.Config<?>> {

	/** How long a run waits for its report to be written. */
	private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);

	/** What the stored document is. */
	public static final String DOCUMENT_TYPE = "text/html; charset=utf-8";

	/** Configuration of {@link ResearchRuns}. */
	public interface Config<I extends ResearchRuns> extends ConfiguredManagedClass.Config<I> {
		// Nothing to configure.
	}

	private ExecutorService _worker;

	/**
	 * Creates a {@link ResearchRuns} from configuration.
	 *
	 * @param context
	 *        For instantiation of dependent configured objects.
	 * @param config
	 *        The configuration.
	 */
	public ResearchRuns(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	protected void startUp() {
		super.startUp();
		_worker = Executors.newSingleThreadExecutor(task -> {
			Thread thread = new Thread(task, "tl-ai-research-runs");
			thread.setDaemon(true);
			return thread;
		});
	}

	@Override
	protected void shutDown() {
		if (_worker != null) {
			_worker.shutdownNow();
			_worker = null;
		}
		super.shutDown();
	}

	/**
	 * Keeps a research run.
	 *
	 * @param report
	 *        What is kept of it.
	 * @param document
	 *        The report as an HTML document.
	 * @param owner
	 *        The account the research ran for; {@code null} for a run without one, which only
	 *        administrators can read.
	 * @return The id of the stored run, as every tool reads it ({@code ResearchRun:...}).
	 * @throws IllegalStateException
	 *         When the run could not be written.
	 */
	public String save(ResearchReport report, String document, Person owner) {
		Future<String> write = _worker.submit(() -> ThreadContextManager.inSystemInteraction(ResearchRuns.class,
			() -> store(report, document, owner)));
		try {
			return write.get(WRITE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while the research report was written.", ex);
		} catch (ExecutionException | TimeoutException ex) {
			throw new IllegalStateException("The research report could not be written: " + ex.getMessage(), ex);
		}
	}

	private String store(ResearchReport report, String document, Person owner) {
		TlAiExecutionFactory factory = TlAiExecutionFactory.getInstance();
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(ResKey.text("Keep research run: " + report.question()))) {
			ResearchRun run = factory.createResearchRun();
			run.setQuestion(report.question());
			run.setStatus(report.status());
			run.setEffort(emptyToNull(report.effort()));
			run.setPlan(emptyToNull(report.plan()));
			run.setReport(report.markdown());
			run.setStarted(date(report.started()));
			run.setFinished(date(report.finished()));
			run.setTokens(report.tokens());
			run.setDocument(BinaryDataFactory.createBinaryData(document.getBytes(StandardCharsets.UTF_8),
				DOCUMENT_TYPE, "research-report.html"));
			if (owner != null) {
				run.setOwner(owner);
			}
			for (ResearchReport.Source source : report.cited()) {
				run.addCitedSource(source(factory, source));
			}
			for (ResearchReport.Source source : report.alsoRead()) {
				run.addAlsoRead(source(factory, source));
			}
			for (Finding finding : report.findings()) {
				run.addFinding(finding(factory, finding));
			}
			for (Finding finding : report.otherFindings()) {
				run.addOtherFinding(finding(factory, finding));
			}
			tx.commit();
			return ReadFilter.idOf(run);
		}
	}

	private static ResearchSource source(TlAiExecutionFactory factory, ResearchReport.Source source) {
		ResearchSource result = factory.createResearchSource();
		result.setTitle(source.title());
		result.setUrl(source.url());
		result.setKind(source.kind());
		result.setArchive(emptyToNull(source.archive()));
		result.setReadAt(date(source.readAt()));
		return result;
	}

	private static ResearchFinding finding(TlAiExecutionFactory factory, Finding finding) {
		ResearchFinding result = factory.createResearchFinding();
		result.setFindingId(finding.id());
		result.setClaim(finding.claim());
		result.setQuote(finding.quote());
		result.setSource(finding.source());
		result.setRecordedBy(emptyToNull(finding.recordedBy()));
		return result;
	}

	private static Date date(Instant instant) {
		return instant == null ? null : Date.from(instant);
	}

	private static String emptyToNull(String value) {
		return value == null || value.isEmpty() ? null : value;
	}

	/** The running instance. */
	public static ResearchRuns getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/** Module of {@link ResearchRuns}. */
	public static final class Module extends TypedRuntimeModule<ResearchRuns> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton.
		}

		@Override
		public Class<ResearchRuns> getImplementation() {
			return ResearchRuns.class;
		}
	}
}
