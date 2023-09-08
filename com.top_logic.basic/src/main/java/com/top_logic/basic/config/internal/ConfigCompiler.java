/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.gen.ConfigItemGenerator;
import com.top_logic.basic.config.internal.gen.ConfigurationDescriptorSPI;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Factory for dynamically generated implementation classes for {@link ConfigurationItem}
 * interfaces.
 * 
 * @see #compileImplementation(Class)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigCompiler {

	private final JavaCompiler _compiler;

	private final DiagnosticListener<JavaFileObject> _diagnostics = new DiagnosticListener<>() {
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			switch (diagnostic.getKind()) {
				case ERROR:
					Logger.error(diagnostic.getMessage(ResourcesModule.getLogLocale()) + ", file " + diagnostic.getSource().getName()
						+ ", line " + diagnostic.getLineNumber()
						+ ", column " + diagnostic.getColumnNumber(), ConfigCompiler.class);
					break;
				case MANDATORY_WARNING:
				case WARNING:
					Logger.warn(diagnostic.getMessage(ResourcesModule.getLogLocale()) + ", file " + diagnostic.getSource().getName()
						+ ", line " + diagnostic.getLineNumber()
						+ ", column " + diagnostic.getColumnNumber(), ConfigCompiler.class);
					break;
				case NOTE:
				case OTHER:
					// Logger.info(diagnostic.getMessage(Locale.ENGLISH), ConfigCompiler.class);
					break;
			}
		}
	};

	private final Locale _locale = ResourcesModule.getLogLocale();

	private final Charset _charset = Charset.defaultCharset();

	private JavaFileManager _fileManager;

	private URLClassLoader _classLoader;

	private Iterable<String> _options;

	/**
	 * Creates a {@link ConfigCompiler}.
	 * 
	 * @param generationDir
	 *        The location where dynamically generated code is stored.
	 */
	public ConfigCompiler(File generationDir) throws IOException {
		_compiler = ToolProvider.getSystemJavaCompiler();
		if (_compiler == null) {
			throw new UnsupportedOperationException("No Java compiler available.");
		}
		_fileManager = createFileManager(generationDir);
		_classLoader = new URLClassLoader(new URL[] { generationDir.toURI().toURL() },
			Thread.currentThread().getContextClassLoader());

		_options = Arrays.asList("-cp",
			System.getProperty("java.class.path") + File.pathSeparatorChar + generationDir.getAbsolutePath());
	}

	private StandardJavaFileManager createFileManager(File outputDir) throws IOException {
		StandardJavaFileManager fileManager = _compiler.getStandardFileManager(_diagnostics, _locale, _charset);
		fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, Collections.singletonList(outputDir));
		setClassPath(fileManager);
		return fileManager;
	}

	private void setClassPath(StandardJavaFileManager fileManager) throws IOException {
		List<File> cpFiles = new ArrayList<>();

		cpFiles.addAll(Workspace.getAppPaths().getClassPath());
		for (File outDir : fileManager.getLocation(StandardLocation.SOURCE_OUTPUT)) {
			cpFiles.add(outDir);
		}

		fileManager.setLocation(StandardLocation.CLASS_PATH, cpFiles);
	}

	/**
	 * Dynamically generates an implementation for a {@link ConfigurationItem}.
	 *
	 * @param configInterface
	 *        The {@link ConfigurationItem} interface to implement.
	 * @return The generated implementation class.
	 */
	public Class<?> compileImplementation(Class<?> configInterface) throws GenerationFailedException {
		ConfigurationDescriptorSPI descriptor =
			(ConfigurationDescriptorSPI) TypedConfiguration.getConfigurationDescriptor(configInterface);
		String implClassName = ConfigItemGenerator.qualifiedImplClassName(configInterface);

		try {
			String className = implClassName.replace('.', '/') + ".class";
			if (_classLoader.getResource(className) != null) {
				return _classLoader.loadClass(implClassName);
			}

			return tryCompile(descriptor, implClassName);
		} catch (RuntimeException | NoClassDefFoundError | ClassNotFoundException | IOException ex) {
			throw new GenerationFailedException(ex);
		}
	}

	private Class<?> tryCompile(ConfigurationDescriptorSPI descriptor, String implClassName)
			throws IOException, ClassNotFoundException, GenerationFailedException {
		JavaFileObject srcOutput =
			_fileManager.getJavaFileForOutput(StandardLocation.SOURCE_OUTPUT, implClassName, Kind.SOURCE, null);

		try (Writer writer = srcOutput.openWriter()) {
			ConfigItemGenerator generator = new ConfigItemGenerator(descriptor);
			generator.generate(writer);
		}

		CompilationTask task =
			_compiler.getTask(null, _fileManager, _diagnostics, _options, null, Collections.singletonList(srcOutput));

		Boolean ok = task.call();
		if (ok == null || !ok.booleanValue()) {
			throw new GenerationFailedException("Class '" + implClassName + "' has compile errors.", null);
		}

		Class<?> result = _classLoader.loadClass(implClassName);
		Logger.info("Generated configuration implementation '" + implClassName + "'.", ConfigCompiler.class);
		return result;
	}

}
