/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tool.boundsec.securityObjectProvider.path.I18NConstants;
import com.top_logic.tool.boundsec.securityObjectProvider.path.PathFormat;
import com.top_logic.tool.boundsec.securityObjectProvider.path.SecurityPath;

/**
 * Test for the {@link PathFormat}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPathFormat extends BasicTestCase {

	public void testValid() throws ConfigurationException {
		assertEquals(0, parse("").size());
		assertEquals(list(SecurityPath.model().getConfig()), parse("model"));
		assertEquals(list(SecurityPath.master().getConfig(), SecurityPath.selection().getConfig(),
			SecurityPath.currentObject().getConfig()), parse("master.selection.currentobject"));
	}

	public void testInvalid() {
		try {
			parse("  ");
			fail("Only space.");
		} catch (ConfigurationException ex) {
			// expected
		}
		try {
			parse("myfunn");
			fail("Unknown path element");
		} catch (ConfigurationException ex) {
			// expected
			assertEquals(I18NConstants.UNKNOWN_PATH_ELEMENT__PATH_ELEMENT, ex.getErrorKey().plain());
		}
		try {
			parse("master.selection.myfun");
			fail("Unknown path element after valid path.");
		} catch (ConfigurationException ex) {
			// expected
			assertEquals(I18NConstants.UNKNOWN_PATH_ELEMENT__PATH_ELEMENT, ex.getErrorKey().plain());
		}
		try {
			parse("master   ");
			fail("Trailing space.");
		} catch (ConfigurationException ex) {
			// expected
		}
		try {
			parse("   master");
			fail("Leading space.");
		} catch (ConfigurationException ex) {
			// expected
		}
	}

	private List<PolymorphicConfiguration<? extends SecurityPath>> parse(String propertyValue)
			throws ConfigurationException {
		return PathFormat.INSTANCE.getValue("prop", propertyValue);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPathFormat}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestPathFormat.class);
	}

}

