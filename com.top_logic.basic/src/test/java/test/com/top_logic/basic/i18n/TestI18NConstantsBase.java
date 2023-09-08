/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.i18n;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.i18n.I18NConstantsBase;

/**
 * Test case for the automatic generation of I18N keys for I18NConstants classes in
 * {@link I18NConstantsBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestI18NConstantsBase extends TestCase {

	private static String PREFIX = "class.test.com.top_logic.basic.i18n.I18NConstants.";

	public void testAll() {
		assertEquals(PREFIX + "EDIT_TOKEN_TIMED_OUT", I18NConstants.EDIT_TOKEN_TIMED_OUT.toString());
		assertEquals(PREFIX + "FORMAT_INVALID__VALUE", I18NConstants.FORMAT_INVALID__VALUE.toString());
		assertEquals(PREFIX + "FORMAT_INVALID__VALUE_EXAMPLE", I18NConstants.FORMAT_INVALID__VALUE_EXAMPLE.toString());
	}
	
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestI18NConstantsBase.class);
	}

}
