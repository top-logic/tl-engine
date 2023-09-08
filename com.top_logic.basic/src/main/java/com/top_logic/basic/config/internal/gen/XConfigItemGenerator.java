/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.internal.gen;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.XMain;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.util.Computation;

/**
 * Main method for invoking {@link ConfigItemGenerator}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class XConfigItemGenerator extends XMain {

	private static final String APP_ROOT = "approot";

	private static final String CLASSES_ROOT = "classes";

	File _appRoot;

	File _classes;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if (APP_ROOT.equals(option)) {
			setAppRoot(args[i++]);
		} else if (CLASSES_ROOT.equals(option)) {
			setClassesRoot(args[i++]);
		} else {
			return super.longOption(option, args, i);
		}
		return i;
	}

	private void setClassesRoot(String string) {
		File classesFolder = new File(string);
		if (!(classesFolder.exists() && classesFolder.isDirectory())) {
			throw new IllegalArgumentException(classesFolder + " is not an existing directory.");
		}
		_classes = classesFolder;
	}

	private void setAppRoot(String string) {
		File appRoot = new File(string);
		if (!(appRoot.exists() && appRoot.isDirectory())) {
			throw new IllegalArgumentException(appRoot + " is not an existing directory.");
		}
		_appRoot = appRoot;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		initFileManager();
		initXMLProperties();

		final int classesPrefixLength = _classes.getAbsolutePath().length();
		final Protocol log = new LogProtocol(ConfigItemGenerator.class);
		ModuleUtil.INSTANCE.inModuleContext(XMLProperties.Module.INSTANCE, new Computation<Void>() {

			@Override
			public Void run() {
				generate(_appRoot, _classes, XConfigItemGenerator.this, classesPrefixLength);
				return null;
			}
		});
		log.checkErrors();
	}

	static void generate(File appRoot, File classes, Log log, int classesPrefixLength) {
		if (classes.isFile()) {
			Class<?> loadedClass;
			try {
				loadedClass =
					Class.forName(getClassName(classes, classesPrefixLength), false,
						ConfigItemGenerator.class.getClassLoader());
			} catch (ClassNotFoundException ex) {
				log.error("File " + classes + " can not be loaded. Class does not exist?", ex);
				return;
			} catch (Throwable ex) {
				log.error("File " + classes + " can not be loaded.", ex);
				return;
			}
			if (ConfigurationItem.class.isAssignableFrom(loadedClass)) {
				if (!loadedClass.isInterface()) {
					/* Only generation for interfaces, not for classes that implement a
					 * ConfigurationItem. */
					return;
				}
				if (ConfigItemGenerator.doNotGenerateImplementationClass(loadedClass)) {
					return;
				}
				ConfigItemGenerator.generate(log, appRoot, (Class<? extends ConfigurationItem>) loadedClass);
			}
		} else {
			try {
				File[] containedFiles = FileUtilities.listFiles(classes, new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory() || pathname.getName().endsWith(".class");
					}
				});
				for (File f : containedFiles) {
					generate(appRoot, f, log, classesPrefixLength);
				}
			} catch (IOException ex) {
				log.error("Directory " + classes + " can not be scanned.", ex);
				return;
			}
		}
	}

	private static String getClassName(File classes, int classesPrefixLength) {
		String className = classes.getAbsolutePath().substring(classesPrefixLength + 1);
		String slashByDot = className.replace('\\', '.').replace('/', '.');
		return slashByDot.substring(0, slashByDot.length() - 6);
	}

	public static void main(String args[]) throws Exception {
		new XConfigItemGenerator().runMainCommandLine(args);
	}

}

