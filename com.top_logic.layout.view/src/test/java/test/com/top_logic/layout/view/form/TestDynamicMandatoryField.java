/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLDynamicVisibility;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a form field whose {@link TLDynamicVisibility} makes it mandatory is validated as a
 * mandatory attribute: an empty value is an error of the field and refuses the save, as long as
 * the mode requires a value.
 *
 * <p>
 * The error of a field the user has not touched yet is shown once the form is to be saved; the
 * tests show it right away.
 * </p>
 */
public class TestDynamicMandatoryField extends AbstractModelAccessTest {

	/** Name of the {@link #STEP} attribute naming the mode of the {@link #REMARK}. */
	private static final String STATE = "state";

	/** Name of the {@link #STEP} attribute whose dynamic visibility is its {@link #STATE}. */
	private static final String REMARK = "remark";

	/** The mode requiring a value. */
	private static final String MANDATORY = "mandatory";

	private TLObject _step;

	private TLObject _item;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_item = DynamicModelService.getFactoryFor(MODULE).createObject(type(ITEM));
			_item.tUpdateByName(NAME, "item");
			_step.tUpdateByName(ITEMS, List.of(_item));
			_project.tUpdateByName(STEPS, List.of(_step));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_step = null;
		_item = null;
		super.tearDown();
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	/**
	 * An empty value of a field its mode makes mandatory is an error and refuses the save; a value
	 * entered clears the error.
	 */
	public void testDynamicMandatoryRefusesEmptyValue() {
		FormControl form = newForm(_step);
		AttributeFieldControl state = field(form, STATE);
		AttributeFieldControl remark = field(form, REMARK);
		assertTrue(form.enterEditMode());
		// As after an attempt to save: the errors of the fields are shown.
		model(remark).setRevealed(true);
		assertFalse("No mode, no requirement.", model(remark).hasError());

		model(state).setValue(MANDATORY);
		assertTrue("The mode requires a value.", model(remark).hasError());
		assertRefused(form);

		model(remark).setValue("entered");
		assertFalse(model(remark).hasError());
		form.validateOrThrow();
	}

	/**
	 * A mode that requires no value clears the error of the empty field again.
	 */
	public void testNonMandatoryModeClearsError() {
		FormControl form = newForm(_step);
		AttributeFieldControl state = field(form, STATE);
		AttributeFieldControl remark = field(form, REMARK);
		assertTrue(form.enterEditMode());
		model(remark).setRevealed(true);

		model(state).setValue(MANDATORY);
		assertTrue(model(remark).hasError());

		for (String mode : List.of("read-only", "disabled", "hidden", "editable", "default")) {
			model(state).setValue(mode);
			assertFalse("No value required in mode " + mode + ".", model(remark).hasError());
			model(state).setValue(MANDATORY);
			assertTrue(model(remark).hasError());
		}

		model(state).setValue(null);
		assertFalse(model(remark).hasError());
		form.validateOrThrow();
	}

	/**
	 * A statically mandatory attribute without a mode is still required, an attribute that is
	 * neither mandatory nor has a mode is not.
	 */
	public void testStaticMandatory() {
		FormControl itemForm = newForm(_item);
		AttributeFieldControl name = field(itemForm, NAME);
		assertTrue(itemForm.enterEditMode());
		model(name).setValue(null);
		assertTrue("A statically mandatory attribute requires a value.", model(name).hasError());
		assertRefused(itemForm);

		FormControl stepForm = newForm(_step);
		AttributeFieldControl state = field(stepForm, STATE);
		assertTrue(stepForm.enterEditMode());
		model(state).setValue(null);
		assertFalse(model(state).hasError());
		stepForm.validateOrThrow();
	}

	private static void assertRefused(FormControl form) {
		try {
			form.validateOrThrow();
			fail("A form with an empty mandatory field must be refused.");
		} catch (TopLogicException ex) {
			assertEquals(com.top_logic.layout.view.command.I18NConstants.ERROR_FORM_HAS_VALIDATION_ERRORS,
				ex.getErrorKey());
		}
	}

	private FormControl newForm(TLObject object) {
		return TLContextManager.inPersonContext(_responsible,
			() -> new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")), object, "no model", NoTokenHandling.INSTANCE));
	}

	private static AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(form.getReactContext(), form, form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	private static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	/**
	 * The test suite, starting the services the fields are built with.
	 */
	public static Test suite() {
		return suiteWith(TestDynamicMandatoryField.class, ResourcesModule.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE);
	}

}
