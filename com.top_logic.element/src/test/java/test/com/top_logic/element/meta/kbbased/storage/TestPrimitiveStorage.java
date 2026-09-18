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
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for the storage of multi-valued primitive attributes in
 * {@link PrimitiveStorage}.
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

	private TLObject _node;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> _node = DynamicModelService.getInstance().createObject(getNodeType()));
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
