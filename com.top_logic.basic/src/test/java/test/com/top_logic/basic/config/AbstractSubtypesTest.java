/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.ConfigurationSchemaConstants.*;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Tests for a scenario with the {@link Subtypes} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractSubtypesTest extends AbstractTypedConfigurationTestCase {

	protected abstract Class<? extends ConfigurationItem> containerClass();

	protected abstract String aClass();

	protected abstract String bClass();

	protected abstract String cClass();

	protected abstract String dClass();

	public void testListSubTypes() throws XMLStreamException, ConfigurationException {
		String x1 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<list-a>"
			+ "<a id='a1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + bClass() + "' b1='b' id='b1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + cClass() + "' c1='c' id='c1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + dClass() + "' c1='' d1='d' id='d1' />"
			+ "</list-a>"
			+ "</x>";

		String x2 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<list-a>"
			+ "<a common='' id='a1' />"
			+ "<b common='' id='b1' b1='b' />"
			+ "<c common='' id='c1' c1='c' />"
			+ "<d common='' id='d1' c1='' d1='d' />"
			+ "</list-a>"
			+ "</x>";

		assertEquivalence(x1, x2);
	}

	public void testMapSubTypes() throws XMLStreamException, ConfigurationException {
		String x1 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<a id='a1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + bClass() + "' b1='b' id='b1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + cClass() + "' c1='c' id='c1' />"
			+ "<a config:" + CONFIG_INTERFACE_ATTR + "='" + dClass() + "' c1='' d1='d' id='d1' />"
			+ "</map-a>"
			+ "</x>";

		String x2 = "<x xmlns:config='" + CONFIG_NS + "'>"
			+ "<map-a>"
			+ "<a common='' id='a1' />"
			+ "<b common='' id='b1' b1='b' />"
			+ "<c common='' id='c1' c1='c' />"
			+ "<d common='' id='d1' c1='' d1='d' />"
			+ "</map-a>"
			+ "</x>";

		assertEquivalence(x1, x2);
	}

	private void assertEquivalence(String explicitTypeNotation, String implicitTypeNotation) throws XMLStreamException,
			ConfigurationException {
		ConfigurationItem x1 = readX(explicitTypeNotation);
		ConfigurationItem x2 = readX(implicitTypeNotation);
		assertEquals(x1, x2);

		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter out = new ConfigurationWriter(buffer)) {
			out.write("x", containerClass(), x2);
		}
		assertEquals(normalize("<?xml version='1.0' ?>" + implicitTypeNotation), normalize(buffer.toString()));
	}

	public void testNormalize() {
		assertEquals(normalize("<a><b/></a>"), "<a></a>");
		assertEquals(normalize("<a><b foo='bar'/></a>"), "<a><b foo=\"bar\"></b></a>");
		assertEquals(normalize("<a><b></b></a>"), "<a></a>");
	}

	private String normalize(String xml) {
		String tagChar = "(?:-|[a-z])";

		String doubleQuoted =
			xml.replace('\'', '"');
		String emptyTagsExpanded =
			doubleQuoted.replaceAll("<(" + tagChar + "+)( (?:[^> ]| [^> ])*)? ?/>", "<$1$2></$1>");
		String emptyTagsRemoved =
			emptyTagsExpanded.replaceAll("<(" + tagChar + "+)></\\1>", "");
		return emptyTagsRemoved;
	}

	protected ConfigurationItem readX(String... sources) throws ConfigurationException {
		CharacterContent[] contents = contents(sources);
		return new ConfigurationReader(context, getDescriptors()).setSources(contents).read();
	}

	private CharacterContent[] contents(String... sources) {
		CharacterContent[] result = new CharacterContent[sources.length];
		for (int n = 0, cnt = sources.length; n < cnt; n++) {
			result[n] = CharacterContents.newContent(sources[n]);
		}
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		Map<String, ConfigurationDescriptor> rootConfigs =
			new MapBuilder<String, ConfigurationDescriptor>()
				.put("x", TypedConfiguration.getConfigurationDescriptor(containerClass()))
				.toMap();
		return rootConfigs;
	}

	public void testListDescriptors() throws ClassNotFoundException {
		checkSubtypesProperty("list-a");
	}

	public void testMapDescriptors() throws ClassNotFoundException {
		checkSubtypesProperty("map-a");
	}

	private void checkSubtypesProperty(String propertyName) throws ClassNotFoundException {
		ConfigurationDescriptor descriptor =
			TypedConfiguration.getConfigurationDescriptor(containerClass());
		PropertyDescriptor property = descriptor.getProperty(propertyName);
		assertEquals(allDescriptors(), getConcreteTypeDescriptors(property));
	}

	public Set<ConfigurationDescriptor> getConcreteTypeDescriptors(PropertyDescriptor property) {
		HashSet<ConfigurationDescriptor> result = new HashSet<>();
		for (String elementName : property.getElementNames()) {
			result.add(property.getElementDescriptor(elementName));
		}
		return result;
	}

	private Set<ConfigurationDescriptor> allDescriptors() throws ClassNotFoundException {
		return BasicTestCase.set(
			TypedConfiguration.getConfigurationDescriptor(Class.forName(aClass())),
			TypedConfiguration.getConfigurationDescriptor(Class.forName(bClass())),
			TypedConfiguration.getConfigurationDescriptor(Class.forName(cClass())),
			TypedConfiguration.getConfigurationDescriptor(Class.forName(dClass())));
	}

}
