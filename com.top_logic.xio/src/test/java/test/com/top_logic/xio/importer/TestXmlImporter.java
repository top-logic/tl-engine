/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.xio.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.basic.util.ResourcesModule;
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

		// Forward references in a multi-reference.
		assertEquals(List.of(bs.get(1), bs.get(2)), bs.get(0).tValueByName("next"));
		assertEquals(List.of(bs.get(0)), bs.get(2).tValueByName("next"));
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

	public void testResolveExistingPartitions() throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		XmlImporter importer = XmlImporter.newInstance(log.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter15-ResolveExistingPartitions.importer.xml"));
		TLModel model = loadModel(log, "testXmlImporter15-ResolveExistingPartitions.model.xml");

		TLModule module = model.getModule("test.com.top_logic.xio.importer.test15");
		TLClass containerType = (TLClass) module.getType("Container");
		TLClass itemType = (TLClass) module.getType("Item");

		TLObject container = TransientObjectFactory.INSTANCE.createObject(containerType, null, null, null);
		container.tUpdateByName("partitions", "");
		TLObject p1a = existingItem(container, itemType, "P1_a");
		TLObject p1b = existingItem(container, itemType, "P1_b");
		TLObject p2a = existingItem(container, itemType, "P2_a");
		TLObject p3x = existingItem(container, itemType, "P3_x");

		ModelBinding binding = new TransientModelBinding(model, false);
		importer.importModel(binding,
			new StreamSource(resource("testXmlImporter15-ResolveExistingPartitions.data.xml").getStream()),
			container);

		// The existing objects are computed once per referenced partition, also for a partition
		// without objects. The partition of an ID with an empty partition key is not computed, nor
		// is a partition that is not referenced.
		assertEquals("P1,P2,Q,", container.tValueByName("partitions"));

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(9, elements.size());

		// Joined with the existing object instead of creating a new one, and linked only once.
		assertSame(p1a, elements.get(0));
		assertEquals("updated-a", p1a.tValueByName("name"));
		assertSame(p1b, elements.get(1));
		assertEquals("old-P1_b", p1b.tValueByName("name"));
		assertSame(p2a, elements.get(2));
		assertSame(p3x, elements.get(3));

		// Unknown IDs create new objects, references resolve to existing objects of their
		// partition.
		TLObject p2n = elements.get(4);
		assertEquals("P2_n", p2n.tValueByName("extId"));
		assertSame(p2a, p2n.tValueByName("other"));

		TLObject p1c = elements.get(5);
		assertEquals("P1_c", p1c.tValueByName("extId"));
		assertSame(p1b, p1c.tValueByName("other"));

		TLObject z = elements.get(6);
		assertEquals("_z", z.tValueByName("extId"));
		assertEquals("new-z", z.tValueByName("name"));

		TLObject q1 = elements.get(7);
		assertEquals("Q_1", q1.tValueByName("extId"));
		TLObject q2 = elements.get(8);
		assertEquals("Q_2", q2.tValueByName("extId"));
		assertSame(q1, q2.tValueByName("other"));
	}

	public void testUnlessKnownAndIgnoreUnresolved() throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		XmlImporter importer = XmlImporter.newInstance(log.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter9-UnlessKnown.importer.xml"));
		TLModel model = loadModel(log, "testXmlImporter9-UnlessKnown.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test9");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);
		TLObject e1 = existingItem(container, (TLClass) module.getType("Item"), "e1");

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource("testXmlImporter9-UnlessKnown.data.xml").getStream()), container);

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(3, elements.size());

		// Known through the resolve-existing lookup: skipped with its content.
		assertSame(e1, elements.get(0));
		assertEquals("old-e1", e1.tValueByName("name"));
		assertEquals(0, list(e1.tValueByName("children")).size());

		// Unknown: imported with its content. The second occurrence is known and skipped.
		TLObject n1 = elements.get(1);
		assertEquals("new1", n1.tValueByName("name"));
		List<? extends TLObject> n1Children = list(n1.tValueByName("children"));
		assertEquals(1, n1Children.size());
		assertEquals("c2", n1Children.get(0).tValueByName("extId"));

		// An ignored unresolved reference stays unset.
		assertNull(n1.tValueByName("other"));
		assertNull(n1.tValueByName("note"));

		// An ignored unresolved ID is left out of a multi-reference, the others (including a
		// forward reference) are stored in order.
		assertEquals(List.of(e1, elements.get(2)), n1.tValueByName("others"));

		// An ignorable reference to an ID defined later is resolved.
		TLObject n2 = elements.get(2);
		assertEquals("new2", n2.tValueByName("name"));
		assertSame(n1, n2.tValueByName("other"));
		assertEquals("forward", n2.tValueByName("note"));
	}

	public void testUnlessKnownUpdate() throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		XmlImporter importer = XmlImporter.newInstance(log.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter14-UnlessKnownUpdate.importer.xml"));
		TLModel model = loadModel(log, "testXmlImporter14-UnlessKnownUpdate.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test14");
		TLClass itemType = (TLClass) module.getType("Item");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);
		TLObject e1 = germanItem(container, itemType, "e1", "Eins", "de");
		TLObject c1 = germanChild(e1, itemType, "c1", "Schritt eins");
		TLObject e2 = germanItem(container, itemType, "e2", "Zwei", "de,en");
		TLObject c2 = germanChild(e2, itemType, "c2", "Schritt zwei");
		TLObject e3 = germanItem(container, itemType, "e3", "Drei", "de");
		TLObject c3 = germanChild(e3, itemType, "c3", "Schritt drei");

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource("testXmlImporter14-UnlessKnownUpdate.data.xml").getStream()), container);

		// Only the unknown item is added.
		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(4, elements.size());
		assertSame(e1, elements.get(0));
		assertSame(e2, elements.get(1));
		assertSame(e3, elements.get(2));

		// The condition holds: The known item and its known child get the translation of the
		// language of the import. An unknown child of a known item is not created.
		assertTranslations(Map.of(Locale.GERMAN, "Eins", Locale.ENGLISH, "One"), e1.tValueByName("name"));
		assertEquals(List.of(c1), e1.tValueByName("children"));
		assertTranslations(Map.of(Locale.GERMAN, "Schritt eins", Locale.ENGLISH, "Step one"),
			c1.tValueByName("text"));

		// The condition does not hold: The known item is skipped with its content.
		assertTranslations(Map.of(Locale.GERMAN, "Zwei"), e2.tValueByName("name"));
		assertEquals(List.of(c2), e2.tValueByName("children"));
		assertTranslations(Map.of(Locale.GERMAN, "Schritt zwei"), c2.tValueByName("text"));

		// An update without condition is applied, content it does not consume is skipped.
		assertTranslations(Map.of(Locale.GERMAN, "Drei", Locale.ENGLISH, "Three"), e3.tValueByName("name"));
		assertEquals(List.of(c3), e3.tValueByName("children"));
		assertTranslations(Map.of(Locale.GERMAN, "Schritt drei"), c3.tValueByName("text"));

		// An unknown item is created with its content.
		TLObject n1 = elements.get(3);
		assertEquals("n1", n1.tValueByName("extId"));
		assertTranslations(Map.of(Locale.ENGLISH, "New"), n1.tValueByName("name"));
		List<? extends TLObject> n1Children = list(n1.tValueByName("children"));
		assertEquals(1, n1Children.size());
		assertEquals("n1c1", n1Children.get(0).tValueByName("extId"));
		assertTranslations(Map.of(Locale.ENGLISH, "New step"), n1Children.get(0).tValueByName("text"));
	}

	private TLObject germanItem(TLObject container, TLClass itemType, String extId, String name, String languages) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(itemType, null, null, null);
		result.tUpdateByName("extId", extId);
		result.tUpdateByName("name", ResKey.builder().add(Locale.GERMAN, name).build());
		result.tUpdateByName("languages", languages);
		container.tAddByName("elements", result);
		return result;
	}

	private TLObject germanChild(TLObject parent, TLClass itemType, String extId, String text) {
		TLObject result = TransientObjectFactory.INSTANCE.createObject(itemType, null, null, null);
		result.tUpdateByName("extId", extId);
		result.tUpdateByName("text", ResKey.builder().add(Locale.GERMAN, text).build());
		parent.tAddByName("children", result);
		return result;
	}

	public void testUnresolvedReported() throws XMLStreamException, IOException {
		BufferingProtocol importLog = new BufferingProtocol();
		XmlImporter importer = XmlImporter.newInstance(importLog.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter9-UnlessKnown.importer.xml"));
		TLModel model = loadModel(new AssertProtocol(), "testXmlImporter9-UnlessKnown.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test9");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource("testXmlImporter9-UnlessKnown-errors.data.xml").getStream()), container);

		String errors = String.join("\n", importLog.getErrors());
		assertEquals(errors, 4, importLog.getErrors().size());
		assertTrue(errors, errors.contains("\"missing1\""));
		assertTrue(errors, errors.contains("\"missing2\""));
		assertTrue(errors, errors.contains("\"missing5\""));
		assertTrue(errors, errors.contains("\"missing6\""));

		// A required unresolved ID is reported and left out of a multi-reference.
		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(2, elements.size());
		TLObject strictItem = elements.get(0);
		TLObject n1 = elements.get(1);
		assertEquals("n1", n1.tValueByName("extId"));
		assertEquals(List.of(n1), strictItem.tValueByName("others"));
	}

	public void testSecondContainerReported() throws XMLStreamException, IOException {
		BufferingProtocol importLog = new BufferingProtocol();
		XmlImporter importer = XmlImporter.newInstance(importLog.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter11-SecondContainer.importer.xml"));
		TLModel model = loadModel(new AssertProtocol(), "testXmlImporter11-SecondContainer.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test11");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource("testXmlImporter11-SecondContainer.data.xml").getStream()), container);

		// Linking an object into a second container is reported with the location of the second
		// occurrence, the import continues.
		String errors = String.join("\n", importLog.getErrors());
		assertEquals(errors, 1, importLog.getErrors().size());
		assertTrue(errors, errors.startsWith("In line 5 "));
		assertTrue(errors, errors.contains("already part of"));
		assertTrue(errors, errors.contains("test11:Container#elements"));
		assertTrue(errors, errors.contains("test11:Item#children"));

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(3, elements.size());
		TLObject top1 = elements.get(0);
		TLObject parent1 = elements.get(1);
		TLObject parent2 = elements.get(2);
		assertEquals("top1", top1.tValueByName("name"));

		// The object stays in its first container.
		assertSame(container, top1.tContainer());
		List<? extends TLObject> children1 = list(parent1.tValueByName("children"));
		assertEquals(1, children1.size());
		assertFalse(children1.contains(top1));

		// Different objects in different containers are linked.
		TLObject child1 = children1.get(0);
		assertEquals("child1", child1.tValueByName("name"));
		assertSame(parent1, child1.tContainer());
		List<? extends TLObject> children2 = list(parent2.tValueByName("children"));
		assertEquals(1, children2.size());
		TLObject child2 = children2.get(0);
		assertEquals("child2", child2.tValueByName("name"));
		assertSame(parent2, child2.tContainer());
	}

	public void testUnknownElementsWarned() throws XMLStreamException, IOException {
		BufferingProtocol importLog = new BufferingProtocol();
		TLObject container = importUnknownElements(importLog, "testXmlImporter12-UnknownElements.data.xml");

		// An unknown element at the top level and one inside a leaf element without own content
		// handlers are both reported with their location, the import continues.
		assertEquals(String.join("\n", importLog.getErrors()), 0, importLog.getErrors().size());
		List<String> warnings = unknownElementMessages(importLog);
		assertEquals(warnings.toString(), 2, warnings.size());
		assertEquals("In line 4 column 21: Unknown element \"unknown-nested\" skipped.", warnings.get(0));
		assertEquals("In line 7 column 16: Unknown element \"unknown-top\" skipped.", warnings.get(1));

		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(2, elements.size());
		assertEquals("i1", elements.get(0).tValueByName("name"));
		TLObject s1 = elements.get(1);
		assertEquals("s1", s1.tValueByName("name"));
		List<? extends TLObject> children = list(s1.tValueByName("children"));
		assertEquals(1, children.size());
		assertEquals("c1", children.get(0).tValueByName("name"));
	}

	public void testUnknownElementsNestedSetting() throws XMLStreamException, IOException {
		BufferingProtocol importLog = new BufferingProtocol();
		TLObject container = importUnknownElements(importLog, "testXmlImporter12-UnknownElements-error.data.xml");

		// The inner setting applies to its subtree, the outer setting applies again after it.
		List<String> errors = importLog.getErrors();
		assertEquals(String.join("\n", errors), 1, errors.size());
		assertEquals("In line 4 column 20: Unknown element \"unknown-strict\".", errors.get(0));
		List<String> warnings = unknownElementMessages(importLog);
		assertEquals(warnings.toString(), 1, warnings.size());
		assertEquals("In line 6 column 18: Unknown element \"unknown-after\" skipped.", warnings.get(0));

		// A configured default handler takes every element without a tag entry.
		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(2, elements.size());
		TLObject l1 = elements.get(1);
		assertEquals("l1", l1.tValueByName("name"));
		List<? extends TLObject> children = list(l1.tValueByName("children"));
		assertEquals(1, children.size());
		assertEquals("d1", children.get(0).tValueByName("name"));
	}

	private TLObject importUnknownElements(BufferingProtocol importLog, String dataResource)
			throws XMLStreamException, IOException {
		XmlImporter importer = XmlImporter.newInstance(importLog.asI18NLog(Resources.getLogInstance()),
			resource("testXmlImporter12-UnknownElements.importer.xml"));
		TLModel model = loadModel(new AssertProtocol(), "testXmlImporter12-UnknownElements.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test12");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource(dataResource).getStream()), container);
		return container;
	}

	private static List<String> unknownElementMessages(BufferingProtocol log) {
		return log.getInfos().stream().filter(message -> message.contains("Unknown element")).toList();
	}

	public void testMultiValueCase() throws XMLStreamException, IOException {
		// The trailing empty entry of the case "0, false," selects it for the empty value.
		assertEquals(List.of("on", "on", "off", "off", "unknown", "off"),
			importMultiValueCase("testXmlImporter13-MultiValueCase.importer.xml"));
	}

	public void testEmptyCaseValue() throws XMLStreamException, IOException {
		// The case with an empty value selects the empty attribute value.
		assertEquals(List.of("on", "on", "off", "off", "unknown", "empty"),
			importMultiValueCase("testXmlImporter13-MultiValueCase-empty.importer.xml"));
	}

	/**
	 * Imports the multi-value test data with the given importer and returns the states of the
	 * items that have a flag attribute.
	 */
	private List<Object> importMultiValueCase(String importerResource) throws XMLStreamException, IOException {
		Protocol log = new AssertProtocol();
		XmlImporter importer = XmlImporter.newInstance(log.asI18NLog(Resources.getLogInstance()),
			resource(importerResource));
		TLModel model = loadModel(log, "testXmlImporter13-MultiValueCase.model.xml");
		TLModule module = model.getModule("test.com.top_logic.xio.importer.test13");
		TLObject container =
			TransientObjectFactory.INSTANCE.createObject((TLClass) module.getType("Container"), null, null, null);

		importer.importModel(new TransientModelBinding(model, false),
			new StreamSource(resource("testXmlImporter13-MultiValueCase.data.xml").getStream()), container);

		// Each value of a case selects that case, other values select the default.
		List<? extends TLObject> elements = list(container.tValueByName("elements"));
		assertEquals(List.of("i1", "i2", "i3", "i4", "i5", "i6", "i7"),
			elements.stream().map(item -> item.tValueByName("name")).toList());

		// Without the attribute, no case runs.
		assertNull(elements.get(6).tValueByName("state"));

		List<Object> states = new ArrayList<>();
		for (TLObject item : elements.subList(0, 6)) {
			states.add(item.tValueByName("state"));
		}
		return states;
	}

	public void testMultiValueCaseDuplicate() {
		String errors = assertInvalidImporter("testXmlImporter13-MultiValueCase-duplicate.importer.xml");
		assertTrue(errors, errors.contains("Duplicate case value 'true'"));
	}

	public void testLanguageFromInput() throws XMLStreamException, IOException {
		assertEquals("Test expects English as fallback language.", Locale.ENGLISH,
			ResourcesModule.getInstance().getFallbackLocale());

		TLObject root = importXml(
			"testXmlImporter10-Language.model.xml",
			"testXmlImporter10-Language.importer.xml",
			"testXmlImporter10-Language.data.xml");

		List<? extends TLObject> as = list(root.tValueByName("as"));
		assertEquals(3, as.size());

		// Language from the variable bound by the outer element, reduced to the language.
		TLObject a1 = as.get(0);
		assertTranslations(Map.of(Locale.GERMAN, "Eins"), a1.tValueByName("label"));
		// Fixed language tag, reduced to the language.
		assertTranslations(Map.of(Locale.GERMAN, "Fest"), a1.tValueByName("fixed"));
		// Language from an attribute.
		assertTranslations(Map.of(Locale.FRENCH, "Texte", Locale.ENGLISH, "Text"), a1.tValueByName("text"));
		// The missing fallback translation is filled.
		assertTranslations(Map.of(Locale.GERMAN, "Nur deutsch", Locale.ENGLISH, "Nur deutsch"),
			a1.tValueByName("other"));

		// An inner element overrides the language for its subtree.
		TLObject a2 = as.get(1);
		assertTranslations(Map.of(Locale.ENGLISH, "Two"), a2.tValueByName("label"));
		assertTranslations(Map.of(Locale.ENGLISH, "Only English"), a2.tValueByName("other"));

		// Without an own language, the outer language stays in effect. An existing fallback
		// translation is not overwritten.
		TLObject a3 = as.get(2);
		assertTranslations(Map.of(Locale.GERMAN, "Drei"), a3.tValueByName("label"));
		assertTranslations(Map.of(Locale.ENGLISH, "English", Locale.GERMAN, "Deutsch"), a3.tValueByName("other"));
	}

	public void testLanguageDefaultAndEmptyValues() throws XMLStreamException, IOException {
		TLObject root = importXml(
			"testXmlImporter10-Language.model.xml",
			"testXmlImporter10-Language-defaults.importer.xml",
			"testXmlImporter10-Language-defaults.data.xml");

		List<? extends TLObject> as = list(root.tValueByName("as"));
		assertEquals(3, as.size());

		// Without a language in the input, the default language is used.
		TLObject a1 = as.get(0);
		assertTranslations(Map.of(Locale.GERMAN, "Eins"), a1.tValueByName("label"));
		assertTranslations(Map.of(Locale.FRENCH, "Texte", Locale.ENGLISH, "Text"), a1.tValueByName("text"));
		// Empty and blank values are not stored, not even for the fallback language.
		assertNull(a1.tValueByName("fixed"));
		assertNull(a1.tValueByName("other"));

		// A language from the input wins over the default language.
		TLObject a2 = as.get(1);
		assertTranslations(Map.of(Locale.ENGLISH, "Two"), a2.tValueByName("label"));

		// An empty value without a language is skipped without error.
		TLObject a3 = as.get(2);
		assertNull(a3.tValueByName("label"));
		assertNull(a3.tValueByName("other"));
	}

	public void testDefaultLanguageWithFixedLanguage() {
		String errors = assertInvalidImporter("testXmlImporter10-Language-default-fixed.importer.xml");
		assertTrue(errors, errors.contains("must not be given together"));
	}

	public void testInvalidDefaultLanguage() {
		String errors = assertInvalidImporter("testXmlImporter10-Language-default-invalid.importer.xml");
		assertTrue(errors, errors.contains("Invalid language"));
	}

	private static void assertTranslations(Map<Locale, String> expected, Object resKey) {
		assertEquals(expected, ((LiteralKey) resKey).getTranslations());
	}

	public void testLanguageOptionsExclusive() {
		assertLanguageOptionsInvalid("testXmlImporter10-Language-none.importer.xml");
		assertLanguageOptionsInvalid("testXmlImporter10-Language-two.importer.xml");
	}

	private void assertLanguageOptionsInvalid(String importerResource) {
		String errors = assertInvalidImporter(importerResource);
		assertTrue(errors, errors.contains("Exactly one of"));
	}

	private String assertInvalidImporter(String importerResource) {
		BufferingProtocol log = new BufferingProtocol();
		try {
			XmlImporter.readImportConfig(new DefaultInstantiationContext(log), resource(importerResource));
			fail("Invalid importer configuration not detected: " + importerResource);
		} catch (AbortExecutionException | ConfigurationError ex) {
			// Expected.
		}
		return String.join("\n", log.getErrors());
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
