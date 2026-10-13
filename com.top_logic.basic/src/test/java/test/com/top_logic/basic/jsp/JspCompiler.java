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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import test.com.top_logic.basic.ConfigLoaderTestUtil;

import com.top_logic.basic.Log;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.tooling.WebXmlBuilder;

/**
 * Compiles the JSPs of a {@link CompileJspConfig} in a web application assembled in the scratch
 * directory.
 *
 * <p>
 * The JSP compiler needs the tag library descriptors and statically included files of all modules
 * of the application, and a <code>web.xml</code> mapping the tag library URIs. These are collected
 * from the application's resource path ({@link Workspace#applicationModules(String...)}) into a
 * separate web application below {@link CompileJSP#TMP_DIR}, together with the JSPs to compile.
 * The source web application of the module is only read, so that processes using it concurrently
 * (e.g. an application started by scripted tests of the same module) are not affected.
 * </p>
 *
 * @see #compile()
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class JspCompiler {

	/**
	 * Name of the directory within {@link CompileJSP#TMP_DIR} holding the assembled web
	 * application.
	 */
	private static final String WEBAPP_DIR = "webapp";

	/**
	 * Number of JSPs translated by a single {@link JspCompileTask}.
	 */
	private static final int CHUNK_SIZE = 100;

	private static final String WEB_XML_PATH = "WEB-INF/web.xml";

	private final CompileJspConfig _config;

	private final Log _log;

	private final File _webapp;

	/**
	 * Creates a {@link JspCompiler}.
	 *
	 * @param log
	 *        The {@link Log} to report compile errors to.
	 * @param config
	 *        The JSPs to compile.
	 */
	JspCompiler(Log log, CompileJspConfig config) {
		_log = log;
		_config = config;
		_webapp = new File(CompileJSP.TMP_DIR, WEBAPP_DIR);
	}

	/**
	 * Assembles the web application and compiles the JSPs of the {@link CompileJspConfig} in it.
	 */
	public void compile() {
		List<File> jspFiles;
		try {
			jspFiles = setUp();
		} catch (Exception ex) {
			reportFailure("Cannot set up compiler.", ex);
			return;
		}

		for (int n = 0, cnt = jspFiles.size(); n < cnt; n += CHUNK_SIZE) {
			List<File> chunk = jspFiles.subList(n, Math.min(cnt, n + CHUNK_SIZE));

			String targetPckg = "org.apache.jsp" + n;
			List<String> translateCommand;
			try {
				translateCommand = CompileJSP.getTranslateCommand(_config, _webapp, targetPckg, chunk);
			} catch (IOException ex) {
				reportFailure("Cannot create compile command for files: " + chunk, ex);
				continue;
			}

			File rootFolder = new File(CompileJSP.TMP_DIR + '/' + targetPckg.replace('.', '/'));
			new JspCompileTask(chunk, translateCommand, _config, rootFolder).run(_log);
		}
	}

	/**
	 * Assembles the web application to compile in.
	 *
	 * @return The JSPs to compile, resolved in the assembled web application.
	 */
	private List<File> setUp() throws IOException, ParserConfigurationException, SAXException {
		Path webapp = _webapp.toPath();
		Files.createDirectories(webapp);

		for (Path resourceDir : Workspace.applicationModules()) {
			copyCompileResources(resourceDir, webapp);
		}

		Path sourceWebapp = _config.getWebapp().getCanonicalFile().toPath();
		List<File> result = new ArrayList<>();
		for (File jspFile : _config.getJspFiles()) {
			Path source = jspFile.getCanonicalFile().toPath();
			Path target = webapp.resolve(sourceWebapp.relativize(source).toString());
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			result.add(target.toFile());
		}

		createWebXml();

		return result;
	}

	/**
	 * Copies the files the JSP compiler needs from the given resource directory into the assembled
	 * web application.
	 *
	 * <p>
	 * The resource directories are visited in overlay order, the overriding module first. A file
	 * already copied from a previous resource directory is therefore kept.
	 * </p>
	 */
	private static void copyCompileResources(Path resourceDir, Path webapp) throws IOException {
		if (!Files.isDirectory(resourceDir)) {
			return;
		}
		List<Path> files;
		try (Stream<Path> paths = Files.walk(resourceDir)) {
			files = paths.filter(Files::isRegularFile).filter(JspCompiler::isCompileResource).toList();
		}
		for (Path file : files) {
			Path target = webapp.resolve(resourceDir.relativize(file).toString());
			if (Files.exists(target)) {
				continue;
			}
			Files.createDirectories(target.getParent());
			Files.copy(file, target);
		}
	}

	private static boolean isCompileResource(Path file) {
		String fileName = file.getFileName().toString();
		return fileName.endsWith(".jsp") || fileName.endsWith(".jspf") || fileName.endsWith(".inc")
			|| fileName.endsWith(".tld");
	}

	private void createWebXml() throws IOException, ParserConfigurationException, SAXException {
		WebXmlBuilder builder = new WebXmlBuilder();
		addWebXml(builder);
		builder.addClassPathFragments();
		builder.dumpTo(new File(_webapp, WEB_XML_PATH));
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

		File sourceWebapp = _config.getWebapp();
		if (new File(sourceWebapp, WEB_XML_PATH).exists()) {
			builder.addFromWebApp(sourceWebapp);
			return;
		}

		// No web.xml.
	}

	private void reportFailure(String message, Throwable ex) {
		_log.error(message, ex);
	}
}
