/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.InterruptibleViewAction;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.layout.view.job.PhaseStatus;
import com.top_logic.layout.view.job.ScriptJobBody;
import com.top_logic.layout.view.job.StartJobAction;
import com.top_logic.model.TLModel;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests what a {@code <start-job>} does to the command it stands in and to the channel it reports
 * on: the state of the job while it runs, the steps it passes through, how it ends, and what
 * cancelling it does.
 *
 * <p>
 * The job is driven through the public seam - the action in a command chain, the channel it
 * publishes to - with bodies written in Java, so that the sequence of published states is what a
 * configured job would produce.
 * </p>
 */
public class TestStartJobAction extends AbstractJobTest {

	/** What the actions beside the job logged. */
	private final List<String> _log = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Tests that the channel holds the running job before the command suspends, so what displays it
	 * is there from the first moment.
	 */
	public void testTheRunningJobIsPublishedBeforeTheCommandSuspends() throws Exception {
		CountDownLatch release = new CountDownLatch(1);
		start(body((job, arguments) -> {
			release.await();
			return "done";
		}), false, List.of(JobPhase.named("read"), JobPhase.named("write")));

		JobState initial = current();
		assertNotNull("The channel holds the job while the command waits for it.", initial);
		assertEquals(JobStatus.RUNNING, initial.status());
		assertTrue(initial.isRunning());
		assertEquals("The job has not entered a step yet.", -1, initial.currentPhase());
		assertNull(initial.phase());
		assertTrue("Nothing is known about the progress yet.", initial.isIndeterminate());
		assertEquals(2, initial.phases().size());
		assertNotNull("The state carries the handle the display cancels through.", initial.control());
		assertTrue("The command waits for the job.", _completions.isEmpty());

		release.countDown();
		assertEquals(JobStatus.COMPLETED, awaitFinished().status());
	}

	/**
	 * Tests that entering a step marks the steps passed over as done, the entered one as active and
	 * the ones behind it as pending - and that a completed job has passed all of them.
	 */
	public void testEnteringAStepMarksTheOnesPassedOverAsDone() throws Exception {
		start(body((job, arguments) -> {
			job.beginPhase("read");
			job.beginPhase("write");
			return null;
		}), false, List.of(JobPhase.named("read"), JobPhase.named("check"), JobPhase.named("write")));

		JobState last = awaitFinished();

		assertEquals("The step the job is in is published on every transition.",
			List.of(Integer.valueOf(-1), Integer.valueOf(0), Integer.valueOf(2), Integer.valueOf(3)),
			currentPhases());

		JobState writing = _states.get(2);
		assertEquals("write", writing.phase().name());
		assertEquals(PhaseStatus.DONE, writing.phaseStatus(0));
		assertEquals("A step the job passed over counts as done.", PhaseStatus.DONE, writing.phaseStatus(1));
		assertEquals(PhaseStatus.ACTIVE, writing.phaseStatus(2));

		JobState reading = _states.get(1);
		assertEquals(PhaseStatus.ACTIVE, reading.phaseStatus(0));
		assertEquals(PhaseStatus.PENDING, reading.phaseStatus(1));

		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals("A completed job has passed all of its steps.", PhaseStatus.DONE, last.phaseStatus(2));
		assertNull(last.phase());
	}

	/**
	 * Tests that the job publishes how much of its work is done, and that it may state that it does
	 * not know.
	 */
	public void testTheProgressIsPublishedAndMayBeUnknown() throws Exception {
		start(body((job, arguments) -> {
			job.progress(1, 4);
			job.fraction(2);
			job.indeterminate();
			return null;
		}), false, List.of());

		awaitFinished();

		assertEquals(Double.valueOf(0.25), _states.get(1).fraction());
		assertEquals("A fraction outside the range is cut down to it.",
			Double.valueOf(1.0), _states.get(2).fraction());
		assertTrue(_states.get(3).isIndeterminate());
	}

	/**
	 * Tests that what the job says about what it is doing reaches the channel.
	 */
	public void testTheMessageOfTheJobIsPublished() throws Exception {
		ResKey message = ResKey.text("reading the file");
		start(body((job, arguments) -> {
			job.message(message);
			return null;
		}), false, List.of());

		awaitFinished();

		assertSame(message, _states.get(1).message());
	}

