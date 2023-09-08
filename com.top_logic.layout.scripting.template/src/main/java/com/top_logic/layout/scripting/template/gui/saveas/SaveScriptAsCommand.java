/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.saveas;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.google.common.io.Files;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ActionWriter;
import com.top_logic.layout.scripting.template.gui.ScriptContainer;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Saves an {@link ApplicationAction} into a {@link File}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveScriptAsCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link SaveScriptAsCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@StringDefault(CommandHandlerFactory.CREATE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault("theme:ICONS_BUTTON_OK")
		ThemeImage getImage();

		@Override
		@FormattedDefault(TARGET_SELECTION_SELF)
		ModelSpec getTarget();

	}

	/** {@link TypedConfiguration} constructor for {@link SaveScriptAsCommand}. */
	public SaveScriptAsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		HandlerResult result = checkForm(component);
		if (result != null) {
			return result;
		}
		File file = (File) model;
		if (file.exists()) {
			LayoutData layout = DefaultLayoutData.newLayoutData(
				DisplayDimension.px(400),
				DisplayDimension.px(150));
			CommandModel ok = MessageBox.button(ButtonType.OK, approveContext -> saveAndCloseDialog(component, file));
			CommandModel cancelButton = MessageBox.button(ButtonType.CANCEL);
			return MessageBox
				.newBuilder(MessageType.CONFIRM)
				.layout(layout)
				.message(I18NConstants.CONFIRM_OVERWRITE_FILE__NAME.fill(file.getName()))
				.buttons(ok, cancelButton)
				.confirm(context.getWindowScope());
		}
		return saveAndCloseDialog(component, file);
	}

	private HandlerResult checkForm(LayoutComponent component) {
		if (component instanceof FormHandler) {
			FormHandler formHandler = (FormHandler) component;
			if (!formHandler.getFormContext().checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(formHandler.getFormContext());
			}
		}
		return null;
	}

	private HandlerResult saveAndCloseDialog(LayoutComponent component, File file) {
		saveScript(component, file);
		component.closeDialog();
		return HandlerResult.DEFAULT_RESULT;
	}

	private void saveScript(LayoutComponent component, File file) {
		/* It would be cleaner if the action would be supplied as part of the target model. But as
		 * the source or the model (the ScriptRecorderTree) has to be known anyway to set a
		 * PersistableScriptContainer, it is better to keep it symmetrical and retrieve the
		 * ScriptContainer the same way as the new ScriptContainer will be set. */
		ScriptContainer scriptContainer = (ScriptContainer) getScriptRecorderTree(component).getModel();
		ApplicationAction action = scriptContainer.getAction();
		writeActionToFile(file, action);
		/* Replacing the ScriptContainer enables the "save" button and makes sure it saves to the correct file. */
		ScriptContainer newScriptContainer = ScriptContainer.createPersistable(scriptContainer.getAction(), file);
		getScriptRecorderTree(component).setModel(newScriptContainer);
	}

	private LayoutComponent getScriptRecorderTree(LayoutComponent component) {
		return component.getDialogParent();
	}

	private void writeActionToFile(File file, ApplicationAction action) {
		String actionXml = ActionWriter.INSTANCE.writeAction(action, true, true);
		writeToFile(file, actionXml);
	}

	private void writeToFile(File file, String text) {
		try {
			Files.write(text, file, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

}
