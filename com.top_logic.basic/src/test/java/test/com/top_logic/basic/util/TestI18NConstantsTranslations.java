/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.GenericTest;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.i18n.I18NConstantsChecker;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Test case that tests that translations exist for all {@link I18NConstantsBase I18N constants}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DeactivatedTest("This test is part of the module independent tests and created and executed explicitly. Deactivated to prevent duplicate execution.")
public class TestI18NConstantsTranslations extends GenericTest {

	/**
	 * Creates a new {@link TestI18NConstantsTranslations}.
	 */
	private TestI18NConstantsTranslations() {
		super(TestI18NConstantsTranslations.class.getName());
	}

	@Override
	protected void executeTest() throws Throwable {
		AssertNoErrorLogListener listener = new AssertNoErrorLogListener(true);
		listener.activate();
		try {
			File moduleDir = new File(".");
			new I18NConstantsChecker() {

				@Override
				protected boolean skipClassCheck(Class<?> i18NClass) {
					if (super.skipClassCheck(i18NClass)) {
						return true;
					}
					return !isModuleClass(i18NClass, moduleDir);
				}

			}.checkI18N();
			listener.assertNoErrorLogged("Errors while checking I18NConstants: ");
		} finally {
			listener.deactivate();
		}
	}

	/**
	 * Whether the given class belongs to the module in the given directory.
	 * 
	 * @see #isModuleLocation(URL, File)
	 */
	static boolean isModuleClass(Class<?> c, File moduleDir) {
		CodeSource codeSource = c.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			return false;
		}
		return isModuleLocation(codeSource.getLocation(), moduleDir);
	}

	/**
	 * Whether a class loaded from the given code source location belongs to the module in the given
	 * directory.
	 * 
	 * <p>
	 * The tested module is the one whose base directory is the working directory of the test run. A
	 * class belongs to it, if its code source lies below this directory (e.g. its
	 * <code>target/classes</code> or <code>target/test-classes</code>). JARs of the Maven repository
	 * and class directories of other modules of the same reactor build are not part of the module.
	 * </p>
	 * 
	 * @param location
	 *        The {@link CodeSource#getLocation() code source location} of a class, may be
	 *        {@code null}.
	 * @param moduleDir
	 *        The base directory of the tested module.
	 */
	static boolean isModuleLocation(URL location, File moduleDir) {
		if (location == null || !"file".equals(location.getProtocol())) {
			return false;
		}
		try {
			Path codePath = Paths.get(location.toURI()).toRealPath();
			Path modulePath = moduleDir.toPath().toRealPath();
			return codePath.startsWith(modulePath);
		} catch (URISyntaxException | IOException ex) {
			return false;
		}
	}

	/**
	 * Creates a {@link Test} checking existence of translations for {@link I18NConstantsBase}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(new TestI18NConstantsTranslations(),
				TypeIndex.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}

}
