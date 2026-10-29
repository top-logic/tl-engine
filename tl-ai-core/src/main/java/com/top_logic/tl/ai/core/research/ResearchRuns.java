/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.execution.WriterShutdown;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.ArchivedPage;
import com.top_logic.tl.ai.core.execution.model.ResearchFinding;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.tool.api.AICategory;
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
 * <b>Once per lead run.</b> A lead run that keeps its report a second time - because the first
 * answer came late, or because it rewrote the report - replaces what it kept before; it never keeps
 * a second run. The writes are done one after the other on one thread, so a second call that comes
 * while the first is still being written finds the run the first one kept.
 * </p>
 *
 * <p>
 * <b>Private to its owner.</b> The run is written with the account the research ran for as its
 * owner; the role rules in {@code tl-ai-core.binarySecurity.config.xml} let that account, and only
 * that account, read the run, its parts and the archived pages they reference. Administrators read
 * everything anyway.
 * </p>
 */
@ServiceDependencies({
	ModelService.Module.class,
	PersistencyLayer.Module.class,
})
@ServiceCategory.Of(AICategory.class)
public class ResearchRuns extends ConfiguredManagedClass<ResearchRuns.Config<?>> {

	/** How long a run waits for its report to be written. */
	private static final Duration WRITE_TIMEOUT = Duration.ofSeconds(30);

	/** Configuration of {@link ResearchRuns}. */
	public interface Config<I extends ResearchRuns> extends ConfiguredManagedClass.Config<I> {
		// Nothing to configure.
	}

	/**
	 * What {@link ResearchRuns#save(ResearchReport, Person)} did.
	 *
	 * @param id
	 *        The id of the kept run, as every tool reads it ({@code ResearchRun:...}).
	 * @param replaced
	 *        Whether the lead run had kept a run before, which is now replaced.
	 */
	public record Saved(String id, boolean replaced) {
		// Value type only.
	}

	/**
	 * Thrown when the run is not written within the time a caller waits: it is still being written,
	 * and will be kept.
	 */
	public static class StillWriting extends IllegalStateException {