	/**
	 * Tests that a completed job continues the command with its result, and that the last state
	 * carries the result as well.
	 */
	public void testACompletedJobContinuesTheCommandWithItsResult() throws Exception {
		run(List.of(job(body((job, arguments) -> "produced"), false, List.of()), record("after")));

		JobState last = awaitFinished();

		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals("produced", last.result());
		assertNotNull("A finished job says when it finished.", last.finishedAt());
		assertEquals(List.of("after"), _log);
		assertEquals("The result of the job is the value the command continues with.",
			List.of("produced"), _completions);
	}

	/**
	 * Tests that the values the job is started with are the input channels followed by the value of
	 * the command.
	 */
	public void testTheJobIsStartedWithTheInputsAndTheValueOfTheCommand() throws Exception {
		_context.resolveChannel(new ChannelRef(CONTEXT)).set("the context");
		AtomicReference<List<Object>> seen = new AtomicReference<>();

		StartJobAction action = new StartJobAction(new ChannelRef(JOB), List.of(new ChannelRef(CONTEXT)),
			List.of(), false, 0, body((job, arguments) -> {
				seen.set(arguments);
				return null;
			}));
		run(List.of(action));

		awaitFinished();

		assertEquals(List.of("the context", INPUT), seen.get());
	}

	/**
	 * Tests that a job written as a TL-Script function is called with the monitor of the job first,
	 * then the inputs, then the value of the command.
	 */
	public void testAScriptJobIsCalledWithTheMonitorFirst() throws Exception {
		_context.resolveChannel(new ChannelRef(CONTEXT)).set("the context");
		AtomicReference<List<Object>> seen = new AtomicReference<>();

		StartJobAction action = new StartJobAction(new ChannelRef(JOB), List.of(new ChannelRef(CONTEXT)),
			List.of(), false, 0, new ScriptJobBody(script(arguments -> {
				seen.set(arguments);
				return "produced";
			})));
		run(List.of(action));

		JobState last = awaitFinished();

		List<Object> arguments = seen.get();
		assertEquals(3, arguments.size());
		assertSame("The monitor of the job leads the arguments of the function.",
			last.control(), arguments.get(0));
		assertEquals("the context", arguments.get(1));
		assertEquals(INPUT, arguments.get(2));
		assertEquals("produced", last.result());
	}

