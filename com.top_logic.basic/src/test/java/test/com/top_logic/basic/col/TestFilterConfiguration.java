/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;

/**
 * Test case for typed configuration of {@link Filter}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestFilterConfiguration extends AbstractTypedConfigurationTestCase {

	public interface Config extends ConfigurationItem {
		@DefaultContainer
		@InstanceFormat
		Filter<? super String> getFilter();
	}

	public void testOr() throws ConfigurationException {
		Config config = read("<config><or><starts-with prefix='foo'/><equals values='bar,bazz'/></or></config>");
		assertTrue(config.getFilter().accept("foo"));
		assertTrue(config.getFilter().accept("foobar"));
		assertTrue(config.getFilter().accept("bar"));
		assertTrue(config.getFilter().accept("bazz"));
		assertFalse(config.getFilter().accept("barfoo"));
	}

	public void testAnd() throws ConfigurationException {
		Config config =
			read("<config><and><matches pattern='.*foo.*'/><not><equals values='foobar'/></not></and></config>");
		assertTrue(config.getFilter().accept("foo"));
		assertTrue(config.getFilter().accept("bazzfoobar"));
		assertFalse(config.getFilter().accept("foobar"));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestFilterConfiguration.class);
	}

}
