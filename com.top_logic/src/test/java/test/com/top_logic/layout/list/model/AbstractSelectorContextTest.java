/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ListModel;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.selection.SelectorContext;
import com.top_logic.layout.provider.DefaultLabelProvider;

/**
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public abstract class AbstractSelectorContextTest extends AbstractListModelTest {

	protected static final int OPTIONS_PER_PAGE = 10;
	private static final int OPTION_COUNT = Integer.parseInt("22222", 3);

	private ListField optionList;
	private StringField patternField;
//	private SelectField optionPages;
//	private ListField selectionList;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		List allOptions = new ArrayList(OPTION_COUNT);
		for (int n = 0; n < OPTION_COUNT; n++) {
			allOptions.add(Integer.toString(n, 3));
		}
		// Sort in alphabetical order.
		Collections.sort(allOptions);
		
		SelectField targetSelectField = FormFactory.newSelectField("source", allOptions, true, false);
		targetSelectField.setOptionLabelProvider(DefaultLabelProvider.INSTANCE);
		SelectorContext selectorContext = createFormContext(targetSelectField);
		
		optionList = selectorContext.getOptionList();
//		selectionList = selectorContext.getSelectionList();
		patternField = selectorContext.getPattern();
//		optionPages = selectorContext.getOptionPages();
	}

	protected abstract SelectorContext createFormContext(SelectField targetSelectField);
	
	public void testPattern() {
		patternField.setValue("10");
		ListModel list = optionList.getListModel();
		assertEquals("10", list.getElementAt(0));
		assertEquals("100", list.getElementAt(1));
		assertEquals("1000", list.getElementAt(2));
		assertEquals("10000", list.getElementAt(3));
		assertEquals("10001", list.getElementAt(4));
		assertEquals(10, list.getSize());
		
		patternField.setValue("1022");
		assertEquals("1022", list.getElementAt(0));
		assertEquals("10220", list.getElementAt(1));
		assertEquals("10221", list.getElementAt(2));
		assertEquals("10222", list.getElementAt(3));
		assertEquals("11022", list.getElementAt(4));
		assertEquals("21022", list.getElementAt(5));
		assertEquals(6, list.getSize());
		
		patternField.setValue("10221");
		assertEquals("10221", list.getElementAt(0));
		assertEquals(1, list.getSize());
		
		patternField.setValue("1");
		// The page stays, where the last option as selected (unique option
		// matching filter criterion).
//		assertEquals("1", list.getElementAt(0));
//		assertEquals("10", list.getElementAt(1));
//		assertEquals("100", list.getElementAt(2));
//		assertEquals("1000", list.getElementAt(3));
//		assertEquals("10000", list.getElementAt(4));
//		assertEquals("10001", list.getElementAt(5));
		assertEquals(10, list.getSize());
		
		patternField.setValue("X");
		assertEquals(0, list.getSize());

		patternField.getFormContext().checkAll();
		assertTrue(patternField.hasWarnings());
	}

	/**
	 * Returns the {@link SelectField} containing the option pages of the given
	 * {@link SelectorContext}.
	 */
	public static SelectField getOptionPages(SelectorContext selectorContext) {
		return ReflectionUtils.getValue(selectorContext, "optionPages", SelectField.class);
	}
}
