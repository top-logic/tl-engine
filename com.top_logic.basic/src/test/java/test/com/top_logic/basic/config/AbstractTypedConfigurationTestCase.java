/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.ConfigurationSchemaConstants.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ErrorIgnoringProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XMLCompare;

/**
 * Common super class for test of {@link TypedConfiguration} related features.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractTypedConfigurationTestCase extends TestCase {

	protected static final String XML_DECLARATION = "<?xml version='1.0' encoding='utf-8'?>";

	protected static final String XML_CONFIG_NAMESPACE_DECLARATION = " xmlns:config='" + CONFIG_NS + "' ";

	protected Protocol protocol;
	protected InstantiationContext context;

	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		initDefaultTest();
	}

	protected void initDefaultTest() {
		this.protocol = createProtocol();
		initResolverAndContext();
	}

	protected AssertProtocol createProtocol() {
		return new AssertProtocol();
	}

	protected void initFailureTest() {
		this.protocol = new ExpectedFailureProtocol();
		initResolverAndContext();
	}

	protected void initErrorIgnoringTest(Protocol chain) {
		this.protocol = new ErrorIgnoringProtocol(chain);
		initResolverAndContext();
	}

	protected void initResolverAndContext() {
		this.context = new DefaultInstantiationContext(protocol);
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.protocol = null;
		this.context = null;
		
		super.tearDown();
	}

	/**
	 * Convenience shortcut for {@link ConfigurationItem#update(PropertyDescriptor, Object)} with
	 * the property name instead of the {@link PropertyDescriptor}.
	 */
	protected Object setValue(ConfigurationItem item, String propertyName, Object value) {
		return item.update(getProperty(item, propertyName), value);
	}

	/**
	 * Convenience shortcut for getting the {@link PropertyDescriptor} for a given property name and
	 * {@link ConfigurationItem} <b>instance</b>.
	 */
	protected PropertyDescriptor getProperty(ConfigurationItem configItem, String propertyName) {
		return configItem.descriptor().getProperty(propertyName);
	}

	/**
	 * Convenience shortcut for getting the {@link PropertyDescriptor} for a given property name and
	 * {@link ConfigurationItem} <b>class</b>.
	 */
	protected PropertyDescriptor getProperty(Class<? extends ConfigurationItem> configClass, String propertyName) {
		return TypedConfiguration.getConfigurationDescriptor(configClass).getProperty(propertyName);
	}

	protected void assertEquals(Class<?> testClass, Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName, String expected, String... actual) throws IOException, ConfigurationException {
		assertEquals(null, testClass, globalDescriptorsByLocalName, expected, actual);
	}
	
	protected void assertEquals(String message, Class<?> testClass, Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName, String expected, String... actual) throws IOException, ConfigurationException {
		final ConfigurationItem expectedConf = readConfiguration(testClass, globalDescriptorsByLocalName, expected, null);
		ConfigurationItem actualConf = new ConfigurationReader(context, globalDescriptorsByLocalName)
			.setSources(toBinaryContents(testClass, actual)).read();
		assertEquals(message, expectedConf, actualConf);
	}

	protected ConfigurationItem readConfiguration(Class<?> testClass,
			Map<String, ConfigurationDescriptor> globalDescriptorsByLocalName, String fileSuffix,
			ConfigurationItem fallback) throws IOException, ConfigurationException {

		BinaryContent binaryContent = getResource(testClass, fileSuffix);
		return new ConfigurationReader(context, globalDescriptorsByLocalName).setBaseConfig(fallback).setSource(binaryContent).read();
	}

	private BinaryContent getResource(Class<?> testClass, String fileSuffix) {
		return ClassRelativeBinaryContent.withSuffix(testClass, fileSuffix);
	}

	protected BinaryContent getResource(String fileSuffix) throws IOException {
		return getResource(getClass(), fileSuffix);
	}
	
	/**
	 * @param testInterfaceContainer
	 *        All subclasses of {@link ConfigurationItem} within this container are registered as
	 *        global descriptors.
	 * @param sources
	 *        XML strings to create {@link ConfigurationItem}s for. The item created from the
	 *        <code>0^th</code> source has no fallback item. The source with index <code>i</code>
	 *        overloads all configurations from index <code>0</code> to <code>i-1</code>.
	 */
	protected ConfigurationItem read(Class<?> testInterfaceContainer, String... sources) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> configDescriptors = new HashMap<>();
		Class<?>[] configInterfaces = testInterfaceContainer.getDeclaredClasses();
		for (Class<?> configInterface : configInterfaces) {
			if (!ConfigurationItem.class.isAssignableFrom(configInterface)) {
				continue;
			}
			ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configInterface);
			configDescriptors.put(configInterface.getSimpleName(), descriptor);
		}
		return read(configDescriptors, sources);
	}

	/**
	 * @param sources
	 *        XML strings to create {@link ConfigurationItem}s for. The item created from the
	 *        <code>0^th</code> source has no fallback item. The source with index <code>i</code>
	 *        overloads all configurations from index <code>0</code> to <code>i-1</code>.
	 */
	protected <T extends ConfigurationItem> T read(String... sources)
			throws ConfigurationException {

		return read(getDescriptors(), sources);
	}

	/**
	 * @param fallback
	 *        Is allowed to be <code>null</code>.
	 * @param sources
	 *        XML strings to create {@link ConfigurationItem}s for. The item created from the
	 *        <code>0^th</code> source has no fallback item. The source with index <code>i</code>
	 *        overloads all configurations from index <code>0</code> to <code>i-1</code>.
	 */
	protected <T extends ConfigurationItem> T read(ConfigurationItem fallback, String... sources)
			throws ConfigurationException {
		return read(getDescriptors(), fallback, sources);
	}

	/**
	 * @param sources
	 *        XML strings to create {@link ConfigurationItem}s for. The item created from the
	 *        <code>0^th</code> source has no fallback item. The source with index <code>i</code>
	 *        overloads all configurations from index <code>0</code> to <code>i-1</code>.
	 */
	protected <T extends ConfigurationItem> T read(Map<String, ConfigurationDescriptor> globalConfigs,
			String... sources)
			throws ConfigurationException {
		
		return read(globalConfigs, null, sources);
	}

	/**
	 * @param fallback
	 *        Is allowed to be <code>null</code>.
	 * @param sources
	 *        XML strings to create {@link ConfigurationItem}s for. The item created from the
	 *        <code>0^th</code> source has no fallback item. The source with index <code>i</code>
	 *        overloads all configurations from index <code>0</code> to <code>i-1</code>.
	 */
	protected <T extends ConfigurationItem> T read(Map<String, ConfigurationDescriptor> globalConfigs,
			ConfigurationItem fallback, String... sources) throws ConfigurationException {
		
		if (sources.length == 0) {
			throw new IllegalArgumentException("No sources given.");
		}
		List<CharacterContent> contents = toCharacterContents(sources);
		ConfigurationItem item =
			new ConfigurationReader(context, globalConfigs).setBaseConfig(fallback).setSources(contents).read();
		context.checkErrors();
		return (T) item;
	}
	
	private List<CharacterContent> toCharacterContents(String... sources) {
		List<CharacterContent> contents = new ArrayList<>();
		for (String source : sources) {
			contents.add(CharacterContents.newContent(source));
		}
		return contents;
	}

	protected ConfigurationItem readConfiguration(String fileSuffix, ConfigurationItem fallback) throws Throwable {
		return readConfiguration(getClass(), getDescriptors(), fileSuffix, fallback);
	}

	protected abstract Map<String, ConfigurationDescriptor> getDescriptors();

	protected ConfigurationItem readConfiguration(String fileSuffix) throws Throwable {
		return readConfiguration(fileSuffix, null);
	}

	protected ConfigurationItem readConfigurationStacked(String... filenameSuffixes) throws Throwable {
		List<BinaryContent> sources = toBinaryContents(getClass(), filenameSuffixes);
		return new ConfigurationReader(context, getDescriptors()).setSources(sources).read();
	}

	private List<BinaryContent> toBinaryContents(Class<?> testClass, String... fileSuffixes) throws IOException {
		List<BinaryContent> actualSources = new ArrayList<>();
		for (String fileSuffix : fileSuffixes) {
			actualSources.add(getResource(testClass, fileSuffix));
		}
		return actualSources;
	}

	protected Entry<String, ConfigurationDescriptor> getDescriptorBinding(
			Map<String, ConfigurationDescriptor> descriptors,
			ConfigurationItem config) {
		Entry<String, ConfigurationDescriptor> candidate = null;
		Class<?> searchedConfigInterface = config.descriptor().getConfigurationInterface();
		for (Entry<String, ConfigurationDescriptor> entry : descriptors.entrySet()) {
			Class<?> foundConfigInterface = entry.getValue().getConfigurationInterface();
			if (foundConfigInterface.isAssignableFrom(searchedConfigInterface)) {
				if (candidate == null || candidate.getValue().getConfigurationInterface()
					.isAssignableFrom(foundConfigInterface)) {

					candidate = entry;
				}
			}
		}
		if (candidate == null) {
			throw new IllegalArgumentException("No binding found for configuration interface '"
				+ searchedConfigInterface + "'");
		}

		return candidate;
	}

	protected void assertEqualsXML(String s1, String s2) {
		Document doc1 = DOMUtil.parse(s1);
		Document doc2 = DOMUtil.parse(s2);
		XMLCompare xmlCompare = new XMLCompare(new AssertProtocol(), false, FilterFactory.trueFilter());
		xmlCompare.assertEqualsNode(doc1, doc2);
	}

	protected String toXML(ConfigurationItem config) throws XMLStreamException {
		return toXML(getDescriptors(), config);
	}

	protected String toXML(Map<String, ConfigurationDescriptor> descriptors, ConfigurationItem config)
			throws XMLStreamException {
		StringWriter buffer = new StringWriter();
		Entry<String, ConfigurationDescriptor> entry = getDescriptorBinding(descriptors, config);
		try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
			String tag = entry.getKey();
			Class<? extends ConfigurationItem> type = (Class<? extends ConfigurationItem>) entry.getValue()
					.getConfigurationInterface();
			writer.write(tag, type, config);
		}
		return buffer.toString();
	}

	protected ConfigurationItem fromXML(String xml) throws ConfigurationException {
		return fromXML(getDescriptors(), xml);
	}

	protected ConfigurationItem fromXML(Map<String, ConfigurationDescriptor> descriptors, String xml)
			throws ConfigurationException {
		CharacterContent content = CharacterContents.newContent(xml);
		return new ConfigurationReader(context, descriptors).setSource(content).read();
	}

	protected ConfigurationItem throughXML(ConfigurationItem config) throws XMLStreamException {
		String xml = toXML(config);
		
		try {
			ConfigurationItem configRestored = fromXML(xml);
			return configRestored;
		} catch (Throwable ex) {
			BasicTestCase.fail("Cannot read back serialized configuration: " + xml, ex);
			return null;
		}
	}

	/**
	 * Asserts that parsing the given config xml {@link String} fails and the error message has the
	 * specified content.
	 * <p>
	 * The exception thrown during parsing or one of it's {@link Throwable#getCause() causes} has to
	 * be matched by the given {@link Pattern}.
	 * <p>
	 * More precisely: The {@link Pattern} has to match a part of one of the messages. To enforce,
	 * that the whole message has to be matched by the pattern, surround it with "\A" and "\z", for
	 * example.
	 * </p>
	 * 
	 * @param message
	 *        If the assert fails, this message starts the error message.
	 * @param configXml
	 *        The illegal config xml {@link String}.
	 * @param errorMessagePattern
	 *        Is not allowed to be null.
	 * @param rootItem
	 *        The {@link ConfigurationItem} that is represented by the root element. The tag name is
	 *        its simple class name. Is not allowed to be null.
	 */
	@SuppressWarnings("unchecked")
	public void assertIllegalXml(String message, String configXml, Pattern errorMessagePattern,
			Class<? extends ConfigurationItem> rootItem) {
		assertIllegalXml(message, configXml, errorMessagePattern, new Class[] { rootItem });
	}

	/**
	 * Asserts that parsing the given config xml {@link String} fails and the error message has the
	 * specified content.
	 * <p>
	 * The exception thrown during parsing or one of it's {@link Throwable#getCause() causes} has to
	 * be matched by the given {@link Pattern}.
	 * <p>
	 * More precisely: The {@link Pattern} has to match a part of one of the messages. To enforce,
	 * that the whole message has to be matched by the pattern, surround it with "\A" and "\z", for
	 * example.
	 * </p>
	 * 
	 * @param message
	 *        If the assert fails, this message starts the error message.
	 * @param configXml
	 *        The illegal config xml {@link String}.
	 * @param errorMessagePattern
	 *        Is not allowed to be null.
	 * @param globalDescriptor
	 *        The list of {@link ConfigurationItem}s that can appear as the root element. The tag
	 *        name is their simple class name. Is not allowed to be null or empty.
	 */
	public void assertIllegalXml(String message, String configXml, Pattern errorMessagePattern,
			Class<? extends ConfigurationItem>... globalDescriptor) {
		try {
			read(createDescriptorMap(globalDescriptor), configXml);
		} catch (RuntimeException ex) {
			BasicTestCase.assertErrorMessage(message, errorMessagePattern, ex);
			return;
		} catch (ConfigurationException ex) {
			BasicTestCase.assertErrorMessage(message, errorMessagePattern, ex);
			return;
		} catch (AssertionFailedError ex) {
			BasicTestCase.assertErrorMessage(message, errorMessagePattern, ex);
			return;
		}
		fail(message);
	}

	/**
	 * Creates a {@link Map} from the simple class names to the {@link ConfigurationDescriptor}s of
	 * the given {@link ConfigurationItem}s.
	 * 
	 * @param globalDescriptors
	 *        Is not allowed to be null. Is not allowed to contain null. Is allowed to be empty,
	 *        which causes the result {@link Map} to be empty.
	 * @return Never null.
	 */
	public static Map<String, ConfigurationDescriptor> createDescriptorMap(
			Class<? extends ConfigurationItem>... globalDescriptors) {
		Map<String, ConfigurationDescriptor> result = new HashMap<>();
		for (Class<? extends ConfigurationItem> configItemClass : globalDescriptors) {
			ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(configItemClass);
			result.put(configItemClass.getSimpleName(), descriptor);
		}
		return result;
	}

	/**
	 * Asserts that building the {@link ConfigurationDescriptor} for the given
	 * {@link ConfigurationItem} type fails.
	 * 
	 * @param message
	 *        Is allowed to be <code>null</code> or empty.
	 * @param type
	 *        Is not allowed to be empty.
	 */
	public static void assertIllegal(String message, String errorPart, Class<? extends ConfigurationItem> type) {
		try {
			TypedConfiguration.getConfigurationDescriptor(type);
		} catch (IllegalArgumentException ex) {
			BasicTestCase.assertContains(message, errorPart, ex.getMessage());
			return;
		}
		fail(message + " Building the " + ConfigurationDescriptor.class.getSimpleName() + " for an illegal "
			+ ConfigurationItem.class.getSimpleName() + " did not fail. Interface: " + type.getCanonicalName());
	}

	/**
	 * Compares the given {@link ConfigurationItem}s with
	 * {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED}, which is the most useful
	 * comparison mode.
	 */
	public static void assertNotEquals(ConfigurationItem expected, ConfigurationItem actual) {
		assertNotEquals("", expected, actual);
	}

	/**
	 * Compares the given {@link ConfigurationItem}s with
	 * {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED}, which is the most useful
	 * comparison mode.
	 */
	public static void assertNotEquals(String message, ConfigurationItem expected, ConfigurationItem actual) {
		String detailMessage =
			" Configurations are expected to be not equal, but are equal. Expected: " + expected + " Actual: " + actual;
		if (expected == actual) {
			fail(message + detailMessage);
		}
		if (expected == null || actual == null) {
			return;
		}
		assertFalse(message + detailMessage, ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expected, actual));
	}

	/**
	 * Compares the given {@link ConfigurationItem}s with
	 * {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED}, which is the most useful
	 * comparison mode.
	 */
	public static void assertEquals(ConfigurationItem expected, ConfigurationItem actual) {
		assertEquals("", expected, actual);
	}

	/**
	 * Compares the given {@link ConfigurationItem}s with
	 * {@link ConfigEquality#INSTANCE_ALL_BUT_DERIVED}, which is the most useful
	 * comparison mode.
	 */
	public static void assertEquals(String message, ConfigurationItem expected, ConfigurationItem actual) {
		BasicTestCase.assertEquals(message, expected, actual);
	}

	/** Convenience shortcut for: {@link TypedConfiguration#newConfigItem(Class)} */
	protected static <T extends ConfigurationItem> T create(Class<T> type) {
		return TypedConfiguration.newConfigItem(type);
	}

	/**
	 * Convenience shortcut for: {@link InstantiationContext#getInstance(PolymorphicConfiguration)}
	 */
	protected <T> T getInstance(PolymorphicConfiguration<T> config) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	protected static Test suite(Class<? extends TestCase> testClass) {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(null, testClass,
			ResourcesModule.Module.INSTANCE));
	}

}
