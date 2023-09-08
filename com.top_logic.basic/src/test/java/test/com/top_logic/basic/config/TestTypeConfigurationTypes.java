/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EnumDefaultValue;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ComplexDefault;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.MemoryBinaryContent;
import com.top_logic.basic.xml.XMLStreamUtil;

/**
 * Test case for {@link TypedConfiguration} testing property types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypeConfigurationTypes extends AbstractTypedConfigurationTestCase {

	public enum E {
		A, B, C;
	}
	
	public interface TestTypesConfig extends ConfigurationItem {
		
		String ENUM_PROPERTY_NAME = "enum";
		
		@Name(ENUM_PROPERTY_NAME)
		E getEnum();
		void setEnum(E value);
		
		@ComplexDefault(EBDefault.class)
		E getEnumWithDefault();
		void setEnumWithDefault(E value);
		
		Enum<?> getGenericEnum();
		void setGenericEnum(Enum<?> value);
		
		@Key(ENUM_PROPERTY_NAME)
		Map<E, TestTypesConfig> getMap();
		void setMap(Map<E, TestTypesConfig> map);

		class EBDefault extends EnumDefaultValue {
			@Override
			public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
				return E.B;
			}
		}
	}
	
	public interface TestTypesConfig2 extends TestTypesConfig {
		// Plain sub-interface.
	}
	
	public void testInheritPropertyTicket6116() {
		TestTypesConfig2 config = TypedConfiguration.newConfigItem(TestTypesConfig2.class);
		
		TestTypesConfig entryConfig = TypedConfiguration.newConfigItem(TestTypesConfig.class); 
		entryConfig.setEnum(E.A);
		
		final Map<E, TestTypesConfig> map = Collections.singletonMap(E.A, entryConfig);
		// must not fail as property has no key property
		config.setMap(map);
		assertEquals(map, config.getMap());

		try {
			config.setMap(Collections.singletonMap(E.B, entryConfig));
			fail("Key does not match value of key property in entry");
		} catch (RuntimeException ex) {
			// expected
		}
	}
	
	public void testEmpty() {
		TestTypesConfig config = TypedConfiguration.newConfigItem(TestTypesConfig.class);
		doTestReadWrite(config);
	}
	
	public void testEnumDefault() {
		TestTypesConfig config = TypedConfiguration.newConfigItem(TestTypesConfig.class);
		assertEquals(E.A, config.getEnum());
		assertEquals(E.B, config.getEnumWithDefault());
	}

	public void testEnumSetNull() {
		TestTypesConfig config = TypedConfiguration.newConfigItem(TestTypesConfig.class);
		try {
			config.setEnum(null);
		} catch (Exception ex) {
			// expected
			return;
		}
		fail("null is an illegal value for an enum.");
	}

	public void testEnumWithDefaultSetNull() {
		TestTypesConfig config = TypedConfiguration.newConfigItem(TestTypesConfig.class);
		try {
			config.setEnumWithDefault(null);
		} catch (Exception ex) {
			// expected
			return;
		}
		fail("null is an illegal value for an enum.");
	}
	
	public void testFull() {
		TestTypesConfig config = TypedConfiguration.newConfigItem(TestTypesConfig.class);
		config.setEnum(E.A);
		config.setGenericEnum(E.B);
		doTestReadWrite(config);
	}
	
	private void doTestReadWrite(ConfigurationItem item) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			XMLStreamWriter out = XMLStreamUtil.getDefaultOutputFactory().createXMLStreamWriter(buffer);
			ConfigurationWriter writer = new ConfigurationWriter(out);
			writer.write("test", ConfigurationItem.class, item);
			out.close();
			
			Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName = Collections.singletonMap("test", TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class));
			BinaryContent binaryContent = new MemoryBinaryContent(buffer.toByteArray(), "unknown location");
			ConfigurationItem readItem =
				new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			globalDescriptorsByLocalName).setSource(binaryContent).read();

			assertEquals(item, readItem);
		} catch (XMLStreamException ex) {
			throw new RuntimeException(ex);
		} catch (ConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypeConfigurationTypes.class);
	}

}
