/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui.templates;

import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.export.AbstractZipDownloadHandler;
import com.top_logic.util.Zipper;

/**
 * Handler to download the given ScriptRecorder template folders.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class DownloadScriptRecorderTemplateFoldersHandler extends AbstractZipDownloadHandler {

	/**
	 * Prefix for the created zip file.
	 */
	protected static final String SCRIPT_TEMPLATE_ZIP_PREFIX_FILENAME = "script-template";

	/**
	 * Creates a {@link DownloadScriptRecorderTemplateFoldersHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DownloadScriptRecorderTemplateFoldersHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private void zipFile(Zipper zipper, String resourcePath, String name) throws FileNotFoundException, IOException {
		FileManager fileManager = FileManager.getInstance();

		if (fileManager.isDirectory(resourcePath)) {
			zipper.addFolderByResourcePath(resourcePath, name);
		} else {
			zipper.addFile(fileManager.getStream(resourcePath), name);
		}
	}


	@Override
	protected void addFilesToZip(Zipper zipper, LayoutComponent component, Map<String, Object> arguments) {
		TemplateTreeComponent templateTreeComponent = getTemplateTreeComponent(component);

		for (String resourcePathToZip : getToZippedTemplateFolderPaths(templateTreeComponent)) {
			zipFiles(zipper, resourcePathToZip, FileUtilities.getFilenameOfResource(resourcePathToZip));
		}
	}

	private void zipFiles(Zipper zipper, String resourcePath, String name) {
		try {
			zipFile(zipper, resourcePath, name);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private TemplateTreeComponent getTemplateTreeComponent(LayoutComponent component) {
		return (TemplateTreeComponent) component;
	}

	/**
	 * All relative template folder paths that should be zipped.
	 */
	protected abstract Collection<String> getToZippedTemplateFolderPaths(TemplateTreeComponent component);
}