	/**
	 * Tests that a failing job ends the command: the failure stays visible in the last state, and
	 * the compensations of the actions before the job run.
	 */
	public void testAFailingJobEndsTheCommand() throws Exception {
		ResKey error = ResKey.text("the import file is unreadable");
		run(List.of(compensate("read"), job(body((job, arguments) -> {
			throw new TopLogicException(error);
		}), false, List.of()), record("after")));

		JobState last = awaitFinished();

		assertEquals(JobStatus.FAILED, last.status());
		assertSame("The message of the failure is what the reader sees.", error, last.error());
		assertNull(last.result());
		assertEquals("The command ends where the job failed.", List.of("read compensated"), _log);
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a failure of the command taken up after the job neither changes how the job ended
	 * nor disappears: the job stays completed and the failure reaches the window it belongs to.
	 */
	public void testAFailureOfTheCommandAfterTheJobIsShownInTheWindow() throws Exception {
		ResKey error = ResKey.text("the result could not be stored");
		run(List.<ViewAction> of(job(body((job, arguments) -> "produced"), false, List.of()),
			(context, input) -> {
				throw new TopLogicException(error);
			}));

		assertTrue("The job must finish within " + TIMEOUT + " ms.",
			_finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertTrue("The failure of the command must be shown within " + TIMEOUT + " ms.",
			_errorShown.await(TIMEOUT, TimeUnit.MILLISECONDS));

		JobState last = current();
		assertEquals("What failed is the command after the job, not the job.",
			JobStatus.COMPLETED, last.status());
		assertEquals("produced", last.result());
		assertNull(last.error());
		assertTrue("The command did not settle, it failed.", _completions.isEmpty());
		assertEquals(1, _shownErrors.size());
	}

	/**
	 * Tests that a failure that carries no message of its own is reported generically rather than
	 * leaving the job running.
	 */
	public void testAnUnexpectedFailureIsReportedGenerically() throws Exception {
		start(body((job, arguments) -> {
			throw new IllegalStateException("broken");
		}), false, List.of());

		JobState last = awaitFinished();

		assertEquals(JobStatus.FAILED, last.status());
		assertNotNull(last.error());
	}

	/**
	 * Tests that naming a step the job did not announce ends the job with a failure naming it.
	 */
	public void testNamingAnUnannouncedStepFailsTheJob() throws Exception {
		start(body((job, arguments) -> {
			job.beginPhase("unannounced");
			return null;
		}), false, List.of(JobPhase.named("read")));

		JobState last = awaitFinished();

		assertEquals(JobStatus.FAILED, last.status());
		assertTrue("The failure names the step: " + last.error(),
			last.error().toString().contains("unannounced"));
	}

	/**
	 * Tests that a job may announce its steps only while it runs, and keeps the step it is in.
	 */
	public void testAJobMayAnnounceItsStepsWhileItRuns() throws Exception {
		start(body((job, arguments) -> {
			job.setPhases(List.of(JobPhase.named("read"), JobPhase.named("write")));
			job.beginPhase("write");
			job.setPhases(List.of(JobPhase.named("read"), JobPhase.named("check"), JobPhase.named("write")));
			return null;
		}), false, List.of());

		awaitFinished();

		JobState announced = _states.get(2);
		assertEquals(1, announced.currentPhase());

		JobState extended = _states.get(3);
		assertEquals("The job stays in the step it is in, wherever the new list puts it.",
			"write", extended.phase().name());
		assertEquals(2, extended.currentPhase());
	}

	/**
	 * Tests that cancelling a job interrupts what it waits for, ends it as cancelled and ends the
	 * command.
	 */
	public void testCancellingInterruptsWhatTheJobWaitsFor() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch interrupted = new CountDownLatch(1);
		run(List.of(compensate("read"), job(body((job, arguments) -> {
			started.countDown();
			try {
				Thread.sleep(TIMEOUT);
			} catch (InterruptedException ex) {
				interrupted.countDown();
				throw ex;
			}
			return "done";
		}), true, List.of()), record("after")));

		assertTrue("The job must start.", started.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertTrue("A cancelable job offers itself to be cancelled.", current().isCancelable());
		current().control().cancel();

		JobState last = awaitFinished();

		assertTrue("The job is woken from what it waits for.", interrupted.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull(last.error());
		assertFalse("A finished job is no longer offered to be cancelled.", last.isCancelable());
		assertEquals(List.of("read compensated"), _log);
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that cancelling a job whose work is a script ends it as cancelled: the abort reaches the
	 * outside as the failure of the expression it was raised in, and the request to stop decides how
	 * the job ended all the same.
	 */
	public void testCancellingAScriptJobEndsItAsCancelled() throws Exception {
		run(List.of(compensate("read"),
			job(new ScriptJobBody(compile("job -> x -> { sleep(10000); $job.jobProgress(1, 2); 'done' }")),
				true, List.of()),
			record("after")));

		assertEquals("The channel holds the running script job.", JobStatus.RUNNING, current().status());
		current().control().cancel();

		JobState last = awaitFinished();

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull("A cancelled job ends without a failure to report.", last.error());
		assertEquals("The work that waited for the job is abandoned.", List.of("read compensated"), _log);
		assertEquals(Collections.singletonList(null), _completions);
	}

	/**
	 * Tests that a body wrapping the abort into a failure of its own ends the job as cancelled, so
	 * that what a body does on its way out does not turn a stopped job into a failed one.
	 */
	public void testABodyWrappingTheAbortEndsAsCancelled() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		start(body((job, arguments) -> {
			started.countDown();
			awaitQuietly(cancelled);
			try {
				job.checkCancelled();
				return "done";
			} catch (AbortExecutionException ex) {
				throw new IllegalStateException("The import was rolled back.", ex);
			}
		}), true, List.of());

		assertTrue(started.await(TIMEOUT, TimeUnit.MILLISECONDS));
		current().control().cancel();
		cancelled.countDown();

		JobState last = awaitFinished();

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull("A cancelled job ends without a failure to report.", last.error());
	}

	/**
	 * Tests that a body abandoning its work on its own ends the job as cancelled, wherever in the
	 * chain of causes it says so.
	 */
	public void testABodyThatAbandonsItsWorkEndsAsCancelled() throws Exception {
		start(body((job, arguments) -> {
			throw new IllegalStateException("The import was rolled back.",
				new AbortExecutionException("There is nothing to import.", null));
		}), false, List.of());

		JobState last = awaitFinished();

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull("Abandoned work is no failure to report.", last.error());
	}

	/**
	 * Tests that the next report of a cancelled job ends it, so a body that reports regularly needs
	 * no check of its own.
	 */
	public void testTheNextReportOfACancelledJobEndsIt() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		AtomicReference<Throwable> seen = new AtomicReference<>();
		start(body((job, arguments) -> {
			started.countDown();
			awaitQuietly(cancelled);
			try {
				job.message(ResKey.text("still working"));
			} catch (RuntimeException ex) {
				seen.set(ex);
				throw ex;
			}
			return "done";
		}), true, List.of());

		assertTrue(started.await(TIMEOUT, TimeUnit.MILLISECONDS));
		current().control().cancel();
		cancelled.countDown();

		JobState last = awaitFinished();

		assertTrue("The report of a cancelled job ends it: " + seen.get(),
			seen.get() instanceof AbortExecutionException);
		assertEquals(JobStatus.CANCELLED, last.status());
	}

	/**
	 * Tests that a result the job still produces after it was cancelled is discarded.
	 */
	public void testAResultProducedAfterTheCancellationIsDiscarded() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		start(body((job, arguments) -> {
			started.countDown();
			awaitQuietly(cancelled);
			return "too late";
		}), true, List.of());

		assertTrue(started.await(TIMEOUT, TimeUnit.MILLISECONDS));
		current().control().cancel();
		cancelled.countDown();

		JobState last = awaitFinished();

		assertEquals(JobStatus.CANCELLED, last.status());
		assertNull("The result of a cancelled job is discarded.", last.result());
	}

	/**
	 * Tests that a job that is not cancelable is not stopped by asking it to stop.
	 */
	public void testAJobThatIsNotCancelableKeepsRunning() throws Exception {
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		start(body((job, arguments) -> {
			started.countDown();
			release.await();
			return "done";
		}), false, List.of());

		assertTrue(started.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertFalse(current().isCancelable());
		current().control().cancel();
		release.countDown();

		JobState last = awaitFinished();

		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals("done", last.result());
	}

	/**
	 * Tests that stating the work of a job twice, and stating it not at all, are both rejected.
	 */
	public void testStatingTheWorkOfTheJobTwiceOrNotAtAllIsRejected() throws Exception {
		assertRejected(configWithWork(true, true));
		assertRejected(configWithWork(false, false));
	}

	private StartJobAction.Config configWithWork(boolean function, boolean body) throws ConfigurationException {
		StartJobAction.Config config = TypedConfiguration.newConfigItem(StartJobAction.Config.class);
		update(config, StartJobAction.Config.JOB, new ChannelRef(JOB));
		if (function) {
			update(config, StartJobAction.Config.FUNCTION, expr("job -> x -> null"));
		}
		if (body) {
			PolymorphicConfiguration<JobBody> bodyConfig =
				TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
			update(config, StartJobAction.Config.BODY, bodyConfig);
		}
		return config;
	}

	private static void assertRejected(StartJobAction.Config config) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestStartJobAction.class);
		context.getInstance(config);
		try {
			context.checkErrors();
		} catch (AbortExecutionException | ConfigurationException expected) {
			return;
		}
		fail("The configuration must be rejected.");
	}

