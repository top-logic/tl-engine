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
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
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
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.ContentControls;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

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
 *     label="q -&gt; $q.get(`demo:Question#title`)"
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
	public interface Config extends WizardStepSource.Config {

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
		 * TL-Script function naming a step in the step indicator: {@code element -> text}.
		 *
		 * <p>
		 * Without one, the element is named as it is named everywhere else in the application.
		 * </p>
		 */
		@Name(LABEL)
		@Nullable
		Expr getLabel();

		/**
		 * TL-Script function choosing the icon shown beside the label:
		 * {@code element -> "css:fa-solid fa-user"}.
		 *
		 * <p>
		 * Without one, the steps carry no icon.
		 * </p>
		 */
		@Name(ICON)
		@Nullable
		Expr getIcon();

		/**
		 * TL-Script function saying how long a step is displayed before the wizard moves on by
		 * itself: {@code element -> millis}.
		 *
		 * <p>
		 * An element the function answers nothing for is a step the user leaves; without a function
		 * that is every step.
		 * </p>
		 *
		 * <p>
		 * The time runs while the flow leads through the step. A step the user came back to waits
		 * for them, whatever the function answers for its element.
		 * </p>
		 */
		@Name(AUTO_ADVANCE)
		@Nullable
		Expr getAutoAdvance();

		/**
		 * The content displayed while one of these steps is the current one, with that step's
		 * element published on the {@link #getElementChannel() element channel}.
		 *
		 * @implNote Written directly as children of the {@code <dynamic-steps>}.
		 */
		@Name(CONTENT)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final ChannelRef _stepsRef;

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
		_label = config.getLabel() == null ? null : QueryExecutor.compile(config.getLabel());
		_icon = config.getIcon() == null ? null : QueryExecutor.compile(config.getIcon());
		_autoAdvance = config.getAutoAdvance() == null ? null : QueryExecutor.compile(config.getAutoAdvance());
		_content = config.getContent().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
	}

	@Override
	public List<WizardStep> steps(ViewContext context) {
		List<Object> elements = elements(context.resolveChannel(_stepsRef).get());
		List<WizardStep> result = new ArrayList<>(elements.size());
		for (Object element : elements) {
			result.add(new WizardStep(element, label(element), icon(element),
				stepContext -> content(stepContext, element), autoAdvance(element)));
		}
		return result;
	}

	@Override
	public List<ViewChannel> observedChannels(ViewContext context) {
		return List.of(context.resolveChannel(_stepsRef));
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
	private ResKey label(Object element) {
		String text = _label != null ? asText(_label.execute(element)) : MetaLabelProvider.INSTANCE.getLabel(element);
		return StringServices.isEmpty(text) ? null : ResKey.text(text);
	}

	/**
	 * The encoded icon of the given element's step, {@code null} for a step without one.
	 */
	private String icon(Object element) {
		if (_icon == null) {
			return null;
		}
		String encoded = asText(_icon.execute(element));
		return StringServices.isEmpty(encoded) ? null : encoded;
	}

	/**
	 * How long the given element's step is displayed before the wizard moves on by itself,
	 * {@code null} for a step the user leaves.
	 */
	private Long autoAdvance(Object element) {
		if (_autoAdvance == null) {
			return null;
		}
		Object millis = _autoAdvance.execute(element);
		return millis instanceof Number number ? Long.valueOf(number.longValue()) : null;
	}

	/**
	 * The content of the step displaying the given element, in a child context that publishes the
	 * element on the element channel.
	 */
	private ReactControl content(ViewContext stepContext, Object element) {
		DefaultViewChannel elementChannel = new DefaultViewChannel(_elementChannelName);
		elementChannel.set(element);
		return ContentControls.toControl(_content,
			stepContext.withLocalChannel(_elementChannelName, elementChannel));
	}

	/**
	 * The given script result as the text the indicator displays.
	 */
	private static String asText(Object value) {
		return value == null ? null : value.toString();
	}
}
