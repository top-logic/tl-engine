/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Reference;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * {@link ConfigurationItem} interfaces for {@link TestValueSet}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public interface ScenarioContainerValueSet {

	public enum ExampleEnum {
		FIRST, SECOND
	}

	/**
	 * A {@link ConfigurationItem} with one property of each "primitive" type.
	 */
	public interface AllPrimitives extends ConfigurationItem {

		String PROPERTY_NAME_PRIMITIVE_BOOLEAN = "primitive-boolean";

		String PROPERTY_NAME_PRIMITIVE_CHAR = "primitive-char";

		String PROPERTY_NAME_PRIMITIVE_BYTE = "primitive-byte";

		String PROPERTY_NAME_PRIMITIVE_SHORT = "primitive-short";

		String PROPERTY_NAME_PRIMITIVE_INT = "primitive-int";

		String PROPERTY_NAME_PRIMITIVE_LONG = "primitive-long";

		String PROPERTY_NAME_PRIMITIVE_FLOAT = "primitive-float";

		String PROPERTY_NAME_PRIMITIVE_DOUBLE = "primitive-double";

		String PROPERTY_NAME_OBJECT_BOOLEAN = "object-boolean";

		String PROPERTY_NAME_OBJECT_CHARACTER = "object-character";

		String PROPERTY_NAME_OBJECT_BYTE = "object-byte";

		String PROPERTY_NAME_OBJECT_SHORT = "object-short";

		String PROPERTY_NAME_OBJECT_INTEGER = "object-integer";

		String PROPERTY_NAME_OBJECT_LONG = "object-long";

		String PROPERTY_NAME_OBJECT_FLOAT = "object-float";

		String PROPERTY_NAME_OBJECT_DOUBLE = "object-double";

		String PROPERTY_NAME_DATE = "date";

		String PROPERTY_NAME_STRING = "string";

		String PROPERTY_NAME_ENUM = "enum";

		String PROPERTY_NAME_CLASS = "class";

		@Name(PROPERTY_NAME_PRIMITIVE_BOOLEAN)
		boolean getPrimitiveBoolean();

		void setPrimitiveBoolean(boolean value);

		@Name(PROPERTY_NAME_PRIMITIVE_CHAR)
		char getPrimitiveChar();

		void setPrimitiveChar(char value);

		@Name(PROPERTY_NAME_PRIMITIVE_BYTE)
		byte getPrimitiveByte();

		void setPrimitiveByte(byte value);

		@Name(PROPERTY_NAME_PRIMITIVE_SHORT)
		short getPrimitiveShort();

		void setPrimitiveShort(short value);

		@Name(PROPERTY_NAME_PRIMITIVE_INT)
		int getPrimitiveInt();

		void setPrimitiveInt(int value);

		@Name(PROPERTY_NAME_PRIMITIVE_LONG)
		long getPrimitiveLong();

		void setPrimitiveLong(long value);

		@Name(PROPERTY_NAME_PRIMITIVE_FLOAT)
		float getPrimitiveFloat();

		void setPrimitiveFloat(float value);

		@Name(PROPERTY_NAME_PRIMITIVE_DOUBLE)
		double getPrimitiveDouble();

		void setPrimitiveDouble(double value);

		@Name(PROPERTY_NAME_OBJECT_BOOLEAN)
		Boolean getObjectBoolean();

		void setObjectBoolean(Boolean value);

		@Name(PROPERTY_NAME_OBJECT_CHARACTER)
		Character getObjectCharacter();

		void setObjectCharacter(Character value);

		@Name(PROPERTY_NAME_OBJECT_BYTE)
		Byte getObjectByte();

		void setObjectByte(Byte value);

		@Name(PROPERTY_NAME_OBJECT_SHORT)
		Short getObjectShort();

		void setObjectShort(Short value);

		@Name(PROPERTY_NAME_OBJECT_INTEGER)
		Integer getObjectInteger();

		void setObjectInteger(Integer value);

		@Name(PROPERTY_NAME_OBJECT_LONG)
		Long getObjectLong();

		void setObjectLong(Long value);

		@Name(PROPERTY_NAME_OBJECT_FLOAT)
		Float getObjectFloat();

		void setObjectFloat(Float value);

		@Name(PROPERTY_NAME_OBJECT_DOUBLE)
		Double getObjectDouble();

		void setObjectDouble(Double value);

		@Name(PROPERTY_NAME_DATE)
		Date getDate();

		void setDate(Date value);

		@Name(PROPERTY_NAME_STRING)
		String getString();

		void setString(String value);

		@Name(PROPERTY_NAME_ENUM)
		ExampleEnum getEnum();

		void setEnum(ExampleEnum value);

		/** Strange name, as 'getClass()' is already a final method in {@link Object}. */
		@Name(PROPERTY_NAME_CLASS)
		Class<?> getClass_();

		void setClass_(Class<?> value);

	}

	public interface ExampleConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		int getExample();

		void setExample(int value);

	}

	class ExampleInstance extends AbstractConfiguredInstance<ExamplePolymorphicConfig> {

		/**
		 * Called by the {@link TypedConfiguration} for creating a {@link ExampleInstance}.
		 * <p>
		 * <b>Don't call directly.</b> Use
		 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
		 * </p>
		 * 
		 * @param context
		 *        For error reporting and instantiation of dependent configured objects.
		 * @param config
		 *        The configuration for the new instance.
		 */
		@CalledByReflection
		public ExampleInstance(InstantiationContext context, ExamplePolymorphicConfig config) {
			super(context, config);
		}

	}

	interface ExamplePolymorphicConfig extends ExampleConfig, PolymorphicConfiguration<ExampleInstance> {

		// Nothing needed but the combination of the super interfaces.

	}

	public interface PropertyWithDefaultConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@IntDefault(3)
		@Name(PROPERTY_NAME_EXAMPLE)
		int getExample();

		void setExample(int value);

	}

	public interface ItemKindConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		@Subtypes({})
		ConfigurationItem getExample();

		void setExample(ConfigurationItem value);

	}

	public interface ReferenceKindConfig extends ConfigPart {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Reference
		@Name(PROPERTY_NAME_EXAMPLE)
		@Subtypes({})
		ConfigurationItem getExample();

		void setExample(ConfigurationItem value);

	}

	public interface ContainerKindConfig extends ConfigPart {

		String PROPERTY_NAME_CONTAINER = "example";

		@Container
		@Name(PROPERTY_NAME_CONTAINER)
		@Subtypes({})
		ConfigurationItem getContainer();

	}

	public interface DerivedKindConfig extends ConfigPart {

		String PROPERTY_NAME_SOURCE = "source";

		String PROPERTY_NAME_DERIVED = "derived";

		@Name(PROPERTY_NAME_SOURCE)
		int getSource();

		@DerivedRef(PROPERTY_NAME_SOURCE)
		@Name(PROPERTY_NAME_DERIVED)
		int getDerived();

	}

	public interface ArrayKindConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		ExampleConfig[] getExample();

		void setExample(ExampleConfig[] value);

	}

	public interface ListOfKindListConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		List<ExampleConfig> getExample();

		void setExample(List<ExampleConfig> value);

	}

	public interface ListOfKindComplexConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example";

		@Name(PROPERTY_NAME_EXAMPLE)
		@ListBinding
		List<String> getExample();

		void setExample(List<String> value);

	}

	public interface MapOfKindMapConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example-map";

		@Name(PROPERTY_NAME_EXAMPLE)
		@Key(ExampleConfig.PROPERTY_NAME_EXAMPLE)
		Map<Integer, ExampleConfig> getExampleMap();

		void setExampleMap(Map<Integer, ExampleConfig> value);

	}

	public interface MapOfKindComplexConfig extends ConfigurationItem {

		String PROPERTY_NAME_EXAMPLE = "example-map";

		@Name(PROPERTY_NAME_EXAMPLE)
		@MapBinding
		Map<String, String> getExampleMap();

		void setExampleMap(Map<String, String> value);

	}

	interface ScenarioTypeModifiedItemDefaultInstance extends ConfigurationItem {

		String EXAMPLE = "example";

		@Name(EXAMPLE)
		@InstanceDefault(ExampleInstance.class)
		ExampleInstance getExample();

	}

	interface ScenarioTypeModifiedListDefault extends ConfigurationItem {

		String EXAMPLE = "example-list";

		@Name(EXAMPLE)
		@ListDefault(ExampleConfig.class)
		List<ExampleConfig> getExampleList();

	}

	interface ScenarioTypeModifiedListDefaultInstances extends ConfigurationItem {

		String EXAMPLE = "example-list";

		@Name(EXAMPLE)
		@ListDefault(ExampleInstance.class)
		List<ExampleInstance> getExampleList();

	}

}
