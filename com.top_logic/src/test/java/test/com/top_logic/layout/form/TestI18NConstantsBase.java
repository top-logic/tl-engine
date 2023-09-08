/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;

/**
 * Test case for the automatic generation of I18N keys for I18NConstants classes in
 * {@link com.top_logic.layout.I18NConstantsBase}.
 * 
 * @see test.com.top_logic.basic.i18n.TestI18NConstantsBase
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestI18NConstantsBase extends TestCase {

	private static String PREFIX = "class.test.com.top_logic.layout.form.I18NConstants.";

	public void testResourcePrefix() {
		assertEquals(PREFIX + "RESOURCE_PREFIX.", I18NConstants.RESOURCE_PREFIX.toString());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestI18NConstantsBase.class);
	}

}
