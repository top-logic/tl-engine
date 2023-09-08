/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.SharedInstance;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.equal.ConfigEquality;

/**
 * Test case for {@link PolymorphicConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestPolymorphicConfiguration extends AbstractTypedConfigurationTestCase {

	public abstract static class A implements ConfiguredInstance<A.Config<?>> {
		
		private final Config<?> config;

		public interface Config<T extends A> extends PolymorphicConfiguration<T> {
			
			/**
			 * {@link ConfiguredInstance} property
			 */
			A getLeft();

			void setLeft(A value);
			
			A getRight();

			void setRight(A value);
			
		}
		
		/**
		 * Creates a {@link TestPolymorphicConfiguration.A} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 * @throws ConfigurationException
		 *         If the configuration is invalid.
		 */
		@CalledByReflection
		public A(InstantiationContext context, Config<?> config) throws ConfigurationException {
			this.config = config;
		}
		
		@Override
		public Config<?> getConfig() {
			return config;
		}

	}
	
	public static class B extends A {

		public interface Config extends A.Config<B> {
			@Override
			@ClassDefault(B.class)
			public Class<? extends B> getImplementationClass();

			int getBValue();
			void setBValue(int value);
			
		}
		
		public B(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}
		
	}
	
	@SharedInstance
	public static class C extends B {

		public interface Config extends B.Config {

			@Override
			@ClassDefault(C.class)
			Class<? extends C> getImplementationClass();
			
			int getCValue();
			void setCValue(int value);
			
		}
		
		public C(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}
		
	}
	
	public static class D extends C {
		
		public interface Config extends C.Config {
			
			int getDValue();
			void setDValue(int value);
			
		}
		
		public D(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj != null && obj.getClass() == D.class
				&& ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(((D) obj).getConfig(), getConfig());
		}

		@Override
		public int hashCode() {
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.hashCode(getConfig());
		}
	}

	/**
	 * Configured class that throws an arbitrary exception in its configuration constructor.
	 */
	public static class Ex extends A {

		static final int ERROR = -2;

		static final int RUNTIME_EXCEPTION = -1;

		static final int CUSTOM_EXCEPION = 0;

		static final int NO_EXCEPTION = 1;

		static class CustomException extends Exception {
			public CustomException() {
				super();
			}
		}

		public interface Config extends A.Config<Ex> {
			@Override
			@ClassDefault(Ex.class)
			public Class<? extends Ex> getImplementationClass();

			int getCriticalValue();

			void setCriticalValue(int value);
		}

		/**
		 * Creates a {@link Ex} from configuration.
		 */
		public Ex(InstantiationContext context, Config config) throws ConfigurationException, CustomException {
			super(context, config);
			if (config.getCriticalValue() == CUSTOM_EXCEPION) {
				throw new CustomException();
			}
			if (config.getCriticalValue() == RUNTIME_EXCEPTION) {
				throw new RuntimeException();
			}
			if (config.getCriticalValue() == ERROR) {
				throw new Error();
			}
		}
	}

	public void testDefaultImplementationClass() {
		A.Config<?> aConfig = TypedConfiguration.newConfigItem(A.Config.class);
		assertEquals(A.class, aConfig.getImplementationClass());
	}

	public void testConfigConstructorWithDeclaredCustomException() throws ConfigurationException {
		Ex.Config validConfig = TypedConfiguration.newConfigItem(Ex.Config.class);
		validConfig.setCriticalValue(Ex.NO_EXCEPTION);

		Ex validInstance = context.getInstance(validConfig);
		assertNotNull(validInstance);
		context.checkErrors();
	}

	public void testFailingConfigConstructorWithCustomException() {
		Ex.Config invalidConfig = TypedConfiguration.newConfigItem(Ex.Config.class);
		invalidConfig.setCriticalValue(Ex.CUSTOM_EXCEPION);

		assertFailingInstantiation(invalidConfig);
	}

	public void testFailingConfigConstructorWithRuntimeException() {
		Ex.Config invalidConfig = TypedConfiguration.newConfigItem(Ex.Config.class);
		invalidConfig.setCriticalValue(Ex.RUNTIME_EXCEPTION);

		assertFailingInstantiation(invalidConfig);
	}

	private void assertFailingInstantiation(Ex.Config invalidConfig) {
		InstantiationContext failingContext = new DefaultInstantiationContext(new ExpectedFailureProtocol());
		try {
			Ex invalidInstance = failingContext.getInstance(invalidConfig);
			assertNull("Instantiation must not succeed.", invalidInstance);
			failingContext.checkErrors();
			fail("Instantiation must report error.");
		} catch (ExpectedFailure ex) {
			// Ignore.
		} catch (ConfigurationException ex) {
			// Ignore.
		}
	}

	public void testFailingConfigConstructorWithError() {
		Ex.Config invalidConfig = TypedConfiguration.newConfigItem(Ex.Config.class);
		invalidConfig.setCriticalValue(Ex.ERROR);

		InstantiationContext failingContext = new DefaultInstantiationContext(new ExpectedFailureProtocol());
		try {
			failingContext.getInstance(invalidConfig);
			fail("Instantiation must report error.");
		} catch (Error ex) {
			// Ignore.
		}
	}

	public void testStoreLoadDefaultImplementation() throws XMLStreamException {
		B.Config bConfig = newB();
		bConfig.setBValue(42);

		// Using default value: bConfig.setImplementationClass(B.class);

		Object bInstance = context.getInstance(bConfig);
		assertTrue(bInstance instanceof B);
		assertEquals(bConfig, ((B) bInstance).getConfig());
		
		ConfigurationItem bConfigRestored = throughXML(bConfig);
		
		assertTrue(Arrays.toString(bConfigRestored.getClass().getInterfaces()), bConfigRestored instanceof B.Config);
		assertEquals(bConfig, bConfigRestored);

		Object bInstanceRestored = context.getInstance((B.Config) bConfigRestored);
		assertTrue(bInstanceRestored instanceof B);
		
		assertEquals(bConfigRestored, ((B) bInstanceRestored).getConfig());
	}
	
	public void testStoreLoadOverriddenDefaultImplementation() throws XMLStreamException {
		C.Config cConfig = newC();
		cConfig.setBValue(42);
		cConfig.setCValue(42);
		
		// Using default value: cConfig.setImplementationClass(C.class);
		
		Object cInstance = context.getInstance(cConfig);
		assertTrue(cInstance instanceof C);
		assertEquals(cConfig, ((C) cInstance).getConfig());
		
		ConfigurationItem cConfigRestored = throughXML(cConfig);
		
		assertTrue(Arrays.toString(cConfigRestored.getClass().getInterfaces()), cConfigRestored instanceof C.Config);
		assertEquals(cConfig, cConfigRestored);
		
		Object cInstanceRestored = context.getInstance((C.Config) cConfigRestored);
		assertTrue(cInstanceRestored instanceof C);
		
		assertEquals(cConfigRestored, ((C) cInstanceRestored).getConfig());
	}
	
	public void testStoreLoadConcreteConfiguration() throws XMLStreamException {
		D.Config dConfig = newD();
		dConfig.setBValue(42);
		dConfig.setCValue(13);
		dConfig.setDValue(99);
		
		// Using default value: dConfig.setImplementationClass(D.class);
		
		Object cInstance = context.getInstance(dConfig);
		assertTrue(cInstance instanceof C);
		assertEquals(dConfig, ((C) cInstance).getConfig());
		
		ConfigurationItem dConfigRestored = throughXML(dConfig);
		
		assertTrue(Arrays.toString(dConfigRestored.getClass().getInterfaces()), dConfigRestored instanceof D.Config);
		assertEquals(dConfig, dConfigRestored);
		
		Object cInstanceRestored = context.getInstance((D.Config) dConfigRestored);
		assertTrue(cInstanceRestored instanceof C);
		
		assertEquals(dConfigRestored, ((C) cInstanceRestored).getConfig());
	}
	
	public void testStoreLoadConfigurationWithMoreGenericImplementation() throws XMLStreamException {
		D.Config dConfig = newD();
		dConfig.setBValue(42);
		dConfig.setCValue(13);
		dConfig.setDValue(99);
		
		// Use more specific configuration than required by implementation class.
		dConfig.setImplementationClass(B.class);
		
		Object bInstance = context.getInstance(dConfig);
		assertTrue(bInstance instanceof B);
		assertEquals(dConfig, ((B) bInstance).getConfig());
		
		ConfigurationItem dConfigRestored = throughXML(dConfig);
		
		assertTrue(Arrays.toString(dConfigRestored.getClass().getInterfaces()), dConfigRestored instanceof D.Config);
		assertEquals(dConfig, dConfigRestored);
		
		Object bInstanceRestored = context.getInstance((D.Config) dConfigRestored);
		assertTrue(bInstanceRestored instanceof B);
		
		assertEquals(dConfigRestored, ((B) bInstanceRestored).getConfig());
	}

	public void testConfigurationInstanceMix() throws XMLStreamException {
		B.Config b = newB();
		b.setBValue(1);

		C.Config cConfig = newC();
		cConfig.setCValue(2);
		B c = context.getInstance(cConfig);
		BasicTestCase.assertInstanceof(c, C.class);
		b.setLeft(c);

		D.Config dConfig = newD();
		dConfig.setImplementationClass(D.class);
		dConfig.setDValue(3);
		B d = context.getInstance(dConfig);
		BasicTestCase.assertInstanceof(d, D.class);
		b.setRight(d);
		
		B.Config bRestored = (B.Config) throughXML(b);
		
		assertEquals(b, bRestored);
	}
	
	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		ConfigurationDescriptor aConfigDescriptor = TypedConfiguration.getConfigurationDescriptor(A.Config.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("config", aConfigDescriptor);
		return descriptors;
	}
	
	protected static test.com.top_logic.basic.config.TestPolymorphicConfiguration.B.Config newB() {
		return TypedConfiguration.newConfigItem(B.Config.class);
	}
	
	protected static test.com.top_logic.basic.config.TestPolymorphicConfiguration.C.Config newC() {
		return TypedConfiguration.newConfigItem(C.Config.class);
	}
	
	protected static test.com.top_logic.basic.config.TestPolymorphicConfiguration.D.Config newD() {
		return TypedConfiguration.newConfigItem(D.Config.class);
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestPolymorphicConfiguration.class));
	}

}
