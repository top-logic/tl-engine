/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.job.ChunkedScriptJobBody;
import com.top_logic.layout.view.job.I18NConstants;
import com.top_logic.layout.view.job.JobRunner;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Tests the job body that is written as TL-Script and committed in chunks: the order the scripts
 * are called with their arguments in, the state they are threaded with, the chunks the work items
 * reach a pass in, the item that is skipped because it cannot be processed, and the cancel reported
 * from inside a chunk.
 *
 * <p>
 * The body is driven through its public seam - the run of the job with a monitor recording what the
 * scripts reported - and the scripts touch the application model in no way, so what they say about
 * their arguments is what is asserted here.
 * </p>
 *
 * @implNote The body is built from scripts compiled the way
 *           {@link AbstractJobTest#compile(String)} does it, because the compilation of a
 *           configured expression resolves the primitive types against the model of a running
 *           application, which a test over the knowledge base alone does not start.
 *           {@link #testTheConfigurationNamesThePasses()} is the one test taking the
 *           configuration path, which it can because a body without work items runs no script at
 *           all.
 */
public class TestChunkedScriptJobBody extends AbstractDBKnowledgeBaseTest {

	/** The item that cannot be processed. */
	private static final String BAD = "bad";

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
	 * Tests that every script is called with the monitor of the job first, that the preparation
	 * receives the values the job was started with, and that what it produced reaches the work
	 * items, every pass and the completion.
	 */
	public void testEveryScriptIsCalledWithTheJobAndTheState() throws Exception {
		ChunkedScriptJobBody body = body(5,
			"job -> a -> b -> { $job.jobMessage('init:' + $a + ':' + $b); $a + '/' + $b; }",
			"job -> state -> { $job.jobMessage('elements:' + $state); ['e1', 'e2']; }",
			List.of("job -> chunk -> state -> $chunk.foreach(e -> $job.jobMessage($e + '@' + $state))"),
			"job -> state -> 'finished:' + $state");

		Object result = body.run(_monitor, List.of("x", "y"));

		assertEquals("The result of the job is what the completion produced.", "finished:x/y", result);
		assertEquals("Every script sees the job and the state the preparation produced.",
			List.of(ResKey.text("init:x:y"),
				ResKey.text("elements:x/y"),
				ResKey.text("e1@x/y"),
				ResKey.text("e2@x/y"),
				processed(2, 0)),
			_monitor._messages);
		assertEquals("The job announces the preparation, the pass and the completion.",
			List.of("init", "step-1", "finish"), _monitor.phaseNames());
	}

	/**
	 * Tests that the work items reach a pass in chunks of the configured size, the last of them
	 * holding what is left over.
	 */
	public void testTheItemsReachAPassInChunks() throws Exception {
		ChunkedScriptJobBody body = body(2, null,
			"job -> state -> ['e1', 'e2', 'e3', 'e4', 'e5']",
			List.of("job -> chunk -> state -> { $job.jobMessage('chunk'); $chunk.foreach(e -> $job.jobMessage($e)); }"),
			null);

		body.run(_monitor, List.of());

		assertEquals("Five items reach the pass as three chunks of two, two and one.",
			List.of(ResKey.text("chunk"), ResKey.text("e1"), ResKey.text("e2"),
				ResKey.text("chunk"), ResKey.text("e3"), ResKey.text("e4"),
				ResKey.text("chunk"), ResKey.text("e5"),
				processed(5, 0)),
			_monitor._messages);
		assertEquals("The progress of the pass counts its chunks.",
			List.of("0/3", "1/3", "2/3", "3/3"), _monitor._progress);
	}

	/**
	 * Tests that an item a script cannot process is skipped: its chunk is retried item by item, the
	 * other items of that chunk are processed all the same, and the skipped one is named in a
	 * message and counted in what the job ends with.
	 */
	public void testAFailingItemIsSkippedAndTheRestOfItsChunkIsProcessed() throws Exception {
		ChunkedScriptJobBody body = body(2, null,
			"job -> state -> ['e1', '" + BAD + "', 'e3', 'e4', 'e5']",
			List.of("job -> chunk -> state -> $chunk.foreach(e -> "
				+ "if($e == '" + BAD + "', throw('This element cannot be processed.'), $job.jobMessage($e)))"),
			null);

		Object result = body.run(_monitor, List.of());

		assertEquals("The item before the failing one is processed twice: once in the chunk that"
			+ " failed and once while that chunk is retried item by item.",
			List.of(ResKey.text("e1"), ResKey.text("e1"), ResKey.text("e3"), ResKey.text("e4"),
				ResKey.text("e5")),
			texts());
		List<ResKey> skipped = skipped();
		assertEquals("Exactly one item is given up on: " + skipped, 1, skipped.size());
		assertTrue("The message names the item that was skipped: " + skipped.get(0),
			skipped.get(0).toString().contains(BAD));
		assertEquals("The job says how many items it processed and how many it skipped.",
			processed(5, 1), _monitor.lastMessage());
		assertEquals("Without a completion, the result of the job is what it says about its items.",
			processed(5, 1), result);
	}

	/**
	 * Tests that a job without a preparation works against the first value it was started with, and
	 * that a job without a completion ends with what it says about its items.
	 */
	public void testWithoutAPreparationTheStateIsTheFirstArgument() throws Exception {
		ChunkedScriptJobBody body = body(2, null,
			"job -> state -> [$state]",
			List.of("job -> chunk -> state -> $job.jobMessage('state:' + $state)"),
			null);

		Object result = body.run(_monitor, List.of("first", "second"));

		assertEquals("Without a preparation, the job announces its passes alone.",
			List.of("step-1"), _monitor.phaseNames());
		assertEquals("The state is the first value the job was started with.",
			List.of(ResKey.text("state:first")), texts());
		assertEquals("Without a completion, the result of the job is what it says about its items.",
			processed(1, 0), result);
	}

	/**
	 * Tests that a cancel reported from inside a chunk ends the job at once: the work is abandoned
	 * rather than the item being reported as one that cannot be processed.
	 */
	public void testACancelFromInsideAChunkAbandonsTheJob() throws Exception {
		_monitor = new RecordingJobMonitor() {
			@Override
			public void message(ResKey message) {
				cancel();

				super.message(message);
			}
		};

		ChunkedScriptJobBody body = body(2, null,
			"job -> state -> ['e1', 'e2', 'e3', 'e4']",
			List.of("job -> chunk -> state -> $chunk.foreach(e -> $job.jobMessage($e))"),
			null);

		try {
			body.run(_monitor, List.of());
			fail("A cancelled job must abandon its work.");
		} catch (RuntimeException ex) {
			assertTrue("The job ends by saying that its work was abandoned: " + ex,
				JobRunner.isAbort(ex));
		}

		assertEquals("A cancelled item is not one that cannot be processed.", List.of(), skipped());
		assertEquals("The job was abandoned before it said anything.", List.of(), _monitor._messages);
		assertNull("A cancelled job does not say how many items it processed.", _monitor.lastMessage());
	}

	/**
	 * Tests that the configuration names the passes of the job: a pass is seen as what it is
	 * configured with, and one that is not named by its number.
	 */
	public void testTheConfigurationNamesThePasses() throws Exception {
		ChunkedScriptJobBody.Config config = TypedConfiguration.newConfigItem(ChunkedScriptJobBody.Config.class);
		TypedConfigUtil.setProperty(config, ChunkedScriptJobBody.Config.STEPS,
			List.of(step("job -> chunk -> state -> null", ResKey.text("Creating")),
				step("job -> chunk -> state -> null", null)));

		ChunkedScriptJobBody body = TypedConfigUtil.createInstance(config);
		Object result = body.run(_monitor, List.of());

		assertEquals("The job announces the configured passes alone.",
			List.of("step-1", "step-2"), _monitor.phaseNames());
		assertEquals("A pass is seen as what it is configured with, and one that is not by its number.",
			List.of(ResKey.text("Creating"), I18NConstants.PHASE_STEP__NUMBER.fill(Integer.valueOf(2))),
			_monitor.phaseLabels());
		assertEquals("A job without work items processes nothing.", processed(0, 0), result);
	}

	/**
	 * A body running the given scripts, each of them compiled without an application model.
	 *
	 * @param chunkSize
	 *        How many work items one transaction takes.
	 * @param init
	 *        The preparation, {@code null} for a body without one.
	 * @param elements
	 *        The script producing the work items.
	 * @param steps
	 *        The passes over the work items.
	 * @param finish
	 *        The completion, {@code null} for a body without one.
	 */
	private static ChunkedScriptJobBody body(int chunkSize, String init, String elements, List<String> steps,
			String finish) {
		List<QueryExecutor> passes = new ArrayList<>();
		for (String step : steps) {
			passes.add(AbstractJobTest.compile(step));
		}
		return new ChunkedScriptJobBody(chunkSize, compileOptional(init), compileOptional(elements), passes,
			compileOptional(finish));
	}

	/** The given script, compiled without an application model; {@code null} for no script. */
	private static QueryExecutor compileOptional(String source) {
		return source == null ? null : AbstractJobTest.compile(source);
	}

	/** One configured pass over the work items. */
	private static ChunkedScriptJobBody.Config.Step step(String source, ResKey label) {
		ChunkedScriptJobBody.Config.Step result =
			TypedConfiguration.newConfigItem(ChunkedScriptJobBody.Config.Step.class);
		TypedConfigUtil.setProperty(result, ChunkedScriptJobBody.Config.Step.EXPR, AbstractJobTest.expr(source));
		TypedConfigUtil.setProperty(result, ChunkedScriptJobBody.Config.Step.LABEL, label);
		return result;
	}

	/** What the job says about the items it worked through. */
	private static ResKey processed(int items, int skipped) {
		return I18NConstants.RESULT__PROCESSED_SKIPPED.fill(Integer.valueOf(items), Integer.valueOf(skipped));
	}

	/** What the scripts reported, without what the frame itself says about the work items. */
	private List<ResKey> texts() {
		List<ResKey> result = new ArrayList<>();
		for (ResKey message : _monitor._messages) {
			if (!isSkipped(message) && message.plain() != I18NConstants.RESULT__PROCESSED_SKIPPED) {
				result.add(message);
			}
		}
		return result;
	}

	/** The messages naming an item the job gave up on. */
	private List<ResKey> skipped() {
		List<ResKey> result = new ArrayList<>();
		for (ResKey message : _monitor._messages) {
			if (isSkipped(message)) {
				result.add(message);
			}
		}
		return result;
	}

	/** Whether the given message names an item that could not be processed. */
	private static boolean isSkipped(ResKey message) {
		return message.plain() == I18NConstants.SKIPPED_ELEMENT__ELEMENT_CAUSE;
	}

	/**
	 * Suite of tests, requiring the knowledge base the chunks are committed to, the
	 * {@link TypeIndex} the script functions are discovered through and the {@link SearchBuilder}
	 * resolving them.
	 */
	public static Test suite() {
		return suite(TestChunkedScriptJobBody.class, DatabaseTestSetup.DEFAULT_DB,
			ServiceTestSetup.createStarterFactoryForModules(DefaultTestFactory.INSTANCE,
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE));
	}

}
