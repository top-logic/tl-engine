/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.equal;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.misc.TypedConfigUtil;

/**
 * Test of {@link ConfigEquality} for properties holding a
 * {@link com.top_logic.basic.config.ConfiguredInstance}, which are compared by their
 * configuration.
 */
@SuppressWarnings("javadoc")
public class TestConfigEqualityConfiguredInstance extends AbstractTypedConfigurationTestCase {

	/**
	 * Instance without value equality of its own, configured by a name.
	 */
	public static class Part extends AbstractConfiguredInstance<Part.Config<?>> {

		public interface Config<I extends Part> extends PolymorphicConfiguration<I> {

			String NAME = "name";

			@Name(NAME)
			String getName();

			void setName(String value);

		}

		public Part(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

	}

	public interface HolderConfig extends ConfigurationItem {

		String PART = "part";

		@Name(PART)
		@InstanceFormat
		Part getPart();

		void setPart(Part value);

	}

	public void testEqualConfigsAreEqual() {
		HolderConfig left = holder("a");
		HolderConfig right = holder("a");

		assertNotSame(left.getPart(), right.getPart());
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(right, left));
		assertEquals(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left),
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(right));
	}

	public void testDifferentConfigsAreNotEqual() {
		HolderConfig left = holder("a");
		HolderConfig right = holder("b");

		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(right, left));
	}

	public void testInstanceWithoutConfig() {
		HolderConfig left = TypedConfiguration.newConfigItem(HolderConfig.class);
		left.setPart(new Part(null, null));
		HolderConfig right = TypedConfiguration.newConfigItem(HolderConfig.class);
		right.setPart(new Part(null, null));

		// Instances without configuration are compared and hashed as instances.
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertFalse(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, holder("a")));
		assertEquals(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left),
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left));

		right.setPart(left.getPart());
		assertTrue(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right));
		assertEquals(ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(left),
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(right));
	}

	private static HolderConfig holder(String partName) {
		@SuppressWarnings("unchecked")
		Part.Config<Part> partConfig = TypedConfiguration.newConfigItem(Part.Config.class);
		partConfig.setImplementationClass(Part.class);
		partConfig.setName(partName);

		HolderConfig result = TypedConfiguration.newConfigItem(HolderConfig.class);
		result.setPart(TypedConfigUtil.createInstance(partConfig));
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("config", TypedConfiguration.getConfigurationDescriptor(HolderConfig.class));
	}

	/**
	 * A cumulative {@link Test} for all tests in {@link TestConfigEqualityConfiguredInstance}.
	 */
	public static Test suite() {
		return suite(TestConfigEqualityConfiguredInstance.class);
	}

}
