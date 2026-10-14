/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.xio.importer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.Resources;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.xio.importer.XmlImporter;
import com.top_logic.xio.importer.binding.ModelBinding;
import com.top_logic.xio.importer.binding.TransientModelBinding;

/**
 * Test case for {@link XmlImporter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXmlImporter extends TestCase {

	public void testImport() throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter1.model.xml",
			"testXmlImporter1.importer.xml",
			"testXmlImporter1.data.xml");
		assertNotNull(result);

		@SuppressWarnings("unchecked")
		List<? extends TLObject> bs = (List<? extends TLObject>) result.tValueByName("bs");
		assertEquals(3, bs.size());

		TLObject b2 = (TLObject) result.tValueByName("defaultValue");
		assertSame(bs.get(1), b2);
		assertEquals("b2", b2.tValueByName("name"));
	}

	public void testImport2() throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter2.model.xml",
			"testXmlImporter2.importer.xml",
			"testXmlImporter2.data.xml");
		assertNotNull(result);

		@SuppressWarnings("unchecked")
		List<? extends TLObject> vs = (List<? extends TLObject>) result.tValueByName("values");
		assertEquals(2, vs.size());

		TLObject v0 = vs.get(0);
		assertEquals(1, collection(v0.tValueByName("as")).size());
		assertEquals(1, collection(v0.tValueByName("bs")).size());
		assertEquals(1, collection(v0.tValueByName("others")).size());

		TLObject v1 = vs.get(1);
		assertEquals(0, collection(v1.tValueByName("as")).size());
		assertEquals(2, collection(v1.tValueByName("bs")).size());
		assertEquals(0, collection(v1.tValueByName("others")).size());
	}

	public void testMultipleIdAttribute() throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter3-MultipleIdAttribute.model.xml",
			"testXmlImporter3-MultipleIdAttribute.importer.xml",
			"testXmlImporter3-MultipleIdAttribute.data.xml");

		checkResult(result);
	}

	public void testTextContentID() throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter4-TextContentID.model.xml",
			"testXmlImporter4-TextContentID.importer.xml",
			"testXmlImporter4-TextContentID.data.xml");

		checkResult(result);
	}

	private void checkResult(TLObject result) {
		assertNotNull(result);

		List<? extends TLObject> vs = list(result.tValueByName("all"));
		assertEquals(7, vs.size());

		TLObject a1 = vs.get(0);
		assertEquals("a1", a1.tValueByName("name"));

		List<? extends TLObject> a1others = list(a1.tValueByName("others"));
		assertEquals(2, a1others.size());

		TLObject a2 = a1others.get(0);
		assertEquals("a2", a2.tValueByName("name"));

		TLObject a7 = a1others.get(1);
		assertEquals("a7", a7.tValueByName("name"));

		List<? extends TLObject> a7others = list(a7.tValueByName("others"));
		assertEquals(5, a7others.size());
		assertEquals(a1, a7others.get(0));
		assertEquals("a5", a7others.get(4).tValueByName("name"));
	}

	public void testJoinDuplicatesLinksOnce() throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter7-JoinDuplicates.model.xml",
			"testXmlImporter7-JoinDuplicates.importer.xml",
			"testXmlImporter7-JoinDuplicates.data.xml");
		assertNotNull(result);

		Collection<? extends TLObject> set = collection(result.tValueByName("set"));
		assertEquals(2, set.size());

		List<? extends TLObject> list = list(result.tValueByName("list"));
		assertEquals(2, list.size());

		TLObject a1 = list.get(0);
		assertEquals("a1", a1.tValueByName("name"));
		assertEquals("second", a1.tValueByName("value"));
		assertEquals("a2", list.get(1).tValueByName("name"));
		assertTrue(set.contains(a1));
		assertTrue(set.contains(list.get(1)));
	}

	public void testI18NImport()throws XMLStreamException, IOException {
		TLObject result = importXml(
			"testXmlImporter5-i18n.model.xml",
			"testXmlImporter5-i18n.importer.xml",
			"testXmlImporter5-i18n.data.xml");

		assertEquals("my-obj", result.tValueByName("name"));
		assertEquals(ResKey.builder().add(Locale.ENGLISH, "My object").add(Locale.GERMAN, "Mein Objekt").build(),
			result.tValueByName("label"));
	}

	public void testImportWithContext() throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		I18NBundle logResources = Resources.getLogInstance();
		XmlImporter importer =
			XmlImporter.newInstance(log.asI18NLog(logResources), resource("testXmlImporter6-context.importer.xml"));
		TLModel model = loadModel(log, "testXmlImporter6-context.model.xml");

		TLClass containerType =
			(TLClass) model.getModule("test.com.top_logic.xio.importer.context").getType("Container");
		TLObject container = TransientObjectFactory.INSTANCE.createObject(containerType, null, null, null);

		ModelBinding binding = new TransientModelBinding(model, false);

		// The import declaration does not create a root object but links the imported items into the
		// scope. The scope is supplied through the context argument.
		importer.importModel(binding,
			new StreamSource(resource("testXmlImporter6-context.data.xml").getStream()), container);

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(3, elements.size());
		assertEquals("x", elements.get(0).tValueByName("name"));
		assertEquals("y", elements.get(1).tValueByName("name"));
		assertEquals("z", elements.get(2).tValueByName("name"));
	}

	public void testResolveExisting() throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		I18NBundle logResources = Resources.getLogInstance();
		XmlImporter importer = XmlImporter.newInstance(log.asI18NLog(logResources),
			resource("testXmlImporter8-ResolveExisting.importer.xml"));
		TLModel model = loadModel(log, "testXmlImporter8-ResolveExisting.model.xml");

		TLModule module = model.getModule("test.com.top_logic.xio.importer.test8");
		TLClass containerType = (TLClass) module.getType("Container");
		TLClass itemType = (TLClass) module.getType("Item");

		TLObject container = TransientObjectFactory.INSTANCE.createObject(containerType, null, null, null);
		container.tUpdateByName("evaluations", 0);
		TLObject e1 = existingItem(container, itemType, "e1");
		TLObject e2 = existingItem(container, itemType, "e2");
		TLObject e3 = existingItem(container, itemType, "e3");

		ModelBinding binding = new TransientModelBinding(model, false);
		importer.importModel(binding,
			new StreamSource(resource("testXmlImporter8-ResolveExisting.data.xml").getStream()), container);

		// The existing objects are computed once, although the handler is applied to each item and
		// several IDs are not found.
		assertEquals(1, ((Number) container.tValueByName("evaluations")).intValue());

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(6, elements.size());

		// Joined with the existing object instead of creating a new one.
		assertSame(e1, elements.get(0));
		assertEquals("updated", e1.tValueByName("name"));
		assertSame(e2, elements.get(1));
		assertEquals("old-e2", e2.tValueByName("name"));

		// An unknown ID creates a new object, a reference resolves to an existing object.
		TLObject n1 = elements.get(3);
		assertEquals("new1", n1.tValueByName("name"));
		assertSame(e2, n1.tValueByName("other"));

		TLObject n2 = elements.get(4);
		assertEquals("new2", n2.tValueByName("name"));
		assertSame(n1, n2.tValueByName("other"));

		// Outside the scope of the lookup, the existing object is not found.
		TLObject duplicate = elements.get(5);
		assertNotSame(e3, duplicate);
		assertEquals("e3", duplicate.tValueByName("extId"));
		assertEquals("duplicate", duplicate.tValueByName("name"));
		assertEquals("old-e3", e3.tValueByName("name"));
	}

	private TLObject existingItem(TLObject container, TLClass itemType, String extId) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(itemType, null, null, null);
		result.tUpdateByName("extId", extId);
		result.tUpdateByName("name", "old-" + extId);
		container.tAddByName("elements", result);
		return result;
	}

	@SuppressWarnings("unchecked")
	private Collection<? extends TLObject> collection(Object v) {
		return v == null ? Collections.emptyList() : (Collection<TLObject>) v;
	}

	@SuppressWarnings("unchecked")
	private List<? extends TLObject> list(Object v) {
		return v == null ? Collections.emptyList() : (List<TLObject>) v;
	}

	private TLObject importXml(String modelResource, String importerResource, String xmlResource)
			throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		I18NBundle logResources = Resources.getLogInstance();
		XmlImporter importer =
			XmlImporter.newInstance(log.asI18NLog(logResources), resource(importerResource));
		TLModel model = loadModel(log, modelResource);
		ModelBinding binding = new TransientModelBinding(model, false);
		return (TLObject) importer.importModel(binding, new StreamSource(resource(xmlResource).getStream()));
	}

	private TLModel loadModel(Protocol log, String modelResource) {
		TLModel model = new TLModelImpl();
		DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE,
			FileManager.getInstance().getData("/WEB-INF/model/tl.core.model.xml"));
		DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE,
			FileManager.getInstance().getData("/WEB-INF/model/tl.model.i18n.model.xml"));
		DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE, resource(modelResource));
		return model;
	}

	private ClassRelativeBinaryContent resource(String importerResource) {
		return new ClassRelativeBinaryContent(TestXmlImporter.class, importerResource);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestXmlImporter.class, SearchBuilder.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE));
	}
}
