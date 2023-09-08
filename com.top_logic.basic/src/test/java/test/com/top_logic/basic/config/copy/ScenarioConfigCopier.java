/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.copy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.equal.ConfigEquality;

/**
 * {@link ConfigurationItem} interfaces for {@link TestConfigCopier}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioConfigCopier {

	public interface ScenarioTypeSubclass extends ConfigurationItem {
		// Nothing needed
	}

	public interface ScenarioTypeJavaPrimitive extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		int getExample();

		void setExample(int example);

	}

	public interface ScenarioTypeDate extends ConfigurationItem {

		Date getExample();

		void setExample(Date example);

	}

	public interface ScenarioTypeItem extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		@Subtypes({})
		ConfigurationItem getExample();

		void setExample(ConfigurationItem example);

	}

	public interface ScenarioTypeArray extends ConfigurationItem {

		ScenarioTypeEntry[] getExample();

		void setExample(ScenarioTypeEntry[] example);

	}

	public interface ScenarioTypeList extends ConfigurationItem {

		@Subtypes({})
		List<ConfigurationItem> getExample();

		void setExample(List<ConfigurationItem> example);

	}

	public interface ScenarioTypeMap extends ConfigurationItem {

		@Key(ScenarioTypeEntry.PROPERTY_NAME_EXAMPLE)
		Map<Integer, ScenarioTypeEntry> getExample();

		void setExample(Map<Integer, ? extends ConfigurationItem> example);

	}

	public interface ScenarioTypeEntry extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		int getExample();

		void setExample(int example);

	}

	public interface ScenarioTypeConfiguredInstanceItem extends ConfigurationItem {

		ScenarioTypeConfiguredInstance getExample();

		void setExample(ScenarioTypeConfiguredInstance example);

	}

	public class ScenarioTypeConfiguredInstance implements ConfiguredInstance<ScenarioTypeConfiguredInstanceConfig> {

		private final ScenarioTypeConfiguredInstanceConfig _config;

		@SuppressWarnings("unused")
		public ScenarioTypeConfiguredInstance(InstantiationContext context, ScenarioTypeConfiguredInstanceConfig config) {
			_config = config;
		}

		@Override
		public ScenarioTypeConfiguredInstanceConfig getConfig() {
			return _config;
		}

		@Override
		public int hashCode() {
			return _config.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (getClass() != other.getClass()) {
				return false;
			}
			ScenarioTypeConfiguredInstance otherConfiguredInstance = (ScenarioTypeConfiguredInstance) other;
			return ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(getConfig(), otherConfiguredInstance.getConfig());
		}

	}

	public interface ScenarioTypeConfiguredInstanceConfig extends
			PolymorphicConfiguration<ScenarioTypeConfiguredInstance> {

		int getInt();

		void setInt(int newValue);
	}

	public interface ScenarioTypeDefaultValue extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@IntDefault(3)
		@Name(PROPERTY_NAME_EXAMPLE)
		int getExample();

		void setExample(int example);

	}

	public interface ScenarioTypeDefaultItemValue extends ConfigurationItem {

		String VALUE = "value";

		@Name(VALUE)
		@ItemDefault
		ValueConfig getValue();

	}

	interface ValueConfig extends NamedConfiguration {
		// marker interface
	}

	public interface ScenarioTypeDefaultArrayValue extends ConfigurationItem {

		String VALUES = "values";

		@Name(VALUES)
		@ComplexDefault(CreateValues.class)
		ValueConfig[] getValues();

		class CreateValues extends DefaultValueProvider {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return new ValueConfig[] {
					TypedConfiguration.newConfigItem(ValueConfig.class),
					TypedConfiguration.newConfigItem(ValueConfig.class)
				};
			}
		}
	}

	public interface ScenarioTypeDefaultListValue extends ConfigurationItem {

		String VALUES = "values";

		@Name(VALUES)
		@ComplexDefault(CreateValues.class)
		List<ValueConfig> getValues();

		class CreateValues extends DefaultValueProvider {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return Arrays.asList(
					TypedConfiguration.newConfigItem(ValueConfig.class),
					TypedConfiguration.newConfigItem(ValueConfig.class));
			}
		}
	}

	public interface ScenarioTypeDifferentDefaultValue extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@IntDefault(7)
		@Name(PROPERTY_NAME_EXAMPLE)
		int getExample();

		void setExample(int example);

	}

	public interface ScenarioTypeMapEntry extends ConfigurationItem {

		String KEY = "key";

		String VALUE = "value";

		@Name(KEY)
		String getKey();

		void setKey(String key);

		@Name(VALUE)
		String getValue();

		void setValue(String value);

	}

	public interface ScenarioTypeReference extends ConfigurationItem {

		String SOURCE = "source";

		String REFERENCE = "reference";

		@Name(SOURCE)
		ScenarioTypeMapEntry getSource();

		void setSource(ScenarioTypeMapEntry value);

		@Name(REFERENCE)
		@Reference
		ScenarioTypeMapEntry getReference();

		void setReference(ScenarioTypeMapEntry value);

	}

	public interface ScenarioTypeReferenceFromArray extends ConfigurationItem {

		String SOURCE = "source";

		String REFERENCE = "reference";

		@Name(SOURCE)
		ScenarioTypeMapEntry[] getSource();

		void setSource(ScenarioTypeMapEntry... value);

		@Name(REFERENCE)
		@Reference
		ScenarioTypeMapEntry getReference();

		void setReference(ScenarioTypeMapEntry value);

	}

	public interface ScenarioTypeReferenceFromList extends ConfigurationItem {

		String SOURCE = "source";

		String REFERENCE = "reference";

		@Name(SOURCE)
		List<ScenarioTypeMapEntry> getSource();

		void setSource(List<ScenarioTypeMapEntry> value);

		@Name(REFERENCE)
		@Reference
		ScenarioTypeMapEntry getReference();

		void setReference(ScenarioTypeMapEntry value);

	}

	public interface ScenarioTypeReferenceFromMap extends ConfigurationItem {

		String SOURCE = "source";

		String REFERENCE = "reference";

		@Name(SOURCE)
		@Key(ScenarioTypeMapEntry.KEY)
		Map<String, ScenarioTypeMapEntry> getSource();

		void setSource(Map<String, ScenarioTypeMapEntry> value);

		@Name(REFERENCE)
		@Reference
		ScenarioTypeMapEntry getReference();

		void setReference(ScenarioTypeMapEntry value);

	}

	/**
	 * Test that the order of in which the {@link ConfigCopier} visits the properties is irrelevant.
	 * <p>
	 * As the order in which Java returns the methods is unspecified and changes, it is not possible
	 * to just switch the declaration order of the properties. Instead, a lot of references are
	 * declared, hoping that at least one of them comes before and one after the "source" property.
	 * </p>
	 */
	public interface ScenarioTypeReferencePropertyOrder extends ConfigurationItem {

		String PROPERTY_01_REFERENCE = "property-01-reference";

		String PROPERTY_02_REFERENCE = "property-02-reference";

		String PROPERTY_03_REFERENCE = "property-03-reference";

		String PROPERTY_04_REFERENCE = "property-04-reference";

		String PROPERTY_05_REFERENCE = "property-05-reference";

		String PROPERTY_06_REFERENCE = "property-06-reference";

		String PROPERTY_07_SOURCE = "property-07-source";

		String PROPERTY_08_REFERENCE = "property-08-reference";

		String PROPERTY_09_REFERENCE = "property-09-reference";

		String PROPERTY_10_REFERENCE = "property-10-reference";

		String PROPERTY_11_REFERENCE = "property-11-reference";

		String PROPERTY_12_REFERENCE = "property-12-reference";

		String PROPERTY_13_REFERENCE = "property-13-reference";

		@Name(PROPERTY_01_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty01Reference();

		void setProperty01Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_02_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty02Reference();

		void setProperty02Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_03_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty03Reference();

		void setProperty03Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_04_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty04Reference();

		void setProperty04Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_05_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty05Reference();

		void setProperty05Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_06_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty06Reference();

		void setProperty06Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_07_SOURCE)
		ScenarioTypeMapEntry getProperty07Source();

		void setProperty07Source(ScenarioTypeMapEntry value);

		@Name(PROPERTY_08_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty08Reference();

		void setProperty08Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_09_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty09Reference();

		void setProperty09Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_10_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty10Reference();

		void setProperty10Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_11_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty11Reference();

		void setProperty11Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_12_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty12Reference();

		void setProperty12Reference(ScenarioTypeMapEntry value);

		@Name(PROPERTY_13_REFERENCE)
		@Reference
		ScenarioTypeMapEntry getProperty13Reference();

		void setProperty13Reference(ScenarioTypeMapEntry value);

	}

	public interface ScenarioTypeConfigPart extends ConfigPart {

		@Container
		ConfigurationItem getContainer();

	}

}
