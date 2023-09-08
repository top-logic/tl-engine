/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.order;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.basic.config.order.DefaultOrderStrategy.Collector;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Test for {@link Collector}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultOrderStrategyCollector extends BasicTestCase {

	@DisplayOrder({
		A.STRING
	})
	public interface A extends ConfigurationItem {

		String STRING1 = "string1";

		@Name(STRING1)
		String getString1();

		String STRING = "string";

		@Name(STRING)
		String getString();

	}

	public interface B extends A {

		String STRING2 = "string2";

		@Name(STRING2)
		String getString2();

	}

	public interface AExt extends A {

		@Override
		@StringDefault("s1")
		String getString1();

	}

	@DisplayOrder({
		AExtB.STRING2
	})
	public interface AExtB extends B, AExt {
		// Sum interface
	}

	private static PropertyDescriptor property(Class<? extends ConfigurationItem> intf, String name) {
		return TypedConfiguration.getConfigurationDescriptor(intf).getProperty(name);

	}

	public void testCollectUnorededProperties() {
		ConfigurationDescriptor configDescriptor = TypedConfiguration.getConfigurationDescriptor(AExtB.class);
		Collector collector = new Collector(NoCustomizations.INSTANCE, configDescriptor);
		List<PropertyDescriptor> properties = collector.collect();
		assertEquals(list(property(AExtB.class, AExtB.STRING), property(AExtB.class, AExtB.STRING2)), properties);

		collector = new Collector(NoCustomizations.INSTANCE, configDescriptor) {
			@Override
			protected boolean collectUnorderedProperties() {
				return true;
			}
		};

		properties = collector.collect();
		assertEquals(list(property(AExtB.class, AExtB.STRING), property(AExtB.class, AExtB.STRING1),
			property(AExtB.class, AExtB.STRING2)), properties);
	}


	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestDefaultOrderStrategyCollector}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestDefaultOrderStrategyCollector.class);
	}


}

