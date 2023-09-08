/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The class {@link DialogOpenerCommandModelConfiguration} is the implementation of
 * {@link CommandModelConfiguration} to open dialogs. It is necessary to specify the dialog by given
 * the dialog name (see {@link Config#getDialog()}).
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogOpenerCommandModelConfiguration<C extends DialogOpenerCommandModelConfiguration.Config<?>>
		extends AbstractCommandModelConfiguration<C> {

	/**
	 * Configuration options for {@link DialogOpenerCommandModelConfiguration}.
	 */
	public interface Config<I extends DialogOpenerCommandModelConfiguration<?>>
			extends AbstractCommandModelConfiguration.Config<I> {
		/** @see #getDialog() */
		String DIALOG_NAME = "dialog";

		/**
		 * The name of the dialog open handler this {@link CommandModelConfiguration} configures.
		 * The corresponding dialog must be registered at the component.
		 */
		@Name(DIALOG_NAME)
		@Mandatory
		String getDialog();

		/** @see #getDialogParent() */
		String DIALOG_PARENT_NAME = "dialogParent";

		/**
		 * The name the component where the dialog with configured {@link #getDialog() dialog
		 * opener} is registered.
		 */
		@Name(DIALOG_PARENT_NAME)
		ComponentName getDialogParent();

	}

	/**
	 * Creates a {@link DialogOpenerCommandModelConfiguration} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DialogOpenerCommandModelConfiguration(InstantiationContext context, C config) {
		super(context, config);
	}

	/**
	 * The given {@link LayoutComponent} must not be <code>null</code>. If there is no name for a
	 * component which can be used to execute the configured command, then the given
	 * {@link LayoutComponent} is also the target component.
	 */
	@Override
	protected CommandModel internalCreateCommandModel(LayoutComponent aLayoutComponent) {
		ComponentName dialogParent = getConfig().getDialogParent();
		if (dialogParent != null) {
			aLayoutComponent = aLayoutComponent.getComponentByName(dialogParent);
			if (aLayoutComponent == null) {
				throw new IllegalArgumentException("No component with name '" + dialogParent + "' found.");
			}
		}
		String dialogName = getConfig().getDialog();
		CommandHandler  handler = aLayoutComponent.getCommandById(dialogName);
		if (handler == null) {
			// Note: Looks strange, but throwing the exception directly may shaddow the underlying
			// problem: If the referenced dialog has configuration problems, these problems are
			// logged and the dialog is not registered. If referencing this dialog now breaks with
			// an exception, the original logs that were produced to the instantiation context are
			// never output.
			String message =
				"No dialog open command with name '" + dialogName + "' found at '" + aLayoutComponent.getName()
					+ "', available commands are: "
					+ aLayoutComponent.getCommands().stream().map(c -> c.getID()).collect(Collectors.joining(", "));
			Logger.error(
				message,
				DialogOpenerCommandModelConfiguration.class);
			return CommandModelFactory.commandModel(new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					throw new IllegalStateException(message);
				}
			});
		}
		return CommandModelFactory.commandModel(handler, aLayoutComponent);
	}

}
