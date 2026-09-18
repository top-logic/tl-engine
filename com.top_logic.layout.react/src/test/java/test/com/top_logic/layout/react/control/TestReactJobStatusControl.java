/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.JobDisplay;
import com.top_logic.layout.react.control.common.JobDisplay.Phase;
import com.top_logic.layout.react.control.common.JobDisplay.PhaseState;
import com.top_logic.layout.react.control.common.JobDisplay.Status;
import com.top_logic.layout.react.control.common.ReactJobStatusControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests what a {@link ReactJobStatusControl} tells the client about a job: how it stands, the steps
 * it goes through, how far it has come and how long it has been at work - and that a request to
 * stop reaches only a job that may be stopped.
 *
 * <p>
 * The elapsed time is counted by the client, so what is tested here is what it counts from: the two
 * instants of the job and the server's own clock reading beside them.
 * </p>
 */
public class TestReactJobStatusControl extends TestCase {

	/** When the job of these tests was started. */
	private static final Date STARTED = new Date(1_700_000_000_000L);

	/** When a finished job of these tests ended, half a minute later. */
	private static final Date FINISHED = new Date(1_700_000_030_000L);

	/** A running job states that it runs, the steps it goes through and how far it has come. */
	public void testARunningJobStatesItsPhasesAndShare() {
		ReactJobStatusControl control = control(running(Double.valueOf(0.5), "Reading", null));

		assertEquals("running", state(control).get(ReactJobStatusControl.STATUS));
		assertEquals(Double.valueOf(0.5), number(state(control).get(ReactJobStatusControl.FRACTION)));
		assertEquals("Reading", state(control).get(ReactJobStatusControl.MESSAGE));
	}

	/** Every step says where it stands, so the display marks the way the job has come. */
	public void testEveryPhaseStatesWhereItStands() {
		ReactJobStatusControl control = control(running(Double.valueOf(0.5), null, null));

		List<?> phases = (List<?>) state(control).get(ReactJobStatusControl.PHASES);
		assertEquals(3, phases.size());
		assertEquals("Prepare", phase(phases, 0).get(ReactJobStatusControl.PHASE_LABEL));
		assertEquals("done", phase(phases, 0).get(ReactJobStatusControl.PHASE_STATUS));
		assertEquals("active", phase(phases, 1).get(ReactJobStatusControl.PHASE_STATUS));
		assertEquals("pending", phase(phases, 2).get(ReactJobStatusControl.PHASE_STATUS));
	}

	/** A job that does not know its share states none, and the client sweeps its bar. */
	public void testAJobWithoutAShareStatesNoFraction() {
		ReactJobStatusControl control = control(running(null, "Working", null));

		assertNull("An unknown share must reach the client as none.",
			state(control).get(ReactJobStatusControl.FRACTION));
	}

	/** A running job has not ended, so the elapsed time it is counted from keeps running. */
	public void testARunningJobStatesNoEnd() {
		ReactJobStatusControl control = control(running(null, null, null));

		assertEquals(Double.valueOf(STARTED.getTime()),
			number(state(control).get(ReactJobStatusControl.STARTED_AT)));
		assertNull(state(control).get(ReactJobStatusControl.FINISHED_AT));
	}

	/** Every report carries the server's clock, the reference the client corrects its own against. */
	public void testEveryReportCarriesTheServerClock() {
		long before = System.currentTimeMillis();
		ReactJobStatusControl control = control(running(null, null, null));
		long after = System.currentTimeMillis();

		double serverNow = number(state(control).get(ReactJobStatusControl.SERVER_NOW)).doubleValue();
		assertTrue("The server's clock must be read when the report is sent: " + serverNow,
			serverNow >= before && serverNow <= after);
	}

	/** A job that ran to its end states when it ended and what it produced. */
	public void testAFinishedJobStatesItsResult() {
		ReactJobStatusControl control = control(new JobDisplay(Status.COMPLETED, phases(2), Double.valueOf(1.0),
			null, STARTED, FINISHED, "42 objects", null, null));

		Map<String, Object> state = state(control);
		assertEquals("completed", state.get(ReactJobStatusControl.STATUS));
		assertEquals(Double.valueOf(FINISHED.getTime()), number(state.get(ReactJobStatusControl.FINISHED_AT)));
		assertEquals("42 objects", state.get(ReactJobStatusControl.RESULT));
		assertNull(state.get(ReactJobStatusControl.ERROR));
	}

	/** A job that stopped with an error says why, and has produced nothing. */
	public void testAFailedJobStatesItsError() {
		ReactJobStatusControl control = control(new JobDisplay(Status.FAILED, phases(1), null, null, STARTED,
			FINISHED, null, "Connection lost", null));

		Map<String, Object> state = state(control);
		assertEquals("failed", state.get(ReactJobStatusControl.STATUS));
		assertEquals("Connection lost", state.get(ReactJobStatusControl.ERROR));
		assertNull(state.get(ReactJobStatusControl.RESULT));
	}

