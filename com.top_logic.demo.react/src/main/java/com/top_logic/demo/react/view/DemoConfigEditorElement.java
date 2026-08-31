/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.view;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import java.util.List;

import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;

/**
 * {@link UIElement} that renders the React configuration editor for a fresh
 * {@link DemoEditorConfig}.
 *
 * <p>
 * Demonstrates the config editor control over a self-contained sample configuration covering a
 * spread of property kinds.
 * </p>
 *
 * <p>
 * Built with {@link ConfigFormControl}'s edit mode on, so the demo shows the whole cycle rather
 * than a form that writes straight through: the configuration is read-only until "Edit" starts a
 * working copy, "Apply" checks that copy and either carries it over or leaves edit mode open with
 * the reasons shown, and "Cancel" drops it. {@link DemoEditorConfig} carries what that cycle needs
 * something to say about - a mandatory property for a refused Apply, and a property whose format
 * can reject what is typed into it.
 * </p>
 *
 * <p>
 * Those three commands go into the enclosing panel's toolbar rather than beside the fields, so the
 * demo looks like what it stands for: a user interface the application generated from a model. A
 * {@code <form>} over a model does exactly this, and the form then fills the panel instead of
 * sharing a row with its own buttons.
 * </p>
 */
public class DemoConfigEditorElement implements UIElement {

	/**
	 * Configuration for {@link DemoConfigEditorElement}.
	 */
	@TagName("config-editor-demo")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(DemoConfigEditorElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link DemoConfigEditorElement} from configuration.
	 */
	@CalledByReflection
	public DemoConfigEditorElement(InstantiationContext context, Config config) {
		// No additional state needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		DemoEditorConfig config = TypedConfiguration.newConfigItem(DemoEditorConfig.class);
		ConfigFormControl form = new ConfigFormControl(context, config, ConfigFormControl.Commands.TOOLBAR);
		contributeWhileDisplayed(form, context.getScope(CommandScope.class), form.commands());
		return form;
	}

	/**
	 * Puts the form's commands into the enclosing scope while the form is displayed, and takes them
	 * out again while it is not.
	 *
	 * <p>
	 * Bound to the control's attach and detach rather than to its creation and disposal: content
	 * that exists but is not displayed - the inactive tab of a tab bar, the hidden child of any
	 * one-of-N container - keeps its controls, and its commands must not stay in the panel's
	 * toolbar meanwhile. The same reasoning, and the same shape, as
	 * {@code FormElement#contributeWhileDisplayed}.
	 * </p>
	 *
	 * @param scope
	 *        The enclosing scope, or {@code null} when the element is used outside any - the form
	 *        then simply offers no commands, rather than failing to render.
	 */
	private static void contributeWhileDisplayed(ConfigFormControl form, CommandScope scope,
			List<CommandModel> commands) {
		if (scope == null) {
			return;
		}
		form.addAttachListener(() -> {
			for (CommandModel command : commands) {
				scope.addCommand(command);
			}
		});
		form.addDetachListener(() -> {
			for (CommandModel command : commands) {
				scope.removeCommand(command);
			}
		});
		form.addCleanupAction(() -> {
			// Disposal while not displayed: the detach listener did not run, or ran already.
			for (CommandModel command : commands) {
				scope.removeCommand(command);
			}
		});
	}
}
