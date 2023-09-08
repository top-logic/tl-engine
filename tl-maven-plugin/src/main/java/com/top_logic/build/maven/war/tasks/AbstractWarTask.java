/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.maven.plugin.logging.Log;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.build.maven.war.MojoRuntimeException;
import com.top_logic.build.maven.war.TLAppWar;
import com.top_logic.build.maven.war.WarContext;

/**
 * Base class for {@link WarTask} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractWarTask implements WarTask {

	private Log _log;

	private Path _webappDirectory;

	/**
	 * The build log.
	 */
	public Log getLog() {
		return _log;
	}
	
	/**
	 * See {@link TLAppWar#getWebappDirectory()}.
	 */
	public Path getWebappDirectory() {
		return _webappDirectory;
	}
	
	@Override
	public final void run(WarContext context) {
		_log = context.getLog();
		_webappDirectory = context.getWebappDirectory();
		try {
			doRun(context);
		} catch (IOException ex) {
			throw new MojoRuntimeException("Failed to build webapp.", ex);
		}
	}

	/**
	 * Internally runs the task.
	 * 
	 * @see #run(WarContext)
	 */
	protected abstract void doRun(WarContext context) throws IOException;

	/**
	 * Utility to copy an arbitrary directory tree rooted at the given {@link Path} to the
	 * {@link #getWebappDirectory()}.
	 */
	protected final void copyToTarget(Path root) throws IOException {
		Files.walk(root).filter(p -> accept(root, p)).forEach(p -> copy(root, p));
	}

	/**
	 * Whether the given {@link Path} is accepted while copying.
	 * 
	 * @param root
	 *        The root of the copy operation.
	 * @param path
	 *        The path to copy to the web application.
	 * 
	 * @see #copyToTarget(Path)
	 */
	protected boolean accept(Path root, Path path) {
		if (Files.isDirectory(path)) {
			return false;
		}

		if (!acceptFileName(path.getFileName().toString())) {
			return false;
		}
		
		return acceptPathName(root.relativize(path).toString());
	}

	/**
	 * Whether the full path name is accepted while copying.
	 * 
	 * @param pathName
	 *        The name of the path relative to {@link #getWebappDirectory()}.
	 * 
	 * @see #copyToTarget(Path)
	 */
	protected boolean acceptPathName(String pathName) {
		if (pathName.equals("META-INF/MANIFEST.MF")) {
			return false;
		}
		if (pathName.equals(ModuleLayoutConstants.META_CONF_PATH)) {
			return false;
		}

		// Exclude IDE-only configurations from deployment.
		if (pathName.startsWith(ModuleLayoutConstants.CONF_PATH + "/devel-")) {
			return false;
		}

		return true;
	}

	/**
	 * Whether the file name (last component) of a path is accepted while copying.
	 * 
	 * @see #copyToTarget(Path)
	 */
	protected boolean acceptFileName(String fileName) {
		if (fileName.startsWith(".")) {
			return false;
		}
		
		if (fileName.endsWith(".layout.xml")) {
			return false;
		}

		if (fileName.endsWith(".layout.overlay.xml")) {
			return false;
		}

		if (fileName.equals("metaConf.txt")) {
			return false;
		}

		return true;
	}

	private void copy(Path root, Path path) {
		Path relative = root.relativize(path);
		Path target = _webappDirectory.resolve(relative.toString());
		
		try {
			Files.createDirectories(target.getParent());
			Files.copy(path, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			throw new MojoRuntimeException("Cannot copy '" + path + "' to target '" + target + "'.", ex);
		}
	}

}
