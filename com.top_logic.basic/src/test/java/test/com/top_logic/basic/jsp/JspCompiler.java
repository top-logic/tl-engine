/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import test.com.top_logic.basic.ConfigLoaderTestUtil;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.tooling.WebXmlBuilder;

/**
 * Setup for running {@link JspCompileTask}s.
 * 
 * @see JspCompiler#JspCompiler(Log, File, List)
 * @see #compile()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class JspCompiler {

	private static final String WEB_XML_PATH = "WEB-INF/web.xml";

	private static final String WEB_XML_BACKUP_PATH = "WEB-INF/web.xml.bak";

	private final File webapp;

	/**
	 * List of creates files. The files are created from 0..size, so deletion
	 * must happens from size...0
	 * 
	 * @see #removeCreatedFiles()
	 */
	private List<Path> createdFiles = new ArrayList<>();

	private List<JspCompileTask> _tasks;

	private Log _log;

	JspCompiler(Log log, File webapp, List<JspCompileTask> tasks) {
		_log = log;
		_tasks = tasks;
		this.webapp = webapp;
	}

	private void setUp() throws IOException, ParserConfigurationException, SAXException {
		List<Path> dependencies =
			Workspace.applicationModules();

		try {
			// ensure existence of web xml.Otherwise compiling fails.
			ensureWebXML();
			// copy needed files from depending modules to target webapp
			for (Path dependency : dependencies) {
				Path normalizedDependency = dependency.normalize();
				copyDependentFiles(normalizedDependency, normalizedDependency.getNameCount(), webapp.toPath());
			}
		} catch (Exception ex) {
			removeCreatedFiles();
			throw ex;
		}
	}

	private void ensureWebXML() throws IOException, ParserConfigurationException, SAXException {
		WebXmlBuilder builder = new WebXmlBuilder();

		addWebXml(builder);
		builder.addClassPathFragments();
		
		File targetWebXml = targetWebXml();
		if (targetWebXml.exists()) {
			targetWebXml.renameTo(backupWebXml());
		}

		builder.dumpTo(targetWebXml);
	}

	private void addWebXml(WebXmlBuilder builder) throws SAXException, IOException {
		Maybe<File> testFolder = ConfigLoaderTestUtil.getTestWebapp();
		if (testFolder.hasValue()) {
			File testWebapp = testFolder.get();
			File testWebXml = new File(testWebapp, WEB_XML_PATH);
			if (testWebXml.exists()) {
				builder.addFromWebApp(testWebapp);
				return;
			}
		}

		File webXml = targetWebXml();
		if (webXml.exists()) {
			builder.addFromWebApp(webapp);
			return;
		}

		// No web.xml.
	}

	private File targetWebXml() {
		return new File(webapp, WEB_XML_PATH);
	}

	private File backupWebXml() {
		return new File(webapp, WEB_XML_BACKUP_PATH);
	}

	private void copyDependentFiles(Path dependentRoot, int webappRootLength, Path targetFolder) throws IOException {
		try (Stream<Path> paths = Files.list(dependentRoot)) {
			paths.forEach(potentialDependent -> {
				try {
					if (Files.isDirectory(potentialDependent)) {
						copyDependentFiles(potentialDependent, webappRootLength, targetFolder);
					} else {
						if (isDependentFile(potentialDependent)) {
							final Path fileName =
								potentialDependent.subpath(webappRootLength, potentialDependent.getNameCount());

							final Path dest = targetFolder.resolve(fileName.toString());
							if (Files.exists(dest) && !createdFiles.contains(dest)) {
								// dest is an original file
								return;
							}

							// create missing folders
							createFolders(dest.getParent());

							copyDependentFile(potentialDependent, dest);
						}
					}
				} catch (IOException ex) {
					Logger.error("Cannot copy '" + potentialDependent + "' to '" + dependentRoot + "'.", ex);
				}
			});
		}
	}

	private void copyDependentFile(Path source, Path dest) throws IOException {
		if (!Files.exists(dest)) {
			Files.createFile(dest);
			createdFiles.add(dest);
		}
		FileUtilities.copyFile(source, dest);
	}

	private boolean isDependentFile(Path potentialDependent) {
		final String fileName = potentialDependent.getFileName().toString();
		return fileName.endsWith(".tld") || fileName.endsWith(".inc") || fileName.endsWith(".jspf");
	}

	private void createFolders(Path dest) throws IOException {
		if (!Files.exists(dest)) {
			createFolders(dest.getParent());
			Files.createDirectory(dest);
			createdFiles.add(dest);
		}
	}

	private void tearDown() {
		removeCreatedFiles();
	}

	private void removeCreatedFiles() {
		File targetWebXml = targetWebXml();
		targetWebXml.delete();
		File backupWebXml = backupWebXml();
		if (backupWebXml.exists()) {
			backupWebXml.renameTo(targetWebXml);
		}

		Collection<Path> missingDeletions = new HashSet<>();
		for (int index = createdFiles.size() - 1; index >= 0; index--) {
			Path toDelete = createdFiles.get(index);
			try {
				final boolean success = Files.deleteIfExists(toDelete);
				if (!success) {
					missingDeletions.add(toDelete);
				}
			} catch (IOException ex) {
				Logger.error("Cannot delete: " + toDelete, ex);
			}
		}

		if (!missingDeletions.isEmpty()) {
			reportFailure("Unable to delete temp files: " + missingDeletions);
		}
	}

	public void compile() {
		try {
			setUp();
		} catch (Exception ex) {
			reportFailure("Cannot set up compiler.", ex);
			return;
		}
		try {
			for (JspCompileTask task : _tasks) {
				task.run(_log);
			}
		} finally {
			tearDown();
		}
	}

	private void reportFailure(String message) {
		_log.error(message);
	}

	private void reportFailure(String message, Throwable ex) {
		_log.error(message, ex);
	}
}
