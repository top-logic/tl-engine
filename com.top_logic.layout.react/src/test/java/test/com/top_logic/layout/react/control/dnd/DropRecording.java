/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.dnd;

import static junit.framework.Assert.*;

import java.util.List;
import java.util.Map;

import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.dnd.DropArguments;
import com.top_logic.layout.react.control.dnd.DropSupport;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.util.error.TopLogicException;

/**
 * Records a drop the way the browser dispatches it and replays the recorded step, for the tests of
 * the drop targets.
 */
public final class DropRecording {

	private DropRecording() {
		// Static utility.
	}

	/**
	 * Records the given drop on the given control through the {@link ScriptRecorder}, without
	 * applying it.
	 *
	 * @param root
	 *        The root control of the window the drop is made in.
	 * @param target
	 *        The control the drop is made on.
	 * @param arguments
	 *        The arguments as the client sends them.
	 * @return The recorded step.
	 */
	public static DropArguments record(ReactControl root, ReactControl target, Map<String, Object> arguments) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		recorder.capture(root, target, DropSupport.CMD_DROP, arguments);
		List<ReactCommand> steps = recorder.steps();
		assertEquals("The drop must be recorded: " + steps, 1, steps.size());
		ReactCommand step = steps.get(0);
		assertEquals(DropSupport.CMD_DROP, step.getName());
		assertTrue("A drop is recorded as the drop command: " + step, step instanceof DropArguments);
		return (DropArguments) step;
	}

	/**
	 * Replays the given recorded step in the window with the given root control, within an
	 * interaction, in which the recorded keys are resolved.
	 *
	 * @return The failure of the replay, {@code null} if it succeeded.
	 */
	public static TopLogicException replay(ReactControl root, ReactCommand step) {
		return CommandFailures.outcome(() -> new InteractionWithoutSession().runWithContext(
			() -> ScriptingSession.forRoot(root).replay(step)));
	}

}
