/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.TestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * Test case for {@link SimpleInstantiationContext}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSimpleInstantiationContext extends TestCase {

	public static class A {
		
		public interface Config extends PolymorphicConfiguration<A> {
			boolean getFail();

			void setFail(boolean value);
		}
		
		/**
		 * Creates a {@link TestSimpleInstantiationContext.A} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public A(InstantiationContext context, Config config) {
			if (config.getFail()) {
				throw new RuntimeException("Tested failure.");
			}
		}

	}
	
	public void testSuccess() {
		A.Config config = TypedConfiguration.newConfigItem(A.Config.class);
		config.setImplementationClass(A.class);
		A a1 = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		assertNotNull(a1);
	}
	
	public void testFail() {
		A.Config config = TypedConfiguration.newConfigItem(A.Config.class);
		config.setImplementationClass(A.class);
		config.setFail(true);
		try {
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
			fail("Direct failure expected.");
		} catch (ConfigurationError ex) {
			// Expected.
		}

		// Problems must not be reported after failure.
		assertFalse(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.hasErrors());
		assertNull(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getFirstProblem());
	}

}
