/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.configedit.ConfigFormControl;
import com.top_logic.layout.configedit.FieldDisplay;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
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
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ConfigFormScope;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewActions;
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
 * A {@link Config#getSaveActions() save action} makes Apply a Save: the action chain runs over the
 * checked working copy, and only once it ran through is the copy carried over and edit mode left.
 * While the form holds unsaved changes, it blocks the write of another item to its input channel
 * and reports the changes to the enclosing dirty-tracked scope, as a {@code <form>} does, see
 * {@link ConfigFormStateHandler}.
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
	public interface Config extends UIElement.Config, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Name of the argument holding the working copy in the {@link #getSaveActions() save} and
		 * {@link #getCancelActions() cancel} actions of a form without {@link #getInput() input}.
		 */
		String EDITED_ARGUMENT = "config";

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getType()}. */
		String TYPE = "type";

		/** Configuration name for {@link #getWithEditMode()}. */
		String WITH_EDIT_MODE = "withEditMode";

		/** Configuration name for {@link #getNoModelMessage()}. */
		String NO_MODEL_MESSAGE = "noModelMessage";

		/** Configuration name for {@link #getFields()}. */
		String FIELDS = "fields";

		/** Configuration name for {@link #getSaveActions()}. */
		String SAVE_ACTIONS = FormElement.Config.SAVE_ACTIONS;

		/** Configuration name for {@link #getCancelActions()}. */
		String CANCEL_ACTIONS = FormElement.Config.CANCEL_ACTIONS;

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

		/**
		 * How properties of the edited item are displayed in this form, beyond what the properties
		 * declare themselves.
		 *
		 * <p>
		 * For instance a value the context of the form fixes, e.g. the name of the model element
		 * whose access rights a dialog edits: it is shown, so that the user sees what is edited, but
		 * cannot be changed there.
		 * </p>
		 */
		@Name(FIELDS)
		@EntryTag(FieldConfig.TAG)
		List<FieldConfig> getFields();

		/**
		 * Actions saving the edited configuration, run by Save.
		 *
		 * <p>
		 * Written like the actions of a {@code <generic-command>}. The chain runs over the working
		 * copy - the checked, edited configuration - as its argument, named after the
		 * {@link #getInput() input} channel, or {@value #EDITED_ARGUMENT} for a form without one.
		 * Once the chain ran through, the copy is carried over into the edited item and the form
		 * leaves edit mode. A chain that fails or is aborted leaves the form in edit mode with the
		 * edits intact; its error is reported like that of any command. Save is not offered while
		 * a field of the form shows an error.
		 * </p>
		 *
		 * <p>
		 * Without save actions, the command is called Apply and carries the copy over right away.
		 * Only with {@link #getWithEditMode() edit mode}.
		 * </p>
		 */
		@Name(SAVE_ACTIONS)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getSaveActions();

		/**
		 * Actions run by Cancel before the working copy is dropped.
		 *
		 * <p>
		 * The chain runs with the same argument as the {@link #getSaveActions() save actions}. Once
		 * it ran through, the copy is dropped and the form leaves edit mode; a chain that fails or
		 * is aborted leaves the form in edit mode.
		 * </p>
		 */
		@Name(CANCEL_ACTIONS)
		@TreeProperty
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<? extends ViewAction>> getCancelActions();

		/**
		 * The {@link #getSaveActions() save} and {@link #getCancelActions() cancel} actions run over
		 * the working copy, see {@link ConfigFormElement#editedScope(Config)}.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (ActionScopes.contains(getSaveActions(), part) || ActionScopes.contains(getCancelActions(), part)) {
				return editedScope(this);
			}
			return outer;
		}
	}

	/**
	 * The scope the {@link Config#getSaveActions() save} and {@link Config#getCancelActions() cancel}
	 * actions of the given form run in: the working copy, named after the {@link Config#getInput()
	 * input} channel, or {@link Config#EDITED_ARGUMENT} without one.
	 */
	public static ActionScope editedScope(Config config) {
		ChannelRef input = config.getInput();
		return ActionScope.single(input == null ? Config.EDITED_ARGUMENT : input.getChannelName());
	}

	/**
	 * How one property of the edited item is displayed, see {@link FieldDisplay}.
	 */
	public interface FieldConfig extends ConfigurationItem {

		/** Tag name of an entry of {@link Config#getFields()}. */
		String TAG = "field";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getMode()}. */
		String MODE = "mode";

		/** Configuration name for {@link #getMandatory()}. */
		String MANDATORY = "mandatory";

		/**
		 * The name of the property of the edited item.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The mode the property is displayed in: <code>active</code> accepts input,
		 * <code>disabled</code> shows an input that cannot be used, <code>immutable</code> shows the
		 * value only, <code>invisible</code> hides the property. Without a mode, the property is
		 * displayed as it declares itself.
		 *
		 * @see FieldDisplay#mode()
		 */
		@Name(MODE)
		@Nullable
		FieldMode getMode();

		/**
		 * Whether the property must have a value, even if it does not declare it. The form refuses
		 * to be saved without it.
		 *
		 * @see FieldDisplay#mandatory()
		 */
		@Name(MANDATORY)
		boolean getMandatory();
	}

	private final Config _config;

	private final List<ViewAction> _saveActions;

	private final List<ViewAction> _cancelActions;

	/**
	 * The {@link Config#getFields() configured} displays, by property name.
	 */
	private Map<String, FieldDisplay> fieldDisplays() {
		Map<String, FieldDisplay> result = new HashMap<>();
		for (FieldConfig field : _config.getFields()) {
			result.put(field.getName(), new FieldDisplay(field.getMode(), field.getMandatory()));
		}
		return result;
	}

	/**
	 * Creates a new {@link ConfigFormElement} from configuration.
	 */
	@CalledByReflection
	public ConfigFormElement(InstantiationContext context, Config config) {
		_config = config;
		ActionScope scope = editedScope(config);
		_saveActions = ViewActions.instantiate(context, config.getSaveActions(), scope);
		_cancelActions = ViewActions.instantiate(context, config.getCancelActions(), scope);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ViewChannel input = _config.getInput() == null ? null : context.resolveChannel(_config.getInput());
		CommandScope scope = context.getScope(CommandScope.class);

		ReactStackControl holder = new ReactStackControl(context, Collections.emptyList());
		holder.setCssClass(_config.getCssClass());
		Display display = new Display(context, input, holder, scope);
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

		private final ViewChannel _input;

		private final ReactStackControl _holder;

		private final CommandScope _scope;

		private ConfigFormControl _form;

		Display(ViewContext context, ViewChannel input, ReactStackControl holder, CommandScope scope) {
			_context = context;
			_input = input;
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
			_form = new ConfigFormControl(_context, item, commands,
				chain(_saveActions), chain(_cancelActions));
			if (!_config.getFields().isEmpty()) {
				_form.setFieldDisplays(fieldDisplays());
			}
			contribute(_form);
			register(_form);
			new ConfigFormStateHandler(_form, _input, _context.getDirtyChannel());
			_holder.setChildren(List.of(_form));
		}

		/**
		 * Makes the form known to the commands of the enclosing element that save what it edits,
		 * see {@link ConfigFormScope}, for as long as it exists.
		 */
		private void register(ConfigFormControl form) {
			ConfigFormScope forms = _context.getScope(ConfigFormScope.class);
			if (forms == null) {
				return;
			}
			forms.register(form);
			form.addCleanupAction(() -> forms.unregister(form));
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

		/**
		 * The given actions as what the form runs at the end of an edit session, <code>null</code>
		 * for none.
		 *
		 * <p>
		 * The chain runs in the context of this element over the working copy; the session ends by
		 * an action appended to the chain, so it ends exactly when the chain ran through - also
		 * when it resumes after a dialog, and not when it was aborted or failed.
		 * </p>
		 */
		private ConfigFormControl.EditAction chain(List<ViewAction> actions) {
			if (actions.isEmpty()) {
				return null;
			}
			ActionScope scope = editedScope(_config);
			return (edited, done) -> {
				List<ViewAction> chain = new ArrayList<>(actions);
				chain.add((context, input) -> {
					done.run();
					return input;
				});
				ViewActionChain.run(_context, chain, scope.arguments(edited), null);
			};
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
