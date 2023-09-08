/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.ConfigurationSchemaConstants.*;
import static java.util.Collections.*;
import static test.com.top_logic.basic.BasicTestCase.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.config.TestTypedConfiguration.TestConfig;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Test case for {@link ConfigurationReader}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigurationReader extends AbstractTypedConfigurationTestCase implements TypedConfigurationSzenario {

	private static final String NL = "\n";
	
	public void testParse1() throws ConfigurationException {
		A.Config config = readA("<a/>");

		assertNotNull(config);
		assertEquals(0, config.getP());
	}

	public void testParse2() throws ConfigurationException {
		A.Config config = readA("<a p='9'/>");
		
		assertNotNull(config);
		assertEquals(9, config.getP());
	}
	
	public void testParse3() throws ConfigurationException {
		A.Config a = readA(
			"<a p='1'>" + NL
			+	"<a p='2'>" + NL
			+		"<b class='" + D.class.getName() + "' x='101' z='Hello moon!'>" + NL
			+		"</b>" + NL
			+	"</a>" + NL
			+	"<b class='" + C.class.getName() + "' x='100' y='200'>" + NL
			+	"</b>" + NL
			+	"<others>" + NL
			+		"<a p='3'/>" + NL
			+		"<a p='4'/>" + NL
			+		"<a p='5'/>" + NL
			+	"</others>" + NL
			+	"<indexed>" + NL
			+		"<a p='6'/>" + NL
			+		"<a p='7'/>" + NL
			+		"<a>" + NL
			+			"<!-- A documented plain value. -->" + NL
			+			"<p>8</p>" + NL
			+		"</a>" + NL
			+	"</indexed>" + NL
			+ "</a>" + NL);
		
		assertNotNull(a);
		A.Config a_a = a.getOtherConfig();
		B.Config a_b = a.getBConfig();
		assertNotNull(a_a);
		assertNotNull(a_b);
		assertEquals(6, a_b.location().getLine());
		
		B.Config a_a_b = a_a.getBConfig();
		assertNotNull(a_a_b);
		
		assertEquals(1, a.getP());
		assertEquals(2, a_a.getP());

		assertEquals(101, a_a_b.getX());
		assertInstanceof(a_a_b, D.Config.class);
		
		assertEquals(100, a_b.getX());
		assertInstanceof(a_b, C.Config.class);
		
		assertNotNull(a.getOthers());
		assertEquals(3, a.getOthers().size());
		assertEquals(3, a.getOthers().get(0).getP());
		assertEquals(4, a.getOthers().get(1).getP());
		assertEquals(5, a.getOthers().get(2).getP());
		
		assertEquals(11, a.getOthers().get(2).location().getLine());
		
		assertNotNull(a.getIndexed());
		assertEquals(3, a.getIndexed().size());
		assertNotNull(a.getIndexed().get(Integer.valueOf(6)));
		assertNotNull(a.getIndexed().get(Integer.valueOf(7)));
		assertNotNull(a.getIndexed().get(Integer.valueOf(8)));
		assertEquals(6, a.getIndexed().get(Integer.valueOf(6)).getP());
		assertEquals(7, a.getIndexed().get(Integer.valueOf(7)).getP());
		assertEquals(8, a.getIndexed().get(Integer.valueOf(8)).getP());
		
		assertEquals(16, a.getIndexed().get(Integer.valueOf(8)).location().getLine());
	}

	public void testListAsProperty() throws ConfigurationException {
		A.Config aEmpty = readA(
			"<a b-configs='' bs=''/>" + NL);
		assertEquals(0, aEmpty.getBConfigs().size());
		assertEquals(0, aEmpty.getBs().size());

		A.Config aSingleton = readA(
			"<a b-configs='" + C.class.getName() + "' bs='" + C.class.getName() + "'/>" + NL);
		assertEquals(1, aSingleton.getBConfigs().size());
		assertEquals(1, aSingleton.getBs().size());
		assertEquals(C.class, aSingleton.getBConfigs().get(0).getImplementationClass());
		assertEquals(C.class, aSingleton.getBs().get(0).getClass());

		A.Config a = readA(
			"<a b-configs='" + C.class.getName() + ", " + D.class.getName() + "' bs='" + C.class.getName() + ", "
				+ D.class.getName() + "'/>" + NL);
		assertEquals(2, a.getBConfigs().size());
		assertEquals(2, a.getBs().size());
		assertEquals(C.class, a.getBConfigs().get(0).getImplementationClass());
		assertEquals(C.class, a.getBs().get(0).getClass());
		assertEquals(D.class, a.getBConfigs().get(1).getImplementationClass());
		assertEquals(D.class, a.getBs().get(1).getClass());
	}

	public void testFailConfigListAsProperty() throws ConfigurationException {
		initFailureTest();
		try {
			readA("<a e-configs='some-value'/>" + NL);
			fail("Expected failure.");
		} catch (ConfigurationException | ExpectedFailure ex) {
			// Note: There is a bug in the way the instantiation context is combined with the
			// underlying protocol resulting in a ConfigurationException instead of the
			// ExpectedFailure exception.
			BasicTestCase.assertContains(null,
				Pattern.compile("Inline configuration for property 'e-configs'.* without specified format"),
				ex.getMessage());
		}
	}

	public void testDuplicateListEntryOnAdd() throws ConfigurationException {
		try {
			readA(
				"<a xmlns:config='" + CONFIG_NS + "'>" + NL
					+ "<indexed-list>" + NL
					+ "<a p='6'/>" + NL
					+ "<a p='6'/>" + NL
					+ "</indexed-list>" + NL
					+ "</a>" + NL);
			fail("Expected list entry with duplicate keys failed");
		} catch (AssertionFailedError ex) {
			// protocol is AssertionProtocol
			if (ex.getMessage().contains("Multiple entries with key")) {
				// expected;
			} else {
				// check whether message has changed
				throw ex;
			}
		}

	}

	public void testDuplicateMapEntryOnAdd() throws ConfigurationException {
		try {
			readA(
					"<a xmlns:config='" + CONFIG_NS + "'>" + NL
					+	"<indexed>" + NL
					// must set operation as default is add or update
					+		"<a config:" + MAP_OPERATION + "='add' p='6'/>" + NL
					+		"<a config:" + MAP_OPERATION + "='add' p='6'/>" + NL
					+	"</indexed>" + NL
					+ "</a>" + NL);
			fail("Expected map entry with duplicate keys failed");
		} catch (AssertionFailedError ex) {
			// protocol is AssertionProtocol
			if (ex.getMessage().contains("")) {
				// expected;
			} else {
				// check whether message has changed
				throw ex;
			}
		}

	}

	public void testDuplicateMapEntryOnUpdate() throws ConfigurationException {
		try {
			readA(
				"<a xmlns:config='" + CONFIG_NS + "'>" + NL
					+ "<indexed>" + NL
					+ "<a p='6'/>" + NL
					+ "<a p='6'/>" + NL
					+ "</indexed>" + NL
					+ "</a>" + NL);
			fail("Expected map entry with duplicate keys in the same configuration failed");
		} catch (AssertionFailedError ex) {
			// protocol is AssertionProtocol
			if (ex.getMessage().contains("Multiple entries with key")) {
				// expected;
			} else {
				// check whether message has changed
				throw ex;
			}
		}

	}

	public void testNoSourcesButFallback() throws ConfigurationException {
		A.Config fallback = create(A.Config.class);
		Map<String, ConfigurationDescriptor> globalDescriptors = emptyMap();
		ConfigurationItem newItem = new ConfigurationReader(context, globalDescriptors)
			.setBaseConfig(fallback)
			.setSources(Collections.<Content> emptyList())
			.read();
		assertNotSame("ConfigReader must never return the fallback item as newly read item.", fallback, newItem);
		assertInstanceof(newItem, A.Config.class);
	}

	public void testNoSourcesButFallbackBuilder() throws ConfigurationException {
		ConfigBuilder fallback = TypedConfiguration.createConfigBuilder(ConfigurationItem.class);
		Map<String, ConfigurationDescriptor> globalDescriptors = emptyMap();
		ConfigBuilder newItem = new ConfigurationReader(context, globalDescriptors)
			.setBaseConfig(fallback)
			.setSources(Collections.<Content> emptyList())
			.readConfigBuilder();
		assertNotSame("ConfigReader must never return the fallback item as newly read item.", fallback, newItem);
	}

	public void testTicket20124() throws ConfigurationException {
		{
			D.Config d = readD(
				"<d z='Hello \uD841\uDDAA\uD841\uDDAA\uE066!'/>"
				);
			assertEquals("Ticket #20124: Problem parsing document with supplementary plane character.",
				"Hello \uD841\uDDAA\uD841\uDDAA\uE066!", d.getZ());
		}
	}

	public void testTicket2729() throws ConfigurationException {
		{
			D.Config d = (D.Config) readB(
					"<b class='" + D.class.getName() + "' z='Hello moon!'/>"
			);
			assertEquals("Ticket #2729: String value expected.", "Hello moon!", d.getZ());
		}
		
		{
			D.Config d = (D.Config) readB(
					"<b class='" + D.class.getName() + "'>"
				+ 		"<z>Hello moon!</z>"
				+	"</b>"
			);
			
			assertEquals("Ticket #2729: String value expected.", "Hello moon!", d.getZ());
		}
	}

	public void testTicket2733() throws ConfigurationException, ParseException {
		D.Config d = (D.Config) readB(
			"<b class='" + D.class.getName() + "' date='2010-04-01'/>"
		);
		assertTrue("Ticket #2733: Date value expected.", XmlDateTimeFormat.INSTANCE.parseObject("2010-04-01").equals(d.getDate()));
	}
	
	public void testTicket8807() throws ConfigurationException {
		A.Config a = readA(
			"<a class='" + A.class.getName() + "'><boolean>true</boolean></a>"
		);
		assertTrue("Ticket #8807: Boolean value not parsed.", a.getBoolean());
	}
	
	@SuppressWarnings("unchecked")
	public void testTicket10843() {
		String message = "An xml with multiple root tags does not cause an error.";
		String xml = ""
			+ XML_DECLARATION
			+ "<ConfigurationItem />"
			+ "<ConfigurationItem />";
		Pattern expectedError = Pattern.compile("(?i)root");
		assertIllegalXml(message, xml, expectedError, ConfigurationItem.class);
	}

	@SuppressWarnings("unchecked")
	public void testTicket10843_WhitespaceBetweenTags() {
		String message = "An xml with multiple root tags does not cause an error.";
		String xml = ""
			+ XML_DECLARATION
			+ "<ConfigurationItem />"
			+ " "
			+ "<ConfigurationItem />";
		Pattern expectedError = Pattern.compile("(?i)root");
		assertIllegalXml(message, xml, expectedError, ConfigurationItem.class);
	}

	@SuppressWarnings("unchecked")
	public void testTicket10843_WhitespaceBeforeEOF() throws ConfigurationException {
		String xml = ""
			+ XML_DECLARATION
			+ "<ConfigurationItem />"
			+ " ";
		// XML is valid, reading must not fail:
		ConfigurationItem item = read(createDescriptorMap(ConfigurationItem.class), xml);
		assertNotNull(item);
	}

	@SuppressWarnings("unchecked")
	public void testTicket10843_CommentBetweenTags() {
		String message = "An xml with multiple root tags does not cause an error.";
		String xml = ""
			+ XML_DECLARATION
			+ "<ConfigurationItem />"
			+ " <!-- This is a comment. -->"
			+ "<ConfigurationItem />";
		Pattern expectedError = Pattern.compile("(?i)root");
		assertIllegalXml(message, xml, expectedError, ConfigurationItem.class);
	}

	@SuppressWarnings("unchecked")
	public void testTicket10843_CommentBeforeEOF() throws ConfigurationException {
		String xml = ""
			+ XML_DECLARATION
			+ "<ConfigurationItem />"
			+ " <!-- This is a comment. -->";
		// XML is valid, reading must not fail:
		ConfigurationItem item = read(createDescriptorMap(ConfigurationItem.class), xml);
		assertNotNull(item);
	}

	public interface InvalidConfiguration extends ConfigurationItem {

		@Subtypes({})
		PolymorphicConfiguration<Object> getInner();

		public class InnerImpl {

			@NoImplementationClassGeneration
			public interface Config extends PolymorphicConfiguration<Object> {

				int invalidProperty(String foo, Object bar);

			}

			/**
			 * Creates a {@link TestConfigurationReader.InvalidConfiguration.InnerImpl} from
			 * configuration.
			 * 
			 * @param context
			 *        The context for instantiating sub configurations.
			 * @param config
			 *        The configuration.
			 */
			@CalledByReflection
			public InnerImpl(InstantiationContext context, Config config) {
				// Ignore.
			}

		}
	}
	
	public void testTicket16019_ReadInvalidConfigurationInterface() {
		initFailureTest();

		String xml = ""
				+ XML_DECLARATION
			+ "<InvalidConfiguration>"
			+ "<inner class='" + InvalidConfiguration.InnerImpl.class.getName() + "'>"
			+ "</inner>"
				+ "</InvalidConfiguration>";
		try {
			@SuppressWarnings("unchecked")
			ConfigurationItem item = read(createDescriptorMap(InvalidConfiguration.class), xml);
			assertNull(item);
			fail("Configuration model is invalid, must fail.");
		} catch (ConfigurationException ex) {
			assertContains("Has getter with to multiple parameters", ex.getMessage());
		}
	}
	
	public void testReadWithoutGlobalConfigDescriptor() throws ConfigurationException {
		Map<String, ConfigurationDescriptor> noGlobalDescriptors = Collections.<String, ConfigurationDescriptor> emptyMap();
		String xml = ""
			+ XML_DECLARATION
			+ "<e " + XML_CONFIG_NAMESPACE_DECLARATION + " config:interface='" + EConfig.class.getName() + "'/>";
		ConfigurationItem item = read(noGlobalDescriptors, xml);
		assertNotNull(item);

		xml = ""
			+ XML_DECLARATION
			+ "<e " + XML_CONFIG_NAMESPACE_DECLARATION + " class='" + B.class.getName() + "'/>";
		try {
			item = read(noGlobalDescriptors, xml);
			assertNull(item);
			fail("Configuration model is invalid, must fail.");
		} catch (AssertionFailedError ex) {
			// expected
			assertErrorMessage("Unexpected error message.", Pattern.compile("(U|u)nexpected element"), ex);
		}

	}

	public void testResourceLocation() throws Throwable {
		TestConfig config = (TestConfig) readConfiguration("definitionFileAnnotation.xml");
		assertTrue(config.location().getResource().contains(TestConfigurationReader.class.getName()));
		assertTrue(config.location().getResource().contains("definitionFileAnnotation.xml"));
		assertTrue(config.location().getResource(), config.location().getResource()
			.contains("workspace:/com.top_logic/" + ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/basic/config/file1.xml"));
		List<TestConfig> children = config.getListWithoutDefault();

		// Resource strings must be shared by the configuration reader to avoid excessive memory
		// usage for location information.
		assertSame(children.get(0).location().getResource(), config.location().getResource());

		String enhancedResource = children.get(1).location().getResource();
		assertTrue(enhancedResource
			.contains("workspace:/com.top_logic/" + ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/basic/config/file1.xml"));
		assertTrue(enhancedResource
			.contains("workspace:/com.top_logic/" + ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/basic/config/file2.xml"));
		assertTrue(enhancedResource
			.indexOf("workspace:/com.top_logic/" + ModuleLayoutConstants.SRC_TEST_DIR
				+ "/test/com/top_logic/basic/config/file1.xml") < enhancedResource
					.indexOf("workspace:/com.top_logic/" + ModuleLayoutConstants.SRC_TEST_DIR
						+ "/test/com/top_logic/basic/config/file2.xml"));
	}

	public void testDerivedIndex() throws ConfigurationException {
		DerivedIndex a = read(
			"<derived-index><values><value first='x' second='y'/></values></derived-index>");

		assertEquals("x", a.getValues().get("xy").getFirst());
	}

	private A.Config readA(String source) throws ConfigurationException {
		return (A.Config) read(GLOBAL_CONFIGS, source);
	}

	private B.Config readB(String source) throws ConfigurationException {
		return (B.Config) read(GLOBAL_CONFIGS, source);
	}

	private D.Config readD(String source) throws ConfigurationException {
		return (D.Config) read(GLOBAL_CONFIGS, source);
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return GLOBAL_CONFIGS;
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestConfigurationReader.class));
	}
	
}
