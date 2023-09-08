/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.declarative.mapping;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.config.misc.TypedConfigUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.declarative.mapping.TLPropertyAccess;
import com.top_logic.layout.form.declarative.mapping.TLPropertyAccess.Config;

/**
 * Tests for {@link TLPropertyAccess}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLPropertyAccess extends TestCase {

	public interface TestConfig extends ConfigurationItem {

		String INT = "int";

		String ITEM = "item";

		@Name(INT)
		int getInt();

		void setInt(int value);

		@Name(ITEM)
		TestConfig getItem();

		void setItem(TestConfig value);

	}

	public void testEmptyPath() {
		TLPropertyAccess tlPropertyAccess = createTLPropertyAccess(list(), true, false);
		assertEquals(null, tlPropertyAccess.apply(null));
		assertEquals("13", tlPropertyAccess.apply("13"));
	}

	public void testEmptyPathFailOnNull() {
		TLPropertyAccess tlPropertyAccess = createTLPropertyAccess(list(), false, false);
		assertEquals(null, tlPropertyAccess.apply(null));
	}

	public void testNavigateSinglePrimitiveProperty() {
		TLPropertyAccess tlPropertyAccess = createTLPropertyAccess(list(TestConfig.INT), true, false);
		TestConfig item = createTestConfig(123, null);
		assertEquals(null, tlPropertyAccess.apply(null));
		assertEquals(123, tlPropertyAccess.apply(item));
	}

	public void testNavigateSingleConfigProperty() {
		TLPropertyAccess tlPropertyAccess = createTLPropertyAccess(list(TestConfig.ITEM), true, false);
		TestConfig item1 = createTestConfig(123, null);
		assertEquals(null, tlPropertyAccess.apply(null));
		assertEquals(null, tlPropertyAccess.apply(item1));
		TestConfig item2 = createTestConfig(456, item1);
		ConfigurationItem actualItem = (ConfigurationItem) tlPropertyAccess.apply(item2);
		AbstractTypedConfigurationTestCase.assertEquals(item1, actualItem);
	}

	public void testNavigatePath() {
		List<String> path = list(TestConfig.ITEM, TestConfig.ITEM, TestConfig.INT);
		TLPropertyAccess tlPropertyAccess = createTLPropertyAccess(path, true, false);
		TestConfig item1 = createTestConfig(123, null);
		TestConfig item2 = createTestConfig(456, item1);
		TestConfig item3 = createTestConfig(789, item2);
		assertEquals(null, tlPropertyAccess.apply(null));
		assertEquals(null, tlPropertyAccess.apply(item1));
		assertEquals(null, tlPropertyAccess.apply(item2));
		assertEquals(123, tlPropertyAccess.apply(item3));
	}

	private TLPropertyAccess createTLPropertyAccess(List<String> property, boolean ignoreNull,
			boolean ignoreTypeError) {
		Config item = newConfigItem(TLPropertyAccess.Config.class);
		setProperty(item, TLPropertyAccess.Config.PROPERTY, property);
		setProperty(item, TLPropertyAccess.Config.IGNORE_NULL, ignoreNull);
		setProperty(item, TLPropertyAccess.Config.IGNORE_TYPE_ERROR, ignoreTypeError);
		TLPropertyAccess tlPropertyAccess = createInstance(item);
		return tlPropertyAccess;
	}

	private TestConfig createTestConfig(int integer, TestConfig innerItem) {
		TestConfig item = newConfigItem(TestConfig.class);
		item.setInt(integer);
		item.setItem(innerItem);
		return item;
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestTLPropertyAccess.class,
			TypeIndex.Module.INSTANCE));
	}

}
