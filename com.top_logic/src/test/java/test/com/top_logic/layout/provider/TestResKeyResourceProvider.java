/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.layout.provider.ResKeyResourceProvider;

/**
 * Test case for {@link ResKeyResourceProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResKeyResourceProvider extends TestCase {

	public void testLabel() {
		assertEquals("ONLY_LABEL", ResKeyResourceProvider.INSTANCE.getLabel(I18NConstants.ONLY_LABEL));
	}

	public void testTooltip() {
		assertNull(ResKeyResourceProvider.INSTANCE.getTooltip(I18NConstants.ONLY_LABEL));
		assertEquals("tooltip", ResKeyResourceProvider.INSTANCE.getTooltip(I18NConstants.LABEL_WITH_TOOLTIP));
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestResKeyResourceProvider.class);
	}
}
