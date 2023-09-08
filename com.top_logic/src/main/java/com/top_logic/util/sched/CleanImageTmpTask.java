/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import java.io.File;
import java.io.IOException;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;

/** 
 * This task deletes the image tmp directory in constant intervals.
 *
 * @author    <a href=mailto:tdi@top-logic.com>Tomas Dickhut</a>
 */
@InApp
public class CleanImageTmpTask extends CleanUpTask {

	public CleanImageTmpTask(InstantiationContext context, Config config) {
		super(context, config);
		if (!StringServices.isEmpty(config.getCleanupPath())) {
			context.error("The cleanup path cannot be specified,"
				+ " as the image temp directory is cleaned up, which is configured independently of this task.");
		}
	}

	/**
	 * Use a Directory in the System tmp-directory.
	 */
	@Override
	protected File createCleanupDir(String aPath) throws IOException {
		return Settings.getInstance().getImageTempDir();
	}

	@Override
	public File getCleanUpDir() {
		/* Overriding this method and calling the getter will ensure that the directory is recreated
		 * if it has been deleted by a cron job. */
		return Settings.getInstance().getImageTempDir();
	}
 
}
