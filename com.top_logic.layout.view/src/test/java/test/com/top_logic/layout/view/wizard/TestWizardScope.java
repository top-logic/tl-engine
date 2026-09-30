/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.wizard.WizardBackAction;
import com.top_logic.layout.view.wizard.WizardGotoAction;
import com.top_logic.layout.view.wizard.WizardNextAction;
import com.top_logic.layout.view.wizard.WizardScope;
import com.top_logic.layout.view.wizard.WizardStep;

/**
 * Tests for {@link WizardScope}: where in a step sequence a channel value places the wizard, and
 * what the moves write back.
 */
public class TestWizardScope extends TestCase {

	private static final List<WizardStep> STEPS =
		List.of(step("contact"), step("payment"), step("summary"));

	/**
	 * Tests that a wizard whose channel has not been written yet displays its first step.
	 */
	public void testNullValueStartsAtFirstStep() {
		WizardScope scope = scope(null);

		assertEquals(0, scope.currentIndex());
		assertEquals("contact", scope.current().key());
		assertFalse("Nothing precedes the first step.", scope.hasBack());
		assertTrue(scope.hasNext());
	}

	/**
	 * Tests that a channel value naming no step displays the first one.
	 */
	public void testUnknownValueStartsAtFirstStep() {
		WizardScope scope = scope("nonsense");

		assertEquals(0, scope.currentIndex());
		assertEquals("contact", scope.current().key());
	}

	/**
	 * Tests that the position is read from the channel value.
	 */
	public void testValueNamesStep() {
		WizardScope scope = scope("payment");

		assertEquals(1, scope.currentIndex());
		assertEquals("payment", scope.current().key());
		assertTrue(scope.hasBack());
		assertTrue(scope.hasNext());
	}

	/**
	 * Tests that moving on writes the key of the following step, not its position.
	 */
	public void testNextWritesKeyOfFollowingStep() {
		ViewChannel channel = channel(null);
		WizardScope scope = new WizardScope(channel, STEPS);

		scope.next();

		assertEquals("payment", channel.get());
	}

	/**
	 * Tests that moving on from the last step changes nothing.
	 */
	public void testNextOnLastStepDoesNothing() {
		ViewChannel channel = channel("summary");
		WizardScope scope = new WizardScope(channel, STEPS);

		assertFalse("Nothing follows the last step.", scope.hasNext());
		scope.next();

		assertEquals("summary", channel.get());
	}

	/**
	 * Tests that stepping back writes the key of the preceding step.
	 */
	public void testBackWritesKeyOfPrecedingStep() {
		ViewChannel channel = channel("summary");
		WizardScope scope = new WizardScope(channel, STEPS);

		scope.back();

		assertEquals("payment", channel.get());
	}

	/**
	 * Tests that stepping back from the first step changes nothing, the unwritten channel included.
	 */
	public void testBackOnFirstStepDoesNothing() {
		ViewChannel channel = channel(null);
		WizardScope scope = new WizardScope(channel, STEPS);

		assertFalse("Nothing precedes the first step.", scope.hasBack());
		scope.back();

		assertNull(channel.get());
	}

	/**
	 * Tests that a jump writes the key of the step it names, wherever in the sequence it lies.
	 */
	public void testGoToKnownKey() {
		ViewChannel channel = channel(null);
		WizardScope scope = new WizardScope(channel, STEPS);

		scope.goTo("summary");
		assertEquals("summary", channel.get());

		scope.goTo("contact");
		assertEquals("contact", channel.get());
	}

	/**
	 * Tests that a jump to a key no step carries leaves the wizard where it is.
	 */
	public void testGoToUnknownKeyDoesNothing() {
		ViewChannel channel = channel("payment");
		WizardScope scope = new WizardScope(channel, STEPS);

		scope.goTo("nonsense");

		assertEquals("payment", channel.get());
		assertEquals(1, scope.currentIndex());
	}

	/**
	 * Tests that a wizard without steps has no position and no move to make.
	 */
	public void testEmptyWizardHasNoStep() {
		ViewChannel channel = channel(null);
		WizardScope scope = new WizardScope(channel, List.of());

		assertEquals(-1, scope.currentIndex());
		assertNull(scope.current());
		assertFalse(scope.hasNext());
		assertFalse(scope.hasBack());

		scope.next();
		scope.back();
		assertNull(channel.get());
	}

	/**
	 * Tests that the scope reads the step sequence afresh, so that a wizard gaining a step offers
	 * the move to it.
	 */
	public void testSequenceIsReadAfresh() {
		ViewChannel channel = channel("contact");
		List<WizardStep> steps = new ArrayList<>(List.of(step("contact")));
		WizardScope scope = new WizardScope(channel, () -> steps);

		assertFalse(scope.hasNext());

		steps.add(step("summary"));

		assertTrue(scope.hasNext());
		scope.next();
		assertEquals("summary", channel.get());
	}

	/**
	 * Tests that the actions move the wizard enclosing the context they run in.
	 */
	public void testActionsMoveEnclosingWizard() {
		ViewChannel channel = channel(null);
		ViewContext context = stepContext(new WizardScope(channel, STEPS));
		Object input = new Object();

		assertSame("The action following the move still sees the object.", input,
			new WizardNextAction().execute(context, input));
		assertEquals("payment", channel.get());

		new WizardGotoAction("summary").execute(context, input);
		assertEquals("summary", channel.get());

		new WizardBackAction().execute(context, input);
		assertEquals("payment", channel.get());
	}

	/**
	 * Tests that a jump without a configured step takes the chain value as its target.
	 */
	public void testGotoActionTakesChainValue() {
		ViewChannel channel = channel(null);
		ViewContext context = stepContext(new WizardScope(channel, STEPS));

		new WizardGotoAction(null).execute(context, "summary");

		assertEquals("summary", channel.get());
	}

	/**
	 * Tests that a move outside of any wizard fails, naming the offending tag.
	 */
	public void testMoveOutsideWizardFails() {
		ViewContext context = new DefaultViewContext(null);

		try {
			new WizardNextAction().execute(context, null);
			fail("Expected failure without an enclosing wizard.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(),
				expected.getMessage().contains(WizardNextAction.Config.TAG_NAME));
		}
	}

	/**
	 * Tests that a move outside of the view layer fails.
	 */
	public void testMoveOutsideViewContextFails() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));

		try {
			new WizardBackAction().execute(context, null);
			fail("Expected failure without a ViewContext.");
		} catch (IllegalStateException expected) {
			assertTrue(expected.getMessage(),
				expected.getMessage().contains(ViewContext.class.getSimpleName()));
		}
	}

	private static WizardScope scope(Object value) {
		return new WizardScope(channel(value), STEPS);
	}

	private static ViewChannel channel(Object value) {
		DefaultViewChannel channel = new DefaultViewChannel("currentStep");
		channel.set(value);
		return channel;
	}

	private static ViewContext stepContext(WizardScope scope) {
		return new DefaultViewContext(null).withScope(WizardScope.class, scope);
	}

	private static WizardStep step(String id) {
		return new WizardStep(id, null, null, context -> null, null);
	}
}
