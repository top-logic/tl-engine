/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command;

import static com.top_logic.doc.export.TLDocExportImportConstants.*;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.doc.model.Page;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Abstract {@link CommandHandler} for import and export of documentation between the workspace and a running application.
 * 
 * @author     <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public abstract class AbstractImportExportDocumentationCommand extends AbstractCommandHandler {

	/** {@link ConfigurationItem} for the {@link AbstractImportExportDocumentationCommand}. */
	public interface Config extends AbstractCommandHandler.Config {

		// nothing special here

	}

	/** 
	 * {@link TypedConfiguration} constructor for {@link AbstractImportExportDocumentationCommand}.
	 */
	public AbstractImportExportDocumentationCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Complete {@link Path} of a {@link Page} in the workspace.
	 * 
	 * @param parentPath
	 *        {@link Path} of the {@link Page} parent.
	 * @param childPage
	 *        {@link Page} to get the {@link Path} of.
	 * @return {@link Path} of {@link Page}.
	 */
	protected Path getPath(Path parentPath, Page childPage) {
		String id = childPage.getName();
		return parentPath.resolve(toFileName(id));
	}

	/**
	 * Convert an ID to a valid file name.
	 * 
	 * @param id
	 *        ID of the {@link Page}.
	 * @return Valid file name.
	 */
	private String toFileName(String id) {
		if (id.isEmpty()) {
			throw new IllegalArgumentException("The help id must not be empty.");
		}
		int idSize = Math.min(id.length(), FILE_NAME_LIMIT + 1);
		String limitedId = id.substring(0, idSize);
		return INVALID_FILE_NAME_CHARACTERS.matcher(limitedId).replaceAll("_");
	}
	
	/**
	 * Resolve the given {@link String} to a {@link Path}, using the {@link FileManager}.
	 */
	protected Path resolvePath(FileSystem fs, String path) {
		return fs.getPath(path);
	}

	/** Correctly typed getter for {@link #getConfig()}. */
	protected Config config() {
		return (Config) getConfig();
	}

}
