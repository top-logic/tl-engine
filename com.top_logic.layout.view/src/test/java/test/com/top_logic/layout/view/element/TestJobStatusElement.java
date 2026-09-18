/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.common.JobDisplay;
import com.top_logic.layout.react.control.common.ReactJobStatusControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.JobStatusElement;
import com.top_logic.layout.view.job.JobControl;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.JobStatus;

/**
 * Tests that a {@link JobStatusElement} displays the job its channel reports about: the control
 * follows every snapshot published there, resolves its texts for the reader, and stops following
 * once the control is disposed.
 *
 * @see JobStatusElement#display(Object)
 */
public class TestJobStatusElement extends TestCase {

	/** Name of the channel the job reports on. */
	private static final String JOB = "job";

	/** When the job of these tests was started. */
	private static final Date STARTED = new Date(1_700_000_000_000L);

	/** When a finished job of these tests ended. */
	private static final Date FINISHED = new Date(1_700_000_030_000L);

	/** A {@link JobControl} remembering whether it was asked to stop. */
	private static final class Handle implements JobControl {

		boolean _cancelable = true;

		boolean _cancelled;

		@Override
		public boolean isCancelable() {
			return _cancelable;
		}

		@Override
		public void cancel() {
			_cancelled = true;
		}
	}

	private ViewContext _context;

	private ViewChannel _channel;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_channel = new DefaultViewChannel(JOB);
		_context.registerChannel(JOB, _channel);
	}

	/** A channel that holds no job displays none: a view needs no case for the time before a start. */
	public void testAChannelWithoutAJobDisplaysNothing() {
		ReactJobStatusControl control = control();

		assertNull(control.getJob());
		assertNull(state(control).get(ReactJobStatusControl.STATUS));
	}

	/** ...and neither does a channel holding something that is no job state at all. */
	public void testAValueThatIsNoJobDisplaysNothing() {
		_channel.set("not a job");

		assertNull(control().getJob());
	}

	/** A snapshot published on the channel is what the control displays. */
	public void testTheControlFollowsTheChannel() {
		ReactJobStatusControl control = control();

		_channel.set(running(1, Double.valueOf(0.5), ResKey.text("Transforming"), null));

		Map<String, Object> state = state(control);
		assertEquals("running", state.get(ReactJobStatusControl.STATUS));
		assertEquals(0.5, ((Number) state.get(ReactJobStatusControl.FRACTION)).doubleValue(), 0.0);
		assertEquals("Transforming", state.get(ReactJobStatusControl.MESSAGE));
		assertEquals(Double.valueOf(STARTED.getTime()),
			Double.valueOf(((Number) state.get(ReactJobStatusControl.STARTED_AT)).doubleValue()));
	}

	/** The steps reach the reader as their texts, each saying where the job stands. */
	public void testThePhasesAreResolvedForTheReader() {
		_channel.set(running(1, null, null, null));

		List<?> phases = (List<?>) state(control()).get(ReactJobStatusControl.PHASES);

		assertEquals(3, phases.size());
		assertEquals("Prepare", ((Map<?, ?>) phases.get(0)).get(ReactJobStatusControl.PHASE_LABEL));
		assertEquals("done", ((Map<?, ?>) phases.get(0)).get(ReactJobStatusControl.PHASE_STATUS));
		assertEquals("active", ((Map<?, ?>) phases.get(1)).get(ReactJobStatusControl.PHASE_STATUS));
		assertEquals("pending", ((Map<?, ?>) phases.get(2)).get(ReactJobStatusControl.PHASE_STATUS));
	}

	/** A job that ended states when, so the elapsed time stops there. */
	public void testAFinishedJobStatesWhenItEnded() {
		_channel.set(new JobState(JobStatus.COMPLETED, phases(), 3, Double.valueOf(1.0), null, STARTED,
			FINISHED, null, null, null));

		Map<String, Object> state = state(control());
		assertEquals("completed", state.get(ReactJobStatusControl.STATUS));
		assertEquals(Double.valueOf(FINISHED.getTime()),
			Double.valueOf(((Number) state.get(ReactJobStatusControl.FINISHED_AT)).doubleValue()));
	}

	/** A failed job says why, in the text of its key. */
	public void testAFailedJobStatesItsError() {
		_channel.set(new JobState(JobStatus.FAILED, phases(), 1, null, null, STARTED, FINISHED, null,
			ResKey.text("Connection lost"), null));

		assertEquals("Connection lost", state(control()).get(ReactJobStatusControl.ERROR));
	}

	/** What a job produced is displayed the way any other value of a channel is: as its label. */
	public void testTheResultIsDisplayedAsItsLabel() {
		_channel.set(new JobState(JobStatus.COMPLETED, phases(), 3, Double.valueOf(1.0), null, STARTED,
			FINISHED, "42 objects", null, null));

		assertEquals("42 objects", state(control()).get(ReactJobStatusControl.RESULT));
	}

	/** A running job that offers a handle may be stopped, and the request reaches that handle. */
	public void testTheCancelRequestReachesTheJob() {
		Handle handle = new Handle();
		_channel.set(running(1, null, null, handle));

		ReactJobStatusControl control = control();
		assertEquals(Boolean.TRUE, state(control).get(ReactJobStatusControl.CANCELABLE));

		control.executeClientCommand(ReactJobStatusControl.CANCEL_COMMAND, Map.of());

		assertTrue("The job must be asked to stop.", handle._cancelled);
	}

	/** A job whose handle refuses to be stopped offers nothing to stop it with. */
	public void testAJobThatRefusesToStopIsNotCancelable() {
		Handle handle = new Handle();
		handle._cancelable = false;
		_channel.set(running(1, null, null, handle));

		ReactJobStatusControl control = control();

		assertEquals(Boolean.FALSE, state(control).get(ReactJobStatusControl.CANCELABLE));
		control.executeClientCommand(ReactJobStatusControl.CANCEL_COMMAND, Map.of());
		assertFalse(handle._cancelled);
	}

	/** A disposed control stops following the channel, so nothing holds on to it. */
	public void testTheListenerIsRemovedOnCleanup() {
		_channel.set(running(0, null, null, null));
		ReactJobStatusControl control = control();
		JobDisplay displayed = control.getJob();

		control.cleanupTree();
		_channel.set(running(2, Double.valueOf(0.9), null, null));

		assertSame("A disposed control must no longer be told about the job.", displayed, control.getJob());
	}

	/** The control of a {@link JobStatusElement} over the job channel. */
	private ReactJobStatusControl control() {
		return (ReactJobStatusControl) new JobStatusElement(new ChannelRef(JOB)).createControl(_context);
	}

	/** A running job at the step with the given index. */
	private static JobState running(int currentPhase, Double fraction, ResKey message, JobControl control) {
		return new JobState(JobStatus.RUNNING, phases(), currentPhase, fraction, message, STARTED, null, null,
			null, control);
	}

	/** The three steps the job of these tests goes through. */
	private static List<JobPhase> phases() {
		return List.of(JobPhase.named("Prepare"), JobPhase.named("Transform"), JobPhase.named("Store"));
	}

	/** The state the client is told, as it is written to it. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactJobStatusControl control) {
		try {
			return (Map<String, Object>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + control.stateAsJSON(), ex);
		}
	}

	/** Suite requiring the resource bundles the texts of a job are resolved against. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestJobStatusElement.class, ThreadContextManager.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}

}
