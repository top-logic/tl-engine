/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.constraints;

import static com.top_logic.layout.form.constraints.OptionsBasedSelectionConstraint.*;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.OptionsBasedSelectionConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.util.Resources;

/**
 * Test cases for {@link OptionsBasedSelectionConstraint}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestOptionsBasedSelectionConstraint extends BasicTestCase {

	private SelectField field;
	private OptionsBasedSelectionConstraint constraint;

	@Override
	protected void setUp() throws Exception {
		field = createSelectField();
		constraint = createConstraint(field);
	}

	private SelectField createSelectField() {
		SelectField selectField =
			FormFactory.newSelectField("selectControlMultiInvalidSelection",
				Arrays.asList(new String[] { "a", "b", "c", "d" }), true, false);
		return selectField;
	}

	private OptionsBasedSelectionConstraint createConstraint(SelectField field) {
		return new OptionsBasedSelectionConstraint(field);
	}

	public void testAcceptFullyOptionsBasedSingleSelection() {
		assertConstraintPermission("b");
	}

	public void testAcceptFullyOptionsBasedMultiSelection() {
		assertConstraintPermission("a", "b", "c");
	}

	public void testAcceptNullValue() {
		assertConstraintPermission((String[]) null);
	}

	public void testAcceptEmptySelection() {
		assertConstraintPermission(new String[] {});
	}

	public void testDenyNonOptionsBasedSingleSelection() {
		String invalidSelection = "e";
		String errorMessage = Resources.getInstance().getMessage(SMALL_INVALID_SELECTION_MESSAGE, invalidSelection);
		assertConstraintDenial(errorMessage, invalidSelection);
	}

	public void testDenyNonOptionsBasedMultiSelection() {
		String[] invalidSelection = { "ab", "e", "f" };
		String invalidSelectionOutput =
			getInvalidSelectionOutput(invalidSelection);
		String errorMessage =
			Resources.getInstance().getMessage(SMALL_INVALID_SELECTION_MESSAGE, invalidSelectionOutput);
		assertConstraintDenial(errorMessage, invalidSelection);
	}

	public void testMassiveDenyNonOptionsBasedMultiSelection() {
		String[] invalidSelection = { "ab", "e", "f", "g", "h", "i", "j", "k", "l" };
		String invalidSelectionOutput =
			getInvalidSelectionOutput(invalidSelection);
		String errorMessage =
			Resources.getInstance().getMessage(BIG_INVALID_SELECTION_MESSAGE, invalidSelectionOutput, 4);
		assertConstraintDenial(errorMessage, invalidSelection);
	}

	private String getInvalidSelectionOutput(String[] invalidSelection) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < invalidSelection.length && i < MAX_SHOWN_INVALID_SELECTION_ITEMS; i++) {
			if (i > 0) {
				output.append(", ");
			}

			output.append(invalidSelection[i]);
		}
		
		return output.toString();
	}

	private void assertConstraintPermission(String... selection) {
		try {
			List<?> selectionToCheck;
			if (selection == null) {
				selectionToCheck = null;
			} else {
				selectionToCheck = Arrays.asList(selection);
			}
			assertTrue("The constraint was not be able to check the selection '" + selectionToCheck + "'!",
				constraint.check(selectionToCheck));
		} catch (Exception ex) {
			fail("Current valid options are '" + field.getOptions() + "'. The constraint should accept the selection '"
				+ selection + "' but threw an exception instead!", ex);
		}

	}

	private void assertConstraintDenial(String errorMessage, String... selection) {
		try {
			constraint.check(Arrays.asList(selection));

			fail("Current valid options are '" + field.getOptions()
				+ "'. The constraint has not denied invalid selection '" + Arrays.asList(selection) + "'!");
		} catch (Exception ex) {
			assertInstanceof(ex, CheckException.class);
			assertEquals(errorMessage, ex.getMessage());
		}
	}

	/**
	 * the test suite
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestOptionsBasedSelectionConstraint.class,
			LabelProviderService.Module.INSTANCE));
	}
}
