/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import static com.top_logic.layout.form.model.SelectFieldUtils.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;

/**
 * Test case for {@link SelectFieldUtils}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestSelectFieldUtils extends BasicTestCase {

	private SelectField field;

	@Override
	protected void setUp() throws Exception {
		field = createSelectField();
	}

	private SelectField createSelectField() {
		SelectField selectField =
			FormFactory.newSelectField("selectControlMultiInvalidSelection",
				Arrays.asList(new String[] { "d", "c", "b", "a" }), true, false);
		return selectField;
	}
	
	public void testGetOrderedOptions() {
		List<?> expected = Arrays.asList(new String[] { "a", "b", "c", "d" });
		assertEquals("Options are not in order!", expected,
			SelectFieldUtils.sortCopy(field.getOptionComparator(), field.getOptions()));
	}
	
	public void testGetOrderedSelection() {
		field.setAsSelection(Arrays.asList(new String[] { "d", "c" }));

		List<?> expected = Arrays.asList(new String[] { "c", "d" });
		assertEquals("Selection is not in order!", expected,
			SelectFieldUtils.sortCopy(field.getOptionComparator(), field.getSelection()));
	}

	public void testGetOptionsAndSelectionOuterJoinOrdered() {
		field.setAsSelection(Arrays.asList(new String[] { "d", "c", "ba", "e", "f", "0", "1" }));

		List<?> expected = Arrays.asList(new String[] { "0", "1", "a", "b", "ba", "c", "d", "e", "f" });
		assertEquals("Joined list of options and selection does not contain all elements or is not in order!",
			expected, getOptionAndSelectionOuterJoinOrdered(field));
	}

	/**
	 * the test suite
	 */
	public static Test suite() {
		TestSuite theSuite = new TestSuite(TestSelectFieldUtils.class);
		return TLTestSetup.createTLTestSetup(theSuite);
	}

}
