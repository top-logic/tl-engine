/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import java.util.ArrayList;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.basic.config.template.TestTemplateExpression;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.template.TemplateExpression;
import com.top_logic.basic.config.template.parser.ParseException;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.values.edit.ResourceScope;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;

/**
 * Test that all resource templates can be parsed and evaluated (at least with a default item as
 * model).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
@DeactivatedTest("Prevent duplicate execution: This is a test that should be run for every project. Such Tests are run via the TestAll, which explizitly calls such tests.")
public class TestResourceTemplates extends TestCase {

	public void testAllResourceTemplates() {
		ResourcesModule module = ResourcesModule.getInstance();
		for (String lang : module.getSupportedLocaleNames()) {
			doTestLocale(module, lang);
		}
	}

	private void doTestLocale(ResourcesModule module, String lang) {
		I18NBundle bundle = module.getBundle(new Locale(lang));

		ResourceScope scope = new ResourceScope(bundle.getLocale());

		ArrayList<String> errors = new ArrayList<>();
		for (Object key : bundle.getLocalKeys()) {
			String keyValue = (String) key;
			try {
				String value = bundle.getString(ResKey.forTest(keyValue));
				if (value.indexOf('{') < 0) {
					continue;
				}

				Class<?> clazz;
				try {
					clazz = Class.forName(keyValue);
				} catch (ClassNotFoundException ex) {
					continue;
				}

				if (!(ConfigurationItem.class.isAssignableFrom(clazz))) {
					continue;
				}

				if (clazz.getAnnotation(UseTemplate.class) != null) {
					// This is another form of resource templates for generating input UIs.
					continue;
				}

				checkTemplate(value, clazz, scope);
			} catch (Throwable ex) {
				errors.add("Cannot evaluate resource template for key '" + keyValue + "' in language '" + lang + "': "
					+ ex.getMessage() + ".");
			}
		}

		if (!errors.isEmpty()) {
			BasicTestCase.fail(StringServices.join(errors, " \n"));
		}
	}

	private void checkTemplate(String templateSource, Class<?> clazz, ResourceScope scope)
			throws ParseException {
		TemplateExpression expression = TestTemplateExpression.parse(templateSource);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ConfigurationItem config = TypedConfiguration.newConfigItem((Class) clazz);

		TestTemplateExpression
			.expand(config, TestTemplateExpression.noVariables(), expression, scope);
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestResourceTemplates.class, ResourcesModule.Module.INSTANCE));
	}
}
