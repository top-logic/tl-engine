/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.field.FieldSpec;
import com.top_logic.layout.react.field.ReactFieldControlProvider;
import com.top_logic.layout.view.command.ViewCommand;

/**
 * {@link ReactFieldControlProvider} for {@code tl.model.wysiwyg:Html} attributes.
 *
 * <p>
 * The editor's toolbar carries the commands the configuration gives it, next to the formatting
 * buttons of the editor itself. Such a command does whatever a command of the surrounding view
 * does - open a dialog, run a script, write a channel - and reaches the text through the
 * insertion channel: what it writes there is inserted at the cursor.
 * </p>
 */
public class WysiwygControlProvider implements ReactFieldControlProvider {

	/**
	 * Configuration options for {@link WysiwygControlProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<WysiwygControlProvider> {

		/** @see #getCommands() */
		String COMMANDS = "commands";

		/** @see #getInsertChannel() */
		String INSERT_CHANNEL = "insert-channel";

		@Override
		@ClassDefault(WysiwygControlProvider.class)
		Class<? extends WysiwygControlProvider> getImplementationClass();

		/**
		 * Commands the editor's toolbar offers beyond its formatting buttons.
		 *
		 * <p>
		 * A command goes to the toolbar unless it says otherwise, as every command placed in a
		 * toolbar does. The commands see the channels of the view the edited field is displayed
		 * in, so they take their input from it and hand it on to the dialogs they open, and they
		 * reach the text through the insertion channel.
		 * </p>
		 */
		@Name(COMMANDS)
		@EntryTag("command")
		@Options(fun = AllInAppImplementations.class)
		List<ViewCommand.Config> getCommands();

		/**
		 * Name of the channel whose text the editor inserts at the cursor.
		 *
		 * <p>
		 * Declared where the editor's commands need somewhere to write their result to; the
		 * channel then exists for them alone, beside the channels of the surrounding view. A view
		 * names the channels it works with, and this is one of them.
		 * </p>
		 *
		 * <p>
		 * Left unset, the editor has no such channel and its commands act on the view only.
		 * </p>
		 */
		@Name(INSERT_CHANNEL)
		@Nullable
		String getInsertChannel();

	}

	private final List<ViewCommand> _commands = new ArrayList<>();

	private final List<ViewCommand.Config> _commandConfigs = new ArrayList<>();

	private final String _insertChannel;

	/**
	 * Creates a {@link WysiwygControlProvider} from configuration.
	 */
	@CalledByReflection
	public WysiwygControlProvider(InstantiationContext context, Config config) {
		for (ViewCommand.Config commandConfig : config.getCommands()) {
			ViewCommand command = context.getInstance(commandConfig);
			if (command != null) {
				_commands.add(command);
				_commandConfigs.add(commandConfig);
			}
		}
		_insertChannel = config.getInsertChannel();
	}

	@Override
	public ReactControl createControl(ReactContext context, FieldSpec field, FieldModel model) {
		return new ReactWysiwygControl(context, model, _commands, _commandConfigs, _insertChannel);
	}

}
