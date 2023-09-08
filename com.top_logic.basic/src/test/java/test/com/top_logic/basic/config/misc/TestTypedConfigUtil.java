/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.misc;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.misc.TypedConfigUtil;

/**
 * Test for {@link TypedConfigUtil}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigUtil extends AbstractTypedConfigurationTestCase {

	public interface TestReplacement extends TestValue {

		List<TestValue> getList();

		TestValue getItem();

		void setItem(TestValue item);

		TestValue[] getArray();

		void setArray(TestValue... array);

		@Key(TestReplacement.NAME_ATTRIBUTE)
		Map<String, TestValue> getMap();
	}

	public interface TestValue extends NamedConfigMandatory {
		// Pure marker.
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public void testReplacement() {
		TestReplacement base = newConfigItem(TestReplacement.class);
		base.setName("base");
		TestValue item1 = newNamedConfiguration("name1");
		TestValue item2 = newNamedConfiguration("name2");
		TestValue item3 = newNamedConfiguration("name3");
		TestValue item4 = newNamedConfiguration("name4");
		TestValue item5 = newNamedConfiguration("name5");

		base.getList().add(item1);
		base.getList().add(newNamedConfiguration("replace"));
		base.getList().add(item2);

		base.setItem(newNamedConfiguration("replace"));

		base.setArray(newNamedConfiguration("replace"), item3, item4);

		base.getMap().put("replace", newNamedConfiguration("replace"));
		base.getMap().put(item5.getName(), item5);

		ConfigurationItem replacement = TypedConfigUtil.replace(base, item -> {
			if (!"replace".equals(((NamedConfiguration) item).getName())) {
				return item;
			}
			TestReplacement tmp = newConfigItem(TestReplacement.class);
			tmp.setName("replace");
			return tmp;
		});
		assertSame(base, replacement);
		assertEquals(item1, base.getList().get(0));
		assertTrue(base.getList().get(1) instanceof TestReplacement);
		assertEquals(item2, base.getList().get(2));

		assertTrue(base.getItem() instanceof TestReplacement);

		assertTrue(base.getArray()[0] instanceof TestReplacement);
		assertEquals(item3, base.getArray()[1]);
		assertEquals(item4, base.getArray()[2]);

		assertTrue(base.getMap().get("replace") instanceof TestReplacement);
		assertEquals(item5, base.getMap().get(item5.getName()));
	}

	public void testRecursiveReplace() {
		TestReplacement item1 = newConfigItem(TestReplacement.class);
		item1.setName("item1");

		TestReplacement item2 = newConfigItem(TestReplacement.class);
		item2.setName("item2");
		TestReplacement item3 = newConfigItem(TestReplacement.class);
		item3.setName("item3");
		item2.setItem(item3);
		TestReplacement item4 = newConfigItem(TestReplacement.class);
		item4.setName("item4");
		ConfigurationItem replacement = TypedConfigUtil.replace(item1, item -> {
			if (item == item1) {
				return item2;
			}
			if (item == item3) {
				return item4;
			}
			return item;
		});
		assertSame(replacement, item2);
		assertSame(item4, ((TestReplacement) replacement).getItem());
	}

	private TestValue newNamedConfiguration(String name) {
		TestValue conf = newConfigItem(TestValue.class);
		conf.setName(name);
		return conf;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestTypedConfigUtil}.
	 */
	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigUtil.class);
	}

}