		/** Creates a {@link StillWriting}. */
		StillWriting(Throwable cause) {
			super("The research run is still being written.", cause);
		}
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
			WriterShutdown.stop(_worker, "research run");
			_worker = null;
		}
		super.shutDown();
	}

	/**
	 * Keeps a research run, or replaces the one its lead run kept before.
	 *
	 * @param report
	 *        What is kept of it.
	 * @param owner
	 *        The account the research ran for; {@code null} for a run without one, which only
	 *        administrators can read.
	 * @return What was kept.
	 * @throws StillWriting
	 *         When the run is not written within the time this call waits; it is written later.
	 * @throws IllegalStateException
	 *         When the run could not be written.
	 */
	public Saved save(ResearchReport report, Person owner) {
		flushArchivedPages(report);
		Future<Saved> write = _worker.submit(() -> ThreadContext.inSystemContext(ResearchRuns.class,
			() -> store(report, owner)));
		try {
			return write.get(WRITE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while the research report was written.", ex);
		} catch (TimeoutException ex) {
			throw new StillWriting(ex);
		} catch (ExecutionException ex) {
			throw new IllegalStateException("The research report could not be written: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Writes what the run read of the pages the report rests on beyond what their copies hold yet,
	 * so that the kept run references copies that show it - also when it is kept while its run
	 * still reads.
	 */
	private static void flushArchivedPages(ResearchReport report) {
		if (!PageArchive.Module.INSTANCE.isActive()) {
			return;
		}
		List<String> handles = new ArrayList<>();
		for (ResearchReport.Source source : report.cited()) {
			handles.add(source.pageHandle());
		}
		for (ResearchReport.Source source : report.alsoRead()) {
			handles.add(source.pageHandle());
		}
		PageArchive.getInstance().flush(handles);
	}

	private Saved store(ResearchReport report, Person owner) {
		TlAiExecutionFactory factory = TlAiExecutionFactory.getInstance();
		Map<String, AgentExecution> executions = executions(report);
		AgentExecution lead = executions.get(report.leadRunId());
		ResearchRun run = lead == null ? null : keptFor(lead);
		boolean replaced = run != null;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase()
			.beginTransaction(ResKey.text("Keep research run: " + report.question()))) {
			if (run == null) {
				run = factory.createResearchRun();
				run.setExecution(lead);
			} else {
				// What the run kept before is replaced as a whole: a part removed from a composition
				// is not deleted with it.
				List<TLObject> before = new ArrayList<>();
				before.addAll(run.getFindings());
				before.addAll(run.getSources());
				run.setFindings(new ArrayList<>());
				run.setSources(new ArrayList<>());
				for (TLObject part : before) {
					part.tDelete();
				}
			}
			run.setQuestion(report.question());
			run.setStatus(status(report.status()));
			run.setEffort(effort(report.effort()));
			run.setPlan(StringServices.nonEmpty(report.plan()));
			run.setReport(report.markdown());
			run.setStarted(date(report.started()));
			run.setFinished(date(report.finished()));
			run.setOwner(owner);
			Map<String, ResearchSource> byKey = new HashMap<>();
			for (ResearchReport.Source source : report.cited()) {
				addSource(factory, run, source, byKey);
			}
			for (ResearchReport.Source source : report.alsoRead()) {
				addSource(factory, run, source, byKey);
			}
			for (Finding finding : report.findings()) {
				ResearchFinding result = factory.createResearchFinding();
				result.setFindingId(finding.id());
				result.setClaim(finding.claim());
				result.setQuote(finding.quote());
				result.setSource(byKey.get(ResearchReport.key(finding.source())));
				result.setRecordedBy(executions.get(finding.runId()));
				run.addFinding(result);
			}
			tx.commit();
			return new Saved(ReadFilter.idOf(run), replaced);
		}
	}

	private static void addSource(TlAiExecutionFactory factory, ResearchRun run, ResearchReport.Source source,
			Map<String, ResearchSource> byKey) {
		ResearchSource result = factory.createResearchSource();
		ArchivedPage page = PageArchive.find(source.pageHandle()).orElse(null);
		if (page != null) {
			result.setPage(page);
		} else if (source.object() != null) {
			result.setObject(source.object());
			// What the source shows once the object is deleted, which clears the reference; the id
			// is what the report cites, so its footnote keeps its number.
			result.setTitle(StringServices.nonEmpty(source.title()));
			result.setUrl(StringServices.nonEmpty(source.reference()));
		} else {
			// Neither reference applies - a page that was not archived, a record that is no model
			// object: the source is kept with what the reading list says of it.
			result.setTitle(StringServices.nonEmpty(source.title()));
			result.setUrl(StringServices.nonEmpty(source.url().isEmpty() ? source.reference() : source.url()));
		}
		result.setKind(StringServices.nonEmpty(source.kind()));
		result.setCited(Boolean.valueOf(source.isCited()));
		result.setFootnote(source.isCited() ? Integer.valueOf(source.footnote()) : null);
		result.setReadAt(date(source.readAt()));
		run.addSource(result);
		byKey.putIfAbsent(ResearchReport.key(source.reference()), result);
	}

	/**
	 * The recorded executions of the lead run and of the runs that recorded findings, by run id.
	 *
	 * <p>
	 * Read here, on this service's thread at the current revision, where the rows the execution
	 * store wrote on its own thread are visible.
	 * </p>
	 */
	private static Map<String, AgentExecution> executions(ResearchReport report) {
		Set<String> wanted = new HashSet<>();
		if (!report.leadRunId().isEmpty()) {
			wanted.add(report.leadRunId());
		}
		for (Finding finding : report.findings()) {
			if (!finding.runId().isEmpty()) {
				wanted.add(finding.runId());
			}
		}
		Map<String, AgentExecution> result = new HashMap<>();
		if (wanted.isEmpty()) {
			return result;
		}
		for (TLObject object : MetaElementUtil.getAllInstancesOf(TlAiExecutionFactory.getAgentExecutionType(),
			TLObject.class)) {
			AgentExecution execution = (AgentExecution) object;
			String runId = execution.getRunId();
			if (runId != null && wanted.contains(runId)) {
				result.put(runId, execution);
			}
		}
		return result;
	}

	/** The run the given lead run kept before, or {@code null}. */
	private static ResearchRun keptFor(AgentExecution lead) {
		for (TLObject referer : lead.tReferers(TlAiExecutionFactory.getExecutionResearchRunAttr())) {
			if (referer instanceof ResearchRun run) {
				return run;
			}
		}
		return null;
	}

	private static TLClassifier status(String status) {
		return ResearchReport.PARTIAL.equals(status)
			? TlAiExecutionFactory.getPARTIALResearchStatusClassifier()
			: TlAiExecutionFactory.getCOMPLETEResearchStatusClassifier();
	}

	private static TLClassifier effort(String effort) {
		switch (effort) {
			case ResearchReport.QUICK:
				return TlAiExecutionFactory.getQUICKResearchEffortClassifier();
			case ResearchReport.STANDARD:
				return TlAiExecutionFactory.getSTANDARDResearchEffortClassifier();
			case ResearchReport.DEEP:
				return TlAiExecutionFactory.getDEEPResearchEffortClassifier();
			default:
				return null;
		}
	}

	private static Date date(Instant instant) {
		return instant == null ? null : Date.from(instant);
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
