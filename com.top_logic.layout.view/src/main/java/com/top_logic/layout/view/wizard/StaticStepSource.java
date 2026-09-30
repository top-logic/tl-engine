/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.wizard;

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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.ContentControls;

/**
 * A single step of a {@link WizardElement &lt;wizard&gt;}, written out in the view.
 *
 * <p>
 * The step is identified by its {@link Config#getId() id}: that is the value the wizard's step
 * channel holds while the step is displayed, and the value a
 * {@link WizardGotoAction &lt;wizard-goto&gt;} names to jump to it.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;wizard current-step="currentStep"&gt;
 *   &lt;step id="contact" label="wizard.contact" icon="css:fa-solid fa-user"&gt;
 *     &lt;form input="order"&gt;...&lt;/form&gt;
 *   &lt;/step&gt;
 *   &lt;step id="summary" label="wizard.summary"&gt;...&lt;/step&gt;
 * &lt;/wizard&gt;
 * </pre>
 */
@InApp
public class StaticStepSource implements WizardStepSource {

	/**
	 * Configuration for {@link StaticStepSource}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends WizardStepSource.Config {

		/** Configuration tag of a {@link StaticStepSource}. */
		String TAG_NAME = "step";

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/** Configuration name for {@link #getAutoAdvance()}. */
		String AUTO_ADVANCE = "auto-advance";

		@Override
		@ClassDefault(StaticStepSource.class)
		Class<? extends WizardStepSource> getImplementationClass();

		/**
		 * Identifies this step within its wizard.
		 *
		 * <p>
		 * The wizard's step channel holds this value while the step is displayed, and a jump to the
		 * step names it.
		 * </p>
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * Name of the step in the step indicator.
		 */
		@Name(LABEL)
		@Nullable
		ResKey getLabel();

		/**
		 * The icon shown beside the label in the step indicator (e.g.
		 * {@code "css:fa-solid fa-user"}), or empty for a step without one.
		 */
		@Name(ICON)
		@Nullable
		String getIcon();

		/**
		 * How long this step is displayed before the wizard moves on by itself, written as a
		 * duration ({@code 2s}, {@code 500ms}).
		 *
		 * <p>
		 * For an interstitial the user only watches. Empty for a step the user leaves.
		 * </p>
		 *
		 * <p>
		 * The time runs while the flow leads through the step. A step the user came back to waits
		 * for them, so a Back out of the step behind an interstitial is not answered by being sent
		 * forward again.
		 * </p>
		 */
		@Name(AUTO_ADVANCE)
		@Nullable
		@Format(MillisFormat.class)
		Long getAutoAdvance();

		/**
		 * The content displayed while this step is the current one.
		 *
		 * @implNote Written directly as children of the {@code <step>}.
		 */
		@Name(CHILDREN)
		@DefaultContainer
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final String _id;

	private final ResKey _label;

	private final String _icon;

	private final List<UIElement> _children;

	private final Long _autoAdvance;

	/**
	 * Creates a new {@link StaticStepSource} from configuration.
	 */
	@CalledByReflection
	public StaticStepSource(InstantiationContext context, Config config) {
		_id = config.getId();
		_label = config.getLabel();
		String icon = config.getIcon();
		_icon = icon == null || icon.isEmpty() ? null : icon;
		_children = config.getChildren().stream()
			.map(context::getInstance)
			.collect(Collectors.toList());
		_autoAdvance = config.getAutoAdvance();
	}

	@Override
	public List<WizardStep> steps(ViewContext context) {
		return List.of(new WizardStep(_id, _label, _icon,
			stepContext -> ContentControls.toControl(_children, stepContext), _autoAdvance));
	}

	@Override
	public List<ChildGroup> childGroups() {
		return List.of(ChildGroup.keyed(_id, _children));
	}
}
