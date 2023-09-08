/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.model;

import static com.top_logic.layout.form.model.SelectFieldUtils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ControlFlowAssertion;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.model.OptionsListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeModel;
import com.top_logic.layout.tree.model.DefaultMutableTLTreeNode;

/**
 * Test case for {@link SelectField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSelectField extends BasicTestCase {

	private static final String ITEM_4_LABEL = "Item 4";
	private static final String ITEM_3_LABEL = "Item 3";
	private static final String ITEM_2_LABEL = "Item 2";
	private static final String ITEM_1_LABEL = "Item 1";
	
	private static final Object ITEM_1 = new NamedConstant("item1");
	private static final Object ITEM_2 = new NamedConstant("item2");
	private static final Object ITEM_3 = new NamedConstant("item3");
	private static final Object ITEM_4 = new NamedConstant("item4");
	
	private static final List SELECTION    = Arrays.asList(new Object[] {ITEM_1, ITEM_2});
	private static final List OPTIONS      = Arrays.asList(new Object[] {ITEM_1, ITEM_2, ITEM_3});
	private static final List MORE_OPTIONS = Arrays.asList(new Object[] {ITEM_1, ITEM_2, ITEM_3, ITEM_4});

	private static final LabelProvider LABEL_PROVIDER = new LabelProvider() {
		@Override
		public String getLabel(Object object) {
			if (object == ITEM_1) return ITEM_1_LABEL;
			if (object == ITEM_2) return ITEM_2_LABEL;
			if (object == ITEM_3) return ITEM_3_LABEL;
			if (object == ITEM_4) return ITEM_4_LABEL;
			
			throw new AssertionFailedError("Unreachable.");
		}};

	final static Mapping SELECT_ID_MAPPING = new Mapping() {
		@Override
		public Object map(Object input) {
			return ((Map) input).get(SelectField.ID_KEY);
		}
	};

	final static Mapping SELECT_LABEL_MAPPING = new Mapping() {
		@Override
		public Object map(Object input) {
			return ((Map) input).get(SelectField.LABEL_KEY);
		}
	};
	
	private FormContext context;
	private SelectField selectField;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupField();
	}

	private void setupField() {
		context = new FormContext("noname", ResPrefix.forTest("i18n"));

		selectField = createSelectField(SelectField.MULTIPLE);
		
		context.addMember(selectField);
	}

	private SelectField createSelectField(boolean multipleSelection) {
		SelectField selectField = FormFactory.newSelectField("noname", OPTIONS, multipleSelection, false);
		selectField.setOptionLabelProvider(LABEL_PROVIDER);
		return selectField;
	}

	public void testNormalizedInitialValue() {
		FormField field = FormFactory.newSelectField("name", Collections.singletonList("value"));

		final BooleanFlag eventReceived = new BooleanFlag(false);
		field.addValueListener(new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				eventReceived.set(true);
			}
		});

		field.setValue(field.getValue());
		assertFalse("Ticket #6809: setting the same value again must not trigger event", eventReceived.get());
	}

	public void testLazyOptionsPropertyValueCreation() {
		final ControlFlowAssertion listenerCalled = new ControlFlowAssertion();
		
		OptionsListener nonValueTouchingPropertyListener = new OptionsListener() {
			@Override
			public Bubble handleOptionsChanged(SelectField sender) {
				// This listener does not the new value of the property event.
				// This value passed to the listener is only a view of
				// the unaccessible lazy list (which represents a representation
				// of the options that can be marshaled to the client-side).
				// This representation is created on demand.
				listenerCalled.touch();
				return Bubble.BUBBLE;
			}
		};
		context.addListener(SelectField.OPTIONS_PROPERTY, nonValueTouchingPropertyListener);
		
		selectField.setOptions(newLazyUnaccessibleList());
		
		listenerCalled.assertReached("handlePropertyEvent() was called.");

		context.removeListener(SelectField.OPTIONS_PROPERTY, nonValueTouchingPropertyListener);

		listenerCalled.reset();
		
		selectField.setOptions(newLazyUnaccessibleList());

		listenerCalled.assertNotReached("handlePropertyEvent() was not called.");
	}
	
	public void testLazyOptionsAccess() {
		final ControlFlowAssertion afterAccessingSize = new ControlFlowAssertion();
		
		OptionsListener valueTouchingPropertyListener = new OptionsListener() {

			@Override
			public Bubble handleOptionsChanged(SelectField sender) {
				List clientSideViewOfOptions = sender.getOptions();

				int size = clientSideViewOfOptions.size();
				afterAccessingSize.touch();
				
				if (size > 0) {
					// Do something.
				}
				return Bubble.BUBBLE;
			}
		};
		
		context.addListener(SelectField.OPTIONS_PROPERTY, valueTouchingPropertyListener);
		
		
		final ControlFlowAssertion afterSettingOptions = new ControlFlowAssertion();
		
		try {
			selectField.setOptions(newLazyUnaccessibleList());
			afterSettingOptions.touch();
		} catch (AssertionError ex) {
			// expected.
		}
		
		afterAccessingSize.assertNotReached("Touching the unaccessible options list throws an exception.");
		afterSettingOptions.assertNotReached("Setting the options fails, because a value listener fails.");
	}

	public void testOptionIDGeneration() {
		selectField.setOptions(newLazyUnaccessibleList());
		
		// The select field does not check the selection against its options.
		// This is necessary to delay the access to the option list as long as
		// possible (because it might be extremely expensive to compute the list
		// of all possible options).
		selectField.setAsSelection(SELECTION);

		// The field now works as expected with the selection, but simply does
		// not access its list of options.
		String id1 = selectField.getOptionID(ITEM_1);
		String id2 = selectField.getOptionID(ITEM_2);

		assertEquals(ITEM_1, selectField.getOptionByID(id1));
		assertEquals(ITEM_2, selectField.getOptionByID(id2));
		try {
			selectField.getOptionByID("13");
			fail("Exception expected.");
		} catch (Exception ex) {
			assertInstanceof(ex, IllegalArgumentException.class);
		}
		try {
			selectField.getOptionByID("some-funny-id");
			fail("Exception expected.");
		} catch (Exception ex) {
			assertInstanceof(ex, IllegalArgumentException.class);
		}
		
		Set selectionSet = selectField.getSelectionSet();
		assertTrue(selectionSet.contains(ITEM_1));
		assertTrue(selectionSet.contains(ITEM_2));
		assertFalse(selectionSet.contains(ITEM_3));
		
		List selectionList = selectField.getSelection();
		assertTrue(selectionList.contains(ITEM_1));
		assertTrue(selectionList.contains(ITEM_2));
		assertFalse(selectionList.contains(ITEM_3));
		
		assertInIterator(ITEM_1, selectField.getSelectionIterator());
		assertInIterator(ITEM_2, selectField.getSelectionIterator());
		assertNotInIterator(ITEM_3, selectField.getSelectionIterator());

		assertEquals(ITEM_1_LABEL + SelectFieldUtils.getMultiSelectionSeparatorFormat(selectField) + ITEM_2_LABEL,
			getSelectionAsText(selectField));

		selectField.setAsSingleSelection(ITEM_2);

		assertEquals(ITEM_2_LABEL, getSelectionAsText(selectField));
	}
	
	public void testNoOptionLabel() {
		selectField.setAsSelection(Collections.EMPTY_LIST);
		selectField.setEmptyLabel("--");
		selectField.setEmptyLabelImmutable("nicht ausgewählt");
		
		selectField.setImmutable(true);
		assertEquals("nicht ausgewählt", getSelectionAsText(selectField));
		assertEquals("nicht ausgewählt", selectField.getOptionLabel(SelectField.NO_OPTION));
		assertEquals("", getSelectionAsTextPlain(selectField));
		
		selectField.setImmutable(false);
		// Note: For the construction of the selection text, only the immutable
		// label for the empty selection is used.
		assertEquals("nicht ausgewählt", getSelectionAsText(selectField));
		assertEquals("--", selectField.getOptionLabel(SelectField.NO_OPTION));
		assertEquals("", getSelectionAsTextPlain(selectField));
	}

	public void testSelectionAsTextNotTouchingOptions() {
		selectField.setOptions(newLazyUnaccessibleList());
		
		selectField.setValue(null);
		selectField.setImmutable(false);
		getSelectionAsTextPlain(selectField);
		getSelectionAsText(selectField);
		
		selectField.setImmutable(true);
		getSelectionAsTextPlain(selectField);
		getSelectionAsText(selectField);
	}
	
	public void testSetOptionsAsTreeInListBasedField() throws Exception {
		assertTrue("SelectField must have a list of options initially.", selectField.isOptionsList());
		
		selectField.setOptionsTree(createOptionsTreeModel());

		assertTrue("SelectField must have a options tree after setting tree model as option model.",
			selectField.isOptionsTree());
	}

	public void testSetOptionsListInTreeBasedField() throws Exception {
		selectField.setOptionsTree(createOptionsTreeModel());
		assertTrue("SelectField must have a options tree after setting tree model as option model.",
			selectField.isOptionsTree());

		selectField.setOptions(OPTIONS);
		assertTrue("SelectField must have a list of options after setting list model as option model.",
			selectField.isOptionsList());
	}

	public void testSuccessfulParseStringBasedSingleSelection() throws Exception {
		SelectField selectField = createSelectField(! SelectField.MULTIPLE);
		List<Object> expectedItems = new ArrayList<>(2);
		expectedItems.add(ITEM_3);

		Object parsedSelection = FormFieldInternals.parseRawValue(selectField, "Item 3");

		assertEquals(expectedItems, parsedSelection);
	}

	public void testFailingParseStringBasedSingleSelection() throws Exception {
		SelectField selectField = createSelectField(!SelectField.MULTIPLE);
		String nonExistentOption = "Item 4";

		try {
			FormFieldInternals.parseRawValue(selectField, nonExistentOption);
			fail("Non-existent selected option should raise CheckException!");
		} catch (CheckException ex) {
			// Works as designed
		}
	}

	public void testRetrievalOfInvalidSelectionMustNotProvokeFeedbackLoop() throws Exception {
		selectField.setFixedOptions(Collections.singletonList(ITEM_1));
		try {
			selectField.update("abc");
			selectField.getSelection();
		} catch (StackOverflowError ex) {
			fail("Retrieval of invalid selection must not provoke feedback loop");
		}
	}

	public void testSuccessfulParseStringBasedMultiSelection() throws Exception {
		List<Object> expectedItems = new ArrayList<>(2);
		expectedItems.add(ITEM_2);
		expectedItems.add(ITEM_3);

		Object parsedSelection =
			FormFieldInternals
				.parseRawValue(selectField, "Item 2" + getMultiSelectionSeparator(selectField) + " Item 3");

		assertEquals(expectedItems, parsedSelection);
	}

	public void testFailingParseStringBasedMultiSelection() throws Exception {
		String nonExistentOption = "Item 4";
		try {
			FormFieldInternals.parseRawValue(selectField, "Item 3" + getMultiSelectionSeparator(selectField)
				+ nonExistentOption);
			fail("Non-existent selected option should raise CheckException!");
		} catch (CheckException ex) {
			// Works as designed
		}
	}

	public void testQuoteSpecialRegExCharacters() throws Exception {
		assertQuotedRegExString("+", "?", "*", "(", ")", "[", "]");
	}

	public void testSuccessfulParseStringBasedMultiSelectionSpecialCharacterSeparator() throws Exception {
		List<Object> expectedItems = new ArrayList<>(2);
		expectedItems.add(ITEM_2);
		expectedItems.add(ITEM_3);

		String specialCharacter = "+";
		setMultiSelectionSeparator(selectField, specialCharacter, specialCharacter);
		Object parsedSelection =
			FormFieldInternals
				.parseRawValue(selectField, "Item 2" + getMultiSelectionSeparator(selectField) + " Item 3");

		assertEquals(expectedItems, parsedSelection);
	}

	public void testSetValidMultiSelectionSeparator() throws Exception {
		setMultiSelectionSeparator(selectField, ":", " : ");
	}

	public void testErrorSetInvalidMultiSelectionSeparator() throws Exception {
		String invalidSeparator1 = ":#";
		String invalidSeparator2 = ": ";
		assertErrorInvalidSeparator(invalidSeparator1, invalidSeparator2);

		String validSeparator = ":";
		String invalidSeparatorFormat1 = " ,";
		String invalidSeparatorFormat2 = " :#";
		assertErrorInvalidSeparatorFormat(validSeparator, invalidSeparatorFormat1, invalidSeparatorFormat2);
	}

	private void assertErrorInvalidSeparatorFormat(String separator, String... invalidSeparatorFormats) {
		for (String invalidSeparatorFormat : invalidSeparatorFormats) {
			try {
				setMultiSelectionSeparator(selectField, separator, invalidSeparatorFormat);
				fail("Invalid separator format must fail test!");
			} catch (IllegalArgumentException ex) {
				String expectedMessage =
					"Separator format of multiple selection must only contain separator '" + separator
					+ "' and optional whitespace, but is: '" + invalidSeparatorFormat + "'.";
				assertEquals(expectedMessage, ex.getMessage());
			}
		}
	}

	private void assertErrorInvalidSeparator(String... invalidSeparators) {
		for (String invalidSeparator : invalidSeparators) {
			try {
				setMultiSelectionSeparator(selectField, invalidSeparator, invalidSeparator);
				fail("Invalid separator must fail test!");
			} catch (IllegalArgumentException ex) {
				String expectedMessage =
					"Separator of multiple selection must be a single character, but is '"
						+ invalidSeparator
						+ "'.";
				assertEquals(expectedMessage, ex.getMessage());
			}
		}
	}

	private void assertQuotedRegExString(String... specialCharacters) {
		for (String specialCharacter : specialCharacters) {
			setMultiSelectionSeparator(selectField, specialCharacter, specialCharacter);
			assertEquals(Pattern.quote(specialCharacter), getQuotedMultiSelectionSeparator(selectField));
		}
	}

	private DefaultMutableTLTreeModel createOptionsTreeModel() {
		DefaultMutableTLTreeModel treeModel = new DefaultMutableTLTreeModel();
		DefaultMutableTLTreeNode rootNode = treeModel.getRoot();
		rootNode.setBusinessObject(ITEM_1);
		rootNode.createChild(ITEM_2);
		rootNode.createChild(ITEM_3);
		rootNode.createChild(ITEM_4);
		return treeModel;
	}

	private List newLazyUnaccessibleList() {
		return new LazyListUnmodifyable() {
			@Override
			protected List initInstance() {
				throw new AssertionError("Unreachable, must not access the field options.");
			}
		};
	}
	
	private static String getSelectionAsText(SelectField selectField) {
		try {
			return writeSelectionAsTextImmutable(new StringBuilder(), selectField).toString();
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder does not throw IOException.", ex);
		}
	}

	private static String getSelectionAsTextPlain(SelectField selectField) {
		try {
			return writeSelectionAsTextEditable(new StringBuilder(), selectField).toString();
		} catch (IOException ex) {
			throw new UnreachableAssertion("StringBuilder does not throw IOException.", ex);
		}
	}

    public static Test suite () {
        TestSuite theSuite = new TestSuite(TestSelectField.class);
        return TLTestSetup.createTLTestSetup(theSuite);
    }
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
