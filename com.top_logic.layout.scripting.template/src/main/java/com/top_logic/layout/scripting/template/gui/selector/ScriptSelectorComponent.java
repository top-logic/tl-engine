/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.selector;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.common.folder.ui.FolderComponent;
import com.top_logic.layout.folder.file.selection.FileSelectionComponent;
import com.top_logic.layout.scripting.template.gui.ScriptUploadComponent;
import com.top_logic.mig.html.SelectionModel;

/**
 * A {@link FolderComponent} for displaying the scripted tests on the server.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptSelectorComponent extends FileSelectionComponent {

	/**
	 * Configuration options for {@link ScriptSelectorComponent}.
	 */
	public interface Config extends FileSelectionComponent.Config {
		// Sum interface.
	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptUploadComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptSelectorComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);

		selectionChannel().addListener(AcceptSelectedScriptListener.INSTANCE);
	}

	@Override
	protected void becomingVisible() {
		super.becomingVisible();
		/* Reset the selections, as they are only used as triggers. When they are not reset, nothing
		 * will happen if the user selects the former selection again. But that is a common use
		 * case: The user has loaded an ExcelTest, fixes one of the templates and wants to load it
		 * again. When the selections are not reset, nothing will happen and the user is forced to
		 * select something else. */
		SelectionModel selectionModel = getTableSelectionModel();
		if (selectionModel != null) {
			selectionModel.clear();
		}
	}

}
