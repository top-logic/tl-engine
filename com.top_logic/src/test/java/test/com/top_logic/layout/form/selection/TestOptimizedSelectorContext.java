/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.list.model.AbstractSelectorContextTest;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.OptimizedSelectorContext;
import com.top_logic.layout.form.selection.SelectorContext;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * Tests for {@link OptimizedSelectorContext}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestOptimizedSelectorContext extends BasicTestCase {

	private SelectField selectField;
	private LabelProvider optionsLabelProvider;
	private OptimizedSelectorContext context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		selectField = createSelectField();
		createLabelProvider();
		createOptimizedSelectorContext(selectField);
	}

	private SelectField createSelectField() {
		String[] options = getValidOptions();
		return FormFactory.newSelectField("selectField", Arrays.asList(options));
	}

	private String[] getValidOptions() {
		return new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l" };
	}

	private void createLabelProvider() {
		optionsLabelProvider = new LabelProvider() {

			@Override
			public String getLabel(Object object) {
				return (String) object;
			}
		};
	}

	private void createOptimizedSelectorContext(SelectField selectField) {
		context = createOptimizedSelectorContext(selectField, 500);
	}

	private OptimizedSelectorContext createOptimizedSelectorContext(SelectField selectField, int optionsPerPage) {
		return new OptimizedSelectorContext(selectField, optionsLabelProvider, optionsPerPage, Command.DO_NOTHING, true);
	}

	public void testInvalidSearchPatternShouldNotCauseContextError() {
		createInvalidPatternFieldEntry();

		assertTrue("Invalid pattern entry should not provoke error in selector context.", context.checkAll());
	}

	public void testInvalidSelectionDoesNotPreventCreationOfOptimizedSelectorContextWithSelectionContraints() {
		setInvalidSelection();
		setFixedOptions();

		assertCreationOfOptimizedSelectorContext();
	}

	private void setInvalidSelection() {
		String invalidSelection = "z";
		selectField.setAsSingleSelection(invalidSelection);
	}

	private void setFixedOptions() {
		selectField.setFixedOptions(new Filter<String>() {

			@Override
			public boolean accept(String anObject) {
				return !anObject.equals("c");
			}
		});
	}

	public void testInvalidSelectionIsAddedToOptionListInSingleSelectionMode() {
		setInvalidSelection();

		createOptimizedSelectorContext(selectField);

		assertEquals("The invalid selection has not been added to option list!", getValidOptions().length + 1, context
			.getOptionList().getListModel().getSize());
	}

	public void testEmptySelectField() {
		ArrayList<Object> emptyOptions = new ArrayList<>();
		SelectField emptyField = FormFactory.newSelectField("emptyField", emptyOptions);
		
		OptimizedSelectorContext selectorContext =
			new OptimizedSelectorContext(emptyField, optionsLabelProvider, SelectorContext.ALL_OPTIONS_ON_ONE_PAGE,
				Command.DO_NOTHING, true);
		SelectField optionPages = AbstractSelectorContextTest.getOptionPages(selectorContext);
		assertEquals("Empty options must result in page with one element.", 1, optionPages.getOptions().size());
	}

	public void testIllegalOptionsPerPage() {
		try {
			assertTrue("This test needs options.", selectField.getOptions().size() > 0);
			int noOptionsPerPage = 0;
			SelectorContext selectorContext =
				new OptimizedSelectorContext(selectField, optionsLabelProvider, noOptionsPerPage, Command.DO_NOTHING,
					true);
			assertNotNull(selectorContext);
			fail("Zero options per page and not empty option list results in infinite many pages.");
		} catch (IllegalArgumentException ex) {
			// expected
		}
	}
	
	public void testEmptySelectionDoesNotPreventCreationOfOptimizedSelectorContext() {
		selectField.setOptionComparator(new NonNullProofedStringComparator());

		assertCreationOfOptimizedSelectorContext();
	}

	public void testAddOptionsToOptionListOfCurrentPageOnly() throws Exception {
		OptimizedSelectorContext selectorContext = createTestSelectorContext();
		selectorContext.removeAllFromSelection();
		assertEquals("Option list of current page does not contain all valid options or valid options only", 3,
			selectorContext.getOptionList().getListModel().getSize());
	}

	private OptimizedSelectorContext createTestSelectorContext() {
		SelectField selectField = createTestSelectField();
		OptimizedSelectorContext selectorContext = createTestSelectorContext(selectField);
		return selectorContext;
	}

	private OptimizedSelectorContext createTestSelectorContext(SelectField selectField) {
		OptimizedSelectorContext selectorContext = createOptimizedSelectorContext(selectField, 3);
		setOptionsPageToSecondPage(selectorContext);
		return selectorContext;
	}

	private void setOptionsPageToSecondPage(OptimizedSelectorContext selectorContext) {
		SelectField optionPages = AbstractSelectorContextTest.getOptionPages(selectorContext);
		optionPages.setAsSingleSelection(optionPages.getOptions().get(1));
	}

	private SelectField createTestSelectField() {
		SelectField selectField =
			FormFactory.newSelectField("testField", Arrays.asList(getValidOptions()), true, false);
		List<?> selection = Arrays.asList(new String[] { "a", "b", "e" });
		selectField.setAsSelection(selection);
		return selectField;
	}

	private void assertCreationOfOptimizedSelectorContext() {
		try {
			createOptimizedSelectorContext(selectField);
		} catch (Exception ex) {
			fail("OptimizedSelectorContext could not be created!", ex);
		}
	}

	private void createInvalidPatternFieldEntry() {
		context.getPattern().setValue("1");
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestOptimizedSelectorContext.class, LabelProviderService.Module.INSTANCE));
	}

	static class NonNullProofedStringComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			return o1.compareTo(o2);
		}

	}
}
