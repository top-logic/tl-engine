/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.mandatory;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * {@link ConfigurationItem} interfaces for testing the {@link Mandatory} annotation.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioMandatory {

	public interface ScenarioContainerNoAutoload {

		public interface ScenarioTypeAddMandatoryInMultipleSupers
				extends ScenarioTypeAddMandatoryA, ScenarioTypeAddMandatoryB {

			// Nothing needed
		}

		public interface ScenarioTypeStringDefaultAnnotation extends ConfigurationItem {

			@StringDefault("Hello World!")
			String getTestProperty();

		}

		public interface ScenarioTypeSubOf_StringDefaultAnnotation extends ScenarioTypeStringDefaultAnnotation {

			@Override
			@Mandatory
			String getTestProperty();

		}

		public interface ScenarioTypeFormattedDefaultAnnotation extends ConfigurationItem {

			@FormattedDefault("Hello World!")
			@Format(StringValueProvider.class)
			String getTestProperty();

		}

		public interface ScenarioTypeSubOf_FormattedDefaultAnnotation extends ScenarioTypeFormattedDefaultAnnotation {

			@Override
			@Mandatory
			String getTestProperty();

		}

		public interface ScenarioTypeItemDefaultAnnotation extends ConfigurationItem {

			/** The default is just some random class, as the actual value does not matter. */
			@ItemDefault(ScenarioTypeStillEmpty.class)
			ScenarioTypeEmpty getTestProperty();

		}

		public interface ScenarioTypeSubOf_ItemDefaultAnnotation extends ScenarioTypeItemDefaultAnnotation {

			@Override
			@Mandatory
			ScenarioTypeEmpty getTestProperty();

		}

		// Intentional raw type, to test the behavior of that case
		@SuppressWarnings("rawtypes")
		public interface ScenarioTypeImplementationClassDefault extends PolymorphicConfiguration {

			@Override
			@ClassDefault(Number.class)
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeSubOf_ImplementationClassDefault extends ScenarioTypeImplementationClassDefault {

			@Override
			@Mandatory
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeSubOf_ImplementationClassDefaultSpecialized extends
				ScenarioTypeImplementationClassDefault {

			@Override
			@Mandatory
			Class<? extends Integer> getImplementationClass();

		}

		/** The type parameter is just some random class, as the actual value does not matter. */
		public interface ScenarioTypeDefaultByTypeParameter extends PolymorphicConfiguration<Number> {
			// Nothing needed
		}

		public interface ScenarioTypeSubOf_DefaultByTypeParameter extends ScenarioTypeDefaultByTypeParameter {

			@Override
			@Mandatory
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeSubOf_DefaultByTypeParameterSpecialized
				extends ScenarioTypeDefaultByTypeParameter {

			@Override
			@Mandatory
			Class<? extends Integer> getImplementationClass();

		}

		// Intentional raw type, to test the behavior of that case
		@SuppressWarnings("rawtypes")
		public interface ScenarioTypeNoTypeParameter1 extends PolymorphicConfiguration {
			// Nothing needed
		}

		public interface ScenarioTypeSubOf_NoTypeParameter1 extends ScenarioTypeNoTypeParameter1 {

			// Intentional raw type, to test the behavior of that case
			@SuppressWarnings("rawtypes")
			@Override
			@Mandatory
			Class getImplementationClass();

		}

		// Intentional raw type, to test the behavior of that case
		@SuppressWarnings("rawtypes")
		public interface ScenarioTypeNoTypeParameter2 extends PolymorphicConfiguration {

			@Override
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeSubOf_NoTypeParameter2 extends
				ScenarioTypeNoTypeParameter2 {

			// Intentional raw type, to test the behavior of that case
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			@Mandatory
			Class getImplementationClass();

		}

		// Intentional raw type, to test the behavior of that case
		@SuppressWarnings("rawtypes")
		public interface ScenarioTypeNoTypeParameter3 extends PolymorphicConfiguration {

			@Override
			Class getImplementationClass();

		}

		public interface ScenarioTypeSubOf_NoTypeParameter3 extends
				ScenarioTypeNoTypeParameter3 {

			@Override
			@Mandatory
			Class<? extends Number> getImplementationClass();

		}

		// Intentional raw type, to test the behavior of that case
		@SuppressWarnings("rawtypes")
		public interface ScenarioTypeNoTypeParameter4 extends PolymorphicConfiguration {

			@Override
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeSubOf_NoTypeParameter4 extends
				ScenarioTypeNoTypeParameter4 {

			@Override
			@Mandatory
			Class<? extends Number> getImplementationClass();

		}

		public interface ScenarioTypeImplicitDefault1<T> extends PolymorphicConfiguration<T> {

			@Override
			@Mandatory
			Class<? extends T> getImplementationClass();

		}

		public interface ScenarioTypeImplicitDefault2<T extends Number> extends ScenarioTypeImplicitDefault1<T> {
			// Nothing needed
		}

	}

	public interface ScenarioTypeEmpty extends ConfigurationItem {
		// Nothing needed
	}

	public interface ScenarioTypeStillEmpty extends ScenarioTypeEmpty {
		// Nothing needed
	}

	public interface ScenarioTypeProperty extends ConfigurationItem {

		/** Not mandatory here, which is the common root. */
		int getTestProperty();

	}

	public interface ScenarioTypeMandatoryProperty extends ConfigurationItem {

		String TEST_PROPERTY = "test-property";

		@Name(TEST_PROPERTY)
		@Mandatory
		int getTestProperty();

		void setTestProperty(int value);

	}

	public interface ScenarioTypeMandatoryList extends ConfigurationItem {

		String VALUES = "values";

		@Mandatory
		@Name(VALUES)
		List<ScenarioTypeMandatoryProperty> getValues();

	}

	public interface ScenarioTypeMandatoryMap extends ConfigurationItem {

		String VALUES = "values";

		@Mandatory
		@Key(ScenarioTypeMandatoryProperty.TEST_PROPERTY)
		@Name(VALUES)
		Map<Integer, ScenarioTypeMandatoryProperty> getValues();

	}

	public interface ScenarioTypeA extends ScenarioTypeMandatoryProperty {
		// Nothing needed
	}

	public interface ScenarioTypeB extends ConfigurationItem {

		ScenarioTypeMandatoryProperty getConfigWithMandatory();

	}

	public static class ScenarioTypeConfiguredInstance implements
			ConfiguredInstance<ScenarioTypeConfiguredInstance.Config> {

		public interface Config extends PolymorphicConfiguration<ScenarioTypeConfiguredInstance> {

			String PROPERTY_NAME_TEST_PROPERTY = "test-property";

			@Mandatory
			@Name(PROPERTY_NAME_TEST_PROPERTY)
			int getTestProperty();

		}

		private final Config _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstance(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;

		}

	}

	public interface ScenarioTypeWithConfiguredInstance extends ConfigurationItem {

		ScenarioTypeConfiguredInstance getExample();

	}

	public interface ScenarioTypeEntry extends ConfigurationItem {

		public static final String KEY_PROPERTY_NAME = "key-property";

		@Name(KEY_PROPERTY_NAME)
		int getKeyProperty();

		@Mandatory
		int getTestProperty();

	}

	public interface ScenarioTypeList extends ConfigurationItem {

		@EntryTag("entry")
		@Key(ScenarioTypeEntry.KEY_PROPERTY_NAME)
		List<ScenarioTypeEntry> getConfigsWithMandatory();

	}

	public class ScenarioTypeConfiguredInstanceEntry implements
			ConfiguredInstance<ScenarioTypeConfiguredInstanceEntry.Config> {

		public interface Config extends PolymorphicConfiguration<ScenarioTypeConfiguredInstanceEntry> {

			public static final String KEY_PROPERTY_NAME = "key-property";

			@Name(KEY_PROPERTY_NAME)
			int getKeyProperty();

			@Mandatory
			int getTestProperty();

		}

		private final Config _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstanceEntry(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

	}

	public interface ScenarioTypeListWithConfiguredInstances extends ConfigurationItem {

		@EntryTag("entry")
		@Key(ScenarioTypeConfiguredInstanceEntry.Config.KEY_PROPERTY_NAME)
		List<ScenarioTypeConfiguredInstanceEntry> getConfigsWithMandatory();

	}

	public interface ScenarioTypeMap extends ConfigurationItem {

		@EntryTag("entry")
		@Key(ScenarioTypeEntry.KEY_PROPERTY_NAME)
		Map<Integer, ScenarioTypeEntry> getConfigsWithMandatory();

	}

	public interface ScenarioTypeMapWithConfiguredInstances extends ConfigurationItem {

		@EntryTag("entry")
		@Key(ScenarioTypeConfiguredInstanceEntry.Config.KEY_PROPERTY_NAME)
		Map<Integer, ScenarioTypeConfiguredInstanceEntry> getConfigsWithMandatory();

	}

	public interface ScenarioTypeD extends ConfigurationItem {

		@Mandatory
		@Format(CommaSeparatedStrings.class)
		@EntryTag("entry")
		List<String> getTestListProperty();

	}

	public interface ScenarioTypeE extends ScenarioTypeEmpty {

		@Mandatory
		int getTestProperty();

	}

	public interface ScenarioTypeF extends ScenarioTypeMandatoryProperty, ScenarioTypeEmpty {
		// Nothing needed
	}

	public interface ScenarioTypeG extends ScenarioTypeEmpty, ScenarioTypeMandatoryProperty {
		// Nothing needed
	}

	public interface ScenarioTypeH extends ScenarioTypeA, ScenarioTypeEmpty {
		// Nothing needed
	}

	public interface ScenarioTypeI extends ScenarioTypeEmpty, ScenarioTypeA {
		// Nothing needed
	}

	public interface ScenarioTypeJ extends ScenarioTypeProperty {
		// Nothing needed
	}

	public interface ScenarioTypeK extends ScenarioTypeProperty {

		@Override
		@Mandatory
		int getTestProperty();

	}

	public interface ScenarioTypeL extends ScenarioTypeK, ScenarioTypeJ {
		// Nothing needed
	}

	public interface ScenarioTypeM extends ScenarioTypeJ, ScenarioTypeK {
		// Nothing needed
	}

	public interface ScenarioTypeN extends ScenarioTypeK {
		// Nothing needed
	}

	public interface ScenarioTypeO extends ScenarioTypeN, ScenarioTypeJ {
		// Nothing needed
	}

	public interface ScenarioTypeP extends ScenarioTypeJ, ScenarioTypeN {
		// Nothing needed
	}

	public interface ScenarioTypeAddMandatoryA extends ScenarioTypeProperty {

		@Override
		@Mandatory
		int getTestProperty();

	}

	public interface ScenarioTypeAddMandatoryB extends ScenarioTypeProperty {

		@Override
		@Mandatory
		int getTestProperty();

	}

	public interface ScenarioTypeRecursiveConfig extends ConfigurationItem {

		ScenarioTypeRecursiveConfig getTestProperty();

	}

	public interface ScenarioTypeIndirectRecursiveConfigA extends ConfigurationItem {

		ScenarioTypeIndirectRecursiveConfigB getTestProperty();

	}

	public interface ScenarioTypeIndirectRecursiveConfigB extends ConfigurationItem {

		ScenarioTypeIndirectRecursiveConfigA getTestProperty();

	}

	public static class ScenarioTypeSingleton {

		public static final ScenarioTypeSingleton INSTANCE = new ScenarioTypeSingleton();

		private ScenarioTypeSingleton() {
			// Singleton
		}
	}

	public interface ScenarioTypeInstanceFormatDefault extends ConfigurationItem {

		@InstanceFormat()
		@InstanceDefault(ScenarioTypeSingleton.class)
		ScenarioTypeSingleton getTestProperty();

	}

	public interface MandatoryTest extends ConfigurationItem {
		@Mandatory
		public String getStringMandatory();

		@Mandatory
		public int getIntMandatory();
	}

	public interface MandatoryTest2 extends MandatoryTest {

		int DEFAULT_INT_MANDATORY = 15;

		/** Property remains mandatory */
		@Override
		String getStringMandatory();

		/** Subinterface has default value */
		@IntDefault(DEFAULT_INT_MANDATORY)
		@Override
		int getIntMandatory();
	}

	public interface MandatoryTest3 extends MandatoryTest2 {

		/** Default value is not fine for this interface */
		@Mandatory
		@Override
		int getIntMandatory();
	}

	public interface DerivedUsesMandatory extends ConfigurationItem {

		String DERIVED = "derived";

		String MANDATORY = "mandatory";

		@DerivedRef(MANDATORY)
		@Name(DERIVED)
		int getDerived();

		@Mandatory
		@Name(MANDATORY)
		int getMandatory();

		void setMandatory(int value);

	}

	public interface Regression20851_1 extends ConfigurationItem {

		String A = "a";

		String B = "b";

		String C = "c";

		@Name(A)
		@Subtypes({})
		ConfigurationItem getA();

		void setA(ConfigurationItem a);

		@Name(B)
		@Mandatory
		@Subtypes({})
		ConfigurationItem getB();

		@Name(C)
		@Subtypes({})
		ConfigurationItem getC();

		void setC(ConfigurationItem c);

	}

	public interface Regression20851_2 extends ConfigurationItem {

		String A = "a";

		@Name(A)
		@ItemDefault
		@Subtypes({})
		ConfigurationItem getA();

	}

	public interface MandatoryWithNullDefaultFormat extends ConfigurationItem {

		String FOO = "foo";

		@Name(FOO)
		@Mandatory
		@NonNullable
		@Format(FormatWithNullDefault.class)
		String getFoo();

		class FormatWithNullDefault extends AbstractConfigurationValueProvider<String> {
			public FormatWithNullDefault() {
				super(String.class);
			}

			@Override
			protected String getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return propertyValue.toString();
			}

			@Override
			protected String getSpecificationNonNull(String configValue) {
				return configValue;
			}

		}

	}

	public interface MandatoryNoDefaultValueInstantiation extends ConfigurationItem {
		
		public static class ConfigValueProvider extends AbstractConfigurationValueProvider<Object> {

			public static Stack<Exception> DEFAULT_VALUE_CALLS = new ArrayStack<>();

			/**
			 * Creates a new {@link ConfigValueProvider}.
			 */
			public ConfigValueProvider() {
				super(Object.class);
			}

			@Override
			protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				return propertyValue;
			}
			
			@Override
			public Object defaultValue() {
				DEFAULT_VALUE_CALLS.push(new Exception());
				return super.defaultValue();
			}

			@Override
			protected String getSpecificationNonNull(Object configValue) {
				return String.valueOf(configValue);
			}
		}

		@Mandatory
		@Format(ConfigValueProvider.class)
		Object getMandatoryProperty();
	}
}
