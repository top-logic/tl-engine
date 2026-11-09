/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.service.KBTestMeta;

import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.JsonValueCodec;
import com.top_logic.model.util.JsonValueCodec.Result;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link JsonValueCodec}.
 */
@SuppressWarnings("javadoc")
public class TestJsonValueCodec extends BasicTestCase {

	private static final String MODULE_NAME = "test.JsonValueCodec";

	private TLModule _module;

	private TLEnumeration _enum;

	private TLClassifier _red;

	private TLClassifier _green;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE);
		_module = TLModelUtil.addModule(ModelService.getApplicationModel(), MODULE_NAME);
		_enum = TLModelUtil.addEnumeration(_module, "Color");
		_red = TLModelUtil.addClassifier(_enum, "red");
		_green = TLModelUtil.addClassifier(_enum, "green");
		tx.commit();
	}

	@Override
	protected void tearDown() throws Exception {
		Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE);
		TLModelUtil.deleteRecursive(_module);
		tx.commit();

		_module = null;
		_enum = null;
		_red = null;
		_green = null;

		super.tearDown();
	}

	public void testNull() {
		Result encoded = JsonValueCodec.encode(null);
		assertTrue(encoded.isPresent());
		assertNull(encoded.get());

		Result decoded = JsonValueCodec.decode(null);
		assertTrue(decoded.isPresent());
		assertNull(decoded.get());
		assertNull(decoded.orElse("fallback"));
	}

	public void testScalars() {
		assertRoundTrip("Hello");
		assertRoundTrip(Boolean.TRUE);
		assertRoundTrip(Long.valueOf(42));
		assertRoundTrip(Double.valueOf(3.25));

		assertEquals("Hello", JsonValueCodec.encode("Hello").get());
		assertEquals(Long.valueOf(42), JsonValueCodec.encode(Long.valueOf(42)).get());
	}

	public void testDate() {
		Date date = new Date(1_700_000_000_123L);
		assertRoundTrip(date);
		assertEquals(tagged(JsonValueCodec.DATE, "2023-11-14T22:13:20.123Z"), JsonValueCodec.encode(date).get());

		Timestamp timestamp = new Timestamp(date.getTime());
		assertEquals(date, decode(encode(timestamp)));
	}

	public void testPart() {
		assertRoundTrip(_red);
		assertRoundTrip(_enum);
		assertEquals(tagged(JsonValueCodec.PART, TLModelUtil.qualifiedName(_red)), encode(_red));
	}

	public void testObject() {
		TLObject object = newObject();
		assertRoundTrip(object);
		assertEquals(tagged(JsonValueCodec.OBJECT, object.tId().asString()), encode(object));
	}

	public void testList() {
		List<Object> list = Arrays.asList("a", Long.valueOf(1), _red, null);
		assertRoundTrip(list);
		assertEquals(Arrays.asList("a", Long.valueOf(1), tagged(JsonValueCodec.PART, TLModelUtil.qualifiedName(_red)),
			null), encode(list));
	}

	public void testSet() {
		Set<Object> set = new LinkedHashSet<>(Arrays.asList(_green, _red, "x"));
		Object decoded = decode(encode(set));
		assertTrue(decoded instanceof Set);
		assertEquals(new ArrayList<>(set), new ArrayList<>((Set<?>) decoded));

		assertEquals(tagged(JsonValueCodec.SET, Arrays.asList(
			tagged(JsonValueCodec.PART, TLModelUtil.qualifiedName(_green)),
			tagged(JsonValueCodec.PART, TLModelUtil.qualifiedName(_red)),
			"x")), encode(set));
	}

	public void testMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", "n");
		map.put("color", _red);
		map.put("none", null);
		assertRoundTrip(map);

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("name", "n");
		payload.put("color", tagged(JsonValueCodec.PART, TLModelUtil.qualifiedName(_red)));
		payload.put("none", null);
		assertEquals(tagged(JsonValueCodec.MAP, payload), encode(map));
	}

	public void testNested() {
		TLObject object = newObject();
		Map<String, Object> inner = new LinkedHashMap<>();
		inner.put("when", new Date(1000));
		inner.put("colors", new LinkedHashSet<>(Arrays.asList(_red, _green)));
		Map<String, Object> outer = new LinkedHashMap<>();
		outer.put("items", Arrays.asList(object, inner, Arrays.asList(Long.valueOf(1), "two")));
		outer.put("flag", Boolean.FALSE);

		assertRoundTrip(outer);
	}

	public void testDeletedObject() {
		TLObject object = newObject();
		Object stored = encode(object);

		Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE);
		object.tDelete();
		tx.commit();

		assertFalse(JsonValueCodec.decode(stored).isPresent());
		assertEquals("fallback", JsonValueCodec.decode(stored).orElse("fallback"));
		assertEquals(Arrays.asList("a", "b"), decode(Arrays.asList("a", stored, "b")));
	}

	public void testRemovedClassifier() {
		Object stored = encode(_green);

		Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE);
		TLModelUtil.deleteRecursive(_green);
		tx.commit();

		assertFalse(JsonValueCodec.decode(stored).isPresent());
		assertEquals(Arrays.asList(_red), decode(Arrays.asList(encode(_red), stored)));

		Map<String, Object> map = new LinkedHashMap<>();
		map.put("red", encode(_red));
		map.put("green", stored);
		Map<String, Object> expected = new LinkedHashMap<>();
		expected.put("red", _red);
		assertEquals(expected, decode(tagged(JsonValueCodec.MAP, map)));
	}

	public void testUnknownPartName() {
		assertFalse(JsonValueCodec.decode(tagged(JsonValueCodec.PART, MODULE_NAME + ":NoSuchType")).isPresent());
		assertFalse(JsonValueCodec.decode(tagged(JsonValueCodec.PART, "noSuchModule:NoSuchType#x")).isPresent());
	}

	public void testUnknownType() {
		assertFalse(JsonValueCodec.decode(tagged("noSuchKind", "x")).isPresent());

		Map<String, Object> untagged = new HashMap<>();
		untagged.put(JsonValueCodec.VALUE, "x");
		assertFalse(JsonValueCodec.decode(untagged).isPresent());

		assertFalse(JsonValueCodec.decode(new HashMap<>()).isPresent());
		assertFalse(JsonValueCodec.decode(tagged(JsonValueCodec.DATE, "no date")).isPresent());
		assertFalse(JsonValueCodec.decode(tagged(JsonValueCodec.OBJECT, "no key")).isPresent());
	}

	public void testNotStorable() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLClass type = TLModelUtil.addClass(TLModelUtil.addModule(model, "transient"), "Element");
		TLObject transientObject = TransientObjectFactory.INSTANCE.createObject(type);
		assertFalse(JsonValueCodec.encode(transientObject).isPresent());

		Map<Object, Object> nonStringKey = new HashMap<>();
		nonStringKey.put(Long.valueOf(1), "one");
		assertFalse(JsonValueCodec.encode(nonStringKey).isPresent());

		assertFalse(JsonValueCodec.encode(new StringBuilder("x")).isPresent());
		assertFalse(JsonValueCodec.encode(Arrays.asList("a", new StringBuilder("x"))).isPresent());
		assertFalse(JsonValueCodec.encode(Set.of(new Object())).isPresent());

		Map<String, Object> nested = new HashMap<>();
		nested.put("bad", transientObject);
		assertFalse(JsonValueCodec.encode(nested).isPresent());
	}

	private TLObject newObject() {
		Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE);
		TLObject result = kb().createKnowledgeObject(KBTestMeta.TEST_B).getWrapper();
		tx.commit();
		return result;
	}

	private static void assertRoundTrip(Object value) {
		assertEquals(value, decode(encode(value)));
	}

	private static Object encode(Object value) {
		Result result = JsonValueCodec.encode(value);
		assertTrue("Not storable: " + value, result.isPresent());
		return result.get();
	}

	private static Object decode(Object json) {
		Result result = JsonValueCodec.decode(json);
		assertTrue("Not resolved: " + json, result.isPresent());
		return result.get();
	}

	private static Map<String, Object> tagged(String type, Object value) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(JsonValueCodec.TYPE, type);
		result.put(JsonValueCodec.VALUE, value);
		return result;
	}

	private static KnowledgeBase kb() {
		return KBSetup.getKnowledgeBase();
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		TestFactory f = new TestFactory() {
			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				Test innerTest = DefaultTestFactory.INSTANCE.createSuite(testCase, suiteName);
				return PersonManagerSetup.createPurePersonManagerSetup(
					ServiceTestSetup.createSetup(innerTest, ModelService.Module.INSTANCE));
			}
		};
		return KBSetup.getSingleKBTest(TestJsonValueCodec.class, f);
	}

}
