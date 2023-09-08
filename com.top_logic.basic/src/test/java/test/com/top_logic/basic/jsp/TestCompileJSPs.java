/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.util.AbstractBasicTestAll;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.io.FileUtilities;

/**
 * Tests compilation of all JSPs.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
@SuppressWarnings("javadoc")
public class TestCompileJSPs extends TestCase {

	public void testCompileJSPs() {
		compile();
	}

	public static void compile(CompileJspConfig config) {
		final List<File> jspFiles = config.getJspFiles();
		if (jspFiles.isEmpty()) {
			// Done.
			return;
		}
	
		File tmp = new File(CompileJSP.TMP_DIR);
		if (tmp.exists()) {
			FileUtilities.deleteR(tmp);
		}
		tmp.mkdirs();
	
		BufferingProtocol log = new BufferingProtocol();

		List<JspCompileTask> tasks = new ArrayList<>();
		for (int n = 0, cnt = jspFiles.size(); n < cnt; n += 100) {
			List<File> chunk = jspFiles.subList(n, Math.min(cnt, n + 100));

			String targetPckg = "org.apache.jsp" + n;
			List<String> translateCommand;
			try {
				translateCommand =
					CompileJSP.getTranslateCommand(config, targetPckg, chunk);
			} catch (IOException ex) {
				log.error("Cannot create compile command for files: " + chunk, ex);
				continue;
			}
			
			File rootFolder = new File(CompileJSP.TMP_DIR + '/' + targetPckg.replace('.', '/'));
			tasks.add(new JspCompileTask(chunk, translateCommand, config, rootFolder));
		}
		new JspCompiler(log, config.getWebapp(), tasks).compile();
		if (log.hasErrors()) {
			fail(log.getErrors().stream().collect(Collectors.joining("\n")));
		}
	}

	/**
	 * Creates for jsp files tests which complete normally iff the jsp's can be compiled.
	 * 
	 * @param webapp
	 *        The file containing the WEB-INF folder (for implementation reasons).
	 * @param relJspPath
	 *        A relative path (with respect to <code>webapp</code>) which contains the jsp files to
	 *        check. The resulting file must be a descendant of <code>webapp</code>.
	 * @param recursive
	 *        Whether also the subfolder of the jsp-folder must be included.
	 */
	public static void compile(File webapp, String relJspPath, boolean recursive) {
		CompileJspConfig config = new CompileJspConfig(webapp);
	
		FileFilter filter;
		if (recursive) {
			filter = CompileJSP.JSP_DIR_FILTER;
		} else {
			filter = CompileJSP.JSP_FILTER;
		}
		File root = new File(webapp, relJspPath);
		final ArrayList<File> jspFiles = CompileJSP.findFiles(new ArrayList<>(), root, filter);
		config.setJspFiles(jspFiles);
		compile(config);
	}

	/**
	 * Creates a test for all jsps in the web application folder.
	 * 
	 * @param webapp
	 *        the name of the folder of the web application.
	 */
	public static void compile(File webapp) {
		compile(webapp, ".", CompileJSP.RECURSIVE);
	}

	/**
	 * Suite of JSP compile tests for the module in which this test is invoked.
	 */
	public static void compile() {
		compile(AbstractBasicTestAll.webapp());
	}

	public static Test suite() {
		return new TestSuite(TestCompileJSPs.class);
	}
}
