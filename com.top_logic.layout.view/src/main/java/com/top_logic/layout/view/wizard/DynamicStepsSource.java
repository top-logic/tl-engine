/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelInputs;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.Inputs;
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.Supplies;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;

/**
 * One step of a {@link WizardElement &lt;wizard&gt;} per element of a list a channel holds.
 *
 * <p>
 * The step sequence follows that channel: appending an element adds a step behind the ones already
 * there, removing one takes its step away, and the wizard is re-expanded in the same breath - so a
 * command that appends an element and then moves on lands on the step it just created.
 * </p>
 *
 * <p>
 * The element itself is the key of its step, which is what the wizard's step channel holds while
 * that step is displayed. The {@link Config#getContent() content} is a template instantiated per
 * displayed step, with the element published on the
 * {@link Config#getElementChannel() element channel} - typically a
 * {@link com.top_logic.layout.view.element.SwitchElement &lt;switch&gt;} choosing the display by the
 * kind of element.
 * </p>
 *
 * <p>
 * Sources are mixable, so a flow can open with a written-out {@link StaticStepSource &lt;step&gt;},
 * continue over as many elements as the channel holds, and close with another written-out step.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;wizard current-step="currentStep"&gt;
 *   &lt;step id="welcome" label="wizard.welcome"&gt;...&lt;/step&gt;
 *   &lt;dynamic-steps steps="questions"
 *     step-as="question"
 *     label="$question.get(`demo:Question#title`)"
 *   &gt;
 *     &lt;switch input="element"&gt;...&lt;/switch&gt;
 *   &lt;/dynamic-steps&gt;
 *   &lt;step id="summary" label="wizard.summary"&gt;...&lt;/step&gt;
 * &lt;/wizard&gt;
 * </pre>
 */
@InApp
public class DynamicStepsSource implements WizardStepSource {

	/**
	 * Configuration for {@link DynamicStepsSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends WizardStepSource.Config, Inputs {

		/** Configuration tag of a {@link DynamicStepsSource}. */
		String TAG_NAME = "dynamic-steps";

		/** Configuration name for {@link #getSteps()}. */
		String STEPS = "steps";

