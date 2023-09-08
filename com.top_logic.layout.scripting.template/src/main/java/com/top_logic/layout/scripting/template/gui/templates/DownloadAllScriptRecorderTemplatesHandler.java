/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.util.Collection;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Handler to download all ScriptRecorder templates.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DownloadAllScriptRecorderTemplatesHandler extends DownloadScriptRecorderTemplateFoldersHandler {

	/**
	 * Creates a {@link DownloadAllScriptRecorderTemplatesHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DownloadAllScriptRecorderTemplatesHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Collection<String> getToZippedTemplateFolderPaths(TemplateTreeComponent component) {
		return FileManager.getInstance().getResourcePaths(ScriptTemplateUtil.getTemplateRootPath(component));
	}

	@Override
	protected String getZipName(LayoutComponent component) {
		return "all-" + SCRIPT_TEMPLATE_ZIP_PREFIX_FILENAME + "s";
	}
}
