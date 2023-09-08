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
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.Resources;
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
		TLModel model = new TLModelImpl();
		DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE,
			FileManager.getInstance().getData("/WEB-INF/model/tl.core.model.xml"));
		DynamicModelService.extendModel(log, model, TransientObjectFactory.INSTANCE, resource(modelResource));
		ModelBinding binding = new TransientModelBinding(model);
		return (TLObject) importer.importModel(binding, new StreamSource(resource(xmlResource).getStream()));
	}

	private ClassRelativeBinaryContent resource(String importerResource) {
		return new ClassRelativeBinaryContent(TestXmlImporter.class, importerResource);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestXmlImporter.class, SearchBuilder.Module.INSTANCE));
	}
}