	private static void update(StartJobAction.Config config, String name, Object value) {
		PropertyDescriptor property = config.descriptor().getProperty(name);
		config.update(property, value);
	}

	/** The step the job was in, in the order the states were published. */
	private List<Object> currentPhases() {
		List<Object> result = new ArrayList<>();
		for (JobState state : _states) {
			result.add(Integer.valueOf(state.currentPhase()));
		}
		return result;
	}

	/** An action that logs its name and hands the value of the command on. */
	private ViewAction record(String name) {
		return (context, input) -> {
			_log.add(name);
			return input;
		};
	}

	/** An action that registers a compensation logging its name. */
	private ViewAction compensate(String name) {
		return new InterruptibleViewAction() {
			@Override
			public void execute(ReactContext context, Object input, Continuation continuation) {
				continuation.onAbort(() -> _log.add(name + " compensated"));
				continuation.resume(input);
			}
		};
	}

	/** A compiled function computing its result from the arguments it is called with. */
	private static QueryExecutor script(java.util.function.Function<List<Object>, Object> function) {
		return new QueryExecutor() {
			@Override
			protected Object internalExecuteWith(EvalContext definitions, Args args) {
				List<Object> values = new ArrayList<>();
				for (Args current = args; current.hasValue(); current = current.next()) {
					values.add(current.value());
				}
				return function.apply(values);
			}

			@Override
			public SearchExpression getSearch() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected KnowledgeBase getKnowledgeBase() {
				return null;
			}

			@Override
			protected TLModel getTLModel() {
				return null;
			}

			@Override
			protected void internalDisableSecurity() {
				// Nothing to switch off, the function accesses no data.
			}
		};
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, the {@link SchedulerService} the job runs on and
	 * the {@link SearchBuilder} a script body is compiled with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestStartJobAction.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, SearchBuilder.Module.INSTANCE));
	}
}
