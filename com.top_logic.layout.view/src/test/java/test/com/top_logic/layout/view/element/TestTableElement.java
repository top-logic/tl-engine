/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TableElement.CriterionConfig;
import com.top_logic.layout.view.element.TableElement.PresetConfig;
import com.top_logic.layout.view.element.TableElement.PresetsConfig;

/**
 * Tests parsing and instantiation of {@link TableElement}.
 */
public class TestTableElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <table>} element can be parsed into configuration.
	 */
	public void testParseTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TableElement config.
		assertTrue("Content should be TableElement config",
			config.getContent() instanceof TableElement.Config);

		TableElement.Config tableConfig = (TableElement.Config) config.getContent();

		// Verify inputs.
		assertEquals("Should have one input", 1, tableConfig.getInputs().size());
		assertEquals("Input channel name", "testInput", tableConfig.getInputs().get(0).getChannelName());

		// Verify rows expression is present (non-null).
		assertNotNull("Rows expression should be set", tableConfig.getRows());

		// Verify selection channel.
		assertNotNull("Selection should be set", tableConfig.getSelection());
		assertEquals("Selection channel name", "selectedRow", tableConfig.getSelection().getChannelName());

		// Verify the table identity and the filter bar switch.
		assertEquals("Configured personalization key", "test-table", tableConfig.getPersonalizationKey());
		assertTrue("Filter bar should be switched on", tableConfig.getFilterBar());
	}

	/**
	 * Tests that the declared {@code <presets>} of a {@code <table>} are parsed with their labels
	 * and criteria.
	 */
	public void testParsePresets() throws Exception {
		TableElement.Config tableConfig = readTableConfig();

		PresetsConfig presetsConfig = tableConfig.getPresets();
		assertNotNull("Presets should be parsed", presetsConfig);
		List<PresetConfig> presets = presetsConfig.getPresets();
		assertEquals("Should have two presets", 2, presets.size());

		PresetConfig mine = presets.get(0);
		assertEquals("mine", mine.getName());
		assertEquals("Preset label", "My rows",
			((ResKey.LiteralKey) mine.getLabel()).getTranslationWithoutFallbacks(Locale.ENGLISH));

		List<CriterionConfig> criteria = mine.getCriteria();
		assertEquals("Should have two criteria", 2, criteria.size());
		assertEquals("owner", criteria.get(0).getColumn());
		assertNotNull("Criterion expression should be set", criteria.get(0).getExpr());
		assertEquals("active", criteria.get(1).getColumn());
		assertNotNull("Criterion expression should be set", criteria.get(1).getExpr());

		PresetConfig allActive = presets.get(1);
		assertEquals("all-active", allActive.getName());
		assertNull("A preset without a label declares none", allActive.getLabel());
		assertEquals("Should have one criterion", 1, allActive.getCriteria().size());
	}

	/**
	 * Tests that the configured {@link UIElement.Config#getPersonalizationKey() personalization
	 * key} is the table's identity, so that a personalization survives adding or removing a column.
	 */
	public void testConfiguredTableId() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(readTableConfig());
		context.checkErrors();

		assertEquals("key:test-table", element.tableId().value());
	}

	/**
	 * Tests that a table without a configured
	 * {@link UIElement.Config#getPersonalizationKey() personalization key} falls back to its
	 * structural signature: its row types and its column attributes.
	 */
	public void testStructuralTableId() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		tableConfig.update(
			tableConfig.descriptor().getProperty(UIElement.Config.PERSONALIZATION_KEY), null);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(tableConfig);
		context.checkErrors();

		assertEquals("demo.test:Row,|name,active,owner,", element.tableId().value());
	}

	/**
	 * The {@code <table>} configuration of the test fixture.
	 */
	private TableElement.Config readTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		return (TableElement.Config) config.getContent();
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTableElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTableElement.class, TypeIndex.Module.INSTANCE);
	}
}
