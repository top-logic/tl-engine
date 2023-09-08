/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.tooling;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.top_logic.basic.Main;
import com.top_logic.basic.io.FileUtilities;

/**
 * Tool for creating a complete <code>web.xml</code> from an initial <code>web.xml</code> and a set
 * of <code>web-fragment.xml</code> resources from tha applications library JARs.
 * 
 * <p>
 * Usage -webapp [path to the web application root folder]
 * </p>
 * 
 * <p>
 * As a result of the tool's execution the application's <code>web.xml</code> is created or updated
 * in <code>WEB-INF/web.xml</code> relative to the given application root folder.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WebXmlTask extends Main {

	private String _webapp;

	@Override
	protected int longOption(String option, String[] args, int i) {
		int n = i;
		if (option.equals("webapp")) {
			_webapp = args[n++];
		} else {
			return super.longOption(option, args, i);
		}
		return n;
	}

	@Override
	protected void doActualPerformance() throws Exception {
		File webApp = new File(_webapp);
		if (!webApp.isDirectory()) {
			error("Not a directory: " + webApp.getAbsolutePath());
			return;
		}

		WebXmlBuilder builder = new WebXmlBuilder().addFromWebApp(webApp);

		File libs = new File(webApp, "WEB-INF/lib");
		File[] jars = FileUtilities.listFiles(libs, new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !pathname.isDirectory() && pathname.getName().endsWith(".jar");
			}
		});
		
		for (File jar : jars) {
			try (ZipFile zip = new ZipFile(jar)) {
				ZipEntry entry = zip.getEntry("META-INF/web-fragment.xml");
				if (entry == null) {
					continue;
				}
				try (InputStream in = zip.getInputStream(entry)) {
					builder.addFragment(in);
				}
			}
		}

		builder.dumpTo(new File(webApp, "WEB-INF/web.xml"));
	}

	public static void main(String[] args) throws Exception {
		new WebXmlTask().runMain(args);
	}
}
