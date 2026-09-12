/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} editing a {@link ConfigurationItem} - the configuration counterpart of
 * {@link FormElement}.
 *
 * <p>
 * The item comes from the {@link Config#getInput() input channel}, so a configuration selected
 * elsewhere (a table row, a tree node) can be edited here. It may equally come from nowhere: with a
 * {@link Config#getType() type} configured, the element edits a fresh item of that type whenever the
 * channel has nothing to offer. That is the case an ordinary {@code <form>} cannot serve at all -
 * there is no model object anywhere, only a configuration.
 * </p>
 *
 * <p>
 * Edit mode works as it does for {@code <form withEditMode="true">}: the item is read-only until
 * Edit starts a working copy, Apply checks it and carries it over, Cancel drops it. Those three
 * commands go to the enclosing panel's toolbar rather than among the fields, which is what makes a
 * configuration form look like the rest of the application.
 * </p>
 *
 * <p>
 * A channel value that is not a {@link ConfigurationItem} is treated as no value: the channel is
 * shared with whatever else is bound to it, and a form is not the place to complain about a
 * selection that simply is not for it.
 * </p>
 */
public class ConfigFormElement implements UIElement {

	/**
	 * Configuration for {@link ConfigFormElement}.
	 */
	@TagName("config-form")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getWithEditMode()}. */
		String WITH_EDIT_MODE = "withEditMode";

		/** Configuration name for {@link #getNoModelMessage()}. */
		String NO_MODEL_MESSAGE = "noModelMessage";

		@Override
		@ClassDefault(ConfigFormElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel carrying the {@link ConfigurationItem} to edit.
		 *
		 * <p>
		 * Optional: without it the element edits a fresh item of {@link #getType()}.
		 * </p>
		 */
		@Name(INPUT)
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * The configuration interface to create an item of when {@link #getInput()} has no value.
		 *
		 * <p>
		 * Without it, an empty channel means there is nothing to edit and
		 * {@link #getNoModelMessage()} is shown instead.
		 * </p>
		 */
		@Name(TYPE)
		Class<? extends ConfigurationItem> getType();

		/**
		 * Whether the item is edited through a working copy, with Edit, Apply and Cancel.
		 *
		 * <p>
		 * Off, the form writes every change straight into the item - for a caller that has its own
		 * save cycle, or an item that is nobody's persistent state.
		 * </p>
		 */
		@Name(WITH_EDIT_MODE)
		@BooleanDefault(true)
		boolean getWithEditMode();

		/**
		 * Shown in place of the form while there is nothing to edit.
		 */
		@Name(NO_MODEL_MESSAGE)
		ResKey getNoModelMessage();
	}

	private final Config _config;

	/**
	 * Creates a new {@link ConfigFormElement} from configuration.
	 */
	@CalledByReflection
	public ConfigFormElement(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = _config.getInput() == null ? null : context.resolveChannel(_config.getInput());
		CommandScope scope = context.getScope(CommandScope.class);

		ReactStackControl holder = new ReactStackControl(context, Collections.emptyList());
		Display display = new Display(context, holder, scope);
		display.show(itemOf(input));

		if (input != null) {
			// A new value replaces the form wholesale rather than re-pointing the existing one: the
			// working copy belongs to the item it was taken from, so a change of item drops it
			// either way, and building afresh keeps a single path instead of two.
			input.addListener((sender, oldValue, newValue) -> display.show(itemOf(input)));
		}
		return holder;
	}

	/**
	 * The item to edit for the given channel's current value: the value itself if it is a
	 * {@link ConfigurationItem}, otherwise a fresh item of the configured type, otherwise none.
	 */
	private ConfigurationItem itemOf(ViewChannel input) {
		Object value = input == null ? null : input.get();
		if (value instanceof ConfigurationItem item) {
			return item;
		}
		Class<? extends ConfigurationItem> type = _config.getType();
		return type == null ? null : TypedConfiguration.newConfigItem(type);
	}

	/**
	 * What the element currently shows - the form over an item, or the message that there is none.
	 *
	 * <p>
	 * Holds the swap in one place, so the form's disposal and the removal of its commands from the
	 * toolbar cannot drift apart: a form that is no longer displayed must not leave an Edit button
	 * behind in the panel that now shows a message.
	 * </p>
	 */
	private final class Display {

		private final ViewContext _context;

		private final ReactStackControl _holder;

		private final CommandScope _scope;

		private ConfigFormControl _form;

		Display(ViewContext context, ReactStackControl holder, CommandScope scope) {
			_context = context;
			_holder = holder;
			_scope = scope;
		}

		/**
		 * Shows the form over the given item, or the no-model message if there is none.
		 */
		void show(ConfigurationItem item) {
			disposeForm();

			if (item == null) {
				_holder.setChildren(List.of(new ReactTextControl(_context, noModelMessage())));
				return;
			}

			ConfigFormControl.Commands commands = _config.getWithEditMode()
				? ConfigFormControl.Commands.TOOLBAR
				: ConfigFormControl.Commands.NONE;
			_form = new ConfigFormControl(_context, item, commands);
			contribute(_form);
			_holder.setChildren(List.of(_form));
		}

		/**
		 * Puts the form's commands into the enclosing toolbar while the form is displayed, and
		 * takes them out again while it is not.
		 *
		 * <p>
		 * Bound to attach and detach rather than to creation and disposal, for the reason
		 * {@link FormElement} gives: content that exists but is not shown - the inactive tab of a
		 * tab bar, the hidden child of any one-of-N container - keeps its controls, and its
		 * commands must not stay in the panel's toolbar meanwhile.
		 * </p>
		 */
		private void contribute(ConfigFormControl form) {
			if (_scope == null) {
				return;
			}
			List<CommandModel> commands = form.commands();
			form.addAttachListener(() -> {
				for (CommandModel command : commands) {
					_scope.addCommand(command);
				}
			});
			form.addDetachListener(() -> removeCommands(commands));
			// Disposal while not displayed: the detach listener did not run, or ran already.
			form.addCleanupAction(() -> removeCommands(commands));
		}

		private void removeCommands(List<CommandModel> commands) {
			for (CommandModel command : commands) {
				_scope.removeCommand(command);
			}
		}

		private void disposeForm() {
			if (_form != null) {
				// Runs the cleanup actions, which is what takes the commands out of the toolbar.
				((ReactControl) _form).cleanupTree();
				_form = null;
			}
		}

		private String noModelMessage() {
			ResKey key = _config.getNoModelMessage();
			return Resources.getInstance()
				.getString(key == null ? com.top_logic.layout.view.I18NConstants.CONFIG_FORM_NO_MODEL : key);
		}
	}

}
