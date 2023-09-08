/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import test.com.top_logic.basic.config.TestTypedConfigurationPolymorphism.E;
import test.com.top_logic.basic.config.TypedConfigurationSzenario.TestEnum.TestEnumDefault;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.func.Concat;

/**
 * {@link ConfigurationItem} interfaces for testing.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public interface TypedConfigurationSzenario {

	public static class A {
		public interface Config extends PolymorphicConfiguration<A> {
			
			String E_CONFIGS = "e-configs";

			boolean getBoolean();

			String P_NAME = "p";
			@Name(P_NAME)
			int getP();

			void setP(int value);
	
			@Name("a")
			A.Config getOtherConfig();
			
			void setOtherConfig(A.Config value);

			String B_CONFIG_NAME = "b";
			@Name(B_CONFIG_NAME)
			B.Config getBConfig();
			
			void setBConfig(B.Config value);

			@EntryTag("a")
			List<A.Config> getOthers();
			
			String INDEXED_NAME = "indexed";
			@EntryTag("a")
			@Name(INDEXED_NAME)
			@Key(P_NAME)
			Map<Integer, A.Config> getIndexed();

			String INDEXED_LIST_NAME = "indexed-list";

			@EntryTag("a")
			@Name(INDEXED_LIST_NAME)
			@Key(P_NAME)
			List<A.Config> getIndexedList();

			@ItemDefault(E.Config.class)
			E.Config getEConfigWithDefault();

			List<B.Config> getBConfigs();

			@InstanceFormat
			List<B> getBs();

			@Name(E_CONFIGS)
			List<EConfig> getEConfigs();
		}
		
		private final A a;
		private final B b;
		private final int p;
		
		public A(InstantiationContext context, Config config) {
			this.a = context.getInstance(config.getOtherConfig());
			this.b = context.getInstance(config.getBConfig());
			this.p = config.getP();
		}
	
		public int getP() {
			return p;
		}
	
		public A getA() {
			return a;
		}
	
		public B getB() {
			return b;
		}
		
	}

	public static class B implements ConfiguredInstance<B.Config> {
		public interface Config extends PolymorphicConfiguration<B> {
			String X_NAME = "x";
			@IntDefault(42)
			@Name(X_NAME)
			int getX();

			void setX(int value);

			int getW();
		}
	
		protected final Config config;;
		
		public B(InstantiationContext context, Config config) {
			this.config = config;
		}
		
		public int getX() {
			return config.getX();
		}

		@Override
		public Config getConfig() {
			return config;
		}
	}

	public static class C extends B {
		public interface Config extends B.Config {

			@Override
			@ClassDefault(C.class)
			Class<? extends B> getImplementationClass();

			@IntDefault(13)
			int getY();

			void setY(int value);

			@Override
			@IntDefault(42)
			int getW();
		}
		
		private int y;
		
		public C(InstantiationContext context, Config config) {
			super(context, config);
			this.y = config.getY();
		}
		
		public int getY() {
			return y;
		}
	}

	public static class D extends B {
		public interface Config extends B.Config {
			String Z_NAME = "z";
			
			@StringDefault("Hello world!")
			@Name(Z_NAME)
			String getZ();
			
			void setZ(String value);

			String getZZ();

			void setZZ(String value);

			Date getDate();
		}
		
		private String z;
		
		public D(InstantiationContext context, Config config) {
			super(context, config);
			this.z = config.getZ();
		}
		
		public String getZ() {
			return z;
		}
	}
	
	// can not be defined as inner enum of EConfig as javac won't find PolymorphicConfiguration in that case
	enum TestEnum {
		
		value1, value2, value3;
		
		public static final class TestEnumDefault extends EnumDefaultValue {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return TestEnum.value2;
			}
		}
		
	}
	
	public interface EConfig extends ConfigurationItem {
		String ENUM_NAME = "enum";
		@ComplexDefault(TestEnumDefault.class)
		@Name(ENUM_NAME)
		Enum<?> getEnum();

		TestEnum getRegularEnum();

		void setRegularEnum(TestEnum value);

		@FormattedDefault("value2")
		TestEnum getEnumFormattedDefault();

		void setEnumFormattedDefault(TestEnum value);

		@Nullable
		TestEnum getNullableEnum();

		void setNullableEnum(TestEnum value);

		@Nullable
		@NullDefault
		TestEnum getNullDefaultEnum();

		void setNullDefaultEnum(TestEnum value);

		String CLAZZ_NAME = "classWithDefault";
		@ClassDefault(EConfig.class)
		@Name(CLAZZ_NAME)
		Class<?> getClazz();
	}

	public interface FConfig extends ConfigurationItem {
		
		String  LIST_DEFAULT_VALUE = "HELLO";
		String LIST_PROPERTY_NAME = "listWithDefault";
    	@Format(CommaSeparatedStrings.class)
    	@FormattedDefault(LIST_DEFAULT_VALUE)
    	@Name(LIST_PROPERTY_NAME)
		List<String> getList();
		
	}
	
	public interface DerivedIndex extends ConfigurationItem {
		@Key(Inner.KEY)
		Map<String, Inner> getValues();

		interface Inner extends ConfigurationItem {
			String KEY = "key";

			String FIRST = "first";

			String SECOND = "second";

			@Name(KEY)
			@Derived(fun = Concat.class, args = { @Ref(FIRST), @Ref(SECOND) })
			String getKey();

			@Name(FIRST)
			String getFirst();

			@Name(SECOND)
			String getSecond();
		}
	}

	public static final Map<String, ConfigurationDescriptor> GLOBAL_CONFIGS =
		new MapBuilder<String, ConfigurationDescriptor>()
			.put("a", TypedConfiguration.getConfigurationDescriptor(A.Config.class))
			.put("b", TypedConfiguration.getConfigurationDescriptor(B.Config.class))
			.put("c", TypedConfiguration.getConfigurationDescriptor(C.Config.class))
			.put("d", TypedConfiguration.getConfigurationDescriptor(D.Config.class))
			.put("e-config", TypedConfiguration.getConfigurationDescriptor(EConfig.class))
			.put("f-config", TypedConfiguration.getConfigurationDescriptor(FConfig.class))
			.put("derived-index", TypedConfiguration.getConfigurationDescriptor(DerivedIndex.class))
			.toMap();

}
