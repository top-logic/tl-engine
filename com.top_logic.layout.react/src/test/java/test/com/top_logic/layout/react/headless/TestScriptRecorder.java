/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.headless;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.headless.RecordedStep;
import com.top_logic.layout.react.headless.ScriptRecorder;

/**
 * Tests {@link ScriptRecorder} capture, in particular the {@link ScriptRecorder#record(RecordedStep,
 * boolean) coalescing} of consecutive same-target steps.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestScriptRecorder extends TestCase {

	private static RecordedStep step(String address, String command, Object value) {
		return new RecordedStep(address, command, Map.of("value", value));
	}

	/**
	 * A run of coalescing steps on the same address+command collapses to a single step holding the
	 * latest arguments.
	 */
	public void testCoalesceConsecutiveSameTarget() {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		recorder.record(step("/form/field[name]", "valueChanged", "h"), true);
		recorder.record(step("/form/field[name]", "valueChanged", "he"), true);
		recorder.record(step("/form/field[name]", "valueChanged", "hello"), true);

		List<RecordedStep> steps = recorder.steps();
		assertEquals("Consecutive edits of one field collapse to one step.", 1, steps.size());
		assertEquals("hello", steps.get(0).arguments().get("value"));
	}

	/**
	 * Coalescing only merges into the immediately preceding step: a different target in between keeps
	 * the two edits of the same field as separate steps.
	 */
	public void testNoCoalesceAcrossDifferentTarget() {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		recorder.record(step("/form/field[a]", "valueChanged", "1"), true);
		recorder.record(step("/form/field[a]", "valueChanged", "12"), true);
		recorder.record(step("/form/field[b]", "valueChanged", "x"), true);
		recorder.record(step("/form/field[a]", "valueChanged", "123"), true);

		List<RecordedStep> steps = recorder.steps();
		assertEquals(3, steps.size());
		assertEquals("12", steps.get(0).arguments().get("value"));
		assertEquals("x", steps.get(1).arguments().get("value"));
		assertEquals("123", steps.get(2).arguments().get("value"));
	}

	/**
	 * A different command on the same address is a distinct step (only same command coalesces).
	 */
	public void testNoCoalesceAcrossDifferentCommand() {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		recorder.record(step("/form/field[a]", "valueChanged", "1"), true);
		recorder.record(step("/form/field[a]", "blur", "1"), true);

		assertEquals(2, recorder.steps().size());
	}

	/**
	 * A non-coalescing capture always appends, even after a coalescing step on the same target.
	 */
	public void testNonCoalescingAppends() {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		recorder.record(step("/counter", "increment", "1"), false);
		recorder.record(step("/counter", "increment", "1"), false);

		assertEquals(2, recorder.steps().size());
	}

	/**
	 * An unaddressable ({@code null} address) step never coalesces — such steps stay distinct.
	 */
	public void testNullAddressNeverCoalesces() {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();

		recorder.record(step(null, "valueChanged", "a"), true);
		recorder.record(step(null, "valueChanged", "b"), true);

		assertEquals(2, recorder.steps().size());
	}

	/**
	 * Capture is a no-op while not recording.
	 */
	public void testNoCaptureWhenNotRecording() {
		ScriptRecorder recorder = new ScriptRecorder();

		recorder.record(step("/form/field[a]", "valueChanged", "1"), true);

		assertTrue(recorder.steps().isEmpty());
	}
}
