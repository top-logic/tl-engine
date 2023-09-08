/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandHandlerCommand;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.check.CanApply;
import com.top_logic.layout.form.component.EditComponent.CancelCommand;
import com.top_logic.layout.form.component.EditComponent.DiscardCommand;
import com.top_logic.layout.form.component.edit.EditMode;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link LayoutComponent} plug-in interface for supporting edit-mode switches.
 * 
 * <p>
 * Note: When implementing this interface in a component, the component configuration must extends
 * {@link Editor.Config} and must call {@link Config#modifyIntrinsicCommands(CommandRegistry)} from
 * an overridden version of that method.
 * </p>
 * 
 * @see EditMode
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Editor extends EditMode, CanApply {
	
	/**
	 * Configuration options for {@link Editor}.
	 */
	public interface Config extends EditMode.Config {

		/** @see #getApplyCommand() */
		String APPLY_COMMAND = "applyCommand";

		/** @see #getSaveCommand() */
		String SAVE_COMMAND = "saveCommand";

		/** @see #getEditCommand() */
		String EDIT_COMMAND = "editCommand";

		/** @see #getCancelCommand() */
		String CANCEL_COMMAND = "cancelCommand";

		/** @see #getDiscardCommand() */
		String DISCARD_COMMAND = "discardCommand";

		/** @see #getShowDiscardButton() */
		String SHOW_DISCARD_BUTTON_ATTRIBUTE = "showDiscardButton";

		/** @see #getSaveClosesDialog() */
		String SAVE_CLOSES_DIALOG_ATTRIBUTE = "saveClosesDialog";

		/**
		 * ID of the edit {@link CommandHandler} switching the component into edit mode.
		 */
		@Name(EDIT_COMMAND)
		@StringDefault(SwitchEditCommandHandler.COMMAND_ID)
		@Nullable
		String getEditCommand();

		/**
		 * ID of the apply {@link CommandHandler}.
		 * 
		 * <p>
		 * The apply command makes current changes persistent, but does not switch back to view
		 * mode.
		 * </p>
		 * 
		 * @see #getSaveCommand()
		 */
		@Name(APPLY_COMMAND)
		@Nullable
		String getApplyCommand();

		/**
		 * ID of the save {@link CommandHandler}.
		 * 
		 * <p>
		 * The save command makes current changes persistent and switches back to view mode.
		 * </p>
		 * 
		 * <p>
		 * The default is to <code>null</code>, which means that the generic save handler is
		 * registered, if there is an apply handler, see {@link #getApplyCommand()}.
		 * </p>
		 */
		@Name(SAVE_COMMAND)
		@Nullable
		String getSaveCommand();

		/**
		 * The identifier of the save command handler to use.
		 * 
		 * <p>
		 * This method takes into account that a default save command handler is used, if there is
		 * an {@link #getApplyCommand()} and no explicit {@link #getSaveCommand()} is given.
		 * </p>
		 */
		@Derived(fun = SaveCommandEffective.class, args = { @Ref(SAVE_COMMAND), @Ref(APPLY_COMMAND) })
		String getSaveCommandEffective();

		/**
		 * Implementation of {@link Config#getSaveCommandEffective()}
		 */
		class SaveCommandEffective extends Function2<String, String, String> {
			@Override
			public String apply(String saveCommand, String applyCommand) {
				if (!StringServices.isEmpty(saveCommand)) {
					return saveCommand;
				}

				if (StringServices.isEmpty(applyCommand)) {
					return null;
				}

				// There is no save command, but an apply command. Register a
				// wrapped apply command as default save command.
				return DefaultSaveCommandHandler.COMMAND_ID;
			}
		}

		/**
		 * ID of the cancel {@link CommandHandler}.
		 * 
		 * <p>
		 * The cancel command drops transient changes and switches back to view mode.
		 * </p>
		 * 
		 * @see #getDiscardCommand()
		 */
		@Name(CANCEL_COMMAND)
		@Nullable
		@StringDefault(CancelCommand.COMMAND_ID)
		String getCancelCommand();

		/**
		 * ID of the discard {@link CommandHandler}.
		 * 
		 * <p>
		 * The discard command drops transient changes, but keeps the edit mode.
		 * </p>
		 * 
		 * <p>
		 * The discard command is only registered as separate button, if
		 * {@link #getShowDiscardButton()} is set. Otherwise, the discard command is a globally
		 * registered command that is looked up from the {@link CommandHandlerFactory} when
		 * required, e.g. when resetting changes upon tab switch.
		 * </p>
		 * 
		 * @see #getCancelCommand()
		 */
		@Name(DISCARD_COMMAND)
		@Nullable
		@StringDefault(DiscardCommand.COMMAND_ID)
		String getDiscardCommand();

		/**
		 * Whether to show the "discard" button in addition to the "edit", "apply", "save", and
		 * "cancel" buttons for mode switch.
		 * 
		 * <p>
		 * The discard button drops user input but keeps the edit mode active.
		 * </p>
		 */
		@Name(SHOW_DISCARD_BUTTON_ATTRIBUTE)
		@BooleanDefault(false)
		boolean getShowDiscardButton();

		/**
		 * Whether the "save" command also closes the surrounding dialog (if this component is
		 * opened in a dialog).
		 */
		@Name(SAVE_CLOSES_DIALOG_ATTRIBUTE)
		@BooleanDefault(true)
		boolean getSaveClosesDialog();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditMode.Config.super.modifyIntrinsicCommands(registry);

			String applyCommand = getApplyCommand();
			String saveCommand = getSaveCommandEffective();

			if ((!StringServices.isEmpty(saveCommand)) || (!StringServices.isEmpty(applyCommand))) {
				registry.registerButton(getEditCommand());
			}

			if (saveCommand != null) {
				registry.registerButton(saveCommand);
			}

			String cancelCommand = getCancelCommand();
			if (cancelCommand != null) {
				registry.registerButton(cancelCommand);
			}

			String discardCommand = getDiscardCommand();
			if (discardCommand != null) {
				if (getShowDiscardButton()) {
					registry.registerButton(discardCommand);
				} else {
					/* Do not show but register command to be able to return in
					 * #getDiscardClosure() */
					registry.registerCommand(discardCommand);
				}
			}

			if (!StringServices.isEmpty(applyCommand)) {
				registry.registerButton(applyCommand);
			}
		}

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();
	}

	/**
	 * The apply {@link CommandHandler}, or null, if no apply is available.
	 */
	default CommandHandler getApplyCommandHandler() {
		return getCommandById(((Config) getConfig()).getApplyCommand());
	}

	/**
	 * {@link Command} invoking the {@link #getApplyCommandHandler()}, if it exists, need no
	 * confirmation and it is executable, <code>null</code> otherwise.
	 */
	@Override
	default Command getApplyClosure() {
		CommandHandler applyCommand = getApplyCommandHandler();
		if (applyCommand == null || applyCommand.needsConfirm()) {
			return null;
		}
	
		if (!applyCommand.isExecutable(self(), CommandHandler.NO_ARGS).isExecutable()) {
			return null;
		}
	
		return new CommandHandlerCommand(applyCommand, self());
	}

	/**
	 * Store changes without switching back to view mode.
	 */
	default HandlerResult apply(DisplayContext context) {
		return CommandDispatcher.getInstance().dispatchCommand(getApplyCommandHandler(), context, self());
	}

	/**
	 * The edit {@link CommandHandler}.
	 */
	default CommandHandler getEditCommandHandler() {
		return getCommandById(((Config) getConfig()).getEditCommand());
	}

	/**
	 * Switch to edit mode.
	 */
	default HandlerResult edit(DisplayContext context) {
		return CommandDispatcher.getInstance().dispatchCommand(getEditCommandHandler(), context, self());
	}

	/**
	 * Returns the cancel command.
	 */
	default CommandHandler getCancelCommandHandler() {
		return getCommandById(((Config) getConfig()).getCancelCommand());
	}

	/**
	 * The save {@link CommandHandler}, or <code>null</code> if no save is available.
	 */
	default CommandHandler getSaveCommandHandler() {
		return getCommandById(((Config) getConfig()).getSaveCommandEffective());
	}

	/**
	 * Same as {@link #apply(DisplayContext)} and leave edit mode.
	 */
	default HandlerResult save(DisplayContext context) {
		return CommandDispatcher.getInstance().dispatchCommand(getSaveCommandHandler(), context, self());
	}

	/**
	 * The discard {@link CommandHandler}, or null, if no discard is available.
	 */
	default CommandHandler getDiscardCommandHandler() {
		return getCommandById(((Config) getConfig()).getDiscardCommand());
	}

	/**
	 * {@link Command} invoking the {@link #getDiscardCommandHandler()}.
	 */
	@Override
	default Command getDiscardClosure() {
		CommandHandler discardCommand = getDiscardCommandHandler();
		if (discardCommand == null) {
			return null;
		}
		return new CommandHandlerCommand(discardCommand, self());
	}

	/**
	 * Discards changes made in edit mode without leaving the edit mode.
	 */
	default void discardChanges() {
		CommandDispatcher.getInstance().dispatchCommand(getDiscardCommandHandler(),
			DefaultDisplayContext.getDisplayContext(), self());
	}

	/**
	 * Return true, when this instance should close the dialog when clicking on the save button.
	 * 
	 * @return <code>true</code> for closing the dialog on clicking save.
	 */
	default boolean saveClosesDialog() {
		return ((Config) getConfig()).getSaveClosesDialog();
	}
}
