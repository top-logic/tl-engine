/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

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
			new I18NConstantsChecker() {

				@Override
				protected boolean skipClassCheck(Class<?> i18NClass) {
					if (super.skipClassCheck(i18NClass)) {
						return true;
					}
					return isInJarFile(i18NClass);
				}
				
				/**
				 * Test only those classes which lie in the tested module. This is done by checking
				 * if the class file is not in a JAR file.
				 */
				private boolean isInJarFile(Class<?> c) {
					String base = c.getProtectionDomain().getCodeSource().getLocation().getFile();
					return (base.endsWith(".jar") || base.indexOf(".jar!") > 0);
				}

			}.checkI18N();
			listener.assertNoErrorLogged("Errors while checking I18NConstants: ");
		} finally {
			listener.deactivate();
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
