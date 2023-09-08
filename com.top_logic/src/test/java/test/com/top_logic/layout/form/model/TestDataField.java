/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link TestCase} for {@link DataField}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestDataField extends BasicTestCase {

	private DataField _dataField;

	@Override
	protected void setUp() throws Exception {
		_dataField = FormFactory.newDataField("testDataField");
	}

	@SuppressWarnings("javadoc")
	public void testFileNameConstraintViolation() {
		_dataField.setFileNameConstraint(DenyAllFileNameConstraint.INSTANCE);
		assertExceptionForInvalidFilename("testFileName");

	}

	@SuppressWarnings("javadoc")
	public void testSubsequentFileNameConstraintViolation() {
		_dataField.setFileNameConstraint(DenyAllFileNameConstraint.INSTANCE);
		assertExceptionForInvalidFilename("testFileName1");

		try {
			assertExceptionForInvalidFilename("testFileName2");
		} catch (AssertionError error) {
			fail("Must not throw assertion, when subsequent check of filename violates defined filename constraint");
		}

	}

	@SuppressWarnings("javadoc")
	public void testNoValueListenerForEqualValueSet() {
		_dataField.setValue(null);
		_dataField.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				fail("Value listener must not be triggered for setting equal value!");
			}
		});
		_dataField.setValue(null);
	}

	@SuppressWarnings("javadoc")
	public void testSetValueResetsFieldState() {
		String errorMessage = "Field is invalid!";
		_dataField.setError(errorMessage);

		assertTrue("Data field must be in error state after error has been set!", _dataField.hasError());
		assertEquals("Data field must contain error message, that has been announced!", errorMessage,
			_dataField.getError());
		assertFalse("Data field must be invalid, if it is in error state!", _dataField.isValid());

		_dataField.setValue(null);

		assertFalse("Data field must be in error state after error has been set!", _dataField.hasError());
		try {
			_dataField.getError();
			fail("Error of data field must be retrievable, if field is not in error state!");
		} catch (IllegalStateException ex) {
			// Expected
		}
		assertTrue("Data field must be valid, if it is in initial state!", _dataField.isValid());
	}

	private void assertExceptionForInvalidFilename(String fileName) {
		try {
			_dataField.checkFileName(fileName);
			fail("File name constraint must throw check exception for invalid file name '" + fileName + "'.");
		} catch (CheckException ex) {
			assertEquals(DenyAllFileNameConstraint.INSTANCE.CONTRAINT_ERROR_MESSAGE, ex.getMessage());
		}
	}

	public static Test suite() {
		TestSuite testSuite = new TestSuite(TestDataField.class);
		return TLTestSetup.createTLTestSetup(testSuite);
	}

	private static class DenyAllFileNameConstraint extends AbstractConstraint {
		
		final String CONTRAINT_ERROR_MESSAGE = "File name not allowed!";

		static final DenyAllFileNameConstraint INSTANCE = new DenyAllFileNameConstraint();

		@Override
		public boolean check(Object value) throws CheckException {
			throw new CheckException(CONTRAINT_ERROR_MESSAGE);
		}

	}

}
