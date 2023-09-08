/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.selector;

import java.io.File;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.template.gui.I18NConstants;
import com.top_logic.layout.scripting.template.gui.ScriptContainer;
import com.top_logic.layout.scripting.template.gui.ScriptingGuiUtil;
import com.top_logic.util.Resources;

/**
 * A {@link ChannelListener} that tries to parse the selected {@link File} as an
 * {@link ApplicationAction} and displays it in the scripting gui.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
final class AcceptSelectedScriptListener implements ChannelListener {

	/** The {@link AcceptSelectedScriptListener} instance. */
	public static final AcceptSelectedScriptListener INSTANCE = new AcceptSelectedScriptListener();

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldSelection, Object newSelection) {
		if (newSelection == null) {
			return;
		}
		File selectedFile = (File) newSelection;
		if (selectedFile.isDirectory()) {
			return;
		}
		ScriptSelectorComponent selectorComponent = (ScriptSelectorComponent) sender.getComponent();
		ScriptContainer scriptContainer = createScriptContainer(selectorComponent, selectedFile);
		if (scriptContainer == null) {
			return;
		}
		selectorComponent.getDialogParent().setModel(scriptContainer);
		selectorComponent.closeDialog();
	}

	private ScriptContainer createScriptContainer(ScriptSelectorComponent selectorComponent, File selectedFile) {
		try {
			return ScriptingGuiUtil.toScriptContainer(selectedFile);
		} catch (Exception ex) {
			String title = translate(I18NConstants.FAILED_TO_PARSE_SCRIPTED_TEST_TITLE);
			String message = translate(
				I18NConstants.FAILED_TO_PARSE_SCRIPTED_TEST__FILE_ERROR.fill(selectedFile.getName(), ex.getMessage()));
			CommandModel okButton = MessageBox.button(ButtonType.OK);
			MessageBox.confirm(selectorComponent.getWindowScope(), MessageType.ERROR, title, message, okButton);
			selectorComponent.getTableSelectionModel().clear();
			return null;
		}
	}

	private String translate(ResKey i18nKey) {
		return Resources.getInstance().getString(i18nKey);
	}

}
