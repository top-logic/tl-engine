/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.instance.importer;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.resolver.AccountResolver;
import com.top_logic.model.instance.importer.resolver.PersistentObjectResolver;
import com.top_logic.model.instance.importer.schema.AttributeValueConf;
import com.top_logic.model.instance.importer.schema.GlobalRefConf;
import com.top_logic.model.instance.importer.schema.ObjectConf;
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
		TLFactory factory = ModelService.getInstance().getFactory();
		TLModel model = getModel();

		XMLInstanceImporter importer = new XMLInstanceImporter(model, factory);
		importer.addResolver(AccountResolver.KIND,
			new AccountResolver(PersonManager.getManager()));
		importer.addResolver(PersistentObjectResolver.KIND,
			new PersistentObjectResolver(PersistencyLayer.getKnowledgeBase()));
		List<ObjectConf> configs = XMLInstanceImporter.loadConfigs(instanceSource);

		importer.importInstances(configs);

		if (importer.getLog().hasErrors()) {
			fail(importer.getLog().getFirstProblem().toString());
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
		anyConf.getReferences().add(refConf);
		a4Conf.getAttributes().add(anyConf);

		importer.importInstances(list(a4Conf));

		// Test case for #22779
		ConfigValue configValue = (ConfigValue) get(a1, "item");
		assertEquals(true, configValue.getBoolean());
		assertEquals(Double.MAX_VALUE, configValue.getDouble());
		assertEquals(Long.MAX_VALUE, configValue.getLong());
		assertEquals("string", configValue.getString());

		TLObject x4 = importer.getObject("x4");
		assertEquals(list(PersonManager.getManager().getRoot()), get(x4, "any"));
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
