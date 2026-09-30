/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.instance.importer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataURI;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.instance.exporter.XMLInstanceExporter;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.AccountResolver;
import com.top_logic.model.instance.importer.resolver.PersistentObjectResolver;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link XMLInstanceImporter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXMLInstanceImporter extends TLModelTest {

	public void testImport() throws ConfigurationException {
		Content instanceSource = ClassRelativeBinaryContent.withSuffix(TestXMLInstanceImporter.class, "scenario.xml");
		TLModel model = getModel();

		Log testLog = new BufferingProtocol();

		XMLInstanceImporter importer = importer(testLog);
		ObjectsConf configs = XMLInstanceImporter.loadConfig(instanceSource);

		importer.importInstances(configs);

		if (testLog.hasErrors()) {
			fail(testLog.getFirstProblem().toString());
		}

		TLObject a1 = importer.getObject("a1");
		TLObject a2 = importer.getObject("a2");
		TLObject a3 = importer.getObject("a3");
		TLObject a4 = importer.getObject("a4");
		assertNotNull(a1);
		assertNotNull(a2);

		assertEquals("A1", get(a1, "name"));
		assertEquals(true, get(a1, "bool"));
		assertEquals(13, get(a1, "int"));
		assertEquals(42.0D, get(a1, "double"));
		assertEquals(a1, get(a1, "other"));
		assertEquals(list(a1, a2), get(a1, "others"));
		assertEquals(TLModelUtil.findPart(model, "TestXMLInstanceImporter:MyEnum#A"), get(a4, "enumSingle"));
		assertEquals(
			list(
				TLModelUtil.findPart(model, "TestXMLInstanceImporter:MyEnum#A"),
				TLModelUtil.findPart(model, "TestXMLInstanceImporter:MyEnum#B")),
			get(a4, "enumMultiple"));

		assertEquals(
			list(
				TLModelUtil.findType(model, "TestXMLInstanceImporter:MyEnum"),
				((TLEnumeration) TLModelUtil.findType(model, "TestXMLInstanceImporter:MyEnum")).getClassifiers().get(0),
				TLModelUtil.findType(model, "TestXMLInstanceImporter:A"),
				TLModelUtil.findPart(model, "TestXMLInstanceImporter:A#any"),
				model.getModule("TestXMLInstanceImporter")),
			get(a2, "any"));

		assertEquals(list(PersonManager.getManager().getRoot()), get(a3, "any"));

		ObjectConf a4Conf = TypedConfiguration.newConfigItem(ObjectConf.class);
		a4Conf.setId("x4");
		a4Conf.setType("TestXMLInstanceImporter:A");
		AttributeValueConf anyConf = TypedConfiguration.newConfigItem(AttributeValueConf.class);
		anyConf.setName("any");
		GlobalRefConf refConf = TypedConfiguration.newConfigItem(GlobalRefConf.class);
		refConf.setKind(PersistentObjectResolver.KIND);
		refConf.setId(PersonManager.getManager().getRoot().tId().asString());
		anyConf.getCollectionValue().add(refConf);
		a4Conf.getAttributes().add(anyConf);

		ObjectsConf config = TypedConfiguration.newConfigItem(ObjectsConf.class);
		config.getObjects().add(a4Conf);

		importer.importInstances(config);

		// Test case for #22779
		ConfigValue configValue = (ConfigValue) get(a1, "item");
		assertEquals(true, configValue.getBoolean());
		assertEquals(Double.MAX_VALUE, configValue.getDouble());
		assertEquals(Long.MAX_VALUE, configValue.getLong());
		assertEquals("string", configValue.getString());

		TLObject x4 = importer.getObject("x4");
		assertEquals(list(PersonManager.getManager().getRoot()), get(x4, "any"));
	}

	/**
	 * Tests that an internationalized attribute given as plain <code>value</code> attribute is
	 * imported with the format of its application type.
	 */
	public void testImportI18NLiteral() throws ConfigurationException {
		BufferingProtocol testLog = new BufferingProtocol();
		XMLInstanceImporter importer = importer(testLog);

		importer.importInstances(i18nImport("c1", "#(\"Travel\"@en, \"Reise\"@de)"));
		assertFalse(testLog.getErrors().toString(), testLog.hasErrors());

		ResKey label = (ResKey) get(importer.getObject("c1"), "label");
		assertNotNull("Internationalized attribute imported as empty value.", label);
		assertEquals("Travel", ResKeyUtil.getTranslation(label, Locale.ENGLISH));
		assertEquals("Reise", ResKeyUtil.getTranslation(label, Locale.GERMAN));
	}

	/**
	 * Tests that a plain <code>value</code> of an internationalized attribute that is no literal
	 * text is read as resource key.
	 * 
	 * <p>
	 * The resource key itself is not persisted, since the storage of an internationalized attribute
	 * keeps the translations of the resolved key. Therefore, the parsed value is inspected directly.
	 * </p>
	 */
	public void testImportI18NResourceKey() {
		BufferingProtocol testLog = new BufferingProtocol();

		String key = "test.label.key";
		Object value = XMLInstanceImporter.parse(log(testLog), i18nType(), key);
		assertFalse(testLog.getErrors().toString(), testLog.hasErrors());

		assertEquals(ResKey.decode(key), value);
	}

	/**
	 * Tests that a malformed value of an internationalized attribute is reported as error instead
	 * of failing the import.
	 */
	public void testImportI18NInvalidValue() throws ConfigurationException {
		BufferingProtocol testLog = new BufferingProtocol();
		XMLInstanceImporter importer = importer(testLog);

		importer.importInstances(i18nImport("c2", "#("));
		assertTrue("Expected an error for a malformed internationalized value.", testLog.hasErrors());

		assertNull(get(importer.getObject("c2"), "label"));
	}

	private ObjectsConf i18nImport(String id, String label) throws ConfigurationException {
		return XMLInstanceImporter.loadConfig(CharacterContents.newContent(
			"<objects>"
				+ "<object id=\"" + id + "\" type=\"TestXMLInstanceImporter:A\">"
				+ "<attribute name=\"label\" value='" + label + "'/>"
				+ "</object>"
				+ "</objects>",
			"i18n-plain-value.xml"));
	}

	private TLPrimitive i18nType() {
		return (TLPrimitive) TLModelUtil.findPart(getModel(), "TestXMLInstanceImporter:A#label").getType();
	}

	private XMLInstanceImporter importer(Log testLog) {
		TLFactory factory = ModelService.getInstance().getFactory();
		XMLInstanceImporter importer = new XMLInstanceImporter(getModel(), factory);
		importer.setLog(log(testLog));
		importer.addResolver(AccountResolver.KIND,
			new AccountResolver());
		importer.addResolver(PersistentObjectResolver.KIND,
			new PersistentObjectResolver(PersistencyLayer.getKnowledgeBase()));
		return importer;
	}

	/**
	 * Tests that content type and file name of a binary value survive the export/import round trip.
	 */
	public void testBinaryRoundTrip() {
		TLPrimitive binaryType = binaryType();
		String name = "Mein Bild; mit Ümläuten.svg";
		byte[] contents = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8);
		BinaryData data = BinaryDataFactory.createBinaryData(contents, "image/svg+xml", name);

		String serialized = XMLInstanceExporter.serialize(binaryType, data);
		assertTrue(serialized, BinaryDataURI.isDataURI(serialized));

		BufferingProtocol testLog = new BufferingProtocol();
		BinaryData parsed = (BinaryData) XMLInstanceImporter.parse(log(testLog), binaryType, serialized);
		assertFalse(testLog.getErrors().toString(), testLog.hasErrors());

		assertEquals("image/svg+xml", parsed.getContentType());
		assertEquals(name, parsed.getName());
		assertEquals(new String(contents, StandardCharsets.UTF_8), readContents(parsed));
	}

	/**
	 * Tests that a binary value given as bare base64 string is imported as octet stream without
	 * name.
	 */
	public void testBinaryFromBase64() {
		BufferingProtocol testLog = new BufferingProtocol();
		BinaryData parsed = (BinaryData) XMLInstanceImporter.parse(log(testLog), binaryType(), "QUJD");
		assertFalse(testLog.getErrors().toString(), testLog.hasErrors());

		assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, parsed.getContentType());
		assertEquals(BinaryData.NO_NAME, parsed.getName());
		assertEquals("ABC", readContents(parsed));
	}

	/**
	 * Tests that a malformed data URI is reported as error.
	 */
	public void testInvalidBinary() {
		BufferingProtocol testLog = new BufferingProtocol();
		assertNull(XMLInstanceImporter.parse(log(testLog), binaryType(), "data:image/png,QUJD"));
		assertTrue("Expected an error for a malformed data URI.", testLog.hasErrors());
	}

	private TLPrimitive binaryType() {
		return (TLPrimitive) TLModelUtil.findType(getModel(), TypeSpec.BINARY_TYPE);
	}

	private static I18NLog log(Log testLog) {
		return testLog.asI18NLog(ResourcesModule.getInstance().getBundle(ResourcesModule.getLogLocale()));
	}

	private static String readContents(BinaryData data) {
		try {
			return new String(StreamUtilities.readStreamContents(data), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
	}

	private Object get(TLObject a1, String attr) {
		TLStructuredTypePart part = a1.tType().getPart(attr);
		return a1.tValue(part);
	}

	@Override
	protected TLModel setUpModel() {
		return ModelService.getApplicationModel();
	}

	@Override
	protected TLFactory setUpFactory() {
		return ModelService.getInstance().getFactory();
	}

	@Override
	protected void tearDownModel() {
		// Ignore.
	}

	public static Test suite() {
		return suiteTransient(
			KBSetup.getSingleKBTest(
				ServiceTestSetup.createSetup(TestXMLInstanceImporter.class, PersonManager.Module.INSTANCE)));
	}
}
