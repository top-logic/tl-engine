/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sql.DBHelper;

/**
 * Test that each {@link DBHelper} can be loaded without the JDBC driver of its database.
 *
 * <p>
 * The JDBC drivers are optional dependencies, an application only contains the driver of the
 * database it uses. Nevertheless, all {@link DBHelper}s are loaded, e.g. when the options for the
 * SQL dialect of a connection pool are computed in the service editor.
 * </p>
 */
public class TestDBHelperWithoutDriver extends TestCase {

	/**
	 * Package prefixes of the JDBC drivers, hidden from the {@link DBHelper}s.
	 */
	private static final String[] DRIVER_PACKAGES = {
		"org.postgresql.",
		"oracle.",
		"com.ibm.db2.",
		"com.mysql.",
		"org.mariadb.",
		"com.microsoft.sqlserver.",
		"org.h2.",
	};

	/**
	 * Loads and initializes each {@link DBHelper} in a class loader that has no JDBC driver.
	 */
	public void testLoadWithoutDriver() throws ModuleException, IOException {
		List<String> helpers = new ArrayList<>();
		try (ModuleContext context = ModuleUtil.INSTANCE.begin()) {
			ModuleUtil.INSTANCE.startModule(TypeIndex.class);
			Collection<Class<?>> implementations =
				TypeIndex.getInstance().getSpecializations(DBHelper.class, true, false, false);
			for (Class<?> implementation : implementations) {
				helpers.add(implementation.getName());
			}
		}
		assertTrue("No database helpers found.", helpers.size() > 1);

		List<String> failures = new ArrayList<>();
		try (URLClassLoader loader = new WithoutDriverClassLoader(classPath())) {
			for (String helper : helpers) {
				try {
					Class.forName(helper, true, loader);
				} catch (ClassNotFoundException | LinkageError ex) {
					failures.add(helper + ": " + ex);
				}
			}
		}
		assertEquals("Database helpers that cannot be loaded without a JDBC driver.", List.of(), failures);
	}

	private static URL[] classPath() throws MalformedURLException {
		String[] entries = System.getProperty("java.class.path").split(File.pathSeparator);
		URL[] result = new URL[entries.length];
		for (int n = 0; n < entries.length; n++) {
			result[n] = new File(entries[n]).toURI().toURL();
		}
		return result;
	}

	/**
	 * {@link ClassLoader} loading the classes of the class path itself, except those of the JDBC
	 * drivers.
	 *
	 * <p>
	 * The classes are not delegated to the application class loader, which would find the drivers.
	 * </p>
	 */
	private static final class WithoutDriverClassLoader extends URLClassLoader {

		WithoutDriverClassLoader(URL[] classPath) {
			super(classPath, ClassLoader.getPlatformClassLoader());
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			for (String driverPackage : DRIVER_PACKAGES) {
				if (name.startsWith(driverPackage)) {
					throw new ClassNotFoundException(name);
				}
			}
			return super.loadClass(name, resolve);
		}

	}

	/**
	 * Suite of tests to execute.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestDBHelperWithoutDriver.class);
	}

}
