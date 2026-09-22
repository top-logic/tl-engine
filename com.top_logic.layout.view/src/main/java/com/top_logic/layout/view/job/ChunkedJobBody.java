/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;

/**
 * The work of a long-running job that changes persistent objects, committed in chunks.
 *
 * <p>
 * A job body runs outside any transaction. This is the transactional frame around work that
 * produces persistent objects: it walks the work items in chunks and commits every chunk on its
 * own, so memory stays bounded, a failure late in the run keeps what the earlier chunks produced,
 * and a single item that cannot be processed costs only itself.
 * </p>
 *
 * <p>
 * The work is described in four hooks a subclass fills:
 * </p>
 *
 * <ol>
 * <li>The preparation runs once, in a transaction, and produces the <em>state</em> every later hook
 * receives - typically the container the items are created in, which must be persistent before they
 * are. Where a subclass has no preparation, the state is the first value the job was started
 * with.</li>
 * <li>The work items are asked for once, read-only and outside any transaction, against that
 * state.</li>
 * <li>Every pass is a full run over those items: it is applied to successive chunks, each chunk in
 * a transaction of its own. A chunk that fails is retried item by item, so that only the items that
 * genuinely cannot be processed are skipped, each of them reported and counted. A pass begins once
 * the pass before it has committed every chunk, which is what makes a later pass the place for work
 * needing all items of an earlier one - resolving cross references between them, for instance.</li>
 * <li>The completion runs once after every pass has committed, in a transaction of its own, against
 * the state. It is the one-shot finalizer that has to see everything committed; its value is the
 * result of the job.</li>
 * </ol>
 *
 * <p>
 * What the reader sees of all this is the job: the phases are announced as the steps of the job -
 * the preparation, every pass, the completion - and the progress within a pass counts its chunks. A
 * cancel takes effect between two committed chunks. The job ends by saying how many items it
 * processed and how many it skipped.
 * </p>
 *
 * @implNote A failure of {@link #init(JobMonitor, List)} or of {@link #finish(JobMonitor, Object)}
 *           is not caught, so it ends the job with its own message; only a failing chunk is retried
 *           and given up on item by item. {@link #hasInit()} and {@link #hasFinish()} are asked
 *           before the job starts, because the phases it announces through
 *           {@link JobMonitor#setPhases(List)} depend on them.
 */
public abstract class ChunkedJobBody implements JobBody {

	/** Name of the phase the preparation is reported as. */
	private static final String PHASE_INIT = "init";

	/** Prefix of the name a pass is reported as, followed by its one-based number. */
	private static final String PHASE_STEP_PREFIX = "step-";

	/** Name of the phase the completion is reported as. */
	private static final String PHASE_FINISH = "finish";

	private final int _chunkSize;

	/**
	 * Creates a {@link ChunkedJobBody}.
	 *
	 * @param chunkSize
	 *        How many work items one transaction takes; a value below one is raised to one.
	 */
	protected ChunkedJobBody(int chunkSize) {
		_chunkSize = Math.max(1, chunkSize);
	}

	/**
	 * Whether this body has a preparation, so that the job announces a phase for it and the state
	 * the later hooks receive is what {@link #init(JobMonitor, List)} produces.
	 *
	 * @return Whether {@link #init(JobMonitor, List)} is to be called; where it is not, the state is
	 *         the first value the job was started with, {@code null} for a job started without
	 *         values.
	 */
	protected abstract boolean hasInit();

	/**
	 * Prepares the work and produces the state every later hook receives.
	 *
	 * <p>
	 * Called once, in a transaction of its own, and only where {@link #hasInit()} says there is a
	 * preparation. A failure here is not caught: it ends the job, and the transaction is given up
	 * with it.
	 * </p>
	 *
	 * @param job
	 *        What to report to.
	 * @param arguments
	 *        The values the job was started with.
	 * @return What the later hooks work against.
	 */
	protected abstract Object init(JobMonitor job, List<Object> arguments);

