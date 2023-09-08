/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.annotation;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Scenario test classes for {@link TestNullable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class ScenarioNullable {

	/**
	 * The name of all the properties that exist to be tested.
	 * <p>
	 * Using a single name for all the properties makes the test code simpler.
	 * </p>
	 */
	public static final String EXAMPLE_PROPERTY_NAME = "example";

	public enum ScenarioTypeEnum {
		A, B, C;
	}

	public interface ScenarioTypeStringProperty extends ConfigurationItem {

		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeEnumProperty extends ConfigurationItem {

		ScenarioTypeEnum getExample();

		void setExample(ScenarioTypeEnum value);

	}

	public interface ScenarioTypeStringPropertyNullable extends ConfigurationItem {

		@Nullable
		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeEnumPropertyNullable extends ConfigurationItem {

		@Nullable
		ScenarioTypeEnum getExample();

		void setExample(ScenarioTypeEnum value);

	}

	public interface ScenarioTypeStringPropertyNullDefault extends ConfigurationItem {

		@NullDefault
		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeEnumPropertyNullDefault extends ConfigurationItem {

		@NullDefault
		ScenarioTypeEnum getExample();

		void setExample(ScenarioTypeEnum value);

	}

	public interface ScenarioTypeStringPropertyNullableNullDefault extends ConfigurationItem {

		@NullDefault
		@Nullable
		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeEnumPropertyNullableNullDefault extends ConfigurationItem {

		@NullDefault
		@Nullable
		ScenarioTypeEnum getExample();

		void setExample(ScenarioTypeEnum value);

	}

	public interface ScenarioTypeNullableProperty extends ConfigurationItem {

		@Nullable
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypeLocalNothingInheritedNullable extends ScenarioTypeNullableProperty {

		@Override
		ConfigurationItem getExample();

	}

	public interface ScenarioTypeLocalNonNullableInheritedNullableNoSetter extends ScenarioTypeNullableProperty {

		@NonNullable
		@Override
		ConfigurationItem getExample();

	}

	public interface ScenarioTypeLocalNonNullableInheritedNullableLocalSetter extends ScenarioTypeNullableProperty {

		@NonNullable
		@Override
		ConfigurationItem getExample();

		void setExample(ConfigurationItem example);

	}

	public interface ScenarioTypeNullablePropertyWithSetter extends ConfigurationItem {

		@Nullable
		@Subtypes({})
		ConfigurationItem getExample();

		void setExample(ConfigurationItem example);

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeLocalNonNullableInheritedNullableInheritedSetter extends
			ScenarioTypeNullablePropertyWithSetter {

		@NonNullable
		@Override
		ConfigurationItem getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeLocalNonNullableInheritedNullableTwoSetter extends
			ScenarioTypeNullablePropertyWithSetter {

		@NonNullable
		@Override
		ConfigurationItem getExample();

		@Override
		void setExample(ConfigurationItem example);

	}

	public interface ScenarioTypeNonNullableProperty extends ConfigurationItem {

		@NonNullable
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypeLocalNothingInheritedNonNullable extends ScenarioTypeNonNullableProperty {

		@Override
		ConfigurationItem getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeLocalNullableInheritedNonNullable extends ScenarioTypeNonNullableProperty {

		@Nullable
		@Override
		ConfigurationItem getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeLocalNullDefaultInheritedNonNullable extends ScenarioTypeNonNullableProperty {

		@NullDefault
		@Override
		ConfigurationItem getExample();

	}

	public interface ScenarioTypeNullDefaultProperty extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@NullDefault
		@Name(PROPERTY_NAME_EXAMPLE)
		String getExample();

		void setExample(String value);

	}

	public interface ScenarioTypeJavaPrimitiveProperty extends ConfigurationItem {

		int getExample();

	}

	public interface ScenarioTypeJavaPrimitivePropertyNonNullable extends ConfigurationItem {

		@NonNullable
		int getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeJavaPrimitivePropertyNullable extends ConfigurationItem {

		@Nullable
		int getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypeJavaPrimitivePropertyNullDefault extends ConfigurationItem {

		@NullDefault
		int getExample();

	}

	public interface ScenarioTypePropertyKindList extends ConfigurationItem {

		@Subtypes({})
		List<ConfigurationItem> getExample();

	}

	public interface ScenarioTypePropertyKindListNonNullable extends ConfigurationItem {

		@NonNullable
		@Subtypes({})
		List<ConfigurationItem> getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypePropertyKindListNullable extends ConfigurationItem {

		@Nullable
		@Subtypes({})
		List<ConfigurationItem> getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypePropertyKindListNullDefault extends ConfigurationItem {

		@NullDefault
		@Subtypes({})
		List<ConfigurationItem> getExample();

	}

	public interface ScenarioTypeMapEntry extends ConfigurationItem {

		String getKey();

	}

	public interface ScenarioTypePropertyKindMap extends ConfigurationItem {

		@Key("key")
		Map<String, ScenarioTypeMapEntry> getExample();

	}

	public interface ScenarioTypePropertyKindMapNonNullable extends ConfigurationItem {

		@NonNullable
		@Key("key")
		Map<String, ScenarioTypeMapEntry> getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypePropertyKindMapNullable extends ConfigurationItem {

		@Nullable
		@Key("key")
		Map<String, ScenarioTypeMapEntry> getExample();

	}

	@NoImplementationClassGeneration
	public interface ScenarioTypePropertyKindMapNullDefault extends ConfigurationItem {

		@NullDefault
		@Key("key")
		Map<String, ScenarioTypeMapEntry> getExample();

	}

	public interface ScenarioTypePropertyKindItem extends ConfigurationItem {

		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypePropertyKindItemNonNullable extends ConfigurationItem {

		@NonNullable
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypePropertyKindItemNullable extends ConfigurationItem {

		@Nullable
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypePropertyKindItemNullDefault extends ConfigurationItem {

		@NullDefault
		@Subtypes({})
		ConfigurationItem getExample();

	}

	public interface ScenarioTypePropertyKindDerived extends ConfigurationItem {

		@DerivedRef("source")
		ConfigurationItem getExample();

		@Subtypes({})
		ConfigurationItem getSource();

	}

	public interface ScenarioTypePropertyKindDerivedNonNullable extends ConfigurationItem {

		@NonNullable
		@DerivedRef("source")
		ConfigurationItem getExample();

		@Subtypes({})
		ConfigurationItem getSource();

	}

	public interface ScenarioTypePropertyKindDerivedNullable extends ConfigurationItem {

		@Nullable
		@DerivedRef("source")
		ConfigurationItem getExample();

		@Subtypes({})
		ConfigurationItem getSource();

	}

	public interface ScenarioTypePropertyKindDerivedNullDefault extends ConfigurationItem {

		@NullDefault
		@DerivedRef("source")
		ConfigurationItem getExample();

		@Subtypes({})
		ConfigurationItem getSource();

	}

}
