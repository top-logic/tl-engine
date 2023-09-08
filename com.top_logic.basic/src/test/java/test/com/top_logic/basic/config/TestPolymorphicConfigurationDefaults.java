/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.A.AConfig;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.B.BConfig;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.C.C1;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.C.C1.C1Config;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.C.CConfig;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.D.DConfig;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.E.E1;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.E.E1.E1Config;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.E.EUsage;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.F.F1;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.F.F2;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.F.F2.F2Config;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.F.F3.F3Config;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.F.FConfig;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.G.G1.G1Config;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.G.GUsage;
import test.com.top_logic.basic.config.TestPolymorphicConfigurationDefaults.I.IConfig;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Test case for the automatic default value generation for
 * {@link PolymorphicConfiguration#getImplementationClass()} from the type parameter of
 * {@link PolymorphicConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestPolymorphicConfigurationDefaults extends AbstractTypedConfigurationTestCase {

	public static class A {
		public interface AConfig extends PolymorphicConfiguration<A> {
			// No options.
		}
	}

	private static final String NL = "\n";
	
	public void testDefaultFromBoundVariable() {
		assertEquals(A.class, TypedConfiguration.newConfigItem(AConfig.class).getImplementationClass());
	}
	
	public static class B {
		@SuppressWarnings("rawtypes")
		public interface BConfig extends PolymorphicConfiguration/* raw type! */{
			// No options.
		}
	}

	public void testDefaultFromUnboundRawType() {
		assertEquals(Object.class, TypedConfiguration.newConfigItem(BConfig.class).getImplementationClass());
	}

	public static class C {
		public interface CConfig<T extends C> extends PolymorphicConfiguration<T> {
			// No options.
		}

		public static class C1 extends C {
			public interface C1Config<T extends C1> extends CConfig<T> {
				// No options.
			}
		}
	}

	public void testDefaultFromUnboundTypeVariable() {
		assertEquals(C.class, TypedConfiguration.newConfigItem(CConfig.class).getImplementationClass());
		assertEquals(C1.class, TypedConfiguration.newConfigItem(C1Config.class).getImplementationClass());
	}

	public static class D {
		public interface D1Config extends ConfigurationItem {
			// No options.
		}

		public interface D2Config extends PolymorphicConfiguration<D> {
			// No options.
		}

		public interface D3Config extends ConfigurationItem {
			// No options.
		}

		public interface DConfig extends D1Config, D2Config, D3Config {
			// No options.
		}
	}

	public void testDefaultFromMultipleSuperInterfaces() {
		assertEquals(D.class, TypedConfiguration.newConfigItem(DConfig.class).getImplementationClass());
	}

	public static class E {
		public interface EUsage extends ConfigurationItem {
			PolymorphicConfiguration<E> getGenericE();

			PolymorphicConfiguration<E1> getGenericE1();

			EConfig getE();

			/**
			 * Problematic definition, since the default implementation class ({@link E}) results in
			 * an invalid configuration type ({@link EConfig} instead of {@link E1Config}).
			 */
			E1Config getE1();

			@Name("generic-e-list")
			List<PolymorphicConfiguration<E>> getGenericEList();

			@Name("generic-e1-list")
			List<PolymorphicConfiguration<E1>> getGenericE1List();

			@Name("e-list")
			List<EConfig> getEList();

			@Name("e1-list")
			List<E1Config> getE1List();

			@Name("generic-e-map")
			@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
			Map<Class<?>, PolymorphicConfiguration<E>> getGenericEMap();

			@Name("generic-e1-map")
			@Key(PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME)
			Map<Class<?>, PolymorphicConfiguration<E1>> getGenericE1Map();

			@Name("e-map")
			@Key(EConfig.X)
			Map<String, EConfig> getEMap();

			@Name("e1-map")
			@Key(EConfig.X)
			Map<String, E1Config> getE1Map();
		}

		public interface EConfig extends PolymorphicConfiguration<E> {
			String X = "x";

			@Name(X)
			String getX();
		}

		@CalledByReflection
		public E(InstantiationContext context, EConfig config) {
			super();
		}

		public static class E1 extends E {

			public interface E1Config extends EConfig {
				String getY();
			}

			@CalledByReflection
			public E1(InstantiationContext context, E1Config config) {
				super(context, config);
			}

		}
	}

	public void testDefaultInParsing() throws ConfigurationException {
		String source = "<u>" + NL +
			"<generic-e x='x'/>" + NL +
			"<generic-e1 x='x' y='y'/>" + NL +
			"<e x='x'/>" + NL +
			// Class attribute required due to problematic definition of
			// configuration interface E1Config (not declaring E1 as default implementation class).
			"<e1 class='" + E1.class.getName() + "' x='x' y='y'/>" + NL +
			
			"<generic-e-list>" + NL + (
				"<entry x='x'/>" + NL +
				// In a list expecting E entries, an E1 subtype entry can be added explicitly.
				"<entry class='" + E1.class.getName() + "' x='x' y='y'/>") + NL +
			"</generic-e-list>" + NL +
			"<generic-e1-list>" + NL + (
				// In a list expecting E1 entries, E1 entries are created implicitly.
				"<entry x='x'/>" + NL +
				"<entry x='x' y='y'/>") + NL +
			"</generic-e1-list>" + NL +
			"<e-list>" + NL + (
				"<entry x='x'/>" + NL +
				// In a list expecting E entries, an E1 subtype entry can be added explicitly.
				"<entry class='" + E1.class.getName() + "' x='x' y='y'/>") + NL +
			"</e-list>" + NL +
			"<e1-list>" + NL + (
				// Class attribute required for all entries due to problematic definition of
				// configuration interface E1Config (not declaring E1 as default implementation class).
				"<entry class='" + E1.class.getName() + "' x='x'/>" + NL +
				"<entry class='" + E1.class.getName() + "' x='x' y='y'/>") + NL +
			"</e1-list>" + NL +
			
			"<generic-e-map>" + NL + (
				"<entry x='x'/>" + NL +
				// In a list expecting E entries, an E1 subtype entry can be added explicitly.
				"<entry class='" + E1.class.getName() + "' x='x' y='y'/>") + NL +
			"</generic-e-map>" + NL +
			"<generic-e1-map>" + NL + (
				// In a list expecting E1 entries, E1 entries are created implicitly.
				"<entry x='x' y='y'/>") + NL +
			"</generic-e1-map>" + NL +
			"<e-map>" + NL + (
				"<entry x='x1'/>" + NL +
				// In a list expecting E entries, an E1 subtype entry can be added explicitly.
				"<entry class='" + E1.class.getName() + "' x='x2' y='y'/>") + NL +
			"</e-map>" + NL +
			"<e1-map>" + NL + (
				// Class attribute required for all entries due to problematic definition of
				// configuration interface E1Config (not declaring E1 as default implementation class).
				"<entry class='" + E1.class.getName() + "' x='x1'/>" + NL +
				"<entry class='" + E1.class.getName() + "' x='x2' y='y'/>") + NL +
			"</e1-map>" + NL +
			"</u>";

		InstantiationContext context = new DefaultInstantiationContext(new AssertProtocol("Ticket #10479"));
		EUsage usage = parseEUsage(source);

		assertEquals(E.class, usage.getE().getImplementationClass());
		assertEquals(E1.class, usage.getE1().getImplementationClass());
		assertEquals(E.class, usage.getGenericE().getImplementationClass());
		assertEquals(E1.class, usage.getGenericE1().getImplementationClass());
		assertEquals("x", usage.getE().getX());
		assertEquals("x", usage.getE1().getX());
		assertEquals("y", usage.getE1().getY());

		assertEquals(usage.getE(), usage.getGenericE());
		assertEquals(usage.getE1(), usage.getGenericE1());

		assertEquals(E.class, context.getInstance(usage.getE()).getClass());
		assertEquals(E.class, context.getInstance(usage.getGenericE()).getClass());
		assertEquals(E1.class, context.getInstance(usage.getE1()).getClass());
		assertEquals(E1.class, context.getInstance(usage.getGenericE1()).getClass());
	}

	private EUsage parseEUsage(String source) throws ConfigurationException {
		InstantiationContext context = new DefaultInstantiationContext(new AssertProtocol("Ticket #10479"));
		Map<String, ConfigurationDescriptor> descriptorsByTag =
			Collections.singletonMap("u", TypedConfiguration.getConfigurationDescriptor(EUsage.class));
		CharacterContent content = CharacterContents.newContent(source);
		return (EUsage) new ConfigurationReader(context, descriptorsByTag).setSource(content).read();
	}

	public static class F {
		public interface FConfig extends PolymorphicConfiguration<F> {
			@ClassDefault(F1.class)
			@Override
			public Class<? extends F> getImplementationClass();
		}
		
		public F(InstantiationContext context, FConfig config) {
			super();
		}

		public static class F1 extends F {
			@CalledByReflection
			public F1(InstantiationContext context, FConfig config) {
				super(context, config);
			}
		}

		public static class F2 extends F {
			public interface F2Config extends FConfig {
				@ClassDefault(F2.class)
				@Override
				public Class<? extends F> getImplementationClass();
			}

			@CalledByReflection
			public F2(InstantiationContext context, F2Config config) {
				super(context, config);
			}
		}

		public static class F3 extends F2 {
			public interface F3Config extends F2Config {
				// No options.
			}

			@CalledByReflection
			public F3(InstantiationContext context, F3Config config) {
				super(context, config);
			}
		}
	}
	
	public void testImplementationClassDefaultAnnotation() {
		FConfig fConfig = TypedConfiguration.newConfigItem(FConfig.class);
		F f = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(fConfig);
		assertEquals(F1.class, f.getClass());
	}
	
	public void testOverridingImplementationClassDefaultAnnotation() {
		F2Config f2Config = TypedConfiguration.newConfigItem(F2Config.class);
		F f = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(f2Config);
		assertEquals(F2.class, f.getClass());
	}

	public void testInheritingOveriddenImplementationClassDefaultAnnotation() {
		F3Config f3Config = TypedConfiguration.newConfigItem(F3Config.class);
		F f = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(f3Config);
		// The default implementation class of F3Config is still F2:
		assertEquals(F2.class, f.getClass());
	}

	public interface G {
		public interface GUsage extends ConfigurationItem {
			PolymorphicConfiguration<? extends G> getG();

			void setG(PolymorphicConfiguration<? extends G> value);
		}

		public class G1 implements G {
			public interface G1Config extends PolymorphicConfiguration<G> {
				// No options.
			}

			@CalledByReflection
			public G1(InstantiationContext context, G1Config config) {
				super();
			}
		}
	}

	public void testPolymorphicInterfaceConfiguration() {
		GUsage gUsage = TypedConfiguration.newConfigItem(GUsage.class);
		G1Config g1Config = TypedConfiguration.newConfigItem(G1Config.class);
		gUsage.setG(g1Config);
	}

	public static class I {
		public interface IConfig extends PolymorphicConfiguration<I> {

			@ClassDefault(ISubclass1.class)
			@Override
			Class<? extends I> getImplementationClass();

		}

		public I(InstantiationContext context, IConfig config) {
			super();
		}
	}

	public static class ISubclass1 extends I {

		public interface ISubclassConfig1 extends IConfig {

			@StringDefault("Default")
			String getString();
		}

		private ISubclassConfig1 _config;

		public ISubclass1(InstantiationContext context, ISubclassConfig1 config) {
			super(context, config);
			_config = config;
		}

		String getString() {
			return _config.getString();
		}

	}

	public static class ISubclass2 extends I {

		public interface ISubclassConfig2 extends IConfig {

			@BooleanDefault(true)
			Boolean getBoolean();
		}

		private ISubclassConfig2 _config;

		public ISubclass2(InstantiationContext context, ISubclassConfig2 config) {
			super(context, config);
			_config = config;
		}

		Boolean getBoolean() {
			return _config.getBoolean();
		}

	}

	public interface H extends ConfigurationItem {

		@ItemDefault
		IConfig getIConfig();

		@ItemDefault(ISubclass2.class)
		IConfig getIConfigImplClassDefault();

	}

	public void testItemDefaultImplementationClassWithDefault() {
		H h = TypedConfiguration.newConfigItem(H.class);
		IConfig iconfig = h.getIConfig();
		assertSame(ISubclass1.class, iconfig.getImplementationClass());
		I instance = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(iconfig);
		assertTrue("ImplementationClass of '" + iconfig + "' is '" + ISubclass1.class + "'",
			instance instanceof ISubclass1);
	}

	public void testItemDefaultWithDefaultImplementationClass() {
		H h = TypedConfiguration.newConfigItem(H.class);
		IConfig fconfig = h.getIConfigImplClassDefault();
		assertSame(ISubclass2.class, fconfig.getImplementationClass());
		I instance = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(fconfig);
		assertTrue("ImplementationClass of '" + fconfig + "' is '" + ISubclass2.class + "'",
			instance instanceof ISubclass2);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestPolymorphicConfigurationDefaults.class);
	}

}