		/** Configuration name for {@link #getElementChannel()}. */
		String ELEMENT_CHANNEL = "element-channel";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getStepAs()}. */
		String STEP_AS = "step-as";

		/** Name of the variable holding the element of a step when no other name is given. */
		String DEFAULT_STEP_AS = "step";

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/** Configuration name for {@link #getAutoAdvance()}. */
		String AUTO_ADVANCE = "auto-advance";

		@Override
		@ClassDefault(DynamicStepsSource.class)
		Class<? extends WizardStepSource> getImplementationClass();

		/**
		 * Reference to the channel holding the elements, one step each, in the order of the list.
		 *
		 * <p>
		 * A channel holding nothing contributes no step at all; a channel holding a single object
		 * that is no list contributes one.
		 * </p>
		 */
		@Name(STEPS)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getSteps();

		/**
		 * Name of the channel publishing the element of a step to the
		 * {@link #getContent() content template}.
		 */
		@Name(ELEMENT_CHANNEL)
		@StringDefault("element")
		String getElementChannel();

		/**
		 * The name of the variable holding the element of a step in the scripts computing its
		 * {@link #getLabel() label}, {@link #getIcon() icon} and {@link #getAutoAdvance() time}.
		 *
		 * <p>
		 * Unset, the variable is named {@value #DEFAULT_STEP_AS}. With
		 * <code>step-as="question"</code>, a step is labeled by
		 * <code>$question.get(`my:Question#title`)</code>. The scripts see the element behind the
		 * values of their {@link #getInputs() inputs}; an input of the same name is a configuration
		 * error and is renamed with <code>as</code>.
		 * </p>
		 */
		@Name(STEP_AS)
		@Nullable
		String getStepAs();

		/**
		 * TL-Script expression naming a step in the step indicator.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} as variables named after
		 * their channels, or by their {@code as} names, and the element of the step as the variable
		 * {@value #DEFAULT_STEP_AS}, or by the name given with {@value #STEP_AS}:
		 * </p>
		 *
		 * <pre>
		 * $step.get(`my:Question#title`)
		 * </pre>
		 *
		 * <p>
		 * Without one, the element is named as it is named everywhere else in the application.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		@Supplies(value = DEFAULT_STEP_AS, renamedBy = STEP_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getLabel();

		/**
		 * TL-Script expression choosing the icon shown beside the label, e.g.
		 * <code>"css:fa-solid fa-user"</code>.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} and the element of the
		 * step as variables, like the {@link #getLabel() label}. Without one, the steps carry no
		 * icon.
		 * </p>
		 */
		@Name(ICON)
		@Nullable
		@Supplies(value = DEFAULT_STEP_AS, renamedBy = STEP_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getIcon();

		/**
		 * TL-Script expression saying how long a step is displayed before the wizard moves on by
		 * itself, in milliseconds.
		 *
		 * <p>
		 * The expression sees the values of the {@link #getInputs() inputs} and the element of the
		 * step as variables, like the {@link #getLabel() label}. An element the expression answers
		 * nothing for is a step the user leaves; without an expression that is every step.
		 * </p>
		 *
		 * <p>
		 * The time runs while the flow leads through the step. A step the user came back to waits
		 * for them, whatever the function answers for its element.
		 * </p>
		 */
		@Name(AUTO_ADVANCE)
		@Nullable
		@Supplies(value = DEFAULT_STEP_AS, renamedBy = STEP_AS)
		@ScriptContextVariables(InputScript.Variables.class)
		Expr getAutoAdvance();

		/**
		 * The content displayed while one of these steps is the current one, with that step's
		 * element published on the {@link #getElementChannel() element channel}.
		 *
		 * @implNote Written directly as children of the {@code <dynamic-steps>}.
		 *           Keyed by {@link UIElement.Config#ID} so that a same-path overlay can
		 *           address an element.
		 */
		@Key(UIElement.Config.ID)
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<UIElement.Config> getContent();
	}

	private final ChannelRef _stepsRef;

	private final List<InputRef> _inputs;

	private final String _elementChannelName;

	private final QueryExecutor _label;

	private final QueryExecutor _icon;

	private final QueryExecutor _autoAdvance;

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link DynamicStepsSource} from configuration.
	 */
	@CalledByReflection
	public DynamicStepsSource(InstantiationContext context, Config config) {
		_stepsRef = config.getSteps();
		_elementChannelName = config.getElementChannel();
		_inputs = config.getInputs();
		_label = InputScript.compile(config, Config.LABEL);
		_icon = InputScript.compile(config, Config.ICON);
		_autoAdvance = InputScript.compile(config, Config.AUTO_ADVANCE);
		_content = config.getContent().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public List<WizardStep> steps(ViewContext context) {
		List<Object> elements = elements(context.resolveChannel(_stepsRef).get());
		List<ViewChannel> inputs = ChannelInputs.resolve(context, _inputs);
		List<WizardStep> result = new ArrayList<>(elements.size());
		for (Object element : elements) {
			result.add(new WizardStep(element, label(inputs, element), icon(inputs, element),
				stepContext -> content(stepContext, element), autoAdvance(inputs, element)));
		}
		return result;
	}

	/**
	 * The channel holding the elements, followed by the {@link Config#getInputs() inputs} the
	 * scripts naming the steps read.
	 */
	@Override
	public List<ViewChannel> observedChannels(ViewContext context) {
		List<ViewChannel> result = new ArrayList<>();
		result.add(context.resolveChannel(_stepsRef));
		result.addAll(ChannelInputs.resolve(context, _inputs));
		return result;
	}

	@Override
	public List<ChildGroup> childGroups() {
		return List.of(ChildGroup.elements(_content));
	}

	/**
	 * The elements the channel value stands for: the entries of a collection, the single object of
	 * a channel holding one, nothing for a channel holding nothing.
	 */
	private static List<Object> elements(Object value) {
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return value == null ? Collections.emptyList() : Collections.singletonList(value);
	}

	/**
	 * The name of the given element's step: what the label function answers, or how the element is
	 * named everywhere else in the application.
	 */
	private ResKey label(List<ViewChannel> inputs, Object element) {
		String text = _label != null ? asText(_label.execute(ChannelInputs.arguments(inputs, element)))
			: MetaLabelProvider.INSTANCE.getLabel(element);
		return StringServices.isEmpty(text) ? null : ResKey.text(text);
	}

	/**
	 * The encoded icon of the given element's step, {@code null} for a step without one.
	 */
	private String icon(List<ViewChannel> inputs, Object element) {
		if (_icon == null) {
			return null;
		}
		String encoded = asText(_icon.execute(ChannelInputs.arguments(inputs, element)));
		return StringServices.isEmpty(encoded) ? null : encoded;
	}

	/**
	 * How long the given element's step is displayed before the wizard moves on by itself,
	 * {@code null} for a step the user leaves.
	 */
	private Long autoAdvance(List<ViewChannel> inputs, Object element) {
		if (_autoAdvance == null) {
			return null;
		}
		Object millis = _autoAdvance.execute(ChannelInputs.arguments(inputs, element));
		return millis instanceof Number number ? Long.valueOf(number.longValue()) : null;
	}

	/**
	 * The content of the step displaying the given element, in a child context that publishes the
	 * element on the element channel.
	 */
	private ReactControl content(ViewContext stepContext, Object element) {
		DefaultViewChannel elementChannel = new DefaultViewChannel(_elementChannelName);
		elementChannel.set(element);
		ViewContext contentContext = stepContext.withLocalChannel(_elementChannelName, elementChannel);
		ReactControl content = ContentControls.toControl(_content, contentContext);
		ObservingChannels.observeWhileDisplayed(content, List.of(elementChannel), contentContext);
		return content;
	}

	/**
	 * The given script result as the text the indicator displays.
	 */
	private static String asText(Object value) {
		return value == null ? null : value.toString();
	}
}
