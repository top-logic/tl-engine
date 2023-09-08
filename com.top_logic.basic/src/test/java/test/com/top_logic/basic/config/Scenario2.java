/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;

/**
 * Container for the definition of various {@link ConfigurationItem} interfaces for tests.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Scenario2 {

	/**
	 * Visitor interface for the {@link ConfigurationItem} hierarchy rooted a {@link A}. 
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface V {
		int visitB(B b, int arg);
		int visitC(C c, int arg);
		int visitD(D d, int arg);
	}

	public interface A extends ConfigurationItem {
		int visit(V visitor, int arg);

		/**
		 * Always polymorphic reference to another {@link A}.
		 */
		A getParent();
		
		void setParent(A value);
	}

	public interface B extends A {
		int getX();
		void setX(int value);
		
		/**
		 * Potentially polymorphic reference to {@link B} or {@link C}.
		 */
		B getOther();
		
		void setOther(B value);
	}

	public interface C extends B {
		int getY();
		void setY(int value);
		
		/**
		 * Always monomorphic reference to another {@link C}.
		 */
		C getNext();
		
		void setNext(C value);
	}

	/**
	 * Sub-interface of {@link TestTypedConfigurationVisit.C} without own visit
	 * choice in {@link TestTypedConfigurationVisit.V}.
	 */
	public interface C2 extends C {
		int getP();
	}

	public interface D extends A {
		int getZ();
		void setZ(int value);
	}

	/**
	 * {@link ConfigurationItem} that is serializes as {@link String}, when all properties are set.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Format(TestItemWithFormat.SerializeIfValuesSet.class)
	public interface TestItemWithFormat extends ConfigurationItem {
	
		String INT = "int";

		String BOOLEAN = "boolean";

		@Name(INT)
		int getInt();
	
		void setInt(int value);
	
		@Name(BOOLEAN)
		boolean getBoolean();
	
		void setBoolean(boolean value);

		public static class SerializeIfValuesSet extends AbstractConfigurationValueProvider<TestItemWithFormat> {
	
			public SerializeIfValuesSet() {
				super(TestItemWithFormat.class);
			}

			@Override
			public boolean isLegalValue(Object value) {
				return value == null || (value instanceof TestItemWithFormat && valuesSet((TestItemWithFormat) value));
			}
	
			private boolean valuesSet(TestItemWithFormat value) {
				ConfigurationDescriptor desc = value.descriptor();
				return value.valueSet(desc.getProperty(INT)) && value.valueSet(desc.getProperty(BOOLEAN));
			}

			@Override
			protected TestItemWithFormat getValueNonEmpty(String propertyName, CharSequence propertyValue)
					throws ConfigurationException {
				String val = propertyValue.toString();
				int separator = val.indexOf(' ');
				if (separator < 0) {
					throw new ConfigurationException(
						ResKey.forTest("Missing separator: Expected value format \"<int> <boolean>\"."), propertyName,
						propertyValue);
				}
				int intVal;
				try {
					intVal = Integer.parseInt(val.substring(0, separator));
				} catch (NumberFormatException ex) {
					throw new ConfigurationException(
						ResKey.forTest("Not an integer: Expected value format \"<int> <boolean>\"."), propertyName,
						propertyValue);
				}
				boolean booleanVal;
				switch (val.substring(separator + 1)) {
					case "true":
						booleanVal = true;
						break;
					case "false":
						booleanVal = false;
						break;
					default:
						throw new ConfigurationException(
							ResKey.forTest("Not a boolean: Expected value format \"<int> <boolean>\"."), propertyName,
							propertyValue);
				}
				TestItemWithFormat result = TypedConfiguration.newConfigItem(TestItemWithFormat.class);
				result.setInt(intVal);
				result.setBoolean(booleanVal);
				return result;
			}
	
			@Override
			protected String getSpecificationNonNull(TestItemWithFormat configValue) {
				return configValue.getInt() + " " + configValue.getBoolean();
			}
	
		}
	}

}
