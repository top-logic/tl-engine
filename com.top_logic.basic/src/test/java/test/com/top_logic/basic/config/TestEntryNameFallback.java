/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;

/**
 * The class {@link TestEntryNameFallback} tests the entry tag of configuration
 * lists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestEntryNameFallback extends AbstractTypedConfigurationTestCase {

	public interface Config extends ConfigurationItem {

		// Tests that entry name is "listConfig"
		List<Config> getListConfigs();

		// Tests that entry name is "property"
		List<Config> getProperties();

		// Tests that entry name is "child"
		List<Config> getChildren();

		// Tests that entry name is "entry"
		List<Config> getSomeList();

		@EntryTag("freakyName")
		List<Config> getSpecialList();
	}

	public void testPluralSFallback() throws IOException, ConfigurationException {
		final ConfigurationItem config = readConfiguration(TestEntryNameFallback.class, getDescriptors(),
				"pluralS.xml", null);
		assertInstanceof(config, Config.class);
		List<Config> listConfigs = ((Config) config).getListConfigs();
		assertEquals(2, listConfigs.size());
	}

	public void testIesFallback() throws IOException, ConfigurationException {
		final ConfigurationItem config = readConfiguration(TestEntryNameFallback.class, getDescriptors(),
				"ies_to_y.xml", null);
		assertInstanceof(config, Config.class);
		List<Config> listConfigs = ((Config) config).getProperties();
		assertEquals(2, listConfigs.size());
	}

	public void testChildrenFallback() throws IOException, ConfigurationException {
		final ConfigurationItem config = readConfiguration(TestEntryNameFallback.class, getDescriptors(),
				"children.xml", null);
		assertInstanceof(config, Config.class);
		List<Config> listConfigs = ((Config) config).getChildren();
		assertEquals(2, listConfigs.size());
	}

	public void testOtherFallback() throws IOException, ConfigurationException {
		final ConfigurationItem config = readConfiguration(TestEntryNameFallback.class, getDescriptors(),
				"general_fallback.xml", null);
		assertInstanceof(config, Config.class);
		List<Config> listConfigs = ((Config) config).getSomeList();
		assertEquals(2, listConfigs.size());
	}

	public void testEntryName() throws IOException, ConfigurationException {
		final ConfigurationItem config = readConfiguration(TestEntryNameFallback.class, getDescriptors(),
				"special_entry_name.xml", null);
		assertInstanceof(config, Config.class);
		List<Config> listConfigs = ((Config) config).getSpecialList();
		assertEquals(2, listConfigs.size());
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("test",
			TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestEntryNameFallback.class);
	}

}
