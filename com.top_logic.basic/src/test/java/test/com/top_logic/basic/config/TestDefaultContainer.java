/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;

/**
 * Test case for {@link DefaultContainer} annotation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultContainer extends AbstractTypedConfigurationTestCase {

	public interface ListContainer extends ConfigurationItem {
		@DefaultContainer
		@Subtypes({
			@Subtype(tag = "y", type = Y.class),
			@Subtype(tag = "z", type = Z.class),
		})
		List<X> getXs();
	}

	public interface ArrayContainer extends ConfigurationItem {
		@DefaultContainer
		@Subtypes({
			@Subtype(tag = "y", type = Y.class),
			@Subtype(tag = "z", type = Z.class),
		})
		X[] getXs();
	}

	public interface InheritedContainer extends ListContainer {

		String getText();

	}

	public interface MapContainer extends ConfigurationItem {
		@DefaultContainer
		@Key(X.NAME_ATTRIBUTE)
		@Subtypes({
			@Subtype(tag = "y", type = Y.class),
			@Subtype(tag = "z", type = Z.class),
		})
		@EntryTag("x")
		Map<String, X> getXMap();
	}

	public interface SingletonContainer extends ConfigurationItem {
		@DefaultContainer
		@Subtypes({
			@Subtype(tag = "y", type = Y.class),
			@Subtype(tag = "z", type = Z.class),
		})
		X getX();
	}

	public interface InstanceContainer extends ConfigurationItem {
		@InstanceFormat
		@DefaultContainer
		XImpl getX();
	}

	public interface FailMultipleInheritedContainers extends ListContainer, MapContainer {
		// Pure sum interface.
	}

	public interface FailInheritedContainer extends ListContainer {
		@DefaultContainer
		List<X> getYList();
	}

	public interface FailMultipleContainers extends ConfigurationItem {
		@DefaultContainer
		List<X> getXList();

		@DefaultContainer
		List<X> getYList();
	}

	public interface FailNameClashWithContainer extends ListContainer {
		int getX();
	}

	public interface FailSubtypeClashWithContainer extends ListContainer {
		int getY();
	}

	public interface FailNameClashWithItemContainer extends SingletonContainer {
		int getZ();
	}

	public interface X extends NamedConfigMandatory {
		// Pure base interface.
	}

	public interface Y extends X {
		int getValue();
	}

	public interface Z extends X {
		String getText();
	}

	public static class XImpl implements ConfiguredInstance<XImpl.Config> {

		private Config _config;

		/**
		 * Configuration options for {@link TestDefaultContainer.XImpl}.
		 */
		@TagName("x-impl")
		public interface Config extends PolymorphicConfiguration<XImpl>, X {
			// Base config.
		}

		@CalledByReflection
		public XImpl(InstantiationContext context, Config config) {
			_config = config;
		}

		@Override
		public Config getConfig() {
			return _config;
		}

	}

	public static class YImpl extends XImpl {

		/**
		 * Configuration options for {@link TestDefaultContainer.XImpl}.
		 */
		@TagName("y-impl")
		public interface Config extends XImpl.Config, Y {
			@Override
			@ClassDefault(TestDefaultContainer.YImpl.class)
			Class<? extends TestDefaultContainer.YImpl> getImplementationClass();
		}

		@CalledByReflection
		public YImpl(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	public void testList() throws ConfigurationException, XMLStreamException {
		ListContainer a =
			read("<list-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></list-container>");
		checkList(a);

		assertEqualsXML(
			"<list-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></list-container>",
			toXML(a));
	}

	public void testListUpdate() throws ConfigurationException, XMLStreamException {
		ListContainer a =
			read("<list-container><x name='1'/></list-container>",
				"<list-container><y name='2' value='42'/><z name='3' text='Hello'/></list-container>");
		checkList(a);

		assertEqualsXML(
			"<list-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></list-container>",
			toXML(a));
	}

	protected void checkList(ListContainer a) {
		assertEquals(3, a.getXs().size());
		assertEquals("1", a.getXs().get(0).getName());
		assertEquals(42, ((Y) a.getXs().get(1)).getValue());
		assertEquals("Hello", ((Z) a.getXs().get(2)).getText());
	}

	public void testArray() throws ConfigurationException, XMLStreamException {
		ArrayContainer a =
			read("<array-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></array-container>");
		checkArray(a);

		assertEqualsXML(
			"<array-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></array-container>",
			toXML(a));
	}

	public void testArrayUpdate() throws ConfigurationException, XMLStreamException {
		ArrayContainer a =
			read("<array-container><x name='1'/></array-container>",
				"<array-container><y name='2' value='42'/><z name='3' text='Hello'/></array-container>");
		checkArray(a);

		assertEqualsXML(
			"<array-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></array-container>",
			toXML(a));
	}

	protected void checkArray(ArrayContainer a) {
		assertEquals(3, a.getXs().length);
		assertEquals("1", a.getXs()[0].getName());
		assertEquals(42, ((Y) a.getXs()[1]).getValue());
		assertEquals("Hello", ((Z) a.getXs()[2]).getText());
	}

	public void testInheritedList() throws ConfigurationException {
		InheritedContainer a =
			read("<inherited-container><text>Hello world!</text><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></inherited-container>");
		assertEquals(3, a.getXs().size());
		assertEquals("1", a.getXs().get(0).getName());
		assertEquals(42, ((Y) a.getXs().get(1)).getValue());
		assertEquals("Hello", ((Z) a.getXs().get(2)).getText());
	}

	public void testMap() throws ConfigurationException {
		MapContainer a =
			read("<map-container><x name='1'/><y name='2' value='42'/><z name='3' text='Hello'/></map-container>");
		assertEquals(3, a.getXMap().size());
		assertEquals("1", a.getXMap().get("1").getName());
		assertEquals(42, ((Y) a.getXMap().get("2")).getValue());
		assertEquals("Hello", ((Z) a.getXMap().get("3")).getText());
	}

	public void testSingleton() throws ConfigurationException, XMLStreamException {
		{
			SingletonContainer a = read("<singleton-container><x name='1'/></singleton-container>");
			assertEquals("1", a.getX().getName());

			assertEqualsXML(
				"<singleton-container><x name='1'/></singleton-container>",
				toXML(a));
		}
		{
			SingletonContainer a = read("<singleton-container><y name='2' value='42'/></singleton-container>");
			assertEquals(42, ((Y) a.getX()).getValue());

			assertEqualsXML(
				"<singleton-container><y name='2' value='42'/></singleton-container>",
				toXML(a));
		}
		{
			SingletonContainer a = read("<singleton-container><z name='3' text='Hello'/></singleton-container>");
			assertEquals("Hello", ((Z) a.getX()).getText());

			assertEqualsXML(
				"<singleton-container><z name='3' text='Hello'/></singleton-container>",
				toXML(a));
		}
	}

	public void testNormalize() throws ConfigurationException, XMLStreamException {
		SingletonContainer a = read(
			"<singleton-container xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "'><x config:interface='"
				+ Y.class.getName() + "' name='foo' value='42'/></singleton-container>");
		assertEquals(42, ((Y) a.getX()).getValue());

		assertEqualsXML(
			"<singleton-container><y name='foo' value='42'/></singleton-container>",
			toXML(a));
	}

	public void testInstanceContainer() throws ConfigurationException, XMLStreamException {
		InstanceContainer a = read(
			"<instance-container><y-impl name='foo' value='42'/></instance-container>");
		assertEquals("foo", a.getX().getConfig().getName());
		assertEquals(42, ((YImpl.Config) a.getX().getConfig()).getValue());

		assertEqualsXML(
			"<instance-container><y-impl name='foo' value='42'/></instance-container>",
			toXML(a));
	}

	public void testInstanceContainerNormalize() throws ConfigurationException, XMLStreamException {
		InstanceContainer a = read(
			"<instance-container><x class='" + YImpl.class.getName()
				+ "' name='foo' value='42'/></instance-container>");
		assertEquals("foo", a.getX().getConfig().getName());
		assertEquals(42, ((YImpl.Config) a.getX().getConfig()).getValue());

		assertEqualsXML(
			"<instance-container><y-impl name='foo' value='42'/></instance-container>",
			toXML(a));
	}

	public void testFailInheritedContainer() {
		assertFail("Ambiguous default container", FailInheritedContainer.class);
	}

	public void testFailFailMultipleContainers() {
		assertFail("Ambiguous default container", FailMultipleContainers.class);
	}

	public void testFailMultipleInheritedContainers() {
		assertFail("Ambiguous default container", FailMultipleInheritedContainers.class);
	}

	public void testFailNameClashWithContainer() {
		assertFail("Ambiguous content tag name 'x'", FailNameClashWithContainer.class);
	}

	public void testFailSubtypeClashWithContainer() {
		assertFail("Ambiguous content tag name 'y'", FailSubtypeClashWithContainer.class);
	}

	public void testFailNameClashWithItemContainer() {
		assertFail("Ambiguous content tag name 'z'", FailNameClashWithItemContainer.class);
	}

	protected static void assertFail(String message, Class<? extends ConfigurationItem> type) {
		try {
			TypedConfiguration.getConfigurationDescriptor(type);
			fail("Expected to fail.");
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains(message, ex.getMessage());
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return new MapBuilder<String, ConfigurationDescriptor>()
			.put("list-container", TypedConfiguration.getConfigurationDescriptor(ListContainer.class))
			.put("array-container", TypedConfiguration.getConfigurationDescriptor(ArrayContainer.class))
			.put("map-container", TypedConfiguration.getConfigurationDescriptor(MapContainer.class))
			.put("inherited-container", TypedConfiguration.getConfigurationDescriptor(InheritedContainer.class))
			.put("singleton-container", TypedConfiguration.getConfigurationDescriptor(SingletonContainer.class))
			.put("instance-container", TypedConfiguration.getConfigurationDescriptor(InstanceContainer.class))
			.toMap();
	}

	public static Test suite() {
		return suite(TestDefaultContainer.class);
	}

}
