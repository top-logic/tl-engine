/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Ambient handle to the surrounding {@link WizardElement &lt;wizard&gt;}.
 *
 * <p>
 * Installed by the wizard into the child {@link ViewContext} of every step's content via
 * {@link ViewContext#withScope(Class, Object)}. Commands and actions inside a step
 * ({@link WizardNextCommand &lt;wizard-next&gt;}, {@link WizardBackCommand &lt;wizard-back&gt;},
 * {@link WizardGotoCommand &lt;wizard-goto&gt;}) and the rules that hide them
 * ({@link WizardHasNext &lt;wizard-has-next&gt;}, {@link WizardHasBack &lt;wizard-has-back&gt;})
 * look it up via {@link #lookup(ReactContext, String)}.
 * </p>
 *
 * <p>
 * The scope is a thin facade over the wizard's step {@link ViewChannel} and its step sequence: every
 * mutator reads the channel's current value, computes the step to go to and writes its key back. The
 * channel is the sole source of truth, so a step reached through a command and one reached through a
 * URL are the same thing.
 * </p>
 *
 * <p>
 * The step sequence is read afresh on every question, because how many steps a wizard has can change
 * while it is displayed.
 * </p>
 */
public class WizardScope {

	/**
	 * The {@link WizardScope} enclosing the given context.
	 *
	 * @param context
	 *        The context in which the requesting command, action or rule executes.
	 * @param tag
	 *        Configuration tag of the requester, quoted in the failure message.
	 * @return The scope of the innermost enclosing {@link WizardElement &lt;wizard&gt;}.
	 * @throws IllegalStateException
	 *         if the context is no {@link ViewContext}, or no wizard encloses it.
	 */
	public static WizardScope lookup(ReactContext context, String tag) {
		if (!(context instanceof ViewContext viewContext)) {
			throw new IllegalStateException(
				"<" + tag + "> requires a ViewContext, got " + context.getClass().getName());
		}
		WizardScope scope = viewContext.getScope(WizardScope.class);
		if (scope == null) {
			throw new IllegalStateException(
				"<" + tag + "> executed outside of any enclosing <wizard>.");
		}
		return scope;
	}

	private final ViewChannel _stepChannel;

	private final Supplier<List<WizardStep>> _steps;

	/**
	 * Creates a {@link WizardScope} over a fixed step sequence.
	 *
	 * @param stepChannel
	 *        The channel holding the key of the current step.
	 * @param steps
	 *        The steps, in the order the wizard walks them.
	 */
	public WizardScope(ViewChannel stepChannel, List<WizardStep> steps) {
		this(stepChannel, () -> steps);
	}

	/**
	 * Creates a {@link WizardScope} over a step sequence that can change while the wizard is
	 * displayed.
	 *
	 * @param stepChannel
	 *        The channel holding the key of the current step.
	 * @param steps
	 *        Answers the steps, in the order the wizard walks them, as they are now.
	 */
	public WizardScope(ViewChannel stepChannel, Supplier<List<WizardStep>> steps) {
		_stepChannel = stepChannel;
		_steps = steps;
	}

	/**
	 * The channel holding the key of the step displayed, the wizard's single source of truth.
	 *
	 * <p>
	 * Every move through the wizard is a write to this channel, so watching it is how something
	 * outside the wizard learns of one - a rule hiding a "Back" button on the first step, for
	 * instance. A value no step carries, {@code null} included, displays the first step.
	 * </p>
	 */
	public ViewChannel stepChannel() {
		return _stepChannel;
	}

	/**
	 * The steps of the wizard, in the order it walks them.
	 */
	public List<WizardStep> steps() {
		List<WizardStep> steps = _steps.get();
		return steps == null ? List.of() : steps;
	}

	/**
	 * The position of the step displayed, {@code -1} while the wizard has no steps at all.
	 *
	 * <p>
	 * A channel value no step carries names the first step, so a wizard whose channel has not been
	 * written yet starts at its beginning.
	 * </p>
	 */
	public int currentIndex() {
		List<WizardStep> steps = steps();
		if (steps.isEmpty()) {
			return -1;
		}
		int index = indexOf(steps, _stepChannel.get());
		return index < 0 ? 0 : index;
	}

	/**
	 * The step displayed, {@code null} while the wizard has no steps at all.
	 */
	public WizardStep current() {
		int index = currentIndex();
		return index < 0 ? null : steps().get(index);
	}

	/**
	 * Whether a step follows the one displayed.
	 */
	public boolean hasNext() {
		int index = currentIndex();
		return index >= 0 && index < steps().size() - 1;
	}

	/**
	 * Whether a step precedes the one displayed.
	 */
	public boolean hasBack() {
		return currentIndex() > 0;
	}

	/**
	 * Moves to the following step, or does nothing on the last one.
	 */
	public void next() {
		move(1);
	}

	/**
	 * Moves to the preceding step, or does nothing on the first one.
	 */
	public void back() {
		move(-1);
	}

	/**
	 * Moves to the step carrying the given key.
	 *
	 * <p>
	 * A key no step carries leaves the wizard where it is: the key is what the channel would hold,
	 * and a value naming no step would display the first one rather than what the caller asked for.
	 * </p>
	 *
	 * @param key
	 *        The {@link WizardStep#key() key} of the step to display.
	 */
	public void goTo(Object key) {
		List<WizardStep> steps = steps();
		int index = indexOf(steps, key);
		if (index < 0) {
			return;
		}
		_stepChannel.set(steps.get(index).key());
	}

	/**
	 * Moves the given number of positions through the sequence, staying within it.
	 */
	private void move(int offset) {
		List<WizardStep> steps = steps();
		int index = currentIndex();
		if (index < 0) {
			return;
		}
		int target = index + offset;
		if (target < 0 || target >= steps.size()) {
			return;
		}
		_stepChannel.set(steps.get(target).key());
	}

	/**
	 * The position of the step carrying the given key, {@code -1} if no step does.
	 */
	private static int indexOf(List<WizardStep> steps, Object key) {
		for (int i = 0; i < steps.size(); i++) {
			if (Objects.equals(steps.get(i).key(), key)) {
				return i;
			}
		}
		return -1;
	}
}
