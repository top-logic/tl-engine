/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.annotation;

import junit.framework.TestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.defaults.IntDefault;

/**
 * Test case for the {@link SharedInstance} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSharedInstance extends TestCase {
	
	@SharedInstance
	public static class SharedImpl {

		public interface Config extends PolymorphicConfiguration<SharedImpl> {
			@IntDefault(2)
			int getValue();

			void setValue(int value);
		}
		
		/**
		 * Creates a {@link TestSharedInstance.SharedImpl} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public SharedImpl(InstantiationContext context, Config config) {
			// Dummy.
		}
		
	}

	public static class NonSharedImpl extends SharedImpl {

		public NonSharedImpl(InstantiationContext context, Config config) {
			super(context, config);
		}

	}

	public void testSharing() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSharedInstance.class);

		SharedImpl.Config config1 = newSharedConfig();
		config1.setValue(2);

		SharedImpl.Config config2 = newSharedConfig();
		config2.setValue(2);

		SharedImpl.Config config3 = newSharedConfig();

		SharedImpl.Config config4 = newSharedConfig();
		config4.setValue(7);

		SharedImpl instance = context.getInstance(config1);
		SharedImpl instanceSameConfig = context.getInstance(config1);
		SharedImpl instanceEqualConfig = context.getInstance(config2);
		SharedImpl instanceEqualDefaultConfig = context.getInstance(config3);
		SharedImpl instanceOtherConfig = context.getInstance(config4);

		assertSame(instance, instanceSameConfig);
		assertSame(instance, instanceEqualConfig);
		assertSame(instance, instanceEqualDefaultConfig);
		assertNotSame(instance, instanceOtherConfig);
	}

	private SharedImpl.Config newSharedConfig() {
		SharedImpl.Config result = TypedConfiguration.newConfigItem(SharedImpl.Config.class);
		result.setImplementationClass(SharedImpl.class);
		return result;
	}

	public void testNoSharing() {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSharedInstance.class);

		SharedImpl.Config config1 = newNonSharedConfig();
		config1.setValue(2);

		SharedImpl.Config config2 = newNonSharedConfig();
		config2.setValue(2);

		SharedImpl.Config config3 = newNonSharedConfig();

		SharedImpl.Config config4 = newNonSharedConfig();
		config4.setValue(7);

		SharedImpl instance = context.getInstance(config1);
		SharedImpl instanceSameConfig = context.getInstance(config1);
		SharedImpl instanceEqualConfig = context.getInstance(config2);
		SharedImpl instanceEqualDefaultConfig = context.getInstance(config3);
		SharedImpl instanceOtherConfig = context.getInstance(config4);

		assertNotSame(instance, instanceSameConfig);
		assertNotSame(instance, instanceEqualConfig);
		assertNotSame(instance, instanceEqualDefaultConfig);
		assertNotSame(instance, instanceOtherConfig);
	}

	private SharedImpl.Config newNonSharedConfig() {
		SharedImpl.Config result = TypedConfiguration.newConfigItem(SharedImpl.Config.class);
		result.setImplementationClass(NonSharedImpl.class);
		return result;
	}

}
