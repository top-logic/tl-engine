/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import java.nio.file.Path;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.tooling.Workspace;
import com.top_logic.doc.export.TLDocExportImportConstants;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} exporting the documentation to the workspace.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExportDocumentationIntoWorkspaceCommand extends AbstractExportDocumentationCommand {

	/**
	 * Creates a {@link ExportDocumentationIntoWorkspaceCommand}.
	 */
	public ExportDocumentationIntoWorkspaceCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		Path ws = Workspace.topLevelProjectDirectory().toPath().getParent();
		Path exportPath = Workspace.topLevelWebapp().toPath().resolve(TLDocExportImportConstants.ROOT_PATH);
		export(exportPath, (Page) model, new ExportArgs().setWorkspace(ws));
		return HandlerResult.DEFAULT_RESULT;
	}

}
