/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;
import com.top_logic.layout.view.job.ScriptJobBody;
import com.top_logic.model.search.expr.config.SearchBuilder;

/**
 * Tests the TL-Script functions a job is reported and read through, evaluated as a script: the body
 * of a job reports with them, and a display asks the published state with them.
 */
public class TestJobFunctions extends AbstractJobTest {

	/** Tests that a script body enters the steps of its job by name. */
	public void testTheScriptEntersTheStepsOfTheJob() throws Exception {
		script("job -> x -> { $job.jobPhase('read'); $job.jobPhase('write'); 'done'; }",
			List.of(JobPhase.named("read"), JobPhase.named("check"), JobPhase.named("write")));

		JobState last = awaitFinished();

		assertEquals(JobStatus.COMPLETED, last.status());
		assertEquals("done", last.result());
		assertEquals("read", _states.get(1).phase().name());
		assertEquals("write", _states.get(2).phase().name());
	}

	/** Tests that naming a step the job did not announce ends the job. */
	public void testTheScriptCannotEnterAnUnannouncedStep() throws Exception {
		script("job -> x -> $job.jobPhase('unannounced')", List.of(JobPhase.named("read")));

		JobState last = awaitFinished();

		assertEquals(JobStatus.FAILED, last.status());
		assertTrue("The failure names the step: " + last.error(),
			last.error().toString().contains("unannounced"));
	}

	/** Tests that a script announces the steps of its job as a list of names. */
	public void testAListOfNamesAnnouncesTheStepsOfTheJob() throws Exception {
		script("job -> x -> { $job.jobPhases(['read', 'write']); $job.jobPhase('write'); }", List.of());

		awaitFinished();

		List<JobPhase> phases = _states.get(1).phases();
		assertEquals(List.of("read", "write"), List.of(phases.get(0).name(), phases.get(1).name()));
		assertEquals("A name announced alone is what the reader sees as well.",
			ResKey.text("read"), phases.get(0).label());
		assertEquals(1, _states.get(2).currentPhase());
	}

	/** Tests that a script announces steps with what the reader sees, given as a map. */
	public void testAMapAnnouncesWhatTheReaderSeesForEachStep() throws Exception {
		script("job -> x -> $job.jobPhases({'read': #('Reading'@en), 'write': 'Writing'})", List.of());

		awaitFinished();

		List<JobPhase> phases = _states.get(1).phases();
		assertEquals(2, phases.size());
		assertEquals("read", phases.get(0).name());
		assertEquals("An internationalized label is taken over as it was written.",
			internationalized("Reading").toString(), phases.get(0).label().toString());
		assertEquals("write", phases.get(1).name());
		assertEquals(ResKey.text("Writing"), phases.get(1).label());
	}

	/** Tests that a script reports its progress as the two counts it is the ratio of. */
	public void testTheScriptReportsItsProgress() throws Exception {
		script("job -> x -> { $job.jobProgress(1, 4); $job.jobIndeterminate(); }", List.of());

		awaitFinished();

		assertEquals(Double.valueOf(0.25), _states.get(1).fraction());
		assertTrue("A job may give up its progress again.", _states.get(2).isIndeterminate());
	}

	/** Tests that a script reports what it is doing, as a text and internationalized. */
	public void testTheScriptReportsWhatItIsDoing() throws Exception {
		script("job -> x -> { $job.jobMessage('reading'); $job.jobMessage(#('Writing'@en)); }", List.of());

		awaitFinished();

		assertEquals(ResKey.text("reading"), _states.get(1).message());
		assertEquals("An internationalized message is taken over as it was written.",
			internationalized("Writing").toString(), _states.get(2).message().toString());
	}

	/** Tests how a display reads a running job. */
	public void testADisplayReadsARunningJob() throws Exception {
		JobState state = new JobState(JobStatus.RUNNING, List.of(), -1, null, null, null, null, null, null, null);

		assertEquals(Boolean.TRUE, eval("s -> $s.jobIsRunning()", state));
		assertEquals(Boolean.FALSE, eval("s -> $s.jobIsFinished()", state));
		assertEquals("running", eval("s -> $s.jobStatus()", state));
	}

	/** Tests how a display reads a completed job, whose result it takes over. */
	public void testADisplayReadsACompletedJob() throws Exception {
		JobState state =
			new JobState(JobStatus.COMPLETED, List.of(), 0, null, null, null, null, "produced", null, null);

		assertEquals(Boolean.FALSE, eval("s -> $s.jobIsRunning()", state));
		assertEquals(Boolean.TRUE, eval("s -> $s.jobIsFinished()", state));
		assertEquals("completed", eval("s -> $s.jobStatus()", state));
		assertEquals("produced", eval("s -> $s.jobResult()", state));
		assertNull(eval("s -> $s.jobError()", state));
	}

	/** Tests how a display reads a failed job, whose failure it shows. */
	public void testADisplayReadsAFailedJob() throws Exception {
		ResKey error = ResKey.text("the import file is unreadable");
		JobState state = new JobState(JobStatus.FAILED, List.of(), 0, null, null, null, null, null, error, null);

		assertEquals("failed", eval("s -> $s.jobStatus()", state));
		assertSame(error, eval("s -> $s.jobError()", state));
		assertNull(eval("s -> $s.jobResult()", state));
	}

	/** Tests that a cancelled job is named by the text a display switches over. */
	public void testACancelledJobIsNamedByItsText() throws Exception {
		JobState state = new JobState(JobStatus.CANCELLED, List.of(), 0, null, null, null, null, null, null, null);

		assertEquals("cancelled", eval("s -> $s.jobStatus()", state));
	}

	/**
	 * Tests that no job at all is read as a job that neither runs nor has ended, so a display over a
	 * channel that never carried a job shows neither.
	 */
	public void testNoJobAtAllIsNeitherRunningNorFinished() throws Exception {
		assertEquals(Boolean.FALSE, eval("s -> $s.jobIsRunning()", (Object) null));
		assertEquals(Boolean.FALSE, eval("s -> $s.jobIsFinished()", (Object) null));
		assertNull(eval("s -> $s.jobStatus()", (Object) null));
		assertNull(eval("s -> $s.jobResult()", (Object) null));
		assertNull(eval("s -> $s.jobError()", (Object) null));
	}

	/** The key a script writes as an internationalized literal in English. */
	private static ResKey internationalized(String text) {
		return ResKey.builder().add(Locale.ENGLISH, text).build();
	}

	/** Runs the given script source as the body of a job announcing the given steps. */
	private void script(String source, List<JobPhase> phases) {
		start(new ScriptJobBody(compile(source)), false, phases);
	}

	/** The result of the given script source, called with the given value. */
	private static Object eval(String source, Object input) {
		return compile(source).execute(input);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} the functions are discovered through, the
	 * {@link SearchBuilder} resolving them, and the {@link SchedulerService} the job runs on.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobFunctions.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, SearchBuilder.Module.INSTANCE));
	}

}
