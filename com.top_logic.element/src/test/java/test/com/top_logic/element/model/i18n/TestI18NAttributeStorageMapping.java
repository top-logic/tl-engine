/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.i18n;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.i18n.I18NAttributeStorageMapping;

/**
 * {@link TestCase} for {@link I18NAttributeStorageMapping}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestI18NAttributeStorageMapping extends TestCase {

	public void testNullValue() {
		assertNull(I18NAttributeStorageMapping.INSTANCE.getBusinessObject(null));
	}

	public void testResKeyValue() {
		ResKey key = ResKey.text("Travel");
		assertSame(key, I18NAttributeStorageMapping.INSTANCE.getBusinessObject(key));
	}

	public void testForeignValue() {
		try {
			I18NAttributeStorageMapping.INSTANCE.getBusinessObject("Travel");
			fail("A plain string is no internationalized value.");
		} catch (IllegalArgumentException ex) {
			String message = ex.getMessage();
			assertTrue(message, message.contains(String.class.getName()));
		}
	}

}