	/**
	 * The items the passes work through.
	 *
	 * <p>
	 * Asked once, read-only and outside any transaction. The same list is handed to every pass, in
	 * chunks.
	 * </p>
	 *
	 * @param job
	 *        What to report to.
	 * @param state
	 *        What the preparation produced.
	 * @return The work items; {@code null} for none.
	 */
	protected abstract List<?> elements(JobMonitor job, Object state);

	/**
	 * How many passes are made over the work items.
	 *
	 * @return The number of times {@link #step(JobMonitor, int, List, Object)} runs over the whole
	 *         list, each pass chunk by chunk.
	 */
	protected abstract int stepCount();

	/**
	 * Processes one chunk of work items in one pass.
	 *
	 * <p>
	 * Called inside a transaction which is committed once this returns. A failure gives the
	 * transaction up and has the chunk retried item by item, each item in a transaction of its own,
	 * so this must be repeatable for an item it already saw in the failed chunk.
	 * </p>
	 *
	 * @param job
	 *        What to report to.
	 * @param index
	 *        The pass this chunk belongs to, counted from zero.
	 * @param chunk
	 *        The items to process, a part of what {@link #elements(JobMonitor, Object)} produced,
	 *        and a single item while a failed chunk is retried.
	 * @param state
	 *        What the preparation produced.
	 */
	protected abstract void step(JobMonitor job, int index, List<?> chunk, Object state);

	/**
	 * Whether this body has a completion, so that the job announces a phase for it and its result is
	 * what {@link #finish(JobMonitor, Object)} produces.
	 *
	 * @return Whether {@link #finish(JobMonitor, Object)} is to be called; where it is not, the
	 *         result of the job is the text saying how many items were processed and how many were
	 *         skipped.
	 */
	protected abstract boolean hasFinish();

	/**
	 * Completes the work, once every pass has committed.
	 *
	 * <p>
	 * Called once, in a transaction of its own, and only where {@link #hasFinish()} says there is a
	 * completion. A failure here is not caught: it ends the job, and the transaction is given up
	 * with it.
	 * </p>
	 *
	 * @param job
	 *        What to report to.
	 * @param state
	 *        What the preparation produced.
	 * @return The result of the job.
	 */
	protected abstract Object finish(JobMonitor job, Object state);

	/**
	 * What the reader sees for the preparation, the step the job begins with.
	 */
	protected ResKey initLabel() {
		return I18NConstants.PHASE_INIT;
	}

	/**
	 * What the reader sees for one of the passes.
	 *
	 * @param index
	 *        The pass, counted from zero.
	 */
	protected ResKey stepLabel(int index) {
		return I18NConstants.PHASE_STEP__NUMBER.fill(Integer.valueOf(index + 1));
	}

	/**
	 * What the reader sees for the completion, the step the job ends with.
	 */
	protected ResKey finishLabel() {
		return I18NConstants.PHASE_FINISH;
	}

	@Override
	public final Object run(JobMonitor job, List<Object> arguments) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		boolean completing = hasFinish();
		job.setPhases(phases());

		Object state = prepare(job, kb, arguments);
		List<?> items = elements(job, state);
		if (items == null) {
			items = Collections.emptyList();
		}

		int skipped = 0;
		for (int n = 0, cnt = stepCount(); n < cnt; n++) {
			job.beginPhase(stepPhase(n));
			skipped += runPass(job, kb, n, items, state);
		}

		Object produced = completing ? complete(job, kb, state) : null;

