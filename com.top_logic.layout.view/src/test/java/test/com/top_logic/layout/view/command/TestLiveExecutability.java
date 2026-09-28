/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.LiveExecutability;
import com.top_logic.layout.view.command.NullInputDisabled;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests for {@link LiveExecutability}: the state it answers for the input channel's value, and the
 * change callback it runs while attached, and only then.
 */
public class TestLiveExecutability extends TestCase {

	private ViewChannel _input;

	/** The number of times the change callback ran. */
	private int _changes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_input = new DefaultViewChannel("input");
		_changes = 0;
	}

	/**
	 * Tests that the state is the rule's answer for the value the channel holds now, attached or
	 * not.
	 */
	public void testStateFollowsChannelValue() {
		LiveExecutability live = live(NullInputDisabled.INSTANCE);

		assertFalse("The rule rejects the empty input.", live.getState().isExecutable());
		_input.set("value");
		assertTrue("The rule accepts the value now on the channel.", live.getState().isExecutable());
		assertEquals("A detached instance reports nothing.", 0, _changes);
		assertFalse("A supplied input is decided without the channel.", live.getState(null).isExecutable());
	}

	/**
	 * Tests that a new channel value runs the callback while attached, and no more after detaching.
	 */
	public void testChannelChangeReportedWhileAttached() {
		LiveExecutability live = live(NullInputDisabled.INSTANCE);
		live.attach(null);
		assertEquals("Attaching for the first time reports nothing.", 0, _changes);

		_input.set("value");
		assertEquals("The new channel value is reported.", 1, _changes);

		live.detach();
		_input.set("other");
		assertEquals("A detached instance reports nothing.", 1, _changes);
	}

	/**
	 * Tests that attaching twice registers the listeners once, and that attaching again after a
	 * detach reports the change that may have been missed meanwhile.
	 */
	public void testAttachIsIdempotentAndResumes() {
		LiveExecutability live = live(NullInputDisabled.INSTANCE);
		live.attach(null);
		live.attach(null);

		_input.set("value");
		assertEquals("The channel listener is registered once.", 1, _changes);

		live.detach();
		live.detach();
		assertFalse(live.isAttached());

		live.attach(null);
		assertEquals("Resuming reports a possibly missed change.", 2, _changes);
	}

	/**
	 * Tests that a rule reporting changes of its own is followed while attached, and that its
	 * reporting is stopped on detach.
	 */
	public void testObservableRuleFollowedWhileAttached() {
		ReportingRule rule = new ReportingRule();
		LiveExecutability live = live(rule);

		live.attach(null);
		assertNotNull("The rule is observed while attached.", rule._revalidate);
		rule._revalidate.run();
		assertEquals("The rule's report reaches the callback.", 1, _changes);

		live.detach();
		assertNull("The observation of the rule is stopped on detach.", rule._revalidate);
	}

	private LiveExecutability live(ViewExecutabilityRule rule) {
		return new LiveExecutability(rule, _input, Set.of(), () -> _changes++);
	}

	/**
	 * {@link ObservableRule} remembering the callback it reports to while observed.
	 */
	private static class ReportingRule implements ViewExecutabilityRule, ObservableRule {

		Runnable _revalidate;

		@Override
		public ExecutableState isExecutable(Object input) {
			return ExecutableState.EXECUTABLE;
		}

		@Override
		public Runnable observe(Runnable revalidate) {
			_revalidate = revalidate;
			return () -> _revalidate = null;
		}
	}

}
