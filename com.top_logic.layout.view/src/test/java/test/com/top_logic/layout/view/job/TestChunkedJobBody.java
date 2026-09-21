/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.layout.view.job.ChunkedJobBody;
import com.top_logic.layout.view.job.I18NConstants;
import com.top_logic.layout.view.job.JobMonitor;

/**
 * Tests what a {@link ChunkedJobBody} commits and what it reports: the chunks that are committed
 * one by one, the item that is skipped because it cannot be processed, the failure of the
 * preparation that ends the job, the cancel that is answered between two chunks, and the steps the
 * job announces and passes through.
 *
 * <p>
 * The body is driven through its public seam - the run of the job with a monitor recording what is
 * reported - so what a configured body produces is what is asserted here. The work items are
 * created as objects of the knowledge base, and what is committed is read back from it.
 * </p>
 */
public class TestChunkedJobBody extends AbstractDBKnowledgeBaseTest {

	/** The item that cannot be processed. */
	private static final String BAD = "bad";

	/** What the failure of an item says. */
	private static final String ITEM_FAILURE = "This element cannot be processed.";

	/** What the failure of the preparation says. */
	private static final String INIT_FAILURE = "The preparation failed.";

	/** What the job reports to. */
	private RecordingJobMonitor _monitor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_monitor = new RecordingJobMonitor();
	}

	@Override
	protected void tearDown() throws Exception {
		_monitor = null;

		super.tearDown();
	}

	/**
	 * Tests that every chunk is committed on its own: while a chunk is processed, the objects of the
	 * chunks before it are committed and those of the chunk itself are not.
	 */
	public void testEveryChunkCommitsOnItsOwn() throws Exception {
		List<String> items = items(7);
		List<List<String>> committedAtChunkStart = new ArrayList<>();

		TestBody body = new TestBody(3, items);
		body.step((index, chunk, state) -> {
			committedAtChunkStart.add(committed());
			create(chunk);
		});

		body.run(_monitor, List.of());

		assertEquals("Every item was committed.", items, committed());
		assertEquals("The seven items were processed in three chunks.", 3, committedAtChunkStart.size());
		assertEquals("Nothing is committed before the first chunk.", List.of(), committedAtChunkStart.get(0));
		assertEquals("The first chunk is committed while the second is processed.",
			items.subList(0, 3), committedAtChunkStart.get(1));
		assertEquals("The first two chunks are committed while the third is processed.",
			items.subList(0, 6), committedAtChunkStart.get(2));
		assertEquals("The progress of the pass counts its chunks.",
			List.of("0/3", "1/3", "2/3", "3/3"), _monitor._progress);
	}

	/**
	 * Tests that a chunk holding an item that cannot be processed is retried item by item: the other
	 * items of that chunk are committed, the failing one is skipped, named in a message and counted
	 * in what the job ends with.
	 */
	public void testAFailingItemIsSkippedAndTheChunkIsRetried() throws Exception {
		List<String> items = List.of("e1", BAD, "e3", "e4");

		TestBody body = new TestBody(3, items);
		body.step((index, chunk, state) -> create(chunk));

		Object result = body.run(_monitor, List.of());

		assertEquals("The items that can be processed are committed, the failing one is not.",
			List.of("e1", "e3", "e4"), committed());
		assertTrue("The skipped item is named in a message: " + _monitor._messages,
			_monitor._messages.contains(I18NConstants.SKIPPED_ELEMENT__ELEMENT_CAUSE.fill(BAD, ITEM_FAILURE)));

		ResKey summary = I18NConstants.RESULT__PROCESSED_SKIPPED.fill(Integer.valueOf(4), Integer.valueOf(1));
		assertEquals("The job says how many items it processed and how many it skipped.",
			summary, _monitor.lastMessage());
		assertEquals("Without a completion, the result of the job is what it says about its items.",
			summary, result);
	}

	/**
	 * Tests that a failing preparation ends the job with its own failure and commits nothing - not
	 * even what the preparation itself produced.
	 */
	public void testAFailingPreparationEndsTheJob() throws Exception {
		TestBody body = new TestBody(3, items(3));
		body.init((job, arguments) -> {
			newB("prepared");
			throw new RuntimeException(INIT_FAILURE);
		});
		body.step((index, chunk, state) -> create(chunk));

		try {
			body.run(_monitor, List.of());
			fail("The failure of the preparation must end the job.");
		} catch (RuntimeException ex) {
			assertEquals(INIT_FAILURE, ex.getMessage());
		}

		assertEquals("A failing preparation commits nothing.", List.of(), committed());
		assertEquals("The job did not reach a pass.", List.of("init"), _monitor._entered);
	}

	/**
	 * Tests that a cancel is answered between two chunks: the job ends by abandoning its work, the
	 * chunk that was committed is kept and the chunks behind it are never processed.
	 */
	public void testACancelIsAnsweredBetweenTwoChunks() throws Exception {
		List<String> items = items(6);

		TestBody body = new TestBody(3, items);
		body.step((index, chunk, state) -> {
			create(chunk);
			_monitor.cancel();
		});

		try {
			body.run(_monitor, List.of());
			fail("A cancelled job must abandon its work.");
		} catch (AbortExecutionException ex) {
			// The job was cancelled, which is how it ends.
		}

		assertEquals("The chunk that was committed is kept, the chunk behind it was not processed.",
			items.subList(0, 3), committed());
		assertNull("A cancelled job does not say how many items it processed.", _monitor.lastMessage());
	}

	/**
	 * Tests the steps a job with a preparation, two passes and a completion announces and passes
	 * through, and that its result is what the completion produced.
	 */
	public void testTheStepsOfTheJobAndItsResult() throws Exception {
		List<String> items = items(2);
		List<String> passes = new ArrayList<>();

		TestBody body = new TestBody(2, items);
		body.init((job, arguments) -> "state");
		body.step((index, chunk, state) -> passes.add("first " + chunk + " of " + state));
		body.step((index, chunk, state) -> passes.add("second " + chunk + " of " + state));
		body.finish((job, state) -> "produced by " + state);

		Object result = body.run(_monitor, List.of());

		assertEquals("The job announces the preparation, every pass and the completion.",
			List.of("init", "step-1", "step-2", "finish"), _monitor.phaseNames());
		assertEquals("Every announced step carries the text the reader sees.",
			List.of(I18NConstants.PHASE_INIT,
				I18NConstants.PHASE_STEP__NUMBER.fill(Integer.valueOf(1)),
				I18NConstants.PHASE_STEP__NUMBER.fill(Integer.valueOf(2)),
				I18NConstants.PHASE_FINISH),
			_monitor.phaseLabels());
		assertEquals("The job passes through the steps it announced.",
			List.of("init", "step-1", "step-2", "finish"), _monitor._entered);
		assertEquals("A pass begins once the pass before it has worked through every item.",
			List.of("first [e1, e2] of state", "second [e1, e2] of state"), passes);
		assertEquals("The preparation and the completion do not know how much of their work is done.",
			2, _monitor._indeterminate);
		assertEquals("The result of the job is what the completion produced.", "produced by state", result);
		assertEquals("The job ends by saying how many items it processed and how many it skipped.",
			I18NConstants.RESULT__PROCESSED_SKIPPED.fill(Integer.valueOf(2), Integer.valueOf(0)), _monitor.lastMessage());
	}

	/**
	 * Tests that a job without a preparation announces no step for it and works against the value it
	 * was started with.
	 */
	public void testWithoutAPreparationTheStateIsTheFirstArgument() throws Exception {
		List<String> items = items(2);
		List<Object> states = new ArrayList<>();

		TestBody body = new TestBody(2, items);
		body.step((index, chunk, state) -> states.add(state));

		body.run(_monitor, List.of("the argument", "another argument"));

		assertEquals("Without a preparation, the job announces its passes alone.",
			List.of("step-1"), _monitor.phaseNames());
		assertEquals("The state is the value the job was started with.", List.of("the argument"), states);
		assertEquals("The state reaches the hook asking for the work items.",
			List.of("the argument"), body._elementsStates);
	}

	/**
	 * Tests that a job without a preparation that was started without values works against nothing.
	 */
	public void testWithoutAPreparationAndWithoutArgumentsTheStateIsNothing() throws Exception {
		TestBody body = new TestBody(2, items(1));
		body.step((index, chunk, state) -> {
			// Nothing to do, the state the hook received is what is asserted.
		});

		body.run(_monitor, List.of());

		assertEquals("A job started without values works against nothing.",
			Collections.singletonList(null), body._elementsStates);
	}

	/**
	 * Tests that the text the reader sees for a step is what the body says it is.
	 */
	public void testTheStepsCanBeNamedByTheBody() throws Exception {
		TestBody body = new TestBody(2, items(1)) {
			@Override
			protected ResKey initLabel() {
				return ResKey.text("Reading");
			}

			@Override
			protected ResKey stepLabel(int index) {
				return ResKey.text("Writing " + index);
			}

			@Override
			protected ResKey finishLabel() {
				return ResKey.text("Cleaning up");
			}
		};
		body.init((job, arguments) -> "state");
		body.step((index, chunk, state) -> {
			// Nothing to do, the announced steps are what is asserted.
		});
		body.finish((job, state) -> null);

		body.run(_monitor, List.of());

		assertEquals("The body names every step the reader sees.",
			List.of(ResKey.text("Reading"), ResKey.text("Writing 0"), ResKey.text("Cleaning up")), _monitor.phaseLabels());
	}

	/** The work items {@code e1} to {@code eN}. */
	private static List<String> items(int count) {
		List<String> result = new ArrayList<>();
		for (int n = 1; n <= count; n++) {
			result.add("e" + n);
		}
		return result;
	}

	/**
	 * Creates an object for every item of the given chunk, failing on {@link #BAD}.
	 */
	private void create(List<?> chunk) throws Exception {
		for (Object item : chunk) {
			if (BAD.equals(item)) {
				throw new RuntimeException(ITEM_FAILURE);
			}
			newB((String) item);
		}
	}

	/**
	 * The names of the objects the knowledge base holds, in their order.
	 */
	private List<String> committed() throws Exception {
		List<String> result = new ArrayList<>();
		for (KnowledgeObject object : kb().getAllKnowledgeObjects(B_NAME)) {
			result.add((String) object.getAttributeValue(A1_NAME));
		}
		Collections.sort(result);
		return result;
	}

	/** The preparation of a {@link TestBody}. */
	@FunctionalInterface
	private interface InitHook {
		/** Produces the state the later hooks receive. */
		Object init(JobMonitor job, List<Object> arguments) throws Exception;
	}

	/** One pass of a {@link TestBody}. */
	@FunctionalInterface
	private interface StepHook {
		/** Processes one chunk of work items. */
		void step(int index, List<?> chunk, Object state) throws Exception;
	}

	/** The completion of a {@link TestBody}. */
	@FunctionalInterface
	private interface FinishHook {
		/** Produces the result of the job. */
		Object finish(JobMonitor job, Object state) throws Exception;
	}

	/**
	 * A {@link ChunkedJobBody} whose hooks are given as functions, so that a test writes only the
	 * ones it is about.
	 */
	private static class TestBody extends ChunkedJobBody {

		private final List<?> _items;

		private final List<StepHook> _steps = new ArrayList<>();

		private InitHook _init;

		private FinishHook _finish;

		/** The states the hook asking for the work items received. */
		final List<Object> _elementsStates = new ArrayList<>();

		/**
		 * Creates a {@link TestBody} working through the given items.
		 */
		TestBody(int chunkSize, List<?> items) {
			super(chunkSize);

			_items = items;
		}

		/** Gives this body a preparation. */
		void init(InitHook init) {
			_init = init;
		}

		/** Adds a pass over the work items. */
		void step(StepHook step) {
			_steps.add(step);
		}

		/** Gives this body a completion. */
		void finish(FinishHook finish) {
			_finish = finish;
		}

		@Override
		protected boolean hasInit() {
			return _init != null;
		}

		@Override
		protected Object init(JobMonitor job, List<Object> arguments) {
			try {
				return _init.init(job, arguments);
			} catch (Exception ex) {
				throw unchecked(ex);
			}
		}

		@Override
		protected List<?> elements(JobMonitor job, Object state) {
			_elementsStates.add(state);
			return _items;
		}

		@Override
		protected int stepCount() {
			return _steps.size();
		}

		@Override
		protected void step(JobMonitor job, int index, List<?> chunk, Object state) {
			try {
				_steps.get(index).step(index, chunk, state);
			} catch (Exception ex) {
				throw unchecked(ex);
			}
		}

		@Override
		protected boolean hasFinish() {
			return _finish != null;
		}

		@Override
		protected Object finish(JobMonitor job, Object state) {
			try {
				return _finish.finish(job, state);
			} catch (Exception ex) {
				throw unchecked(ex);
			}
		}

		/** The given failure as the body raises it. */
		private static RuntimeException unchecked(Exception failure) {
			if (failure instanceof RuntimeException runtime) {
				return runtime;
			}
			return new RuntimeException(failure);
		}

	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestChunkedJobBody.class);
	}

}