	/** Without a job there is nothing to display: no status, no steps, nothing to stop. */
	public void testNoJobDisplaysNothing() {
		ReactJobStatusControl control = control(null);

		Map<String, Object> state = state(control);
		assertNull(state.get(ReactJobStatusControl.STATUS));
		assertEquals(List.of(), state.get(ReactJobStatusControl.PHASES));
		assertEquals(Boolean.FALSE, state.get(ReactJobStatusControl.CANCELABLE));
		assertNull(state.get(ReactJobStatusControl.SERVER_NOW));
	}

	/** A report replaces the one before it, so nothing of the previous one is left standing. */
	public void testAReportReplacesTheOneBefore() {
		ReactJobStatusControl control = control(running(Double.valueOf(0.5), "Working", () -> fail()));

		control.setJob(new JobDisplay(Status.CANCELLED, phases(1), null, null, STARTED, FINISHED, null, null,
			null));

		Map<String, Object> state = state(control);
		assertEquals("cancelled", state.get(ReactJobStatusControl.STATUS));
		assertNull("The share of the report before must be gone.", state.get(ReactJobStatusControl.FRACTION));
		assertNull("The message of the report before must be gone.", state.get(ReactJobStatusControl.MESSAGE));
		assertEquals(Boolean.FALSE, state.get(ReactJobStatusControl.CANCELABLE));
	}

	/** A running job offering a handler may be stopped, and the request reaches the job. */
	public void testACancelReachesTheJob() {
		boolean[] cancelled = { false };
		ReactJobStatusControl control = control(running(Double.valueOf(0.5), null, () -> cancelled[0] = true));

		assertEquals(Boolean.TRUE, state(control).get(ReactJobStatusControl.CANCELABLE));

		control.executeClientCommand(ReactJobStatusControl.CANCEL_COMMAND, Map.of());

		assertTrue("The job must be asked to stop.", cancelled[0]);
	}

	/** A job that offers no handler may not be stopped, whatever the client asks. */
	public void testAJobWithoutAHandlerIsNotCancelable() {
		ReactJobStatusControl control = control(running(null, null, null));

		assertEquals(Boolean.FALSE, state(control).get(ReactJobStatusControl.CANCELABLE));

		control.executeClientCommand(ReactJobStatusControl.CANCEL_COMMAND, Map.of());
	}

	/**
	 * A request crossing the report that ended the job is dropped: the button is gone, but the
	 * click was already on its way.
	 */
	public void testACancelOfAFinishedJobIsIgnored() {
		boolean[] cancelled = { false };
		ReactJobStatusControl control = control(new JobDisplay(Status.COMPLETED, phases(2), Double.valueOf(1.0),
			null, STARTED, FINISHED, "done", null, () -> cancelled[0] = true));

		control.executeClientCommand(ReactJobStatusControl.CANCEL_COMMAND, Map.of());

		assertFalse("A job that has ended must not be asked to stop.", cancelled[0]);
	}

	/** Asking to stop is a gesture of the reader about the job, so it is recorded like any other. */
	public void testTheCancelCommandIsRecordable() {
		assertTrue(control(running(null, null, () -> {
			// Nothing to do, the command is not run here.
		})).isRecordable(ReactJobStatusControl.CANCEL_COMMAND));
	}

	/** A job at its second of three steps. */
	private static JobDisplay running(Double fraction, String message, Runnable cancel) {
		return new JobDisplay(Status.RUNNING, phases(1), fraction, message, STARTED, null, null, null, cancel);
	}

	/** Three steps, the one at the given index being the active one. */
	private static List<Phase> phases(int active) {
		return List.of(
			new Phase("Prepare", state(0, active)),
			new Phase("Transform", state(1, active)),
			new Phase("Store", state(2, active)));
	}

	private static PhaseState state(int index, int active) {
		if (index < active) {
			return PhaseState.DONE;
		}
		return index == active ? PhaseState.ACTIVE : PhaseState.PENDING;
	}

	/** The entry of the phase list at the given index. */
	private static Map<?, ?> phase(List<?> phases, int index) {
		return (Map<?, ?>) phases.get(index);
	}

	/** The given state value as the number the client reads. */
	private static Double number(Object value) {
		assertTrue("Not a number: " + value, value instanceof Number);
		return Double.valueOf(((Number) value).doubleValue());
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

	private static ReactJobStatusControl control(JobDisplay job) {
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		return new ReactJobStatusControl(context, job);
	}

}
