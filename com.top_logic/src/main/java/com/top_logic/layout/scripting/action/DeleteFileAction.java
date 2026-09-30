/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.layout.scripting.action;

import java.io.File;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ApplicationAction} to delete a file.
 * 
 * <p>
 * This {@link ApplicationAction} is designed to cleanup the system after tests. It deletes files
 * that are created during the test by the application.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DeleteFileAction extends ApplicationAction {

	/**
	 * Name of the file to delete. It is resolved using the {@link FileManager}.
	 */
	String getFileName();

	/**
	 * Whether the file is deleted when the system exists.
	 * 
	 * @see File#deleteOnExit()
	 */
	boolean isDeleteOnExit();

	/**
	 * Whether the application action fails, when deletion of the file is not successful.
	 * 
	 * @see File#delete()
	 */
	@BooleanDefault(true)
	boolean isFailOnError();

	/**
	 * Whether the application action fails, when {@link #getFileName()} could not be resolved to an
	 * IDE file.
	 */
	@BooleanDefault(true)
	boolean isFailIfNotExists();

	/**
	 * Default implementation of {@link ControlAction}.
	 */
	@Label("Delete file '{file-name}'.")
	class Op extends AbstractApplicationActionOp<DeleteFileAction> {

		/**
		 * Constructor creates a new {@link Op}.
		 */
		public Op(InstantiationContext context, DeleteFileAction config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			String fileName = config.getFileName();
			
			File ideFile = FileManager.getInstance().getIDEFileOrNull(fileName);
			if (ideFile == null) {
				if (config.isFailIfNotExists()) {
					throw ApplicationAssertions.fail(config, "File '" + fileName + "' does not exist.");
				}
				return argument;
			}
			if (config.isDeleteOnExit()) {
				ideFile.deleteOnExit();
			} else {
				boolean success = ideFile.delete();
				if (!success && config.isFailOnError()) {
					throw ApplicationAssertions.fail(config, "Deleting file '" + ideFile + "' failed.");
				}
			}
			return argument;
		}

	}

}
