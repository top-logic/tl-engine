/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import java.io.IOException;
import java.nio.file.Path;

import com.top_logic.build.maven.war.WarContext;

/**
 * {@link AbstractFragmentOverlay} copying the contents of a deploy folder.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeployOverlay extends AbstractWarTask {

	private Path _path;

	/**
	 * Creates a {@link DeployOverlay}.
	 */
	public DeployOverlay(Path path) {
		_path = path;
	}

	@Override
	protected void doRun(WarContext context) throws IOException {
		getLog().info("Copying deploy folder: " + _path);

		copyToTarget(_path);
	}

}
