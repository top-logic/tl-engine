/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.config.i18n;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.config.i18n.InternationalizedUtil;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.util.Resources;

/**
 * Tests for the {@link InternationalizedUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestInternationalizedUtil extends BasicTestCase {

	public interface A extends ConfigurationItem {
		ResKey getValue();

		void setValue(ResKey value);
	}

	public void testStoreLoadWithNullValue() {
		ConfigurationItem item = TypedConfiguration.newConfigItem(A.class);
		InternationalizedUtil.storeI18N(item);
		InternationalizedUtil.fillLiteralI18N(item);

		item = TypedConfiguration.newConfigItem(ConfigurationItem.class);
		InternationalizedUtil.storeI18N(item);
		InternationalizedUtil.fillLiteralI18N(item);
	}

	public void testStoreLoad() {
		A item = TypedConfiguration.newConfigItem(A.class);
		Builder builder = ResKey.builder();
		List<Locale> supportedLocales = ResourcesModule.getInstance().getSupportedLocales();
		for (Locale locale : supportedLocales) {
			String newTranslation = expectedText(locale);
			builder.add(locale, newTranslation);
		}
		ResKey literal = builder.build();
		item.setValue(literal);

		InternationalizedUtil.storeI18N(item);

		ResKey plain = item.getValue();
		assertFalse(plain.isLiteral());
		assertNotSame(literal, plain);

		for (Locale locale : supportedLocales) {
			String expectedText = expectedText(locale);
			assertEquals(expectedText, Resources.getInstance(locale).getString(plain));
		}

		InternationalizedUtil.fillLiteralI18N(item);

		ResKey copy = item.getValue();
		assertTrue(copy.isLiteral());
		assertNotSame(plain, copy);
		assertNotSame(literal, copy);

		for (Locale locale : supportedLocales) {
			String expectedText = expectedText(locale);
			assertEquals(expectedText, Resources.getInstance(locale).getString(copy));
		}
	}

	private String expectedText(Locale locale) {
		return "translation_" + locale.toString();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestInternationalizedUtil}.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestInternationalizedUtil.class);
		test = ServiceTestSetup.createSetup(TestInternationalizedUtil.class);
		return KBSetup.getSingleKBTest(test);
	}
}