		ResKey summary =
			I18NConstants.RESULT__PROCESSED_SKIPPED.fill(Integer.valueOf(items.size()), Integer.valueOf(skipped));
		job.message(summary);
		return completing ? produced : summary;
	}

	/**
	 * The steps the job announces it goes through: the preparation where there is one, every pass,
	 * and the completion where there is one.
	 */
	private List<JobPhase> phases() {
		List<JobPhase> result = new ArrayList<>();
		if (hasInit()) {
			result.add(new JobPhase(PHASE_INIT, initLabel()));
		}
		for (int n = 0, cnt = stepCount(); n < cnt; n++) {
			result.add(new JobPhase(stepPhase(n), stepLabel(n)));
		}
		if (hasFinish()) {
			result.add(new JobPhase(PHASE_FINISH, finishLabel()));
		}
		return result;
	}

	/**
	 * The name the pass with the given index is reported as.
	 */
	private static String stepPhase(int index) {
		return PHASE_STEP_PREFIX + (index + 1);
	}

	/**
	 * Runs the preparation and answers the state the later hooks work against.
	 */
	private Object prepare(JobMonitor job, KnowledgeBase kb, List<Object> arguments) {
		if (!hasInit()) {
			return arguments.isEmpty() ? null : arguments.get(0);
		}

		job.beginPhase(PHASE_INIT);
		job.indeterminate();
		try (Transaction tx = kb.beginTransaction(I18NConstants.COMMIT_JOB)) {
			Object state = init(job, arguments);
			tx.commit();
			return state;
		}
	}

	/**
	 * Runs the completion and answers what it produced.
	 */
	private Object complete(JobMonitor job, KnowledgeBase kb, Object state) {
		job.beginPhase(PHASE_FINISH);
		job.indeterminate();
		try (Transaction tx = kb.beginTransaction(I18NConstants.COMMIT_JOB)) {
			Object result = finish(job, state);
			tx.commit();
			return result;
		}
	}

	/**
	 * Runs one pass over all work items, chunk by chunk, and answers how many items it skipped.
	 *
	 * <p>
	 * A chunk that fails is retried item by item, so only the items that genuinely cannot be
	 * processed are given up. The cancellation of the job is answered between two chunks, both of
	 * which have committed.
	 * </p>
	 */
	private int runPass(JobMonitor job, KnowledgeBase kb, int index, List<?> items, Object state) {
		int total = items.size();
		int chunks = (total + _chunkSize - 1) / _chunkSize;
		int done = 0;
		int skipped = 0;
		for (int start = 0; start < total; start += _chunkSize) {
			job.checkCancelled();
			job.progress(done, chunks);

			List<?> chunk = new ArrayList<>(items.subList(start, Math.min(start + _chunkSize, total)));
			try (Transaction tx = kb.beginTransaction(I18NConstants.COMMIT_JOB)) {
				step(job, index, chunk, state);
				tx.commit();
			} catch (RuntimeException ex) {
				if (JobRunner.isAbort(ex)) {
					throw ex;
				}
				// A cancelled job ends here rather than reporting the chunk as failed: what stopped
				// it is the interrupt of the work it was waiting for, not a defect of the chunk.
				job.checkCancelled();
				Logger.warn("Chunk [" + (start + 1) + ".." + (start + chunk.size())
					+ "] failed; retrying its items one by one.", ex, ChunkedJobBody.class);
				skipped += retry(job, kb, index, chunk, state);
			}
			done++;
		}
		job.progress(done, chunks);
		return skipped;
	}

	/**
	 * Applies the given pass to every item of a failed chunk on its own and answers how many of them
	 * were skipped.
	 */
	private int retry(JobMonitor job, KnowledgeBase kb, int index, List<?> chunk, Object state) {
		int skipped = 0;
		for (Object item : chunk) {
			try (Transaction tx = kb.beginTransaction(I18NConstants.COMMIT_JOB)) {
				step(job, index, Collections.singletonList(item), state);
				tx.commit();
			} catch (RuntimeException ex) {
				if (JobRunner.isAbort(ex)) {
					throw ex;
				}
				Logger.error("Skipping an element that cannot be processed: " + item, ex, ChunkedJobBody.class);
				job.message(I18NConstants.SKIPPED_ELEMENT__ELEMENT_CAUSE.fill(item, cause(ex)));
				skipped++;
			}
		}
		return skipped;
	}

	/**
	 * What the given failure says, for the message naming the skipped element.
	 */
	private static String cause(RuntimeException failure) {
		String message = failure.getMessage();
		return message == null ? failure.getClass().getName() : message;
	}

}
