/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider;

import junit.framework.Test;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * Test case for {@link MetaResourceProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMetaResourceProvider extends BasicTestCase {

	/**
	 * Must be <code>null</code>-safe.
	 */
	public void testNull() {
		assertNull(MetaResourceProvider.INSTANCE.getLabel(null));
		assertNull(MetaResourceProvider.INSTANCE.getType(null));
		assertNull(MetaResourceProvider.INSTANCE.getImage(null, Flavor.DEFAULT));
		assertNull(MetaResourceProvider.INSTANCE.getTooltip(null));
		assertNull(MetaResourceProvider.INSTANCE.getLink(DummyDisplayContext.newInstance(), null));
		assertNull(MetaResourceProvider.INSTANCE.getCssClass(null));
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestMetaResourceProvider.class);
	}
}
