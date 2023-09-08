/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import java.util.Map;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.FunctionalSuccess;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Parses the uploaded file and sets it in the {@link ScriptRecorderTree}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptUploadCommand extends PreconditionCommandHandler {

	/** {@link ConfigurationItem} for the {@link ScriptUploadCommand}. */
	public interface Config extends PreconditionCommandHandler.Config {

		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.SYSTEM_NAME)
		CommandGroupReference getGroup();

		@Override
		@FormattedDefault(CommandHandlerFactory.SAVE_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault("theme:ICONS_IMPORT")
		ThemeImage getImage();

	}

	/** {@link TypedConfiguration} constructor for {@link ScriptUploadCommand}. */
	public ScriptUploadCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		ScriptUploadComponent uploadComponent = (ScriptUploadComponent) component;
		DataField field = uploadComponent.getFileUploadField();
		BinaryData binaryData = (BinaryData) CollectionUtil.getSingleValueFrom(field.getValue());
		if (binaryData == null) {
			return new Failure(I18NConstants.UPLOAD_DIALOG_NO_FILE_UPLOADED);
		}
		ScriptContainer scriptContainer;
		try {
			scriptContainer = ScriptingGuiUtil.toScriptContainer(binaryData);
		} catch (RuntimeException exception) {
			return new Failure(I18NConstants.UPLOAD_DIALOG_SCRIPT_NOT_VALID__ERROR.fill(exception.getMessage()));
		}
		return FunctionalSuccess.toSuccess(ignored -> acceptScriptAndCloseDialog(component, scriptContainer));
	}

	private void acceptScriptAndCloseDialog(LayoutComponent component, ScriptContainer scriptContainer) {
		setScriptIntoScriptRecorderTree(component, scriptContainer);
		component.closeDialog();
	}

	private boolean setScriptIntoScriptRecorderTree(LayoutComponent component, ScriptContainer scriptContainer) {
		return component.getDialogParent().setModel(scriptContainer);
	}

}
