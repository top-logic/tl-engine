/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import java.util.Map;

import com.top_logic.basic.NoProtocol;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.doc.export.DocumentationImporter;
import com.top_logic.doc.export.TLDocExportImportConstants;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * A {@link CommandHandler} that imports the documentation from the workspace.
 * 
 * @author <a href="mailto:dpa@top-logic.com">Diana Pankratz</a>
 */
public class ImportDocumentationFromWorkspaceCommand extends AbstractImportExportDocumentationCommand {

	/**
	 * Configuration of an {@link ImportDocumentationFromWorkspaceCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractImportExportDocumentationCommand.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * {@link TypedConfiguration} constructor for {@link ImportDocumentationFromWorkspaceCommand}.
	 */
	public ImportDocumentationFromWorkspaceCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {

		DocumentationImporter importer = new DocumentationImporter();
		importer.setMissingDocumentationHandler(locale -> InfoService
			.showInfo(I18NConstants.NO_IMPORT_FILES, I18NConstants.NO_IMPORT_FILES__LANGUAGE__PATH
				.fill(locale.getDisplayLanguage(ThreadContext.getLocale()),
					TLDocExportImportConstants.ROOT_RELATIVE_PATH)));

		KBUtils.inTransaction(PersistencyLayer.getKnowledgeBase(), () -> {
			importer.doImport(NoProtocol.INSTANCE);
		});

		return HandlerResult.DEFAULT_RESULT;
	}

}
