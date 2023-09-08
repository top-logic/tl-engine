/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Test case for {@link ListBinding} annotations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestListBinding extends AbstractTypedConfigurationTestCase {

	public interface Config extends ConfigurationItem {

		@ListBinding(tag = "str", attribute = "val")
		List<String> getStringList();

		@ListBinding(tag = "int", attribute = "val")
		List<Integer> getIntList();

		@ListBinding(tag = "str", attribute = "val")
		String[] getStringArray();

		@ListBinding(tag = "int", attribute = "val", format = TestingFormat.class)
		int[] getIntArray();

		class TestingFormat extends AbstractConfigurationValueProvider<Integer> {

			public TestingFormat() {
				super(Integer.class);
			}

			@Override
			public Integer defaultValue() {
				return Integer.valueOf(0);
			}

			@Override
			protected Integer getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String value = propertyValue.toString();

				return value.equals("thirteen") ? 13 : Integer.parseInt(value);
			}

			@Override
			protected String getSpecificationNonNull(Integer configValue) {
				return configValue.intValue() == 13 ? "thirteen" : Integer.toString(configValue.intValue());
			}

		}
	}

	public void testStringList() throws ConfigurationException {
		Config item = read("<config><string-list><str val='x'/><str val='y'/></string-list></config>");
		assertEquals(list("x", "y"), item.getStringList());
	}

	public void testIntList() throws ConfigurationException {
		Config item = read("<config><int-list><int val='42'/><int val='13'/></int-list></config>");
		assertEquals(list(42, 13), item.getIntList());
	}

	public void testStringArray() throws ConfigurationException {
		Config item = read("<config><string-array><str val='x'/><str val='y'/></string-array></config>");
		assertEquals(list("x", "y"), Arrays.asList(item.getStringArray()));
	}

	public void testIntArray() throws ConfigurationException {
		Config item = read("<config><int-array><int val='42'/><int val='thirteen'/></int-array></config>");
		assertEquals(2, item.getIntArray().length);
		assertEquals(42, item.getIntArray()[0]);
		assertEquals(13, item.getIntArray()[1]);
	}

	public void testListAddDuplicates() throws Throwable {
		Config item = read(
			"<config><string-list><str val='a' /><str val='b' /><str val='b' /><str val='c' /></string-list></config>");
		assertEquals(list("a", "b", "b", "c"), item.getStringList());
	}

	public void testListAdd() throws Throwable {
		String base = "<config><string-list><str val='a' /><str val='b' /><str val='c' /></string-list></config>";
		String inc = "<config><string-list><str val='b' /><str val='d' /></string-list></config>";

		Config item = (Config) new ConfigurationReader(context, getDescriptors()).setSources(
			CharacterContents.newContent(base),
			CharacterContents.newContent(inc)).read();
		assertEquals(list("a", "b", "c", "b", "d"), item.getStringList());
	}

	public void testListAddWithPosition() throws Throwable {
		Config item = (Config) readConfigurationStacked("values.xml", "add.xml");
		assertEquals(list("begin", "a", "before", "b", "after", "c", "d", "e", "f", "new", "end"),
			item.getStringList());
	}

	public void testListUpdate() throws Throwable {
		Config item = (Config) readConfigurationStacked("values.xml", "update.xml");
		assertEquals(list("c", "a", "d", "e", "f", "b"), item.getStringList());
	}

	public void testListAddOrUpdate() throws Throwable {
		Config item = (Config) readConfigurationStacked("values.xml", "addOrUpdate.xml");
		assertEquals(list("y", "a", "z", "c", "d", "e", "f", "x", "b"), item.getStringList());
	}

	public void testListRemove() throws Throwable {
		Config item = (Config) readConfigurationStacked("values.xml", "remove.xml");
		assertEquals(list("a", "b", "d", "f"), item.getStringList());
	}

	public void testtInvalidUpdateObject() {
		try {
			readConfigurationStacked("values.xml", "invalid-update.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The update operation object 'x' could not be found", ex.getMessage());
		}
	}

	public void testtInvalidUpdatePosition() {
		try {
			readConfigurationStacked("values.xml", "invalid-update-position.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("No new position was given for the object 'a' to update", ex.getMessage());
		}
	}

	public void testInvalidRemove() {
		try {
			readConfigurationStacked("values.xml", "invalid-remove.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The value 'x' does not exist", ex.getMessage());
		}
	}

	public void testInvalidReference() {
		try {
			readConfigurationStacked("values.xml", "invalid-reference.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("The reference object 'invalid' could not be found", ex.getMessage());
		}
	}

	public void testInvalidOperation() {
		try {
			readConfigurationStacked("values.xml", "invalid-operation.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains(
				"Invalid value 'invalid', expected is one of 'addOrUpdate, add, remove, update'", ex.getMessage());
		}
	}

	public void testInvalidPosition() {
		try {
			readConfigurationStacked("values.xml", "invalid-position.xml");
		} catch (Throwable ex) {
			BasicTestCase.assertContains("Invalid value 'invalid', expected is one of 'begin, end, before, after'",
				ex.getMessage());
		}
	}


	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestListBinding.class);
	}
}
