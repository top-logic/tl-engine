/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import junit.framework.Test;

import test.com.top_logic.model.search.model.testJavaBinding.EnumsDerived;
import test.com.top_logic.model.search.model.testJavaBinding.Primitives;
import test.com.top_logic.model.search.model.testJavaBinding.ReferencesDerived;

import com.top_logic.basic.AliasManager;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.MetaAttributeFactory;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLScope;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.search.expr.CalendarField;
import com.top_logic.model.search.expr.CalendarUpdate;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.ToDate;
import com.top_logic.model.search.expr.ToMillis;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.ToSystemCalendar;
import com.top_logic.model.search.expr.ToUserCalendar;
import com.top_logic.model.search.expr.config.operations.Label;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.supplier.SearchExpressionNow;
import com.top_logic.model.search.expr.supplier.SearchExpressionToday;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test for evaluating {@link SearchExpression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSearchExpression extends AbstractSearchExpressionTest {

	public void testSimpleSearch() {
		with("TestSearchExpression-testSimpleSearch.scenario.xml",
			scenario -> {
				TLObject a1 = scenario.getObject("a1");
				assertNotNull(a1);
				TLObject a2 = scenario.getObject("a2");
				assertNotNull(a2);
				Object context = value(a1, "context");
				assertEquals(scenario.getObject("context"), context);

				assertEquals(a1.tType(), execute(search("x -> $x.type()"), a1));
				assertEquals(a1.tType(), execute(search("x -> $x.type()"), list(a1)));
				assertEquals(a1.tType().getPart("name"), execute(search("t -> $t.attribute('name')"), a1.tType()));

				// Test that implicit flap-map semantics uses lists as intermediate collections.
				TLObject a0 = scenario.getObject("a0");
				TLObject a3 = scenario.getObject("a3");
				assertEquals(25.0,
					execute(search("x -> $x.get(`TestSearchExpression:A#int`).sum()"), list(a0, a1, a2, a3)));

				SearchExpression search1 = search(
					"context -> all(`TestSearchExpression:A`).filter(x -> $context.isEqual($x.get(`TestSearchExpression:A#context`)))");

				assertEquals(set(a1, a2), executeAsSet(search1, context));

				SearchExpression search2 = search(
					"context -> all(`TestSearchExpression:A`).filter(x -> $context.isEqual($x.get(`TestSearchExpression:A#context`)) and $x.get(`TestSearchExpression:A#str`).isEqual('foo'))");

				assertEquals(set(a1), executeAsSet(search2, context));
				assertEquals(set(a1), executeCompiledAsSet(search2, context));

				SearchExpression search3 = search(
					"context -> val -> all(`TestSearchExpression:A`).filter(x -> $context.isEqual($x.get(`TestSearchExpression:A#context`)) and $x.get(`TestSearchExpression:A#str`).isEqual($val))");

				assertEquals(set(a1), executeAsSet(search3, context, "foo"));

				SearchExpression search4 = search(
					"context -> all(`TestSearchExpression:A`).filter(x -> $context.isEqual($x.get(`TestSearchExpression:A#context`)) and $x.get(`TestSearchExpression:A#name`).isEqual('A1'))");

				assertEquals(set(a1), executeAsSet(search4, context));
				assertEquals(set(a1), executeCompiledAsSet(search4, context));

				SearchExpression search5 = search(
					"all(`TestSearchExpression:A`).filter(x -> $x.get(`TestSearchExpression:A#name`) == 'A1').foreach(x -> {$x.set(`TestSearchExpression:A#str`, 'Hello world!'); $x.set(`TestSearchExpression:A#int`, 42);})");
				executeCompiledAsSet(search5, context);

				assertEquals("Hello world!", value(a1, "str"));
				assertEquals("bar", value(a2, "str"));
				assertEquals(42, value(a1, "int"));
				assertEquals(0, value(a2, "int"));

				SearchExpression test = search("x -> if($x.get(`TestSearchExpression:A#str`)=='Hello world!', 1, -1)");
				assertEquals(1.0, executeCompiled(test, a1));
				assertEquals(-1.0, executeCompiled(test, a2));

				SearchExpression search6 =
					search(
						"all(`TestSearchExpression:A`).filter(a -> $a.get(`TestSearchExpression:A#name`).stringEndsWith('2'))");
				assertEquals(set(a2), executeAsSet(search6, context));

				TLStructuredTypePart nameAttr = a1.tType().getPart("name");
				TLStructuredTypePart contextAttr = a1.tType().getPart("context");
				assertEquals(a1.tValue(nameAttr), eval("self -> attr -> $self.get($attr)", a1, nameAttr));
				assertEquals(a1.tValue(contextAttr), eval("self -> attr -> $self.get($attr)", a1, contextAttr));

				assertNotNull(a3);
				Object a3other = a3.tValueByName("other");
				assertNull(a3other);
				assertEquals(null, eval("self -> $self.get(`TestSearchExpression:A#other`)", a3));
				assertEquals("Accessing an attribute of 'null' must not fail.", null,
					eval("self -> $self.get(`TestSearchExpression:A#other`).get(`TestSearchExpression:A#name`)", a3));

				testRefereres(scenario);
				testNameBinding(scenario);
			});
	}

	private void testNameBinding(XMLInstanceImporter scenario) throws ParseException {
		TLObject a0 = scenario.getObject("a0");
		assertEquals(list(a0),
			eval(
				"{ a0 = all(`TestSearchExpression:A`).filter(x -> $x.get(`TestSearchExpression:A#name`) == 'A0'); $a0 }"));
		assertEquals(list(a0),
			eval(
				"{ a0 = all(true ? `TestSearchExpression:A` : null).filter(x -> $x.get(`TestSearchExpression:A#name`) == 'A0'); $a0; }"));
	}

	private void testRefereres(XMLInstanceImporter scenario) throws ParseException {
		TLObject target = scenario.getObject("childContext");
		TLObject a0 = scenario.getObject("a0");
		TLObject otherA1 = scenario.getObject("otherA1");
		TLObject a4 = scenario.getObject("a4");
		TLObject a5 = scenario.getObject("a5");
		TLObject a6 = scenario.getObject("a6");

		assertEquals(set(a0, otherA1),
			toSet((Iterable<?>) eval("self -> $self.referers(`TestSearchExpression:A#context`)", target)));
		assertEquals(set(a4),
			toSet((Iterable<?>) eval("self -> $self.referers(`TestSearchExpression:A#other`)", a0)));
		assertEquals(set(a4),
			toSet((Iterable<?>) eval("ref -> self -> $self.referers($ref)",
				TLModelUtil.findPart("TestSearchExpression:A#other"), a0)));
		assertEquals(set(a5, a6),
			toSet((Iterable<?>) eval("self -> $self.referers(`TestSearchExpression:A#other`)", otherA1)));
		assertEquals(set(a4, a5, a6),
			toSet((Iterable<?>) eval(
				"self -> $self.referers(`TestSearchExpression:A#context`).referers(`TestSearchExpression:A#other`)",
				target)));
		assertEquals(set(a4, a6),
			toSet((Iterable<?>) eval("self -> $self.referers(`TestSearchExpression:A#others`)", a0)));
		assertEquals(set(a5, a6),
			toSet((Iterable<?>) eval(
				"self -> $self.referers(`TestSearchExpression:A#others`).referers(`TestSearchExpression:A#others`)",
				a0)));
	}

	public void testContainer() {
		with("TestSearchExpression-testContainer.scenario.xml", scenario -> {
			TLObject root = scenario.getObject("root");
			TLObject b1 = scenario.getObject("b1");
			TLObject b2 = scenario.getObject("b2");
			TLObject b3 = scenario.getObject("b3");
			TLObject b4 = scenario.getObject("b4");

			assertEquals(root, eval("x -> $x.container()", b1));
			assertEquals(root, eval("x -> $x.container()", b2));
			assertEquals(root, eval("x -> $x.container()", b3));
			assertEquals(null, eval("x -> $x.container()", b4));

			assertEquals(root.tType().getPart("contents"), eval("x -> $x.containerReference()", b1));
			assertEquals(root.tType().getPart("contents"), eval("x -> $x.containerReference()", b2));
			assertEquals(root.tType().getPart("content"), eval("x -> $x.containerReference()", b3));
			assertEquals(null, eval("x -> $x.containerReference()", b4));
		});

	}

	public void testDynamicAll() {
		with("TestSearchExpression-testDynamicGet.scenario.xml",
			scenario -> {
				assertEquals(list("A0"), eval(
					"all("
						+ "`TestSearchExpression`"
						+ ".get(`tl.model:TLModule#types`)"
						+ ".filter(c -> $c.get(`tl.model:TLClass#name`) == 'A'))"
					+ ".filter(x -> $x.get(`TestSearchExpression:A#str`) == 'foo')"
					+ ".map(x -> $x.get(`TestSearchExpression:A#name`))"));
			});
	}

	public void testDynamicGet() {
		with("TestSearchExpression-testDynamicGet.scenario.xml",
			scenario -> {
				assertEquals(list("foo"), eval(
					"all(`TestSearchExpression:A`)" +
						".get(" +
						"   all(`tl.model:TLStructuredTypePart`)" +
						"   .filter(a -> " +
						"      $a.get(`tl.model:TLStructuredTypePart#name`) == 'str' and " +
						"      $a.get(`tl.model:TLStructuredTypePart#owner`) == `TestSearchExpression:A`).singleElement())"));

				TLTypePart part = TLModelUtil.findPart("TestSearchExpression:A#str");
				update("part -> all(`TestSearchExpression:A`).foreach(a -> $a.set($part, 'bar'))", part);
				assertEquals(list("bar"), eval("part -> all(`TestSearchExpression:A`).map(a -> $a.get($part))", part));
			});
	}

	public void testModelNavigation() {
		with("TestSearchExpression-testDynamicGet.scenario.xml",
			scenario -> {
				assertEquals("Ticket #24885: Model navigation failed.",
					list(TLModelUtil.findType("TestSearchExpression:A")), eval(
						"all(`tl.model:TLModule`)" +
							".filter(module -> " +
							"   $module.get(`tl.model:TLModule#name`) == 'TestSearchExpression')" +
							".referers(`tl.model:TLClass#module`)" +
							".filter(type -> " +
							"   $type.get(`tl.model:TLClass#name`) == 'A')"));
			});
	}

	public void testNextId() throws ParseException {
		assertEquals(1.0, update("nextId()"));
		assertEquals(2.0, update("nextId()"));
		assertEquals(3.0, update("nextId(null)"));

		assertEquals(1.0, update("nextId('foo')"));
		assertEquals(2.0, update("nextId('foo')"));

		assertEquals(1.0, update("nextId('foo', 'bar')"));
		assertEquals(2.0, update("nextId('foo', 'bar')"));

		assertEquals(1.0, update("nextId(`TestSearchExpression:A`)"));
		assertEquals(2.0, update("nextId(`TestSearchExpression:A`)"));
	}

	public void testNextIdWithoutTransaction() throws ParseException {
		try {
			eval("nextId()");
		} catch (TopLogicException ex) {
			assertEquals(
				com.top_logic.model.search.expr.config.operations.arithmetic.I18NConstants.ERROR_NO_TRANSACTION__EXPR,
				ex.getErrorKey().plain());
		}
	}

	public void testModelLiterals() throws ParseException {
		TLModel model = ModelService.getApplicationModel();
		assertNotNull(model);
		TLModule module = model.getModule("TestSearchExpression");
		assertNotNull(module);
		assertEquals(module, execute(search("`TestSearchExpression`")));
		TLStructuredType type = (TLStructuredType) module.getType("A");
		assertNotNull(type);
		assertEquals(type, execute(search("`TestSearchExpression:A`")));
		TLStructuredTypePart attribute = type.getPart("name");
		assertNotNull(attribute);
		assertEquals(attribute, execute(search("`TestSearchExpression:A#name`")));
		TLEnumeration classification = (TLEnumeration) module.getType("MyEnum");
		assertNotNull(classification);
		assertEquals(classification, execute(search("`TestSearchExpression:MyEnum`")));
		TLClassifier classifier = classification.getClassifier("A");
		assertNotNull(classifier);
		assertEquals(classifier, execute(search("`TestSearchExpression:MyEnum#A`")));
		TLObject singleton = module.getSingleton("ROOT");
		assertNotNull(singleton);
		assertEquals(singleton, execute(search("`TestSearchExpression#ROOT`")));
	}

	public void testAllClassifiers() throws ParseException {
		TLEnumeration classification = (TLEnumeration) TLModelUtil.findType("TestSearchExpression:MyEnum");
		assertEquals(classification.getClassifiers(), execute(search("all(`TestSearchExpression:MyEnum`)")));
		SearchExpression fun = search("x -> $x.instanceOf(`TestSearchExpression:MyEnum`)");
		assertTrue((Boolean) execute(fun, classification.getClassifiers().get(0)));
		assertFalse((Boolean) execute(fun, "foobar"));
	}

	public void testLocalType() {
		with("TestSearchExpression-testLocalType.scenario.xml",
			scenario -> {
				TLScope context = scenario.getObject("context");
				KnowledgeItem contextHandle = context.tHandle();
				String contextTable = contextHandle.tTable().getName();

				TLClass localType;
				TLClassProperty propertyX;
				KnowledgeBase kb = kb();
				try (Transaction tx = kb.beginTransaction()) {
					TLModel model = ModelService.getInstance().getModel();
					TLModule module = model.getModule("TestSearchExpression");
					localType = MetaElementFactory.getInstance().createMetaElement(module, context, "Local", kb);
					propertyX = MetaAttributeFactory.getInstance().createClassProperty(kb);
					propertyX.setName("x");
					propertyX.setType(model.getModule("tl.core").getType("Integer"));
					propertyX.setDefinition(propertyX);
					localType.getLocalClassParts().add(propertyX);
					TLClassProperty propertyName = MetaAttributeFactory.getInstance().createClassProperty(kb);
					propertyName.setName("name");
					propertyName.setType(model.getModule("tl.core").getType("String"));
					propertyName.setDefinition(propertyName);
					localType.getLocalClassParts().add(propertyName);
					tx.commit();
				}

				TLObject local;
				try (Transaction tx = kb.beginTransaction()) {
					local = ModelService.getInstance().getFactory().createObject(localType);
					local.tUpdateByName("name", "local");
					local.tUpdateByName("x", 42);
					tx.commit();
				}

				// Make sure, it is deleted with other object in the scenario. Otherwise, the
				// context object cannot be deleted, because it would delete its local type, which
				// cannot be done, because it is used in the allocated object.
				scenario.addObject("local", local);

				String contextBranch = Long.toString(contextHandle.getBranchContext());
				String contextId = contextHandle.getObjectName().toExternalForm();
				assertEquals(localType,
					execute(search("`" + contextTable + "/" + contextBranch + "/" + contextId + ":Local`")));
				assertEquals(propertyX,
					execute(search("`" + contextTable + "/" + contextBranch + "/" + contextId + ":Local#x`")));
				assertEquals(42.0,
					execute(
						search("x -> $x.get(`" + contextTable + "/" + contextBranch + "/" + contextId + ":Local#x`)"),
						local));
			});
	}

	public void testJavaBinding() {
		with("TestSearchExpression-testJavaBinding.scenario.xml", (scenario) -> {
			{
				ReferencesDerived obj = scenario.getObject("ReferencesDerivedSingleton");

				assertRef("A", obj.getRef());
				assertRef("A", obj.getRefMandatory());
				assertRefs(list("A"), obj.getRefMultiple());
				assertRefs(list("A"), obj.getRefMultipleMandatory());
				assertRefs(list("A"), obj.getRefOrdered());
				assertRefs(list("A"), obj.getRefOrderedBag());
				assertRefs(list("A"), obj.getRefBag());
				assertRefs(list("A"), obj.getRefOrderedBag());
			}

			{
				EnumsDerived obj = scenario.getObject("EnumsDerivedSingleton");

				assertEnum("A", obj.getRef());
				assertEnum("A", obj.getRefMandatory());
				assertEnums(list("A"), obj.getRefMultiple());
				assertEnums(list("A"), obj.getRefMultipleMandatory());
				assertEnums(list("A"), obj.getRefOrdered());
				assertEnums(list("A"), obj.getRefOrderedBag());
				assertEnums(list("A"), obj.getRefBag());
				assertEnums(list("A"), obj.getRefOrderedBag());
			}

			{
				ReferencesDerived obj = scenario.getObject("ReferencesDerivedMultiple");

				assertRefs(list("A", "B", "C"), obj.getRefMultiple());
				assertRefs(list("A", "B", "C"), obj.getRefMultipleMandatory());
				assertRefs(list("A", "B", "C"), obj.getRefOrdered());
				assertRefs(list("A", "B", "C"), obj.getRefOrderedBag());
				assertRefs(list("A", "B", "C"), obj.getRefBag());
				assertRefs(list("A", "B", "C"), obj.getRefOrderedBag());

				try {
					obj.getRef();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE,
						ex.getErrorKey().plain());
				}
				try {
					obj.getRefMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE,
						ex.getErrorKey().plain());
				}
			}

			{
				EnumsDerived obj = scenario.getObject("EnumsDerivedMultiple");

				assertEnums(list("A", "B", "C"), obj.getRefMultiple());
				assertEnums(list("A", "B", "C"), obj.getRefMultipleMandatory());
				assertEnums(list("A", "B", "C"), obj.getRefOrdered());
				assertEnums(list("A", "B", "C"), obj.getRefOrderedBag());
				assertEnums(list("A", "B", "C"), obj.getRefBag());
				assertEnums(list("A", "B", "C"), obj.getRefOrderedBag());

				try {
					obj.getRef();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE,
						ex.getErrorKey().plain());
				}
				try {
					obj.getRefMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_RESULT_IS_COLLECTION__ATTR_VALUE,
						ex.getErrorKey().plain());
				}
			}

			{
				ReferencesDerived obj = scenario.getObject("ReferencesDerivedEmpty");

				assertRefs(list(), obj.getRefMultiple());
				assertRefs(list(), obj.getRefOrdered());
				assertRefs(list(), obj.getRefOrderedBag());
				assertRefs(list(), obj.getRefBag());
				assertRefs(list(), obj.getRefOrderedBag());

				assertNull(obj.getRef());

				try {
					obj.getRefMultipleMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ,
						ex.getErrorKey().plain());
				}

				try {
					obj.getRefMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ,
						ex.getErrorKey().plain());
				}
			}

			{
				EnumsDerived obj = scenario.getObject("EnumsDerivedEmpty");

				assertEnums(list(), obj.getRefMultiple());
				assertEnums(list(), obj.getRefOrdered());
				assertEnums(list(), obj.getRefOrderedBag());
				assertEnums(list(), obj.getRefBag());
				assertEnums(list(), obj.getRefOrderedBag());

				assertNull(obj.getRef());

				try {
					obj.getRefMultipleMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ,
						ex.getErrorKey().plain());
				}

				try {
					obj.getRefMandatory();
					fail("Expected error.");
				} catch (TopLogicException ex) {
					assertEquals(
						com.top_logic.model.search.providers.I18NConstants.ERROR_SCRIPT_DELIVERED_NO_RESULT_FOR_MANDATORY_ARRTIBUTE__ATTR_OBJ,
						ex.getErrorKey().plain());
				}
			}
		});
	}

	private void assertRef(String name, Primitives ref) {
		assertEquals(name, ref.getName());
	}

	private void assertEnum(String name, TLClassifier ref) {
		assertEquals(name, ref.getName());
	}

	private void assertRefs(List<String> names, Collection<? extends Primitives> ref) {
		List<String> seen = new ArrayList<>();
		for (Primitives p : ref) {
			seen.add(p.getName());
		}
		if (ref instanceof Set<?>) {
			assertEquals(new HashSet<>(names), new HashSet<>(seen));
		} else {
			assertEquals(names, seen);
		}
	}

	private void assertEnums(List<String> names, Collection<? extends TLClassifier> ref) {
		List<String> seen = new ArrayList<>();
		for (TLClassifier p : ref) {
			seen.add(p.getName());
		}
		if (ref instanceof Set<?>) {
			assertEquals(new HashSet<>(names), new HashSet<>(seen));
		} else {
			assertEquals(names, seen);
		}
	}

	public void testInt() throws ParseException {
		assertEquals(7.0, execute(search("7")));
		assertEquals(7.0, execute(search("7.0")));
		assertEquals(Double.valueOf(7_000_000_000_000_000L), execute(search("7_000_000_000_000_000")));
	}

	public void testDouble() throws ParseException {
		assertEquals(7.0, execute(search("7.0")));
	}

	public void testArithmetic() throws ParseException {
		assertEquals(7.0, execute(search("1+2*3")));
		assertEquals(3.0, execute(search("10%7")));
		assertEquals(2.5, execute(search("5/2")));
		assertEquals(2.0, execute(search("floor(5/2)")));
		assertEquals(3.0, execute(search("ceil(5/2)")));
		assertEquals(2.5, execute(search("x -> y -> $x / $y"), 5, 2));
		assertEquals(3.0, execute(search("5-2")));
	}

	public void testArithmeticList() throws ParseException {
		assertEquals(list(7.0, 5.0, 4.0, 3.0), execute(search("[5, 3, 2, 1] + 2")));
		assertEquals(list(3.0, 1.0, 0.0, -1.0), execute(search("[5, 3, 2, 1] - 2")));
		assertEquals(list(10.0, 6.0, 4.0, 2.0), execute(search("[5, 3, 2, 1] * 2")));
		assertEquals(list(2.5, 1.5, 1.0, 0.5), execute(search("[5, 3, 2, 1] / 2")));
		assertEquals(list(1.0, 1.0, 0.0, 1.0), execute(search("[5, 3, 2, 1] % 2")));
	}

	public void testArithmeticString() throws ParseException {
		assertEquals("HelloWorld", execute(search("'Hello' + 'World'")));
		assertEquals("Hello", execute(search("'Hello' + null")));
		assertEquals("Hello", execute(search("null + 'Hello'")));
		assertEquals("12", execute(search("1 + '2'")));
		assertEquals("12", execute(search("'1' + 2")));

		assertEquals(list("Hello!", "World!"), execute(search("['Hello', 'World'] + '!'")));
	}

	public void testArithmeticScenario() {
		with("TestSearchExpression-testArithmetic.scenario.xml", (scenario) -> {
			TLObject d = scenario.getObject("d");
			assertEquals(42.0, d.tValueByName("in"));

			assertEquals(43.0d, d.tValueByName("derivedDouble"));
			assertEquals(43, d.tValueByName("derivedInt"));

			assertEquals(43, d.tValueByName("derivedByte"));
			assertEquals(43, d.tValueByName("derivedShort"));
			assertEquals(43L, d.tValueByName("derivedLong"));
			assertEquals(43.0f, d.tValueByName("derivedFloat"));
		});
	}

	public void testArithmeticScenarioUpdate() {
		with("TestSearchExpression-testArithmetic.scenario.xml", (scenario) -> {
			TLObject d = scenario.getObject("d");

			update(d, 3.0);

			assertEquals(1.5d, d.tValueByName("double"));
			assertEquals(1, d.tValueByName("int"));
			assertEquals(1, d.tValueByName("byte"));
			assertEquals(1, d.tValueByName("short"));
			assertEquals(1L, d.tValueByName("long"));
			assertEquals(1.5f, d.tValueByName("float"));

			double large = ((double) Integer.MAX_VALUE) + 1;
			update(d, 2 * large);

			assertEquals(large, d.tValueByName("double"));
			assertEquals((long) large, d.tValueByName("long"));
			assertEquals((float) large, d.tValueByName("float"));
		});
	}

	private void update(TLObject d, double base) throws ParseException {
		Object result = update("d -> x -> $d" +
			"..set(`TestSearchExpression:D#double`, $x / 2) " +
			"..set(`TestSearchExpression:D#int`, $x / 2) " +
			"..set(`TestSearchExpression:D#byte`, $x / 2) " +
			"..set(`TestSearchExpression:D#short`, $x / 2) " +
			"..set(`TestSearchExpression:D#long`, $x / 2) " +
			"..set(`TestSearchExpression:D#float`, $x / 2) ", d, base);

		assertEquals(d, result);
	}

	private Object update(String expr, Object... args) throws ParseException {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Object result = execute(search(expr), args);
			tx.commit();
			return result;
		}
	}

	public void testNullArithmetic() throws ParseException {
		assertEquals(null, execute(search("1+null")));
		assertEquals(null, execute(search("null+1")));
		assertEquals(null, execute(search("1-null")));
		assertEquals(null, execute(search("null-1")));
		assertEquals(null, execute(search("5/null")));
		assertEquals(null, execute(search("null/5")));
		assertEquals(null, execute(search("5*null")));
		assertEquals(null, execute(search("null*5")));
		assertEquals(null, execute(search("5%null")));
		assertEquals(null, execute(search("null%5")));

		assertEquals(null, execute(search("null+null")));
		assertEquals(null, execute(search("null-null")));
		assertEquals(null, execute(search("null*null")));
		assertEquals(null, execute(search("null/null")));
		assertEquals(null, execute(search("null%null")));
	}

	public void testSum() throws ParseException {
		assertEquals(3.0, execute(search("sum(3)")));
		assertEquals(7.0, execute(search("sum(3,5,-1)")));
		assertEquals(7.0, execute(search("list(3,5,-1).sum()")));
		assertEquals(8.0, execute(search("sum(3,5,null)")));
		assertEquals(6.0, execute(search("sum(list(3,3))")));
		assertEquals(6.0, execute(search("sum(3,3)")));

		assertEquals(0.0, execute(search("sum(list())")));
		assertEquals(0.0, execute(search("sum(list(null))")));
		assertEquals(0.0, execute(search("sum()")));
		assertEquals(0.0, execute(search("sum(null)")));
	}

	public void testMin() throws ParseException {
		assertEquals(3.0, execute(search("min(3)")));
		assertEquals(-1.0, execute(search("min(3,5,-1)")));
		assertEquals(-1.0, execute(search("list(3,5,-1).min()")));
		assertEquals(3.0, execute(search("min(3,5,null)")));
		assertEquals(3.0, execute(search("min(list(3,4))")));
		assertEquals(3.0, execute(search("min(3,4)")));

		assertEquals(null, execute(search("min(list())")));
		assertEquals(null, execute(search("min(list(null))")));
		assertEquals(null, execute(search("min()")));
		assertEquals(null, execute(search("min(null)")));
	}

	public void testMax() throws ParseException {
		assertEquals(3.0, execute(search("max(3)")));
		assertEquals(5.0, execute(search("max(3,5,-1)")));
		assertEquals(5.0, execute(search("list(3,5,-1).max()")));
		assertEquals(5.0, execute(search("max(3,5,null)")));
		assertEquals(4.0, execute(search("max(list(3,4))")));
		assertEquals(4.0, execute(search("max(3,4)")));

		assertEquals(null, execute(search("max(list())")));
		assertEquals(null, execute(search("max(list(null))")));
		assertEquals(null, execute(search("max()")));
		assertEquals(null, execute(search("max(null)")));
	}

	public void testAverage() throws ParseException {
		assertEquals(3.0, execute(search("average(3)")));
		assertEquals(3.0, execute(search("average(3,7,-1)")));
		assertEquals(3.0, execute(search("list(3,7,-1).average()")));
		assertEquals(4.0, execute(search("average(3,5)")));
		assertEquals(3.0, execute(search("average(list(3,3))")));
		assertEquals(3.0, execute(search("average(3,3)")));

		assertEquals(null, execute(search("average()")));
		assertEquals(null, execute(search("average(null)")));
		assertEquals(null, execute(search("average(null, null)")));
		assertEquals(null, execute(search("list(null, null).average()")));
		assertEquals(3.0, execute(search("average(null,3)")));
		assertEquals(3.0, execute(search("average(3, null)")));
		assertEquals(3.0, execute(search("average(null, list(null, 3), null)")));
	}

	public void testBooleanExpressions() throws ParseException {
		assertIsTrue(true);
		assertIsTrue("Hello world!");
		assertIsTrue(list(42));
		assertIsFalse(false);
		assertIsFalse(null);
		assertIsFalse("");
		assertIsFalse(list());

		assertEquals(Boolean.TRUE, execute(search("true")));
		assertEquals(Boolean.TRUE, execute(search("!false")));
		assertEquals(Boolean.TRUE, execute(search("false || true")));
		assertEquals(Boolean.TRUE, execute(search("false or true")));
		assertEquals(Boolean.TRUE, execute(search("true && true")));
		assertEquals(Boolean.TRUE, execute(search("true and true")));

		assertEquals(Boolean.TRUE, execute(search("toBoolean(true)")));
		assertEquals(Boolean.TRUE, execute(search("toBoolean('some value')")));
		assertEquals(Boolean.TRUE, execute(search("toBoolean(list(42))")));
		assertEquals(Boolean.TRUE, execute(search("toBoolean(list(0))")));
		assertEquals(Boolean.TRUE, execute(search("toBoolean(42)")));
		assertEquals(Boolean.TRUE, execute(search("toBoolean(0)")));
		assertEquals(Boolean.FALSE, execute(search("toBoolean(false)")));
		assertEquals(Boolean.FALSE, execute(search("toBoolean('')")));
		assertEquals(Boolean.FALSE, execute(search("toBoolean(null)")));
		assertEquals(Boolean.FALSE, execute(search("toBoolean(list())")));

		assertEquals(Boolean.TRUE, execute(search("null || true")));
		assertEquals(Boolean.TRUE, execute(search("true || null")));

		// This looks little asymmetric,...
		assertEquals(Boolean.FALSE, execute(search("null || false")));
		assertEquals(null, execute(search("false || null")));

		// ... but in a boolean context it behaves the same.
		assertEquals(Boolean.FALSE, execute(search("toBoolean(null || false)")));
		assertEquals(Boolean.FALSE, execute(search("toBoolean(false || null)")));

		assertEquals(null, execute(search("null || null")));
		assertEquals(Boolean.FALSE, execute(search("null && true")));
		assertEquals(Boolean.FALSE, execute(search("true && null")));
		assertEquals(Boolean.FALSE, execute(search("false && null")));
		assertEquals(Boolean.FALSE, execute(search("null && false")));
		assertEquals(Boolean.FALSE, execute(search("null && null")));
		assertEquals(Boolean.TRUE, execute(search("!null")));

		assertEquals(Boolean.FALSE, execute(search("true && (null || false)")));
		assertEquals(Boolean.FALSE, execute(search("true && (false || null)")));
		assertEquals(Boolean.TRUE, execute(search("true && (true || null)")));
		assertEquals(Boolean.TRUE, execute(search("true && (null || true)")));

		// Empty things are treated like null.
		assertEquals(Boolean.TRUE, execute(search("list() == null")));
		assertEquals(Boolean.TRUE, execute(search("'' == null")));

		assertEquals(null, execute(search("null")));
		assertEquals(list(), execute(search("list()")));
		assertEquals("", execute(search("''")));
	}

	public void testIfElse() throws ParseException {
		assertEquals("foo", execute(search("true ? 'foo' : 'bar'")));
		assertEquals("bar", execute(search("false ? 'foo' : 'bar'")));

		assertEquals("foo", execute(search("2 > 1 && 1 == 3 || 3 * 2 + 1 > 0 ? 'foo' : 'bar'")));
		assertEquals("bar", execute(search("2 > 1 && 1 == 3 || 3 * 2 + 1 < 0 ? 'foo' : 'bar'")));
	}

	public void testOrChains() throws ParseException {
		assertEquals("unknown", execute(search("null || 'unknown'")));

		SearchExpression firstNonEmpty = search("x -> y -> $x || $y || 'unknown'");
		assertEquals("unknown", execute(firstNonEmpty, null, ""));
		assertEquals("hello", execute(firstNonEmpty, list(), "hello"));
		assertEquals(list(1, 2), execute(firstNonEmpty, list(), list(1, 2)));
	}

	private void assertIsTrue(Object value) {
		assertTrue(SearchExpression.isTrue(value));
	}

	private void assertIsFalse(Object value) {
		assertTrue(!SearchExpression.isTrue(value));
	}

	public void testNumberFormat() throws ParseException {
		assertEquals("0042", execute(search("numberFormat('0000').format(42)")));
	}

	public void testDateFormat() throws ParseException {
		assertEquals("2019/08", execute(search("dateFormat('y/MM').format(dateTime(2019,8-1,1))")));
	}

	public void testMessageFormat() throws ParseException {
		assertEquals("Value: 05", execute(search("messageFormat('Value: {0,number,00}').format(5)")));
		assertEquals("Value: 05(a)", execute(search("messageFormat('Value: {0,number,00}({1})').format(5, 'a')")));
	}

	public void testParse() throws ParseException {
		assertEquals(42.0, execute(search("numberFormat('0000').parse('0042')")));
	}

	/**
	 * Test for {@link SearchExpressionNow}
	 */
	public void testNow() throws ParseException {
		Date before = new Date();
		Date now = (Date) execute(search("now()"));
		Date after = new Date();
		assertTrue(now.after(before) || now.equals(before));
		assertTrue(now.before(after) || now.equals(after));
	}

	/**
	 * Test for {@link SearchExpressionToday}
	 */
	public void testToday() throws ParseException {
		Date now = (Date) execute(search("today()"));

		Calendar userNow = CalendarUtil.createCalendarInUserTimeZone();
		Calendar systemNow = CalendarUtil.convertToSystemZone(userNow);
		systemNow.set(Calendar.HOUR_OF_DAY, 0);
		systemNow.set(Calendar.MINUTE, 0);
		systemNow.set(Calendar.SECOND, 0);
		systemNow.set(Calendar.MILLISECOND, 0);

		assertEquals(systemNow.getTime(), now);
	}

	/**
	 * Test for {@link ToDate}
	 */
	public void testToDate() throws ParseException, java.text.ParseException {
		assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("1970-01-01T00:00:00.000"),
			execute(search("toDate(0)")));
		assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("2019-08-05T15:38:52.123"),
			execute(search("toDate('2019-08-05T15:38:52.123')")));
		assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("2019-08-05T15:38:52.123"),
			execute(search("toDate('2019-08-05T15:38:52.123').toSystemCalendar().toDate()")));
		assertEquals(XmlDateTimeFormat.INSTANCE.parseObject("2019-08-05T15:38:52.123"),
			execute(search("toDate('2019-08-05T15:38:52.123').toUserCalendar().toDate()")));
	}

	/**
	 * Test for {@link CalendarField}
	 */
	public void testCalendarField() throws ParseException {
		ThreadContext context = ThreadContext.getThreadContext();

		TimeZone before = context.getCurrentTimeZone();
		try {
			TimeZone timeZone = TimeZone.getTimeZone("GMT+05:00");
			context.setCurrentTimeZone(timeZone);

			assertEquals("2019-08-05T15:38:52.123",
				execute(
					search(
						"{c=toDate('2019-08-05T15:38:52.123+05:00').toUserCalendar(); " +
							"f2=numberFormat('00'); " +
							"toString(numberFormat('#').format($c.year()), '-', $f2.format($c.month() + 1), '-', $f2.format($c.day()), 'T', $c.hour(), ':', $c.minute(), ':', $c.second(), '.', $c.millisecond());}")));
		} finally {
			context.setCurrentTimeZone(before);
		}
	}

	/**
	 * Test for {@link ToSystemCalendar}
	 */
	public void testSystemCalendar() throws ParseException {
		assertEquals(NumberFormat.getInstance(TLContext.getLocale()).format(2019) + "-8-5",
			execute(
				search(
					"{c=date(2019, 8 - 1, 5).toSystemCalendar(); " +
						"toString($c.year(), '-', $c.month() + 1, '-', $c.day());}")));
	}

	/**
	 * Test for {@link ToUserCalendar}
	 */
	public void testUserCalendar() throws ParseException {
		assertEquals(NumberFormat.getInstance(TLContext.getLocale()).format(2019) + "-8-5T15:38:52.123",
			execute(
				search(
					"{c=dateTime(" +
						"year: 2019, " +
						"month: 8 - 1, " +
						"day: 5, " +
						"hour: 15, " +
						"minute: 38, " +
						"second: 52, " +
						// Note: A single trailing comma is allowed in the argument list to make all
						// lines uniform in one-argument-per-line style.
						"millisecond: 123, " +
					").toUserCalendar(); " +
					"toString($c.year(), '-', $c.month() + 1, '-', $c.day(), 'T', $c.hour(), ':', $c.minute(), ':', $c.second(), '.', $c.millisecond());}")));
	}

	/**
	 * Test for {@link ToMillis}
	 */
	public void testToMillis() throws ParseException, java.text.ParseException {
		assertEquals(
			SearchExpression
				.toNumber(((Date) XmlDateTimeFormat.INSTANCE.parseObject("2019-08-05T15:38:52.123")).getTime()),
			execute(
				search(
					"toDate('2019-08-05T15:38:52.123').toMillis()")));
	}

	/**
	 * Test for {@link CalendarUpdate}
	 */
	public void testCalendarUpdate() throws ParseException, java.text.ParseException {
		assertEquals(
			SearchExpression.toNumber(CalendarUtil.convertToSystemZone(
				CalendarUtil.createCalendar(
					(Date) XmlDateTimeFormat.INSTANCE.parseObject("2019-08-05T15:38:52.123Z"), getTimeZone("GMT")))
				.getTimeInMillis()),
			execute(
				search(
					"now().toUserCalendar().withYear(2019).withMonth(8-1).withDay(5).withHour(15).withMinute(38).withSecond(52).withMillisecond(123).toDate().toMillis()")));
	}

	public void testListEquals() throws ParseException {
		toTestEq("==", Boolean.TRUE, Boolean.FALSE);
	}

	public void testListNotEquals() throws ParseException {
		toTestEq("!=", Boolean.FALSE, Boolean.TRUE);
	}

	private void toTestEq(String eq, Boolean isEqual, Boolean notEqual) throws ParseException {
		assertEquals(isEqual, execute(search("list('a', 'b') " + eq + " list('a', 'b')")));
		assertEquals(notEqual, execute(search("list('a', 'b') " + eq + " list('b', 'a')")));
		assertEquals(isEqual, execute(search("list('a', 'b') " + eq + " sort(list('b', 'a'))")));

		// Single element lists are identified with their element.
		assertEquals(isEqual, execute(search("list('a') " + eq + " 'a'")));
		assertEquals(isEqual, execute(search("'a' " + eq + " list('a')")));

		// Singleton sets are identified with their element.
		assertEquals(isEqual, execute(search("singleton('a') " + eq + " 'a'")));
		assertEquals(isEqual, execute(search("'a' " + eq + " singleton('a')")));

		// Singleton sets and lists are equal.
		assertEquals(isEqual, execute(search("singleton('a') " + eq + " list('a')")));
		assertEquals(isEqual, execute(search("list('a') " + eq + " singleton('a')")));

		// Lists and sets with more than one element are never equal
		assertEquals(notEqual, execute(search("list('a', 'b') " + eq + " union(singleton('a'), singleton('b'))")));

		assertEquals(isEqual,
			execute(search("union(singleton('a'), singleton('b')) " + eq + " union(singleton('b'), singleton('a'))")));
		assertEquals(notEqual,
			execute(search("union(singleton('a'), singleton('b')) " + eq + " union(singleton('a'), singleton('c'))")));

		// Empty lists and sets are identified with null
		assertEquals(isEqual, execute(search("null " + eq + " list()")));
		assertEquals(isEqual, execute(search("list() " + eq + " null")));
		assertEquals(isEqual, execute(search("null " + eq + " none()")));
		assertEquals(isEqual, execute(search("none() " + eq + " null")));
		assertEquals(isEqual, execute(search("list() " + eq + " none()")));
		assertEquals(isEqual, execute(search("none() " + eq + " list()")));

		assertEquals(notEqual, execute(search("'a' " + eq + " none()")));
		assertEquals(notEqual, execute(search("'a' " + eq + " list()")));
	}

	public void testSort() throws ParseException {
		assertEquals(list("A", "B", "C"), execute(search("sort(list('B', 'C', 'A'))")));
		assertEquals(list("A"), execute(search("sort(list('A'))")));
		assertEquals(list(), execute(search("sort(null)")));
	}

	public void testDescendingSort() throws ParseException {
		assertEquals(list("C", "B", "A"), execute(search("list('A','B','C').sort(desc(comparator(x -> $x)))")));
		assertEquals(list("A", "B", "C"), execute(search("list('A','B','C').sort(desc(desc(comparator(x -> $x))))")));
	}

	public void testRecursion() {
		with("TestSearchExpression-testRecursion.scenario.xml",
			scenario -> {
				SearchExpression search1 = search(
					"start -> recursion($start, x->$x.get(`TestSearchExpression:A#others`))");

				TLObject a1 = scenario.getObject("a1");
				TLObject a2 = scenario.getObject("a2");
				TLObject a3 = scenario.getObject("a3");
				TLObject a4 = scenario.getObject("a4");
				assertEquals(set(a1, a2, a3, a4), executeAsSet(search1, a1));
			});
	}

	public void testRecursionWithJson() throws ParseException {
		Object search1 = execute(
			search("{\"v\": 5}.recursion(x -> $x[\"v\"] > 0 ? {\"v\": $x[\"v\"] - 1} : {\"v\": $x[\"v\"]})"));
		assertEquals(6, ((Collection<?>) search1).size());
	}

	public void testTraversal() throws ParseException {
		SearchExpression treeSum =
			search("r -> $r.traverse(n -> $n['children'], n -> $n['value'], n -> r -> $n + $r.sum())");
		assertEquals(5.0, execute(treeSum, node(5)));
		assertEquals(11.0, execute(treeSum, node(5, node(3), node(2, node(1)))));
	}

	@SafeVarargs
	private static Map<String, Object> node(int value, Map<String, Object>... children) {
		HashMap<String, Object> result = new HashMap<>();
		result.put("value", value);
		result.put("children", children);
		return result;
	}

	public void testGeneration() throws ParseException {
		assertEquals(list(0.0, 1.0, 2.0, 3.0, 4.0, 5.0), execute(search("0.0.recursion(x -> $x + 1, 0, 5)")));
		assertEquals(list(0.0, 1.0, 2.0, 3.0, 4.0, 5.0), execute(search("0.recursion(x -> $x + 1, 0, 5)")));
	}

	public void testSubstraction() throws ParseException {
		assertEquals(-2.0, execute(search("1 - 3")));
		assertEquals(-2.0, execute(search("1-3")));
		// Note: "-3" must not be interpreted as the number "-3", but the operation "-" and the
		// number "3":
		assertEquals(-2.0, execute(search("1 -3")));
	}

	public void testNegativeNumber() throws ParseException {
		assertEquals(3.0, execute(search("-1+4")));
	}

	public void testNumberCompare() throws ParseException {
		assertEquals(true, execute(search("[1, 2].containsElement([\"a\", \"b\"].size())")));

		assertEquals(true, execute(search("[1, 2].containsElement(1)")));
		assertEquals(true, execute(search("[1, 2].containsElement(1.0)")));
		assertEquals(false, execute(search("[1, 2].containsElement(1.1)")));

		assertEquals(true, execute(search("[1.0, 2.0].containsElement(1)")));
		assertEquals(true, execute(search("[1.0, 2.0].containsElement(1.0)")));
		assertEquals(false, execute(search("[1.0, 2.0].containsElement(1.1)")));

		assertEquals(true, execute(search("[1, [\"a\", \"b\"].size()].containsElement([\"a\", \"b\"].size())")));

		assertEquals(true, execute(search("[1, 2].containsElement([\"a\", \"b\"].size()+0)")));
		assertEquals(true, execute(search("[1, [\"a\", \"b\"].size()].containsElement([\"a\", \"b\"].size()+0)")));

		assertEquals(true, execute(search("[\"a\", \"b\"].size() == 2")));
		assertEquals(true, execute(search("[\"a\", \"b\"].size() == 2.0")));

		assertEquals("b", execute(search("[\"a\", \"b\"][1]")));
		assertEquals("b", execute(search("[\"a\", \"b\"][1.0]")));

		assertEquals(true, execute(search("count(0, 3).containsElement(2)")));
		assertEquals(true, execute(search("count(0, 3).containsElement(2.0)")));
		assertEquals(true, execute(search("count(0, 3).containsElement(1 + 1)")));

		assertEquals(true, execute(search("100 == 1e2")));
		assertEquals(true, execute(search("157.6 == 1.576E2")));
	}

	public void testNullFragment() throws ParseException {
		assertEquals(true, execute(search("0.1 > 0.0")));
		assertEquals(true, execute(search("0 == 0.0")));
	}

	public void testCompare() throws ParseException {
		assertEquals(true, execute(search("1 > 0.5")));
		assertEquals(false, execute(search("1 > 1.0")));
		assertEquals(true, execute(search("1.0 >= 1")));
		String intExpression = "list('a', 'b').size()";
		String doubleExpression = "1.0";
		assertInstanceof(execute(search(intExpression)), Double.class);
		assertInstanceof(execute(search(doubleExpression)), Double.class);
		assertEquals(true, execute(search(intExpression + " >= " + doubleExpression)));
		assertEquals(true, execute(search(intExpression + " == " + intExpression)));
	}

	public void testTextWithEmbeddedExpressions() throws ParseException {
		assertEquals("{13 + 42} = 55", execute(textWithEmbeddedExpressions("\\{13 + {6 * 7}\\} = {13 + 42}")));
	}

	public void testHtml() {
		// Decimal separator depends on Locale
		char decimalSeparator = DecimalFormatSymbols.getInstance(TLContext.getLocale()).getDecimalSeparator();

		with("TestSearchExpression-testHtml.scenario.xml",
			scenario -> {
				SearchExpression renderer = search(
					"{{{" + "<table>"
						+ "{"
						+ "all(`TestSearchExpression:A`)"
						+ ".sort(comparator(x -> list($x.get(`TestSearchExpression:A#double`), desc($x.get(`TestSearchExpression:A#str`)))))"
						+ ".map(row -> {{{"
						+ "<tr class=\"{if($row.get(`TestSearchExpression:A#double`) > 10, 'critical')}\">"
						+ "<td>{$row.get(`TestSearchExpression:A#name`)}</td>"
						+ "<td>{$row.get(`TestSearchExpression:A#double`)}</td>"
						+ "<td>{$row.get(`TestSearchExpression:A#str`)}</td></tr>"
						+ "}}})"
						+ "}"
						+ "</table>"
						+ "}}}");

				String html = render(renderer);
				{
					assertEquals(
						"<table>" +
							"<tr><td>a4</td>" + "<td>8" + decimalSeparator + "9</td>" + "<td>D</td></tr>" +
							"<tr><td>a3</td>" + "<td>8" + decimalSeparator + "9</td>" + "<td>C</td></tr>" +
							"<tr><td>a5</td>" + "<td>8" + decimalSeparator + "9</td>" + "<td></td></tr>" +
							"<tr class=\"critical\"><td>a1</td>" + "<td>42" + decimalSeparator + "13</td>"
							+ "<td>A</td></tr>" +
							"<tr><td>a2</td>" + "<td></td>" + "<td>B</td></tr>" +
							"</table>",
						html);
				}
			});
	}

	public void testHtmlComment() throws ParseException {
		SearchExpression renderer = search(
			"{{{" +
				"<div>" +
				"<%-----------------------------------------------------------%>" +
				"<%-- JSP-like comment: Content is not shown in the output. --%>" +
				"<%-----------------------------------------------------------%>" +
				"<%--" +
				"<span ....>It's OK to have & arbitrary < content > within a comment" +
				"--%>" +

				"<!-- A regular HTML comment: Content is also not shown in the output. -->" +
				"<!--" +
				"<span ....>It's OK to have & arbitrary < content > within a comment" +
				"-->" +

				"Hello world!" +
				"</div>" +
				"}}}");

		String html = render(renderer);
		{
			assertEquals("<div>Hello world!</div>", html);
		}
	}

	private String render(SearchExpression renderer, Object... args) {
		TagWriter buffer = new TagWriter();
		execute(DummyDisplayContext.newInstance(), buffer, renderer, args);
		return buffer.toString();
	}

	public void testHtmlSafety() throws ParseException {
		try {
			search("{{{<a href=\"javascript:alert();\">click</a>}}}");
			fail("Expected error.");
		} catch (I18NRuntimeException ex) {
			assertEquals(com.top_logic.basic.html.I18NConstants.NO_JAVASCRIPT_ALLOWED,
				Location.detail(ex.getErrorKey()));
		}
	}

	public void testHtmlSafetyDynamic() throws ParseException {
		SearchExpression expr = search("{{{<a href=\"{concat('java', 'script:alert();')}\">click</a>}}}");
		assertFalse(render(expr).contains("javascript:"));
	}

	public void testDynamicValuesInURL() throws ParseException {
		SearchExpression expr =
			search("x -> {{{<a href=\"{concat('http://', 'top-logic.com', '/', $x)}\">click</a>}}}");
		assertEquals("<a href=\"http://top-logic.com/dynamic\">click</a>", render(expr, "dynamic"));
	}

	public void testTree() {
		with("TestSearchExpression-testTree.scenario.xml",
			scenario -> {
				SearchExpression search1 = search(
					"start -> recursion($start, x->$x.get(`TestSearchExpression:A#others`), 1, 2)");

				TLObject r = scenario.getObject("root");
				TLObject n1 = scenario.getObject("n1");
				TLObject n2 = scenario.getObject("n2");
				TLObject n11 = scenario.getObject("n1.1");
				TLObject n21 = scenario.getObject("n2.1");
				TLObject n22 = scenario.getObject("n2.2");
				assertEquals(set(n1, n2, n11, n21, n22), executeAsSet(search1, r));
			});
	}

	public void testSwitch() throws ParseException {
		SearchExpression expr =
			search("x -> switch { $x == 1: 'one'; $x == 2: 'a group'; $x >= 3: 'a crowd'; default: 'unknown'; }");
		assertEquals("one", execute(expr, 1));
		assertEquals("a group", execute(expr, 2));
		assertEquals("a crowd", execute(expr, 3));
		assertEquals("a crowd", execute(expr, 100));
		assertEquals("unknown", execute(expr, 0));
	}

	public void testSwitchValue() throws ParseException {
		SearchExpression expr =
			search("x -> switch ($x) { 1: 'one'; 2: 'two'; 3: 'three'; default: 'unknown'; }");
		assertEquals("one", execute(expr, 1));
		assertEquals("two", execute(expr, 2));
		assertEquals("three", execute(expr, 3));
		assertEquals("unknown", execute(expr, 100));
		assertEquals("unknown", execute(expr, 0));
	}

	public void testSwitchWithoutDefault() throws ParseException {
		SearchExpression expr =
			search("x -> switch ($x) { 2: 'a group'; 3: 'a crowd'; }");
		assertEquals("a group", execute(expr, 2));
		assertEquals("a crowd", execute(expr, 3));
		assertEquals(null, execute(expr, 100));
		assertEquals(null, execute(expr, 0));
	}

	public void testSwitchLiteral() throws ParseException {
		SearchExpression expr = search(
			"""
			switch (1) {
				0: false;
				1: true;
			}
			""");
		assertEquals(Boolean.TRUE, execute(expr));
	}
	
	public void testBlockComment() throws ParseException {
		SearchExpression expr =
			search("x -> /********/ $x /* foobar */ + /*/*/ 3");
		assertEquals(4.0, execute(expr, 1));
	}

	public void testLineComment() throws ParseException {
		SearchExpression expr =
			search(
				"// A function adding three to its argument.\n" +
				"x -> $x + 3");
		assertEquals(4.0, execute(expr, 1));
	}

	public void testLineCommentCr() throws ParseException {
		SearchExpression expr =
				search(
					"// A function adding three to its argument.\r" +
					"x -> $x + 3");
		assertEquals(4.0, execute(expr, 1));
	}
	
	public void testLineCommentCrLf() throws ParseException {
		SearchExpression expr =
				search(
					"// A function adding three to its argument.\r\n" +
					"x -> $x + 3");
		assertEquals(4.0, execute(expr, 1));
	}
	
	public void testResKey() throws ParseException {
		SearchExpression search1 = search("'foo {0} bar'@en.fill('xxx')");

		assertEquals("foo xxx bar", Resources.getInstance().getString((ResKey) execute(search1)));
	}

	public void testCreate() throws ParseException {
		Object search1 = execute(search("new(`TestSearchExpression:A`)"));
		assertNotNull(search1);
		assertEquals("TestSearchExpression:A", ((TLObject) search1).tType().toString());

		assertTrue(((TLObject) search1).tValid());
		execute(search("x -> $x.delete()"), search1);
		assertFalse(((TLObject) search1).tValid());

		execute(search("null.delete()"));
	}

	public void testCreateSwitch() throws ParseException {
		Object result = execute(search(
			"x -> switch($x) {" +
				"'A': new(`TestSearchExpression:A`);" +
				"'B': new(`TestSearchExpression:B`);" +
			"}" + 
			"..set(`TestSearchExpression:A#name`, 'foo')" +
			"..map(y -> if ($y.instanceOf(`TestSearchExpression:B`), $y.set(`TestSearchExpression:B#name`, 'foo-b')))"
		), "B");

		assertNotNull(result);
		assertEquals("foo-b", ((TLObject) result).tValueByName("name"));
	}

	public void testInstanceOf() throws ParseException {
		assertTrue(SearchExpression.asBoolean(
			execute(search("new(`TestSearchExpression:A`).instanceOf(`TestSearchExpression:A`)"))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("new(`TestSearchExpression:A`).instanceOf(`TestSearchExpression:B`)"))));
		assertTrue(SearchExpression.asBoolean(
			execute(search("new(`TestSearchExpression:C`).instanceOf(`TestSearchExpression:B`)"))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("new(`TestSearchExpression:B`).instanceOf(`TestSearchExpression:C`)"))));

		assertTrue(SearchExpression.asBoolean(
			execute(search("(t -> new(`TestSearchExpression:A`).instanceOf($t))(`TestSearchExpression:A`)"))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("(t -> new(`TestSearchExpression:A`).instanceOf($t))(`TestSearchExpression:B`)"))));
		assertTrue(SearchExpression.asBoolean(
			execute(search("(t -> new(`TestSearchExpression:C`).instanceOf($t))(`TestSearchExpression:B`)"))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("(t -> new(`TestSearchExpression:B`).instanceOf($t))(`TestSearchExpression:C`)"))));

		assertTrue(SearchExpression.asBoolean(
			execute(search("t -> new(`TestSearchExpression:A`).instanceOf($t)"),
				execute(search("`TestSearchExpression:A`")))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("t -> new(`TestSearchExpression:A`).instanceOf($t)"),
				execute(search("`TestSearchExpression:B`")))));
		assertTrue(SearchExpression.asBoolean(
			execute(search("t -> new(`TestSearchExpression:C`).instanceOf($t)"),
				execute(search("`TestSearchExpression:B`")))));
		assertFalse(SearchExpression.asBoolean(
			execute(search("t -> new(`TestSearchExpression:B`).instanceOf($t)"),
				execute(search("`TestSearchExpression:C`")))));
	}

	public void testBulkDelete() throws ParseException {
		List<?> search = (List<?>) execute(search(
			"list('a1', 'a2').map(n -> new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#name`, $n))"));
		assertNotNull(search);
		assertEquals(2, search.size());

		for (var x : search) {
			assertTrue(((TLObject) x).tValid());
		}

		execute(search("x -> $x.delete()"), search);
		for (var x : search) {
			assertFalse(((TLObject) x).tValid());
		}
	}

	public void testCreateWithContext() throws ParseException {
		Object context = execute(search(
			"new(`TestSearchExpression:DefaultProvidingContext`)..set(`TestSearchExpression:DefaultProvidingContext#contextValue`, 'my-context')"));
		assertNotNull(context);

		Object withDefault =
			execute(search("context -> new(`TestSearchExpression:WithContextDependingDefault`, $context)"), context);
		assertNotNull(withDefault);

		Object defaultValue = execute(
			search("withDefault -> $withDefault.get(`TestSearchExpression:WithContextDependingDefault#withDefault`)"),
			withDefault);
		assertEquals("my-context by default", defaultValue);
	}

	public void testCopy() {
		with("TestSearchExpression-testCopy.scenario.xml",
			scenario -> {
				TLObject orig = scenario.getObject("a1");

				TLObject copy = (TLObject) execute(search("x -> $x.copy()"), orig);
				checkCopy(orig, copy);

				TLObject stable = stabilize(orig);

				// The result must be the same, when using the stable version of the current
				// original as input to the copy operation.
				TLObject stableCopy = (TLObject) execute(search("x -> $x.copy()"), stable);
				checkCopy(orig, stableCopy);
			});
	}

	private TLObject stabilize(TLObject orig) {
		HistoryManager historyManager = orig.tKnowledgeBase().getHistoryManager();
		TLObject stable =
			WrapperHistoryUtils.getWrapper(historyManager.getRevision(historyManager.getLastRevision()), orig);
		return stable;
	}

	private void checkCopy(TLObject orig, TLObject copy) {
		assertNotNull(copy);
		assertNotEquals(orig, copy);
		assertDifferent(orig, copy, "b");
		assertDifferent(orig, copy, "b", "contents", 0);
		assertDifferent(orig, copy, "b", "contents", 1);
		assertDifferent(orig, copy, "b", "contents", 2);

		assertNotNull(value(orig, "b", "contents", 0, "other"));
		assertNotNull(value(copy, "b", "contents", 0, "other"));

		assertEquals(value(copy, "b", "contents", 1), value(copy, "b", "contents", 0, "other"));
		assertEquals(value(orig, "b", "name"), value(copy, "b", "name"));
	}

	public void testCopyFilter() {
		with("TestSearchExpression-testCopy.scenario.xml",
			scenario -> {
				TLObject orig = scenario.getObject("a1");
				TLObject copy =
					(TLObject) execute(
						search("x -> $x.copy(null, filter: part -> $part != `TestSearchExpression:B#others`)"),
						orig);
				checkCopyFilter(orig, copy);

				TLObject stable = stabilize(orig);

				TLObject stableCopy =
					(TLObject) execute(search(
						"x -> $x.copy(null, filter: part -> !$part.equalsUnversioned(`TestSearchExpression:B#others`))"),
						stable);
				checkCopyFilter(orig, stableCopy);
			});
	}

	private void checkCopyFilter(TLObject orig, TLObject copy) {
		assertNotNull(value(orig, "b", "contents", 0, "other"));
		assertNotNull(value(copy, "b", "contents", 0, "other"));
		assertDifferent(orig, copy, "b", "contents", 0, "other");

		assertNotEmpty(value(orig, "b", "contents", 1, "others"));
		assertEmpty(value(copy, "b", "contents", 1, "others"));
	}

	public void testCopyConstructor() {
		with("TestSearchExpression-testCopy.scenario.xml",
			scenario -> {
				TLObject orig = scenario.getObject("a1");
				TLObject origB = (TLObject) orig.tValueByName("b");
				origB.tUpdateByName("value", 42);

				TLObject copy =
					(TLObject) execute(search(
						"x -> $x.copy(constructor: orig -> "  + 
							"if ($orig.instanceOf(`TestSearchExpression:B`), " +
								"new(`TestSearchExpression:C`) " + 
								"..set(`TestSearchExpression:C#orig`, $orig) " +
							"))"),
						orig);

				checkCopyConstructor(orig, copy);

				TLObject copyB = (TLObject) copy.tValueByName("b");
				assertEquals(42, copyB.tValueByName("value"));

				// Check that updates are visible in copy through derived attribute.
				origB.tUpdateByName("value", 13);
				assertEquals(13, copyB.tValueByName("value"));

				TLObject stable = stabilize(orig);

				TLObject stableCopy =
					(TLObject) execute(search(
						"x -> $x.copy(constructor: orig -> " +
							"if ($orig.instanceOf(`TestSearchExpression:B`), " +
								"new(`TestSearchExpression:C`) " +
								"..set(`TestSearchExpression:C#orig`, $orig.inCurrent()) " +
							"))"),
						stable);

				checkCopyConstructor(orig, stableCopy);
			});
	}

	private void checkCopyConstructor(TLObject orig, TLObject copy) {
		assertEquals(value(orig, "b", "contents", 0), value(copy, "b", "contents", 0, "orig"));
		assertNull(value(copy, "b", "contents", 0, "special"));
	}

	private void assertEmpty(Object value) {
		assertTrue(value == null || ((Collection<?>) value).isEmpty());
	}

	private void assertNotEmpty(Object value) {
		assertTrue(value != null && !((Collection<?>) value).isEmpty());
	}

	private void assertDifferent(TLObject orig, TLObject copy, Object... attrs) {
		Object origValue = value(orig, attrs);
		assertNotNull(origValue);
		Object copyValue = value(copy, attrs);
		assertNotNull(copyValue);
		assertNotEquals(origValue, copyValue);
	}

	private Object value(TLObject obj, Object... attrs) {
		Object result = obj;
		for (Object attr : attrs) {
			assertNotNull("Cannot access '" + attr + "' on null.", result);
			if (attr instanceof String) {
				result = ((TLObject) result).tValueByName((String) attr);
			} else {
				result = ((List<?>) result).get((int) attr);
			}
		}
		return result;
	}

	public void testDeleteAll() throws ParseException {
		Object result = execute(search("count(0, 10).map(x -> new(`TestSearchExpression:A`))"));
		assertTrue(result instanceof List<?>);
		List<?> list = (List<?>) result;
		assertEquals(10, list.size());

		for (Object x : list) {
			assertEquals("TestSearchExpression:A", ((TLObject) x).tType().toString());
			assertTrue(((TLObject) x).tValid());
		}

		execute(search("x -> $x.delete()"), result);

		for (Object x : list) {
			assertFalse(((TLObject) x).tValid());
		}
	}

	public void testAdd() throws ParseException {
		TLObject a = (TLObject) executeCompiled(search(
			"new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#list`, list(new(`TestSearchExpression:A`), new(`TestSearchExpression:A`)))"));
		List<?> list = (List<?>) value(a, "list");
		assertEquals(2, list.size());

		executeCompiled(search(
			"a -> $a.add(`TestSearchExpression:A#list`, new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#name`, 'A3'))"),
			a);
		List<?> list1 = (List<?>) value(a, "list");
		assertEquals(3, list1.size());
		TLObject a3 = (TLObject) list1.get(2);
		assertEquals("A3", value(a3, "name"));

		executeCompiled(search(
			"a -> $a.add(`TestSearchExpression:A#list`, 1, list("
				+ "new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#name`, 'A4'), "
				+ "new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#name`, 'A5')))"),
			a);
		List<?> list2 = (List<?>) value(a, "list");
		assertEquals(5, list2.size());
		TLObject a4 = (TLObject) list2.get(1);
		TLObject a5 = (TLObject) list2.get(2);
		assertEquals("A4", value(a4, "name"));
		assertEquals("A5", value(a5, "name"));
		TLObject a3x = (TLObject) list2.get(4);
		assertEquals(a3, a3x);
	}

	public void testAssign() throws ParseException {
		Object search1 = execute(
			search(
				"new(`TestSearchExpression:A`)" +
					"..set(`TestSearchExpression:A#name`, 'Hello world!')" +
					"..set(`TestSearchExpression:A#other`, new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#name`, 'Other object'))"));
		assertNotNull(search1);
		assertEquals("TestSearchExpression:A", ((TLObject) search1).tType().toString());
		assertEquals("Hello world!", ((TLObject) search1).tValueByName("name"));
		assertEquals("Other object", ((TLObject) ((TLObject) search1).tValueByName("other")).tValueByName("name"));
	}

	public void testApply() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; plus3 = $add(3); $plus3($plus3(2)); }"));
		assertEquals(8.0, search1);
	}

	/**
	 * Test case for Ticket #26223.
	 */
	public void testNesteFunctionDefinition() throws ParseException {
		Object search1 = execute(
			search(
				"a -> {add = x -> y -> z -> $x + ', ' + $y + ', ' + $z; $add($a, 'b', 'c')}"),
			"a", "unused");
		assertEquals("a, b, c", search1);
	}

	public void testApplyDirect() throws ParseException {
		Object search1 = execute(
			search(
				"(x -> y -> $x + $y)(3)(5)"));
		assertEquals(8.0, search1);
	}

	public void testApplyDirectMultiple() throws ParseException {
		Object search1 = execute(
			search(
				"(x -> y -> $x + $y)(3, 5)"));
		assertEquals(8.0, search1);
	}

	public void testApplyMultipleArgs() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add(3, 5); }"));
		assertEquals(8.0, search1);
	}

	public void testApplyUnusedArg() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add(3, 5, 'unused'); }"));
		assertEquals(8.0, search1);
	}

	public void testApplyExplicit() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; plus3 = $add.apply(3); $plus3.apply($plus3.apply(2)); }"));
		assertEquals(8.0, search1);
	}

	public void testApplyExplicitMultipleArgs() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add.apply(3, 5); }"));
		assertEquals(8.0, search1);
	}

	public void testApplyExplicitUnusedArg() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add.apply(3, 5, 'unused'); }"));
		assertEquals(8.0, search1);
	}

	public void testFunctionAsArgument() throws ParseException {
		Object search1 = execute(
			search(
				"{plus3 = x -> $x + 3; op = fun -> $fun(3) * $fun(4); $op($plus3); }"));
		assertEquals(42.0, search1);
	}

	public void testMultipleApply() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add(6)(7); }"));
		assertEquals(13.0, search1);
	}

	public void testMultipleApplyShortcut() throws ParseException {
		Object search1 = execute(
			search(
				"{add = x -> y -> $x + $y; $add(6, 7); }"));
		assertEquals(13.0, search1);
	}

	public void testReduce() throws ParseException {
		Object search1 = execute(
			search(
				"list(3, 7, 30, 2).reduce(0, x -> y-> $x + $y)"));
		assertEquals(42.0, search1);
	}

	public void testCreateReduce() throws ParseException {
		TLObject a = (TLObject) execute(
			search(
				"{l=list(3, 7, 30, 2); a=new(`TestSearchExpression:A`); others=$l.map(val -> new(`TestSearchExpression:A`)..set(`TestSearchExpression:A#int`, $val)); $a.set(`TestSearchExpression:A#others`, $others); $a; }"));
		Collection<?> others = (Collection<?>) a.tValueByName("others");
		assertEquals(4, others.size());
		assertNotNull(((TLObject) others.iterator().next()).tValueByName("int"));

		Object result = execute(search(
			"a -> $a.get(`TestSearchExpression:A#others`).map(o -> $o.get(`TestSearchExpression:A#int`)).reduce(0, x -> y -> $x + $y)"),
			a);
		assertEquals(42.0, result);
	}

	public void testLength() throws ParseException {
		assertEquals(5.0, eval("'Hello'.length()"));
		assertEquals(0.0, eval("''.length()"));
		assertEquals(0.0, eval("null.length()"));
		assertEquals(null, eval("(3).length()"));
		assertEquals(null, eval("length(3)"));
		assertEquals(null, eval("list(3, 'foo', 5).length()"));
	}

	public void testList() throws ParseException {
		assertEquals(list("Hello", "world", "!"), eval("list('Hello', 'world', '!')"));
	}

	public void testListLiteral() throws ParseException {
		assertEquals(list("Hello", "world", "!"), eval("['Hello', 'world', '!']"));
	}

	public void testListAccess() throws ParseException {
		assertEquals("world", eval("list('Hello', 'world', '!')[1]"));
	}

	public void testListLiteralAccess() throws ParseException {
		assertEquals("world", eval("['Hello', 'world', '!'][1]"));
	}

	public void testListAccessWithString() throws ParseException {
		assertEquals("world", eval("list('Hello', 'world', '!')['1']"));
	}

	public void testListWithNull() throws ParseException {
		// null is equivalent to the empty list.
		assertEquals(0.0, eval("null.size()"));
		assertEquals(0.0, eval("list().size()"));
		assertEquals(1.0, eval("list(42).size()"));
		assertEquals(1.0, eval("list(list()).size()"));

		// list() does not filter out null values.
		assertEquals(1.0, eval("x -> list($x).size()", new Object[] { null }));
		assertEquals(1.0, eval("list(null).size()"));
		assertEquals(2.0, eval("list(null, null).size()"));

		// In contrast, singleton() collapses null values to empty lists.
		assertEquals(0.0, eval("singleton(null).size()"));
		assertEquals(1.0, eval("singleton(42).size()"));

		assertEquals(-1.0, eval("null.elementIndex(null)"));
		assertEquals(-1.0, eval("list().elementIndex(null)"));
		assertEquals(0.0, eval("list(null).elementIndex(null)"));
		assertEquals(0.0, eval("list(null, null).elementIndex(null)"));
	}

	public void testListAccessWithInvalidString() throws ParseException {
		try {
			assertEquals("world", eval("list('Hello', 'world', '!')['foo']"));
			fail("Error expected.");
		} catch (I18NRuntimeException ex) {
			assertEquals(ex.getErrorKey().plain(),
				com.top_logic.model.search.expr.I18NConstants.ERROR_NUMBER_REQUIRED__VALUE_EXPR);
		}
	}

	public void testMap() throws ParseException {
		assertEquals(
			new MapBuilder<>().put("foo", 42.0).put("bar", "Hello").toMap(),
			eval("structType('foo', 'bar').newStruct(42.0, 'Hello')"));
	}

	public void testMapLiteral() throws ParseException {
		assertEquals(
			new MapBuilder<>().put("foo", 42.0).put("bar", "Hello").toMap(),
			eval("{toString('f', 'oo'): 42.0, 'bar': 'Hello'}"));
	}

	public void testInvalidMapLiteral() throws ParseException {
		try {
			eval("{'foo': 42.0, toString('f', 'oo'): 'Hello'}");
			fail("Error expected.");
		} catch (TopLogicException ex) {
			assertContains("duplicated", ex.getMessage());
		}
	}

	public void testStructIteration() throws ParseException {
		assertEquals(
			"foo=>42, bar=>Hello",
			eval(
				"{'foo': 42, 'bar': 'Hello'}" +
					".map(e -> toString($e.getKey(), '=>', $e.getValue()))" +
					".reduce(null, a->b-> $a == null ? $b : toString($a, ', ', $b))"));
	}

	public void testStructKeys() throws ParseException {
		assertEquals(
			"foo, bar",
			eval(
				"{'foo': 42, 'bar': 'Hello'}" +
					".keySet()" +
					".reduce(null, a->b-> $a == null ? $b : toString($a, ', ', $b))"));
	}

	public void testStructValues() throws ParseException {
		assertEquals(
			"42, Hello",
			eval(
				"{'foo': 42, 'bar': 'Hello'}" +
					".values()" +
					".reduce(null, a->b-> $a == null ? $b : toString($a, ', ', $b))"));
	}

	public void testMapAccess() throws ParseException {
		assertEquals("Hello", eval("{'foo': 42, 'bar': 'Hello'}['bar']"));
	}

	public void testNestedMap() throws ParseException {
		assertEquals("Hello",
			eval("{'foo': 42, 'bar': {'baz': 'Hello'}}['bar']['baz']"));
	}

	public void testIndex() throws ParseException {
		assertEquals(new MapBuilder<>().put("f", "foo").put("b", "bar").toMap(),
			eval("list('foo', 'bar').indexBy(s -> $s.subString(0, 1))"));
	}

	public void testIndexEquality() throws ParseException {
		assertEquals(Boolean.TRUE,
			eval("['foo', 'bar'].indexBy(s -> $s.subString(0, 1)) == {'f': 'foo', 'b': 'bar'}"));
	}

	public void testIndexUsage() throws ParseException {
		assertEquals("foofoobar",
			eval(
				"{" + 
					"index = list('foo', 'bar').indexBy(s -> $s.subString(0, 1));" + 
					"list('f', 'f', 'b').map(x -> $index[$x]).toString();" + 
				"}"));
	}

	public void testIndexClashResolve() throws ParseException {
		assertEquals(new MapBuilder<>().put("f", "foo").put("b", "bar-bazz").toMap(),
			eval("list('foo', 'bar', 'bazz').indexBy(s -> $s.subString(0, 1), a -> b -> toString($a, '-', $b))"));
	}

	public void testIndexReduce() throws ParseException {
		assertEquals(new MapBuilder<>().put("f", 1.0).put("b", 2.0).toMap(),
			eval("list('foo', 'bar', 'bazz').indexReduce(s -> $s.subString(0, 1), 0, a -> b -> $a + 1)"));
	}
	
	public void testIndexAsCollection() throws ParseException {
		assertEquals(list("F", "B"),
			eval(
				"list('foo', 'bar', 'bazz')" +
					".indexBy(s -> $s.subString(0, 1), a -> b -> $a)" +
					".keySet()" +
					".map(s -> $s.toUpperCase())"));
	}

	public void testGroup() throws ParseException {
		assertEquals(new MapBuilder<>().put("f", list("foo")).put("b", list("bar", "bazz")).toMap(),
			eval("list('foo', 'bar', 'bazz').groupBy(s -> $s.subString(0, 1))"));
	}

	public void testGroupNested() throws ParseException {
		assertEquals(
			new MapBuilder<>(true)
				.put("f",
					new MapBuilder<>().put("o", list("foo")).toMap())
				.put("b",
					new MapBuilder<>(true).put("a", list("bar")).put("i", list("bizz")).toMap())
				.toMap(),
			eval(
				"list('foo', 'bar', 'bizz')" +
					".groupBy(s -> $s.subString(0, 1), l -> $l.groupBy(s -> $s.subString(1, 2)))"));
	}

	public void testSubString() throws ParseException {
		assertEquals("Bar", eval("'FooBar'.subString(3)"));
		assertEquals("Bar", eval("'FooBar'.subString(from: 3)"));
		assertEquals("Foo", eval("'FooBar'.subString(0, 3)"));
		assertEquals("Foo", eval("'FooBar'.subString(from: 0, to: 3)"));
		assertEquals("Foo", eval("'FooBar'.subString(to: 3, from: 0)"));
		assertEquals("789", eval("'123456789'.subString(-3)"));
		assertEquals("678", eval("'123456789'.subString(-4, -1)"));
		assertEquals("6789", eval("'123456789'.subString(-4, 0)"));
	}

	public void testToLowerCase() throws ParseException {
		assertEquals("foobar", eval("'FooBar'.toLowerCase()"));
	}

	public void testToUpperCase() throws ParseException {
		assertEquals("FOOBAR", eval("'FooBar'.toUpperCase()"));
	}

	public void testConcat() throws ParseException {
		assertEquals(list(1.0, 2.0, "Hello", "world", "!"), eval("concat(list(1, 2), list('Hello', 'world'), '!')"));
	}

	public void testConcatWithSelf() throws ParseException {
		assertEquals(list(1.0, 2.0, "Hello", "world", "!"), eval("list(1, 2).concat(list('Hello', 'world'), '!')"));
	}

	public void testFlatten() throws ParseException {
		assertEquals(list(1.0, 2.0, "Hello", "world", "!"),
			eval("list(list(1, 2), list('Hello', 'world'), null, '!').flatten()"));
	}

	public void testSubList() throws ParseException {
		assertEquals(list(2.0, "Hello"), eval("list(1, 2, 'Hello', 'world', '!').subList(1, 3)"));
		assertEquals(list("Hello", "world", "!"), eval("list(1, 2, 'Hello', 'world', '!').subList(2, 10)"));
		assertEquals(list(), eval("list(1, 2, 'Hello', 'world', '!').subList(10, 20)"));
		assertEquals(list(1.0, 2.0), eval("list(1, 2, 'Hello', 'world', '!').subList(-10, -3)"));
		assertEquals(list(1.0, 2.0), eval("list(1, 2, 3).subList(0, -1)"));
		assertEquals(list(2.0), eval("list(1, 2, 3).subList(1, -1)"));
		assertEquals(list(2.0, 3.0), eval("list(1, 2, 3).subList(1, 0)"));
	}

	public void testReverse() throws ParseException {
		assertEquals(list(3.0, 2.0, 1.0), eval("list(1, 2, 3).reverse()"));
		assertEquals(list(1.0), eval("list(1).reverse()"));
		assertEquals(list(), eval("list().reverse()"));
	}

	public void testSize() throws ParseException {
		assertEquals(1.0, eval("'Hello'.size()"));
		assertEquals(1.0, eval("''.size()"));
		assertEquals(0.0, eval("null.size()"));
		assertEquals(1.0, eval("(3).size()"));
		assertEquals(1.0, eval("size(3)"));
		assertEquals(3.0, eval("list(3, 'foo', 5).size()"));
	}

	public void testFirstElement() throws ParseException {
		assertEquals(3.0, eval("list(3, 7, 30, 2).firstElement()"));
		assertEquals(3.0, eval("3.firstElement()"));
		assertEquals(null, eval("list().firstElement()"));
		assertEquals(null, eval("null.firstElement()"));
	}

	public void testLastElement() throws ParseException {
		assertEquals(2.0, eval("list(3, 7, 30, 2).lastElement()"));
		assertEquals(2.0, eval("(2).lastElement()"));
		assertEquals(2.0, eval("lastElement(2)"));
		assertEquals(null, eval("list().lastElement()"));
		assertEquals(null, eval("null.lastElement()"));
	}

	public void testElementAt() throws ParseException {
		assertEquals(30.0, eval("list(3, 7, 30, 2).elementAt(2)"));
		assertEquals(3.0, eval("list(3, 7, 30, 2).elementAt(0)"));
		assertEquals(2.0, eval("list(3, 7, 30, 2).elementAt(3)"));
		assertEquals(42.0, eval("(42).elementAt(0)"));
		assertEquals(42.0, eval("elementAt(42, 0)"));
		assertEquals(null, eval("(42).elementAt(4)"));
		assertEquals(null, eval("list(3, 7, 30, 2).elementAt(-1)"));
		assertEquals(null, eval("list(3, 7, 30, 2).elementAt('foobar')"));
		assertEquals(null, eval("list(3, 7, 30, 2).elementAt(4)"));
		assertEquals(null, eval("list().elementAt(4)"));
		assertEquals(null, eval("null.elementAt(4)"));
	}

	public void testElementIndex() throws ParseException {
		assertEquals(3.0, eval("list(3, 7, 30, 2).elementIndex(2)"));
		assertEquals(-1.0, eval("list(3, 7, 30, 2).elementIndex(10)"));
		assertEquals(1.0, eval("list(3, 'foobar', 30, 2).elementIndex('foobar')"));
		assertEquals(-1.0, eval("list(3, 'foobar', 30, 2).elementIndex('xxx')"));
		assertEquals(0.0, eval("list(3, 'foobar', 30, 2).elementIndex(3)"));
		assertEquals(0.0, eval("list(3).elementIndex(3)"));
		assertEquals(0.0, eval("(3).elementIndex(3)"));
		assertEquals(-1.0, eval("list().elementIndex(3)"));
		assertEquals(-1.0, eval("null.elementIndex(3)"));
		assertEquals(-1.0, eval("null.elementIndex(null)"));
	}

	public void testCount() throws ParseException {
		assertEquals(list(3.0, 4.0, 5.0), execute(search("count(3, 6)")));
		assertEquals(list(3.0, 4.0, 5.0), execute(search("count(3, 6, 0)")));
		assertEquals(list(), execute(search("count(3, 3)")));
		assertEquals(list(), execute(search("count(3, 2)")));
		assertEquals(list(3.0), execute(search("count(3, 4)")));
		assertEquals(list(3.0, 7.0, 11.0), execute(search("count(3, 15, 4)")));
		assertEquals(list(11.0, 7.0, 3.0), execute(search("count(11, 2, -4)")));
	}

	public void testToList() throws ParseException {
		assertEquals(list(), execute(search("null.toList()")));
		assertEquals(list("a"), execute(search("'a'.toList()")));
		assertEquals(list("a"), execute(search("singleton('a').toList()")));
	}

	public void testSingletonUnion() throws ParseException {
		assertEquals(true, execute(search("union(list(1, 2, 2, 4, 3)) == list(1, 2, 3, 4).toSet()")));
	}

	public void testToSet() throws ParseException {
		assertEquals(set(), execute(search("null.toSet()")));
		assertEquals(set("a"), execute(search("'a'.toSet()")));
		assertEquals(set("a"), execute(search("list('a').toSet()")));
		assertEquals(set("a", "b", "c"), execute(search("list('a', 'b', 'c').toSet()")));
		assertEquals(set("a"), execute(search("list('a', 'a').toSet()")));
	}

	public void testEquals() throws ParseException {
		assertEquals(true, execute(search("'x' == 'x'")));
		assertEquals(false, execute(search("'x' == 'y'")));
		assertEquals(true, execute(search("null == null")));
		assertEquals(false, execute(search("null == 'x'")));
		assertEquals(false, execute(search("'x' == null")));
		assertEquals(true, execute(search("null == list()")));
		assertEquals(true, execute(search("list() == null")));
		assertEquals(false, execute(search("list('x') == null")));
		assertEquals(false, execute(search("null == list('x')")));
		assertEquals(true, execute(search("list('x') == 'x'")));
		assertEquals(false, execute(search("list('x') == 'y'")));
	}

	public void testRegexEquals() throws ParseException {
		assertEquals(true, execute(search("regex('x').regexSearch('x').singleElement().regexGroup() == 'x'")));
		assertEquals(true, execute(search("regex('x').regexSearch('x').regexGroup() == 'x'")));
		assertEquals(true, execute(search("regex('x').regexSearch('x') == list('x')")));
		assertEquals(true, execute(search("regex('x').regexSearch('x') == 'x'")));
		assertEquals(false, execute(search("regex('x').regexSearch('x') == 'y'")));
	}

	public void testRegex() throws ParseException {
		assertEquals(list("ab", "a", "a", "ab"),
			execute(search("regex('ab?').regexSearch('abcaxyaabcd').map(m -> $m.regexGroup())")));
		assertEquals(list(0.0, 2.0, 3.0, 4.0, 6.0, 7.0, 7.0, 9.0),
			execute(search(
				// -----------------------01234567890
				"regex('ab?').regexSearch('abcaxyaabcd').map(m -> list($m.regexStart(), $m.regexEnd())).flatten()")));
		assertEquals(list("xxb", null, "xb"),
			execute(search("regex('a(x*b)?').regexSearch('axxbccaxcaxb').map(m -> $m.regexGroup(1))")));
		assertEquals(list("abc", "ac", "acx"),
			execute(search("l -> $l.filter(x -> regex('ab?c').regexSearch($x))"), list("ab", "abc", "ac", "acx")));
		assertEquals(list("abc", "ac"),
			execute(search("l -> $l.filter(x -> regex('ab?c').regexSearch($x) == $x)"),
				list("ab", "abc", "ac", "acx")));
		assertEquals(list("ab"),
			execute(search("l -> $l.filter(x -> !(regex('ab?c').regexSearch($x)))"), list("ab", "abc", "ac")));
		assertEquals("x_y_z",
			execute(search("regex('a(b)?c').regexReplace('xacyabcz', '_')")));
		assertEquals("x__y_bb_z",
			execute(search("regex('a(b+)?c').regexReplace('xacyabbcz', '_$1_')")));
		assertEquals("x-y+bb+z",
			execute(
				search("regex('a(b+)?c').regexReplace('xacyabbcz', m -> if($m.regexGroup(1) == null, '-', '+$1+'))")));
		assertEquals("XXX3YYY",
			execute(search("regex('a(b+)').regexReplace('XXXabbbYYY', m -> $m.regexGroup(1).length())")));
	}

	public void testRegexNull() throws ParseException {
		assertNull(execute(search("regex('foo').regexSearch(null)")));
		assertNull(execute(search("regex('foo').regexReplace(null, 'bar')")));
		assertNull(execute(search("null.regexGroup()")));
		assertNull(execute(search("null.regexStart()")));
		assertNull(execute(search("null.regexEnd()")));
	}

	public void testRegexNotNull() throws ParseException {
		try {
			execute(search("regex(null).regexSearch('foobar')"));
			fail("Must deliver failure.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_UNEXPECTED_NULL_VALUE__EXPR, ex.getErrorKey().plain());
		}
	}

	public void testIsCompatible() throws ParseException {
		assertTrue((Boolean) execute(search("`TestSearchExpression:A`.isCompatible(`TestSearchExpression:A`)")));
		assertTrue((Boolean) execute(search("`TestSearchExpression:R`.isCompatible(`TestSearchExpression:S`)")));
		assertFalse((Boolean) execute(search("`TestSearchExpression:S`.isCompatible(`TestSearchExpression:R`)")));
	}

	public void testConstantFolding() throws ParseException {
		try {
			compile(search("list('a', 'b').singleElement()"));
			fail("Must fail due to constant folding.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_MORE_THAN_A_SINGLE_ELEMENT__VAL_EXPR, ex.getErrorKey().plain());
		}

		SearchExpression toStringExpression = compileExpr(search("toString(#('foo'@de, 'bar'@en))"));
		assertInstanceof(toStringExpression, ToString.class);
		SearchExpression literalExpression = compileExpr(search("toString('foo' + 'bar')"));
		assertInstanceof(literalExpression, Literal.class);
		assertEquals("foobar", interpret(kb(), model(), literalExpression).execute());
	}

	public void testInternationalize() throws ParseException {
		assertTrue((Boolean) execute(search(
			"{'en': 'Hello world!', 'de': 'Hallo Welt!'}.internationalize() == #('Hello world!'@en, 'Hallo Welt!'@de)")));
	}

	public void testResolveAlias() throws ParseException {
		String alias1 = "%TEST.COM.TOP_LOGIC.MODEL.SEARCH.EXPR.TESTSEARCHEXPRESSION__RESOLVE_ALIAS1%";
		String resolvedAlias1 = "resolved value 1";
		assertEquals("Setup of alias incorrect.", resolvedAlias1, AliasManager.getInstance().replace(alias1));
		String alias2 = "%TEST.COM.TOP_LOGIC.MODEL.SEARCH.EXPR.TESTSEARCHEXPRESSION__RESOLVE_ALIAS2%";
		String resolvedAlias2 = "resolved value 2";
		assertEquals("Setup of alias incorrect.", resolvedAlias2, AliasManager.getInstance().replace(alias2));

		assertEquals(resolvedAlias1, eval("resolveAlias('" + alias1 + "')"));
		assertEquals(resolvedAlias2, eval("resolveAlias('" + alias2 + "')"));
		assertEquals("ab" + resolvedAlias2 + resolvedAlias1 + "cd",
			eval("resolveAlias('ab" + alias2 + alias1 + "cd')"));

		assertEquals("NoAlias", eval("resolveAlias('NoAlias')"));
		assertEquals("", eval("resolveAlias('')"));
	}

	@FunctionalInterface
	interface TestFun {
		void accept(XMLInstanceImporter scenario) throws Exception;
	}

	public void testEnumLiteralAccess() throws ParseException {
		SearchExpression search = search("`TestSearchExpression:MyEnum#A`.singleton()");
		Set<?> result = asSet(executeAsSet(search));
		assertEquals(1, result.size());
		assertEquals(TLModelUtil.findPart("TestSearchExpression:MyEnum#A"), result.iterator().next());
	}

	/** Test for {@link Label}. */
	public void testLabel() throws ParseException {
		// The test uses floating point numbers as objects, as this is easiest to test.
		assertLabel("1.1", "1.1", "en");
		assertLabel("1,1", "1.1", "de");
	}

	private void assertLabel(String expected, String object, String locale) throws ParseException {
		SearchExpression labelExpression = search("label(" + object + ", \"" + locale + "\")");
		Object label = execute(labelExpression);
		assertEquals(expected, label);
	}

	private void with(String scenarioName, TestFun test) {
		try {
			XMLInstanceImporter scenario = scenario(scenarioName);
			Throwable outer = null;
			try {
				test.accept(scenario);

				// Roll back any implicitly started (and potentially failed) transactions.
				PersistencyLayer.getKnowledgeBase().rollback();
			} catch (Throwable e1) {
				outer = e1;
				throw e1;
			} finally {
				try {
					drop(scenario);
				} catch (Throwable e2) {
					if (outer != null) {
						outer.addSuppressed(e2);
					} else {
						throw e2;
					}
				}
			}
		} catch (I18NRuntimeException ex) {
			// Ensure that the message can be read (it may require a valid thread context to be
			// rendered). A valid thread context is not available at the time the exception is
			// displayed by JUnit.
			throw new RuntimeException(Resources.getInstance().getString(ex.getErrorKey()), ex);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private void drop(XMLInstanceImporter scenario) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			for (TLObject x : scenario.getAllImportedObjects()) {
				x.tDelete();
			}
			tx.commit();
		}
	}

	private Object value(TLObject obj, String name) {
		return obj.tValue(((TLClass) obj.tType()).getPart(name));
	}

	public static Test suite() {
		return suite(TestSearchExpression.class, SafeHTML.Module.INSTANCE);
	}

}
