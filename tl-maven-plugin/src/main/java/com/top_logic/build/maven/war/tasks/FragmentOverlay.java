/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.apache.maven.artifact.Artifact;

import com.top_logic.build.maven.war.WarContext;

/**
 * {@link WarTask} to copy the <code>web-fragment</code> artifact of a dependency to the
 * {@link #getWebappDirectory()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FragmentOverlay extends AbstractWarTask {

	private Artifact _fragment;

	/** 
	 * Creates a {@link FragmentOverlay}.
	 */
	public FragmentOverlay(Artifact fragment) {
		_fragment = fragment;
	}

	@Override
	protected void doRun(WarContext context) throws IOException {
		getLog().info("Expanding web fragment: " + _fragment);
		
		FileSystem zip = FileSystems.newFileSystem(_fragment.getFile().toPath());
		for (Path root : zip.getRootDirectories()) {
			copyToTarget(root);
		}
	}
	
}
