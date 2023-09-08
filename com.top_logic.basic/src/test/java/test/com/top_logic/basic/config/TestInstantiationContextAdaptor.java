/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.InstantiationContextAdaptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for {@link InstantiationContextAdaptor}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestInstantiationContextAdaptor extends AbstractTypedConfigurationTestCase {

	private final class MyContext extends InstantiationContextAdaptor {
		public MyContext(InstantiationContext context) {
			super(context, context);
		}
	}

	public interface Config extends PolymorphicConfiguration<Impl> {
		// No additional options.
	}

	public static class Impl extends AbstractConfiguredInstance<Config> {
		public Impl(InstantiationContext context, Config config) {
			super(context, config);
			assertTrue(context instanceof MyContext);
		}
	}

	public void testCreate() {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		new MyContext(context).getInstance(config);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap("impl",
			TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

}
