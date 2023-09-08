/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.tooling.Workspace;

/**
 * {@link TemplateSource} resolved by the {@link FileManager} relative to the application's template
 * path.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateResourceSource implements TemplateSource {

	private final String _resourceName;

	private final TemplateLocator _locator;

	/**
	 * Creates a {@link TemplateResourceSource}.
	 * 
	 * @param locator
	 *        The locator to resolve unqualified template references from the loaded template.
	 * @param resourceName
	 *        {@link FileManager} name of the template resource.
	 */
	public TemplateResourceSource(TemplateLocator locator, String resourceName) {
		_locator = locator;
		_resourceName = resourceName;
	}

	@Override
	public InputStream getContent() throws IOException {
		FileManager fileManager = FileManager.getInstance();
		return fileManager.getStream(_resourceName);
	}

	@Override
	public ResourceTransaction update() throws IOException {
		return new FilesystemTransaction(file());
	}

	@Override
	public void delete() throws IOException {
		File file = file();
		if (!file.exists()) {
			return;
		}

		boolean success = file.delete();
		if (!success) {
			throw new IOException("Deletion of the underlying file failed.");
		}
	}

	private File file() {
		File ideFile = FileManager.getInstance().getIDEFileOrNull(_resourceName);

		if (ideFile == null) {
			ideFile = new File(Workspace.topLevelWebapp(), _resourceName);
		}

		return ideFile;
	}

	@Override
	public String toString() {
		return "resource:" + _resourceName;
	}

	@Override
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		return _locator.resolve(context, templateReference);
	}

}
