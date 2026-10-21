/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased.storage;

import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Arrays.*;

import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for the storage of primitive attributes in {@link PrimitiveStorage}, and for
 * transient objects reading the same values as persistent ones.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestPrimitiveStorage extends BasicTestCase {

	private static final Class<TestPrimitiveStorage> THIS_CLASS = TestPrimitiveStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String TYPE_NAME = "Node";

	private static final String DATES_ATTRIBUTE = "dates";

	private static final String NUMBERS_ATTRIBUTE = "numbers";

	private static final String FLAGS_ATTRIBUTE = "flags";

	private static final String NAMES_ATTRIBUTE = "names";

	private static final String BLOBS_ATTRIBUTE = "blobs";

	private static final String FLAG_ATTRIBUTE = "flag";

	private static final String TRISTATE_ATTRIBUTE = "tristate";

	private static final String NUMBER_ATTRIBUTE = "number";

	private TLObject _node;

	private TLObject _transientNode;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> _node = DynamicModelService.getInstance().createObject(getNodeType()));
		_transientNode = TransientObjectFactory.INSTANCE.createObject(getNodeType());
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> _node.tDelete());
		super.tearDown();
	}

	public void testDates() {
		Date first = new Date(1234567890123L);
		Date second = new Date(987654321987L);

		store(DATES_ATTRIBUTE, asList(first, second));

		assertEquals("[1234567890123,987654321987]", storedValue(DATES_ATTRIBUTE));
		List<?> loaded = load(DATES_ATTRIBUTE);
		assertEquals(asList(first, second), loaded);
		assertEquals(Date.class, loaded.get(0).getClass());
		assertEquals(1234567890123L, ((Date) loaded.get(0)).getTime());
	}

	public void testNullElement() {
		Date first = new Date(1234567890123L);
		Date second = new Date(987654321987L);

		store(DATES_ATTRIBUTE, asList(first, null, second));

		assertEquals("[1234567890123,null,987654321987]", storedValue(DATES_ATTRIBUTE));
		assertEquals(asList(first, null, second), load(DATES_ATTRIBUTE));
	}

	public void testLargeNumbers() {
		Long large = Long.valueOf(Long.MAX_VALUE - 1);
		Long small = Long.valueOf(Long.MIN_VALUE + 1);

		store(NUMBERS_ATTRIBUTE, asList(large, small));

		assertEquals("[" + large + "," + small + "]", storedValue(NUMBERS_ATTRIBUTE));
		assertEquals(asList(large, small), load(NUMBERS_ATTRIBUTE));
	}

	/**
	 * Tests that a number stored with a fractional part is still read as number value.
	 */
	public void testFractionalNumberValue() {
		inTransaction(() -> _node.tSetDataString(NUMBERS_ATTRIBUTE, "[1.0,42.0]"));

		assertEquals(asList(Long.valueOf(1), Long.valueOf(42)), load(NUMBERS_ATTRIBUTE));
	}

	public void testBooleans() {
		store(FLAGS_ATTRIBUTE, asList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE));

		assertEquals("[true,false,true]", storedValue(FLAGS_ATTRIBUTE));
		assertEquals(asList(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE), load(FLAGS_ATTRIBUTE));
	}

	public void testStrings() {
		store(NAMES_ATTRIBUTE, asList("foo", "42", ""));

		assertEquals("[\"foo\",\"42\",\"\"]", storedValue(NAMES_ATTRIBUTE));
		assertEquals(asList("foo", "42", ""), load(NAMES_ATTRIBUTE));
	}

	public void testUnsupportedStorageType() {
		try {
			store(BLOBS_ATTRIBUTE, asList(BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 })));
			fail("A binary value cannot be stored in a multi-valued attribute.");
		} catch (TopLogicException ex) {
			Throwable cause = rootCause(ex);
			assertEquals(IllegalArgumentException.class, cause.getClass());
			assertContains("only numbers, booleans, strings and dates are supported", cause.getMessage());
		}
	}

	public void testUnsetValues() {
		assertEquals(Boolean.FALSE, _node.tValueByName(FLAG_ATTRIBUTE));
		assertEquals(Boolean.FALSE, _transientNode.tValueByName(FLAG_ATTRIBUTE));

		assertNull(_node.tValueByName(TRISTATE_ATTRIBUTE));
		assertNull(_transientNode.tValueByName(TRISTATE_ATTRIBUTE));

		assertNull(_node.tValueByName(NUMBER_ATTRIBUTE));
		assertNull(_transientNode.tValueByName(NUMBER_ATTRIBUTE));
	}

	public void testBooleanValue() {
		storeBoth(FLAG_ATTRIBUTE, Boolean.TRUE);
		assertEquals(Boolean.TRUE, _node.tValueByName(FLAG_ATTRIBUTE));
		assertEquals(Boolean.TRUE, _transientNode.tValueByName(FLAG_ATTRIBUTE));

		storeBoth(FLAG_ATTRIBUTE, null);
		assertEquals(Boolean.FALSE, _node.tValueByName(FLAG_ATTRIBUTE));
		assertEquals(Boolean.FALSE, _transientNode.tValueByName(FLAG_ATTRIBUTE));
	}

	public void testTristateValue() {
		storeBoth(TRISTATE_ATTRIBUTE, Boolean.FALSE);
		assertEquals(Boolean.FALSE, _node.tValueByName(TRISTATE_ATTRIBUTE));
		assertEquals(Boolean.FALSE, _transientNode.tValueByName(TRISTATE_ATTRIBUTE));

		storeBoth(TRISTATE_ATTRIBUTE, null);
		assertNull(_node.tValueByName(TRISTATE_ATTRIBUTE));
		assertNull(_transientNode.tValueByName(TRISTATE_ATTRIBUTE));
	}

	public void testNumberNormalization() {
		storeBoth(NUMBER_ATTRIBUTE, Integer.valueOf(42));
		assertNormalizedLong(_node.tValueByName(NUMBER_ATTRIBUTE));
		assertNormalizedLong(_transientNode.tValueByName(NUMBER_ATTRIBUTE));
	}

	public void testNumbersNormalization() {
		storeBoth(NUMBERS_ATTRIBUTE, asList(Integer.valueOf(42), Long.valueOf(13)));
		assertNormalizedLongs((List<?>) _node.tValueByName(NUMBERS_ATTRIBUTE));
		assertNormalizedLongs((List<?>) _transientNode.tValueByName(NUMBERS_ATTRIBUTE));
	}

	public void testIncompatibleNumber() {
		try {
			inTransaction(() -> _node.tUpdateByName(NUMBER_ATTRIBUTE, "42"));
			fail("A string cannot be stored in a long attribute of a persistent object.");
		} catch (TopLogicException ex) {
			assertIncompatibleValue(ex.getCause());
		}
		try {
			_transientNode.tUpdateByName(NUMBER_ATTRIBUTE, "42");
			fail("A string cannot be stored in a long attribute of a transient object.");
		} catch (IllegalArgumentException ex) {
			assertIncompatibleValue(ex);
		}
		assertNull(_node.tValueByName(NUMBER_ATTRIBUTE));
		assertNull(_transientNode.tValueByName(NUMBER_ATTRIBUTE));
	}

	public void testIncompatibleNumbers() {
		try {
			inTransaction(() -> _node.tUpdateByName(NUMBERS_ATTRIBUTE, asList(Long.valueOf(1), "42")));
			fail("A string cannot be stored in a multiple long attribute of a persistent object.");
		} catch (TopLogicException ex) {
			assertIncompatibleValue(ex.getCause());
		}
		try {
			_transientNode.tUpdateByName(NUMBERS_ATTRIBUTE, asList(Long.valueOf(1), "42"));
			fail("A string cannot be stored in a multiple long attribute of a transient object.");
		} catch (IllegalArgumentException ex) {
			assertIncompatibleValue(ex);
		}
		assertEquals(List.of(), _node.tValueByName(NUMBERS_ATTRIBUTE));
		assertEquals(List.of(), _transientNode.tValueByName(NUMBERS_ATTRIBUTE));
	}

	private static void assertIncompatibleValue(Throwable problem) {
		assertEquals(IllegalArgumentException.class, problem.getClass());
		assertContains("Value '42' (java.lang.String) can not be converted", problem.getMessage());
	}

	private static void assertNormalizedLong(Object value) {
		assertEquals(Long.valueOf(42), value);
		assertEquals(Long.class, value.getClass());
	}

	private static void assertNormalizedLongs(List<?> values) {
		assertEquals(asList(Long.valueOf(42), Long.valueOf(13)), values);
		for (Object value : values) {
			assertEquals(Long.class, value.getClass());
		}
	}

	private void storeBoth(String attributeName, Object value) {
		inTransaction(() -> _node.tUpdateByName(attributeName, value));
		_transientNode.tUpdateByName(attributeName, value);
	}

	private Throwable rootCause(Throwable problem) {
		Throwable result = problem;
		while (result.getCause() != null) {
			result = result.getCause();
		}
		return result;
	}

	private void store(String attributeName, List<?> values) {
		inTransaction(() -> _node.tUpdateByName(attributeName, values));
	}

	private List<?> load(String attributeName) {
		return (List<?>) _node.tValueByName(attributeName);
	}

	private String storedValue(String attributeName) {
		return (String) _node.tGetData(attributeName);
	}

	private TLClass getNodeType() {
		return (TLClass) getModule().getType(TYPE_NAME);
	}

	private TLModule getModule() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		Test customConfigSetup = createCustomConfigSetup(THIS_CLASS, kbSetup);
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

	private static Test createCustomConfigSetup(Class<?> testClass, Test innerSetup) {
		String configFileName = testClass.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(testClass, configFileName);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(innerSetup, createFilePath, true));
	}

}
