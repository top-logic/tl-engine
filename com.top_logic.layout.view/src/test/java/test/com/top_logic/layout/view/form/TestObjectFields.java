/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.ObjectFieldsControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the {@link ObjectFieldsControl}: the fields of the object a single-valued reference of the
 * form's object points to, edited in the edit session of the form.
 */
@SuppressWarnings("javadoc")
public class TestObjectFields extends AbstractModelAccessTest {

	private static final String OBJECT_FIELDS_MODULE = "TestViewObjectFields";

	private static final String HOLDER = OBJECT_FIELDS_MODULE + ":Holder";

	private static final String DETAIL_TYPE = OBJECT_FIELDS_MODULE + ":Detail";

	/** The forward reference of a holder to its detail. */
	private static final String DETAIL = "detail";

	/** A forward reference of a holder that is read-only by its visibility. */
	private static final String FIXED_DETAIL = "fixedDetail";

	/** A forward reference of a holder that is hidden by its visibility. */
	private static final String HIDDEN_DETAIL = "hiddenDetail";

	/** The backward reference of a detail to its holder, the inverse of {@link #DETAIL}. */
	private static final String HOLDER_REF = "holder";

	private ReactContext _reactContext;

	private RecordingScope _scope;

	private TLObject _holder;

	private TLObject _detail;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_scope = new RecordingScope();
		_holder = newObject(HOLDER, "holder");
		_detail = newObject(DETAIL_TYPE, "detail");
		store(_holder, DETAIL, _detail);
	}

	/**
	 * The tests are about the edit session, not about the access rights.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_scope = null;
		_holder = null;
		_detail = null;
		super.tearDown();
	}

	/**
	 * Values entered for the form's object and for the referenced object are stored by one save.
	 */
	public void testSaveStoresBoth() {
		FormControl form = newForm(_holder);
		AttributeFieldControl holderName = field(form, NAME);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertFalse(nested.isHidden());
		assertSame(_detail, nested.getCurrentObject());

		assertTrue(form.enterEditMode());
		assertTrue(nested.isEditMode());
		assertTrue(nested.getCurrentObject() instanceof TLObjectOverlay);

		setValue(holderName, "new holder");
		setValue(detailName, "new detail");
		assertEquals("Not stored before the save.", "detail", _detail.tValueByName(NAME));

		form.executeSave();

		assertFalse(form.isEditMode());
		assertFalse(nested.isEditMode());
		assertEquals("new holder", _holder.tValueByName(NAME));
		assertEquals("new detail", _detail.tValueByName(NAME));
		assertSame(_detail, nested.getCurrentObject());
	}

	/**
	 * A change of the referenced object alone makes the form dirty, and is stored.
	 */
	public void testNestedChangeMakesFormDirty() {
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		assertFalse(form.isDirty());

		setValue(detailName, "changed");
		assertTrue(form.isDirty());
		assertTrue(nested.isDirty());

		form.executeSave();
		assertEquals("changed", _detail.tValueByName(NAME));
		assertFalse(form.isDirty());
	}

	/**
	 * Storing the form state without leaving the edit mode starts a fresh edit session of the
	 * referenced object as well.
	 */
	public void testApplyKeepsEditing() {
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		setValue(detailName, "first");
		form.executeApply();
		assertEquals("first", _detail.tValueByName(NAME));
		assertTrue(nested.isEditMode());
		assertFalse(form.isDirty());

		setValue(detailName, "second");
		assertTrue(form.isDirty());
		form.executeSave();
		assertEquals("second", _detail.tValueByName(NAME));
	}

	/**
	 * A validation error of the referenced object refuses the save, and neither object is stored.
	 */
	public void testNestedValidationErrorBlocksSave() {
		FormControl form = newForm(_holder);
		AttributeFieldControl holderName = field(form, NAME);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		setValue(holderName, "new holder");
		setValue(detailName, null);

		try {
			form.executeSave();
			fail("A missing mandatory value of the referenced object must refuse the save.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS,
				ex.getErrorKey());
		}

		assertTrue(form.isEditMode());
		assertEquals("holder", _holder.tValueByName(NAME));
		assertEquals("detail", _detail.tValueByName(NAME));
	}

	/**
	 * Cancelling the edit discards the changes of both objects.
	 */
	public void testCancelDiscardsBoth() {
		FormControl form = newForm(_holder);
		AttributeFieldControl holderName = field(form, NAME);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		setValue(holderName, "new holder");
		setValue(detailName, "new detail");

		form.executeCancel();

		assertFalse(form.isEditMode());
		assertFalse(form.isDirty());
		assertEquals("holder", _holder.tValueByName(NAME));
		assertEquals("detail", _detail.tValueByName(NAME));
		assertSame(_detail, nested.getCurrentObject());
		assertEquals("detail", fieldValue(detailName));

		assertTrue(form.enterEditMode());
		assertFalse("A new edit session starts without the discarded changes.", form.isDirty());
		assertEquals("detail", fieldValue(detailName));
	}

	/**
	 * Without a referenced object nothing is displayed, and nothing is edited.
	 */
	public void testNoReferencedObject() {
		TLObject empty = newObject(HOLDER, "empty");
		FormControl form = newForm(empty);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		field(nested, NAME);

		assertTrue(nested.isHidden());
		assertNull(nested.getCurrentObject());

		assertTrue(form.enterEditMode());
		assertTrue(nested.isHidden());
		assertFalse(nested.isEditMode());
		assertFalse(form.isDirty());
		form.executeSave();
	}

	/**
	 * A reference that is read-only by its visibility displays the referenced object, but does
	 * not edit it.
	 */
	public void testReadOnlyReference() {
		store(_holder, FIXED_DETAIL, _detail);
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, FIXED_DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		assertFalse(nested.isHidden());
		assertFalse(nested.isEditMode());
		assertSame(_detail, nested.getCurrentObject());
		assertFalse(isEditable(detailName));
	}

	/**
	 * A configured read-only display does not edit the referenced object.
	 */
	public void testReadOnlyConfigured() {
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = new ObjectFieldsControl(_reactContext, form, DETAIL, true, _scope);
		AttributeFieldControl detailName = field(nested, NAME);

		assertTrue(form.enterEditMode());
		assertFalse(nested.isEditMode());
		assertFalse(isEditable(detailName));
	}

	/**
	 * A reference that is hidden by its visibility displays nothing.
	 */
	public void testHiddenReference() {
		store(_holder, HIDDEN_DETAIL, _detail);
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, HIDDEN_DETAIL);

		assertTrue(nested.isHidden());
		assertTrue(form.enterEditMode());
		assertTrue(nested.isHidden());
		assertNull(nested.getCurrentObject());
	}

	/**
	 * The object of a backward reference is edited and stored like that of a forward reference.
	 */
	public void testBackwardReference() {
		FormControl form = newForm(_detail);
		AttributeFieldControl detailName = field(form, NAME);
		ObjectFieldsControl nested = objectFields(form, HOLDER_REF);
		AttributeFieldControl holderName = field(nested, NAME);

		assertSame(_holder, nested.getCurrentObject());

		assertTrue(form.enterEditMode());
		assertTrue(nested.isEditMode());
		assertTrue(isEditable(holderName));
		setValue(detailName, "new detail");
		setValue(holderName, "new holder");

		form.executeSave();
		assertEquals("new detail", _detail.tValueByName(NAME));
		assertEquals("new holder", _holder.tValueByName(NAME));
	}

	/**
	 * A referenced object created outside the form - here: an object of a backward reference - is
	 * displayed as soon as its creation is reported, and is gone after its deletion is reported.
	 */
	public void testFollowsReferenceChangedOutside() {
		TLObject lonely = newObject(DETAIL_TYPE, "lonely");
		FormControl form = newForm(lonely);
		ObjectFieldsControl nested = objectFields(form, HOLDER_REF);
		AttributeFieldControl holderName = field(nested, NAME);
		form.attach();
		nested.attach();
		assertTrue(nested.isHidden());

		TLObject holder = newObject(HOLDER, "created outside");
		store(holder, DETAIL, lonely);
		_scope.reportCreation(holder);

		assertFalse(nested.isHidden());
		assertSame(holder, nested.getCurrentObject());
		assertEquals("created outside", fieldValue(holderName));

		TLStructuredType holderType = holder.tType();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			holder.tDelete();
			tx.commit();
		}
		_scope.reportDeletion(holder, holderType);
		assertTrue(nested.isHidden());
		assertNull(nested.getCurrentObject());
	}

	/**
	 * A change of the referenced object stored outside the form is displayed in view mode.
	 */
	public void testFollowsStoredChangeOfReferencedObject() {
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		AttributeFieldControl detailName = field(nested, NAME);
		form.attach();
		nested.attach();

		store(_detail, NAME, "stored outside");
		_scope.reportUpdate(_detail);

		assertEquals("stored outside", fieldValue(detailName));
	}

	/**
	 * A constraint check of the form's object sees the value entered for the referenced object.
	 */
	public void testOverlayVisibleToForm() {
		FormControl form = newForm(_holder);
		ObjectFieldsControl nested = objectFields(form, DETAIL);
		assertTrue(form.enterEditMode());

		assertSame(nested.getCurrentObject(), form.getValidationModel().getExistingOverlay(_detail));
	}

	private FormControl newForm(TLObject object) {
		FormControl result = new FormControl(_reactContext, object, "no model", NoTokenHandling.INSTANCE);
		result.setModelScope(_scope);
		return result;
	}

	private ObjectFieldsControl objectFields(FormModel form, String reference) {
		return new ObjectFieldsControl(_reactContext, form, reference, false, _scope);
	}

	private AttributeFieldControl field(FormModel form, String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(_reactContext, form, form.getFormControl(), attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	private void store(TLObject object, String attribute, Object value) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			object.tUpdateByName(attribute, value);
			tx.commit();
		}
	}

	private static AbstractFieldModel fieldModel(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	/**
	 * Enters the given value into the input of the given field, as the user does.
	 */
	private static void setValue(AttributeFieldControl field, Object value) {
		fieldModel(field).setValue(value);
	}

	private static Object fieldValue(AttributeFieldControl field) {
		return fieldModel(field).getValue();
	}

	private static boolean isEditable(AttributeFieldControl field) {
		return fieldModel(field).isEditable();
	}

	/**
	 * The test suite, starting the services building the fields and the scripting the model
	 * computes with.
	 */
	public static Test suite() {
		return suiteWith(TestObjectFields.class, TypeIndex.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
