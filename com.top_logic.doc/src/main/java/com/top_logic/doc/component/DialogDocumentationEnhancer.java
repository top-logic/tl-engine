/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.doc.command.OpenDocumentationCommand;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.help.HelpFinder;
import com.top_logic.layout.structure.DialogEnhancer;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.layout.window.WindowManager;
import com.top_logic.mig.html.layout.DialogComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link DialogEnhancer} adding a button that opens the documentation for the dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DialogDocumentationEnhancer extends AbstractConfiguredInstance<DialogDocumentationEnhancer.Config>
		implements DialogEnhancer {

	/**
	 * Typed configuration interface definition for {@link DialogDocumentationEnhancer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<DialogDocumentationEnhancer> {

		/**
		 * Name of the window template displaying the documentation.
		 */
		@Mandatory
		String getTemplate();
	}

	/**
	 * Create a {@link DialogDocumentationEnhancer}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext} to create the new object in.
	 * @param config
	 *        The configuration object to be used for instantiation.
	 */
	public DialogDocumentationEnhancer(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void enhanceDialog(DialogComponent dialog) {
		addHelpButton(dialog);
	}

	private void addHelpButton(DialogComponent dialog) {
		LayoutComponent contentComponent = dialog.getContentComponent();

		String dialogHelpID = dialog.getHelpID();
		String helpID;
		if (dialogHelpID != null) {
			helpID = dialogHelpID;
		} else {
			String innerHelpID = HelpFinder.findHelpId(contentComponent);
			if (innerHelpID != null) {
				helpID = innerHelpID;
			} else {
				// No help for dialog.
				return;
			}
		}

		CommandModel openingModel = CommandModelFactory.commandModel(context -> {

			WindowComponent documentationWindow = WindowManager.openGlobalWindow(context,
				contentComponent, getConfig().getTemplate());

			OpenDocumentationCommand.showDocumentation(documentationWindow, helpID);
			return HandlerResult.DEFAULT_RESULT;

		});
		openingModel.setImage(com.top_logic.doc.command.Icons.OPEN_DOCUMENTATION);
		openingModel.setLabel(Resources.getInstance().getString(I18NConstants.OPEN_DOCUMENTATION));

		dialog.getToolbar().defineGroup(CommandHandlerFactory.HELP_GROUP).addButton(openingModel);
	}

}

