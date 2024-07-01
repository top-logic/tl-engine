/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.jsp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;

/**
 * Task triggering the JSP compiler and reading output back.
 * 
 * @see #run(Log)
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
class JspCompileTask {

	private static final String ERROR_KEY_WORD = "Exception";
	
	private static FileFilter JAVA_DIR_FILTER = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() || pathname.getName().endsWith("java");
		}
	};
	
	private final List<String> translateCommand;

	private final CompileJspConfig config;

	private final List<File> _jspFiles;

	private final File rootFolder;

	private Log _log;

	/**
	 * This constructor creates a new CompileJspTest.
	 * 
	 * @param jspFiles
	 *        the tested jsp file must be a descendant of {@link CompileJspConfig#getWebapp()}
	 * @param translateCommand
	 *        the command to trigger the translator
	 * @param config
	 *        the configuration of the test
	 * @param rootFolder
	 *        root folder to which the jsp-Files will be translated
	 */
	public JspCompileTask(List<File> jspFiles, List<String> translateCommand, CompileJspConfig config,
			File rootFolder) {
		this._jspFiles = jspFiles;
		this.translateCommand = translateCommand;
		this.config = config;
		this.rootFolder = rootFolder;
	}

	public void run(Log log) {
		_log = log;

		try {
			Logger.info("Compiling '" + _jspFiles.get(0) + "' and others.", JspCompileTask.class);
			translate();
			if (CompileJSP.USE_JAVAC) {
				compile();
			}
		} catch (Exception ex) {
			reportFailure("Problem translating file. ", ex);
		}
	}

	void reportFailure(String message, Throwable ex) {
		_log.error(message, ex);
	}

	private void translate() throws IOException, InterruptedException {
		executeComand(translateCommand);
	}

	private void compile() throws IOException, InterruptedException {
		List<String> compileCommand = new ArrayList<>();
		compileCommand.add("javac");
		CompileJSP.addClassPath(compileCommand);
		compileCommand.add("-compilerSourceVM");
		compileCommand.add(CompileJSP.SOURCE_CODE_VERSION);
		compileCommand.add("-compilerTargetVM");
		compileCommand.add(CompileJSP.TARGET_VM_VERSION);
		final ArrayList<File> generatedJavaFiles = CompileJSP.findFiles(new ArrayList<>(), rootFolder, JAVA_DIR_FILTER);
		if (generatedJavaFiles.isEmpty()) {
			throw new IllegalArgumentException("No generated java files in " + rootFolder);
		}
		for (File javaFile : generatedJavaFiles) {
			compileCommand.add(javaFile.getCanonicalPath());
		}
		executeComand(compileCommand);
	}

	private void executeComand(List<String> command) throws IOException, InterruptedException {
//		System.out.println("Execute: " + command);
		final Process process = Runtime.getRuntime().exec(command.toArray(new String[command.size()]));
		final StringBuilder outBuffer = new StringBuilder();
		final StringBuilder errorBuffer = new StringBuilder();
		
		// lock[0] == true iff the process has finished
		// lock[1] == true iff the waiting thread is running
		final boolean[] lock = new boolean[2];
		/* contains exceptions occurred in waiting thread */
		new Thread("JSP compiler observer") {
			{
				setDaemon(true);
			}
			
			@Override
			public void run() {
				super.run();
				try {
					synchronized (lock) {
						lock[1] = true;
						lock.notifyAll();
					}

					InputStream outStream = process.getInputStream();
					try {
						InputStream errStream = process.getErrorStream();
						try {
							while (true) {
								boolean stop;
								synchronized (lock) {
									try {
										lock.wait(1000);
										stop = lock[0];
									} catch (InterruptedException e) {
										stop = true;
									}
								}
								if (outStream == null) {
									outStream = process.getInputStream();
								}
								if (outStream != null) {
									int data;
									int available = outStream.available();
									while (available > 0 && (data = outStream.read()) != -1) {
										outBuffer.append((char)data);
										available--;
									}
								}
								if (errStream == null) {
									errStream = process.getErrorStream();
								}
								if (errStream != null) {
									int data;
									int available = errStream.available();
									while (available > 0 && (data = errStream.read()) != -1) {
										errorBuffer.append((char)data);
										available--;
									}
								}
								if (stop) {
									break;
								}
							}
						} finally {
							if (errStream != null) {
								errStream.close();
							}
						}
					} finally {
						if (outStream != null) {
							outStream.close();
						}
					}
				} catch (Throwable ex) {
					synchronized (lock) {
						reportFailure("Problem waiting for compiler.", ex);
					}
				} finally {
					synchronized (lock) {
						lock[1] = false;
						lock.notifyAll();
					}
				}
			}
		}.start();

		// Must wait for completion to be able to evaluate the results
		final int returnCode = process.waitFor();

		synchronized (lock) {
			lock[0] = true;
			lock.notifyAll();
			while (lock[1]) {
				lock.wait();
			}
		}
		
		final String error = errorBuffer.toString();
		final String out = outBuffer.toString();
		
		if (returnCode != 0 || error.contains(ERROR_KEY_WORD) || out.contains(ERROR_KEY_WORD)) {
			reportFailure(error + "\n" + Arrays.asList(out.split("\n")).stream().filter(line -> !line.contains("INFO"))
				.collect(Collectors.joining("\n")), null);
		}
	}

}
