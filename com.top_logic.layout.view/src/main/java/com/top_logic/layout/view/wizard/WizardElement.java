/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * UIElement that leads through a sequence of steps, displaying one of them at a time.
 *
 * <p>
 * The key of the step displayed lives in the channel referenced by
 * {@link Config#getCurrentStep()}, and that channel is the single source of truth: moving through
 * the wizard is a write to it, whether
 * the move comes from a {@link WizardNextCommand &lt;wizard-next&gt;}, a
 * {@link WizardBackCommand &lt;wizard-back&gt;}, a {@link WizardGotoCommand &lt;wizard-goto&gt;} or
 * from the step indicator. A value no step carries, {@code null} included, displays the first step,
 * so a wizard starts at its beginning without anyone writing the channel first. Binding the channel
 * to the address of the page through {@code <param-bindings>} makes a step a deep link.
 * </p>
 *
 * <p>
 * The step sequence is the concatenation of what the wizard's {@link Config#getSteps() sources}
 * contribute; a {@link StaticStepSource &lt;step&gt;} contributes exactly the one step it is
 * written as, a {@link DynamicStepsSource &lt;dynamic-steps&gt;} one per element of a list a channel
 * holds, and the two mix in one wizard. The sequence is expanded anew whenever a channel a source
 * decides by takes a new value. The content of the step displayed is built when the step is reached
 * and disposed when it is left, so a step leaves nothing behind in the enclosing scope - a form's
 * Save button included.
 * </p>
 *
 * <p>
 * The wizard displays the step indicator and the content of the step, and no navigation buttons of
 * its own: what moves through the wizard is composed in the view from the commands above, inside a
 * step or raised out of it through a {@code <slot-content>}. Every step's content sees a
 * {@link WizardScope}, so those commands need no configuration to find the wizard they move.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view&gt;
 *   &lt;channels&gt;&lt;channel name="currentStep"/&gt;&lt;/channels&gt;
 *   &lt;wizard current-step="currentStep" step-list="true"&gt;
 *     &lt;step id="contact" label="wizard.contact"&gt;...&lt;/step&gt;
 *     &lt;step id="payment" label="wizard.payment"&gt;...&lt;/step&gt;
 *     &lt;step id="summary" label="wizard.summary"&gt;...&lt;/step&gt;
 *   &lt;/wizard&gt;
 * &lt;/view&gt;
 * </pre>
 */
@InApp
public class WizardElement implements UIElement {

	/**
	 * Configuration for {@link WizardElement}.
	 */
	@TagName("wizard")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getCurrentStep()}. */
		String CURRENT_STEP = "current-step";

		/** Configuration name for {@link #getSteps()}. */
		String STEPS = "steps";

		/** Configuration name for {@link #getCounter()}. */
		String COUNTER = "counter";

		/** Configuration name for {@link #getProgress()}. */
		String PROGRESS = "progress";

		/** Configuration name for {@link #getStepList()}. */
		String STEP_LIST = "step-list";

		@Override
		@ClassDefault(WizardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Reference to the channel holding the key of the step displayed.
		 *
		 * <p>
		 * The channel must be declared in an enclosing view. A value no step carries, {@code null}
		 * included, displays the first step.
		 * </p>
		 */
		@Name(CURRENT_STEP)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getCurrentStep();

		/**
		 * The sources contributing the steps, in the order the wizard walks them.
		 *
		 * @implNote Written directly as children of the {@code <wizard>}.
		 */
		@Name(STEPS)
		@DefaultContainer
		@TreeProperty
		List<PolymorphicConfiguration<? extends WizardStepSource>> getSteps();

		/**
		 * Whether the indicator counts the step displayed against the number of steps.
		 */
		@Name(COUNTER)
		@BooleanDefault(true)
		boolean getCounter();

		/**
		 * Whether the indicator shows how far through the wizard the step displayed is, as a bar.
		 */
		@Name(PROGRESS)
		@BooleanDefault(true)
		boolean getProgress();

		/**
		 * Whether the indicator lists the steps by name, marking each one as done, current or still
		 * ahead.
		 *
		 * <p>
		 * A step already done is offered for a jump back to it; a step still ahead is not, because
		 * the way there leads through the steps in between.
		 * </p>
		 */
		@Name(STEP_LIST)
		@BooleanDefault(false)
		boolean getStepList();
	}

	private final ChannelRef _stepRef;

	private final List<WizardStepSource> _sources;

	private final boolean _counter;

	private final boolean _progress;

	private final boolean _stepList;

	private final String _cssClass;

	/**
	 * Creates a new {@link WizardElement} from configuration.
	 */
	@CalledByReflection
	public WizardElement(InstantiationContext context, Config config) {
		_stepRef = config.getCurrentStep();
		_sources = config.getSteps().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_counter = config.getCounter();
		_progress = config.getProgress();
		_stepList = config.getStepList();
		_cssClass = config.getCssClass();
	}

	/**
	 * The sources contributing the steps, in the order the wizard walks them.
	 */
	public List<WizardStepSource> getSources() {
		return _sources;
	}

	/**
	 * Whether the indicator counts the step displayed against the number of steps.
	 */
	public boolean hasCounter() {
		return _counter;
	}

	/**
	 * Whether the indicator shows how far through the wizard the step displayed is, as a bar.
	 */
	public boolean hasProgress() {
		return _progress;
	}

	/**
	 * Whether the indicator lists the steps by name.
	 */
	public boolean hasStepList() {
		return _stepList;
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		List<ChildGroup> result = new ArrayList<>();
		for (WizardStepSource source : _sources) {
			result.addAll(source.childGroups());
		}
		return result;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel stepChannel = context.resolveChannel(_stepRef);
		ReactWizardControl result = new ReactWizardControl(context, this, stepChannel);
		result.setCssClass(_cssClass);
		return result;
	}
}
