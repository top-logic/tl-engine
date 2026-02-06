/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.json;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.json.JsonConfigSchemaBuilder;
import com.top_logic.basic.config.json.JsonConfigurationWriter;
import com.top_logic.basic.config.misc.TypedConfigUtil;

/**
 * {@link AbstractJsonConfigurationWriterTest} testing {@link JsonConfigurationWriter} and
 * {@link JsonConfigSchemaBuilder}.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestJsonConfigurationWithSchema extends AbstractJsonConfigurationWriterTest {

	public interface ConcretePolymorphicItem extends ConfigurationItem {
		A getB();

		void setB(A value);

		public interface A extends ConfigurationItem {
			int getX();

			void setX(int value);
		}

		public interface B extends A {
			int getY();

			void setY(int value);
		}
	}

	public interface AbstractPolymorphicItem extends ConfigurationItem {
		A getB();

		void setB(A value);

		@Abstract
		public interface A extends ConfigurationItem {
			int getX();

			void setX(int value);
		}

		public interface B extends A {
			int getY();

			void setY(int value);
		}

		public interface C extends A {
			int getZ();

			void setZ(int value);
		}
	}

	public interface ConfiguredImplementation extends ConfigurationItem {
		PolymorphicConfiguration<A> getB();

		void setB(PolymorphicConfiguration<? extends A> value);

		public interface A {
			String doIt();
		}

		public class B implements A {

			/**
			 * Singleton {@link B} instance.
			 */
			public static final B INSTANCE = new B();

			private B() {
				// Singleton constructor.
			}

			@Override
			public String doIt() {
				return "B";
			}
		}

		public abstract class C implements A {
			// Pure marker class.
		}

		public abstract class D<CI extends D.Config<?>> extends C implements ConfiguredInstance<CI> {

			private CI _config;

			/**
			 * Configuration options for {@link D}.
			 */
			public interface Config<I extends D<?>> extends PolymorphicConfiguration<I> {
				String getS();

				void setS(String value);
			}

			/**
			 * Creates a {@link D} from configuration.
			 * 
			 * @param context
			 *        The context for instantiating sub configurations.
			 * @param config
			 *        The configuration.
			 */
			@CalledByReflection
			public D(InstantiationContext context, CI config) {
				_config = config;
			}

			@Override
			public CI getConfig() {
				return _config;
			}
		}

		public class E<CI extends D.Config<?>> extends D<CI> {
			/**
			 * Creates a {@link E}.
			 */
			public E(InstantiationContext context, CI config) {
				super(context, config);
			}

			@Override
			public String doIt() {
				return "E(" + getConfig().getS() + ")";
			}
		}

		public class F extends D<F.Config<?>> {

			/**
			 * Configuration options for {@link F}.
			 */
			public interface Config<I extends F> extends D.Config<I> {
				int getX();

				void setX(int value);
			}

			/**
			 * Creates a {@link E}.
			 */
			public F(InstantiationContext context, Config<?> config) {
				super(context, config);
			}

			@Override
			public String doIt() {
				return "F(" + getConfig().getS() + ", " + getConfig().getX() + ")";
			}
		}
	}

	public void testPolymorphicBaseValue() throws Exception {
		ConcretePolymorphicItem a = TypedConfiguration.newConfigItem(ConcretePolymorphicItem.class);
		ConcretePolymorphicItem.A b = TypedConfiguration.newConfigItem(ConcretePolymorphicItem.A.class);
		b.setX(42);
		a.setB(b);

		doReadWrite("a", TypedConfiguration.getConfigurationDescriptor(ConcretePolymorphicItem.class), a);
	}

	public void testPolymorphicExtensionValue() throws Exception {
		ConcretePolymorphicItem a = TypedConfiguration.newConfigItem(ConcretePolymorphicItem.class);
		ConcretePolymorphicItem.B c = TypedConfiguration.newConfigItem(ConcretePolymorphicItem.B.class);
		c.setX(42);
		c.setY(13);
		a.setB(c);

		doReadWrite("a", TypedConfiguration.getConfigurationDescriptor(ConcretePolymorphicItem.class), a);
	}

	public void testPolymorphicValue() throws Exception {
		AbstractPolymorphicItem a = TypedConfiguration.newConfigItem(AbstractPolymorphicItem.class);
		AbstractPolymorphicItem.B c = TypedConfiguration.newConfigItem(AbstractPolymorphicItem.B.class);
		c.setX(42);
		c.setY(13);
		a.setB(c);

		doReadWrite("a", TypedConfiguration.getConfigurationDescriptor(AbstractPolymorphicItem.class), a);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testImplementationSingleton() throws Exception {
		ConfiguredImplementation doc = TypedConfiguration.newConfigItem(ConfiguredImplementation.class);
		PolymorphicConfiguration b = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		b.setImplementationClass(ConfiguredImplementation.B.class);
		doc.setB(b);

		ConfiguredImplementation newDoc = (ConfiguredImplementation) doReadWrite("a",
			TypedConfiguration.getConfigurationDescriptor(ConfiguredImplementation.class), doc);

		assertEquals("B", TypedConfigUtil.createInstance(newDoc.getB()).doIt());
	}

	public void testConfiguredImplementation() throws Exception {
		ConfiguredImplementation doc = TypedConfiguration.newConfigItem(ConfiguredImplementation.class);
		ConfiguredImplementation.F.Config<?> b =
			TypedConfiguration.newConfigItem(ConfiguredImplementation.F.Config.class);
		b.setS("str");
		b.setX(42);
		doc.setB(b);

		ConfiguredImplementation newDoc = (ConfiguredImplementation) doReadWrite("a",
			TypedConfiguration.getConfigurationDescriptor(ConfiguredImplementation.class), doc);

		assertEquals("F(str, 42)", TypedConfigUtil.createInstance(newDoc.getB()).doIt());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJsonConfigurationWithSchema.class));
	}

}

