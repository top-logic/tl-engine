/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationResolver;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Test case for {@link ConfigurationResolver}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestImplementationFactory extends BasicTestCase {

	public static class A implements ConfiguredInstance<A.Config> {
		private final Config config;
		private final B b;
		
		public A(InstantiationContext context, Config config) {
			this.b = context.getInstance(config.getBConfig());
			this.config = config;
		}
		
		@Override
		public Config getConfig() {
			return config;
		}
		
		public B getB() {
			return b;
		}
		
		public interface Config extends PolymorphicConfiguration<A> {
			
			@Override
			@ClassDefault(A.class)
			Class getImplementationClass();
			
			B.Config getBConfig();
			void setBConfig(B.Config bConfig);
			
		}
		
	}

	public static class B implements ConfiguredInstance<B.Config> {
		private final Config config;
		
		public B(InstantiationContext context, Config config) {
			this.config = config;
		}
		
		@Override
		public Config getConfig() {
			return config;
		}
		
		public interface Config extends PolymorphicConfiguration<B> {
			
			@Override
			@ClassDefault(B.class)
			Class getImplementationClass();
			
		}
		
	}
	
	public void testInstantiationSingle() {
		A a =
			(A) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(TypedConfiguration
				.newConfigItem(A.Config.class));
		
		// There is no configuration for B.
		assertNull(a.getB());
	}
	
	public void testInstantiationComposite() {
		A.Config aConfig = TypedConfiguration.newConfigItem(A.Config.class);
		aConfig.setBConfig(TypedConfiguration.newConfigItem(B.Config.class));
		
		A a = (A) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(aConfig);
		
		assertNotNull(a.getB());
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestImplementationFactory.class));
	}

}
