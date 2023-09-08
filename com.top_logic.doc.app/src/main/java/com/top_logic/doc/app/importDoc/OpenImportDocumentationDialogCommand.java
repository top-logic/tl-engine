/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.app.importDoc;

import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.doc.app.I18NConstants;
import com.top_logic.doc.component.WithDocumentationLanguage;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * {@link CommandHandler} opening an import dialog.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class OpenImportDocumentationDialogCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link OpenImportDocumentationDialogCommand}. */
	public interface Config extends AbstractCommandHandler.Config, WithDocumentationLanguage {
		// Pure sum interface.
	}

	/** {@link TypedConfiguration} constructor for {@link OpenImportDocumentationDialogCommand}. */
	public OpenImportDocumentationDialogCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		if (!(model instanceof Page)) {
			return HandlerResult.error(com.top_logic.doc.command.I18NConstants.SELECTION_IS_NO_PAGE);
		}
		Page page = (Page) model;
		Locale language = ((Config) getConfig()).resolveLanguage(aComponent);
		AbstractFormDialogBase importDialog =
			new CreateConfigurationDialog<>(ImportSettings.class,
				settings -> new ImportCommand(page, language, settings).processImport(),
				I18NConstants.IMPORT_DIALOG.key("title"),
				DisplayDimension.px(400),
				DisplayDimension.px(200));
		return importDialog.open(aContext);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NullModelDisabled.INSTANCE);
	}
}
