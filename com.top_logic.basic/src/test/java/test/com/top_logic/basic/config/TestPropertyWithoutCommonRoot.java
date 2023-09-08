/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.config.ScenarioPropertyWithoutCommonRoot.ScenarioTypeC;
import test.com.top_logic.basic.config.ScenarioPropertyWithoutCommonRoot.ScenarioTypeD;
import test.com.top_logic.basic.config.ScenarioPropertyWithoutCommonRoot.ScenarioTypeE;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * A configuration interface is not allowed to inherit properties with the same name from multiple
 * super interfaces, if there is no common root for all of them.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPropertyWithoutCommonRoot extends AbstractTypedConfigurationTestCase {

	/** Tests: {@link TypedConfiguration#getConfigurationDescriptor(Class)} */
	public void testGetConfigurationDescriptor() {
		assertIllegal(null, "Inherited properties have no common root property", ScenarioTypeC.class);
	}

	/**
	 * Tests: {@link TypedConfiguration#getConfigurationDescriptor(Class)} with a config which has a
	 * property of the broken type.
	 */
	public void testReferencesAndGetConfigurationDescriptor() {
		assertIllegal(null, "Inherited properties have no common root property", ScenarioTypeD.class);
	}

	/**
	 * Tests: {@link TypedConfiguration#getConfigurationDescriptor(Class)} with a config which has a
	 * config which has in turn a property of the broken type.
	 */
	public void testIndirectReferencesAndGetConfigurationDescriptor() {
		assertIllegal(null, "Inherited properties have no common root property", ScenarioTypeE.class);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPropertyWithoutCommonRoot.class));
	}

}
